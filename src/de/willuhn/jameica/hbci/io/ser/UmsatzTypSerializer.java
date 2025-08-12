/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.ser;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.logging.Logger;

/**
 * Implementierung eines Serializers fuer Umsatz-Kategorien.
 */
public class UmsatzTypSerializer extends DefaultSerializer<UmsatzTyp>
{
  private final static String SEP = "/";
  
  private Map<String,UmsatzTyp> cache = null;

  /**
   * @see de.willuhn.jameica.hbci.io.ser.DefaultSerializer#serialize(java.lang.Object, java.lang.Object)
   */
  @Override
  public String serialize(Object context, UmsatzTyp value) throws IOException
  {
    if (value == null)
      return super.serialize(context,value);

    return value.getName();
  }

  /**
   * @see de.willuhn.jameica.hbci.io.ser.DefaultSerializer#unserialize(java.lang.Object, java.lang.String)
   */
  @Override
  public UmsatzTyp unserialize(Object context, String value) throws IOException
  {
    if (value == null || value.length() == 0)
      return null;

    final UmsatzTyp t = this.getCache().get(value.toLowerCase());
    
    // Haben wir im Cache
    if (t != null)
      return t;
    
    // Neu anlegen
    return this.create(value);
  }
  
  /**
   * Initialisiert den Cache der Kategorien.
   * @return der Cache.
   */
  private synchronized Map<String,UmsatzTyp> getCache()
  {
    if (this.cache != null)
      return this.cache;

    Logger.info("init category cache");
    this.cache = new HashMap<String,UmsatzTyp>();

    try
    {
      final DBIterator<UmsatzTyp> i = Settings.getDBService().createList(UmsatzTyp.class);
      while (i.hasNext())
      {
        this.addToCache(i.next());
      }
      Logger.info("category cache initialized");
    }
    catch (Exception e)
    {
      Logger.error("error while creating category cache",e);
    }
    
    return this.cache;
  }
  
  /**
   * Fügt die Kategorie zum Cache hinzu.
   * @param t die Kategorie.
   * @throws RemoteException
   */
  private void addToCache(UmsatzTyp t) throws RemoteException
  {
    final LinkedList<String> kategorien = new LinkedList<>();
    
    UmsatzTyp current = t;
    while (current != null)
    {
      kategorien.addFirst(current.getName());
      current = (UmsatzTyp) current.getParent();
    }

    this.cache.put(String.join(SEP, kategorien).toLowerCase(),t);
  }
  
  /**
   * Erzeugt einen UmstatzTyp mit seinen Unterkategorien
   * z.B Kategrorie1/Katergorie2/etc.
   * @param name der Name der Kategorie.
   * @return angelegten Umsatztyp
   */
  private UmsatzTyp create(String name)
  {
    UmsatzTyp typ = null;
    UmsatzTyp parent = null;
    final StringBuilder pathBuilder = new StringBuilder();
    
    try
    {
      for (String current:name.split(SEP))
      {
        if (pathBuilder.length() > 0)
          pathBuilder.append(SEP);
        
        pathBuilder.append(current);
        
        final String path = pathBuilder.toString();            
        
        // Nachschauen ob Parent schon im Cache
        final UmsatzTyp cacheParent = this.getCache().get(path.toLowerCase());
        if (cacheParent != null)
        {
          parent = cacheParent;
          continue;
        }
        
        // UmsatzTyp anlegen
        typ = (UmsatzTyp) Settings.getDBService().createObject(UmsatzTyp.class,null);
        typ.setName(current);
        typ.setTyp(UmsatzTyp.TYP_EGAL);
        typ.setParent(parent);
        typ.store();     

        Logger.info("auto-created category " + path);
        this.addToCache(typ);
        
        // Typ als Parten für nächsten Typ setzen
        parent = typ;
      }
    }
    catch (Exception e)
    {
      Logger.error("error while auto-creating category",e);
    }
    
    return typ;
  }
}
