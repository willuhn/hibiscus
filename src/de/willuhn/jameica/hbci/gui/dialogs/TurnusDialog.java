/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/TurnusDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/03/06 16:06:10 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.TurnusControl;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Turnus bearbeiten.
 */
public class TurnusDialog extends AbstractDialog {

	private I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
	private TurnusControl control = new TurnusControl();

  /**
   * @param position
   */
  public TurnusDialog(int position)
  {
    super(position);
		this.setTitle(i18n.tr("Zahlungsturnus auswählen/bearbeiten"));
  }

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
	 */
	protected void paint(Composite parent) throws Exception
	{

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		LabelGroup group = new LabelGroup(parent,i18n.tr("Vorhandene Einträge"));
		group.addPart(control.getTurnusList());


		LabelGroup group2 = new LabelGroup(parent,i18n.tr("Eigenschaften"));

		try {
			group2.addLabelPair(i18n.tr("Zeiteinheit"),control.getZeiteinheit());
			group2.addLabelPair(i18n.tr("Zahlung aller"),control.getIntervall());
			group2.addLabelPair(i18n.tr("Zahlung am"), control.getTagWoechentlich());
			group2.addLabelPair("", control.getTagMonatlich());
			
			group2.addSeparator();
			group2.addLabelPair("", control.getComment());
			
		}
		catch (RemoteException e)
		{
			Logger.error("error while reading turnus",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen des Zahlungsturnus."));
		}

		// und noch die Abschicken-Knoepfe
		if (!control.getTurnus().isInitial())
		{
			ButtonArea buttonArea = new ButtonArea(parent,3);
			buttonArea.addButton(i18n.tr("Neu"), new Action()
			{
				public void handleAction(Object context) throws ApplicationException
				{
					control.handleCreate();
				}
			},null,true);
			buttonArea.addButton(i18n.tr("Übernehmen"), new Action()
			{
				public void handleAction(Object context) throws ApplicationException
				{
					control.handleStore();
					close();
				}
			},null,true);
			buttonArea.addButton(i18n.tr("Abbrechen"), new Action()
			{
				public void handleAction(Object context) throws ApplicationException
				{
					close();
				}
			});
		}


  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
  	Turnus t = control.getTurnus();
  	return (t == null || t.isNewObject()) ? null : t;
  }

}


/**********************************************************************
 * $Log: TurnusDialog.java,v $
 * Revision 1.3  2005/03/06 16:06:10  web0
 * *** empty log message ***
 *
 * Revision 1.2  2004/11/26 01:23:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/26 00:04:08  willuhn
 * @N TurnusDetail
 *
 * Revision 1.3  2004/11/18 23:46:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/11/15 00:38:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 **********************************************************************/