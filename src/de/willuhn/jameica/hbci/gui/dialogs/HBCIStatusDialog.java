/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/HBCIStatusDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/24 17:19:03 $
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
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 */
public class HBCIStatusDialog extends AbstractDialog
{
	private I18N i18n;
	private Runnable job;

  /**
   * @param position
   */
  public HBCIStatusDialog(Runnable r,int position)
  {
    super(position);
    this.setSize(500,250);
    job = r;
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();    
    setTitle(i18n.tr("Status der Auftragsbearbeitung"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(final Composite parent) throws Exception
  {
		Settings.getHBCIProgressBar().paint(parent);

		ButtonArea b = new ButtonArea(parent,1);
		b.addButton(i18n.tr("Starten"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	GUI.getDisplay().asyncExec(job);
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

}


/**********************************************************************
 * $Log: HBCIStatusDialog.java,v $
 * Revision 1.1  2004/10/24 17:19:03  willuhn
 * *** empty log message ***
 *
 **********************************************************************/