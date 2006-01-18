/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/ProtokollList.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/01/18 00:51:00 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.controller.KontoControl;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste der Protokoll-Eintraege eines Kontos an.
 */
public class ProtokollList extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    KontoControl control = new KontoControl(this);

    Konto k = control.getKonto();
    if (k != null)
    {
      String s1 = k.getBezeichnung();
      if (s1 == null) s1 = "";

      String s2 = k.getKontonummer();
      GUI.getView().setTitle(i18n.tr("Protokoll des Kontos: {0} [Ktr.-Nr.: {1}]",new String[]{s1,s2}));
    }
    else
      GUI.getView().setTitle(i18n.tr("Protokoll des Kontos"));
		
		
		try {

			control.getProtokoll().paint(getParent());

			ButtonArea buttons = new ButtonArea(getParent(),1);
			buttons.addButton(i18n.tr("Zurück"),new Back(),null,true);

		}
		catch (Exception e)
		{
			Logger.error("error while loading protocol",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen des Konto-Protokolls."));
		}
  }
}


/**********************************************************************
 * $Log: ProtokollList.java,v $
 * Revision 1.2  2006/01/18 00:51:00  willuhn
 * @B bug 65
 *
 * Revision 1.1  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 **********************************************************************/