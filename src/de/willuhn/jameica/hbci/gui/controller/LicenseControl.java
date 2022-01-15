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
    for (File file:finder.findRecursive())
    {
      if (!file.isFile() || !file.canRead())
      {
        Logger.warn("unable to read " + file + ", skipping");
        continue;
      }

      try {
        InfoReader ir = new InfoReader(new FileInputStream(file));
        buffer.append("<p>");
        buffer.append("<b>" + ir.getName() + "</b>");
        buffer.append("<br/>" + ir.getDescription());
        buffer.append("<br/>" + ir.getUrl());
        buffer.append("<br/>" + ir.getLicense());
        buffer.append("</p>");
      }
      catch (Exception e)
      {
        Logger.error("unable to parse " + file,e);
      }
    }
    buffer.append("</form>");

    libList = new FormTextPart(buffer.toString());
    return libList;
  }
}
