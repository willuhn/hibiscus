/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/Welcome.java,v $
 * $Revision: 1.9 $
 * $Date: 2004/05/25 23:23:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.WelcomeControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * 
 */
public class Welcome extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		I18N i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
		WelcomeControl control = new WelcomeControl(this);

		GUI.getView().setTitle(i18n.tr("Hibiscus - HBCI-Onlinebanking"));

		new Headline(getParent(),i18n.tr("Offene Überweisungen"));
		control.getOffeneUeberweisungen().paint(getParent());

		control.getWelcomeText().paint(getParent());
	
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
  }

}


/**********************************************************************
 * $Log: Welcome.java,v $
 * Revision 1.9  2004/05/25 23:23:18  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.8  2004/05/23 15:33:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.6  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.3  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.2  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/09 13:06:03  willuhn
 * @C misc
 *
 **********************************************************************/