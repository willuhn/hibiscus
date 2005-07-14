/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/About.java,v $
 * $Revision: 1.10 $
 * $Date: 2005/07/14 18:03:54 $
 * $Author: web0 $
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

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * About-Dialog.
 */
public class About extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {
    I18N i18n = Application.getI18n();

    Label l = new Label(getParent(),SWT.BORDER);
    l.setImage(new Image(GUI.getDisplay(),getClass().getClassLoader().getResourceAsStream("img/hibiscus.jpg")));

    FormTextPart text = new FormTextPart();
    text.setText("<form>" +
      "<p><b>Hibiscus - HBCI-Onlinebanking für Jameica</b></p>" +
      "<br/><p>Licence: GPL [<a href=\"" + Program.class.getName() + "\">http://www.gnu.org/copyleft/gpl.html</a>]</p>" +
      "<br/><p>Copyright by Olaf Willuhn [<a href=\"" + Program.class.getName() + "\">mailto:hibiscus@willuhn.de</a>]</p>" +
      "<p><a href=\"" + Program.class.getName() + "\">http://www.willuhn.de/projects/hibiscus/</a></p>" +
      "</form>");

    text.paint(getParent());

    LabelGroup group = new LabelGroup(getParent(),i18n.tr("Hibiscus"));

    AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
    group.addLabelPair(i18n.tr("Version"), new LabelInput(""+ plugin.getManifest().getVersion()));
    group.addLabelPair(i18n.tr("Build-Nummer"), new LabelInput(""+ plugin.getBuildnumber() + " [" + plugin.getBuildDate() + "]"));

  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: About.java,v $
 * Revision 1.10  2005/07/14 18:03:54  web0
 * @N buildnumber/date in About-Dialog
 *
 * Revision 1.9  2005/03/31 23:05:46  web0
 * @N geaenderte Startseite
 * @N klickbare Links
 *
 * Revision 1.8  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.7  2004/10/11 22:41:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.4  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/06/08 22:28:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/05/18 22:45:24  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/18 22:40:59  willuhn
 * @N added about screen
 *
 **********************************************************************/