/*
 * Copyright 2008-2017 Wells Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.forty11.j.it;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import io.forty11.j.api.Files;
import io.forty11.j.api.Lang;
import io.forty11.j.api.PathSet;
import io.forty11.j.api.PathSet.Path;
import io.forty11.j.api.Paths;
import io.forty11.j.api.Strings;

/**
 * TODO: matching with ** performs extra matches.  As an optimization, 
 * add a shortcircuit * match against the ramaining chunk regex once ** is hit
 * 
 * TODO: cache the compiled regex Pattern objects
 * 
 * TODO: allow passing an optional base file to be used instead of the 
 * working dir for relative matches
 * 
 * TODO: add support for programmer directed search pruning based on Iterator.remove()
 * 
 * TODO: supercede class Ls
 */
/**
 * Supports file searching based on path.separator (or comma) separated  
 * list of patterns with simple wildcards.  Patterns that end in a '/'
 * will only return directories.  Patterns not ending in a '/' will only
 * return files.
 * <br>
 * <pre>
 * wildcards supported
 * ------------------------------
 * '*'  any zero or more characters in a file or directory name
 * '**' any number of directories
 * '?'  any one character
 * 
 * ex: find(*.xml,*.java:**&#47;*.css)
 * ex: find(../base/*&#47;classes/*.class)
 * ex: find(../base/**&#47;classes/*.class)
 * ex: find(../base/**&#47;etc/"star"/lib/*.jar)
 * </pre>
 * 
 * @param pattern - a path.separator (or comma) seperated list 
 *                   of expressions optionally containing wildcards
 * 
 * @return an iteraotr of <code>File</code> objects whose paths match
 *          one or more of the <code>pattern</code> expressions
 */
public class FindIt extends It<File>
{

   // Options ----------------

   protected boolean        followSymLinks = true;

   protected String         baseDir        = ".";

   protected boolean        includeHidden  = false;

   protected String[]       includes       = null;
   protected String[]       excludes       = null;

   //----------------

   private PathSet          paths          = null;

   private int              patternIndex   = 0;

   private int              chunkIndex     = -1;
   private Iterator<File>[] chunkStack     = null;

   public FindIt(String... includes)
   {
      setIncludes(includes);
   }

   protected PathSet getPaths()
   {
      if (this.paths == null)
      {
         paths = new PathSet();
         if (includes != null)
         {
            paths.setIncludes(includes);
         }
         if (excludes != null)
         {
            paths.setExcludes(excludes);
         }
      }
      return paths;
   }

   public void setPaths(PathSet paths)
   {
      this.paths = paths;
   }

   protected File findNext()
   {
      List<Path> includePaths = getPaths().getIncludes();
      int length = includePaths.size();

      File file = null;

      while (patternIndex < length)
      {
         if (chunkStack == null)
         {
            chunkStack = new Iterator[includePaths.get(patternIndex).getChunks().length];
            file = new File(baseDir);
         }

         if (file != null)
         {
            if (chunkIndex == chunkStack.length - 1)
            {
               return file;
            }
            else
            {
               chunkIndex += 1;

               String target = includePaths.get(patternIndex).getChunks()[chunkIndex];

               Iterator it = listFiles(file, target);
               chunkStack[chunkIndex] = it;
               file = null;
            }
         }
         else
         {
            Iterator<File> it = chunkStack[chunkIndex];
            while (chunkIndex > 0 && (it == null || !it.hasNext()))
            {
               it = chunkStack[--chunkIndex];
            }

            if (it != null && it.hasNext())
            {
               file = it.next();
            }
            else
            {
               chunkIndex = -1;
               patternIndex++;
               chunkStack = null;
            }
         }
      }

      return null;
   }

   public Iterator<File> listFiles(final File file, final String wildcard)
   {
      if (!Strings.isWildcard(wildcard))
      {
         final File child = wildcard.equals("/") ? new File("/") : new File(file, wildcard);

         if (child.exists())
         {
            return Arrays.asList(new File[]{child}).iterator();
         }
         else
         {
            // this was added as a fix for windows os (tcollins - 8-11-2008)
            File child2 = new File(wildcard);
            if (child2.exists())
            {
               return Arrays.asList(new File[]{child2}).iterator();
            }
            else
            {
               return Arrays.asList(new File[]{}).iterator();
            }
         }
      }
      else
      {
         final boolean recurse = "**".equals(wildcard);
         final boolean dirsOnly = wildcard != null && wildcard.endsWith("/");
         final Pattern regex = wildcard != null ? Pattern.compile(Strings.wildcardToRegex(wildcard)) : null;

         final List<File> start = new ArrayList();

         if (file.isDirectory())
         {
            File[] arr = file.listFiles();
            for (int i = 0; arr != null && i < arr.length; i++)
            {
               start.add(arr[i]);
            }
            Collections.sort(start);
         }
         else
         {
            start.add(file);
         }

         return new It<File>()
            {
               List<File> children = start;

               public File findNext()
               {
                  File file = null;

                  if (children.size() > 0)
                  {
                     do
                     {
                        try
                        {
                           file = children.remove(0);

                           if (file != null && (!includeHidden && file.isHidden()))
                           {
                              file = null;
                           }

                           if (file != null && !followSymLinks && Files.isLink(file))
                           {
                              file = null;
                           }

                           if (file != null && file.isDirectory() && recurse)
                           {
                              children.addAll(0, Lang.asList(Files.listFiles(file)));
                           }

                           if (file != null && dirsOnly && !file.isDirectory())
                           {
                              file = null;
                           }

                           if (file != null && regex != null && !regex.matcher(file.getName()).matches())
                           {
                              file = null;
                           }
                        }
                        catch (Exception ex)
                        {
                           file = null;
                           ex.printStackTrace();
                        }
                     }
                     while (children.size() > 0 && file == null);
                  }
                  return file;
               }
            };
      }
   }

   /* 
   +------------------------------------------------------------------------------+
   | Accessors
   +------------------------------------------------------------------------------+
   */

   public void setFollowSymLinks(boolean followSymLinks)
   {
      this.followSymLinks = followSymLinks;
   }

   public void setBaseDir(String baseDir)
   {
      if (baseDir == null || baseDir.length() == 0)
      {
         baseDir = ".";
      }

      this.baseDir = Paths.path(new File(baseDir));
   }

   public void setHidden(boolean includeHidden)
   {
      this.includeHidden = includeHidden;
   }

   public void setIncludes(String... includes)
   {
      this.includes = includes;
   }

   public void setExcludes(String... excludes)
   {
      this.excludes = excludes;
   }

}