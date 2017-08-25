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
   public static final int            KB  = 1048;
   public static final int            MB  = 1048576;
   public static final long           GB  = 1073741824;
   public static final int            K64 = 65536;

   public static final BufferedReader in  = new BufferedReader(new InputStreamReader(System.in), MB);

   //TODO add support for character encodings
   public static final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out), MB);

   static
   {
      //adds an extra return after the program ends to make sure
      //that the prompt is back at the left of the screen
      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
         {
            @Override
            public void run()
            {
               try
               {
                  Streams.flush(out);
               }
               catch (Exception ex)
               {
                  ex.printStackTrace();
               }
            }
         }));
   }

   @ApiMethod
   public static String read(InputStream in) throws Exception
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      pipe(in, out);
      return new String(out.toByteArray());
   }

   public static String readLine(InputStream in) throws Exception
   {
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      return reader.readLine();
   }

   @ApiMethod
   public static String in() throws Exception
   {
      return in.readLine();
   }

   @ApiMethod
   public static void out(Object... obj) throws RuntimeException
   {
      try
      {
         for (Object o : obj)
         {
            if (o instanceof InputStream)
            {
               BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) o));
               String line = null;

               while ((line = reader.readLine()) != null)
               {
                  out.write(line);
                  out.write(System.getProperty("line.separator"));
               }
            }
            else
            {
               out.write(o != null ? o.toString() : "null");
            }
         }
         out.write(System.getProperty("line.separator"));
      }
      catch (Exception ex)
      {
         Lang.rethrow(ex);
      }
   }

   @ApiMethod
   public static void err(Object... obj)
   {
      for (Object o : obj)
      {
         System.err.print(o);
      }
      System.err.println("");
   }

   @ApiMethod
   public static void flush(Flushable stream) throws Exception
   {
      if (stream != null)
         stream.flush();
   }

   @ApiMethod
   public static void close(Closeable stream) throws Exception
   {
      if (stream != null)
         stream.close();
   }

   @ApiMethod
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

   @ApiMethod
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

   @ApiMethod
   public static InputStream findInputStream(String url)
   {
      try
      {
         if (url.startsWith("file:/"))
         {
            url = URLDecoder.decode(url);
         }
         if (url.startsWith("file:///"))
         {
            url = url.substring(7, url.length());
         }
         if (url.startsWith("file:/"))
         {
            url = url.substring(5, url.length());
         }

         if (url.indexOf(':') >= 0)
         {
            return new URL(url).openStream();
         }
         else if (new File(url).exists())
         {
            return new FileInputStream(url);
         }
         else
         {
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(url);
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
   public static String read(String resource) throws Exception
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      InputStream in = findInputStream(resource);
      Streams.pipe(in, out);
      return new String(out.toByteArray());
   }

}
