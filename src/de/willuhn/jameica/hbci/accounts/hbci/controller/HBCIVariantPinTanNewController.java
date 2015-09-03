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
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
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
  private TextInput url = null;
  
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
    
    this.blz = new BLZInput(null);
    this.blz.setMandatory(true);
    this.blz.addBLZListener(new Listener()
    {
      public void handleEvent(Event arg0)
      {
        try
        {
          String b = (String) arg0.data;
          Logger.info("auto detecting pin/tan url by blz " + b);
          String s = cleanUrl(HBCIUtils.getPinTanURLForBLZ(b));
          getURL().setValue(s);
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
   * Liefert die URL des HBCI-Servers.
   * @return die URL des HBCI-Servers.
   */
  public TextInput getURL()
  {
    if (this.url != null)
      return this.url;
    
    this.url = new TextInput(null)
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
    this.url.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        // Triggert den Code zum Entfernen des "https://" und der Leerzeichen
        getURL().setValue(getURL().getValue());
      }
    
    });

    this.url.setMandatory(true);
    this.url.setName(i18n.tr("Hostname/URL des Bankservers"));
    this.url.setComment(i18n.tr("Bitte ohne \"https://\" eingeben"));
    return this.url;
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


