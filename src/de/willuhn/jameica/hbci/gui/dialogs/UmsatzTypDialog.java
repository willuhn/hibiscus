/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/UmsatzTypDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/12/05 20:16:15 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.parts.UmsatzTypList;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, ueber den die Umsatz-Typen bearbeitet werden koennen.
 */
public class UmsatzTypDialog extends AbstractDialog
{

	private I18N i18n;

  /**
   * ct.
   * @param position
   */
  public UmsatzTypDialog(int position)
  {
    super(position);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		this.setTitle(i18n.tr("Umsatz-Filter"));
    this.setSize(SWT.DEFAULT,300);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
		final UmsatzTypList list = new UmsatzTypList(new Action() {
      public void handleAction(Object context) throws ApplicationException {}
    });
    
    
    list.setMulti(false);
    list.setSummary(false);
    list.paint(parent);

		ButtonArea b = new ButtonArea(parent,1);
		b.addButton(i18n.tr("Schliessen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
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
 * $Log: UmsatzTypDialog.java,v $
 * Revision 1.1  2005/12/05 20:16:15  willuhn
 * @N Umsatz-Filter Refactoring
 *
 **********************************************************************/