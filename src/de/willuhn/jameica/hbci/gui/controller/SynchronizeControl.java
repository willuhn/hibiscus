/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/SynchronizeControl.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/08/01 16:10:41 $
 * $Author: web0 $
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
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.parts.KontoList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCISynchronizer;
import de.willuhn.logging.Logger;

/**
 * @author willuhn
 */
public class SynchronizeControl extends AbstractControl
{

  private de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(HBCISynchronizer.class);
  
  private CheckboxInput syncDauer = null;
  private CheckboxInput syncUeb   = null;
  private CheckboxInput syncLast  = null;
  
  /**
   * @param view
   */
  public SynchronizeControl(AbstractView view) {
    super(view);
    settings.setStoreWhenRead(false);
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
      // TODO
      t.printStackTrace();
    }
    finally
    {
      Logger.info("Synchronize finished");
    }
  }
}


/*********************************************************************
 * $Log: SynchronizeControl.java,v $
 * Revision 1.1  2005/08/01 16:10:41  web0
 * @N synchronize
 *
 * Revision 1.1  2005/07/29 16:48:13  web0
 * @N Synchronize
 *
 *********************************************************************/