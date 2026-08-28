/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.serialize.AbstractXmlIO;
import de.willuhn.datasource.serialize.ObjectFactory;
import de.willuhn.datasource.serialize.Reader;
import de.willuhn.logging.Logger;

/**
 * Reader for the legacy datasource XML format with external XML features disabled.
 */
public final class SafeXmlReader extends AbstractXmlIO implements Reader
{
  private final InputStream input;
  private final ObjectFactory factory;
  private final Document document;
  private int position = 0;

  /**
   * @param input XML input.
   * @param factory destination object factory.
   * @throws Exception if the XML cannot be parsed securely.
   */
  public SafeXmlReader(InputStream input, ObjectFactory factory) throws Exception
  {
    this.input = input;
    this.factory = factory;

    DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();
    builder.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
    builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
    builder.setFeature("http://xml.org/sax/features/external-general-entities",false);
    builder.setFeature("http://xml.org/sax/features/external-parameter-entities",false);
    builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
    builder.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD,"");
    builder.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA,"");
    builder.setExpandEntityReferences(false);
    builder.setXIncludeAware(false);
    DocumentBuilder parser = builder.newDocumentBuilder();
    parser.setEntityResolver((publicId,systemId) -> { throw new SAXException("external entities are disabled"); });
    this.document = parser.parse(input);
  }

  @Override
  public GenericObject read() throws IOException
  {
    NodeList objects = this.document.getElementsByTagName("object");
    if (objects == null || objects.getLength() == 0)
      return null;

    Node object = objects.item(this.position++);
    if (object == null)
      return null;

    NamedNodeMap attributes = object.getAttributes();
    String type = attributes.getNamedItem("type").getNodeValue();
    Node idAttribute = attributes.getNamedItem("id");
    String id = idAttribute == null ? null : idAttribute.getNodeValue();
    Map values = new HashMap();
    NodeList children = object.getChildNodes();
    for (int i=0;i<children.getLength();++i)
    {
      Node child = children.item(i);
      if (child.getNodeType() != Node.ELEMENT_NODE)
        continue;

      String name = child.getNodeName();
      Node valueTypeAttribute = child.getAttributes().getNamedItem("type");
      String valueType = valueTypeAttribute == null ? null : valueTypeAttribute.getNodeValue();
      String text = null;
      try
      {
        text = child.getLastChild().getNodeValue();
      }
      catch (NullPointerException e)
      {
        // Empty values are represented as null by the original reader.
      }

      Value value = (Value) valueMap.get(valueType);
      if (value == null)
        value = (Value) valueMap.get(null);
      values.put(name,value.unserialize(text));
    }

    try
    {
      return this.factory.create(type,id,values);
    }
    catch (IOException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      Logger.error("unable to create object " + type + ":" + id,e);
      throw new IOException("unable to create object " + type + ":" + id);
    }
  }

  @Override
  public void close() throws IOException
  {
    this.input.close();
  }
}
