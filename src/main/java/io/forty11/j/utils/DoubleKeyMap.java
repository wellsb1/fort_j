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
package io.forty11.j.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class DoubleKeyMap<K1, K2, V1> extends LinkedHashMap<K1, Map>
{

   public Object put(K1 key1, K2 key2, V1 value)
   {
      Map key2Map = (Map) get(key1);
      if (key2Map == null)
      {
         key2Map = new LinkedHashMap();
         put(key1, key2Map);
      }

      return key2Map.put(key2, value);
   }

   public V1 get(K1 key1, K2 key2)
   {
      Map key2Map = (Map) get(key1);
      if (key2Map != null)
      {
         return (V1) key2Map.get(key2);
      }

      return null;
   }

   public boolean remove(K1 key1, K2 key2)
   {
      Map key2Map = (Map) get(key1);
      if (key2Map != null)
      {
         if (key2Map.size() == 1)
         {
            remove(key2Map);
         }

         return key2Map.remove(key2) != null;
      }
      else
      {
         return false;
      }
   }

   public boolean containsKey(K1 key1, K2 key2)
   {
      return get(key1, key2) != null;
   }

}