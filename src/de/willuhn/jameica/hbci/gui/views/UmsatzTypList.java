/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/UmsatzTypList.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/11/23 17:25:38 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypNew;
import de.willuhn.jameica.hbci.gui.controller.UmsatzTypControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Listet alle Umsatz-Kategorien auf.
 */
public class UmsatzTypList extends AbstractView
{
  
  /**
   * ct.
   */
  public UmsatzTypList()
  {
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


    UmsatzTypControl control = new UmsatzTypControl(this);

		GUI.getView().setTitle(i18n.tr("Umsatz-Kategorien"));
		

		try {
			
			Part list = control.getUmsatzTypListe();
			list.paint(getParent());
			
			ButtonArea buttons = new ButtonArea(getParent(),2);
			buttons.addButton(i18n.tr("Zurück"),new Back(),null,true);
      buttons.addButton(i18n.tr("Neue Umsatz-Kategorie"),new UmsatzTypNew());
		}
		catch (RemoteException e)
		{
			Logger.error("error while loading umsatztype list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Laden der Umsatz-Kategorien"));
		}

  }
}


/**********************************************************************
 * $Log: UmsatzTypList.java,v $
 * Revision 1.1  2006/11/23 17:25:38  willuhn
 * @N Umsatz-Kategorien - in PROGRESS!
 *
 **********************************************************************/