/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/Attic/KategorieItem.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/03/08 18:56:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;

/**
 */
public class KategorieItem implements GenericObjectNode
{
  private UmsatzTyp typ        = null;
  private Date von             = null;
  private Date bis             = null;

  /**
   * ct.
   * @param typ der Umsatztyp.
   * @param von Start-Datum.
   * @param bis End-Datum.
   */
  public KategorieItem(UmsatzTyp typ, Date von, Date bis)
  {
    this.typ = typ;
    this.von = von;
    this.bis = bis;
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getChildren()
   */
  public GenericIterator getChildren() throws RemoteException
  {
    return typ.getUmsaetze(von,bis);
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getParent()
   */
  public GenericObjectNode getParent() throws RemoteException
  {
    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getPath()
   */
  public GenericIterator getPath() throws RemoteException
  {
    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getPossibleParents()
   */
  public GenericIterator getPossibleParents() throws RemoteException
  {
    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#hasChild(de.willuhn.datasource.GenericObjectNode)
   */
  public boolean hasChild(GenericObjectNode object) throws RemoteException
  {
    if (object == null)
      return false;

    GenericIterator children = getChildren();
    
    try
    {
      while (children.hasNext())
      {
        GenericObject child = children.next();
        if (child.equals(object))
          return true;
      }
      return false;
    }
    finally
    {
      children.begin();
    }
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject other) throws RemoteException
  {
    if (other == null)
      return false;
    return this.getID().equals(other.getID());
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String name) throws RemoteException
  {
    return this.typ.getAttribute(name);
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] { "name", "betrag" };
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  public String getID() throws RemoteException
  {
    return this.typ.getID();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "name";
  }
}
/*******************************************************************************
 * $Log: KategorieItem.java,v $
 * Revision 1.3  2007/03/08 18:56:39  willuhn
 * @N Mehrere Spalten in Kategorie-Baum
 *
 * Revision 1.2  2007/03/07 10:29:41  willuhn
 * @B rmi compile fix
 * @B swt refresh behaviour
 *
 * Revision 1.1  2007/03/06 20:05:34  jost
 * Neu: Umsatz-Kategorien-Übersicht
 *
 ******************************************************************************/
