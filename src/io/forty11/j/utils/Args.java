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
package io.forty11.j.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Args
{
   Map<String, String> args = new HashMap();

   public Args(String[] args)
   {
      for (int i = 0; args != null && i < args.length - 1; i++)
      {
         if (args[i].startsWith("-"))
         {
            this.args.put(args[i].substring(1, args[i].length()).trim().toLowerCase(), args[i + 1]);
            i += 1;
         }
      }
   }

   public String put(String key, String value)
   {
      return args.put(key, value);
   }

   public void putDefault(String key, String value)
   {
      if (getArg(key) == null)
         args.put(key.toLowerCase(), value);
   }

   public String remove(String key)
   {
      return args.remove(key.toLowerCase());
   }

   public String getArg(String name)
   {
      return args.get(name.trim().toLowerCase());
   }

   public String getArg(String name, String deafultValue)
   {
      String value = args.get(name.trim().toLowerCase());
      return value != null ? value : deafultValue;
   }

   public void setAll(Object target)
   {
      try
      {
         Set done = new HashSet();
         Class clazz = target.getClass();
         do
         {
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; fields != null && i < fields.length; i++)
            {
               Field f = fields[i];
               String name = f.getName().toLowerCase();
               //System.out.println(name);

               if (done.contains(name))
                  continue;

               done.add(name);

               String value = args.get(name);
               if (value != null)
               {
                  f.setAccessible(true);

                  Class type = f.getType();
                  if (type.isAssignableFrom(String.class))
                  {
                     f.set(target, value);
                  }
                  else if (type.isAssignableFrom(int.class))
                  {
                     f.set(target, Integer.parseInt(value));
                  }
                  else if (type.isAssignableFrom(long.class))
                  {
                     f.set(target, Long.parseLong(value));
                  }
                  else if (type.isAssignableFrom(boolean.class))
                  {
                     boolean val = value.toLowerCase().startsWith("t") || value.equals("1");
                     f.set(target, val);
                  }
                  else if (Collection.class.isAssignableFrom(type))
                  {
                     Collection c = (Collection) f.get(target);
                     c.clear();
                     c.addAll(Arrays.asList(value.split(",")));
                  }
                  else
                  {
                     System.out.println("unknown type:" + type);
                  }
               }
            }
            clazz = clazz.getSuperclass();
         }
         while (clazz != null && !clazz.getPackage().getName().startsWith("java"));
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
   }
}
