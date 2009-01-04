/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/MT940Importer.java,v $
 * $Revision: 1.14 $
 * $Date: 2009/01/04 01:25:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Properties;

import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.manager.HBCIKey;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.swift.Swift;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Importer fuer Swift MT 940.
 */
public class MT940Importer implements Importer
{

  private I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {

    if (is == null)
      throw new ApplicationException(i18n.tr("Keine zu importierende Datei ausgewählt"));
    
    if (format == null)
      throw new ApplicationException(i18n.tr("Kein Datei-Format ausgewählt"));
    
    try
    {
      
      de.willuhn.jameica.hbci.rmi.Konto konto = null;
      
      if (context != null && context instanceof de.willuhn.jameica.hbci.rmi.Konto)
        konto = (de.willuhn.jameica.hbci.rmi.Konto) context;
      
      // Wir erzeugen das HBCI4Java-Umsatz-Objekt selbst. Dann muessen wir
      // an der eigentlichen Parser-Routine nichts mehr aendern.
      GVRKUms umsaetze = new MyGVRKUms();

      if (monitor != null)
        monitor.setStatusText(i18n.tr("Lese Datei ein"));

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      int read = 0;
      byte[] buf = new byte[8192];

      do
      {
        read = is.read(buf);
        if (read > 0)
          bos.write(buf, 0, read);
      }
      while (read != -1);
      bos.close();

      StringBuffer buffer = new StringBuffer(Swift.decodeUmlauts(bos.toString()));
      umsaetze.appendMT940Data(buffer.toString());

      if (monitor != null)
        monitor.setStatusText(i18n.tr("Speichere Umsätze"));
      
      GVRKUms.UmsLine[] lines = umsaetze.getFlatData();
      
      if (lines.length == 0)
      {
        konto.addToProtokoll(i18n.tr("Keine Umsätze importiert"),Protokoll.TYP_ERROR);
        return;
      }
      
      double factor = 100d / (double) lines.length;

      int created = 0;
      int error   = 0;

      for (int i=0;i<lines.length;++i)
      {
        if (monitor != null)
        {
          monitor.log(i18n.tr("Umsatz {0}", "" + (i+1)));
          // Mit diesem Factor sollte sich der Fortschrittsbalken
          // bis zum Ende der Swift-MT940-Datei genau auf 100% bewegen
          monitor.setPercentComplete((int)((i+1) * factor));
        }

        try
        {
          final Umsatz umsatz = Converter.HBCIUmsatz2HibiscusUmsatz(lines[i]);
          umsatz.setKonto(konto); // muessen wir noch machen, weil der Converter das Konto nicht kennt
          umsatz.store();
          created++;
          try
          {
            Application.getMessagingFactory().sendMessage(new ImportMessage(umsatz));
          }
          catch (Exception ex)
          {
            Logger.error("error while sending import message",ex);
          }
        }
        catch (ApplicationException ae)
        {
          monitor.log("  " + ae.getMessage());
          error++;
        }
        catch (Exception e)
        {
          Logger.error("unable to import line",e);
          monitor.log("  " + i18n.tr("Fehler beim Import des Datensatzes: {0}",e.getMessage()));
          error++;
        }
      }
      monitor.setStatusText(i18n.tr("{0} Umsätze erfolgreich importiert, {1} fehlerhafte übersprungen", new String[]{""+created,""+error}));
      monitor.addPercentComplete(1);
    }
    catch (OperationCanceledException oce)
    {
      Logger.warn("operation cancelled");
      throw new ApplicationException(i18n.tr("Import abgebrochen"));
    }
    catch (Exception e)
    {
      Logger.error("error while reading file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Import der Swift-Datei"));
    }
    finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        }
        catch (IOException e)
        {
          Logger.error("error while closing inputstream",e);
        }
      }
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("Swift MT-940 Format");
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (!Umsatz.class.equals(objectType))
      return null; // Wir bieten uns nur fuer Umsaetze an
    
    IOFormat f = new IOFormat() {
      public String getName()
      {
        return i18n.tr("Swift MT-940");
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      public String[] getFileExtensions()
      {
        return new String[] {"*.sta"};
      }
    };
    return new IOFormat[] { f };
  }
  
  
  /**
   * Hilfsklasse um getPassport umzubiegen.
   * BUGZILLA 255
   * @author willuhn
   */
  private class MyGVRKUms extends GVRKUms
  {
    /**
     * Wir liefern hier einen Dummy-Passport zurueck.
     * @see org.kapott.hbci.GV_Result.HBCIJobResultImpl#getPassport()
     */
    public HBCIPassport getPassport()
    {
      return new HBCIPassport()
      {
      
        public void syncSysId() {}
        public void syncSigId() {}
        public void setUserId(String userid) {}
        public void setPort(Integer port) {}
        public void setHost(String host) {}
        public void setFilterType(String filter) {}
        public void setCustomerId(String customerid) {}
        public void setCountry(String country) {}
        public void setClientData(String id, Object o) {}
        public void setBLZ(String blz) {}
        public void saveChanges() {}
        public boolean onlyBPDGVs() {return false;}
        public boolean needUserKeys() {return false;}
        public boolean needInstKeys() {return false;}
        public boolean needDigKey() {return false;}
        public boolean isSupported() {return false;}
        public boolean hasMySigKey() {return false;}
        public boolean hasMyEncKey() {return false;}
        public boolean hasInstSigKey() {return false;}
        public boolean hasInstEncKey() {return false;}
        public boolean hasInstDigKey() {return false;}
        public String getUserId() {return null;}
        public String getUPDVersion() {return null;}
        public Properties getUPD() {return null;}
        public String[] getSuppVersions() {return null;}
        public String[][] getSuppSecMethods() {return null;}
        public String[] getSuppLangs() {return null;}
        public String[][] getSuppCompMethods() {return null;}
        public Integer getPort() {return null;}
        public HBCIKey getMyPublicSigKey() {return null;}
        public HBCIKey getMyPublicEncKey() {return null;}
        public HBCIKey getMyPublicDigKey() {return null;}
        public HBCIKey getMyPrivateSigKey() {return null;}
        public HBCIKey getMyPrivateEncKey() {return null;}
        public HBCIKey getMyPrivateDigKey() {return null;}
        public int getMaxMsgSizeKB() {return 0;}
        public int getMaxGVperMsg() {return 0;}
        public HBCIKey getInstSigKey() {return null;}
        public String getInstName() {return null;}
        public HBCIKey getInstEncKey() {return null;}
        public String getHost() {return null;}
        public String getHBCIVersion() {return null;}
        public String getFilterType() {return null;}
        public String getDefaultLang() {return null;}
        public String getCustomerId(int idx) {return null;}
        public String getCustomerId() {return null;}
        public String getCountry() {return null;}
        public Object getClientData(String id) {return null;}
        public String getBPDVersion() {return null;}
        public Properties getBPD() {return null;}
        public String getBLZ() {return null;}
        public Konto[] getAccounts() {return null;}
        public Konto getAccount(String number) {return null;}
        public void fillAccountInfo(Konto account) {}
        public void close() {}
        public void clearUPD() {}
        public void clearInstSigKey() {}
        public void clearInstEncKey() {}
        public void clearInstDigKey(){}
        public void clearBPD(){}
        public void changePassphrase(){}
      
      };
    }
    
  }
}

/*******************************************************************************
 * $Log: MT940Importer.java,v $
 * Revision 1.14  2009/01/04 01:25:47  willuhn
 * @N Checksumme von Umsaetzen wird nun generell beim Anlegen des Datensatzes gespeichert. Damit koennen Umsaetze nun problemlos geaendert werden, ohne mit "hasChangedByUser" checken zu muessen. Die Checksumme bleibt immer erhalten, weil sie in UmsatzImpl#insert() sofort zu Beginn angelegt wird
 * @N Umsaetze sind nun vollstaendig editierbar
 *
 * Revision 1.13  2008/01/22 13:34:45  willuhn
 * @N Neuer XML-Import/-Export
 *
 * Revision 1.12  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.11  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 * Revision 1.10  2006/11/20 23:07:54  willuhn
 * @N new package "messaging"
 * @C moved ImportMessage into new package
 *
 * Revision 1.9  2006/08/21 23:15:01  willuhn
 * @N Bug 184 (CSV-Import)
 *
 * Revision 1.8  2006/08/02 17:49:44  willuhn
 * @B Bug 255
 * @N Erkennung des Kontos beim Import von Umsaetzen aus dem Kontextmenu heraus
 *
 * Revision 1.7  2006/06/19 11:52:17  willuhn
 * @N Update auf hbci4java 2.5.0rc9
 *
 * Revision 1.6  2006/06/06 21:37:55  willuhn
 * @R FilternEngine entfernt. Wird jetzt ueber das Jameica-Messaging-System abgewickelt
 *
 * Revision 1.5  2006/01/23 23:07:23  willuhn
 * @N csv import stuff
 *
 * Revision 1.4  2006/01/23 12:16:57  willuhn
 * @N Update auf HBCI4Java 2.5.0-rc5
 *
 * Revision 1.3  2006/01/23 00:36:29  willuhn
 * @N Import, Export und Chipkartentest laufen jetzt als Background-Task
 *
 * Revision 1.2  2006/01/18 00:51:01  willuhn
 * @B bug 65
 *
 * Revision 1.1  2006/01/17 00:22:36  willuhn
 * @N erster Code fuer Swift MT940-Import
 *
 ******************************************************************************/