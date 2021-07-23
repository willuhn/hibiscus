/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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

  @Override
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

  @Override
  public String getName()
  {
    return i18n.tr("Swift MT940-Format");
  }

  @Override
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (!Umsatz.class.equals(objectType))
      return null; // Wir bieten uns nur fuer Umsaetze an
    
    IOFormat f = new IOFormat() {
      @Override
      public String getName()
      {
        return MT940UmsatzImporter.this.getName();
      }

      @Override
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
     */
    @Override
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
