/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/AbstractExecuteDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/05/11 10:05:23 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakter Basis-Dialog fuer die Sicherheitsabfrage vor dem Ausfuehren von Auftraegen.
 */
public abstract class AbstractExecuteDialog extends AbstractDialog
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
	private Boolean choosen = Boolean.FALSE;

  /**
   * ct.
   * @param position
   */
  public AbstractExecuteDialog(int position)
  {
    super(position);
    this.setTitle(i18n.tr("Sicher?"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return choosen;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent);
    ButtonArea b = new ButtonArea();
    b.addButton(i18n.tr("Jetzt ausführen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        choosen = Boolean.TRUE;
        close();
      }
    },null,false,"ok.png");
    b.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        choosen = Boolean.FALSE;
        close();
      }
    },null,false,"process-stop.png");
    group.addButtonArea(b);
  }
}


/**********************************************************************
 * $Log: AbstractExecuteDialog.java,v $
 * Revision 1.1  2011/05/11 10:05:23  willuhn
 * @N Bestaetigungsdialoge ueberarbeitet (Buttons mit Icons, Verwendungszweck-Anzeige via VerwendungszweckUtil, keine Labelgroups mehr, gemeinsame Basis-Klasse)
 *
 **********************************************************************/