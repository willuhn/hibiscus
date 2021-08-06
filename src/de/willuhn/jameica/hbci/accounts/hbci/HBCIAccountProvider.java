/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci;

import java.util.LinkedList;
import java.util.List;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.accounts.AccountProvider;
import de.willuhn.jameica.hbci.accounts.hbci.action.HBCIAccountNew;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung des Account-Providers fuer HBCI-Konten.
 */
@Lifecycle(Type.CONTEXT)
public class HBCIAccountProvider implements AccountProvider
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Das Primaer-Verfahren. Der steht immer oben.
   */
  private final static Class<? extends HBCIVariant> PRIMARY = HBCIVariantPinTan.class;

  private List<HBCIVariant> variants = null;

  /**
   * @see de.willuhn.jameica.hbci.accounts.AccountProvider#getName()
   */
  @Override
  public String getName()
  {
    return i18n.tr("FinTS/HBCI-Bankzugang");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.accounts.AccountProvider#getInfo()
   */
  @Override
  public InfoPanel getInfo()
  {
    InfoPanel info = new InfoPanel();
    info.setTitle(this.getName());
    info.setText(i18n.tr("Verwenden Sie diese Option für die Anlage von Bankzugängen mit PIN/TAN-Verfahren, Schlüsseldatei oder Chipkarte."));
    info.setComment(i18n.tr("HBCI/FinTS ist der in Hibiscus hauptsächlich verwendete Bankzugang."));
    info.setUrl("http://www.willuhn.de/wiki/doku.php?id=support:list:banken");
    info.setIcon("hibiscus-large.png");
    return info;
  }
  
  /**
   * Liefert eine Liste der unterstuetzten HBCI-Verfahren.
   * @return Liste der HBCI-Verfahren.
   */
  public synchronized List<HBCIVariant> getVariants()
  {
    if (this.variants != null)
      return this.variants;
    
    this.variants = new LinkedList<HBCIVariant>();
    
    try
    {
      Logger.info("loading hbci variants");
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      Class[] found = Application.getPluginLoader().getPlugin(HBCI.class).getManifest().getClassLoader().getClassFinder().findImplementors(HBCIVariant.class);
      for (Class<HBCIVariant> c:found)
      {
        try
        {
          Logger.debug("  " + c.getName());
          this.variants.add(service.get(c));
        }
        catch (Exception e)
        {
          Logger.error("unable to load hbci varian " + c.getName() + ", skipping",e);
        }
      }

      this.variants.sort((v1, v2) -> {
        if (PRIMARY.isInstance(v1))
          return -1;
        if (PRIMARY.isInstance(v2))
          return 1;
        return v1.getName().compareTo(v2.getName());
      });
    }
    catch (ClassNotFoundException e)
    {
      Logger.warn("no hbci variants found");
    }
    catch (Exception e)
    {
      Logger.error("error while searching for hbci variants",e);
    }
    return this.variants;
  }

  
  /**
   * @see de.willuhn.jameica.hbci.accounts.AccountProvider#create()
   */
  @Override
  public void create() throws ApplicationException
  {
    new HBCIAccountNew().handleAction(null);
  }
}
