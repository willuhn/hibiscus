/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/AbstractDTAUSImporter.java,v $
 * $Revision: 1.4 $
 * $Date: 2006/08/07 14:31:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

import org.eclipse.swt.SWTException;

import de.jost_net.OBanToo.Dtaus.ASatz;
import de.jost_net.OBanToo.Dtaus.CSatz;
import de.jost_net.OBanToo.Dtaus.DtausDateiParser;
import de.jost_net.OBanToo.Dtaus.ESatz;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Abstrakte Basis-Klasse fuer DTAUS-Import/Export.
 */
public abstract class AbstractDTAUSImporter extends AbstractDTAUSIO implements Importer
{
  /**
   * ct.
   */
  public AbstractDTAUSImporter()
  {
    super();
  }
  

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(de.willuhn.datasource.GenericObject, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doImport(GenericObject context, IOFormat format, InputStream is,
      ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    try
    {
      if (format == null || !(format instanceof MyIOFormat))
        throw new ApplicationException(i18n.tr("Unbekanntes Import-Format"));
      
      DtausDateiParser parser = new DtausDateiParser(is);
      
      int files = parser.getAnzahlLogischerDateien();
      
      for (int i=0;i<files;++i)
      {
        monitor.setPercentComplete(0);
        
        monitor.setStatusText(i18n.tr("Importiere logische Datei Nr. {0}",""+(i+1)));
        
        parser.setLogischeDatei(i+1);
        
        // Im E-Satz steht die Anzahl der Datensaetze. Die brauchen wir, um
        // den Fortschrittsbalken mit sinnvollen Daten fuettern zu koennen.
        ASatz a = parser.getASatz();
        ESatz e = parser.getESatz();

        double factor = 100d / e.getAnzahlDatensaetze();
        int count = 0;
        int success = 0;
        
        DBService service = Settings.getDBService();

        CSatz c = null;
        while ((c = parser.next()) != null)
        {
          try
          {
            // Mit diesem Factor sollte sich der Fortschrittsbalken
            // bis zum Ende der DTAUS-Datei genau auf 100% bewegen
            monitor.setPercentComplete((int)((++count) * factor));
            
            if (c == null)
            {
              monitor.log(i18n.tr("Datensatz {0} nicht lesbar. Überspringe",""+count));
              continue;
            }
            
            monitor.log(i18n.tr("Importiere Datensatz {0}",c.getNameEmpfaenger()));
           
            // Gewuenschtes Objekt erstellen
            final DBObject skel = service.createObject(((MyIOFormat)format).type,null);
            
            // Mit Daten befuellen lassen
            create(skel,context,c,a);

            success++;

            // Jetzt noch ggf. andere ueber das neue Objekt informieren
            try
            {
              ImportMessage im = new ImportMessage() {
                public GenericObject getImportedObject() throws RemoteException
                {
                  return skel;
                }
              };
              Application.getMessagingFactory().sendMessage(im);
            }
            catch (Exception ex)
            {
              Logger.error("error while sending import message",ex);
            }
          }
          catch (ApplicationException ace)
          {
            monitor.log("  " + ace.getMessage());
            monitor.log("  " + i18n.tr("Überspringe Datensatz"));
          }
          catch (Exception e1)
          {
            if (e1 instanceof SWTException)
            {
              if (e1.getCause() instanceof OperationCanceledException)
              {
                Logger.info("operation cancelled");
                monitor.setStatusText(i18n.tr("Import abgebrochen"));
                monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
                return;
              }
            }

            Logger.error("unable to import transfer",e1);
            monitor.log("  " + i18n.tr("Fehler beim Import des Datensatzes, überspringe Datensatz"));
          }
        }
        monitor.setStatusText("  " + i18n.tr("{0} Datensätze erfolgreich importiert",""+success));
      }
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
      monitor.setStatusText(i18n.tr("Import abgebrochen"));
      monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
    }
    catch (Exception e)
    {
      throw new RemoteException(i18n.tr("Fehler beim Import der DTAUS-Daten"),e);
    }
    finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        }
        catch (IOException ioe)
        {
          Logger.error("unable to close inputstream",ioe);
        }
      }
    }
  }

  /**
   * Muss von den abgeleiteten Klassen implementiert werden, damit sie dort das Hibiscus-Fachobjekt befuellen
   * und speichern.
   * @param skel das schon vorbereitete Hibiscus-Fachobjekt.
   * @param context der Kontext. Kann zB ein Konto sein.
   * @param csatz der C-Satz mit den auszulesenden Daten.
   * @param asatz der A-Satz.
   * @throws RemoteException
   * @throws ApplicationException
   */
  abstract void create(DBObject skel, GenericObject context, CSatz csatz, ASatz asatz)
    throws RemoteException, ApplicationException;

}


/*********************************************************************
 * $Log: AbstractDTAUSImporter.java,v $
 * Revision 1.4  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.3  2006/06/19 12:57:31  willuhn
 * @N DTAUS-Import fuer Umsaetze
 * @B Formatierungsfehler in Umsatzliste
 *
 * Revision 1.2  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.1  2006/06/08 17:40:59  willuhn
 * @N Vorbereitungen fuer DTAUS-Import von Sammellastschriften und Umsaetzen
 *
 **********************************************************************/