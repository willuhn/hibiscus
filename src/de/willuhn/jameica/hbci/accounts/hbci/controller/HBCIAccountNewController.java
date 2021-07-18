/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci.controller;

import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.accounts.hbci.HBCIAccountProvider;
import de.willuhn.jameica.hbci.accounts.hbci.HBCIVariant;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller zum Anlegen eines neuen HBCI-Accounts.
 */
public class HBCIAccountNewController extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private List<InfoPanel> variants;

  /**
   * ct.
   * @param view Verfahren
   */
  public HBCIAccountNewController(AbstractView view)
  {
    super(view);
  }
  
  /**
   * Liefert die Liste der verfuegbaren HBCI-Varianten.
   * @return die Liste der verfuegbaren HBCI-Varianten.
   */
  public synchronized List<InfoPanel> getVariants()
  {
    if (this.variants != null)
      return this.variants;
    
    this.variants = new ArrayList<InfoPanel>();
    
    BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    final HBCIAccountProvider hbci = bs.get(HBCIAccountProvider.class);
    
    for (final HBCIVariant v:hbci.getVariants())
    {
      InfoPanel p = v.getInfo();
      final Button button = new Button(i18n.tr("Verfahren auswählen..."), new Action()
      {
        @Override
        public void handleAction(Object context) throws ApplicationException
        {
          Logger.info("creating new account. type: " + hbci.getName() + ", variant: " + v.getName());
          v.create();
        }
      },null,false,"go-next.png");
      p.addButton(button);
      this.variants.add(p);
    }
    
    return this.variants;
  }
  
}


