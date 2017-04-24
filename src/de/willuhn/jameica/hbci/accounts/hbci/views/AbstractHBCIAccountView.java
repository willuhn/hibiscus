/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci.views;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-View zum Erstellen neuer HBCI-Accounts.
 */
public abstract class AbstractHBCIAccountView extends AbstractView
{
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Liefert den passenden Controller.
   * @param type der gewuenschte Typ des Controllers.
   * @return der Controller.
   */
  protected <T> T getController(Class<? extends AbstractControl> type)
  {
    // Checken, ob die View selbst mit dem passenden Controller erzeugt wurde. Wenn nicht, dann erzuegen wir selbst eine Instanz.
    Object o = this.getCurrentObject();
    if (o != null && o.getClass().isAssignableFrom(type))
      return (T) o;
    
    // Ansonsten neu erstellen
    BeanService bs = Application.getBootLoader().getBootable(BeanService.class);
    return (T) bs.get(type);
  }
  
  /**
   * Liefert das Composite des Info-Panel kompatibel zu aelteren Jameica-Versionen.
   * @param panel das Panel.
   * @return das Composite.
   * @throws Exception
   */
  protected Composite getComposite(InfoPanel panel) throws Exception
  {
    Class c = panel.getClass();
    
    Composite comp = null;
    
    try
    {
      // Jameica-Versionen nach 14.04.2016
      Method m = c.getMethod("getComposite");
      if (m != null)
        comp = (Composite) m.invoke(panel);
    }
    catch (NoSuchMethodException e)
    {
      // Aeltere Jameica-Versionen
      Field f = c.getDeclaredField("comp");
      f.setAccessible(true);
      comp = (Composite) f.get(panel);
    }
    
    Composite wrap = new Composite(comp,SWT.NONE);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    wrap.setLayoutData(gd);
    
    GridLayout gl = new GridLayout(); 
    gl.marginHeight=0;
    gl.marginWidth=0;
    
    wrap.setLayout(gl);
    return wrap;
  }

}
