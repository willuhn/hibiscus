/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/Overview.java,v $
 * $Revision: 1.18 $
 * $Date: 2011/09/08 08:02:37 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Box zur Anzeige des Vermoegens-Ueberblicks.
 */
public class Overview extends AbstractBox implements Box
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  // BUGZILLA 993
  private static Konto konto       = null;
  private static Date startDate    = null;
  private static Date endDate      = null;

  private KontoInput kontoAuswahl  = null;
  private Input saldo              = null;
  private Input ausgaben           = null;
  private Input einnahmen          = null;
  private Input bilanz             = null;
  
  private DateInput start          = null;
  private DateInput end            = null;
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Finanz-Übersicht");
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    Container group = new SimpleContainer(parent);
    group.addLabelPair(i18n.tr("Konto") + ":", getKontoAuswahl());
    group.addLabelPair(i18n.tr("Beginn des Zeitraumes") + ":", getStart());
    group.addLabelPair(i18n.tr("Ende des Zeitraumes") + ":", getEnd());
    group.addLabelPair(i18n.tr("Saldo") + ":", getSaldo());
    group.addLabelPair(i18n.tr("Einnahmen im Zeitraum") + ":", getEinnahmen());
    group.addLabelPair(i18n.tr("Ausgaben im Zeitraum") + ":", getAusgaben());
    group.addSeparator();
    group.addLabelPair(i18n.tr("Bilanz") + ":", getBilanz());
    refresh();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 0;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return true;
  }
  
  /**
   * Liefert eine Auswahlbox fuer das Konto.
   * @return Auswahlbox.
   * @throws RemoteException
   */
  private Input getKontoAuswahl() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;
    
    this.kontoAuswahl = new KontoInput(konto != null && konto.getID() != null ? konto : null,KontoFilter.ACTIVE);
    this.kontoAuswahl.setPleaseChoose(i18n.tr("Alle Konten"));
    this.kontoAuswahl.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        refresh();
      }
    });
    return this.kontoAuswahl;
  }

  /**
   * Liefert ein Anzeige-Feld mit dem Saldo ueber alle Konten.
   * @return Saldo ueber alle Konten.
   */
  private Input getSaldo()
  {
    if (this.saldo != null)
      return this.saldo;
    this.saldo = new LabelInput("");
    this.saldo.setComment("");
    return this.saldo;
  }

  /**
   * Liefert ein Auswahl-Feld fuer das Start-Datum.
   * @return Auswahl-Feld.
   */
  private Input getStart()
  {
    if (this.start != null)
      return this.start;
    
    if (startDate == null)
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      cal.set(Calendar.DAY_OF_MONTH,1);
      startDate = cal.getTime();
    }
    
    this.start = new DateInput(DateUtil.startOfDay(startDate),HBCI.DATEFORMAT);
    this.start.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        refresh();
      }
    });
    return this.start;
  }
  
  /**
   * Liefert ein Auswahl-Feld fuer das End-Datum.
   * @return Auswahl-Feld.
   */
  private Input getEnd()
  {
    if (this.end != null)
      return this.end;

    if (endDate == null)
    {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      cal.set(Calendar.DAY_OF_MONTH,cal.getActualMaximum(Calendar.DAY_OF_MONTH));
      endDate = cal.getTime();
    }

    this.end = new DateInput(DateUtil.endOfDay(endDate),HBCI.DATEFORMAT);
    this.end.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        refresh();
      }
    });
    return this.end;
  }

  /**
   * Aktualisiert die Salden.
   */
  private synchronized void refresh()
  {
    try
    {
      konto     = (Konto) this.getKontoAuswahl().getValue();
      startDate = (Date) getStart().getValue();
      endDate   = (Date) getEnd().getValue();
      Date saldoDate = endDate;

      ////////////////////////////////////////////////////////////////////////////
      // Saldo ausrechnen
      double d = 0d;
      if (konto == null)
      {
        DBIterator konten = Settings.getDBService().createList(Konto.class);
        while (konten.hasNext())
        {
          Konto k = (Konto) konten.next();
          d += k.getSaldo();
        }
      }
      else
      {
        d = konto.getSaldo();
        saldoDate = konto.getSaldoDatum();
      }
      
      LabelInput saldo = (LabelInput) this.getSaldo();
      saldo.setValue(HBCI.DECIMALFORMAT.format(d));
      saldo.setComment(HBCIProperties.CURRENCY_DEFAULT_DE + " [" + HBCI.DATEFORMAT.format(saldoDate != null ? saldoDate : new Date()) + "]");
      if (d < 0)       saldo.setColor(Color.ERROR);
      else if (d == 0) saldo.setColor(Color.WIDGET_FG);
      else             saldo.setColor(Color.SUCCESS);
      ////////////////////////////////////////////////////////////////////////////

      
      if (startDate == null || endDate == null || startDate.after(endDate))
        return;

      double in = 0d;
      double out = 0d;
      if (konto == null)
      {
        DBIterator i = Settings.getDBService().createList(Konto.class);
        while (i.hasNext())
        {
          Konto k = (Konto) i.next();
          in  += KontoUtil.getEinnahmen(k,startDate,endDate);
          out += KontoUtil.getAusgaben(k,startDate,endDate);
        }
      }
      else
      {
        in  = KontoUtil.getEinnahmen(konto,startDate,endDate);
        out = KontoUtil.getAusgaben(konto,startDate,endDate);
      }
      out = Math.abs(out); // BUGZILLA 405
      getAusgaben().setValue(HBCI.DECIMALFORMAT.format(out));
      getEinnahmen().setValue(HBCI.DECIMALFORMAT.format(in));

      double diff = in - out;
      getBilanz().setValue(HBCI.DECIMALFORMAT.format(diff));
      if (diff < 0)
        ((LabelInput)getBilanz()).setColor(Color.ERROR);
      else if (diff == 0)
        ((LabelInput)getBilanz()).setColor(Color.WIDGET_FG);
      else
        ((LabelInput)getBilanz()).setColor(Color.SUCCESS);
    }
    catch (RemoteException e)
    {
      Logger.error("unable to calculate sum",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Berechnen der Bilanz"),StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Liefert ein Anzeige-Feld fuer die Bilanz.
   * @return Anzeige-Feld.
   */
  private Input getBilanz()
  {
    if (this.bilanz != null)
      return this.bilanz;
    bilanz = new LabelInput("");
    bilanz.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
    return bilanz;
  }
  
  /**
   * Liefert ein Anzeige-Feld fuer die Ausgaben.
   * @return Anzeige-Feld.
   */
  private Input getAusgaben()
  {
    if (this.ausgaben != null)
      return this.ausgaben;
    ausgaben = new LabelInput("");
    ausgaben.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
    ((LabelInput)ausgaben).setColor(Color.ERROR);
    return ausgaben;
  }

  /**
   * Liefert ein Anzeige-Feld fuer die Einnahmen.
   * @return Anzeige-Feld.
   */
  private Input getEinnahmen()
  {
    if (this.einnahmen != null)
      return this.einnahmen;
    einnahmen = new LabelInput("");
    einnahmen.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
    ((LabelInput)einnahmen).setColor(Color.SUCCESS);
    return einnahmen;
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#isActive()
   */
  public boolean isActive()
  {
    return super.isActive() && !Settings.isFirstStart();
  }

}


/*********************************************************************
 * $Log: Overview.java,v $
 * Revision 1.18  2011/09/08 08:02:37  willuhn
 * @B Saldo-Datum in Box "Finanz-Übersicht" nicht ganz korrekt
 *
 * Revision 1.17  2011-02-21 09:00:20  willuhn
 * @N BUGZILLA 993
 *
 * Revision 1.16  2011-01-20 17:13:21  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 *
 * Revision 1.15  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 * Revision 1.14  2010/06/07 22:41:13  willuhn
 * @N BUGZILLA 844/852
 *
 * Revision 1.13  2010/03/21 22:43:21  willuhn
 * @B Wenn ein Konto (als Default-Konto) vorausgewaehlt war, wurden die Werte erst nach Neuauswahl auf den korrekten Stand gebracht
 *
 * Revision 1.12  2010/03/15 00:18:07  willuhn
 * @N Siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=65782#65782 (Punkt 2)
 *
 * Revision 1.11  2008/04/23 13:20:54  willuhn
 * @B Bug 588
 *
 * Revision 1.10  2007/12/18 17:10:22  willuhn
 * @N Neues ExpandPart
 * @N Boxen auf der Startseite koennen jetzt zusammengeklappt werden
 *
 * Revision 1.9  2007/05/25 14:16:47  willuhn
 * @B Bug 405
 *
 * Revision 1.8  2007/02/21 11:58:52  willuhn
 * @N Bug 315
 *
 * Revision 1.7  2006/12/29 14:28:47  willuhn
 * @B Bug 345
 * @B jede Menge Bugfixes bei SQL-Statements mit Valuta
 *
 * Revision 1.6  2006/12/27 17:56:49  willuhn
 * @B Bug 341
 *
 * Revision 1.5  2006/06/29 23:10:33  willuhn
 * @R Box-System aus Hibiscus in Jameica-Source verschoben
 * @C keine eigene Startseite mehr, jetzt alles ueber Jameica-Boxsystem geregelt
 *
 * Revision 1.4  2006/04/03 20:37:51  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2006/03/30 20:56:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2006/03/20 16:59:01  willuhn
 * @N Overview ueber alle Konten
 *
 * Revision 1.1  2005/11/09 01:13:53  willuhn
 * @N chipcard modul fuer AMD64 vergessen
 * @N Startseite jetzt frei konfigurierbar
 *
 **********************************************************************/