/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/keyformat/AbstractSizRdhFormat.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/06/17 11:26:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
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
public abstract class AbstractSizRdhFormat implements KeyFormat
{
  protected static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat#createKey(java.io.File)
   */
  public RDHKey createKey(File file) throws ApplicationException, OperationCanceledException
  {
    throw new ApplicationException(i18n.tr("Das Erstellen von neuen Schlüsseln wird für dieses Format nicht unterstützt"));
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat#hasFeature(int)
   */
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
      throw new ApplicationException(res.getI18N().tr("SizRDH-Schlüsseldisketten werden für Ihr Betriebssystem nicht von Hibiscus unterstützt"));

    file = mf.getPluginDir() + File.separator + "lib" + File.separator + file;
    Logger.info("using sizrdh native lib " + file);
    return file;
  }
}


/**********************************************************************
 * $Log: AbstractSizRdhFormat.java,v $
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.5  2009/03/29 22:25:56  willuhn
 * @B Warte-Dialog wurde nicht angezeigt, wenn Schluesseldiskette nicht eingelegt
 *
 * Revision 1.4  2008/11/17 23:23:27  willuhn
 * @N SizRDH nur noch fuer Win32 und Linux32 zulassen. Fuer alle anderen Plattformen haben wir sowieso keine Lib
 * @C Code zur Ermittlung des OS in Jameica verschoben
 *
 * Revision 1.3  2008/07/25 11:34:56  willuhn
 * @B Bugfixing
 *
 * Revision 1.2  2008/07/25 11:06:08  willuhn
 * @N RDH-2 Format
 * @C Haufenweise Code-Cleanup
 *
 * Revision 1.1  2008/07/24 23:36:20  willuhn
 * @N Komplette Umstellung der Schluessel-Verwaltung. Damit koennen jetzt externe Schluesselformate erheblich besser angebunden werden.
 * ACHTUNG - UNGETESTETER CODE - BITTE NOCH NICHT VERWENDEN
 *
 **********************************************************************/
