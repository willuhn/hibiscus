/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/AccountContainerDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/03/09 01:07:02 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.widgets.Composite;
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
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 */
public class AccountContainerDialog extends AbstractDialog
{

	private HBCIPassport passport = null;
	private AccountContainer container = null;
	private I18N i18n = null;

	private Input blz 				= null;
	private Input host 				= null;
	private Input port				= null;
	private Input filter			= null;
	private Input userid			= null;
	private Input customerid	= null;

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
		group2.addLabelPair(i18n.tr("Hostname des Bankservers"),getHost());
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
			blz = new TextInput(passport.getBLZ(),8);
		return blz;
	}
	
	private Input getHost()
	{
		if (host == null)
			host = new TextInput(passport.getHost());
		return host;
	}
	
	private Input getPort()
	{
		if (port == null)
			port = new IntegerInput(passport.getPort().intValue());
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
			customerid = new TextInput(passport.getCustomerId());
		return customerid;
	}

	private Input getFilter()
	{
		if (filter == null)
			filter = new SelectInput(new String[] {"None","Base64"},passport.getFilterType());
		return filter;
	}

}


/**********************************************************************
 * $Log: AccountContainerDialog.java,v $
 * Revision 1.2  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.1  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 **********************************************************************/