/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/KontoauszugList.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/05/14 19:53:09 $
 * $Author: jost $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.KontoauszugControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt Kontoauszüge an und gibt gibt sie in eine PDF-Datei aus.
 */
public class KontoauszugList extends AbstractView
{
	private I18N i18n = null;

	private KontoauszugControl control = null;

	public KontoauszugList()
	{
		super();
		this.i18n = Application.getPluginLoader().getPlugin(HBCI.class)
				.getResources().getI18N();

	}

	/**
	 * @see de.willuhn.jameica.gui.AbstractView#bind()
	 */
	public void bind() throws Exception
	{
		GUI.getView().setTitle(i18n.tr("Kontoauszug"));

		control = new KontoauszugControl(this);

		LabelGroup settings = new LabelGroup(getParent(), i18n.tr("Filter"));

		settings.addLabelPair(i18n.tr("Konto"), control.getKontoAuswahl());
		settings.addLabelPair(i18n.tr("Start-Datum"), control.getStart());
		settings.addLabelPair(i18n.tr("Ende-Datum"), control.getEnd());

		ButtonArea buttons = new ButtonArea(getParent(), 1);
		buttons.addButton(i18n.tr("Ausgabe"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				control.startReport();
			}
		});

	}

}

/*******************************************************************************
 * $Log: KontoauszugList.java,v $
 * Revision 1.1  2006/05/14 19:53:09  jost
 * Prerelease Kontoauszug-Report
 * Revision 1.4 2006/01/18 00:51:00
 ******************************************************************************/
