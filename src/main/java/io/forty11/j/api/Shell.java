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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Shell
{
   @ApiMethod
   public static String getPathSeparator()
   {
      return System.getProperty("path.separator");
   }

   @ApiMethod
   public static String getLineSeparator()
   {
      return System.getProperty("line.separator");
   }

   @ApiMethod
   public static int getTerminalWidth()
   {
      try
      {
         return Integer.parseInt(run("tput cols"));
         //Process proc = Runtime.getRuntime().exec("tput cols");
         //String output = Streams.read(proc.getInputStream());
         //return Integer.parseInt(output);
      }
      catch (Exception ex)
      {

      }
      return 120;
   }

   @ApiMethod
   public static String run(String cmd) throws Exception
   {
      return run(Strings.parseArgs(cmd));
   }

   @ApiMethod
   public static String run(List<String> cmd) throws Exception
   {
      return run(cmd.toArray(new String[cmd.size()]));
   }

   @ApiMethod
   public static String run(String... cmd) throws Exception
   {
      String debug = new ArrayList(Arrays.asList(cmd)).toString();
      debug = debug.replaceAll(",", "");
      debug = debug.substring(1, debug.length() - 1);

      String output = null;
      String error = null;
      int status = 0;

      try
      {
         Process process = Runtime.getRuntime().exec(cmd);
         StreamReader inReader = new StreamReader(process.getInputStream());
         StreamReader errReader = new StreamReader(process.getErrorStream());

         inReader.start();
         errReader.start();
         status = process.waitFor();
         inReader.join();
         errReader.join();

         output = inReader.getResult();
         error = errReader.getResult();

         if (status != 0)
         {
            if (!Lang.empty(error))
            {
               throw new IOException(error);
            }
            else
            {
               throw new IOException("error executing: " + debug + "\r\n");
            }
         }
      }
      finally
      {
         //String msg = "exec(" + status + ") \"" + debug + "\"\r\n" + output + error;
         //J.info(msg);
      }

      return output;
   }

   static class StreamReader extends Thread
   {
      private InputStream  is;
      private StringWriter sw;

      StreamReader(InputStream is)
      {
         this.is = is;
         sw = new StringWriter();
      }

      @Override
      public void run()
      {
         try
         {
            int c;
            while ((c = is.read()) != -1)
               sw.write(c);
         }
         catch (IOException e)
         {
            ;
         }
      }

      String getResult()
      {
         return sw.toString();
      }
   }

}
