/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.csv;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.willuhn.jameica.hbci.io.ser.BooleanSerializer;
import de.willuhn.jameica.hbci.io.ser.DateSerializer;
import de.willuhn.jameica.hbci.io.ser.DefaultSerializer;
import de.willuhn.jameica.hbci.io.ser.ExtendedUsageSerializer;
import de.willuhn.jameica.hbci.io.ser.Serializer;
import de.willuhn.jameica.hbci.io.ser.UmsatzTypSerializer;
import de.willuhn.jameica.hbci.io.ser.ValueSerializer;

/**
 * Data-only reader for CSV profiles written by {@link java.beans.XMLEncoder}.
 */
final class ProfileXmlDecoder
{
  private static final int MAX_PROFILES = 1000;
  private static final int MAX_COLUMNS = 1000;

  private ProfileXmlDecoder()
  {
  }

  /**
   * Reads profiles without executing constructors or methods selected by XML input.
   * @param input encoded profiles.
   * @return decoded profiles.
   * @throws Exception if the document is malformed or contains unsupported data.
   */
  static List<Profile> decode(InputStream input) throws Exception
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities",false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities",false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD,"");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA,"");
    factory.setExpandEntityReferences(false);
    factory.setXIncludeAware(false);

    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver((publicId,systemId) -> { throw new SAXException("external entities are disabled"); });
    Element root = builder.parse(input).getDocumentElement();
    requireName(root,"java");
    requireAttributes(root,"version","class");
    requireAttribute(root,"class","java.beans.XMLDecoder");

    List<Element> encoded = children(root);
    if (encoded.size() > MAX_PROFILES)
      throw invalid("too many profiles");

    List<Profile> profiles = new ArrayList<Profile>();
    Map<String,Serializer> serializers = new HashMap<String,Serializer>();
    for (Element element:encoded)
      profiles.add(parseProfile(element,serializers));
    return profiles;
  }

  private static Profile parseProfile(Element object, Map<String,Serializer> serializers) throws Exception
  {
    requireName(object,"object");
    requireAttributes(object,"class","id");
    requireAttribute(object,"class",Profile.class.getName());

    Profile profile = new Profile();
    Set<String> properties = new HashSet<String>();
    for (Element property:children(object))
    {
      requireName(property,"void");
      requireAttributes(property,"property");
      String name = requiredAttribute(property,"property");
      if (!properties.add(name))
        throw invalid("duplicate profile property: " + name);

      if ("columns".equals(name))
        profile.setColumns(parseColumns(property,serializers));
      else
      {
        Element value = onlyChild(property);
        if ("fileEncoding".equals(name))
          profile.setFileEncoding(parseString(value));
        else if ("invert".equals(name))
          profile.setInvert(parseBoolean(value));
        else if ("name".equals(name))
          profile.setName(parseString(value));
        else if ("quotingChar".equals(name))
          profile.setQuotingChar(parseString(value));
        else if ("separatorChar".equals(name))
          profile.setSeparatorChar(parseString(value));
        else if ("skipLines".equals(name))
          profile.setSkipLines(parseInt(value));
        else
          throw invalid("unsupported profile property: " + name);
      }
    }
    return profile;
  }

  private static List<Column> parseColumns(Element property, Map<String,Serializer> serializers) throws Exception
  {
    List<Element> encoded = children(property);
    if (encoded.size() > MAX_COLUMNS)
      throw invalid("too many columns");

    List<Column> columns = new ArrayList<Column>();
    for (Element add:encoded)
    {
      requireName(add,"void");
      requireAttributes(add,"method");
      requireAttribute(add,"method","add");
      columns.add(parseColumn(onlyChild(add),serializers));
    }
    return columns;
  }

  private static Column parseColumn(Element object, Map<String,Serializer> serializers) throws Exception
  {
    requireName(object,"object");
    requireAttributes(object,"class","id");
    requireAttribute(object,"class",Column.class.getName());

    Column column = new Column();
    Set<String> properties = new HashSet<String>();
    for (Element property:children(object))
    {
      requireName(property,"void");
      requireAttributes(property,"property");
      String name = requiredAttribute(property,"property");
      if (!properties.add(name))
        throw invalid("duplicate column property: " + name);
      Element value = onlyChild(property);
      if ("column".equals(name))
        column.setColumn(parseInt(value));
      else if ("name".equals(name))
        column.setName(parseString(value));
      else if ("property".equals(name))
        column.setProperty(parseString(value));
      else if ("serializer".equals(name))
        column.setSerializer(parseSerializer(value,serializers));
      else
        throw invalid("unsupported column property: " + name);
    }
    return column;
  }

  private static Serializer parseSerializer(Element object, Map<String,Serializer> serializers) throws Exception
  {
    requireName(object,"object");
    if (object.hasAttribute("idref"))
    {
      requireAttributes(object,"idref");
      requireNoChildren(object);
      Serializer serializer = serializers.get(requiredAttribute(object,"idref"));
      if (serializer == null)
        throw invalid("unknown serializer reference");
      return serializer;
    }

    requireAttributes(object,"class","id");
    requireNoChildren(object);
    String type = requiredAttribute(object,"class");
    Serializer serializer;
    if (BooleanSerializer.class.getName().equals(type))
      serializer = new BooleanSerializer();
    else if (DateSerializer.class.getName().equals(type))
      serializer = new DateSerializer();
    else if (DefaultSerializer.class.getName().equals(type))
      serializer = new DefaultSerializer();
    else if (ExtendedUsageSerializer.class.getName().equals(type))
      serializer = new ExtendedUsageSerializer();
    else if (UmsatzTypSerializer.class.getName().equals(type))
      serializer = new UmsatzTypSerializer();
    else if (ValueSerializer.class.getName().equals(type))
      serializer = new ValueSerializer();
    else
      throw invalid("unsupported serializer: " + type);

    if (object.hasAttribute("id") && serializers.put(requiredAttribute(object,"id"),serializer) != null)
      throw invalid("duplicate serializer id");
    return serializer;
  }

  private static String parseString(Element value) throws IOException
  {
    requireName(value,"string");
    return parseText(value);
  }

  private static boolean parseBoolean(Element value) throws IOException
  {
    requireName(value,"boolean");
    String text = parseText(value);
    if (!"true".equals(text) && !"false".equals(text))
      throw invalid("invalid boolean");
    return Boolean.parseBoolean(text);
  }

  private static int parseInt(Element value) throws IOException
  {
    requireName(value,"int");
    try
    {
      return Integer.parseInt(parseText(value));
    }
    catch (NumberFormatException e)
    {
      throw invalid("invalid integer");
    }
  }

  private static String parseText(Element value) throws IOException
  {
    requireAttributes(value);
    requireNoChildren(value);
    return value.getTextContent();
  }

  private static Element onlyChild(Element parent) throws IOException
  {
    List<Element> children = children(parent);
    if (children.size() != 1)
      throw invalid("expected one child in " + parent.getTagName());
    return children.get(0);
  }

  private static void requireNoChildren(Element parent) throws IOException
  {
    if (!children(parent).isEmpty())
      throw invalid("unexpected child in " + parent.getTagName());
  }

  private static List<Element> children(Element parent) throws IOException
  {
    List<Element> result = new ArrayList<Element>();
    NodeList nodes = parent.getChildNodes();
    for (int i=0;i<nodes.getLength();++i)
    {
      Node node = nodes.item(i);
      if (node instanceof Element)
        result.add((Element)node);
      else if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE || node.getNodeType() == Node.COMMENT_NODE)
        continue;
      else
        throw invalid("unexpected XML content");
    }
    return result;
  }

  private static void requireName(Element element, String name) throws IOException
  {
    if (!name.equals(element.getTagName()))
      throw invalid("expected " + name + ", got " + element.getTagName());
  }

  private static void requireAttributes(Element element, String... allowed) throws IOException
  {
    Set<String> names = new HashSet<String>();
    for (String name:allowed)
      names.add(name);
    NamedNodeMap attributes = element.getAttributes();
    for (int i=0;i<attributes.getLength();++i)
    {
      String name = attributes.item(i).getNodeName();
      if (!names.contains(name))
        throw invalid("unsupported attribute: " + name);
    }
  }

  private static String requiredAttribute(Element element, String name) throws IOException
  {
    if (!element.hasAttribute(name))
      throw invalid("missing attribute: " + name);
    return element.getAttribute(name);
  }

  private static void requireAttribute(Element element, String name, String value) throws IOException
  {
    if (!value.equals(requiredAttribute(element,name)))
      throw invalid("invalid " + name + " attribute");
  }

  private static IOException invalid(String message)
  {
    return new IOException("invalid CSV profile XML: " + message);
  }
}
