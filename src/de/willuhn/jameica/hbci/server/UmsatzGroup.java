/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Hilfsklasse, um eine Gruppen von Umsaetzen samt Name als GenericObjectNode abzubilden.
 */
public class UmsatzGroup implements GenericObjectNode, Comparable
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private UmsatzTyp typ = null;
  private ArrayList<Umsatz> umsaetze = new ArrayList();
  
  /**
   * ct.
   * @param typ
   */
  public UmsatzGroup(UmsatzTyp typ)
  {
    this.typ = typ;
  }
  
  /**
   * Liefert den zugehoerigen Umsatztyp.
   * @return der zugehoerige Umsatztyp.
   */
  public UmsatzTyp getUmsatzTyp()
  {
    return this.typ;
  }
  
  /**
   * Fuegt der Gruppe einen neuen Umsatz hinzu.
   * @param umsatz
   */
  public void add(Umsatz umsatz)
  {
    this.umsaetze.add(umsatz);
  }

  /**
   * @see de.willuhn.datasource.GenericObjectNode#getChildren()
   */
  public GenericIterator getChildren() throws RemoteException
  {
    return PseudoIterator.fromArray(umsaetze.toArray(new GenericObject[umsaetze.size()]));
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
  public boolean hasChild(GenericObjectNode node) throws RemoteException
  {
    for (Umsatz u : this.umsaetze) {
      if (u.equals(node))
        return true;
    }
    return false;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject other) throws RemoteException
  {
    if (other == null || !(other instanceof UmsatzGroup))
      return false;
    return this.getID().equals(other.getID());
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
    if (this.typ == null && "name".equalsIgnoreCase(arg0))
      return  i18n.tr("Nicht zugeordnet");
   
    if ("betrag".equalsIgnoreCase(arg0))
    {
      // Rechnen wir manuell zusammen, damit der vom User eingegebene Datumsbereich
      // uebereinstimmt. Wir koennten zwar auch typ.getUmsatz(Date,Date) aufrufen,
      // allerdings wuerde das intern nochmal alle Umsaetze laden und haufen
      // zusaetzliche SQL-Queries ausloesen
      double betrag = 0.0d;
      for (Umsatz u : this.umsaetze)
      {
        betrag+= u.getBetrag();
      }
      return new Double(betrag);
    }
    
    return this.typ == null ? null : this.typ.getAttribute(arg0);
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  public String[] getAttributeNames() throws RemoteException
  {
    return this.typ == null ? new String[]{"name"} : this.typ.getAttributeNames();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  public String getID() throws RemoteException
  {
    return this.typ == null ? "<unassigned>" : this.typ.getID();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return this.typ == null ? "name" : this.typ.getPrimaryAttribute();
  }

  /**
   * Implementiert, damit wir nach dem Feld "nummer" sortieren koennen.
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    if (o == null || !(o instanceof UmsatzGroup))
      return -1;
    
    if (this.typ == null)
      return -1; // Wir sind "Nicht zugeordnet" - und die steht immer oben
    
    UmsatzGroup other = (UmsatzGroup) o;
    if (other.typ == null)
      return 1; // Die sind "Nicht zugeordnet" - wir ordnen uns unter
    
    try
    {
      // BUGZILLA 512
      // Gruppiert nach Einnahmen und Ausgaben
      
      // Erst Ausgaben, dann Einnahmen, dann Rest
      int thisType  = this.typ.getTyp();
      int otherType = other.typ.getTyp();
      if (thisType != otherType)
        return thisType < otherType ? -1 : 1;
      
      String n1  = this.typ.getNummer();  if (n1  == null) n1  = "";
      String n2  = other.typ.getNummer(); if (n2  == null) n2  = "";
      String na1 = this.typ.getName();    if (na1 == null) na1 = "";
      String na2 = other.typ.getName();   if (na2 == null) na2 = "";

      // erst nach Numer
      int numberCompare = n1.compareTo(n2);
      if (numberCompare != 0)
        return numberCompare;
      
      // Falls Nummer identisch/leer, dann nach Name
      return na1.compareTo(na2);
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine umsatztyp number",re);
    }
    return 0;
    
  }
}


/*********************************************************************
 * $Log: UmsatzGroup.java,v $
 * Revision 1.6  2008/08/29 16:46:24  willuhn
 * @N BUGZILLA 616
 *
 * Revision 1.5  2008/04/27 22:22:56  willuhn
 * @C I18N-Referenzen statisch
 *
 * Revision 1.4  2007/12/06 09:29:45  willuhn
 * @D javadoc
 *
 * Revision 1.3  2007/12/05 00:09:28  willuhn
 * @N Bug 512 - Sortierung der Kategorien auch nach Name und Typ (Einnahmen vor Ausgaben)
 *
 * Revision 1.2  2007/12/04 23:59:00  willuhn
 * @N Bug 512
 *
 * Revision 1.1  2007/04/18 08:54:21  willuhn
 * @N UmsatzGroup to fetch items from UmsatzTypTree
 *
 **********************************************************************/