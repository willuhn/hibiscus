/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/Attic/KategorieItem.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/03/06 20:05:34 $
 * $Author: jost $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Item;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.hbci.HBCI;

public class KategorieItem implements Item
{
  private String text;

  private double betrag;

  private ArrayList childs;

  public KategorieItem(String text, double betrag)
  {
    this.text = text;
    this.betrag = betrag;
    childs = new ArrayList();
  }

  public GenericIterator getChildren() throws RemoteException
  {
    return PseudoIterator.fromArray((GenericObjectNode[]) childs
        .toArray(new KategorieItem[childs.size()]));
  }

  public GenericObjectNode getParent() throws RemoteException
  {
    return null;
  }

  public GenericIterator getPath() throws RemoteException
  {
    return null;
  }

  public GenericIterator getPossibleParents() throws RemoteException
  {
    return null;
  }

  public boolean hasChild(GenericObjectNode object) throws RemoteException
  {
    return childs.size() > 0;
  }

  public boolean equals(GenericObject other) throws RemoteException
  {
    return false;
  }

  public Object getAttribute(String name) throws RemoteException
  {
    if (name.equals("text"))
    {
      return text + "  |  "
          + new CurrencyFormatter("", HBCI.DECIMALFORMAT).format(betrag);
    }
    if (name.equals("betrag"))
    {
      return betrag;
    }
    return null;
  }

  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] { "text", "betrag" };
  }

  public String getID() throws RemoteException
  {
    return "1";
  }

  public String getPrimaryAttribute() throws RemoteException
  {
    return "text";
  }

  public void addChild(Item child) throws RemoteException
  {
    childs.add(child);
  }

  public Action getAction() throws RemoteException
  {
    return null;
  }

  public String getName() throws RemoteException
  {
    return null;
  }

  public boolean isEnabled() throws RemoteException
  {
    return true;
  }

  public void setEnabled(boolean enabled, boolean recursive)
      throws RemoteException
  {

  }

  public String getExtendableID()
  {
    return null;
  }

}
/*******************************************************************************
 * $Log: KategorieItem.java,v $
 * Revision 1.1  2007/03/06 20:05:34  jost
 * Neu: Umsatz-Kategorien-Übersicht
 *
 ******************************************************************************/
