/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/About.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/05/18 22:45:24 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.AbstractPlugin;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * About-Dialog.
 */
public class About extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {
  	I18N i18n = Application.getI18n();

		Label l = new Label(getParent(),SWT.BORDER);
		l.setImage(new Image(GUI.getDisplay(),getClass().getClassLoader().getResourceAsStream("img/hibiscus.jpg")));

		FormTextPart text = new FormTextPart();
		text.setText("<form>" +
			"<p><b>Hibiscus - HBCI-Onlinebanking für Jameica</b></p>" +			"<br/>Licence: GPL (http://www.gnu.org/copyleft/gpl.html)" +
			"<br/><p>Copyright by Olaf Willuhn [hbci@willuhn.de]</p>" +			"<p>http://www.willuhn.de/projects/hibiscus/</p>" +			"</form>");

		text.paint(getParent());

  	LabelGroup group = new LabelGroup(getParent(),i18n.tr("Hibiscus"));
 	
 		AbstractPlugin plugin = PluginLoader.getPlugin(HBCI.class);
  	group.addLabelPair(i18n.tr("Version"), 					new LabelInput(""+ plugin.getVersion() + "-" + plugin.getBuildnumber()));

  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: About.java,v $
 * Revision 1.2  2004/05/18 22:45:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/18 22:40:59  willuhn
 * @N added about screen
 *
 **********************************************************************/