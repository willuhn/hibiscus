/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh.keyformat;

import java.io.File;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.PluginResources;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Platform;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Abstrakte Basis-Implementierung des Schluesselformats SizRDH.
 */
public abstract class AbstractSizRdhFormat extends AbstractKeyFormat
{
  protected static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  @Override
  public RDHKey createKey(File file) throws ApplicationException, OperationCanceledException
  {
    throw new ApplicationException(i18n.tr("Das Erstellen von neuen Schlüsseln wird für dieses Format nicht unterstützt"));
  }

  @Override
  public boolean hasFeature(int feature)
  {
    switch (feature)
    {
      case KeyFormat.FEATURE_CREATE:
        return false;
      case KeyFormat.FEATURE_IMPORT:
        int os = Application.getPlatform().getOS();
        // Wir haben die Lib nur fuer Windows-32 und Linux-32
        return os == Platform.OS_LINUX || 
               os == Platform.OS_WINDOWS;
    }
    Logger.warn("unknown feature " + feature);
    return false;
  }

  /**
   * Liefert Pfad und Dateiname der nativen SIZRDH-Lib.
   * Diese unterscheidet sich je nach Betriebssystem.
   * @return Pfad zur RDHLib.
   * @throws ApplicationException
   */
  protected static String getRDHLib() throws ApplicationException
  {
    AbstractPlugin p    = Application.getPluginLoader().getPlugin(HBCI.class);
    PluginResources res = p.getResources();
    Settings settings   = res.getSettings();
    Manifest mf         = p.getManifest();

    String file = null;
  
    switch (Application.getPlatform().getOS())
    {
      case Platform.OS_LINUX:
        file = settings.getString("sizrdh.nativelib","libhbci4java-sizrdh-linux-gcc3.so");
        break;
      case Platform.OS_WINDOWS:
        file = settings.getString("sizrdh.nativelib","hbci4java-sizrdh-win32.dll");;
        break;
    }
    if (file == null)
      throw new ApplicationException(res.getI18N().tr("SizRDH-Schlüsseldateien werden für Ihr Betriebssystem nicht von Hibiscus unterstützt"));

    file = mf.getPluginDir() + File.separator + "lib" + File.separator + file;
    Logger.info("using sizrdh native lib " + file);
    return file;
  }
}
