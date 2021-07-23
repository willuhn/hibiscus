/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Deaktiviert ein Konto.
 */
public class KontoDisable implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Konto))
      return;

    Konto k = (Konto) context;

    try
    {
      // Ist schon deaktiviert
      if (k.hasFlag(Konto.FLAG_DISABLED))
        return;

      String s = i18n.tr("Sind Sie sicher, dass Sie das Konto deaktivieren möchten?\n\n" +
                         "Der Saldo wird hierbei gelöscht. Geschäftsvorfälle können anschließend\n" +
                         "nicht mehr über dieses Konto ausgeführt werden.");

      if (!Application.getCallback().askUser(s))
        return;

      // Konto zuruecksetzen
      k.transactionBegin();
      k.reset();
      k.setFlags(k.getFlags() | Konto.FLAG_DISABLED);
      k.store();

      Logger.info("disabled account id: " + k.getID());

      // Dauerauftraege, die noch bei der Bank liegen als offline markieren
      DBIterator dalist = k.getDauerauftraege();
      while (dalist.hasNext())
      {
        Dauerauftrag da = (Dauerauftrag) dalist.next();
        if (da.isActive())
        {
          Logger.info("  removing order id from da: " + da.getID() + ", order id: " + da.getOrderID());
          da.setOrderID(null);
          da.store();
        }
      }

      k.transactionCommit();
      Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(k));
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Konto deaktiviert"), StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      try {
        k.transactionRollback();
      }
      catch (Exception e1) {
        Logger.error("unable to rollback transaction",e1);
      }

      if (e instanceof ApplicationException)
        throw (ApplicationException) e;

      Logger.error("error while disabling account",e);
      throw new ApplicationException(i18n.tr("Fehler beim Deaktivieren des Kontos"));
    }
  }

}


/**********************************************************************
 * $Log: KontoDisable.java,v $
 * Revision 1.2  2010/04/22 16:10:43  willuhn
 * @C Saldo kann bei Offline-Konten zwar nicht manuell bearbeitet werden, dafuer wird er aber beim Zuruecksetzen des Kontos (heisst jetzt "Saldo und Datum zuruecksetzen" statt "Kontoauszugsdatum zuruecksetzen") jetzt ebenfalls geloescht
 *
 * Revision 1.1  2009/09/15 00:23:34  willuhn
 * @N BUGZILLA 745
 *
 **********************************************************************/
