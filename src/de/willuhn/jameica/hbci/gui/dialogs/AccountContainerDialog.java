/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.passport.AbstractPinTanPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.AccountContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Abfrage der Benutzerdaten fuer einen Bank-Zugang.
 */
public class AccountContainerDialog extends AbstractDialog
{
  private final static int WINDOW_WIDTH = 600;

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

	private final HBCIPassport passport;
	private AccountContainer container = null;

	private Input blz 				  = null;
	private Input host 				  = null;
	private Input port				  = null;
	private SelectInput filter	= null;
	private Input userid			  = null;
	private Input customerid	  = null;
	
	private LabelInput text     = null;

  /**
   * ct.
   * @param passport
   */
  public AccountContainerDialog(HBCIPassport passport)
  {
    super(AccountContainerDialog.POSITION_CENTER);
    this.passport = passport;
    this.setTitle(i18n.tr("Eingabe Ihrer Bank-Daten"));
    setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container c = new SimpleContainer(parent);

    c.addHeadline(i18n.tr("Benutzerdaten"));
  	c.addText(i18n.tr("Bitte geben Sie die Benutzerdaten des Kontos ein."),true);
  	c.addLabelPair(i18n.tr("Benutzerkennung"),getUserId());
  	c.addLabelPair(i18n.tr("Kundenkennung"),getCustomerId());
  	c.addLabelPair(i18n.tr("Bankleitzahl"),getBLZ());

		c.addHeadline(i18n.tr("Verbindungsdaten"));
		c.addText(i18n.tr("Geben Sie hier bitte die Verbindungsdaten zu Ihrer Bank ein."),true);
		c.addLabelPair(i18n.tr("Hostname/URL des Bankservers"),getHost());
		c.addLabelPair(i18n.tr("TCP-Port des Bankservers"),getPort());
		c.addLabelPair(i18n.tr("Filter für die Übertragung"),getFilter());

		c.addLabelPair("",getText());

		ButtonArea buttons = new ButtonArea();
		buttons.addButton(i18n.tr("Übernehmen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	container = new AccountContainer();
      	container.blz 				= (String) getBLZ().getValue();
        container.userid      = (String) getUserId().getValue();
      	container.customerid 	= (String) getCustomerId().getValue();
      	container.filter 			= (String) getFilter().getValue();
      	container.host 				= (String) getHost().getValue();
      	
      	Integer i = ((Integer) getPort().getValue());
      	container.port 				= i != null ? i.intValue() : -1;
      	
      	// Check der Pflichtfelder
        if (container.userid == null || container.userid.length() == 0)
        {
          getText().setValue(i18n.tr("Bitte geben Sie eine Benutzerkennung ein."));
          return;
        }
      	if (container.blz == null || container.blz.length() == 0)
      	{
      	  getText().setValue(i18n.tr("Bitte geben Sie eine Bankleitzahl ein."));
      	  return;
      	}
        if (container.host == null || container.host.length() == 0)
        {
          getText().setValue(i18n.tr("Bitte geben Sie den Hostnamen bzw. die URL des Bankservers ein."));
          return;
        }
        if (container.port <= 0)
        {
          getText().setValue(i18n.tr("Bitte geben Sie den TCP-Port des Bankservers ein."));
          return;
        }
      	
      	close();
      }
    },null,true,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
    c.addButtonArea(buttons);

    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return container;
  }
  
  /**
   * Liefert ein Label mit einem Hinweistext.
   * @return Label.
   */
  private LabelInput getText()
  {
    if (this.text == null)
    {
      this.text = new LabelInput("");
      this.text.setColor(Color.ERROR);
    }
    return this.text;
  }

	/**
	 * Liefert ein Eingabefeld fuer die BLZ.
	 * @return Eingabefeld.
	 */
	private Input getBLZ()
	{
	  if (this.blz != null)
	    return this.blz;
	  
	  this.blz = new BLZInput(this.passport.getBLZ());
	  this.blz.setMandatory(true);
	  this.blz.addListener(new Listener()
    {
      public void handleEvent(Event arg0)
      {
        try
        {
          String host = (String) getHost().getValue();
          if (host == null || host.length() == 0)
          {
            
            String b = (String) blz.getValue();
            BankInfo bi = HBCIProperties.getBankInfo(b);
            
            if (bi != null)
            {
              String clazz = passport.getClass().getName();
              if (clazz.toUpperCase().indexOf("PINTAN") != -1)
              {
                String s = bi.getPinTanAddress();
                if (s != null && s.startsWith("https://"))
                  s = s.replaceFirst("https://","");
                getHost().setValue(s);
              }
              else
              {
                getHost().setValue(bi.getRdhAddress());
              }
            }

          }
        }
        catch (Exception e)
        {
          Logger.error("error while auto detecting url/ip for blz",e);
        }
      }
    });
		return this.blz;
	}
	
	/**
	 * Liefert ein Eingabefeld fuer den Hostnamen des Bank-Servers.
	 * @return Eingabefeld.
	 */
	private Input getHost()
	{
    if (this.host != null)
      return this.host;

    this.host = new TextInput(this.passport.getHost())
    {
      public Object getValue()
      {
        // Ueberschrieben, um ggf. das https:// am Anfang abzuschneiden
        String s = (String) super.getValue();
        if (s == null || s.length() == 0)
          return null;
        return cleanUrl(s);
      }
    };
    
    // BUGZILLA 381
    this.host.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        // Triggert den Code zum Entfernen des "https://" und der Leerzeichen
        getHost().setValue(getHost().getValue());
      }
    
    });
		this.host.setComment(i18n.tr("Bitte ohne \"https://\" eingeben"));
		this.host.setMandatory(true);
		return this.host;
	}
	
	/**
	 * Liefert ein Eingabefeld fuer den TCP-Port.
	 * @return
	 */
	private Input getPort()
	{
		if (this.port == null)
		{
		  Integer value = this.passport.getPort();
		  int i = (value != null && value.intValue() > 0) ? value.intValue() : ((this.passport instanceof AbstractPinTanPassport) ? 443 : 3000); 
      this.port = new IntegerInput(i);
      this.port.setComment(i18n.tr("Bei PIN/TAN \"443\", sonst \"3000\""));
      this.port.setMandatory(true);
		}
		return this.port;
	}

	/**
	 * Liefert ein Eingabefeld fuer die Benutzerkennung.
	 * @return Eingabefeld.
	 */
	private Input getUserId()
	{
		if (this.userid == null)
		{
      this.userid = new TextInput(this.passport.getUserId(),30);
      this.userid.setMandatory(true);
		}
		return this.userid;
	}
	
	/**
	 * Liefert ein Eingabefeld fuer die Kundenkennung.
	 * @return Eingabefeld.
	 */
	private Input getCustomerId()
	{
		if (this.customerid == null)
		{
			this.customerid = new TextInput(this.passport.getCustomerId(),30);
			this.customerid.setComment(i18n.tr("Meist identisch mit Benutzerkennung"));
		}
		return this.customerid;
	}

	/**
	 * Liefert ein Auswahlfeld fuer den Transport-Filter.
	 * @return Auswahlfeld.
	 */
	private SelectInput getFilter()
	{
		if (this.filter == null)
		{
      this.filter = new SelectInput(new String[] {"None","Base64"},this.passport.getFilterType());
      this.filter.setComment(i18n.tr("Bei PIN/TAN meist \"Base64\", sonst \"None\""));

      String clazz = this.passport.getClass().getName();
      if (clazz.toUpperCase().indexOf("PINTAN") != -1)
        this.filter.setPreselected("Base64");
		}
    return this.filter;
	}
	
	/**
	 * Entfernt das "https://" und die Port-Angabe aus der URL.
	 * @param url die zu bereinigende URL.
	 * @return die bereinigte URL.
	 */
	private String cleanUrl(String url)
	{
	  url = StringUtils.trimToEmpty(url); // BUGZILLA 381
	  if (url.length() == 0)
	    return url;
	  
    if (url.startsWith("https://"))
      url = url.replaceFirst("https://","");
    
    url = url.replaceFirst(":[0-9]{1,5}/","/"); // BUGZILLA 1159
    return url;
	}

}


/**********************************************************************
 * $Log: AccountContainerDialog.java,v $
 * Revision 1.16  2012/03/13 22:09:10  willuhn
 * @B BUGZILLA 1207
 *
 * Revision 1.15  2011/12/09 23:16:43  willuhn
 * @N BUGZILLA 1159
 *
 * Revision 1.14  2011-04-29 11:38:58  willuhn
 * @N Konfiguration der HBCI-Medien ueberarbeitet. Es gibt nun direkt in der Navi einen Punkt "Bank-Zugaenge", in der alle Medien angezeigt werden.
 *
 * Revision 1.13  2010-07-13 10:55:29  willuhn
 * @N Erster Code zum Aendern der Bank-Daten direkt auf der Karte. Muss dringend noch getestet werden - das will ich aber nicht mit meiner Karte machen, weil ich mir schonmal meine Karte mit Tests zerschossen hatte und die aber taeglich brauche ;)
 *
 * Revision 1.12  2009/09/28 13:02:05  willuhn
 * @N Pflichtfeld-Check in Account-Container-Dialog
 *
 * Revision 1.11  2009/03/02 13:43:19  willuhn
 * @B Trotz Auswahl von "None" als Filter wurde anschliessend wieder "Base64" angezeigt - lag lediglich daran, dass die Combobox mehrfach initialisiert wurde
 *
 * Revision 1.10  2007/04/10 13:25:14  willuhn
 * @B Bug 381
 *
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