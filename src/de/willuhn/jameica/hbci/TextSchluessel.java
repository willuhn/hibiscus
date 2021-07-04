/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Bean fuer die Textschluessel.
 */
public class TextSchluessel
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Abbuchungsverfahren
   */
  public final static String TS_ABBUCHUNG = "04";
  
  /**
   * Einzugsermaechtigung
   */
  public final static String TS_EINZUG = "05";
  
  /**
   * Ueberweisung
   */
  public final static String TS_UEB = "51";
  
  /**
   * Dauerauftrag
   */
  public final static String TS_DAUER = "52";
  
  /**
   * Lohn/Gehalt/Rente
   */
  public final static String TS_LOHN = "53";
  
  /**
   * Vermoegenswirksame Leistungen
   */
  public final static String TS_VL = "54";
  
  /**
   * Rueckueberweisung
   */
  public final static String TS_RUECKUEB = "59";
  
  /**
   * BZU-Ueberweisung (Beleglose Zahlungsueberweisung, pruefziffergesichert) 
   */
  public final static String TS_BZU = "67";
  
  /**
   * Spendenueberweisung.
   */
  public final static String TS_SPENDE = "69";
  
  /**
   * Set von zulaessigen Textschluesseln fuer Dauerauftraege.
   */
  public final static String[] SET_DAUER = new String[]{TS_DAUER,TS_LOHN,TS_VL};

  /**
   * Set von zulaessigen Textschluesseln fuer Sammel-Lastschriften.
   */
  public final static String[] SET_SAMMELLAST = new String[]{TS_EINZUG,TS_ABBUCHUNG};

  /**
   * Set von zulaessigen Textschluesseln fuer Lastschriften.
   */
  public final static String[] SET_LAST = new String[]{TS_EINZUG,TS_ABBUCHUNG};

  /**
   * Set von zulaessigen Textschluesseln fuer Sammel-Ueberweisungen.
   */
  public final static String[] SET_SAMMELUEB = new String[]{TS_UEB,TS_LOHN,TS_VL};

  /**
   * Set von zulaessigen Textschluesseln fuer Ueberweisungen.
   */
  public final static String[] SET_UEB = new String[]{TS_UEB,TS_LOHN,TS_VL,TS_RUECKUEB,TS_BZU};

  private final static List<TextSchluessel> list = new ArrayList<TextSchluessel>();
  
  static
  {
    list.add(new TextSchluessel(TS_ABBUCHUNG,i18n.tr("Abbuchungsverfahren")));
    list.add(new TextSchluessel(TS_EINZUG,   i18n.tr("Einzugsermächtigung")));
    list.add(new TextSchluessel(TS_DAUER,    i18n.tr("Dauerauftrag")));
    list.add(new TextSchluessel(TS_UEB,      i18n.tr("Überweisung")));
    list.add(new TextSchluessel(TS_LOHN,     i18n.tr("Überweisung Lohn/Gehalt/Rente")));
    list.add(new TextSchluessel(TS_VL,       i18n.tr("Vermögenswirksame Leistungen")));
    list.add(new TextSchluessel(TS_RUECKUEB, i18n.tr("Rücküberweisung")));
    list.add(new TextSchluessel(TS_BZU,      i18n.tr("BZÜ-Überweisung")));
    list.add(new TextSchluessel(TS_SPENDE,   i18n.tr("Spende")));
  }

  private String code = null;
  private String name = null;
  
  /**
   * Liefert eine Liste der Textschluessel-Objekte mit den genannten Codes.
   * @param codes Liste der Codes oder <code>null</code>, wenn alle zurueckgeliefert werden sollen.
   * @return Liste der Textschluessel mit diesen Codes.
   * Die Textschluessel werden in der gleichen Reihenfolge zurueckgeliefert, in der die Codes uebergeben wurden.
   */
  public static TextSchluessel[] get(String[] codes)
  {
    if (codes == null || codes.length == 0)
      return list.toArray(new TextSchluessel[list.size()]);

    List<TextSchluessel> l = new ArrayList<TextSchluessel>();
    for (String code : codes)
    {
      for (TextSchluessel ts : list)
      {
        if (code.equals(ts.getCode()))
        {
          l.add(ts);
        }
      }
    }
    
    return l.toArray(new TextSchluessel[l.size()]);
  }
  
  /**
   * Liefert einen einzelnen Textschluessel.
   * @param code Code des Textschluessels.
   * @return der Textschluessel oder <code>null</code>, wenn er nicht existiert.
   */
  public static TextSchluessel get(String code)
  {
    if (code == null || code.length() == 0)
      return null;

    for (TextSchluessel ts : list) {
      if (code.equals(ts.getCode()))
        return ts;
    }
    return null;
  }
  
  /**
   * ct
   * @param code Nummer des Textschlüssel.
   * @param name Bezeichnung.
   */
  private TextSchluessel(String code, String name)
  {
    this.code = code;
    this.name = name;
  }
  
  /**
   * Liefert den Textschluessel.
   * @return der Textschluessel.
   */
  public String getCode()
  {
    return this.code;
  }
  
  /**
   * Liefert den Namen des Textschluessels.
   * @return der Name des Textschluessels.
   */
  public String getName()
  {
    return this.name;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return i18n.tr("[{0}] {1}", new String[]{this.code,this.name});
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other)
  {
    if (this == other)
      return true;
    if (other == null || !(other instanceof TextSchluessel))
      return false;
    return this.code.equals(((TextSchluessel)other).getCode());
  }
}


/*********************************************************************
 * $Log: TextSchluessel.java,v $
 * Revision 1.5  2011/05/11 16:23:57  willuhn
 * @N BUGZILLA 591
 *
 * Revision 1.4  2011-05-10 11:41:30  willuhn
 * @N Text-Schluessel als Konstanten definiert - Teil aus dem Patch von Thomas vom 07.12.2010
 *
 * Revision 1.3  2010-09-24 12:22:04  willuhn
 * @N Thomas' Patch fuer Textschluessel in Dauerauftraegen
 *
 * Revision 1.2  2010/06/07 12:43:41  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.1  2008/08/01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 **********************************************************************/