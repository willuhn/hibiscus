/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/OffenerPostenNew.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/25 00:42:04 $
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
import de.willuhn.jameica.hbci.gui.action.OffenerPostenDelete;
import de.willuhn.jameica.hbci.gui.controller.OffenerPostenControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Offenen Posten bearbeiten.
 */
public class OffenerPostenNew extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		GUI.getView().setTitle(i18n.tr("Offenen Posten bearbeiten"));
		
		final OffenerPostenControl control = new OffenerPostenControl(this);
		LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));

// TODO Hier weiter
//		try {
//			group.addLabelPair(i18n.tr("Bezeichnung"),			    		control.getBezeichnung());
//
//      new Headline(getParent(),i18n.tr("Weitere Filter-Kriterien"));
//      control.getFilterListe().paint(getParent());
//		}
//		catch (RemoteException e)
//		{
//			Logger.error("error while reading OP entry",e);
//			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen des offenen Posten."));
//		}

		// und noch die Abschicken-Knoepfe
		ButtonArea buttonArea = new ButtonArea(getParent(),3);
		buttonArea.addButton(i18n.tr("Zurück"),new Back());
		buttonArea.addButton(i18n.tr("Löschen"), new OffenerPostenDelete(), control.getOffenerPosten());
		buttonArea.addButton(i18n.tr("Speichern"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        // TODO Action
      	// control.handleStore();
      }
    },null,true);

  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: OffenerPostenNew.java,v $
 * Revision 1.1  2005/05/25 00:42:04  web0
 * @N Dialoge fuer OP-Verwaltung
 *
 **********************************************************************/