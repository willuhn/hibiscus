/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/Welcome.java,v $
 * $Revision: 1.22 $
 * $Date: 2005/06/21 20:25:10 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.SWTError;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.WelcomeControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * View des Start-Bildschirms.
 */
public class Welcome extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
		WelcomeControl control = new WelcomeControl(this);

		GUI.getView().setTitle(i18n.tr("Hibiscus - HBCI-Onlinebanking"));

    LabelGroup group = new LabelGroup(getParent(),i18n.tr("Offene Überweisungen"));
    group.addPart(control.getOffeneUeberweisungen());

    try
    {
      control.getQuickLinks().paint(getParent());
    }
    catch (SWTError e)
    {
      Logger.error("unable to paint quicklinks. this seems to be a known bug in the sax parser",e);
    }
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
  }

}


/**********************************************************************
 * $Log: Welcome.java,v $
 * Revision 1.22  2005/06/21 20:25:10  web0
 * *** empty log message ***
 *
 * Revision 1.21  2005/06/17 16:11:33  web0
 * *** empty log message ***
 *
 * Revision 1.20  2005/06/13 11:24:21  web0
 * *** empty log message ***
 *
 * Revision 1.18  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.17  2005/03/31 23:05:46  web0
 * @N geaenderte Startseite
 * @N klickbare Links
 *
 * Revision 1.16  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.15  2005/01/14 00:48:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/09/13 20:54:38  willuhn
 * @N bg color
 *
 * Revision 1.12  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.11  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/07/20 23:31:49  willuhn
 * *** empty log message ***
 *
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