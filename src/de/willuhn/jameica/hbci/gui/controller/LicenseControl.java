/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/LicenseControl.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/06/08 22:28:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.io.File;
import java.io.FileInputStream;
import java.rmi.RemoteException;

import de.willuhn.jameica.AbstractPlugin;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.InfoReader;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.util.FileFinder;
import de.willuhn.util.I18N;

/**
 * Controller fuer den Dialog Lizenzinformationen.
 */
public class LicenseControl extends AbstractControl {

  private FormTextPart libList = null;
  private I18N i18n = null;

  /**
   * ct.
   * @param view
   */
  public LicenseControl(AbstractView view) {
    super(view);
    i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Liefert eine Liste mit allen direkt von Hibiscus verwendeten Komponenten.
   * @return Liste der verwendeten Komponenten
   * @throws RemoteException
   */
  public FormTextPart getLibList() throws RemoteException
  {
    if (libList != null)
      return libList;

    AbstractPlugin plugin = PluginLoader.getPlugin(HBCI.class);

    StringBuffer buffer = new StringBuffer();
    buffer.append("<form>");

    InfoReader ir = null;
    try {
      ir = PluginLoader.getPluginContainer(HBCI.class).getInfo();
    }
    catch (Exception e)
    {
      Application.getLog().error("unable to read info.xml from plugin hibiscus",e);
    }
    buffer.append("<p><span color=\"header\" font=\"header\">" + i18n.tr("Hibiscus") + "</span></p>");
    if (ir != null)
    {
      buffer.append("<p>");
      buffer.append("<br/>" + i18n.tr("Version") + ": " + plugin.getVersion() + "-" + plugin.getBuildnumber());
      buffer.append("<br/>" + i18n.tr("Beschreibung") + ": " + ir.getDescription());
      buffer.append("<br/>" + i18n.tr("URL") + ": " + ir.getUrl());
      buffer.append("<br/>" + i18n.tr("Lizenz") + ": " + ir.getLicense());
      buffer.append("</p>");
    }


    String path = plugin.getResources().getPath();

    FileFinder finder = new FileFinder(new File(path + "/lib"));
    finder.matches(".*?info\\.xml$");
    File[] infos = finder.findRecursive();
    for (int i=0;i<infos.length;++i)
    {
      try {
        ir = new InfoReader(new FileInputStream(infos[i]));
        if (ir == null)
        {
          Application.getLog().warn("inforeader is null, skipping lib");
          continue;
        }
        buffer.append("<p>");
        buffer.append("<b>" + ir.getName() + "</b>");
        buffer.append("<br/>" + i18n.tr("Beschreibung") + ": " + ir.getDescription());
        buffer.append("<br/>" + i18n.tr("Verzeichnis") + ": " + infos[i].getParentFile().getAbsolutePath());
        buffer.append("<br/>" + i18n.tr("URL") + ": " + ir.getUrl());
        buffer.append("<br/>" + i18n.tr("Lizenz") + ": " + ir.getLicense());
        buffer.append("</p>");
      }
      catch (Exception e)
      {
        Application.getLog().error("unable to parse " + infos[0],e);
      }
    }
    buffer.append("</form>");

    libList = new FormTextPart(buffer.toString());
    return libList;
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
    GUI.startPreviousView();
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o) {
  }

}


/**********************************************************************
 * $Log: LicenseControl.java,v $
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