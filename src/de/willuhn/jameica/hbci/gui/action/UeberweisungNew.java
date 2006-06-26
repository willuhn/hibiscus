/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UeberweisungNew.java,v $
 * $Revision: 1.6 $
 * $Date: 2006/06/26 13:25:20 $
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
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.io.EbayKontoData;
import de.willuhn.jameica.hbci.io.EbayKontoParser;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer neue Ueberweisung.
 */
public class UeberweisungNew implements Action
{

  /**
   * Als Context kann ein Konto, ein Empfaenger oder eine Ueberweisung angegeben werden.
   * Abhaengig davon wird das eine oder andere Feld in der neuen Ueberweisung
   * vorausgefuellt oder die uebergebene Ueberweisung geladen.
   * Wenn nichts angegeben ist, wird eine leere Ueberweisung erstellt und angezeigt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
	  final I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
	  
		Ueberweisung u = null;

		if (context instanceof Ueberweisung)
		{
			u = (Ueberweisung) context;
		}
		else if (context instanceof Konto)
		{
			try {
				Konto k = (Konto) context;
				u = (Ueberweisung) Settings.getDBService().createObject(Ueberweisung.class,null);
				u.setKonto(k);
			}
			catch (RemoteException e)
			{
				// Dann halt nicht
			}
		}
		else if (context instanceof Adresse)
		{
			try {
				Adresse e = (Adresse) context;
				u = (Ueberweisung) Settings.getDBService().createObject(Ueberweisung.class,null);
				u.setGegenkonto(e);
			}
			catch (RemoteException e)
			{
				// Dann halt nicht
			}
		}
		else 
		{
			// Clipboard parsen
			EbayKontoParser parser = new EbayKontoParser();
			EbayKontoData data = null;
      
      try
      {
        try {
          data = parser.readFromClipboard();
        }
        catch (ApplicationException ex)
        {
          Application.getController().getApplicationCallback().notifyUser(i18n.tr("Kontodaten aus Zwischenablage fehlerhaft.\n{0}",ex.getLocalizedMessage()));
        }
          
        if (data != null)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr(i18n.tr("In der Zwischenablage wurden Kontodaten gefunden.\nDie Überweisung wird damit vorbelegt.")),StatusBarMessage.TYPE_SUCCESS));
             
          u = (Ueberweisung) Settings.getDBService().createObject(Ueberweisung.class,null);
          u.setGegenkontoName(data.getInhaber());
          u.setGegenkontoBLZ(data.getBlz());
          u.setGegenkontoNummer(data.getNummer());
        }
      }
      catch (ApplicationException ae)
      {
        throw ae;
      }
      catch (Exception e)
      {
        Logger.error("unable to parse account data from clipboard",e);
      }
    }
		GUI.startView(de.willuhn.jameica.hbci.gui.views.UeberweisungNew.class,u);
 	}
}


/**********************************************************************
 * $Log: UeberweisungNew.java,v $
 * Revision 1.6  2006/06/26 13:25:20  willuhn
 * @N Franks eBay-Parser
 *
 * Revision 1.5  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.4  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 * Revision 1.3  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.2  2004/11/13 17:12:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/13 17:02:03  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.2  2004/10/18 23:38:17  willuhn
 * @C Refactoring
 * @C Aufloesung der Listener und Ersatz gegen Actions
 *
 * Revision 1.1  2004/10/12 23:48:39  willuhn
 * @N Actions
 *
 **********************************************************************/