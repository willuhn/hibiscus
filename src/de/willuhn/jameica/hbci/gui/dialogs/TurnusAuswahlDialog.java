/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/TurnusAuswahlDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/11/13 17:02:04 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.menus.TurnusList;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Auswahl eines Zahlungs-Turnus.
 */
public class TurnusAuswahlDialog extends AbstractDialog
{

	private Turnus choosen = null;
	private I18N i18n;

  /**
   * ct.
   * @param position
   */
  public TurnusAuswahlDialog(int position)
  {
    super(position);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
		setTitle(i18n.tr("Auswahl des Zahlungsturnus"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
		DBIterator list = Settings.getDBService().createList(Turnus.class);

		TablePart table = new TablePart(list,new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	choosen = (Turnus) context;
      	close();
      }
    });

		table.addColumn(i18n.tr("Bezeichnung"),"bezeichnung");
		table.disableSummary();
		table.setContextMenu(new TurnusList());
		table.paint(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return choosen;
  }

}


/**********************************************************************
 * $Log: TurnusAuswahlDialog.java,v $
 * Revision 1.3  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.2  2004/10/25 23:12:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 **********************************************************************/