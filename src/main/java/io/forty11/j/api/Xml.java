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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Wells Burke
 *
 */
public class Xml
{
   @ApiMethod
   public static Document loadXml(String textOrUrl)
   {
      if (textOrUrl.startsWith("<"))
      {
         ByteArrayInputStream bais = new ByteArrayInputStream(textOrUrl.getBytes());
         return loadXml(bais);
      }
      else
      {
         InputStream stream = Streams.findInputStream(textOrUrl);
         if (stream == null)
            throw new RuntimeException("Unabel to find input stream: " + textOrUrl);
         else
            return loadXml(stream);
      }
   }

   @ApiMethod
   public static Document loadXml(InputStream stream)
   {
      String docText = null;
      try
      {
         int buffSize = 2048;
         BufferedInputStream buff = new BufferedInputStream(stream, buffSize);
         byte[] debug = new byte[buffSize];
         buff.mark(buffSize);

         buff.read(debug);
         docText = new String(debug);
         docText = docText.trim();
         buff.reset();
         stream = buff;

         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         Document doc = db.parse(stream);
         doc.getDocumentElement().normalize();

         return doc;
      }
      catch (Exception ex)
      {
         String msg = "Unable to parse document: " + ex.getMessage();
         if (docText != null)
            msg += "\r\n" + docText;

         throw new RuntimeException(msg);
      }
   }



   @ApiMethod
   public static List<Element> childElements(Node node)
   {
      List<Element> elements = new ArrayList();
      NodeList list = node.getChildNodes();
      for (int i = 0; i < list.getLength(); i++)
      {
         if (list.item(i) instanceof Element)
         {
            elements.add((Element) list.item(i));
         }
      }
      return elements;
   }

   @ApiMethod
   public static String nodePath(Node node)
   {
      String path = "";
      while (node != null && !(node instanceof Document))
      {
         path = "/" + node.getNodeName() + path;
         node = node.getParentNode();
      }
      return path;
   }
}
