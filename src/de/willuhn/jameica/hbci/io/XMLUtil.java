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

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Hilfsklasse für XML.
 */
public class XMLUtil
{
  /**
   * Erzeugt eine DocumentBuilderFactory.
   * @return die DocumentBuilderFactory.
   */
  public static DocumentBuilderFactory createDocumentBuilderFactory()
  {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    
    setFeature(factory,XMLConstants.FEATURE_SECURE_PROCESSING,true);
    setFeature(factory,"http://apache.org/xml/features/disallow-doctype-decl",true);
    setFeature(factory,"http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
    setFeature(factory,"http://xml.org/sax/features/external-general-entities",false);
    setFeature(factory,"http://xml.org/sax/features/external-parameter-entities",false);
    setAttribute(factory,XMLConstants.ACCESS_EXTERNAL_DTD,"");
    setAttribute(factory,XMLConstants.ACCESS_EXTERNAL_SCHEMA,"");
    
    try
    {
      factory.setExpandEntityReferences(false);
      factory.setXIncludeAware(false);
    }
    catch (Exception ex)
    {
      Logger.write(Level.WARN,"xml feature not supported: " + ex.getMessage(),ex);
    }
    
    return factory;
  }
  
  /**
   * Versucht, ein Feature in der XML Document-Builder Factory zu setzen, toleriert aber, wenn das fehlschlägt.
   * @param factory die Factory.
   * @param name der Name des Feature.
   * @param enabled der Wert.
   */
  private static void setFeature(DocumentBuilderFactory factory, String name, boolean enabled)
  {
    try
    {
      factory.setFeature(name,enabled);
    }
    catch (Exception ex)
    {
      Logger.write(Level.WARN,"xml feature not supported: " + ex.getMessage(),ex);
    }
  }
  
  /**
   * Versucht, ein Attribut in der XML Document-Builder Factory zu setzen, toleriert aber, wenn das fehlschlägt.
   * @param factory die Factory.
   * @param name der Name des Attributs.
   * @param value der Wert.
   */
  private static void setAttribute(DocumentBuilderFactory factory, String name, String value)
  {
    try
    {
      factory.setAttribute(name,value);
    }
    catch (Exception ex)
    {
      Logger.write(Level.WARN,"xml attribute not supported: " + ex.getMessage(),ex);
    }
  }
}


