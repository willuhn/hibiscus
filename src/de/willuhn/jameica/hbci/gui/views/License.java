/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/License.java,v $
 * $Revision: 1.6 $
 * $Date: 2005/03/09 01:07:02 $
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
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.controller.LicenseControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * View fuer die Lizenz-Informationen
 */
public class License extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {

		I18N i18n = Application.getI18n();
		GUI.getView().setTitle(i18n.tr("Lizenzinformationen"));
    
		LicenseControl control = new LicenseControl(this);

		Part libs = control.getLibList();
		libs.paint(getParent());

		ButtonArea buttons = new ButtonArea(getParent(),1);
		buttons.addButton(i18n.tr("Zurück"),new Back());
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: License.java,v $
 * Revision 1.6  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.5  2004/10/20 12:34:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/10/08 13:37:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.1  2004/04/26 22:57:32  willuhn
 * @N License informations
 *
 * Revision 1.2  2004/04/26 22:42:17  willuhn
 * @N added InfoReader
 *
 * Revision 1.1  2004/04/14 23:53:44  willuhn
 * *** empty log message ***
 *
 **********************************************************************/