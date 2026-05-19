/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog mit dem Sicherheitshinweis beim Loeschen eines Datensatzes.
 */
public class DeleteDialog extends AbstractDialog<Boolean>
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private final static int WINDOW_WIDTH = 500;
  private final static int WINDOW_HEIGHT= 150;

  private String text    = null;
  private Button apply   = null;
  private Boolean choice = null;

  /**
   * ct.
   * @param text der anzuzeigende Text.
   */
  public DeleteDialog(String text)
  {
    super(KontoDeleteDialog.POSITION_CENTER);
    this.setText(text);
    this.setTitle(i18n.tr("Daten löschen"));
    this.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
    this.setSideImage(SWTUtil.getImage("dialog-warning-large.png"));
  }
  
  /**
   * Speichert den anzuzeigenden Text. 
   * @param text der anzuzeigende Text.
   */
  public void setText(String text)
  {
    this.text = StringUtils.trimToNull(text);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    final Container c = new SimpleContainer(parent,true);
    c.addText(this.text != null ? this.text : i18n.tr("Wollen Sie diese Daten wirklich löschen?"),true);

    final Container c2 = new SimpleContainer(parent);
    c2.addText(i18n.tr("Der Vorgang kann nicht rückgängig gemacht werden."),true,Color.LINK);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(getApply());
    buttons.addButton(new Cancel());
    c2.addButtonArea(buttons);
    
    this.getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,WINDOW_HEIGHT));
  }
  
  /**
   * Liefert den Apply-Button.
   * @return der Apply-Button.
   */
  private Button getApply()
  {
    if (this.apply != null)
      return this.apply;
    
    this.apply = new Button(i18n.tr("Jetzt löschen"),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        choice = Boolean.TRUE;
        close();
      }
    },null,false,"user-trash-full.png");
    
    return this.apply;
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected Boolean getData() throws Exception
  {
    return choice;
  }
}
