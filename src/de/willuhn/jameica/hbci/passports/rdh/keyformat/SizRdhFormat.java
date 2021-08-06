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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.HBCIPassportInternal;
import org.kapott.hbci.passport.HBCIPassportRDHNew;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCICallbackSWT;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey;
import de.willuhn.jameica.hbci.passports.rdh.server.PassportHandleImpl;
import de.willuhn.jameica.hbci.passports.rdh.server.RDHKeyImpl;
import de.willuhn.jameica.plugin.PluginResources;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;


/**
 * Implementierung des Schluesselformats SizRDH, jedoch als Import.
 */
public class SizRdhFormat extends AbstractSizRdhFormat
{
  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat#getName()
   */
  public String getName()
  {
    return i18n.tr("SizRDH-Format, Import und Konvertierung in Hibiscus-Format");
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat#importKey(java.io.File)
   */
  public RDHKey importKey(File file) throws ApplicationException, OperationCanceledException
  {
    // Checken, ob die Datei lesbar ist.
    if (file == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie eine Schlüsseldatei aus"));
    
    if (!file.canRead() || !file.isFile())
      throw new ApplicationException(i18n.tr("Schlüsseldatei nicht lesbar"));

    HBCI plugin           = (HBCI) Application.getPluginLoader().getPlugin(HBCI.class);
    PluginResources res   = plugin.getResources();
    
    // Wir fragen den User, wo er den Schluessel hinhaben will.
    FileDialog dialog = new FileDialog(GUI.getShell(), SWT.SAVE);
    dialog.setText(Application.getI18n().tr("Bitte wählen einen Pfad und Dateinamen, an dem der importierte Schlüssel gespeichert werden soll."));
    dialog.setFileName("hibiscus-" + file.getName());
    dialog.setOverwrite(true);
    dialog.setFilterPath(res.getWorkPath());
    String newFile = dialog.open();
    
    if (newFile == null || newFile.length() == 0)
      throw new ApplicationException(i18n.tr("Keine Datei ausgewählt"));
    
    File newKey = new File(newFile);
    if (!newKey.getParentFile().canWrite())
      throw new ApplicationException(i18n.tr("Keine Schreibberechtigung"));

    // BUGZILLA 289
    Settings settings = res.getSettings();
    HBCICallback callback = plugin.getHBCICallback();

    try
    {
      ////////////////////////////////////////////////////////////////////////
      // Erst laden wir den SizRDH-Schluessel
      Logger.info("loading sizrdh key");
      if (callback != null && (callback instanceof HBCICallbackSWT))
        ((HBCICallbackSWT)callback).setCurrentHandle(new PassportHandleImpl());
      
      // Abfrage des Passwortes erzwingen
      settings.setAttribute("hbcicallback.askpassphrase.force",true);
      
      HBCIUtils.setParam("client.passport.SIZRDHFile.filename",file.getAbsolutePath());
      HBCIUtils.setParam("client.passport.SIZRDHFile.libname",getRDHLib());
      HBCIUtils.setParam("client.passport.SIZRDHFile.init","0");
      HBCIPassportInternal source = (HBCIPassportInternal) AbstractHBCIPassport.getInstance("SIZRDHFile");
      ////////////////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////////////////////////
      // Jetzt erzeugen wir einen im HBCI4Java-Format und kopieren die Daten
      Logger.info("converting into hbci4java format");
      HBCIUtils.setParam("client.passport.default","RDHNew");
      HBCIUtils.setParam("client.passport.RDHNew.filename",newKey.getAbsolutePath());
      HBCIUtils.setParam("client.passport.RDHNew.init","0");
      HBCIPassportInternal target = (HBCIPassportInternal) AbstractHBCIPassport.getInstance("RDHNew");

      target.setCountry(source.getCountry());
      target.setBLZ(source.getBLZ());
      target.setHost(source.getHost());
      target.setPort(source.getPort());
      target.setUserId(source.getUserId());
      target.setCustomerId(source.getCustomerId());
      target.setSysId(source.getSysId());
      target.setSigId(source.getSigId());
      target.setHBCIVersion(source.getHBCIVersion());
      target.setBPD(source.getBPD());
      target.setUPD(source.getUPD());
          
      ((HBCIPassportRDHNew)target).setInstSigKey(source.getInstSigKey());
      ((HBCIPassportRDHNew)target).setInstEncKey(source.getInstEncKey());
      ((HBCIPassportRDHNew)target).setMyPublicSigKey(source.getMyPublicSigKey());
      ((HBCIPassportRDHNew)target).setMyPrivateSigKey(source.getMyPrivateSigKey());
      ((HBCIPassportRDHNew)target).setMyPublicEncKey(source.getMyPublicEncKey());
      ((HBCIPassportRDHNew)target).setMyPrivateEncKey(source.getMyPrivateEncKey());
          
      target.saveChanges();
      target.close();
      source.close();
      ////////////////////////////////////////////////////////////////////////
      RDHKeyImpl key = new RDHKeyImpl(newKey);
      key.setFormat(new HBCI4JavaFormat()); // wir tragen nicht uns selbst ein - da wir den ja ins HBCI4Java-Format konvertiert haben
      return key;
    }
    catch (ApplicationException | OperationCanceledException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      OperationCanceledException oce = (OperationCanceledException) HBCIProperties.getCause(e,OperationCanceledException.class);
      if (oce != null)
        throw oce;
        
      ApplicationException ae = (ApplicationException) HBCIProperties.getCause(e,ApplicationException.class);
      if (ae != null)
        throw ae;

      Logger.error("unable to import key " + file.getAbsolutePath(),e);
      throw new ApplicationException(i18n.tr("Schlüsseldatei kann nicht importiert werden: {0}",e.getMessage()));
    }
    finally
    {
      settings.setAttribute("hbcicallback.askpassphrase.force",false);
      if (callback != null && (callback instanceof HBCICallbackSWT))
        ((HBCICallbackSWT)callback).setCurrentHandle(null);
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat#load(de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey)
   */
  public HBCIPassport load(RDHKey key) throws ApplicationException, OperationCanceledException
  {
    // Hihi - nach dem Import ist das ja dann unser Format ;)
    // Aber eigentlich kann das gar nicht passieren, da der Schluessel ja konvertiert wurde.
    Logger.warn("SUSPECT - key should have hbci4java format");
    return new HBCI4JavaFormat().load(key);
  }


}


/**********************************************************************
 * $Log: SizRdhFormat.java,v $
 * Revision 1.4  2011/05/24 09:15:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2011-05-24 09:11:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2011-05-24 09:06:10  willuhn
 * @C Refactoring und Vereinfachung von HBCI-Callbacks
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
