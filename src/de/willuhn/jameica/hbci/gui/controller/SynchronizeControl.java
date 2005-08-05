/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/SynchronizeControl.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/08/05 16:33:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.parts.KontoList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCISynchronizer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * @author willuhn
 */
public class SynchronizeControl extends AbstractControl
{

  private de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(HBCISynchronizer.class);
  
  private CheckboxInput syncDauer = null;
  private CheckboxInput syncUeb   = null;
  private CheckboxInput syncLast  = null;
  
  private I18N i18n = null;
  
  /**
   * @param view
   */
  public SynchronizeControl(AbstractView view) {
    super(view);
    settings.setStoreWhenRead(false);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Liefert eine Liste der zu synchronisierenden Konten.
   * @return Liste der zu synchroinisierenden Konten.
   * @throws RemoteException
   */
  public Part getKontoList() throws RemoteException
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
  public CheckboxInput getSyncDauer()
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
  public CheckboxInput getSyncUeb()
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
  public CheckboxInput getSyncLast()
  {
    if (this.syncLast != null)
      return this.syncLast;
    this.syncLast = new CheckboxInput(settings.getBoolean("sync.last",true));
    return this.syncLast;
  }
  
  
  /**
   * Startet die Synchronisierung der Konten.
   */
  public void handleStart()
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
 * $Log: SynchronizeControl.java,v $
 * Revision 1.3  2005/08/05 16:33:42  willuhn
 * @B bug 108
 * @B bug 110
 *
 * Revision 1.2  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/08/01 16:10:41  web0
 * @N synchronize
 *
 * Revision 1.1  2005/07/29 16:48:13  web0
 * @N Synchronize
 *
 *********************************************************************/