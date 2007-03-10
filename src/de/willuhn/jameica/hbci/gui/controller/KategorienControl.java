/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/KategorienControl.java,v $
 * $Revision: 1.4 $
 * $Date: 2007/03/10 07:16:37 $
 * $Author: jost $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.KategorieItem;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Umsatz-Kategorien-Auswertung
 */
public class KategorienControl extends AbstractControl
{

  private SelectInput kontoAuswahl = null;

  private DateInput start = null;

  private DateInput end = null;

  private I18N i18n = null;

  private de.willuhn.jameica.system.Settings settings = null;

  private SimpleDateFormat sdf = null;

  /**
   * ct.
   * 
   * @param view
   */
  public KategorienControl(AbstractView view)
  {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources()
        .getI18N();
    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    settings.setStoreWhenRead(true);
    sdf = new SimpleDateFormat("dd.MM.yyyy");
  }

  /**
   * Liefert eine Auswahlbox fuer das Konto.
   * 
   * @return Auswahlbox.
   * @throws RemoteException
   */
  public Input getKontoAuswahl() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;

    DBIterator it = de.willuhn.jameica.hbci.Settings.getDBService().createList(
        Konto.class);
    it.setOrder("ORDER BY blz, kontonummer");
    this.kontoAuswahl = new SelectInput(it, null);
    this.kontoAuswahl.setAttribute("longname");
    this.kontoAuswahl.setPleaseChoose(i18n.tr("Alle Konten"));
    return this.kontoAuswahl;
  }

  /**
   * Liefert ein Auswahl-Feld fuer das Start-Datum.
   * 
   * @return Auswahl-Feld.
   */
  public Input getStart()
  {
    if (this.start != null)
    {
      return this.start;
    }
    Date dStart = null;
    try
    {
      dStart = sdf.parse(settings.getString("von", "01.01.2007"));
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    this.start = new DateInput(dStart, HBCI.DATEFORMAT);
    return this.start;
  }

  /**
   * Liefert ein Auswahl-Feld fuer das End-Datum.
   * 
   * @return Auswahl-Feld.
   */
  public Input getEnd()
  {
    if (this.end != null)
      return this.end;

    Date dEnd = null;
    try
    {
      dEnd = sdf.parse(settings.getString("bis", "31.12.2007"));
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
    this.end = new DateInput(dEnd, HBCI.DATEFORMAT);
    return this.end;
  }

  /**
   * Liefert einen Baum von Umsatzkategorien mit den Umsaetzen.
   * @return Baum mit Umsatz-Kategorien.
   * @throws RemoteException
   */
  public TreePart getTree() throws RemoteException
  {
    Date von = (Date) getStart().getValue();
    Date bis = (Date) getEnd().getValue();

    settings.setAttribute("von", sdf.format(von));
    settings.setAttribute("bis", sdf.format(bis));
    
    ArrayList rootItems = new ArrayList();

    DBService service = Settings.getDBService();
    DBIterator list = service.createList(UmsatzTyp.class);
    list.setOrder("order by nummer");
    while (list.hasNext())
      rootItems.add(new KategorieItem((UmsatzTyp)list.next(),von,bis));
    
    TreePart tp = new TreePart(PseudoIterator.fromArray((GenericObject[])rootItems.toArray(new GenericObject[rootItems.size()])), null);
    tp.addColumn(i18n.tr("Bezeichnung"),"name");
    tp.addColumn(i18n.tr("Betrag"),"betrag",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    return tp;
  }
}

/*******************************************************************************
 * $Log: KategorienControl.java,v $
 * Revision 1.4  2007/03/10 07:16:37  jost
 * Neu: Nummer für die Sortierung der Umsatz-Kategorien
 *
 * Revision 1.3  2007/03/08 18:56:39  willuhn
 * @N Mehrere Spalten in Kategorie-Baum
 *
 * Revision 1.2  2007/03/07 10:29:41  willuhn
 * @B rmi compile fix
 * @B swt refresh behaviour
 *
 * Revision 1.1  2007/03/06 20:06:08  jost
 * Neu: Umsatz-Kategorien-Übersicht
 *
 ******************************************************************************/
