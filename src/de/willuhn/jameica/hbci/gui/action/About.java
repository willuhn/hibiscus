/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/About.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/12 23:48:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.ViewDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Action fuer About-Dialog.
 */
public class About implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
  	ViewDialog d = new ViewDialog(new de.willuhn.jameica.hbci.gui.views.About(),ViewDialog.POSITION_CENTER);
  	try
  	{
			d.open();
  	}
  	catch (Exception e)
  	{
  		Logger.error("error while opening about dialog",e);
  		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  		throw new ApplicationException(i18n.tr("Fehler beim Anzeigen des About-Dialogs"),e);
  	}
  }

}


/**********************************************************************
 * $Log: About.java,v $
 * Revision 1.1  2004/10/12 23:48:39  willuhn
 * @N Actions
 *
 **********************************************************************/