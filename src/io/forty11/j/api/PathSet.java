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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PathSet
{
   List<Path> includes = new ArrayList();
   List<Path> excludes = new ArrayList();

   public PathSet()
   {
   }

   public PathSet(String includes)
   {
      setIncludes(includes);
   }

   public PathSet(String includes, String excludes)
   {
      setIncludes(includes);
      setExcludes(excludes);
   }

   public String toString()
   {
      return includes.toString();
   }

   public boolean included(String path)
   {
      if (includes.size() == 0)
      {
         return !excluded(path);
      }
      else
      {
         for (Path p : includes)
         {
            if (p.matches(path))
               return true;
         }
      }
      return false;
   }

   public boolean excluded(String path)
   {
      if (excludes.size() == 0)
      {
         return false;
      }
      else
      {
         for (Path p : excludes)
         {
            if (p.matches(path))
               return true;
         }
      }

      return false;
   }

   public List<Path> getIncludes()
   {
      return includes;
   }

   public void setIncludes(String... includes)
   {
      this.includes = new ArrayList();
      for (String str : includes)
      {
         for (String path : Paths.paths(str))
         {
            Path p = new Path(path);
            if (!this.includes.contains(p))
               this.includes.add(p);
         }
      }
   }

   public List<Path> getExcludes()
   {
      return excludes;
   }

   public void setExcludes(String... excludes)
   {
      this.excludes = new ArrayList();
      for (String str : excludes)
      {
         for (String path : Paths.paths(str))
         {
            Path p = new Path(path);
            if (!this.excludes.contains(p))
               this.excludes.add(p);
         }
      }
   }

   public static class Path
   {
      String   path   = null;
      Pattern  regex  = null;
      String[] chunks = null;

      public Path(String path)
      {
         path = Paths.path(path);
         this.path = path;
         regex = Pattern.compile(Strings.wildcardToRegex(path));
         chunks = Paths.chunks(path);
      }

      public boolean isWildcard()
      {
         return Strings.isWildcard(path);
      }

      public boolean matches(String path)
      {
         return regex.matcher(path).matches();
      }

      public String getPath()
      {
         return path;
      }

      public String[] getChunks()
      {
         return chunks;
      }

      public Pattern getRegex()
      {
         return regex;
      }

      public String toString()
      {
         return path;
      }

      public int hashCode()
      {
         return toString().hashCode();
      }

      public boolean equals(Object object)
      {
         return object instanceof Path && path.equals(((Path) object).path);
      }
   }

}
