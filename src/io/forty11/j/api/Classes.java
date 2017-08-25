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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.forty11.j.it.ClassesIt;


public class Classes
{
   public static Iterable<String> listClasses()
   {
      return new ClassesIt();
   }

   public static Class loadClass(final String className, boolean quiet)
   {
      try
      {
         return Thread.currentThread().getContextClassLoader().loadClass(className);
      }
      catch (ClassNotFoundException cnfe)
      {
         //ignore
      }
      catch (Throwable ex)
      {
         if (!quiet)
         {
            System.err.println("ERROR: unable to load class \"" + className + "\"  - " + ex.getClass().getName() + " " + ex.getMessage());
         }
      }
      return null;
   }

   public static Class findClass(String name, boolean quiet)
   {
      Class c = loadClass(name, true);
      if (c != null)
         return c;

      String lc = name.toLowerCase();

      for (String className : new ClassesIt())
      {
         String str = className.toLowerCase();
         if (lc.equals(str) || lc.equals(getSimpleName(str)))
         {
            return loadClass(className, quiet);
         }
      }
      return null;
   }

   public static String getSimpleName(String name)
   {
      int index = name.lastIndexOf('.');
      if (index > -1)
      {
         name = name.substring(index + 1, name.length());
      }
      return name;
   }

   public static List<String> getClassPath()
   {
      List<String> paths = new ArrayList();
      String sep = System.getProperty("path.separator");
      String cp = System.getProperty("java.class.path");

      String[] parts = cp.split(sep);
      for (int i = 0; i < parts.length; i++)
      {
         String part = parts[i];
         part = part.trim();
         if (part.length() > 0)
         {
            try
            {
               File file = new File(part).getCanonicalFile();
               String path = file.toString();

               if (!paths.contains(path))
               {
                  paths.add(path);
               }
            }
            catch (Exception ex)
            {
               //igonre;
            }
         }
      }

      return paths;
   }
}
