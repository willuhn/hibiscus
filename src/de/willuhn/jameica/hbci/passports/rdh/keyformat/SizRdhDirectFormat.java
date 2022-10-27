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
import java.rmi.RemoteException;

import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCICallbackSWT;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey;
import de.willuhn.jameica.hbci.passports.rdh.server.PassportHandleImpl;
import de.willuhn.jameica.hbci.passports.rdh.server.RDHKeyImpl;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;


/**
 * Implementierung des Schluesselformats SizRDH, jedoch parallele Nutzung.
 */
public class SizRdhDirectFormat extends AbstractSizRdhFormat
{
  @Override
  public String getName()
  {
    return i18n.tr("SizRDH-Format, parallele Nutzung");
  }

  @Override
  public RDHKey importKey(File file) throws ApplicationException, OperationCanceledException
  {
    // Checken, ob die Datei lesbar ist.
    if (file == null)
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie eine Schl�sseldatei aus"));
    
    if (!file.canRead() || !file.isFile())
      throw new ApplicationException(i18n.tr("Schl�sseldatei nicht lesbar"));
    
    // Das ist ein Hibiscus-Schluessel. Wir lassen den Schluessel gleich dort, wo er ist
    try
    {
      RDHKeyImpl key = new RDHKeyImpl(file);
      key.setFormat(this);
      return key;
    }
    catch (RemoteException re)
    {
      Logger.error("unable to import key " + file.getAbsolutePath(),re);
      throw new ApplicationException(i18n.tr("Schl�sseldatei kann nicht importiert werden: {0}",re.getMessage()));
    }
  }

  @Override
  public HBCIPassport load(RDHKey key) throws ApplicationException, OperationCanceledException
  {
    HBCICallback callback = null;
    try
    {
      String filename = key.getFilename();
      Logger.error("load key " + filename);
      
      HBCI plugin = (HBCI) Application.getPluginLoader().getPlugin(HBCI.class);
      callback = plugin.getHBCICallback();
      if (callback != null && (callback instanceof HBCICallbackSWT))
        ((HBCICallbackSWT)callback).setCurrentHandle(new PassportHandleImpl());

      HBCIUtils.setParam("client.passport.SIZRDHFile.filename",filename);
      HBCIUtils.setParam("client.passport.SIZRDHFile.libname",getRDHLib());
      HBCIUtils.setParam("client.passport.SIZRDHFile.init","1");
      return AbstractHBCIPassport.getInstance("SIZRDHFile");
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      OperationCanceledException oce = (OperationCanceledException) HBCIProperties.getCause(e,OperationCanceledException.class);
      if (oce != null)
        throw oce;
        
      ApplicationException ae = (ApplicationException) HBCIProperties.getCause(e,ApplicationException.class);
      if (ae != null)
        throw ae;

      Logger.error("unable to load key",e);
      throw new ApplicationException(i18n.tr("Fehler beim Laden des Schl�ssels: {0}",e.getMessage()));
    }
    finally
    {
      if (callback != null && (callback instanceof HBCICallbackSWT))
        ((HBCICallbackSWT)callback).setCurrentHandle(null);
    }
  }

}


/**********************************************************************
 * $Log: SizRdhDirectFormat.java,v $
 * Revision 1.2  2011/05/24 09:23:26  willuhn
 * @C Exception-Handling
 *
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.3  2008/11/17 23:23:27  willuhn
 * @N SizRDH nur noch fuer Win32 und Linux32 zulassen. Fuer alle anderen Plattformen haben wir sowieso keine Lib
 * @C Code zur Ermittlung des OS in Jameica verschoben
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
