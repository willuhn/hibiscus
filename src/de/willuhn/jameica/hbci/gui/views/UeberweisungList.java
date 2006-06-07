/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/UeberweisungList.java,v $
 * $Revision: 1.7 $
 * $Date: 2006/06/07 17:26:40 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.UeberweisungImport;
import de.willuhn.jameica.hbci.gui.action.UeberweisungNew;
import de.willuhn.jameica.hbci.gui.controller.UeberweisungControl;
import de.willuhn.jameica.hbci.io.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Ueberweisungen an.
 */
public class UeberweisungList extends AbstractView {

  private UeberweisungControl control = null;
  private MessageConsumer mc          = null;
  
  /**
   * ct.
   */
  public UeberweisungList()
  {
    this.mc      = new UebMessageConsumer();
    this.control = new UeberweisungControl(this);
  }
    
  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Vorhandene Überweisungen"));
		
		
		try {

			control.getUeberweisungListe().paint(getParent());

			ButtonArea buttons = new ButtonArea(getParent(),3);
      buttons.addButton(i18n.tr("Zurück"),new Back());
      buttons.addButton(i18n.tr("Importieren..."),new UeberweisungImport());
			buttons.addButton(i18n.tr("neue Überweisung"),new UeberweisungNew());

      // Wir erstellen noch einen Message-Consumer, damit wir ueber neu eintreffende
      // Uebweiseungen informiert werden.
      Application.getMessagingFactory().registerMessageConsumer(this.mc);

		}
		catch (Exception e)
		{
			Logger.error("error while loading ueberweisung list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Überweisungen."));
		}
  }
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
    Application.getMessagingFactory().unRegisterMessageConsumer(this.mc);
    super.unbind();
  }
  

  /**
   * Hilfsklasse damit wir ueber importierte Ueberweisungen informiert werden.
   */
  public class UebMessageConsumer implements MessageConsumer
  {
    /**
     * ct.
     */
    public UebMessageConsumer()
    {
      super();
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{ImportMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      if (message == null || !(message instanceof ImportMessage))
        return;
      final GenericObject o = ((ImportMessage)message).getImportedObject();
      
      if (o == null || !(o instanceof Ueberweisung))
        return;
      
      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          try
          {
            control.getUeberweisungListe().addItem(o);
          }
          catch (Exception e)
          {
            Logger.error("unable to add object to list",e);
          }
        }
      });
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
  }
}


/**********************************************************************
 * $Log: UeberweisungList.java,v $
 * Revision 1.7  2006/06/07 17:26:40  willuhn
 * @N DTAUS-Import fuer Lastschriften
 * @B Satusbar-Update in DTAUSImport gefixt
 *
 * Revision 1.6  2006/06/06 22:41:26  willuhn
 * @N Generische Loesch-Action fuer DBObjects (DBObjectDelete)
 * @N Live-Aktualisierung der Tabelle mit den importierten Ueberweisungen
 * @B Korrekte Berechnung des Fortschrittsbalken bei Import
 *
 * Revision 1.5  2006/05/25 13:47:03  willuhn
 * @N Skeleton for DTAUS-Import
 *
 * Revision 1.4  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.3  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 * Revision 1.2  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.1  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.10  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/10/20 12:34:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.6  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.3  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.1  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/