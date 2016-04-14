/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci.controller;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Controller zum Erstellen einer neuen PIN/TAN-Config.
 */
public class HBCIVariantPinTanNewController extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private BLZInput blz = null;
  private URLInput url = null;
  private String foundBlz = null;
  
  /**
   * ct.
   * @param view
   */
  public HBCIVariantPinTanNewController(AbstractView view)
  {
    super(view);
  }
  
  /**
   * Liefert das Eingabefeld fuer die BLZ.
   * @return das Eingabefeld.
   */
  public BLZInput getBLZ()
  {
    if (this.blz != null)
      return this.blz;
    
    final Listener lBlz = new Listener()
    {
      /**
       * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
       */
      @Override
      public void handleEvent(Event arg0)
      {
        try
        {
          String b = StringUtils.trimToEmpty((String) arg0.data);
          if (b.length() == 0)
            return;
          
          Logger.info("auto detecting pin/tan url by blz " + b);
          String s = HBCIUtils.getPinTanURLForBLZ(b);
          getURL().setValue(s);
        }
        catch (Exception e)
        {
          Logger.error("error while auto detecting url/ip for blz",e);
        }
      }
    };

    final Listener lReset = new DelayedListener(800,new Listener()
    {
      /**
       * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
       */
      @Override
      public void handleEvent(Event arg0)
      {
        // Nichts zu resetten
        if (foundBlz == null)
          return;

        // Wenn der aktuell eingegebene Wert nicht mehr mit dem gespeicherten uebereinstimmt,
        // resetten wir die bisherigen Eingaben
        String s = (String) blz.getValue();
        if (!StringUtils.equals(s,foundBlz))
          getURL().reset();
      }
    });

    final Listener lType = new DelayedListener(900, new Listener()
    {
      /**
       * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
       */
      @Override
      public void handleEvent(Event arg0)
      {
        if (foundBlz != null)
          return;
        
        // Wenn wenigstens 8 Zeichen eingegeben wurden, aktivieren wir das URL-Feld
        // zur manuellen Eingabe. Dann wurde zwar eine BLZ eingegeben, wir kennen
        // sie aber nicht.
        String b = StringUtils.trimToEmpty((String) blz.getValue());
        if (b.indexOf(' ') != -1)
          b = b.replaceAll(" ","");

        if (b.length() >= HBCIProperties.HBCI_BLZ_LENGTH)
          getURL().setValue("");
      }
    });

    // FIXME: Ich muss das ueber ein SuggestInput mit der Liste aller passenden BLZ machen.
    // Hier kommen sonst die vielen Listener durcheinander.
    this.blz = new BLZInput(null)
    {
      /**
       * @see de.willuhn.jameica.hbci.gui.input.BLZInput#getControl()
       */
      @Override
      public Control getControl()
      {
        Control c = super.getControl();
        c.addListener(SWT.KeyUp,lType);
        c.addListener(SWT.KeyUp,lReset);
        return c;
      }
    };
    this.blz.setHint(i18n.tr("Bankleitzahl"));
    this.blz.setMandatory(true);
    this.blz.addBLZListener(lBlz);
    return this.blz;
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
      this.setEnabled(false);
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
     * Resettet die Eingaben.
     */
    public void reset()
    {
      Logger.info("reset URL input");
      this.setComment("");
      super.setValue("");
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
      this.setEnabled(!found);
      this.setMandatory(!found);
      if (found)
      {
        foundBlz = s;
        getBLZ().setComment(i18n.tr("Bankleitzahl eingegeben"));
        this.setComment(i18n.tr("Adresse des Bankservers gefunden"));
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Adresse des Bankservers gefunden"),StatusBarMessage.TYPE_SUCCESS));
      }
      else
      {
        foundBlz = null;
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte geben Sie die Adresse des Bankservers manuell ein"),StatusBarMessage.TYPE_INFO));
        this.setComment(i18n.tr("Bitte geben Sie die Adresse des Bankservers sein"));
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


