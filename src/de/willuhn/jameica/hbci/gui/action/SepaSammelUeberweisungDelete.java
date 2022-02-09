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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
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
public class SepaSammelUeberweisungDelete extends DBObjectDelete
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null)
      throw new ApplicationException(i18n.tr("Keine zu l�schenden Daten ausgew�hlt"));

    if (!(context instanceof SepaSammelUeberweisung) && !(context instanceof SepaSammelUeberweisung[]))
    {
      Logger.warn("wrong type to delete: " + context.getClass());
      return;
    }
    
    
    try
    {
      boolean array = (context instanceof SepaSammelUeberweisung[]);
      
      SepaSammelUeberweisung[] list = null;
      if (array)
        list = (SepaSammelUeberweisung[]) context;
      else
        list = new SepaSammelUeberweisung[]{(SepaSammelUeberweisung)context}; // Array mit einem Element

      int count = 0;
      for (SepaSammelUeberweisung a:list)
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
            msg = i18n.tr("Einer der Auftr�ge wurde bereits als bankseitige SEPA-Termin�berweisung gesendet.\n" +
                          "Das L�schen des Auftrages geschieht nur lokal in Hibiscus. Melden Sie sich\n" +
                          "daher ggf. auf der Webseite Ihrer Bank an und l�schen den Auftrag auch dort,\n" +
                          "wenn er nicht ausgef�hrt werden soll.\n\n" +
                          "Auftr�ge lokal in Hibiscus l�schen?");
          }
          else
          {
            msg = i18n.tr("{0} Auftr�ge wurden bereits als bankseitige SEPA-Termin�berweisung gesendet.\n" +
                          "Das L�schen dieser Auftr�ge geschieht nur lokal in Hibiscus. Melden Sie sich\n" +
                          "daher ggf. auf der Webseite Ihrer Bank an und l�schen diese auch dort,\n" +
                          "wenn sie nicht ausgef�hrt werden sollen.\n\n" +
                          "Auftr�ge lokal in Hibiscus l�schen?",Integer.toString(count));
          }
        }
        else
        {
          msg = i18n.tr("Der Auftrag wurde bereits als bankseitige SEPA-Termin�berweisung gesendet.\n" +
                        "Das L�schen des Auftrages geschieht nur lokal in Hibiscus. Melden Sie sich\n" +
                        "daher ggf. auf der Webseite Ihrer Bank an und l�schen den Auftrag auch dort,\n" +
                        "wenn er nicht ausgef�hrt werden soll.\n\n" +
                        "Auftrag lokal in Hibiscus l�schen?");
        }
        
        if (!Application.getCallback().askUser(msg))
          throw new OperationCanceledException();
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("error while checking object",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Auftrag wurde bereits als Termin-�berweisung an Bank �bertragen"),StatusBarMessage.TYPE_INFO));
    }
    
    super.handleAction(context);
  }

}


