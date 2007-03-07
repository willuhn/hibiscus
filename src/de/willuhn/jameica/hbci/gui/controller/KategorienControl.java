/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/KategorienControl.java,v $
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
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.KategorieItem;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
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

  public TreePart getTree() throws RemoteException
  {
    settings.setAttribute("von", sdf.format((Date) getStart().getValue()));
    settings.setAttribute("bis", sdf.format((Date) getEnd().getValue()));
    return new TreePart(getTreeData(), null);
  }

  public KategorieItem getTreeData() throws RemoteException
  {
    KategorieItem root = new KategorieItem(null,"Kategorien", new Double(0));
    DBIterator listKat = Settings.getDBService().createList(UmsatzTyp.class);
    while (listKat.hasNext())
    {
      UmsatzTyp typ = (UmsatzTyp) listKat.next();
      KategorieItem kat = new KategorieItem(root, typ.getName(), new Double(typ.getUmsatz(
          (Date) start.getValue(), (Date) end.getValue())));
      GenericIterator umsaetze = typ.getUmsaetze((Date) start.getValue(),
          (Date) end.getValue());
      while (umsaetze.hasNext())
      {
        Umsatz ums = (Umsatz) umsaetze.next();
        KategorieItem umsatz = new KategorieItem(kat,ums.getEmpfaengerName() + ", "
            + ums.getZweck() + " " + ums.getZweck2(), new Double(ums.getBetrag()));
        kat.addChild(umsatz);
      }
      root.addChild(kat);
    }
    return root;
  }
}

/*******************************************************************************
 * $Log: KategorienControl.java,v $
 * Revision 1.2  2007/03/07 10:29:41  willuhn
 * @B rmi compile fix
 * @B swt refresh behaviour
 *
 * Revision 1.1  2007/03/06 20:06:08  jost
 * Neu: Umsatz-Kategorien-Übersicht
 *
 ******************************************************************************/
