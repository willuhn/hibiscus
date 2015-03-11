/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ueberschrieben, um zu schecken, ob Termin-Ueberweisungen dabei sind,
 * die bereits an die Bank gesendet wurden.
 */
public class AuslandsUeberweisungDelete extends DBObjectDelete
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.hbci.gui.action.DBObjectDelete#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null)
      throw new ApplicationException(i18n.tr("Keine zu löschenden Daten ausgewählt"));

    if (!(context instanceof AuslandsUeberweisung) && !(context instanceof AuslandsUeberweisung[]))
    {
      Logger.warn("wrong type to delete: " + context.getClass());
      return;
    }
    
    
    try
    {
      boolean array = (context instanceof AuslandsUeberweisung[]);
      
      AuslandsUeberweisung[] list = null;
      if (array)
        list = (AuslandsUeberweisung[]) context;
      else
        list = new AuslandsUeberweisung[]{(AuslandsUeberweisung)context}; // Array mit einem Element

      int count = 0;
      for (AuslandsUeberweisung a:list)
      {
        if (!a.isNewObject() && a.ausgefuehrt() && a.isTerminUeberweisung())
          count++;
      }
      
      if (count > 0)
      {
        String msg = null;
        
        if (array)
        {
          if (count == 1)
          {
            msg = i18n.tr("Einer der Aufträge wurde bereits als bankseitige SEPA-Terminüberweisung gesendet.\n" +
                          "Das Löschen des Auftrages geschieht nur lokal in Hibiscus. Melden Sie sich\n" +
                          "daher ggf. auf der Webseite Ihrer Bank an und löschen den Auftrag auch dort,\n" +
                          "wenn er nicht ausgeführt werden soll.\n\n" +
                          "Aufträge lokal in Hibiscus löschen?");
          }
          else
          {
            msg = i18n.tr("{0} Aufträge wurden bereits als bankseitige SEPA-Terminüberweisung gesendet.\n" +
                          "Das Löschen dieser Aufträge geschieht nur lokal in Hibiscus. Melden Sie sich\n" +
                          "daher ggf. auf der Webseite Ihrer Bank an und löschen diese auch dort,\n" +
                          "wenn sie nicht ausgeführt werden sollen.\n\n" +
                          "Aufträge lokal in Hibiscus löschen?",Integer.toString(count));
          }
        }
        else
        {
          msg = i18n.tr("Der Auftrag wurde bereits als bankseitige SEPA-Terminüberweisung gesendet.\n" +
                        "Das Löschen des Auftrages geschieht nur lokal in Hibiscus. Melden Sie sich\n" +
                        "daher ggf. auf der Webseite Ihrer Bank an und löschen den Auftrag auch dort,\n" +
                        "wenn er nicht ausgeführt werden soll.\n\n" +
                        "Auftrag lokal in Hibiscus löschen?");
        }
        
        if (!Application.getCallback().askUser(msg))
          return;
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("error while checking object",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Auftrag wurde bereits als Termin-Überweisung an Bank übertragen"),StatusBarMessage.TYPE_INFO));
    }
    
    super.handleAction(context);
  }

}


