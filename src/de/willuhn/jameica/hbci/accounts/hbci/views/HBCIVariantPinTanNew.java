/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.accounts.hbci.views;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.Resource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.accounts.hbci.HBCIVariantPinTan;
import de.willuhn.jameica.hbci.accounts.hbci.controller.HBCIVariantPinTanNewController;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * View zum Erstellen einer neuen PIN/TAN-Config.
 */
public class HBCIVariantPinTanNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  @Resource private HBCIVariantPinTan variant;

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Neuer FinTS/HBCI-Bankzugang PIN/TAN..."));

    final HBCIVariantPinTanNewController control = new HBCIVariantPinTanNewController(this);

    Container c = new SimpleContainer(this.getParent());
    c.addHeadline(i18n.tr("Schritt 3: Auswahl der Bank"));
    c.addText(i18n.tr("Bitte wählen Sie die Bank aus, zu der Sie einen Zugang einrichten möchten."),true);
    
    InfoPanel panel = this.variant.getInfo();
    c.addPart(panel);
    
    
    Composite comp = this.getComposite(panel);
    Container cs = new SimpleContainer(comp);
    cs.addText("\n" + i18n.tr("Bitte geben Sie die BLZ, BIC oder den Namen Ihrer Bank ein.\nHibiscus wird anschließend versuchen, die Adresse des Bankservers zu ermitteln."),true);
    cs.addPart(control.getBank());
    cs.addPart(control.getURL());
//    c.addInput(control.getHBCIVersion());
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(control.getNextButton());
    buttons.paint(comp);
  }
  
  /**
   * Liefert das Composite des Info-Panel kompatibel zu aelteren Jameica-Versionen.
   * @param panel das Panel.
   * @return das Composite.
   * @throws Exception
   */
  private Composite getComposite(InfoPanel panel) throws Exception
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


