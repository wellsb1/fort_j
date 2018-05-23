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
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class Strings
{
   @ApiMethod
   public static boolean contains(String string, String target)
   {
      if (string == null || target == null)
         return false;
      else
         return string.indexOf(target) >= 0;
   }

   //   @ApiMethod
   //   public static String implode(String glue, Object... pieces)
   //   {
   //      StringBuffer str = new StringBuffer("");
   //      for (int i = 0; pieces != null && i < pieces.length; i++)
   //      {
   //         str.append(pieces[i]);
   //         if (i < pieces.length - 1)
   //            str.append(glue);
   //      }
   //      return str.toString();
   //   }

   @ApiMethod
   public static List<String> explode(String str, String delim)
   {
      String[] parts = str.split(delim);
      for (int i = 0; i < parts.length; i++)
      {
         parts[i] = parts[i].trim();
      }
      return Arrays.asList(parts);
   }

   @ApiMethod
   public static String startUpper(String str)
   {
      if (!Lang.empty(str))
      {
         str = Character.toUpperCase(str.charAt(0)) + str.substring(1, str.length());
      }
      return str;
   }

   @ApiMethod
   public static String startLower(String str)
   {
      if (!Lang.empty(str))
      {
         str = Character.toLowerCase(str.charAt(0)) + str.substring(1, str.length());
      }
      return str;
   }

   @ApiMethod
   public static String fromCamelCase(String string)
   {
      //convert camel case style
      char ch = string.charAt(0);
      ch = Character.toTitleCase(ch);

      StringBuffer buff = new StringBuffer(ch + "");
      for (int i = 1; i < string.length(); i++)
      {
         ch = string.charAt(i);

         if (ch == '-')
         {
            buff.append(' ');
            if (i < string.length() - 1)
            {
               if (!Character.isUpperCase(string.charAt(i + 1)))
               {
                  buff.append(Character.toUpperCase(string.charAt(i + 1)));
                  i++;
                  continue;
               }
            }
         }

         if (Character.isUpperCase(ch) && //
               !Character.isUpperCase(string.charAt(i - 1)))
         {
            buff.append(' ');
         }

         if (Character.isDigit(ch) && Character.isLetter(string.charAt(i - 1)) || Character.isLetter(ch) && Character.isDigit(string.charAt(i - 1)))
         {
            buff.append(' ');
         }

         buff.append(string.charAt(i));
      }

      String str = buff.toString();
      str = str.replace('_', ' ');

      return str;
   }

   @ApiMethod
   public static String slugify(String str)
   {
      if (str == null)
      {
         return null;
      }

      str = str.toLowerCase().trim();

      str = str.replaceAll("[']+", "");
      str = str.replaceAll("[^a-z0-9]+", "-");

      //removes consecutive -'s
      str = str.replaceAll("([\\-])(\\1{2,})", "$1");

      // remove preceding and trailing dashes
      str = str.replaceAll("^-", "");
      str = str.replaceAll("-$", "");

      return str;
   }

   @ApiMethod
   public static String sha1(byte[] bytes)
   {
      return hash(bytes, "SHA-1");
   }

   @ApiMethod
   public static String md5(byte[] bytes)
   {
      return hash(bytes, "MD5");
   }

   @ApiMethod
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
         Lang.rethrow(ex);
      }
      return null;
   }

   /**
    * Instance of <code>${key}</code> in <code>str</code> will be replaced
    * with <code>value</code> .
    * 
    * Variables must be wapped in ${}
    * @param str
    * @param values
    * @return
    */
   @ApiMethod
   public static String replaceAll(String str, Map<String, Object> values)
   {
      StringBuffer buff = new StringBuffer("");
      Pattern p = Pattern.compile("\\$\\{([^\\}]*)\\}");
      Matcher m = p.matcher(str);
      while (m.find())
      {
         String key = m.group(1);
         String value = Matcher.quoteReplacement(values.get(key) + "");

         m.appendReplacement(buff, value);
      }
      m.appendTail(buff);
      return buff.toString();
   }

   @ApiMethod
   public static String indent(String str, int indent)
   {
      try
      {
         StringBuffer buff = new StringBuffer();
         String line = null;
         BufferedReader reader = new BufferedReader(new StringReader(str));
         while ((line = reader.readLine()) != null)
         {
            buff.append(pad("", indent)).append(line).append(Shell.getLineSeparator());
         }

         return buff.toString();
      }
      catch (Exception ex)
      {

      }
      return "null";
   }

   @ApiMethod
   public static String wrap(String str, int wrap)
   {
      StringBuffer buff = new StringBuffer();

      while (str.length() > 0)
      {
         int length = str.length();
         if (length > wrap)
         {
            buff.append(str.substring(0, wrap));
            buff.append(Shell.getLineSeparator());
            str = str.substring(wrap, str.length());
         }
         else
         {
            buff.append(str);
            break;
         }
      }

      return buff.toString().trim();
   }

   @ApiMethod
   public static String chop(String str, int length)
   {
      if (str.length() > length)
      {
         str = str.substring(0, length - 4);
         str += " ...";
      }
      return str;
   }

   @ApiMethod
   public static String pad(String str, int length)
   {
      if (str.length() > length)
         str = str.substring(0, length);

      while (str.length() < length)
      {
         str += " ";
      }
      return str;
   }

   @ApiMethod
   public static String replace(String string, String target, String replacement)
   {
      for (int i = 0; i < 100 && string.indexOf(target) >= 0; i++)
      {
         string = string.replace(target, replacement);
      }
      return string;
   }

   @ApiMethod
   public static String replace(String string, String[][] replacements)
   {
      boolean changed = false;
      int limiter = 0;
      do
      {
         changed = false;
         limiter++;

         for (int i = 0; i < replacements.length; i++)
         {
            if (string.indexOf(replacements[i][0]) >= 0)
            {
               string = string.replace(replacements[i][0], replacements[i][1]);
               changed = true;
               break;
            }
         }

      }
      while (changed && limiter < 100);

      return string;
   }

   @ApiMethod
   public static boolean isWildcard(String str)
   {
      return str.indexOf('*') >= 0 || str.indexOf('?') >= 0;
   }

   @ApiMethod
   public static boolean wildcardMatch(String wildcard, String string)
   {
      if (Lang.empty(wildcard) || Lang.empty(string))
         return false;

      if (!isWildcard(wildcard))
         return wildcard.equals(string);
      else
         return regexMatch(wildcardToRegex(wildcard), string);
   }

   @ApiMethod
   public static boolean regexMatch(String regex, String string)
   {
      if (Lang.empty(regex) || Lang.empty(string))
         return false;

      return string.matches(regex);
   }

   /**
    * @see http://www.rgagnon.com/javadetails/java-0515.html
    * @param wildcard
    * @return
    */
   @ApiMethod
   public static String wildcardToRegex(String wildcard)
   {
      wildcard = wildcard.replace("**", "*");
      StringBuffer s = new StringBuffer(wildcard.length());
      s.append('^');
      for (int i = 0, is = wildcard.length(); i < is; i++)
      {
         char c = wildcard.charAt(i);
         switch (c)
         {
            case '*':
               s.append(".*");
               break;
            case '?':
               s.append(".");
               break;
            // escape special regexp-characters
            case '(':
            case ')':
            case '[':
            case ']':
            case '$':
            case '^':
            case '.':
            case '{':
            case '}':
            case '|':
            case '\\':
               s.append("\\");
               s.append(c);
               break;
            default :
               s.append(c);
               break;
         }
      }
      s.append('$');
      return (s.toString());
   }

   /**
   * Convenience methods for escaping special characters related to HTML, XML, 
   * and regular expressions.
   * 
   * <P>To keep you safe by default, WEB4J goes to some effort to escape 
   * characters in your data when appropriate, such that you <em>usually</em>
   * don't need to think too much about escaping special characters. Thus, you
   *  shouldn't need to <em>directly</em> use the services of this class very often. 
   * 
   * <P><span class='highlight'>For Model Objects containing free form user input, 
   * it is highly recommended that you use {@link SafeText}, not <tt>String</tt></span>.
   * Free form user input is open to malicious use, such as
   * <a href='http://www.owasp.org/index.php/Cross_Site_Scripting'>Cross Site Scripting</a>
   * attacks. 
   * Using <tt>SafeText</tt> will protect you from such attacks, by always escaping 
   * special characters automatically in its <tt>toString()</tt> method.   
   * 
   * <P>The following WEB4J classes will automatically escape special characters 
   * for you, when needed : 
   * <ul>
   * <li>the {@link SafeText} class, used as a building block class for your 
   * application's Model Objects, for modeling all free form user input
   * <li>the {@link Populate} tag used with forms
   * <li>the {@link Report} class used for creating quick reports
   * <li>the {@link Text}, {@link TextFlow}, and {@link Tooltips} custom tags used 
   * for translation
   * </ul> 
   * 
   * @see http://www.javapractices.com/topic/TopicAction.do?Id=96
   */

   /**
    * Escape characters for text appearing in HTML markup.
    * 
    * <P>This method exists as a defence against Cross Site Scripting (XSS) hacks.
    * This method escapes all characters recommended by the Open Web App
    * Security Project - 
    * <a href='http://www.owasp.org/index.php/Cross_Site_Scripting'>link</a>.  
    * 
    * <P>The following characters are replaced with corresponding HTML 
    * character entities : 
    * <table border='1' cellpadding='3' cellspacing='0'>
    * <tr><th> Character </th><th> Encoding </th></tr>
    * <tr><td> < </td><td> &lt; </td></tr>
    * <tr><td> > </td><td> &gt; </td></tr>
    * <tr><td> & </td><td> &amp; </td></tr>
    * <tr><td> " </td><td> &quot;</td></tr>
    * <tr><td> ' </td><td> &#039;</td></tr>
    * <tr><td> ( </td><td> &#040;</td></tr> 
    * <tr><td> ) </td><td> &#041;</td></tr>
    * <tr><td> # </td><td> &#035;</td></tr>
    * <tr><td> % </td><td> &#037;</td></tr>
    * <tr><td> ; </td><td> &#059;</td></tr>
    * <tr><td> + </td><td> &#043; </td></tr>
    * <tr><td> - </td><td> &#045; </td></tr>
    * </table>
    * 
    * <P>Note that JSTL's {@code <c:out>} escapes <em>only the first 
    * five</em> of the above characters.
    */
   @ApiMethod
   public static String forHTML(String aText)
   {
      final StringBuilder result = new StringBuilder();
      final StringCharacterIterator iterator = new StringCharacterIterator(aText);
      char character = iterator.current();
      while (character != CharacterIterator.DONE)
      {
         if (character == '<')
         {
            result.append("&lt;");
         }
         else if (character == '>')
         {
            result.append("&gt;");
         }
         else if (character == '&')
         {
            result.append("&amp;");
         }
         else if (character == '\"')
         {
            result.append("&quot;");
         }
         else if (character == '\'')
         {
            result.append("&#039;");
         }
         else if (character == '(')
         {
            result.append("&#040;");
         }
         else if (character == ')')
         {
            result.append("&#041;");
         }
         else if (character == '#')
         {
            result.append("&#035;");
         }
         else if (character == '%')
         {
            result.append("&#037;");
         }
         else if (character == ';')
         {
            result.append("&#059;");
         }
         else if (character == '+')
         {
            result.append("&#043;");
         }
         else if (character == '-')
         {
            result.append("&#045;");
         }
         else
         {
            //the char is not a special one
            //add it to the result as is
            result.append(character);
         }
         character = iterator.next();
      }
      return result.toString();
   }

   /**
    * Synonym for <tt>URLEncoder.encode(String, "UTF-8")</tt>.
    *
    * <P>Used to ensure that HTTP query strings are in proper form, by escaping
    * special characters such as spaces.
    *
    * <P>It is important to note that if a query string appears in an <tt>HREF</tt>
    * attribute, then there are two issues - ensuring the query string is valid HTTP
    * (it is URL-encoded), and ensuring it is valid HTML (ensuring the 
    * ampersand is escaped).
    */
   @ApiMethod
   public static String forURL(String aURLFragment)
   {
      String result = null;
      try
      {
         result = URLEncoder.encode(aURLFragment, "UTF-8");
      }
      catch (UnsupportedEncodingException ex)
      {
         throw new RuntimeException("UTF-8 not supported", ex);
      }
      return result;
   }

   /**
   * Escape characters for text appearing as XML data, between tags.
   * 
   * <P>The following characters are replaced with corresponding character entities : 
   * <table border='1' cellpadding='3' cellspacing='0'>
   * <tr><th> Character </th><th> Encoding </th></tr>
   * <tr><td> < </td><td> &lt; </td></tr>
   * <tr><td> > </td><td> &gt; </td></tr>
   * <tr><td> & </td><td> &amp; </td></tr>
   * <tr><td> " </td><td> &quot;</td></tr>
   * <tr><td> ' </td><td> &#039;</td></tr>
   * </table>
   * 
   * <P>Note that JSTL's {@code <c:out>} escapes the exact same set of 
   * characters as this method. <span class='highlight'>That is, {@code <c:out>}
   *  is good for escaping to produce valid XML, but not for producing safe HTML.</span>
   */
   @ApiMethod
   public static String forXML(String aText)
   {
      final StringBuilder result = new StringBuilder();
      final StringCharacterIterator iterator = new StringCharacterIterator(aText);
      char character = iterator.current();
      while (character != CharacterIterator.DONE)
      {
         if (character == '<')
         {
            result.append("&lt;");
         }
         else if (character == '>')
         {
            result.append("&gt;");
         }
         else if (character == '\"')
         {
            result.append("&quot;");
         }
         else if (character == '\'')
         {
            result.append("&#039;");
         }
         else if (character == '&')
         {
            result.append("&amp;");
         }
         else
         {
            //the char is not a special one
            //add it to the result as is
            result.append(character);
         }
         character = iterator.next();
      }
      return result.toString();
   }

   /**
   * Return <tt>aText</tt> with all <tt>'<'</tt> and <tt>'>'</tt> characters
   * replaced by their escaped equivalents.
   */
   @ApiMethod
   public static String toDisableTags(String aText)
   {
      final StringBuilder result = new StringBuilder();
      final StringCharacterIterator iterator = new StringCharacterIterator(aText);
      char character = iterator.current();
      while (character != CharacterIterator.DONE)
      {
         if (character == '<')
         {
            result.append("&lt;");
         }
         else if (character == '>')
         {
            result.append("&gt;");
         }
         else
         {
            //the char is not a special one
            //add it to the result as is
            result.append(character);
         }
         character = iterator.next();
      }
      return result.toString();
   }

   /**
   * Replace characters having special meaning in regular expressions
   * with their escaped equivalents, preceded by a '\' character.
   *
   * <P>The escaped characters include :
   *<ul>
   *<li>.
   *<li>\
   *<li>?, * , and +
   *<li>&
   *<li>:
   *<li>{ and }
   *<li>[ and ]
   *<li>( and )
   *<li>^ and $
   *</ul>
   *
   */
   @ApiMethod
   public static String forRegex(String aRegexFragment)
   {
      final StringBuilder result = new StringBuilder();

      final StringCharacterIterator iterator = new StringCharacterIterator(aRegexFragment);
      char character = iterator.current();
      while (character != CharacterIterator.DONE)
      {
         /*
         * All literals need to have backslashes doubled.
         */
         if (character == '.')
         {
            result.append("\\.");
         }
         else if (character == '\\')
         {
            result.append("\\\\");
         }
         else if (character == '?')
         {
            result.append("\\?");
         }
         else if (character == '*')
         {
            result.append("\\*");
         }
         else if (character == '+')
         {
            result.append("\\+");
         }
         else if (character == '&')
         {
            result.append("\\&");
         }
         else if (character == ':')
         {
            result.append("\\:");
         }
         else if (character == '{')
         {
            result.append("\\{");
         }
         else if (character == '}')
         {
            result.append("\\}");
         }
         else if (character == '[')
         {
            result.append("\\[");
         }
         else if (character == ']')
         {
            result.append("\\]");
         }
         else if (character == '(')
         {
            result.append("\\(");
         }
         else if (character == ')')
         {
            result.append("\\)");
         }
         else if (character == '^')
         {
            result.append("\\^");
         }
         else if (character == '$')
         {
            result.append("\\$");
         }
         else
         {
            //the char is not a special one
            //add it to the result as is
            result.append(character);
         }
         character = iterator.next();
      }
      return result.toString();
   }

   /**
   * Disable all <tt><SCRIPT></tt> tags in <tt>aText</tt>.
   * 
   * <P>Insensitive to case.
   */
   @ApiMethod
   public static String forScriptTagsOnly(String aText)
   {
      String result = null;
      Matcher matcher = SCRIPT.matcher(aText);
      result = matcher.replaceAll("&lt;SCRIPT>");
      matcher = SCRIPT_END.matcher(result);
      result = matcher.replaceAll("&lt;/SCRIPT>");
      return result;
   }

   @ApiMethod
   public static String substring(String string, String regex, int group)
   {
      Pattern p = Pattern.compile(regex);
      Matcher m = p.matcher(string);
      if (m.find())
         return m.group(group);

      return null;
   }

   private static final Pattern SCRIPT     = Pattern.compile("<SCRIPT>", Pattern.CASE_INSENSITIVE);
   private static final Pattern SCRIPT_END = Pattern.compile("</SCRIPT>", Pattern.CASE_INSENSITIVE);

   /**
    * Parses the specified command line into an array of individual arguments.
    * Arguments containing spaces should be enclosed in quotes.
    * Quotes that should be in the argument string should be escaped with a
    * preceding backslash ('\') character.  Backslash characters that should
    * be in the argument string should also be escaped with a preceding
    * backslash character.
    * @param args the command line to parse
    * @return an argument array representing the specified command line.
    */
   @ApiMethod
   public static String[] parseArgs(String args)
   {
      List resultBuffer = new java.util.ArrayList();

      if (args != null)
      {
         args = args.trim();
         int z = args.length();
         boolean insideQuotes = false;
         StringBuffer buf = new StringBuffer();

         for (int i = 0; i < z; ++i)
         {
            char c = args.charAt(i);
            if (c == '"')
            {
               appendToBuffer(resultBuffer, buf);
               insideQuotes = !insideQuotes;
            }
            else if (c == '\\')
            {
               if ((z > i + 1) && ((args.charAt(i + 1) == '"') || (args.charAt(i + 1) == '\\')))
               {
                  buf.append(args.charAt(i + 1));
                  ++i;
               }
               else
               {
                  buf.append("\\");
               }
            }
            else
            {
               if (insideQuotes)
               {
                  buf.append(c);
               }
               else
               {
                  if (Character.isWhitespace(c))
                  {
                     appendToBuffer(resultBuffer, buf);
                  }
                  else
                  {
                     buf.append(c);
                  }
               }
            }
         }
         appendToBuffer(resultBuffer, buf);

      }

      String[] result = new String[resultBuffer.size()];
      return ((String[]) resultBuffer.toArray(result));
   }

   private static void appendToBuffer(List resultBuffer, StringBuffer buf)
   {
      if (buf.length() > 0)
      {
         resultBuffer.add(buf.toString());
         buf.setLength(0);
      }
   }
}
