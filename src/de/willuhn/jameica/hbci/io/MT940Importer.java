/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/MT940Importer.java,v $
 * $Revision: 1.7 $
 * $Date: 2006/06/19 11:52:17 $
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

import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.swift.Swift;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
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
   * ct.
   */
  public MT940Importer()
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(de.willuhn.datasource.GenericObject, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(GenericObject context, IOFormat format, InputStream is, ProgressMonitor monitor) throws RemoteException, ApplicationException
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
      GVRKUms umsaetze = new GVRKUms();

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
      
      DBIterator existing = konto.getUmsaetze();
      GVRKUms.UmsLine[] lines = umsaetze.getFlatData();
      
      if (lines.length == 0)
      {
        konto.addToProtokoll(i18n.tr("Keine Umsätze importiert"),Protokoll.TYP_ERROR);
        return;
      }
      
      double factor = 100d / (double) lines.length;

      int created = 0;
      int skipped = 0;
      // Eine Transaktion beim Speichern brauchen wir nicht, weil beim
      // naechsten Import die schon importierten erkannt und uebersprungen werden.
      for (int i=0;i<lines.length;++i)
      {
        if (monitor != null)
        {
          monitor.log(i18n.tr("Umsatz {0}", "" + (i+1)));
          // Mit diesem Factor sollte sich der Fortschrittsbalken
          // bis zum Ende der Swift-MT940-Datei genau auf 100% bewegen
          monitor.setPercentComplete((int)((i+1) * factor));
        }
        
        final Umsatz umsatz = Converter.HBCIUmsatz2HibiscusUmsatz(lines[i]);
        umsatz.setKonto(konto); // muessen wir noch machen, weil der Converter das Konto nicht kennt
        
        // Wenn keine geparsten Verwendungszwecke da sind, machen wir
        // den Umsatz editierbar.
        if(lines[i].usage == null || lines[i].usage.length == 0)
          umsatz.setChangedByUser();
        
        if (existing.contains(umsatz) == null)
        {
          try
          {
            umsatz.store(); // den Umsatz haben wir noch nicht, speichern!
            created++;

            try
            {
              ImportMessage im = new ImportMessage() {
                public GenericObject getImportedObject() throws RemoteException
                {
                  return umsatz;
                }
              };
              Application.getMessagingFactory().sendMessage(im);
            }
            catch (Exception e)
            {
              Logger.error("error while sending import message",e);
            }
          }
          catch (Exception e2)
          {
            Logger.error("error while adding umsatz, skipping this one",e2);
          }
        }
        else
        {
          skipped++;
        }
      }
      if (monitor != null)
      {
        monitor.setStatusText(i18n.tr("{0} Umsätze importiert, {1} übersprungen (bereits vorhanden)", new String[]{""+created,""+skipped}));
        monitor.addPercentComplete(1);
      }
      konto.addToProtokoll(i18n.tr("{0} Umsätze importiert, {1} übersprungen (bereits vorhanden)", new String[]{""+created,""+skipped}),Protokoll.TYP_SUCCESS);
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
}

/*******************************************************************************
 * $Log: MT940Importer.java,v $
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