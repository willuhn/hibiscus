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

import org.kapott.hbci.passport.AbstractHBCIPassport;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passport.PassportChangeRequest;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, die das Aendern der Kundendaten basierend auf dem 3072-Code uebernimmt.
 */
public class PassportProcessCode3072 implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Objekt vom Typ AbstractHBCIPassport.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof AbstractHBCIPassport))
    {
      Logger.warn("expected object type AbstractHBCIPassport but was " + context);
      return;
    }

    AbstractHBCIPassport hbciPassport = (AbstractHBCIPassport) context;

    Object o = hbciPassport.getPersistentData(PassportHandle.CONTEXT_USERID_CHANGED);
    if (o == null)
    {
      Logger.debug("no changed customer data in persistent data of passport found");
      return;
    }

    try
    {
      String changes = o.toString();
      int pos = changes.indexOf("|");
      if (pos == -1)
      {
        Logger.warn("changes did not contain userId|custId");
        return;
      }

      String userId = changes.substring(0,pos);
      String custId = changes.substring(pos+1);
      if (userId.length() == 0)
      {
        Logger.warn("no userId found");
        return;
      }
      if (custId.length() == 0)
      {
        Logger.warn("no custId found");
        return;
      }

      String custOld = hbciPassport.getCustomerId();
      String userOld = hbciPassport.getUserId();

      String text = i18n.tr("Die Bank hat mitgeteilt, dass sich die Benutzer- und Kundenkennung Ihres\n" +
                            "Bank-Zugangs geändert hat. Die neuen Zugangsdaten lauten:\n\n" +
                            "  Alte Kundenkennung: {0}\n" +
                            "  Neue Kundenkennung: {1}\n\n" +
                            "  Alte Benutzerkennung: {2}\n" +
                            "  Neue Benutzerkennung: {3}\n\n" +
                            "Möchten Sie die geänderten Zugangsdaten jetzt übernehmen?");

      boolean b = Application.getCallback().askUser(text,new String[]{custOld,custId,userOld,userId});
      if (!b)
      {
        Logger.warn("user cancelled request to change userId/customerId");
        return;
      }

      PassportChangeRequest pcr = new PassportChangeRequest(hbciPassport,custId,userId);
      new PassportChange().handleAction(pcr);
    }
    catch (Exception e)
    {
      Logger.error("error while applying new user-/customer data",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Übernehmen der geänderten Zugangsdaten: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
    finally
    {
      // aus den Context-Daten entfernen, wenn wir es behandelt haben
      ((AbstractHBCIPassport)hbciPassport).setPersistentData(PassportHandle.CONTEXT_USERID_CHANGED,null);
    }
  }

}
