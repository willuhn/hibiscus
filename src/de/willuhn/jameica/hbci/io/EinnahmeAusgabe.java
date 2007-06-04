/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/EinnahmeAusgabe.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/06/04 17:37:00 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.HBCI;

/**
 * Container fuer die EinnahmeAusgabe-Daten.
 */

public class EinnahmeAusgabe implements Serializable, GenericObject
{
  private static final long serialVersionUID = 6176029502462174158L;

  String text;

  Date startdatum;

  Date enddatum;

  double anfangssaldo;

  double einnahme;

  double ausgabe;

  double endsaldo;

  String bemerkung = "";

  /**
   * ct.
   * @param text
   * @param startdatum
   * @param anfangssaldo
   * @param einnahme
   * @param ausgabe
   * @param enddatum
   * @param endsaldo
   */
  public EinnahmeAusgabe(String text, Date startdatum, double anfangssaldo,
      double einnahme, double ausgabe, Date enddatum, double endsaldo)
  {
    this.text = text;
    this.startdatum = startdatum;
    this.anfangssaldo = anfangssaldo;
    this.einnahme = einnahme;
    this.ausgabe = ausgabe;
    this.enddatum = enddatum;
    this.endsaldo = endsaldo;
    if (!HBCI.DECIMALFORMAT.format(anfangssaldo + einnahme + ausgabe).equals(
        HBCI.DECIMALFORMAT.format(endsaldo)))
    {
      this.bemerkung = "Achtung Differenz!";
    }
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject other) throws RemoteException
  {
    return other.getAttribute("text").equals(text);
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String name) throws RemoteException
  {
    if (name.equals("text"))
    {
      return text;
    }
    if (name.equals("anfangssaldo"))
    {
      return new Double(anfangssaldo);
    }
    if (name.equals("einnahme"))
    {
      return new Double(einnahme);
    }
    if (name.equals("ausgabe"))
    {
      return new Double(ausgabe);
    }
    if (name.equals("endsaldo"))
    {
      return new Double(endsaldo);
    }
    if (name.equals("bemerkung"))
    {
      return bemerkung;
    }
    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] { "text", "anfangssaldo", "einnahme", "ausgabe",
        "endsaldo", "bemerkung" };
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  public String getID() throws RemoteException
  {
    return (text + anfangssaldo + einnahme + ausgabe + endsaldo + bemerkung);
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "text";
  }
}

/*******************************************************************************
 * $Log: EinnahmeAusgabe.java,v $
 * Revision 1.2  2007/06/04 17:37:00  willuhn
 * @D javadoc
 * @C java 1.4 compatibility
 * @N table colorized
 *
 * Revision 1.1  2007/06/04 15:58:31  jost
 * Neue Auswertung: Einnahmen/Ausgaben
 *
 ******************************************************************************/
