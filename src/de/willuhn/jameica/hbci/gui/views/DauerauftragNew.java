/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/DauerauftragNew.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/08/01 23:27:42 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.DauerauftragDelete;
import de.willuhn.jameica.hbci.gui.action.DauerauftragExecute;
import de.willuhn.jameica.hbci.gui.controller.DauerauftragControl;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 */
public class DauerauftragNew extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		final DauerauftragControl control = new DauerauftragControl(this);

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Dauerauftrag bearbeiten"));
		
		LabelGroup konten = new LabelGroup(getParent(),i18n.tr("Konten"));
		
		konten.addLabelPair(i18n.tr("persönliches Konto"),				control.getKontoAuswahl());		
		konten.addLabelPair(i18n.tr("Konto des Empfängers"),			control.getEmpfaengerKonto());		
		konten.addLabelPair(i18n.tr("BLZ des Empfängers"),				control.getEmpfaengerBlz());		
		konten.addLabelPair(i18n.tr("Name des Empfängers"),			control.getEmpfaengerName());
		konten.addCheckbox(control.getStoreEmpfaenger(),i18n.tr("Empfängerdaten im Adressbuch speichern"));

		LabelGroup details = new LabelGroup(getParent(),i18n.tr("Details"));

		details.addLabelPair(i18n.tr("Verwendungszweck"),					control.getZweck());
		details.addLabelPair(i18n.tr("weiterer Verwendungszweck"),control.getZweck2());
		details.addLabelPair(i18n.tr("Betrag"),										control.getBetrag());
		details.addLabelPair(i18n.tr("Zahlungsturnus"),						control.getTurnus());
		details.addLabelPair(i18n.tr("Erste Zahlung"),						control.getErsteZahlung());
		details.addLabelPair(i18n.tr("Letzte Zahlung"),						control.getLetzteZahlung());

		details.addSeparator();

		details.addLabelPair(i18n.tr("Auftragsnummer"),						control.getOrderID());

		final Dauerauftrag da = (Dauerauftrag) control.getTransfer();
		ButtonArea buttonArea = new ButtonArea(getParent(),4);
		String s = i18n.tr("Speichern und ausführen");
		if (da.isActive())
			s = "Speichern und aktualisieren";

		buttonArea.addButton(i18n.tr("Zurück"), 	 		 new Back());
		buttonArea.addButton(i18n.tr("Löschen"),	 		 new DauerauftragDelete(), da);
		buttonArea.addButton(s,										 		 new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	if (control.handleStore())
					new DauerauftragExecute().handleAction(da);
      }
    });
		buttonArea.addButton(i18n.tr("Speichern"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				control.handleStore();
			}
    },null,true);
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
  }

}


/**********************************************************************
 * $Log: DauerauftragNew.java,v $
 * Revision 1.4  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.2  2005/03/04 00:52:03  web0
 * @B typo
 *
 * Revision 1.1  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 * Revision 1.6  2004/10/29 16:16:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/10/25 17:58:56  willuhn
 * @N Haufen Dauerauftrags-Code
 *
 * Revision 1.4  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/23 17:34:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/07/13 23:08:37  willuhn
 * @N Views fuer Dauerauftrag
 *
 **********************************************************************/