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
package io.forty11.j.api;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.forty11.j.it.FileIt;
import io.forty11.j.it.It;

public class Files
{

   static String    illegalFileNameCharacters       = "\"*/:<>?\\|";
   //static String    unsafeFileNameCharacters        = illegalFileNameCharacters + //
   //                                                       " \'\"~`!@#$%^&*()[]{}+=;:?/\\,<>\t\r\n";
   /* So that spaces aren't considered unsafe */
   static String    unsafeWindowsFileNameCharacters = illegalFileNameCharacters +   //
         "\'\"~`!@#$%^&*()[]{}+=;:?/\\,<>\t\r\n";

   static ArrayList invalidFileNames                = new ArrayList();

   static
   {
      invalidFileNames.add("CLOCK$");
      invalidFileNames.add("AUX");
      invalidFileNames.add("CON");
      invalidFileNames.add("NUL");
      invalidFileNames.add("PRN");
      invalidFileNames.add("COM1");
      invalidFileNames.add("COM2");
      invalidFileNames.add("COM3");
      invalidFileNames.add("COM4");
      invalidFileNames.add("COM5");
      invalidFileNames.add("COM6");
      invalidFileNames.add("COM7");
      invalidFileNames.add("COM8");
      invalidFileNames.add("COM9");
      invalidFileNames.add("LPT1");
      invalidFileNames.add("LPT2");
      invalidFileNames.add("LPT3");
      invalidFileNames.add("LPT4");
      invalidFileNames.add("LPT5");
      invalidFileNames.add("LPT6");
      invalidFileNames.add("LPT7");
      invalidFileNames.add("LPT8");
      invalidFileNames.add("LPT9");
   }

   @ApiMethod
   public static String sterilizeFileName(String fileName)
   {
      fileName = encodeFileName(fileName);

      StringBuffer buff = new StringBuffer("");
      for (int i = 0; i < fileName.length(); i++)
      {
         if (unsafeWindowsFileNameCharacters.indexOf(fileName.charAt(i)) < 0)
         {
            buff.append(fileName.charAt(i));
         }
         else
         {
            buff.append('_');
         }
      }

      return buff.toString();
   }

   public static String getFileName(String fullName)
   {
      fullName = fullName.replace('\\', '/');
      String fileName = fullName;

      String[] paths = fullName.split("/");
      if (paths.length > 0)
      {
         fileName = paths[paths.length - 1];
      }

      fileName = URLDecoder.decode(fileName);
      return fileName;
   }

   public static String getFileExtension(String fileName)
   {
      fileName = getFileName(fileName);
      int lastDot = fileName.lastIndexOf(".");
      int length = fileName.length();
      if (lastDot > 0 && length - lastDot <= 5)
      {
         String ext = fileName.substring(lastDot + 1, length);
         return ext;
      }

      return null;
   }

   public static String encodeFileName(String path)
   {
      path = normalizePath(path);
      String[] parts = split(path);

      for (int i = 0; i < parts.length; i++)
      {
         String part = parts[i];

         // -- check for invalid names
         String extension = getFileExtension(part);
         String prefix = extension == null ? part : part.substring(0, part.lastIndexOf('.'));
         extension = extension == null ? "" : extension;
         if (invalidFileNames.contains(prefix))
         {
            prefix = prefix + "_";
         }
         part = prefix + "." + extension;

         // -- check for invalid characters
         StringBuffer partBuffer = new StringBuffer(part);
         for (int j = 0; j < partBuffer.length(); j++)
         {
            char ch = partBuffer.charAt(j);
            if (ch < 31)
            {
               partBuffer.setCharAt(j, '_');
            }
            else
            {
               if (illegalFileNameCharacters.indexOf(ch) > -1)
               {
                  partBuffer.setCharAt(j, '_');
               }
            }
         }

         parts[i] = partBuffer.toString();

      }

      StringBuffer newPath = new StringBuffer("");
      if (path.startsWith("/"))
      {
         newPath.append("/");
      }

      for (int i = 0; i < parts.length; i++)
      {
         newPath.append(parts[i]);
         if (i + 1 < parts.length)
         {
            newPath.append("/");
         }
      }

      path = normalizePath(newPath.toString());

      return path;

   }

   public static String[] split(String path)
   {
      path = path.replace('\\', '/');
      List partsList = new ArrayList(Arrays.asList(path.split("/")));
      for (int j = 0; j < partsList.size(); j++)
      {
         if (partsList.get(j) == null || (((String) partsList.get(j))).trim().equals(""))
         {
            partsList.remove(j);
            j--;
         }
      }
      return (String[]) partsList.toArray(new String[partsList.size()]);
   }

   public static String normalizePath(String path)
   {
      if (path == null)
      {
         return "";
      }

      path = path.replace('\\', '/');
      String[] parts = split(path);
      StringBuffer buffer = new StringBuffer();

      if (path.length() > 0)
      {
         if (path.charAt(0) == '/')
         {
            buffer.append('/');
         }
      }

      for (int i = 0; i < parts.length; i++)
      {
         buffer.append(parts[i]);
         if (i + 1 < parts.length)
         {
            buffer.append('/');
         }
      }

      return buffer.toString();
   }

   @ApiMethod
   public static boolean isLink(File file) throws Exception
   {
      String cnnpath = file.getCanonicalPath();
      String abspath = file.getAbsolutePath();
      return !abspath.equals(cnnpath);
   }

   //   @ApiMethod
   //   public static String read(String file) throws Exception
   //   {
   //      return read(new File(file));
   //   }

   @ApiMethod
   public static String read(File file) throws Exception
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      FileInputStream in = new FileInputStream(file);
      Streams.pipe(in, out);
      return new String(out.toByteArray());
   }

   @ApiMethod
   public static void write(File file, String text) throws Exception
   {
      if (!file.exists())
         file.getParentFile().mkdirs();

      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
      bw.write(text);
      bw.flush();
      bw.close();
   }

   @ApiMethod
   public static void write(String file, String text) throws Exception
   {
      if(text == null)
         return;
      write(new File(file), text);
   }

   /**
    * @return a sorted list of files in the dir
    */
   @ApiMethod
   public static It<File> listFiles(File dir)
   {
      return new FileIt(dir);
   }

   @ApiMethod
   public static File file(String pathOrUrl)
   {
      if (pathOrUrl.startsWith("file:/"))
      {
         //pathOrUrl = URLDecoder.decode(pathOrUrl);
         pathOrUrl = URLDecoder.decode(pathOrUrl);
      }

      if (pathOrUrl.startsWith("file:///"))
      {
         pathOrUrl = pathOrUrl.substring(7, pathOrUrl.length());
      }

      if (pathOrUrl.startsWith("file:/"))
      {
         pathOrUrl = pathOrUrl.substring(5, pathOrUrl.length());
      }

      return new File(pathOrUrl);
   }

   @ApiMethod
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

   @ApiMethod
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

   @ApiMethod
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
         Lang.rethrow(ex);
      }
      return false;
   }

   protected static void copyDir(File srcDir, File dstDir)
   {
      for (File file : Files.listFiles(srcDir))
      {
         copy(srcDir, file, dstDir);
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

   @ApiMethod
   public static File createTempFile(File file) throws IOException
   {
      if (file == null)
         return createTempFile("working.tmp");
      else
         return createTempFile(file.getName());
   }

   @ApiMethod
   public static File createTempFile(String fileName) throws IOException
   {
      if (Lang.empty(fileName))
         fileName = "working.tmp";

      fileName = fileName.trim();
      fileName = fileName.replace('\\', '/');

      if (fileName.endsWith("/"))
      {
         fileName = "working.tmp";
      }
      else
      {
         if (fileName.lastIndexOf('/') > 0)
         {
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1, fileName.length());
         }
      }

      if (Lang.empty(fileName))
         fileName = "working.tmp";

      fileName = Strings.slugify(fileName);
      if (fileName.lastIndexOf('.') > 0)
      {
         String prefix = fileName.substring(0, fileName.lastIndexOf('.'));
         String suffix = fileName.substring(fileName.lastIndexOf('.'), fileName.length());

         return File.createTempFile(prefix + "-", suffix);
      }
      else
      {
         return File.createTempFile(fileName, "");
      }
   }
}
