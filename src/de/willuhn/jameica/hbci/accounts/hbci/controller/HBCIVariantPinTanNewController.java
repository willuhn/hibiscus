/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci.controller;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.BankInfo;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.input.BankInfoInput;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Controller zum Erstellen einer neuen PIN/TAN-Config.
 */
public class HBCIVariantPinTanNewController extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private BankInfoInput bank = null;
  private URLInput url = null;
  private Button next = null;
  
  /**
   * ct.
   * @param view
   */
  public HBCIVariantPinTanNewController(AbstractView view)
  {
    super(view);
  }
  
  /**
   * Liefert das Suchfeld fuer die Bank.
   * @return das Eingabefeld.
   */
  public BankInfoInput getBank()
  {
    if (this.bank != null)
      return this.bank;
    
    this.bank = new BankInfoInput();
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
        }
        else
        {
          bank.setComment("");
          return;
        }
        
        String url = info.getPinTanAddress();
        getURL().setValue(url);
        getNextButton().setEnabled(url != null && url.length() > 0);
      }
    });
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
    
    this.url = new URLInput();
    return this.url;
  }
  
  /**
   * Liefert den Weiter-Button.
   * @return der Weiter-Button.
   */
  public Button getNextButton()
  {
    if (this.next != null)
      return this.next;
    
    this.next = new Button(i18n.tr("Übernehmen"),null,null,true,"go-next.png");
    this.next.setEnabled(false);
    return this.next;
  }
  
  /**
   * Eingabefeld fuer die URL.
   */
  private class URLInput extends TextInput
  {
    /**
     * ct.
     */
    public URLInput()
    {
      super("");
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
      
      boolean found = StringUtils.trimToNull(s) != null;
      this.setMandatory(!found);
      if (found)
      {
        this.setComment(i18n.tr("Adresse des Bankservers gefunden"));
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Adresse des Bankservers gefunden"),StatusBarMessage.TYPE_SUCCESS));
      }
      else
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte geben Sie die Adresse des Bankservers manuell ein"),StatusBarMessage.TYPE_INFO));
        this.setComment(i18n.tr("Bitte geben Sie die Adresse des Bankservers ein"));
      }
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
}


