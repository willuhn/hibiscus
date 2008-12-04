/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/AbstractTransferMerge.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/12/04 21:30:06 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.TransferMergeDialog;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Action, ueber Einzel-Auftraege zu einem Sammel-Auftrag zusammenzufassen.
 */
public abstract class AbstractTransferMerge implements Action
{

  /**
   * Erzeugt den Sammelauftrag basierend auf dem Context, speichert alles in der Datenbank
   * und liefert ihn zurueck.
   * @param context der Context aus handleAction.
   * @return der erzeugte und bereits gespeicherte Sammel-Auftrag.
   * @throws ApplicationException
   */
  SammelTransfer create(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Aufträge aus"));

    if (!(context instanceof HibiscusTransfer) && !(context instanceof HibiscusTransfer[]))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Aufträge aus"));

    HibiscusTransfer[] transfers = null;
    
    if (context instanceof HibiscusTransfer)
      transfers = new HibiscusTransfer[]{(HibiscusTransfer) context};
    else
      transfers = (HibiscusTransfer[]) context;
    
    if (transfers.length == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Aufträge aus"));
      
    SammelTransfer t = null;
		try
    {
		  t = (SammelTransfer) Settings.getDBService().createObject(getTransferClass(),null);
      // Wenn der Sammel-Transfer noch kein Konto hat, nehmen wir das erste
      // der Einzel-Auftraege
      if (t.getKonto() == null)
        t.setKonto(transfers[0].getKonto());
      TransferMergeDialog d = new TransferMergeDialog(t,TransferMergeDialog.POSITION_CENTER);
      boolean delete = ((Boolean) d.open()).booleanValue();
      
      // OK, wir starten die Erzeugung des Auftrages
      t.transactionBegin();
      t.store();
      
      Class bClass = getBuchungClass();
      for (int i=0;i<transfers.length;++i)
      {
        SammelTransferBuchung buchung = (SammelTransferBuchung) Settings.getDBService().createObject(bClass,null);
        buchung.setSammelTransfer(t);
        buchung.setBetrag(transfers[i].getBetrag());
        buchung.setGegenkontoBLZ(transfers[i].getGegenkontoBLZ());
        buchung.setGegenkontoName(transfers[i].getGegenkontoName());
        buchung.setGegenkontoNummer(transfers[i].getGegenkontoNummer());
        buchung.setZweck(transfers[i].getZweck());
        buchung.setZweck2(transfers[i].getZweck2());
        buchung.setWeitereVerwendungszwecke(transfers[i].getWeitereVerwendungszwecke());
        buchung.store();
        
        if (delete)
          transfers[i].delete();
      }
      t.transactionCommit();
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Sammelauftrag erzeugt"), StatusBarMessage.TYPE_SUCCESS));
      return t;
		}
		catch (ApplicationException ae)
		{
      if (t != null) {
        try {
          t.transactionRollback();
        } catch (Exception e) {Logger.error("unable to rollback transaction",e);}
      }
			throw ae;
		}
    catch (OperationCanceledException oce)
    {
      if (t != null) {
        try {
          t.transactionRollback();
        } catch (Exception e) {Logger.error("unable to rollback transaction",e);}
      }
      throw oce;
    }
		catch (Exception e)
		{
      if (t != null) {
        try {
          t.transactionRollback();
        } catch (Exception ex) {Logger.error("unable to rollback transaction",ex);}
      }
			Logger.error("error while exporting transfers",e);
			throw new ApplicationException(i18n.tr("Fehler beim Erzeugen des Sammel-Auftrages"));
		}
  }

  /**
   * Muss von abgeleieteten Klassen implementiert werden, um das Interface
   * des Sammel-Auftrages zurueckzuliefern.
   * @return Interface des Sammelauftrages.
   * @throws RemoteException
   */
  abstract Class getTransferClass() throws RemoteException;
  
  /**
   * Muss von abgeleieteten Klassen implementiert werden, um das Interface
   * einer Buchung des Sammel-Auftrages zurueckzuliefern.
   * @return Interface einer Buchung des Sammel-Auftrages.
   * @throws RemoteException
   */
  abstract Class getBuchungClass() throws RemoteException;
}


/**********************************************************************
 * $Log: AbstractTransferMerge.java,v $
 * Revision 1.2  2008/12/04 21:30:06  willuhn
 * @N BUGZILLA 188
 *
 * Revision 1.1  2007/10/25 15:47:21  willuhn
 * @N Einzelauftraege zu Sammel-Auftraegen zusammenfassen (BUGZILLA 402)
 *
 **********************************************************************/