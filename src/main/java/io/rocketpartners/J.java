/*
 * Copyright (c) 2015-2018 Rocket Partners, LLC
 * http://rocketpartners.io
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
package io.rocketpartners;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import io.rocketpartners.utils.ISO8601Util;

/**
 * Collection of utility methods designed to make
 * java programming less verbose
 *
 * @author Wells Burke
 */
public class J
{
   public static final int  KB    = 1048;
   public static final int  MB    = 1048576;
   public static final long GB    = 1073741824;
   public static final int  K64   = 65536;

   public static final long HOUR  = 1000 * 60 * 60;
   public static final long DAY   = 1000 * 60 * 60 * 24;
   public static final long MONTH = 1000 * 60 * 60 * 24 * 31;
   public static final long WEEK  = 1000 * 60 * 60 * 24 * 7;
   public static final long YEAR  = 1000 * 60 * 60 * 24 * 365;

   /**
    * A null safe loose equality checker.  
    * @param obj1
    * @param obj2
    * @return Test for strict == equality, then .equals() equality, then .toString().equals() equality.  Either param can be null. 
    */
   public static boolean equal(Object obj1, Object obj2)
   {
      if (obj1 == obj2)
         return true;

      if (obj1 == null || obj2 == null)
         return false;

      return obj1.toString().equals(obj2.toString());
   }

   /**
    * @return true if any args are not null with a toString().length() > 0 
    */
   public static boolean empty(Object... arr)
   {
      boolean empty = true;
      for (int i = 0; empty && arr != null && i < arr.length; i++)
      {
         Object obj = arr[i];
         if (obj != null && obj.toString().length() > 0)
            empty = false;
      }
      return empty;
   }

   /**
    * @param glue
    * @param pieces
    * @return Concatenates pieces[0] + glue + pieces[n]... Intelligently recurses through Collections
    */
   public static String implode(String glue, Object... pieces)
   {
      if (pieces != null && pieces.length == 1 && pieces[0] instanceof Collection)
         pieces = ((Collection) pieces[0]).toArray();

      StringBuffer str = new StringBuffer("");
      for (int i = 0; pieces != null && i < pieces.length; i++)
      {
         if (pieces[i] != null)
         {
            String piece = pieces[i] instanceof Collection ? implode(glue, pieces[i]) : pieces[i].toString();

            if (piece.length() > 0)
            {
               List<String> subpieces = explode(glue, piece);

               for (String subpiece : subpieces)
               {
                  if (subpiece.length() > 0)
                  {
                     if (str.length() > 0)
                        str.append(glue);
                     str.append(subpiece);
                  }
               }
            }
         }
      }
      return str.toString();
   }

   /**
    * @param delim
    * @param pieces
    * @return Same as String.split() but performs a trim() on each piece and returns an list instead of an array
    */
   public static List<String> explode(String delim, String... pieces)
   {
      List exploded = new ArrayList();
      for (int i = 0; pieces != null && i < pieces.length; i++)
      {
         if (J.empty(pieces[i]))
            continue;

         String[] parts = pieces[i].split(delim);
         for (int j = 0; j < parts.length; j++)
         {
            String part = parts[j].trim();
            if (!J.empty(part))
            {
               exploded.add(part);
            }
         }
      }

      return exploded;
   }

   /**
    * Faster and null safe way to call Integer.parseInt(str.trim()) that swallows exceptions.
    */
   public static int atoi(String str)
   {
      try
      {
         return Integer.parseInt(str.trim());
      }
      catch (Exception ex)
      {
         //ignore
      }
      return -1;
   }

   /**
    * Faster and null safe way to call Long.parseLong(str.trim()) that swallows exceptions.
    */
   public static long atol(String str)
   {
      try
      {
         return Long.parseLong(str.trim());
      }
      catch (Exception ex)
      {
         //ignore
      }
      return -1;
   }

   /**
    * Faster and null safe way to call Float.parseFloat(str.trim()) that swallows exceptions.
    */
   public static float atof(String str)
   {
      try
      {
         return Float.parseFloat(str.trim());
      }
      catch (Exception ex)
      {
         //ignore
      }
      return -1;
   }

   /**
    * Faster and null safe way to call Double.parseDouble(str.trim()) that swallows exceptions.
    */
   public static double atod(String str)
   {
      try
      {
         return Double.parseDouble(str.trim());
      }
      catch (Exception ex)
      {
         //ignore
      }
      return -1;
   }

   /**
    * @param bytes
    * @return Hash the bytes with SHA-1
    */
   public static String sha1(byte[] bytes)
   {
      return hash(bytes, "SHA-1");
   }

   /**
    * @param bytes
    * @return Hash the bytes with MD5
    */
   public static String md5(byte[] bytes)
   {
      return hash(bytes, "MD5");
   }

   /**
    * @param byteArr
    * @param algorithm
    * @return Hash the bytes with the given algorithm
    */
   public static String hash(byte[] byteArr, String algorithm)
   {
      try
      {
         MessageDigest digest = MessageDigest.getInstance(algorithm);
         digest.update(byteArr);
         byte[] bytes = digest.digest();

         String hex = (new HexBinaryAdapter()).marshal(bytes);

         return hex;
      }
      catch (Exception ex)
      {
         rethrow(ex);
      }
      return null;
   }

   /**
    * Less typing to call System.currentTimeMillis()
    */
   public static long time()
   {
      return System.currentTimeMillis();
   }

   //   public static String formatDate(Date date)
   //   {
   //      TimeZone tz = TimeZone.getTimeZone("UTC");
   //      DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
   //      df.setTimeZone(tz);
   //      return df.format(date);
   //   }

   /**
    * Simple one liner to avoid verbosity of using SimpleDateFormat
    */
   public static String formatDate(Date date, String format)
   {
      SimpleDateFormat f = new SimpleDateFormat(format);
      return f.format(date);
   }

   /**
    * Faster way to apply a SimpleDateForamt without having to catch ParseException
    * @param date
    * @param format
    */
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

   /**
    * Attempts an ISO8601 data as yyyy-MM-dd|yyyyMMdd][T(hh:mm[:ss[.sss]]|hhmm[ss[.sss]])]?[Z|[+-]hh[:]mm], 
    * then yyyy-MM-dd, 
    * then MM/dd/yy, 
    * then MM/dd/yyyy, 
    * then yyyyMMdd
    * @param date
    * @return
    */
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
         throw new RuntimeException("unsupported format: " + date);
      }

   }

   /**
    * Tries to \"unwrap\" nested exceptions looking for the root cause
    * @param t
    */
   public static Throwable getCause(Throwable t)
   {
      Throwable origional = t;

      int guard = 0;
      while (t != null && t.getCause() != null && t.getCause() != t && guard < 100)
      {
         t = t.getCause();
         guard++;
      }

      if (t == null)
      {
         t = origional;
      }

      return t;
   }

   /**
    * Shortcut for throw new RuntimeException(message); 
    */
   public static void error(String message)
   {
      throw new RuntimeException(message);
   }

   /**
    * Throws the root cause of e as a RuntimeException
    * @param e
    */
   public static void rethrow(Throwable e)
   {
      rethrow(null, e);
   }

   /**
    * Throws the root cause of e as a RuntimeException
    * @param e
    */
   public static void rethrow(String message, Throwable e)
   {
      Throwable cause = e;

      while (cause.getCause() != null && cause.getCause() != e)
         cause = cause.getCause();

      if (cause instanceof RuntimeException)
      {
         throw (RuntimeException) cause;
      }

      if (e instanceof RuntimeException)
         throw (RuntimeException) e;

      if (!empty(message))
      {
         throw new RuntimeException(message, e);
      }
      else
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Easy way to call Thread.sleep(long) without worrying about try/catch for InterruptedException
    * @param millis
    */
   public static void sleep(long millis)
   {
      try
      {
         Thread.sleep(millis);
      }
      catch (InterruptedException e)
      {
         rethrow(e);
      }
   }

   /**
    * Same as calling Class.getMethod but it returns null intead of throwing a NoSuchMethodException
    * @param clazz
    * @param name
    * @param args
    * @return
    */
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

   /**
    * Searches the inheritance hierarchy for a field with the the given name and makes sure it is settable via Field.setAccesible
    * @param fieldName
    * @param clazz
    * @return
    */
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

   /**
    * Gets all the fields from from all classes in the inheritance hierarchy EXCEPT for any class who's packages starts with \"java\"
    * @param clazz
    * @return
    */
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

   /**
    * Finds the Field in the inheritance heirarchy and sets it
    * @param name
    * @param value
    * @param o
    * @throws NoSuchFieldException
    * @throws IllegalAccessException
    */
   public static void setField(String name, Object value, Object o) throws NoSuchFieldException, IllegalAccessException
   {
      Field f = getField(name, o.getClass());
      f.setAccessible(true);
      f.set(o, value);
   }

   /**
    * Searches the inheritance hierarchy for the first method of the given name (ignores case).  No distinction is made for overloaded method names.
    * @param clazz
    * @param name
    * @return
    */
   public static Method getMethod(Class clazz, String name)
   {
      do
      {
         for (Method m : clazz.getMethods())
         {
            if (m.getName().equalsIgnoreCase(name))
               return m;
         }

         if (clazz != null)
         {
            clazz = clazz.getSuperclass();
         }
      }
      while (clazz != null && !Object.class.equals(clazz));

      return null;
   }

   /**
    * @param clazz
    * @param name
    * @return all methods in the inheritance hierarchy with the given name
    */
   public static List<Method> getMethods(Class clazz, String name)
   {
      List<Method> methods = new ArrayList();

      do
      {
         for (Method m : clazz.getMethods())
         {
            if (m.getName().equalsIgnoreCase(name))
               methods.add(m);
         }
      }
      while (clazz != null && !Object.class.equals(clazz));

      return methods;
   }

   /**
    * Tries to find a bean property getter then defaults to returning the Field value
    * @param name
    * @param object
    * @return
    */
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
            Field field = getField(name, object.getClass());
            if (field != null)
               return field.get(object);
         }
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
      return null;
   }

   /**
    * Tries to find a bean property getter then tries Field value, then defaults to the supplied defaultVal
    * @param name
    * @param object
    * @param defaultVal
    * @return
    */
   public static Object getProperty(String name, Object object, Object defaultVal)
   {
      Object value = getProperty(name, object);
      if (empty(value))
      {
         value = defaultVal;
      }

      return value;
   }

   /**
    * A best effort field by field shallow copier that will ignore all errors. This does not recurse.
    * @param src
    * @param dest
    */
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

   /**
    * Utility to call a close() method on supplied objects if it exists and completely ignore any errors
    * @param toClose
    */
   public static void close(Object... toClose)
   {
      for (Object o : toClose)
      {
         if (o != null)
         {
            try
            {
               if (o instanceof Closeable)
               {
                  ((Closeable) o).close();
               }
               else
               {
                  Method m = o.getClass().getMethod("close");
                  if (m != null)
                  {
                     m.invoke(o);
                  }
               }
            }
            catch (NoSuchMethodException nsme)
            {
               //nsme.printStackTrace();
            }
            catch (Exception ex)
            {
               //ex.printStackTrace();
            }
         }
      }
   }

   /**
    * Read all of the stream to a string and close the stream.  Throws RuntimeException instead of IOException
    * @param in
    * @return
    */
   public static String read(InputStream in)
   {
      try
      {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         pipe(in, out);
         return new String(out.toByteArray());
      }
      catch (Exception ex)
      {
         rethrow(ex);
      }
      return null;
   }

   /**
    * Read teh contents of a file to a string
    * @param file
    * @return
    * @throws Exception
    */
   public static String read(File file) throws Exception
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      FileInputStream in = new FileInputStream(file);
      pipe(in, out);
      return new String(out.toByteArray());
   }

   /**
    * Write the string value to a file
    * @param file
    * @param text
    * @throws Exception
    */
   public static void write(File file, String text) throws Exception
   {
      if (!file.exists())
         file.getParentFile().mkdirs();

      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
      bw.write(text);
      bw.flush();
      bw.close();
   }

   /**
    * Write the string value to a file
    * @param file
    * @param text
    * @throws Exception
    */
   public static void write(String file, String text) throws Exception
   {
      if (text == null)
         return;
      write(new File(file), text);
   }

   /**
    * Copy all data from src to dst and close the streams
    * @param src
    * @param dest
    * @throws Exception
    */
   public static void pipe(InputStream src, OutputStream dest) throws Exception
   {
      try
      {
         boolean isBlocking = true;
         byte[] buf = new byte[K64];

         int nread;
         int navailable;
         //int total = 0;
         synchronized (src)
         {
            while ((navailable = isBlocking ? Integer.MAX_VALUE : src.available()) > 0 //
                  && (nread = src.read(buf, 0, Math.min(buf.length, navailable))) >= 0)
            {
               dest.write(buf, 0, nread);
               //total += nread;
            }
         }
         dest.flush();

      }
      finally
      {
         close(src);
         close(dest);
      }
   }

   /**
    * Copy all data from src to dst and close the reader/writer
    * @param src
    * @param dest
    * @throws Exception
    */
   public static void pipe(Reader src, Writer dest) throws Exception
   {
      try
      {
         char buffer[] = new char[K64];
         int len = buffer.length;
         synchronized (src)
         {
            while (true)
            {
               len = src.read(buffer);
               if (len == -1)
                  break;
               dest.write(buffer, 0, len);
            }
         }
      }
      finally
      {
         flush(dest);
         close(src);
         close(dest);
      }
   }

   /**
    * Simply calls stream.flush() but throws RuntimeException instead of IOException
    * @param stream
    */
   public static void flush(Flushable stream)
   {
      try
      {
         if (stream != null)
         {
            stream.flush();
         }
      }
      catch (Exception ex)
      {
         rethrow(ex);
      }
   }

   /**
    * Attempts to locate the stream as a file, url, or classpath resource
    * @param fileOrUrl
    * @return
    */
   public static InputStream findInputStream(String fileOrUrl)
   {
      try
      {
         if (fileOrUrl.startsWith("file:/"))
         {
            fileOrUrl = URLDecoder.decode(fileOrUrl);
         }
         if (fileOrUrl.startsWith("file:///"))
         {
            fileOrUrl = fileOrUrl.substring(7, fileOrUrl.length());
         }
         if (fileOrUrl.startsWith("file:/"))
         {
            fileOrUrl = fileOrUrl.substring(5, fileOrUrl.length());
         }

         if (fileOrUrl.indexOf(':') >= 0)
         {
            return new URL(fileOrUrl).openStream();
         }
         else if (new File(fileOrUrl).exists())
         {
            return new FileInputStream(fileOrUrl);
         }
         else
         {
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(fileOrUrl);
         }
      }
      catch (Exception ex)
      {
         if (ex instanceof RuntimeException)
            throw (RuntimeException) ex;

         throw new RuntimeException(ex);
      }
   }

   /**
    * Attempts to locate the stream as a file, url, or classpath resource and then reads it all as a string
    * @param fileOrUrl
    * @return
    * @throws Exception
    */
   public static String read(String fileOrUrl) throws Exception
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      InputStream in = findInputStream(fileOrUrl);
      pipe(in, out);
      return new String(out.toByteArray());
   }

   /**
    * Recursively deletes the file or directory
    * @param file
    * @return
    */
   public static boolean delete(File file)
   {
      boolean deleted = true;
      if (file != null && file.exists())
      {
         if (file.isDirectory())
         {
            for (File f : file.listFiles())
            {
               deleted &= delete(f);
            }
         }
         deleted &= file.delete();
      }
      return deleted;
   }

   /**
    * Copies the given file or recursively copies a directory
    * @param src
    * @param dst
    */
   public static void copy(File src, File dst)
   {
      if (src.isFile())
      {
         copyFile(src, dst);
      }
      else
      {
         copyDir(src, dst);
      }
   }

   public static boolean copy(File srcDir, File srcFile, File dstDir)
   {
      try
      {
         String dest = srcFile.getCanonicalPath();
         dest = dest.substring(srcDir.getCanonicalPath().length(), dest.length());
         if (dest.startsWith("/") || dest.startsWith("\\"))
         {
            dest = dest.substring(1, dest.length());
         }

         File dstFile = new File(dstDir, dest);
         return copyFile(srcFile, dstFile);
      }
      catch (Exception ex)
      {
         rethrow(ex);
      }
      return false;
   }

   protected static void copyDir(File srcDir, File dstDir)
   {
      File[] files = srcDir.listFiles();
      for (int i = 0; files != null && i < files.length; i++)
      {
         copy(srcDir, files[i], dstDir);
      }
   }

   protected static boolean copyFile(File srcFile, File dstFile)
   {
      FileInputStream fis = null;
      FileOutputStream fos = null;
      FileChannel sourceChannel = null;
      FileChannel destinationChannel = null;

      try
      {
         if (!dstFile.getParentFile().exists())
         {
            dstFile.getParentFile().mkdirs();
         }
         else
         {
            if (dstFile.exists() && dstFile.lastModified() >= srcFile.lastModified())
            {
               return false;
            }
         }

         fis = new FileInputStream(srcFile);
         fos = new FileOutputStream(dstFile);
         sourceChannel = fis.getChannel();
         destinationChannel = fos.getChannel();
         destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
         sourceChannel.close();
         destinationChannel.close();
         fis.close();
         fos.close();

         dstFile.setLastModified(srcFile.lastModified());
      }
      catch (Exception ex)
      {
         return false;
      }
      finally
      {
         if (sourceChannel != null)
         {
            try
            {
               sourceChannel.close();
            }
            catch (Exception ex)
            {

            }
         }

         if (destinationChannel != null)
         {
            try
            {
               destinationChannel.close();
            }
            catch (Exception ex)
            {

            }
         }

         if (fis != null)
         {
            try
            {
               fis.close();
            }
            catch (Exception ex)
            {

            }
         }

         if (fos != null)
         {
            try
            {
               fos.close();
            }
            catch (Exception ex)
            {

            }
         }
      }
      return true;
   }

   public static String substring(String string, String regex, int group)
   {
      Pattern p = Pattern.compile(regex);
      Matcher m = p.matcher(string);
      if (m.find())
         return m.group(group);

      return null;
   }

   //   /**Tries to make a pretty title case string with spaces out of a camel case style string")
   //   public static String fromCamelCase(String string)
   //   {
   //      //convert camel case style
   //      char ch = string.charAt(0);
   //      ch = Character.toTitleCase(ch);
   //   
   //      StringBuffer buff = new StringBuffer(ch + "");
   //      for (int i = 1; i < string.length(); i++)
   //      {
   //         ch = string.charAt(i);
   //   
   //         if (ch == '-')
   //         {
   //            buff.append(' ');
   //            if (i < string.length() - 1)
   //            {
   //               if (!Character.isUpperCase(string.charAt(i + 1)))
   //               {
   //                  buff.append(Character.toUpperCase(string.charAt(i + 1)));
   //                  i++;
   //                  continue;
   //               }
   //            }
   //         }
   //   
   //         if (Character.isUpperCase(ch) && //
   //               !Character.isUpperCase(string.charAt(i - 1)))
   //         {
   //            buff.append(' ');
   //         }
   //   
   //         if (Character.isDigit(ch) && Character.isLetter(string.charAt(i - 1)) || Character.isLetter(ch) && Character.isDigit(string.charAt(i - 1)))
   //         {
   //            buff.append(' ');
   //         }
   //   
   //         buff.append(string.charAt(i));
   //      }
   //   
   //      String str = buff.toString();
   //      str = str.replace('_', ' ');
   //   
   //      return str;
   //   }
   //   
   //   
   //   /**Returns a lower cased string replacing \"[^a-z0-9]+\" with \"-\"")
   //   public static String slugify(String str)
   //   {
   //      if (str == null)
   //      {
   //         return null;
   //      }
   //   
   //      str = str.toLowerCase().trim();
   //   
   //      str = str.replaceAll("[']+", "");
   //      str = str.replaceAll("[^a-z0-9]+", "-");
   //   
   //      //removes consecutive -'s
   //      str = str.replaceAll("([\\-])(\\1{2,})", "$1");
   //   
   //      // remove preceding and trailing dashes
   //      str = str.replaceAll("^-", "");
   //      str = str.replaceAll("-$", "");
   //   
   //      return str;
   //   }

   //   /**
   //    * Instance of <code>${key}</code> in <code>str</code> will be replaced
   //    * with <code>value</code> .
   //    *
   //    * Variables must be wapped in ${}
   //    * @param str
   //    * @param values
   //    * @return
   //    */
   //
   //   /**Replaces ${key} style text literals in str with values from the map")
   //   public static String replaceAll(String str, Map<String, Object> values)
   //   {
   //      StringBuffer buff = new StringBuffer("");
   //      Pattern p = Pattern.compile("\\$\\{([^\\}]*)\\}");
   //      Matcher m = p.matcher(str);
   //      while (m.find())
   //      {
   //         String key = m.group(1);
   //         String value = Matcher.quoteReplacement(values.get(key) + "");
   //   
   //         m.appendReplacement(buff, value);
   //      }
   //      m.appendTail(buff);
   //      return buff.toString();
   //   }
   //   
   //   
   //   /**Prepends spaces to the begining of each line")
   //   public static String indent(String str, int indent)
   //   {
   //      try
   //      {
   //         StringBuffer buff = new StringBuffer();
   //         String line = null;
   //         BufferedReader reader = new BufferedReader(new StringReader(str));
   //         while ((line = reader.readLine()) != null)
   //         {
   //            buff.append(pad("", indent)).append(line).append(Shell.getLineSeparator());
   //         }
   //   
   //         return buff.toString();
   //      }
   //      catch (Exception ex)
   //      {
   //   
   //      }
   //      return "null";
   //   }
   //   
   //   
   //   /**Performans a word wrap limiting each line to the specified number of characters")
   //   public static String wrap(String str, int wrap)
   //   {
   //      StringBuffer buff = new StringBuffer();
   //   
   //      while (str.length() > 0)
   //      {
   //         int length = str.length();
   //         if (length > wrap)
   //         {
   //            buff.append(str.substring(0, wrap));
   //            buff.append(Shell.getLineSeparator());
   //            str = str.substring(wrap, str.length());
   //         }
   //         else
   //         {
   //            buff.append(str);
   //            break;
   //         }
   //      }
   //   
   //      return buff.toString().trim();
   //   }
   //   
   //   
   //   /**Limits line to <code>length</code> characters inclusive of \"...\" trailing characters indicating the string was in fact choppped")
   //   public static String chop(String str, int length)
   //   {
   //      if (str.length() > length)
   //      {
   //         str = str.substring(0, length - 4);
   //         str += " ...";
   //      }
   //      return str;
   //   }
   //   
   //   
   //   /**Appends spaces until the string is at least <code>length</code> characters long")
   //   public static String pad(String str, int length)
   //   {
   //      if (str.length() > length)
   //         str = str.substring(0, length);
   //   
   //      while (str.length() < length)
   //      {
   //         str += " ";
   //      }
   //      return str;
   //   }
   //
   //   /**
   //    * A
   //    * @param string
   //    * @param target
   //    * @param replacement
   //    * @return
   //    */
   //   public static String replace(String string, String target, String replacement)
   //   {
   //      for (int i = 0; i < 100 && string.indexOf(target) >= 0; i++)
   //      {
   //         string = string.replace(target, replacement);
   //      }
   //      return string;
   //   }

   //   
   //   public static String replace(String string, String[][] replacements)
   //   {
   //      boolean changed = false;
   //      int limiter = 0;
   //      do
   //      {
   //         changed = false;
   //         limiter++;
   //
   //         for (int i = 0; i < replacements.length; i++)
   //         {
   //            if (string.indexOf(replacements[i][0]) >= 0)
   //            {
   //               string = string.replace(replacements[i][0], replacements[i][1]);
   //               changed = true;
   //               break;
   //            }
   //         }
   //
   //      }
   //      while (changed && limiter < 100);
   //
   //      return string;
   //   }

   //   /**Returns true if the string contains a * or a ?")
   //   public static boolean isWildcard(String str)
   //   {
   //      return str.indexOf('*') >= 0 || str.indexOf('?') >= 0;
   //   }
   //   
   //   
   //   /**Pattern matches the string using ? to indicate any one single value and * to indicate any 0-n multiple values")
   //   public static boolean wildcardMatch(String wildcard, String string)
   //   {
   //      if (empty(wildcard) || empty(string))
   //         return false;
   //   
   //      if (!isWildcard(wildcard))
   //         return wildcard.equals(string);
   //      else
   //         return regexMatch(wildcardToRegex(wildcard), string);
   //   }
   //   
   //   
   //   /**Performs string.matches() but also checks for null")
   //   public static boolean regexMatch(String regex, String string)
   //   {
   //      if (empty(regex) || empty(string))
   //         return false;
   //   
   //      return string.matches(regex);
   //   }
   //   
   //   /**
   //    * @see http://www.rgagnon.com/javadetails/java-0515.html
   //    * @param wildcard
   //    * @return
   //    */
   //
   //   /**Converts a * and ? wildcard style patterns into regex style pattern")
   //   public static String wildcardToRegex(String wildcard)
   //   {
   //      wildcard = wildcard.replace("**", "*");
   //      StringBuffer s = new StringBuffer(wildcard.length());
   //      s.append('^');
   //      for (int i = 0, is = wildcard.length(); i < is; i++)
   //      {
   //         char c = wildcard.charAt(i);
   //         switch (c)
   //         {
   //            case '*':
   //               s.append(".*");
   //               break;
   //            case '?':
   //               s.append(".");
   //               break;
   //            // escape special regexp-characters
   //            case '(':
   //            case ')':
   //            case '[':
   //            case ']':
   //            case '$':
   //            case '^':
   //            case '.':
   //            case '{':
   //            case '}':
   //            case '|':
   //            case '\\':
   //               s.append("\\");
   //               s.append(c);
   //               break;
   //            default :
   //               s.append(c);
   //               break;
   //         }
   //      }
   //      s.append('$');
   //      return (s.toString());
   //   }
   //   
   //   /**
   //   * Convenience methods for escaping special characters related to HTML, XML,
   //   * and regular expressions.
   //   *
   //   * <P>To keep you safe by default, WEB4J goes to some effort to escape
   //   * characters in your data when appropriate, such that you <em>usually</em>
   //   * don't need to think too much about escaping special characters. Thus, you
   //   *  shouldn't need to <em>directly</em> use the services of this class very often.
   //   *
   //   * <P><span class='highlight'>For Model Objects containing free form user input,
   //   * it is highly recommended that you use {@link SafeText}, not <tt>String</tt></span>.
   //   * Free form user input is open to malicious use, such as
   //   * <a href='http://www.owasp.org/index.php/Cross_Site_Scripting'>Cross Site Scripting</a>
   //   * attacks.
   //   * Using <tt>SafeText</tt> will protect you from such attacks, by always escaping
   //   * special characters automatically in its <tt>toString()</tt> method.
   //   *
   //   * <P>The following WEB4J classes will automatically escape special characters
   //   * for you, when needed :
   //   * <ul>
   //   * <li>the {@link SafeText} class, used as a building block class for your
   //   * application's Model Objects, for modeling all free form user input
   //   * <li>the {@link Populate} tag used with forms
   //   * <li>the {@link Report} class used for creating quick reports
   //   * <li>the {@link Text}, {@link TextFlow}, and {@link Tooltips} custom tags used
   //   * for translation
   //   * </ul>
   //   *
   //   * @see http://www.javapractices.com/topic/TopicAction.do?Id=96
   //   */
   //
   //   /**
   //    * Escape characters for text appearing in HTML markup.
   //    *
   //    * <P>This method exists as a defence against Cross Site Scripting (XSS) hacks.
   //    * This method escapes all characters recommended by the Open Web App
   //    * Security Project -
   //    * <a href='http://www.owasp.org/index.php/Cross_Site_Scripting'>link</a>.
   //    *
   //    * <P>The following characters are replaced with corresponding HTML
   //    * character entities :
   //    * <table border='1' cellpadding='3' cellspacing='0'>
   //    * <tr><th> Character </th><th> Encoding </th></tr>
   //    * <tr><td> < </td><td> &lt; </td></tr>
   //    * <tr><td> > </td><td> &gt; </td></tr>
   //    * <tr><td> & </td><td> &amp; </td></tr>
   //    * <tr><td> " </td><td> &quot;</td></tr>
   //    * <tr><td> ' </td><td> &#039;</td></tr>
   //    * <tr><td> ( </td><td> &#040;</td></tr>
   //    * <tr><td> ) </td><td> &#041;</td></tr>
   //    * <tr><td> # </td><td> &#035;</td></tr>
   //    * <tr><td> % </td><td> &#037;</td></tr>
   //    * <tr><td> ; </td><td> &#059;</td></tr>
   //    * <tr><td> + </td><td> &#043; </td></tr>
   //    * <tr><td> - </td><td> &#045; </td></tr>
   //    * </table>
   //    *
   //    * <P>Note that JSTL's {@code <c:out>} escapes <em>only the first
   //    * five</em> of the above characters.
   //    */
   //
   //   /**Escape HTML special characters so this string can be displayed as text not marketup in an HTML document")
   //   public static String forHTML(String aText)
   //   {
   //      final StringBuilder result = new StringBuilder();
   //      final StringCharacterIterator iterator = new StringCharacterIterator(aText);
   //      char character = iterator.current();
   //      while (character != CharacterIterator.DONE)
   //      {
   //         if (character == '<')
   //         {
   //            result.append("&lt;");
   //         }
   //         else if (character == '>')
   //         {
   //            result.append("&gt;");
   //         }
   //         else if (character == '&')
   //         {
   //            result.append("&amp;");
   //         }
   //         else if (character == '\"')
   //         {
   //            result.append("&quot;");
   //         }
   //         else if (character == '\'')
   //         {
   //            result.append("&#039;");
   //         }
   //         else if (character == '(')
   //         {
   //            result.append("&#040;");
   //         }
   //         else if (character == ')')
   //         {
   //            result.append("&#041;");
   //         }
   //         else if (character == '#')
   //         {
   //            result.append("&#035;");
   //         }
   //         else if (character == '%')
   //         {
   //            result.append("&#037;");
   //         }
   //         else if (character == ';')
   //         {
   //            result.append("&#059;");
   //         }
   //         else if (character == '+')
   //         {
   //            result.append("&#043;");
   //         }
   //         else if (character == '-')
   //         {
   //            result.append("&#045;");
   //         }
   //         else
   //         {
   //            //the char is not a special one
   //            //add it to the result as is
   //            result.append(character);
   //         }
   //         character = iterator.next();
   //      }
   //      return result.toString();
   //   }
   //   
   //   /**
   //    * Synonym for <tt>URLEncoder.encode(String, "UTF-8")</tt>.
   //    *
   //    * <P>Used to ensure that HTTP query strings are in proper form, by escaping
   //    * special characters such as spaces.
   //    *
   //    * <P>It is important to note that if a query string appears in an <tt>HREF</tt>
   //    * attribute, then there are two issues - ensuring the query string is valid HTTP
   //    * (it is URL-encoded), and ensuring it is valid HTML (ensuring the
   //    * ampersand is escaped).
   //    */
   //
   //   /**Does URLEncoder.encode() but throws a RuntimeException instead of an UnsupportedEncodingException")
   //   public static String forURL(String aURLFragment)
   //   {
   //      String result = null;
   //      try
   //      {
   //         result = URLEncoder.encode(aURLFragment, "UTF-8");
   //      }
   //      catch (UnsupportedEncodingException ex)
   //      {
   //         throw new RuntimeException("UTF-8 not supported", ex);
   //      }
   //      return result;
   //   }
   //   
   //   /**
   //   * Escape characters for text appearing as XML data, between tags.
   //   *
   //   * <P>The following characters are replaced with corresponding character entities :
   //   * <table border='1' cellpadding='3' cellspacing='0'>
   //   * <tr><th> Character </th><th> Encoding </th></tr>
   //   * <tr><td> < </td><td> &lt; </td></tr>
   //   * <tr><td> > </td><td> &gt; </td></tr>
   //   * <tr><td> & </td><td> &amp; </td></tr>
   //   * <tr><td> " </td><td> &quot;</td></tr>
   //   * <tr><td> ' </td><td> &#039;</td></tr>
   //   * </table>
   //   *
   //   * <P>Note that JSTL's {@code <c:out>} escapes the exact same set of
   //   * characters as this method. <span class='highlight'>That is, {@code <c:out>}
   //   *  is good for escaping to produce valid XML, but not for producing safe HTML.</span>
   //   */
   //
   //   /**Escape xml tag characters so that this can be rendered as text instead of markup when included in a xml/html document")
   //   public static String forXML(String aText)
   //   {
   //      final StringBuilder result = new StringBuilder();
   //      final StringCharacterIterator iterator = new StringCharacterIterator(aText);
   //      char character = iterator.current();
   //      while (character != CharacterIterator.DONE)
   //      {
   //         if (character == '<')
   //         {
   //            result.append("&lt;");
   //         }
   //         else if (character == '>')
   //         {
   //            result.append("&gt;");
   //         }
   //         else if (character == '\"')
   //         {
   //            result.append("&quot;");
   //         }
   //         else if (character == '\'')
   //         {
   //            result.append("&#039;");
   //         }
   //         else if (character == '&')
   //         {
   //            result.append("&amp;");
   //         }
   //         else
   //         {
   //            //the char is not a special one
   //            //add it to the result as is
   //            result.append(character);
   //         }
   //         character = iterator.next();
   //      }
   //      return result.toString();
   //   }
   //   
   //   /**
   //   * Return <tt>aText</tt> with all <tt>'<'</tt> and <tt>'>'</tt> characters
   //   * replaced by their escaped equivalents.
   //   */
   //
   //   /**Return text with all '<' and '>' characters replaced by their escaped equivalents.")
   //   public static String toDisableTags(String text)
   //   {
   //      final StringBuilder result = new StringBuilder();
   //      final StringCharacterIterator iterator = new StringCharacterIterator(text);
   //      char character = iterator.current();
   //      while (character != CharacterIterator.DONE)
   //      {
   //         if (character == '<')
   //         {
   //            result.append("&lt;");
   //         }
   //         else if (character == '>')
   //         {
   //            result.append("&gt;");
   //         }
   //         else
   //         {
   //            //the char is not a special one
   //            //add it to the result as is
   //            result.append(character);
   //         }
   //         character = iterator.next();
   //      }
   //      return result.toString();
   //   }
   //   
   //   /**
   //   * Replace characters having special meaning in regular expressions
   //   * with their escaped equivalents, preceded by a '\' character.
   //   *
   //   * <P>The escaped characters include :
   //   *<ul>
   //   *<li>.
   //   *<li>\
   //   *<li>?, * , and +
   //   *<li>&
   //   *<li>:
   //   *<li>{ and }
   //   *<li>[ and ]
   //   *<li>( and )
   //   *<li>^ and $
   //   *</ul>
   //   *
   //   */
   //
   //   /**Escapes any regex specicial characters")
   //   public static String forRegex(String aRegexFragment)
   //   {
   //      final StringBuilder result = new StringBuilder();
   //   
   //      final StringCharacterIterator iterator = new StringCharacterIterator(aRegexFragment);
   //      char character = iterator.current();
   //      while (character != CharacterIterator.DONE)
   //      {
   //         /*
   //         * All literals need to have backslashes doubled.
   //         */
   //   if(character=='.')
   //
   //   {
   //      result.append("\\.");
   //   }else if(character=='\\')
   //   {
   //      result.append("\\\\");
   //   }else if(character=='?')
   //   {
   //      result.append("\\?");
   //   }else if(character=='*')
   //   {
   //      result.append("\\*");
   //   }else if(character=='+')
   //   {
   //      result.append("\\+");
   //   }else if(character=='&')
   //   {
   //      result.append("\\&");
   //   }else if(character==':')
   //   {
   //      result.append("\\:");
   //   }else if(character=='{')
   //   {
   //      result.append("\\{");
   //   }else if(character=='}')
   //   {
   //      result.append("\\}");
   //   }else if(character=='[')
   //   {
   //      result.append("\\[");
   //   }else if(character==']')
   //   {
   //      result.append("\\]");
   //   }else if(character=='(')
   //   {
   //      result.append("\\(");
   //   }else if(character==')')
   //   {
   //      result.append("\\)");
   //   }else if(character=='^')
   //   {
   //      result.append("\\^");
   //   }else if(character=='$')
   //   {
   //      result.append("\\$");
   //   }else
   //   {
   //      //the char is not a special one
   //      //add it to the result as is
   //      result.append(character);
   //   }character=iterator.next();
   //}return result.toString();}
}