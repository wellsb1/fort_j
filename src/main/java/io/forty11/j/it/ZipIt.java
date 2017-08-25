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
package io.forty11.j.it;

import io.forty11.j.api.Lang;

import java.net.URL;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipIt implements Iterable<ZipEntry>, Iterator<ZipEntry>
{
   ZipInputStream zio  = null;
   ZipEntry       next = null;

   public ZipIt(URL url)
   {
      try
      {
         zio = new ZipInputStream(url.openStream());
      }
      catch (Exception ex)
      {
         Lang.rethrow(ex);
      }
   }

   @Override
   public Iterator<ZipEntry> iterator()
   {
      return this;
   }

   @Override
   public boolean hasNext()
   {
      if (next == null)
      {
         next = findNext();
      }
      return next != null;
   }

   @Override
   public ZipEntry next()
   {
      ZipEntry temp = next;
      next = null;
      return temp;
   }

   @Override
   public void remove()
   {
   }

   public ZipEntry findNext()
   {
      try
      {
         return zio.getNextEntry();
      }
      catch (Exception ex)
      {
         Lang.rethrow(ex);
      }
      return null;
   }

}
