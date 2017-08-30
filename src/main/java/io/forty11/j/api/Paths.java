/*
 * Copyright 2008-2017 Wells Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.forty11.j.api;

import io.forty11.j.it.CollectionIt;
import io.forty11.j.it.EnumIt;
import io.forty11.j.it.FileIt;
import io.forty11.j.it.PathIt;
import io.forty11.j.it.ZipIt;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipFile;

public class Paths
{
   //   public static String[][] replacements = new String[7][2];
   //   static
   //   {
   //      int i = -1;
   //      replacements[++i][0] = "\\";
   //      replacements[i][1] = "/";
   //
   //      replacements[++i][0] = "/**/**/";
   //      replacements[i][1] = "/**/";
   //
   //      replacements[++i][0] = "/**/*/";
   //      replacements[i][1] = "/**/";
   //
   //      replacements[++i][0] = "/*/**/";
   //      replacements[i][1] = "/**/";
   //
   //      replacements[++i][0] = "?*";
   //      replacements[i][1] = "*";
   //
   //      replacements[++i][0] = "*?";
   //      replacements[i][1] = "*";
   //
   //      replacements[++i][0] = "***";
   //      replacements[i][1] = "**";
   //   }

   @ApiMethod
   @Comment(value = "Attempts to normalize a file path reference removing redundant wild cards and normalizing to \"/\" as the separator")
   public static String path(String path)
   {
      if (path == null)
         return "";

      path = path.trim();

      if (path.length() == 0)
         return "";

      path = Strings.replace(path, "\\", "/");
      path = Strings.replace(path, "/**/**/", "/**/");
      path = Strings.replace(path, "/**/*/", "/**/");
      path = Strings.replace(path, "/*/**/", "/**/");
      path = Strings.replace(path, "?*", "*");
      path = Strings.replace(path, "*?", "*");
      path = Strings.replace(path, "***", "**");

      String[] segs = path.split("/");

      StringBuffer buff = path.charAt(0) == '/' ? new StringBuffer("/") : new StringBuffer("");
      for (int i = 0; i < segs.length; i++)
      {
         if (segs[i].length() > 0)
         {
            buff.append(segs[i]).append('/');
         }
      }

      String p = buff.toString();
      if (path.charAt(path.length() - 1) != '/')
      {
         p = p.substring(0, p.length() - 1);
      }

      return p;
   }

   @ApiMethod
   @Comment(value = "Returns a path string for file makeing sure that the string ends with a / if the file is a directory")
   public static String path(File file)
   {
      try
      {
         String path = path(file.getCanonicalPath());
         if (file.isDirectory() && path.charAt(path.length() - 1) != '/')
            path = path + '/';

         return path;
      }
      catch (Exception ex)
      {
         Lang.rethrow(ex);
      }
      return null;
   }

   @ApiMethod
   public static String path(Object obj)
   {
      return path(obj.toString());
   }

   @ApiMethod
   public static PathIt paths(URL url)
   {
      String str = url.toString();
      if (str.startsWith("jar:") || str.endsWith(".jar") || str.endsWith(".zip"))
      {
         return new PathIt(new ZipIt(url));
      }
      else if (str.startsWith("file:"))
      {
         File file = Files.file(url.toString());
         return new PathIt(new FileIt(file));
      }

      return new PathIt(new CollectionIt(Collections.EMPTY_LIST));
   }

   @ApiMethod
   public static PathIt paths(File dir)
   {
      return new PathIt(new FileIt(dir));
   }

   @ApiMethod
   public static PathIt paths(ZipFile zip)
   {
      return new PathIt(new EnumIt(zip.entries()));
   }

   @ApiMethod
   public static PathIt paths(String path)
   {
      List<String> paths = new ArrayList();
      if (path != null)
      {
         String[] arr = path.split(System.getProperty("path.separator"));
         for (int i = 0; arr != null && i < arr.length; i++)
         {
            String str = path(arr[i]);
            if (str.length() > 0)
               paths.add(str);
         }
      }
      return new PathIt(new CollectionIt(paths));
   }

   @ApiMethod
   public static String chunk(String path, int index)
   {
      String chunk = null;
      String[] chunks = path(path).split("/");
      if(chunks != null && chunks.length > index)
         chunk = chunks[index];

      return chunk;
   }

   @ApiMethod
   public static String[] chunks(String path)
   {
      String[] parts = path.split("/");

      List<String> chunks = new ArrayList();

      //treat the leading '/' as if were its own
      //directory name not simply a path separator
      if (path.charAt(0) == '/')
      {
         chunks.add("/");
      }

      StringBuffer chunk = new StringBuffer("");
      for (int i = 0; i < parts.length; i++)
      {
         if (parts[i].length() == 0)
            continue;

         if (Strings.isWildcard(parts[i]))
         {
            if (chunk.length() > 0)
            {
               chunks.add(chunk.toString());
               chunk = new StringBuffer("");
            }

            chunks.add(parts[i]);
         }
         else
         {
            if (chunk.length() > 0)
               chunk.append("/");
            chunk.append(parts[i]);
         }
      }
      if (chunk.length() > 0)
      {
         chunks.add(chunk.toString());
      }

      //if the path ends with a "/", the last chunk
      //needs to include the slash
      if (path.charAt(path.length() - 1) == '/')
      {
         String lastChunk = chunks.get(chunks.size() - 1);
         lastChunk += "/";
         chunks.set(chunks.size() - 1, lastChunk);
      }
      return (String[]) chunks.toArray(new String[chunks.size()]);

   }

}
