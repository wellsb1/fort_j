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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.forty11.j.api.Classes;

/**
 * 
 * @author Wells Burke
 *
 */
public class ClassesIt implements Iterator<String>, Iterable<String>
{
   String  next    = null;
   boolean started = false;

   List    stack   = new ArrayList();

   /**
    * A list of strings that have been returned alread so they should not 
    * be returned a second time, and a list of jar/zip file names that 
    * have been examined so that duplicats jar/zips at different paths
    * can be skipped
    */
   List    done    = new ArrayList();
   
   Set ignoredDirs = new HashSet(); 

   /**
    * Finds all classes in the java.class.path
    */
   public ClassesIt()
   {
      ignoredDirs.add(file("."));
      ignoredDirs.add(file(System.getProperty("user.home")));
      ignoredDirs.add(file("/"));
      
      //File workingDir = file(".");
      List<String> paths = Classes.getClassPath();

      Set<String> jarNames = new HashSet();

      for (String path : paths)
      {
         path = path.trim();
         if (path.length() == 0)
            continue;

         try
         {
            File f = file(path);
            if (f.exists() && !f.isHidden() && !stack.contains(f))
            {
               String name = f.getName();
               if (f.isFile() && name.endsWith(".jar") || name.endsWith(".zip"))
               {
                  if (!jarNames.contains(name))
                  {
                     jarNames.add(name);
                     stack.add(new Dir(null, f));
                  }
               }
               else if (!f.isFile() && !ignoredDirs.contains(f))
               {
                  //don't add an iterator for the working directory becuase it will most
                  //likely not be useful and may contain zillions of directories
                  stack.add(new Dir(f, f));
               }
            }
         }
         catch (Exception ex)
         {
            System.err.println(ex);
         }
      }
   }

   protected String findNext()
   {
      while (stack.size() > 0)
      {
         Object next = stack.remove(0);

         if (next instanceof String)
         {
            String name = (String) next;
            if (name.endsWith(".class") && !done.contains(name))
            {
               done.add(name);
               name = name.substring(0, name.length() - 6);
               name = name.replace('/', '.');

               if (name.startsWith("."))
               {
                  name = name.substring(1, name.length());
               }

               return name;
            }
         }
         else
         {
            Dir dir = (Dir) next;
            File base = dir.base;
            File file = dir.file;

            String name = file.getName();

            if (file.isDirectory())
            {
               File[] children = file.listFiles();
               for (int i = 0; children != null && i < children.length; i++)
               {
                  File child = file(children[i]);
                  if (!child.isHidden() && !done.contains(child))
                  {
                     done.add(child);
                     stack.add(new Dir(base, child));
                  }
               }
            }
            else if (name.endsWith(".class"))
            {
               String className = file.toString();
               String baseName = base.toString();
               className = className.substring(baseName.length(), className.length());
               stack.add(className);
            }
            else if (name.endsWith(".jar") || name.endsWith(".zip"))
            {
               try
               {
                  ZipFile zf = new ZipFile(file);
                  Enumeration en = zf.entries();

                  while (en.hasMoreElements())
                  {
                     ZipEntry e = (ZipEntry) en.nextElement();
                     if (!e.isDirectory())
                     {
                        String eName = e.getName();
                        if (eName.endsWith(".class") && !done.contains(eName))
                        {
                           stack.add(eName);
                        }
                     }
                  }
                  zf.close();
               }
               catch (Exception ex)
               {
                  System.err.println(ex.getMessage());
               }
            }
         }
      }

      return null;
   }

   class Dir
   {
      File base = null;
      File file = null;

      public Dir(File base, File file)
      {
         this.base = base;
         this.file = file;
      }
   }

   /* 
   +------------------------------------------------------------------------------+
   | Iterator/Iterable interface support
   +------------------------------------------------------------------------------+
   */

   public Iterator<String> iterator()
   {
      if (!started)
      {
         return this;
      }
      else
      {
         throw new UnsupportedOperationException("Can't call iterator() after hasNext() has been called");
      }
   }

   public boolean hasNext()
   {
      started = true;
      if (next == null)
      {
         next = findNext();
      }
      return next != null;
   }

   public String next()
   {
      String temp = next;
      next = null;
      return temp;
   }

   public void remove()
   {
      throw new UnsupportedOperationException("remove() is not supported.  Subclasses should provide necessary implementation");
   }

   /* 
   +------------------------------------------------------------------------------+
   | Helpers
   +------------------------------------------------------------------------------+
   */

   protected File file(String name)
   {
      try
      {
         return new File(name).getCanonicalFile();
      }
      catch (Exception ex)
      {
         return null;

      }
   }

   protected File file(File file)
   {
      try
      {
         return file.getCanonicalFile();
      }
      catch (Exception ex)
      {
         return null;
      }
   }
}
