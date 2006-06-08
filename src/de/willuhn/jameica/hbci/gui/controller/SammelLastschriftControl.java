/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/SammelLastschriftControl.java,v $
 * $Revision: 1.12 $
 * $Date: 2006/06/08 22:29:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.SammelLastBuchungExport;
import de.willuhn.jameica.hbci.gui.action.SammelLastBuchungNew;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftNew;
import de.willuhn.jameica.hbci.gui.action.SammelTransferBuchungDelete;
import de.willuhn.jameica.hbci.gui.action.SammelTransferBuchungImport;
import de.willuhn.jameica.hbci.gui.parts.SammelTransferBuchungList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung des Controllers fuer den Dialog "Liste der Sammellastschriften".
 * @author willuhn
 */
public class SammelLastschriftControl extends AbstractSammelTransferControl
{

  private I18N i18n                     	= null;

  private SammelTransfer transfer         = null;
  private TablePart table               	= null;

  /**
   * ct.
   * @param view
   */
  public SammelLastschriftControl(AbstractView view)
  {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSammelTransferControl#getTransfer()
   */
  public SammelTransfer getTransfer() throws RemoteException
  {
    if (transfer != null)
      return transfer;

    transfer = (SammelTransfer) getCurrentObject();
    if (transfer != null)
      return transfer;

    transfer = (SammelLastschrift) Settings.getDBService().createObject(SammelLastschrift.class,null);
    return transfer;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSammelTransferControl#getListe()
   */
  public TablePart getListe() throws RemoteException
  {
    if (table != null)
      return table;

    table = new de.willuhn.jameica.hbci.gui.parts.SammelLastschriftList(new SammelLastschriftNew());
    return table;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSammelTransferControl#getBuchungen()
   */
  public Part getBuchungen() throws RemoteException
  {
    Action a = new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new SammelLastBuchungNew().handleAction(context);
      }
    };
    
    TablePart buchungen = new SammelTransferBuchungList(getTransfer(),a);

    ContextMenu ctx = new ContextMenu();
    ctx.addItem(new CheckedContextMenuItem(i18n.tr("Buchung öffnen"), new SammelLastBuchungNew()));
    ctx.addItem(new NotActiveMenuItem(i18n.tr("Buchung löschen..."), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new SammelTransferBuchungDelete().handleAction(context);
        try
        {
          getSumme().setValue(HBCI.DECIMALFORMAT.format(getTransfer().getSumme()));
        }
        catch (RemoteException e)
        {
          Logger.error("unable to refresh summary",e);
        }
      }
    }));
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new ContextMenuItem(i18n.tr("Neue Buchung..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (handleStore())
        {
          try
          {
            new SammelLastBuchungNew().handleAction(getTransfer());
          }
          catch (RemoteException e)
          {
            Logger.error("unable to load sammellastschrift",e);
            throw new ApplicationException(i18n.tr("Fehler beim Laden der Sammel-Lastschrift"));
          }
        }
      }
    }));
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new ContextMenuItem(i18n.tr("Buchungen importieren..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (handleStore())
        {
          try
          {
            new SammelTransferBuchungImport().handleAction(getTransfer());
          }
          catch (RemoteException e)
          {
            Logger.error("unable to load sammellastschrift",e);
            throw new ApplicationException(i18n.tr("Fehler beim Laden der Sammellastschrift"));
          }
        }
      }
    }));
    ctx.addItem(new ContextMenuItem(i18n.tr("Buchungen exportieren..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          SammelTransfer transfer = getTransfer();
          if (transfer == null || transfer.getBuchungen().size() == 0)
            return;
          new SammelLastBuchungExport().handleAction(transfer);
        }
        catch (RemoteException e)
        {
          Logger.error("unable to load sammellastschrift",e);
          throw new ApplicationException(i18n.tr("Fehler beim Laden der Sammellastschrift"));
        }
      }
    }));
    buchungen.setContextMenu(ctx);
    return buchungen;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSammelTransferControl#handleStore()
   */
  public synchronized boolean handleStore()
  {
    try {
      getTransfer().setKonto((Konto)getKontoAuswahl().getValue());
      getTransfer().setBezeichnung((String)getName().getValue());
      getTransfer().setTermin((Date)getTermin().getValue());
      getTransfer().store();
      GUI.getStatusBar().setSuccessText(i18n.tr("Sammel-Lastschrift gespeichert"));
      return true;
    }
    catch (ApplicationException e2)
    {
      GUI.getView().setErrorText(e2.getMessage());
    }
    catch (RemoteException e)
    {
      Logger.error("error while storing sammellastschrift",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Sammel-Lastschrift"));
    }
    return false;
  }


  /**
   * Ueberschreiben wir, damit das Item nur dann aktiv ist, wenn die
   * Lastschrift noch nicht ausgefuehrt wurde.
   */
  private class NotActiveMenuItem extends ContextMenuItem
  {
    
    /**
     * ct.
     * @param text anzuzeigender Text.
     * @param a auszufuehrende Action.
     */
    public NotActiveMenuItem(String text, Action a)
    {
      super(text, a);
    }

    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o == null)
        return false;
      try
      {
        SammelLastBuchung u = (SammelLastBuchung) o;
        return !u.getSammelTransfer().ausgefuehrt();
      }
      catch (Exception e)
      {
        Logger.error("error while enable check in menu item",e);
      }
      return false;
    }
  }

}

/*****************************************************************************
 * $Log: SammelLastschriftControl.java,v $
 * Revision 1.12  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.11  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.10  2005/08/02 20:09:33  web0
 * @B bug 106
 *
 * Revision 1.9  2005/07/04 12:41:39  web0
 * @B bug 90
 *
 * Revision 1.8  2005/07/04 11:36:53  web0
 * @B bug 89
 *
 * Revision 1.7  2005/06/23 23:03:20  web0
 * @N much better KontoAuswahlDialog
 *
 * Revision 1.6  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.5  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.4  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.3  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 * Revision 1.2  2005/02/28 18:40:49  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
*****************************************************************************/