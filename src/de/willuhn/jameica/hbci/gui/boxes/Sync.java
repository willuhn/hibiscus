/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/Sync.java,v $
 * $Revision: 1.7 $
 * $Date: 2006/03/27 21:34:16 $
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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.HBCISynchronize;
import de.willuhn.jameica.hbci.gui.parts.SynchronizeList;
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
  private SynchronizeList list = null;

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
    // BUGZILLA 209
    LabelGroup sync = new LabelGroup(parent,getName(),true);
    getSynchronizeList().paint(sync.getComposite());

    ButtonArea b = sync.createButtonArea(1);
    b.addButton(i18n.tr("Synchronisierung starten"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        handleStart();
      }
    },null,true);
  }

  /**
   * Liefert die Liste der Synchronisierungs-Jobs.
   * @return Liste.
   * @throws RemoteException
   */
  private TablePart getSynchronizeList() throws RemoteException
  {
    if (this.list == null)
    {
      this.list = new SynchronizeList();
      this.list.setSummary(false);
    }
    return this.list;
  }
  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 2;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return true;
  }

  /**
   * Startet die Synchronisierung der Konten.
   */
  private void handleStart()
  {
    try
    {
      Logger.info("Start synchronize");
      HBCISynchronize sync = new HBCISynchronize();
      sync.handleAction(null);
    }
    catch (Throwable t)
    {
      Logger.error("error while synchronizing",t);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Synchronisieren der Konten"));
    }
  }
}


/*********************************************************************
 * $Log: Sync.java,v $
 * Revision 1.7  2006/03/27 21:34:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2006/03/20 00:35:53  willuhn
 * @N new box "Konten-Übersicht"
 *
 * Revision 1.5  2006/03/19 23:04:49  willuhn
 * @B bug 209
 *
 * Revision 1.4  2006/03/17 00:51:25  willuhn
 * @N bug 209 Neues Synchronisierungs-Subsystem
 *
 * Revision 1.3  2006/02/06 17:16:10  willuhn
 * @B Fehler beim Synchronisieren mehrerer Konten (Dead-Lock)
 *
 * Revision 1.2  2006/01/11 00:29:21  willuhn
 * @C HBCISynchronizer nach gui.action verschoben
 * @R undo bug 179 (blendet zu zeitig aus, wenn mehrere Jobs (Synchronize) laufen)
 *
 * Revision 1.1  2005/11/09 01:13:53  willuhn
 * @N chipcard modul fuer AMD64 vergessen
 * @N Startseite jetzt frei konfigurierbar
 *
 **********************************************************************/