/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/EinnahmeAusgabe.java,v $
 * $Revision: 1.4 $
 * $Date: 2010/04/06 22:49:54 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.GenericObject;

/**
 * Container fuer die EinnahmeAusgabe-Daten.
 */

public class EinnahmeAusgabe implements GenericObject
{
  String text;
  double anfangssaldo;
  double einnahme;
  double ausgabe;
  double endsaldo;
  double differenz;
  Date startdatum;
  Date enddatum;
  boolean hasDiff = false;

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
  public EinnahmeAusgabe(String text, Date startdatum, double anfangssaldo, double einnahme, double ausgabe, Date enddatum, double endsaldo)
  {
    this.text         = text;
    this.startdatum   = startdatum;
    this.anfangssaldo = anfangssaldo;
    this.einnahme     = einnahme;
    this.ausgabe      = ausgabe;
    this.endsaldo     = endsaldo;
    this.enddatum     = enddatum;

    BigDecimal v1 = new BigDecimal(anfangssaldo + einnahme + ausgabe);
    BigDecimal v2 = new BigDecimal(endsaldo);
    this.differenz = v1.subtract(v2).setScale(2,BigDecimal.ROUND_HALF_EVEN).doubleValue();
    this.hasDiff = Math.abs(this.differenz) >= 0.01;
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
    if ("text".equals(name))          return text;
    if ("hasdiff".equals(name))       return Boolean.valueOf(this.hasDiff);
    if ("anfangssaldo".equals(name))  return new Double(anfangssaldo);
    if ("einnahme".equals(name))      return new Double(einnahme);
    if ("ausgabe".equals(name))       return new Double(ausgabe);
    if ("endsaldo".equals(name))      return new Double(endsaldo);
    if ("differenz".equals(name))     return new Double(differenz);

    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[] { "text", "anfangssaldo", "einnahme", "ausgabe", "endsaldo","differenz"};
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  public String getID() throws RemoteException
  {
    return (text + anfangssaldo + einnahme + ausgabe + endsaldo);
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
 * Revision 1.4  2010/04/06 22:49:54  willuhn
 * @B BUGZILLA 844
 *
 * Revision 1.3  2010/02/17 10:43:41  willuhn
 * @N Differenz in Einnahmen/Ausgaben anzeigen, Cleanup
 *
 * Revision 1.2  2007/06/04 17:37:00  willuhn
 * @D javadoc
 * @C java 1.4 compatibility
 * @N table colorized
 *
 * Revision 1.1  2007/06/04 15:58:31  jost
 * Neue Auswertung: Einnahmen/Ausgaben
 *
 ******************************************************************************/
