/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/EinnahmeAusgabe.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/06/04 15:58:31 $
 * $Author: jost $
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

  public boolean equals(GenericObject other) throws RemoteException
  {
    return other.getAttribute("text").equals(text);
  }

  public Object getAttribute(String name) throws RemoteException
  {
    if (name.equals("text"))
    {
      return text;
    }
    if (name.equals("anfangssaldo"))
    {
      return anfangssaldo;
    }
    if (name.equals("einnahme"))
    {
      return einnahme;
    }
    if (name.equals("ausgabe"))
    {
      return ausgabe;
    }
    if (name.equals("endsaldo"))
    {
      return endsaldo;
    }
    if (name.equals("bemerkung"))
    {
      return bemerkung;
    }
    return null;
  }

  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] { "test", "anfangssaldo", "einnahme", "ausgabe",
        "endsaldo", "bemerkung" };
  }

  public String getID() throws RemoteException
  {
    return "1";
  }

  public String getPrimaryAttribute() throws RemoteException
  {
    return "text";
  }
}

/*******************************************************************************
 * $Log: EinnahmeAusgabe.java,v $
 * Revision 1.1  2007/06/04 15:58:31  jost
 * Neue Auswertung: Einnahmen/Ausgaben
 *
 ******************************************************************************/
