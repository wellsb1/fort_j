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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import io.forty11.j.it.It;

public class Lang
{
   protected static final String   NEW_LINE           = System.getProperty("line.separator");

   protected static final String[] EMPTY_STRING_ARRAY = new String[0];

   public static long time()
   {
      return System.currentTimeMillis();
   }

   @ApiMethod
   public static boolean empty(Object obj)
   {
      if (obj == null)
         return true;

      if (obj.toString().length() == 0)
         return true;

      return false;
   }

   /**
    * Tests for ==,equals() or toString().equals()
    * @param obj1
    * @param obj2
    * @return
    */
   @ApiMethod
   public static boolean equal(Object obj1, Object obj2)
   {
      if (obj1 == obj2)
         return true;

      if (obj1 == null || obj2 == null)
         return false;

      return obj1.toString().equals(obj2.toString());
   }

   @ApiMethod
   public static int atoi(String str)
   {
      if (str == null)
         return 0;
      str = str.trim();
      return Integer.parseInt(str);
   }

   @ApiMethod
   public static long atol(String str)
   {
      if (str == null)
         return 0;
      str = str.trim();
      return Long.parseLong(str);
   }

   @ApiMethod
   public static float atof(String str)
   {
      if (str == null)
         return 0;
      str = str.trim();
      return Float.parseFloat(str);
   }

   @ApiMethod
   public static double atod(String str)
   {
      if (str == null)
         return 0;
      str = str.trim();
      return Double.parseDouble(str);
   }

   @ApiMethod
   public static String formatDate(Date date)
   {
      TimeZone tz = TimeZone.getTimeZone("UTC");
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
      df.setTimeZone(tz);
      return df.format(date);
   }

   @ApiMethod
   public static String formatDate(Date date, String format)
   {
      SimpleDateFormat f = new SimpleDateFormat(format);
      return f.format(date);
   }

   @ApiMethod
   public static Date date(String date, String format)
   {
      try
      {
         date = date.trim();
         SimpleDateFormat df = new SimpleDateFormat(format);
         return df.parse(date);
      }
      catch (Exception ex)
      {
         rethrow(ex);
      }
      return null;
   }

   @ApiMethod
   public static Date date(String date)
   {
      try
      {
         //not supported in JDK 1.6
         //         DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
         //         TemporalAccessor accessor = timeFormatter.parse(date);
         //         return Date.from(Instant.from(accessor));
         return ISO8601Util.parse(date, new ParsePosition(0));
      }
      catch (Exception ex)
      {

      }
      try
      {
         SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
         return f.parse(date);

      }
      catch (Exception ex)
      {

      }

      try
      {
         SimpleDateFormat f = new SimpleDateFormat("MM/dd/yy");

         int lastSlash = date.lastIndexOf("/");
         if (lastSlash > 0 && lastSlash == date.length() - 5)
         {
            f = new SimpleDateFormat("MM/dd/yyyy");
         }
         Date d = f.parse(date);
         //System.out.println(d);
         return d;

      }
      catch (Exception ex)
      {

      }

      try
      {
         SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
         return f.parse(date);
      }
      catch (Exception ex)
      {

      }
      throw new RuntimeException("unsupported format: " + date);
   }

   @ApiMethod
   public static Throwable getCause(Throwable t)
   {
      Throwable origional = t;

      int guard = 0;
      while (t != null && t.getCause() != null && t.getCause() != t && guard < 100)
      {
         t = t.getCause();

         //--known special cases
         //         if (t instanceof SAXParseException)
         //         {
         //            t = ((SAXParseException) t).getException();
         //         }

         guard++;
      }

      if (t == null)
      {
         t = origional;
      }

      return t;
   }

   @ApiMethod
   public static String[] splitLines(String text)
   {
      if (text == null || "".equals(text))
      {
         return EMPTY_STRING_ARRAY;
      }

      String lineSeparator = text.indexOf(NEW_LINE) >= 0 ? NEW_LINE : "\n";
      return text.split(lineSeparator);
   }

   @ApiMethod
   public static final String limitLines(String text, int limit)
   {
      StringBuffer buffer = new StringBuffer("");
      String[] lines = splitLines(text);
      for (int i = 0; i < lines.length && i < limit; i++)
      {
         if (i == limit - 1 && i != lines.length - 1)
         {
            buffer.append("..." + (lines.length - i) + " more");
         }
         else
         {
            buffer.append(lines[i]).append(NEW_LINE);
         }
      }

      return buffer.toString();
   }

   @ApiMethod
   public static void rethrow(Throwable ex)
   {
      rethrow(null, ex);
   }

   @ApiMethod
   public static void rethrow(String message, Throwable ex)
   {
      Throwable cause = ex;

      while (cause.getCause() != null && cause.getCause() != ex)
         cause = cause.getCause();

      if (cause instanceof RuntimeException)
      {
         throw (RuntimeException) cause;
      }

      if (ex instanceof RuntimeException)
         throw (RuntimeException) ex;

      if (!empty(message))
      {
         throw new RuntimeException(message, ex);
      }
      else
      {
         throw new RuntimeException(ex);
      }
   }

   @ApiMethod
   public static String getShortCause(Throwable t)
   {
      return getShortCause(t, 15);
   }

   @ApiMethod
   public static String getShortCause(Throwable t, int lines)
   {
      t = getCause(t);
      //return System.getProperty("line.separator") + limitLines(clean(getStackTraceString(t)), lines);
      return limitLines(clean(getStackTraceString(t)), lines);
   }

   @ApiMethod
   public static List<String> getStackTraceLines(Throwable stackTrace)
   {
      ByteArrayOutputStream baos = null;
      PrintWriter writer;

      baos = new ByteArrayOutputStream();
      writer = new PrintWriter(baos);

      if (stackTrace != null)
      {
         stackTrace.printStackTrace(writer);
      }
      else
      {
         try
         {
            throw new Exception();
         }
         catch (Exception e)
         {
            e.printStackTrace(writer);
         }
      }

      writer.close();

      List lines = new ArrayList();
      String s = new String(baos.toByteArray());
      String[] sArr = s.split("\n");
      lines.addAll(new ArrayList(Arrays.asList(sArr)));

      return lines;
   }

   @ApiMethod
   public static String getStackTraceString(Thread t)
   {
      return getStackTraceString(t, t.getStackTrace());
   }

   @ApiMethod
   public static String getStackTraceString(Thread t, StackTraceElement[] stackTrace)
   {
      StringBuffer buff = new StringBuffer();

      buff.append("Thread -------------------------").append("\r\n");
      buff.append("  id    = ").append(t.getId()).append("\r\n");
      buff.append("  name  = ").append(t.getName()).append("\r\n");
      buff.append("  state = ").append(t.getState()).append("\r\n");
      buff.append("  trace: ").append("\r\n");
      for (int i = 0; i < stackTrace.length; i++)
      {
         buff.append("\tat").append(stackTrace[i]).append("\r\n");
      }

      return buff.toString();
   }

   @ApiMethod
   public static String getStackTraceString(Throwable stackTrace)
   {
      ByteArrayOutputStream baos = null;
      PrintWriter writer;

      baos = new ByteArrayOutputStream();
      writer = new PrintWriter(baos);

      boolean createNewTrace = false;

      if (stackTrace != null)
      {
         try
         {
            stackTrace.printStackTrace(writer);
         }
         catch (Exception e)
         {
            createNewTrace = true;
         }
      }
      else
      {
         createNewTrace = true;
      }

      if (createNewTrace)
      {
         try
         {
            throw new Exception("Unable to get original stacktrace.");
         }
         catch (Exception e)
         {
            e.printStackTrace(writer);
         }
      }

      writer.close();

      return new String(baos.toByteArray());

   }

   /** Get the class from a line on the stack trace line. */
   @ApiMethod
   public static String getMethodNameFromStackLine(String line)
   {
      if (line != null)
      {
         line = line.trim();
         int pos = line.indexOf("at ");
         if (pos == 0)
         {
            line = line.substring(3);
            pos = line.indexOf('(');
            if (pos < 0)
            {
               pos = line.indexOf(' ');
            }
            if (pos > 0)
            {
               String clsStr = line.substring(0, pos);
               clsStr = clsStr.trim();

               pos = clsStr.lastIndexOf('.');
               String methodName = clsStr.substring(pos + 1);

               return methodName;
            }
         }
      }

      return null;
   }

   @ApiMethod
   public static Class getClassFromStackLine(String line)
   {
      if (line != null)
      {
         line = line.trim();
         int pos = line.indexOf("at ");
         if (pos == 0)
         {
            line = line.substring(3);
            pos = line.indexOf('(');
            if (pos < 0)
            {
               pos = line.indexOf(' ');
            }
            if (pos > 0)
            {
               String clsStr = line.substring(0, pos);
               clsStr = clsStr.trim();

               pos = clsStr.lastIndexOf('.');
               clsStr = clsStr.substring(0, pos);

               try
               {
                  return Class.forName(clsStr);
               }
               catch (ClassNotFoundException e)
               {
               }
            }
         }
      }
      return null;
   }


   static String clean(String stackTrace)
   {
      String[] ignoredCauses = new String[]{//
            //
            "java.lang.reflect.UndeclaredThrowableException", //
            "java.lang.reflect.InvocationTargetException"};

      String[] lines = splitLines(stackTrace);

      boolean chop = false;
      if (stackTrace.indexOf("Caused by: ") > 0)
      {
         for (int i = 0; i < ignoredCauses.length; i++)
         {
            if (lines[0].indexOf(ignoredCauses[i]) > -1)
            {
               chop = true;
               break;
            }
         }
      }

      int start = 0;
      if (chop)
      {
         for (int i = 0; i < lines.length; i++)
         {
            if (lines[i].startsWith("Caused by:"))
            {
               lines[i] = lines[i].substring(10, lines[i].length());
               break;
            }

            start++;
         }
      }

      StringBuffer buffer = new StringBuffer();
      for (int i = start; i < lines.length; i++)
      {
         buffer.append(lines[i]).append("\r\n");
      }

      if (chop)
      {
         return clean(buffer.toString());
      }
      else
      {
         return buffer.toString();
      }
   }

   @ApiMethod
   public static void sleep(long millis)
   {
      try
      {
         Thread.sleep(millis);
      }
      catch (Exception ex)
      {
         rethrow(ex);
      }
   }

   @ApiMethod
   public static Method getMethod(Class clazz, String name, Class... args)
   {
      try
      {
         return clazz.getMethod(name, args);
      }
      catch (NoSuchMethodException ex)
      {

      }
      return null;
   }

   @ApiMethod
   public static Field getField(String fieldName, Class clazz)
   {
      if (fieldName == null || clazz == null)
      {
         return null;
      }

      Field[] fields = clazz.getDeclaredFields();
      for (int i = 0; i < fields.length; i++)
      {
         if (fields[i].getName().equals(fieldName))
         {
            Field field = fields[i];
            field.setAccessible(true);
            return field;
         }
      }

      if (clazz.getSuperclass() != null && !clazz.equals(clazz.getSuperclass()))
      {
         return getField(fieldName, clazz.getSuperclass());
      }

      return null;
   }

   @ApiMethod
   public static List<Field> getFields(Class clazz)
   {
      List<Field> fields = new ArrayList();

      do
      {
         if (clazz.getName().startsWith("java"))
            break;

         Field[] farr = clazz.getDeclaredFields();
         if (farr != null)
         {
            for (Field f : farr)
            {
               f.setAccessible(true);
            }
            fields.addAll(Arrays.asList(farr));
         }
         clazz = clazz.getSuperclass();
      }
      while (clazz != null && !Object.class.equals(clazz));

      return fields;
   }

   @ApiMethod
   public static Method getMethod(Class clazz, String name)
   {
      for (Method m : clazz.getMethods())
      {
         if (m.getName().equalsIgnoreCase(name))
            return m;
      }
      return null;
   }

   @ApiMethod
   public static List<Method> getMethods(Class clazz, String name)
   {
      List<Method> methods = new ArrayList();
      for (Method m : clazz.getMethods())
      {
         if (m.getName().equalsIgnoreCase(name))
            methods.add(m);
      }
      return methods;
   }

   @ApiMethod
   public static Object getProperty(String name, Object object)
   {
      try
      {
         Method getter = getMethod(object.getClass(), "get" + name);
         if (getter != null)
         {
            return getter.invoke(object);
         }
         else
         {
            Class clazz = object.getClass();
            while (clazz != null)
            {
               //               Field[] fields = clazz.getDeclaredField(name)Fields();
               //               for(int i=0; fields != null && i < fields.length; i++)
               //               {
               //                  if(fields[i].getName().equalsIgnoreCase(name))
               //                  {
               //                     fields[i].setAccessible(true);
               //                     return fields[i].get(object);
               //                  }
               //               }

               try
               {
                  Field field = clazz.getDeclaredField(name);
                  if (field != null)
                  {
                     field.setAccessible(true);
                     return field.get(object);
                  }
               }
               catch (NoSuchFieldException ex)
               {
               }

               if (clazz.equals(Object.class))
                  break;

               clazz = clazz.getSuperclass();
            }
         }
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
      return null;
   }

   @ApiMethod
   public static Object getProperty(String name, Object object, Object defaultValue)
   {
      Object value = getProperty(name, object);
      if (Lang.empty(value))
      {
         value = defaultValue;
      }

      return value;
   }

   /**
    * A best effort field by field shallow copier that will ignore all errors.
    * This is not a recursive copyier.
    *
    * The src and the dest object do not have to be the same class.
    *
    * @param src
    * @param dest
    */
   @ApiMethod
   public static void copyFields(Object src, Object dest)
   {
      List<Field> fields = getFields(src.getClass());
      for (Field f : fields)
      {
         try
         {
            Object value = f.get(src);
            setField(f.getName(), value, dest);
         }
         catch (Exception ex)
         {
         }
      }
   }

   @ApiMethod
   public static void setField(String name, Object value, Object o) throws NoSuchFieldException, IllegalAccessException
   {
      Field f = getField(name, o.getClass());
      f.setAccessible(true);
      f.set(o, value);
   }

   @ApiMethod
   public static void close(Object... toClose)
   {
      for (Object o : toClose)
      {
         if (o != null)
         {
            try
            {
               Method m = o.getClass().getMethod("close");
               if (m != null)
               {
                  m.invoke(o);
               }
            }
            catch (NoSuchMethodException nsme)
            {
               nsme.printStackTrace();
            }
            catch (Exception ex)
            {
               ex.printStackTrace();
            }
         }
      }
   }

   /*
   +------------------------------------------------------------------------------+
   | Collection Converstion
   +------------------------------------------------------------------------------+
   */

   @ApiMethod
   public static List asList(It it)
   {
      return asList(it.iterator());
   }

   @ApiMethod
   public static List asList(Iterable it)
   {
      return asList(it.iterator());
   }

   @ApiMethod
   public static List asList(Iterator it)
   {
      List list = new ArrayList();
      while (it.hasNext())
      {
         list.add(it.next());
      }
      return list;
   }

   @ApiMethod
   public static List asList(Object... array)
   {
      if (array != null && array.length == 1 && array[0] instanceof List)
         return (List) array[0];

      List list = new ArrayList();
      for (int i = 0; array != null && i < array.length; i++)
      {
         list.add(array[i]);
      }
      return list;
   }

   @ApiMethod
   public static Object[] asArray(Iterable it)
   {
      return asArray(asList(it));
   }

   //   @JjMethod
   //   public static Object[] asArray(Iterator it)
   //   {
   //      return asArray(asList(it));
   //   }

   @ApiMethod
   public static Object[] asArray(Collection c)
   {
      ArrayList al = new ArrayList(c);
      if (al.size() == 0)
      {
         return new Object[0];
      }
      else
      {
         try
         {
            Class clazz = al.get(0).getClass();
            Object[] arr = (Object[]) Array.newInstance(clazz, al.size());
            return al.toArray(arr);
         }
         catch (Exception ex)
         {
            return al.toArray();
         }
      }
   }

}
