/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.io.File;
import java.io.FileInputStream;

import de.willuhn.io.FileFinder;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.InfoReader;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Controller fuer den Dialog Lizenzinformationen.
 */
public class LicenseControl extends AbstractControl {

  private Part libList = null;
  private I18N i18n = null;

  /**
   * ct.
   * @param view
   */
  public LicenseControl(AbstractView view) {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Liefert eine Liste mit allen direkt von Hibiscus verwendeten Komponenten.
   * @return Liste der verwendeten Komponenten
   */
  public Part getLibList()
  {
    if (libList != null)
      return libList;

    AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);

    StringBuffer buffer = new StringBuffer();
    buffer.append("<form>");

    Manifest manifest = null;
    try {
      manifest = Application.getPluginLoader().getManifest(HBCI.class);
    }
    catch (Exception e)
    {
      Logger.error("unable to read info.xml from plugin hibiscus",e);
    }
    buffer.append("<p><span color=\"header\" font=\"header\">" + i18n.tr("Hibiscus") + "</span></p>");
    if (manifest != null)
    {
      buffer.append("<p>");
      buffer.append(manifest.getDescription());
      buffer.append("<br/>" + manifest.getHomepage());
      buffer.append("<br/>" + manifest.getLicense());
      buffer.append("</p>");
    }


    String path = plugin.getManifest().getPluginDir();

    FileFinder finder = new FileFinder(new File(path + "/lib"));
    finder.matches(".*?info\\.xml$");
    File[] infos = finder.findRecursive();
    for (File info : infos) {
      if (!info.isFile() || !info.canRead())
      {
        Logger.warn("unable to read " + info + ", skipping");
        continue;
      }

      try {
        InfoReader ir = new InfoReader(new FileInputStream(info));
        buffer.append("<p>");
        buffer.append("<b>" + ir.getName() + "</b>");
        buffer.append("<br/>" + ir.getDescription());
        buffer.append("<br/>" + ir.getUrl());
        buffer.append("<br/>" + ir.getLicense());
        buffer.append("</p>");
      }
      catch (Exception e)
      {
        // TODO hier auch "info" statt "infos[0]"?
        Logger.error("unable to parse " + infos[0],e);
      }
    }
    buffer.append("</form>");

    libList = new FormTextPart(buffer.toString());
    return libList;
  }
}


/**********************************************************************
 * $Log: LicenseControl.java,v $
 * Revision 1.16  2011/06/08 09:16:51  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2011-04-26 12:15:51  willuhn
 * @B Potentielle Bugs gemaess Code-Checker
 *
 * Revision 1.14  2009/03/10 23:51:31  willuhn
 * @C PluginResources#getPath als deprecated markiert - stattdessen sollte jetzt Manifest#getPluginDir() verwendet werden
 *
 * Revision 1.13  2006/06/30 13:51:54  willuhn
 * @N Pluginloader Redesign in HEAD uebernommen
 *
 * Revision 1.12  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.11  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.9  2004/10/11 22:41:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/10/08 13:37:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/10/08 00:19:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.5  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.3  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/06/08 22:28:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/04/26 22:57:32  willuhn
 * @N License informations
 *
 * Revision 1.1  2004/04/26 22:42:18  willuhn
 * @N added InfoReader
 *
 **********************************************************************/