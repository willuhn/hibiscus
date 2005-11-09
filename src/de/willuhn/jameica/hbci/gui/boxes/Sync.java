/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/Sync.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/11/09 01:13:53 $
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

import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.parts.KontoList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCISynchronizer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Box zum Synchronisieren der Konten.
 */
public class Sync extends AbstractBox implements Box
{

  private I18N i18n = null;

  private de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(HBCISynchronizer.class);
  
  private CheckboxInput syncDauer = null;
  private CheckboxInput syncUeb   = null;
  private CheckboxInput syncLast  = null;

  /**
   * ct.
   */
  public Sync()
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return i18n.tr("Konten synchronisieren");
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    LabelGroup sync = new LabelGroup(parent,getName());
    getKontoList().paint(sync.getComposite());

    sync.addHeadline(i18n.tr("Optionen"));
    sync.addCheckbox(getSyncUeb(),i18n.tr("Offene fällige Überweisungen senden"));
    sync.addCheckbox(getSyncLast(),i18n.tr("Offene fällige Lastschriften senden"));
    sync.addCheckbox(getSyncDauer(),i18n.tr("Daueraufträge synchronisieren"));
    
    ButtonArea b = sync.createButtonArea(1);
    b.addButton(i18n.tr("Synchronisierung starten"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        handleStart();
      }
    },null,true);
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 1;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return true;
  }

  /**
   * Liefert eine Liste der zu synchronisierenden Konten.
   * @return Liste der zu synchroinisierenden Konten.
   * @throws RemoteException
   */
  private Part getKontoList() throws RemoteException
  {
    DBIterator list = Settings.getDBService().createList(Konto.class);
    list.addFilter("synchronize = 1 or synchronize is null");
    KontoList l = new KontoList(list,new KontoNew());
    // BUGZILLA 108 http://www.willuhn.de/bugzilla/show_bug.cgi?id=108
    l.addColumn(i18n.tr("Saldo aktualisiert am"),"saldo_datum", new DateFormatter(HBCI.LONGDATEFORMAT));
    l.setSummary(false);
    
    return l;
  }
  
  /**
   * Liefert eine Checkbox zur Aktivierung der Synchronisierung der Dauerauftraege.
   * @return Checkbox.
   */
  private CheckboxInput getSyncDauer()
  {
    if (this.syncDauer != null)
      return this.syncDauer;
    this.syncDauer = new CheckboxInput(settings.getBoolean("sync.dauer",true));
    return this.syncDauer;
  }

  /**
   * Liefert eine Checkbox zur Aktivierung der Synchronisierung der Ueberweisungen.
   * @return Checkbox.
   */
  private CheckboxInput getSyncUeb()
  {
    if (this.syncUeb != null)
      return this.syncUeb;
    this.syncUeb = new CheckboxInput(settings.getBoolean("sync.ueb",true));
    return this.syncUeb;
  }
  
  /**
   * Liefert eine Checkbox zur Aktivierung der Synchronisierung der Lastschriften.
   * @return Checkbox.
   */
  private CheckboxInput getSyncLast()
  {
    if (this.syncLast != null)
      return this.syncLast;
    this.syncLast = new CheckboxInput(settings.getBoolean("sync.last",true));
    return this.syncLast;
  }

  /**
   * Startet die Synchronisierung der Konten.
   */
  private void handleStart()
  {
    try
    {
      Logger.info("Start synchronize");
      boolean dauer = ((Boolean)getSyncDauer().getValue()).booleanValue();
      boolean last  = ((Boolean)getSyncLast().getValue()).booleanValue();
      boolean ueb   = ((Boolean)getSyncUeb().getValue()).booleanValue();
      settings.setAttribute("sync.dauer",dauer);
      settings.setAttribute("sync.last",last);
      settings.setAttribute("sync.ueb",ueb);
      
      HBCISynchronizer sync = new HBCISynchronizer();
      sync.start();
    }
    catch (Throwable t)
    {
      Logger.error("error while synchronizing",t);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Synchronisieren der Konten"));
    }
    finally
    {
      Logger.info("Synchronize finished");
    }
  }
}


/*********************************************************************
 * $Log: Sync.java,v $
 * Revision 1.1  2005/11/09 01:13:53  willuhn
 * @N chipcard modul fuer AMD64 vergessen
 * @N Startseite jetzt frei konfigurierbar
 *
 **********************************************************************/