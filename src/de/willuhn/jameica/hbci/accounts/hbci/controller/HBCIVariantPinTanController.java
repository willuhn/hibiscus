/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci.controller;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.BankInfo;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.accounts.hbci.HBCIAccountPinTan;
import de.willuhn.jameica.hbci.accounts.hbci.action.HBCIVariantPinTanTest;
import de.willuhn.jameica.hbci.accounts.hbci.views.HBCIVariantPinTanStep2;
import de.willuhn.jameica.hbci.gui.input.BankInfoInput;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller zum Erstellen einer neuen PIN/TAN-Config.
 */
@Lifecycle(Type.REQUEST)
public class HBCIVariantPinTanController extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private HBCIAccountPinTan account = new HBCIAccountPinTan();
  private BankInfoInput bank = null;
  private URLInput url = null;
  private TextInput customer = null;
  private TextInput username = null;
  
  private Button step1 = null;
  private Button step2 = null;
  
  private Listener step1Listener = new Step1Listener();
  private Listener step2Listener = new Step2Listener();
  
  /**
   * ct.
   */
  public HBCIVariantPinTanController()
  {
    super(null);
  }
  
  /**
   * Liefert das Suchfeld fuer die Bank.
   * @return das Eingabefeld.
   */
  public BankInfoInput getBank()
  {
    if (this.bank != null)
      return this.bank;
    
    this.bank = new BankInfoInput(this.account.getBlz());
    if (this.bank.getValue() == null) // Falls es keine bekannte BLZ war
      this.bank.setValue(this.account.getBlz());
    this.bank.addListener(new Listener() {
      
      @Override
      public void handleEvent(Event event)
      {
        BankInfo info = (BankInfo) bank.getValue();
        if (info != null)
        {
          String bic = info.getBic();
          if (bic != null && bic.length() > 0)
            bank.setComment(i18n.tr("Bank gefunden, BIC: {0}",info.getBic()));
          else
            bank.setComment(i18n.tr("Bank gefunden"));

          account.setBlz(info.getBlz());
        }
        else
        {
          bank.setComment("");
          return;
        }
        
        String url = info.getPinTanAddress();
        if (url != null && url.length() > 0)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Adresse des Bankservers gefunden"),StatusBarMessage.TYPE_SUCCESS));
          account.setUrl(url);
          getURL().setValue(url);
        }
        else
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte geben Sie die Adresse des Bankservers manuell ein"),StatusBarMessage.TYPE_INFO));
        }
      }
    });
    this.bank.addListener(this.step1Listener);
    this.bank.setComment("");
    this.bank.setMandatory(true);
    return this.bank;
  }
  
  /**
   * Liefert die URL des HBCI-Servers.
   * @return die URL des HBCI-Servers.
   */
  public URLInput getURL()
  {
    if (this.url != null)
      return this.url;
    
    this.url = new URLInput(this.account.getUrl());
    this.url.addListener(this.step1Listener);
    this.url.setMandatory(true);
    return this.url;
  }
  
  /**
   * Liefert den Listener zur Freigabe des ersten Weiter-Buttons.
   * @return der Listener zur Freigabe des ersten Weiter-Buttons.
   */
  public Listener getStep1Listener()
  {
    return this.step1Listener;
  }
  
  /**
   * Liefert den Listener zur Freigabe des zweiten Weiter-Buttons.
   * @return der Listener zur Freigabe des zweiten Weiter-Buttons.
   */
  public Listener getStep2Listener()
  {
    return this.step2Listener;
  }
  
  /**
   * Liefert das Eingabefeld fuer die Kundenkennung.
   * @return das Eingabefeld fuer die Kundenkennung.
   */
  public TextInput getCustomer()
  {
    if (this.customer != null)
      return this.customer;
    
    this.customer = new TextInput(null,50);
    this.customer.setHint(i18n.tr("Kundenkennung (oft identisch mit Benutzerkennung)"));
    this.customer.setComment("");
    this.customer.addListener(this.step2Listener);
    return this.customer;
  }
  
  /**
   * Liefert das Eingabefeld fuer die Benutzerkennung.
   * @return das Eingabefeld fuer die Benutzerkennung.
   */
  public TextInput getUsername()
  {
    if (this.username != null)
      return this.username;
    
    this.username = new TextInput(null,50);
    this.username.setHint(i18n.tr("Benutzerkennung"));
    this.username.setComment(this.getBankText());
    this.username.setMandatory(true);
    this.username.addListener(this.step2Listener);
    this.username.focus();
    return this.username;
  }
  
  /**
   * Liefert den Weiter-Button.
   * @return der Weiter-Button.
   */
  public Button getStep1Button()
  {
    if (this.step1 != null)
      return this.step1;
    
    this.step1 = new Button(i18n.tr("Übernehmen"),new Action() {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        bank = null;
        url = null;
        username = null;
        customer = null;
        GUI.startView(HBCIVariantPinTanStep2.class,HBCIVariantPinTanController.this);
      }
    },null,true,"go-next.png");
    this.step1.setEnabled(false);
    return this.step1;
  }
  
  /**
   * Liefert den Weiter-Button.
   * @return der Weiter-Button.
   */
  public Button getStep2Button()
  {
    if (this.step2 != null)
      return this.step2;
    
    this.step2 = new Button(i18n.tr("Bankzugang jetzt testen..."),new HBCIVariantPinTanTest(),this.account,true,"mail-send-receive.png");
    this.step2.setEnabled(false);
    return this.step2;
  }
  
  /**
   * Liefert einen sprechenden Text fuer die ausgewaehlte Bank insofern verfuegbar.
   * @return sprechender Text fuer die ausgewaehlte Bank insofern verfuegbar.
   */
  public String getBankText()
  {
    String blz = this.account.getBlz();
    if (blz == null || blz.trim().length() == 0)
      return null;

    List<BankInfo> result = HBCIUtils.searchBankInfo(blz);
    if (result == null || result.size() == 0)
      return null;
    return result.get(0).getName();
  }

  /**
   * Eingabefeld fuer die URL.
   */
  private class URLInput extends TextInput
  {
    /**
     * ct.
     * @param url die vordefinierte URL.
     */
    public URLInput(String url)
    {
      super(url);
      this.setHint(i18n.tr("Adresse des Bankservers"));
      this.setComment("");
      
      this.addListener(new Listener()
      {
        /**
         * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
         */
        @Override
        public void handleEvent(Event event)
        {
          // Triggert den Code zum Entfernen des "https://" und der Leerzeichen
          getURL().setValue(getURL().getValue());
        }
      });
    }
    
    /**
     * @see de.willuhn.jameica.gui.input.TextInput#getValue()
     */
    @Override
    public Object getValue()
    {
      // Ueberschrieben, um ggf. das https:// am Anfang abzuschneiden
      String s = (String) super.getValue();
      if (s == null || s.length() == 0)
        return null;
      return this.cleanUrl(s);
    }
    
    /**
     * @see de.willuhn.jameica.gui.input.TextInput#setValue(java.lang.Object)
     */
    @Override
    public void setValue(Object value)
    {
      String s = this.cleanUrl((String) value);
      super.setValue(s);
    }

    /**
     * Entfernt das "https://" und die Port-Angabe aus der URL.
     * @param url die zu bereinigende URL.
     * @return die bereinigte URL.
     */
    private String cleanUrl(String url)
    {
      url = StringUtils.trimToEmpty(url);
      if (url.length() == 0)
        return url;
      
      if (url.startsWith("https://"))
        url = url.replaceFirst("https://","");
      
      url = url.replaceFirst(":[0-9]{1,5}/","/"); // BUGZILLA 1159
      return url;
    }
  }

  /**
   * Listener zum Freischalten des ersten Weiter-Buttons.
   */
  private class Step1Listener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event)
    {
      String url = (String) getURL().getValue();
      boolean enabled = url != null && url.trim().length() > 0;
      getStep1Button().setEnabled(enabled);
      
      if (enabled)
      {
        account.setUrl(url);

        // Fallback, falls die Bank nicht bekannt ist
        if (account.getBlz() == null)
          account.setBlz(getBank().getText());
      }
    }
  }

  /**
   * Listener zum Freischalten des zweiten Weiter-Buttons.
   */
  private class Step2Listener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event)
    {
      String s = (String) getUsername().getValue();
      boolean enabled = s != null && s.trim().length() > 0;
      getStep2Button().setEnabled(enabled);
      
      if (enabled)
      {
        account.setUsername(s);
        String customer = (String) getCustomer().getValue();
        account.setCustomer(customer != null && customer.trim().length() > 0 ? customer : s);
      }
    }
  }
}
