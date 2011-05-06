/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/Sync.java,v $
 * $Revision: 1.18 $
 * $Date: 2011/05/06 12:35:48 $
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

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.HBCISynchronize;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.gui.dialogs.SynchronizeOptionsDialog;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.parts.SynchronizeList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.synchronize.SynchronizeEngine;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Box zum Synchronisieren der Konten.
 */
public class Sync extends AbstractBox implements Box
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private SynchronizeList list = null;

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Konten synchronisieren");
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    // BUGZILLA 433
    this.list = new SynchronizeList();
    list.paint(parent);

    ButtonArea b = new ButtonArea();

    // BUGZILLA 226
    b.addButton(i18n.tr("Optionen..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          KontoAuswahlDialog d1 = new KontoAuswahlDialog(null,KontoFilter.SYNCED,KontoAuswahlDialog.POSITION_CENTER);
          d1.setText(i18n.tr("Bitte wählen Sie das Konto, für welches Sie die " +
                             "Synchronisierungsoptionen ändern möchten."));
          Konto k = (Konto) d1.open();
          if (k == null)
            return;

          SynchronizeOptionsDialog d = new SynchronizeOptionsDialog(k,SynchronizeOptionsDialog.POSITION_CENTER);
          d.open();
          
          // So, jetzt muessen wir die Liste der Sync-Jobs neu befuellen
          list.removeAll();
          GenericIterator items = SynchronizeEngine.getInstance().getSynchronizeJobs();
          while (items.hasNext())
          {
            list.addItem(items.next());
          }
          
          // und neu sortieren
          list.sort();
        }
        catch (OperationCanceledException oce)
        {
          // ignore
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (Exception e)
        {
          Logger.error("unable to configure synchronize options");
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Konfigurieren der Synchronisierungsoptionen"),StatusBarMessage.TYPE_ERROR));
        }
        
      }
    },null,false,"document-properties.png");
    
    b.addButton(i18n.tr("Synchronisierung starten"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        handleStart();
      }
    },null,true,"mail-send-receive.png");
    
    b.paint(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 2;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#getHeight()
   */
  public int getHeight()
  {
    return 200;
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
      sync.handleAction(this.list == null ? null : this.list.getItems());
    }
    catch (Throwable t)
    {
      Logger.error("error while synchronizing",t);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Synchronisieren der Konten"));
    }
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
 * $Log: Sync.java,v $
 * Revision 1.18  2011/05/06 12:35:48  willuhn
 * @N Neuer Konto-Auswahldialog mit Combobox statt Tabelle. Ist ergonomischer.
 *
 * Revision 1.17  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 * Revision 1.16  2009/01/20 10:51:46  willuhn
 * @N Mehr Icons - fuer Buttons
 *
 * Revision 1.15  2008/01/04 16:39:31  willuhn
 * @N Weitere Hoehen-Angaben von Komponenten
 *
 * Revision 1.14  2007/12/18 17:10:22  willuhn
 * @N Neues ExpandPart
 * @N Boxen auf der Startseite koennen jetzt zusammengeklappt werden
 *
 * Revision 1.13  2007/07/10 09:33:38  willuhn
 * @B Bug 433
 *
 * Revision 1.12  2007/05/16 11:32:30  willuhn
 * @N Redesign der SynchronizeEngine. Ermittelt die HBCI-Jobs jetzt ueber generische "SynchronizeJobProvider". Damit ist die Liste der Sync-Jobs erweiterbar
 *
 * Revision 1.11  2006/12/29 15:26:56  willuhn
 * @N ImportMessageConsumer
 *
 * Revision 1.10  2006/07/05 22:18:16  willuhn
 * @N Einzelne Sync-Jobs koennen nun selektiv auch einmalig direkt in der Sync-Liste deaktiviert werden
 *
 * Revision 1.9  2006/06/29 23:10:33  willuhn
 * @R Box-System aus Hibiscus in Jameica-Source verschoben
 * @C keine eigene Startseite mehr, jetzt alles ueber Jameica-Boxsystem geregelt
 *
 * Revision 1.8  2006/04/18 22:38:16  willuhn
 * @N bug 227
 *
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