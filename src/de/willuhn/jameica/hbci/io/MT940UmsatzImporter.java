/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/MT940UmsatzImporter.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/06/23 07:37:28 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;

import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.manager.HBCIKey;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.swift.Swift;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Importer fuer Umsaetze im Swift MT940-Format.
 */
public class MT940UmsatzImporter implements Importer
{

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor, de.willuhn.jameica.system.BackgroundTask)
   */
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor, BackgroundTask t) throws RemoteException, ApplicationException
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
      
      if (konto == null)
      {
        KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_CENTER);
        d.setText(i18n.tr("Bitte wählen Sie das zu verwendende Konto aus."));
        konto = (de.willuhn.jameica.hbci.rmi.Konto) d.open();
      }
      // Wir erzeugen das HBCI4Java-Umsatz-Objekt selbst. Dann muessen wir
      // an der eigentlichen Parser-Routine nichts mehr aendern.
      GVRKUms umsaetze = new MyGVRKUms();

      if (monitor != null)
        monitor.setStatusText(i18n.tr("Lese Datei ein"));

      InputStreamReader encodedIs = new InputStreamReader(is,MT940UmsatzExporter.CHARSET);
      StringBuffer sb = new StringBuffer();
      int read = 0;
      char[] buf = new char[8192];

      while ((read = encodedIs.read(buf)) != -1)
      {
        if (read > 0)
          sb.append(buf,0,read);
      }
      umsaetze.appendMT940Data(Swift.decodeUmlauts(sb.toString()));

      if (monitor != null)
        monitor.setStatusText(i18n.tr("Speichere Umsätze"));
      
      List lines = umsaetze.getFlatData();
      
      if (lines.size() == 0)
      {
        konto.addToProtokoll(i18n.tr("Keine Umsätze importiert"),Protokoll.TYP_ERROR);
        return;
      }
      
      double factor = 100d / (double) lines.size();

      int created = 0;
      int error   = 0;

      for (int i=0;i<lines.size();++i)
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
          if (t != null && t.isInterrupted())
            throw new OperationCanceledException();

          final Umsatz umsatz = Converter.HBCIUmsatz2HibiscusUmsatz((GVRKUms.UmsLine)lines.get(i));
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
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while reading file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Import der Datei"));
    }
    finally
    {
      IOUtil.close(is);
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("Swift MT940-Format");
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
        return MT940UmsatzImporter.this.getName();
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
        public boolean isSupported() {return false;}
        public boolean hasMySigKey() {return false;}
        public boolean hasMyEncKey() {return false;}
        public boolean hasInstSigKey() {return false;}
        public boolean hasInstEncKey() {return false;}
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
        public void clearBPD(){}
        public void changePassphrase(){}
      };
    }
    
  }
}

/*******************************************************************************
 * $Log: MT940UmsatzImporter.java,v $
 * Revision 1.2  2011/06/23 07:37:28  willuhn
 * @N Ersetzen der Umlaute beim MT940-Export abschaltbar
 * @N Beim MT940-Import explizit mit ISO-8859 lesen - ist zwar eigentlich nicht noetig, weil da per Definition keine Umlaute enthalten sein duerfen - aber wir sind ja tolerant ;)
 *
 * Revision 1.1  2010-06-02 15:32:22  willuhn
 * @Importer umbenannt
 *
 * Revision 1.16  2009/12/07 22:55:32  willuhn
 * @R nicht mehr benoetigte Funktionen entfernt
 *
 * Revision 1.15  2009/02/12 16:14:34  willuhn
 * @N HBCI4Java-Version mit Unterstuetzung fuer vorgemerkte Umsaetze
 *
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