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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.Flushable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLDecoder;

public class Streams
{
   public static final int  KB  = 1048;
   public static final int  MB  = 1048576;
   public static final long GB  = 1073741824;
   public static final int  K64 = 65536;

   @ApiMethod
   @Comment(value = "Writes each object to System.err with a space between")
   public static void err(Object... obj)
   {
      for (Object o : obj)
      {
         System.err.print(o);
      }
      System.err.println("");
   }

   @ApiMethod
   @Comment(value = "Writes each object to System.out with a space between")
   public static void out(Object... obj)
   {
      for (Object o : obj)
      {
         System.out.print(o);
      }
      System.out.println("");
   }

   @ApiMethod
   @Comment(value = "Read all of the stream to a string and close the stream.  Throws RuntimeException instead of IOException")
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
         Lang.rethrow(ex);
      }
      return null;
   }

   @ApiMethod
   @Comment(value = "Simply calls stream.flush() but throws RuntimeException instead of IOException")
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
         Lang.rethrow(ex);
      }
   }

   @ApiMethod
   @Comment(value = "Copy all data from src to dst and close the streams")
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
         Lang.close(src);
         Lang.close(dest);
      }
   }

   @ApiMethod
   @Comment(value = "Copy all data from src to dst and close the reader/writer")
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
         Lang.close(src);
         Lang.close(dest);
      }
   }

   @ApiMethod
   @Comment(value = "Attempts to locate the stream as a file, url, or classpath resource")
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

   @ApiMethod
   @Comment(value = "Attempts to locate the stream as a file, url, or classpath resource and then reads it all as a string")
   public static String read(String fileOrUrl) throws Exception
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      InputStream in = findInputStream(fileOrUrl);
      Streams.pipe(in, out);
      return new String(out.toByteArray());
   }

   //   public static BufferedReader in  = null;

   //   public static BufferedWriter out = null;

   //   static
   //   {
   //      //adds an extra return after the program ends to make sure
   //      //that the prompt is back at the left of the screen
   //      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
   //         {
   //            @Override
   //            public void run()
   //            {
   //               if (out != null)
   //               {
   //                  try
   //                  {
   //                     Streams.flush(out);
   //                  }
   //                  catch (Exception ex)
   //                  {
   //                     ex.printStackTrace();
   //                  }
   //               }
   //            }
   //         }));
   //   }

   //   @ApiMethod
   //   @Comment(value = "Writes to a BufferedWriter wrapping System.out but does not call stream.flush, call Streams.flush as desired.")
   //   public static synchronized void out(Object... obj)
   //   {
   //      if (out == null)
   //         out = new BufferedWriter(new OutputStreamWriter(System.out), MB);
   //
   //      try
   //      {
   //         for (Object o : obj)
   //         {
   //            if (o instanceof InputStream)
   //            {
   //               BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) o));
   //               String line = null;
   //
   //               while ((line = reader.readLine()) != null)
   //               {
   //                  out.write(line);
   //                  out.write(System.getProperty("line.separator"));
   //               }
   //            }
   //            else
   //            {
   //               out.write(o != null ? o.toString() : "null");
   //            }
   //         }
   //         out.write(System.getProperty("line.separator"));
   //      }
   //      catch (Exception ex)
   //      {
   //         Lang.rethrow(ex);
   //      }
   //   }

   //   @ApiMethod
   //   @Comment(value = "Read the next line from a BufferedReader wrapping System.in.  Using this means that for the duration of the JVM")
   //   public static synchronized String in()
   //   {
   //      try
   //      {
   //         if (in == null)
   //            in = new BufferedReader(new InputStreamReader(System.in), MB);
   //
   //         return in.readLine();
   //      }
   //      catch (Exception ex)
   //      {
   //         Lang.rethrow(ex);
   //      }
   //      return null;
   //   }

}
