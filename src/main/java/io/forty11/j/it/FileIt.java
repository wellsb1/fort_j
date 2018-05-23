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
import java.util.List;

public class FileIt extends It<File>
{
   File       root  = null;
   List<File> files = new ArrayList();

   public FileIt(File file)
   {
      this.root = file;
      files.addAll(listDir(file));
   }

   public File findNext()
   {
      while (files.size() > 0)
      {
         File file = files.remove(0);
         if (file.isDirectory())
         {
            files.addAll(listDir(file));
         }
         else
         {
            return file;
         }
      }
      return null;
   }

   public static List<File> listDir(File dir)
   {
      File[] children = dir.listFiles();

      if (children != null)
      {
         List childList = Arrays.asList(children);
         Collections.sort(childList);
         return childList;
      }
      else
      {
         return Collections.EMPTY_LIST;
      }
   }

}
