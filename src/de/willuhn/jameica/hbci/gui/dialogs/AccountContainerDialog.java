/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/AccountContainerDialog.java,v $
 * $Revision: 1.9 $
 * $Date: 2007/04/09 22:45:12 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.AccountContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 */
public class AccountContainerDialog extends AbstractDialog
{

	private HBCIPassport passport = null;
	private AccountContainer container = null;
	private I18N i18n = null;

	private Input blz 				  = null;
	private Input host 				  = null;
	private Input port				  = null;
	private SelectInput filter	= null;
	private Input userid			  = null;
	private Input customerid	  = null;

  /**
   * ct.
   * @param passport
   */
  public AccountContainerDialog(HBCIPassport passport)
  {
    super(AccountContainerDialog.POSITION_CENTER);
    this.passport = passport;
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setTitle(i18n.tr("Eingabe Ihrer Bank-Daten"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
  	LabelGroup group = new LabelGroup(parent,i18n.tr("Benutzerdaten"));
  	group.addText(i18n.tr("Bitte geben Sie die Benutzerdaten des Kontos ein."),true);
  	group.addLabelPair(i18n.tr("Benutzerkennung"),getUserId());
		group.addLabelPair(i18n.tr("Kundenkennung"),getCustomerId());
		group.addLabelPair(i18n.tr("Bankleitzahl"),getBLZ());

		LabelGroup group2 = new LabelGroup(parent,i18n.tr("Verbindungsdaten"));
		group2.addText(i18n.tr("Geben Sie hier bitte die Verbindungsdaten zu Ihrer Bank ein."),true);
		group2.addLabelPair(i18n.tr("Hostname/URL des Bankservers"),getHost());
		group2.addLabelPair(i18n.tr("TCP-Port des Bankservers"),getPort());
		group2.addLabelPair(i18n.tr("Filter für die Übertragung"),getFilter());

		ButtonArea buttons = new ButtonArea(parent,2);
		buttons.addButton(i18n.tr("Übernehmen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	container = new AccountContainer();
      	container.blz 				= (String) getBLZ().getValue();
      	container.customerid 	= (String) getCustomerId().getValue();
      	container.filter 			= (String) getFilter().getValue();
      	container.host 				= (String) getHost().getValue();
      	container.port 				= ((Integer) getPort().getValue()).intValue();
      	container.userid 			= (String) getUserId().getValue();
      	close();
      }
    },null,true);
    buttons.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	throw new OperationCanceledException();
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return container;
  }

	private Input getBLZ()
	{
		if (blz == null)
    {
      blz = new BLZInput(passport.getBLZ());
      blz.addListener(new Listener()
      {
        public void handleEvent(Event arg0)
        {
          try
          {
            String b = (String)blz.getValue();
            // Neu im aktuellen HBCI4Java-Snapshot. IP/Hostname zur BLZ ermitteln
            String host = (String) getHost().getValue();
            if (host == null || host.length() == 0)
            {
              String clazz = passport.getClass().getName();
              if (clazz.toUpperCase().indexOf("PINTAN") != -1)
              {
                Logger.info("auto detecting pin/tan url by blz");
                String s = HBCIUtils.getPinTanURLForBLZ(b);
                if (s != null && s.startsWith("https://"))
                  s = s.replaceFirst("https://","");
                getHost().setValue(s);
              }
              else
              {
                Logger.info("auto detecting rdh/ddv ip by blz");
                getHost().setValue(HBCIUtils.getHBCIHostForBLZ(b));
              }
            }
          }
          catch (Exception e)
          {
            Logger.error("error while auto detecting url/ip for blz",e);
          }
        }
      });
    }
		return blz;
	}
	
	private Input getHost()
	{
		if (host == null)
			host = new TextInput(passport.getHost())
      {
        public Object getValue()
        {
          // Ueberschrieben, um ggf. das https:// am Anfang abzuschneiden
          String s = (String) super.getValue();
          if (s == null)
            return null;
          if (s.startsWith("https://"))
            s = s.replaceFirst("https://","");
          return s;
        }
      };
		host.setComment(i18n.tr("Bei PIN/TAN bitte ohne \"https://\" eingeben"));
		return host;
	}
	
	private Input getPort()
	{
		if (port == null)
			port = new IntegerInput(passport.getPort().intValue());
		port.setComment(i18n.tr("Bei PIN/TAN \"443\", sonst \"3000\""));
		return port;
	}

	private Input getUserId()
	{
		if (userid == null)
			userid = new TextInput(passport.getUserId());
		return userid;
	}
	
	private Input getCustomerId()
	{
		if (customerid == null)
		{
			customerid = new TextInput(passport.getCustomerId());
			customerid.setComment(i18n.tr("Meist identisch mit Benutzerkennung"));
		}
			
		return customerid;
	}

	private SelectInput getFilter()
	{
		if (filter == null)
			filter = new SelectInput(new String[] {"None","Base64"},passport.getFilterType());
		filter.setComment(i18n.tr("Bei PIN/TAN meist \"Base64\", sonst \"None\""));

    String clazz = passport.getClass().getName();
    if (clazz.toUpperCase().indexOf("PINTAN") != -1)
      filter.setPreselected("Base64");
    
    return filter;
	}

}


/**********************************************************************
 * $Log: AccountContainerDialog.java,v $
 * Revision 1.9  2007/04/09 22:45:12  willuhn
 * @N Bug 380
 *
 * Revision 1.8  2005/06/24 14:55:49  web0
 * *** empty log message ***
 *
 * Revision 1.7  2005/06/13 09:18:21  web0
 * @N url autodetection while creating a new passport
 *
 * Revision 1.6  2005/05/25 00:42:04  web0
 * @N Dialoge fuer OP-Verwaltung
 *
 * Revision 1.5  2005/04/05 21:51:54  web0
 * @B Begrenzung aller BLZ-Eingaben auf 8 Zeichen
 *
 * Revision 1.4  2005/03/23 00:05:46  web0
 * @C RDH fixes
 *
 * Revision 1.3  2005/03/11 02:44:42  web0
 * @N added pin/tan support
 *
 * Revision 1.2  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.1  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 **********************************************************************/