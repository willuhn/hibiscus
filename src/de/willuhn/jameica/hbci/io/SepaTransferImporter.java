/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.kapott.hbci.GV.parsers.ISEPAParser;
import org.kapott.hbci.GV.parsers.SEPAParserFactory;
import org.kapott.hbci.sepa.PainVersion;

import de.willuhn.datasource.rmi.DBService;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.BaseUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;


/**
 * Importer fuer SEPA-Ueberweisungen und -Lastschriften.
 */
public class SepaTransferImporter implements Importer
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
//  private final static Class[] supported = new Class[]{AuslandsUeberweisung.class,SepaLastschrift.class};
  private final static Class[] supported = new Class[]{AuslandsUeberweisung.class};
  
  private Map<String,Konto> kontenCache = new HashMap<String,Konto>();

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  @Override
  public void doImport(Object context, IOFormat format, InputStream is,
      ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    try
    {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      IOUtil.copy(is,bos);
      
      PainVersion version = PainVersion.autodetect(new ByteArrayInputStream(bos.toByteArray()));
      if (version == null)
        throw new ApplicationException(i18n.tr("SEPA-Version der XML-Datei nicht ermittelbar"));
      
      monitor.log(i18n.tr("SEPA-Version: {0}",version.getURN()));
      
      List<Properties> props = new ArrayList<Properties>();
      ISEPAParser parser = SEPAParserFactory.get(version);
      parser.parse(new ByteArrayInputStream(bos.toByteArray()),props);
      
      DBService service = de.willuhn.jameica.hbci.Settings.getDBService();
      
      double factor = 100d / props.size();
      int count = 0;
      int success = 0;
      int error = 0;

      for (Properties prop:props)
      {
        try
        {
          // Mit diesem Factor sollte sich der Fortschrittsbalken
          // bis zum Ende der DTAUS-Datei genau auf 100% bewegen
          monitor.setPercentComplete((int)((++count) * factor));
          monitor.log(i18n.tr("Importiere Datensatz {0}",Integer.toString(count)));

          // Gewuenschtes Objekt erstellen
          final BaseUeberweisung object = (BaseUeberweisung) service.createObject(((MyIOFormat)format).type,null);
          object.setKonto(this.findKonto(prop.getProperty(ISEPAParser.Names.SRC_IBAN.getValue())));
          object.setGegenkontoName(prop.getProperty(ISEPAParser.Names.DST_NAME.getValue()));
          object.setGegenkontoNummer(prop.getProperty(ISEPAParser.Names.DST_IBAN.getValue()));
          object.setGegenkontoBLZ(prop.getProperty(ISEPAParser.Names.DST_BIC.getValue()));
          object.setZweck(prop.getProperty(ISEPAParser.Names.USAGE.getValue()));
          
          object.setBetrag(Double.valueOf(prop.getProperty(ISEPAParser.Names.VALUE.getValue())));

          String date = prop.getProperty(ISEPAParser.Names.DATE.getValue());
          String endToEndId = prop.getProperty(ISEPAParser.Names.ENDTOENDID.getValue());

          object.store();
          System.out.println(object.getID());

          success++;

          Application.getMessagingFactory().sendMessage(new ImportMessage(object));
        }
        catch (ApplicationException ace)
        {
          error++;
          monitor.log("  " + ace.getMessage());
          monitor.log("  " + i18n.tr("Überspringe Datensatz"));
        }
        catch (OperationCanceledException oce)
        {
          throw oce;
        }
        catch (Exception e1)
        {
          error++;
          Logger.error("unable to import transfer",e1);
          monitor.log("  " + i18n.tr("Fehler beim Import des Datensatzes, überspringe Datensatz"));
        }
      }
      if (error > 0)
      {
        monitor.setStatus(ProgressMonitor.STATUS_ERROR);
        monitor.setStatusText("  " + i18n.tr("{0} Datensätze importiert, {1} wegen Fehlern übersprungen",new String[]{""+success,""+error}));
      }
      else
      {
        monitor.setStatusText("  " + i18n.tr("{0} Datensätze erfolgreich importiert",""+success));
      }
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
      monitor.setStatusText(i18n.tr("Import abgebrochen"));
      monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      throw new ApplicationException(i18n.tr("Fehler beim Import der SEPA-XML-Datei: {0}",e.getMessage()),e);
    }
    finally
    {
      IOUtil.close(is);
    }
  }
  
  /**
   * Sucht nach dem Konto mit der angegebenen IBAN.
   * @param iban
   * @return das gefundene Konto oder wenn es nicht gefunden wurde, dann das vom Benutzer ausgewaehlte.
   * Die Funktion liefert nie <code>null</code> sondern wirft eine ApplicationException, wenn kein Konto ausgewaehlt wurde.
   * @throws RemoteException
   * @throws ApplicationException
   * @throws OperationCanceledException
   */
  protected Konto findKonto(String iban) throws RemoteException, ApplicationException
  {
    // Erstmal schauen, ob der User das Konto schonmal ausgewaehlt hat:
    Konto k = (Konto) kontenCache.get(iban);

    // Haben wir im Cache
    if (k != null)
      return k;

    // In der Datenbank suchen
    k = KontoUtil.findByIBAN(iban);

    // Nichts gefunden. Dann fragen wir den User
    if (k == null)
    {
      // Das Konto existiert nicht im Hibiscus-Datenbestand. Also soll der
      // User eines auswaehlen
      KontoAuswahlDialog d = new KontoAuswahlDialog(null,KontoFilter.FOREIGN,KontoAuswahlDialog.POSITION_CENTER);
      d.setText(i18n.tr("Konto {0} nicht gefunden\n" +
                        "Bitte wählen Sie das zu verwendende Konto aus.",iban == null || iban.length() == 0 ? i18n.tr("<unbekannt>") : iban));

      try
      {
        k = (Konto) d.open();
      }
      catch (OperationCanceledException oce)
      {
        throw new ApplicationException(i18n.tr("Auftrag wird übersprungen"));
      }
      catch (Exception e)
      {
        throw new ApplicationException(i18n.tr("Fehler beim Auswählen des Kontos"),e);
      }
    }

    if (k != null)
    {
      kontenCache.put(iban,k);
      return k;
    }
    throw new ApplicationException(i18n.tr("Kein Konto ausgewählt"));
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  @Override
  public String getName()
  {
    return i18n.tr("SEPA-XML");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    // Kein Typ angegeben?
    if (objectType == null)
      return null;

    for (int i=0;i<supported.length;++i)
    {
      if (objectType.equals(supported[i]))
        return new IOFormat[] { new MyIOFormat(objectType) };
    }
    return null;
  }

  /**
   * Hilfsklasse, damit wir uns den Objekt-Typ merken koennen.
   * @author willuhn
   */
  class MyIOFormat implements IOFormat
  {
    Class type = null;
    
    /**
     * ct.
     * @param type
     */
    private MyIOFormat(Class type)
    {
      this.type = type;
    }

    /**
     * @see de.willuhn.jameica.hbci.io.IOFormat#getName()
     */
    public String getName()
    {
      return SepaTransferImporter.this.getName();
    }

    /**
     * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
     */
    public String[] getFileExtensions()
    {
      return new String[] {"*.xml"};
    }
  }
}

