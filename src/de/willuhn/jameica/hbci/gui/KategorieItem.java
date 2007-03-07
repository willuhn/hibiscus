/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/Attic/KategorieItem.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/03/07 10:29:41 $
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
  private KategorieItem parent;
  private String text;
  private Double betrag;
  private ArrayList childs;

  public KategorieItem(KategorieItem parent, String text, Double betrag)
  {
    this.parent = parent;
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
    return this.parent;
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
    if (object == null)
      return false;

    for (int i=0;i<this.childs.size();++i)
    {
      GenericObject child = (GenericObject) this.childs.get(i);
      if (child.equals(object))
        return true;
    }
    return false;
  }

  public boolean equals(GenericObject other) throws RemoteException
  {
    if (other == null)
      return false;
    return this.getID().equals(other.getID());
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
    if (this.parent != null)
      return this.parent.getID() + "." + this.text;
    return this.text;
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
    try
    {
      return this.getID();
    }
    catch (RemoteException re)
    {
      re.printStackTrace();
    }
    return null;
  }

}
/*******************************************************************************
 * $Log: KategorieItem.java,v $
 * Revision 1.2  2007/03/07 10:29:41  willuhn
 * @B rmi compile fix
 * @B swt refresh behaviour
 *
 * Revision 1.1  2007/03/06 20:05:34  jost
 * Neu: Umsatz-Kategorien-Übersicht
 *
 ******************************************************************************/
