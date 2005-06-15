/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/CSVExporter.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/06/15 16:10:48 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Exporters fuer das CSV-Format.
 */
public class CSVExporter implements Exporter
{

  private final static char DELIMTITER = ';';

  private String header = null;

  private I18N i18n = null;
  
  /**
   * ct.
   */
  public CSVExporter()
  {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    StringBuffer header = new StringBuffer();
    header.append(i18n.tr("#"));
    header.append(DELIMTITER);
    header.append(i18n.tr("Eigene Kontonummer"));
    header.append(DELIMTITER);
    header.append(i18n.tr("Eigene Bankleitzahl"));
    header.append(DELIMTITER);
    header.append(i18n.tr("Konto-Bezeichnung"));
    header.append(DELIMTITER);
    header.append(i18n.tr("Nummer des Gegenkontos"));
    header.append(DELIMTITER);
    header.append(i18n.tr("Bankleitzahl des Gegenkontos"));
    header.append(DELIMTITER);
    header.append(i18n.tr("Kontoinhaber des Gegenkontos"));
    header.append(DELIMTITER);
    header.append(i18n.tr("Betrag"));
    header.append(DELIMTITER);
    header.append(i18n.tr("Valuta-Datum"));
    header.append(DELIMTITER);
    header.append(i18n.tr("Buchungs-Datum"));
    header.append(DELIMTITER);
    header.append(i18n.tr("Verwendungszweck Zeile 1"));
    header.append(DELIMTITER);
    header.append(i18n.tr("Verwendungszweck Zeile 2"));
    header.append(DELIMTITER);
    header.append(i18n.tr("Saldo nach der Buchung"));
    header.append(DELIMTITER);
    header.append(i18n.tr("Primanota"));
    header.append(DELIMTITER);
    header.append(i18n.tr("Kunden-Referenz"));
    this.header = header.toString();
  }

  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#export(de.willuhn.jameica.hbci.rmi.Umsatz[], java.io.OutputStream)
   */
  public void export(Umsatz[] umsaetze, OutputStream os) throws RemoteException, ApplicationException
  {
    if (os == null)
      throw new ApplicationException(i18n.tr("Kein Ausgabe-Ziel für die CSV-Datei angegeben"));

    if (umsaetze == null || umsaetze.length == 0)
      throw new ApplicationException(i18n.tr("Keine zu exportierenden Umsätze angegeben"));

    BufferedWriter writer = null;
    try
    {
      writer = new BufferedWriter(new OutputStreamWriter(os));

      writer.write(header.toString());
      writer.newLine();

      Umsatz current = null;
      Konto konto    = null;
      for (int i=0;i<umsaetze.length;++i)
      {
        current = umsaetze[i];
        konto = current.getKonto();

        StringBuffer sb = new StringBuffer();
        sb.append((i+1)); // Fortlaufende Nummer
        sb.append(DELIMTITER);
        sb.append(konto.getKontonummer());
        sb.append(DELIMTITER);
        sb.append(konto.getBLZ());
        sb.append(DELIMTITER);
        sb.append(konto.getBezeichnung());
        sb.append(DELIMTITER);
        sb.append(current.getEmpfaengerKonto());
        sb.append(DELIMTITER);
        sb.append(current.getEmpfaengerBLZ());
        sb.append(DELIMTITER);
        sb.append(current.getEmpfaengerName());
        sb.append(DELIMTITER);
        sb.append(HBCI.DECIMALFORMAT.format(current.getBetrag()));
        sb.append(DELIMTITER);
        sb.append(HBCI.DATEFORMAT.format(current.getValuta()));
        sb.append(DELIMTITER);
        sb.append(HBCI.DATEFORMAT.format(current.getDatum()));
        sb.append(DELIMTITER);
        sb.append(current.getZweck());
        sb.append(DELIMTITER);
        sb.append(current.getZweck2());
        sb.append(DELIMTITER);
        sb.append(HBCI.DECIMALFORMAT.format(current.getSaldo()));
        sb.append(DELIMTITER);
        sb.append(current.getPrimanota());
        sb.append(DELIMTITER);
        sb.append(current.getCustomerRef());

        writer.write(sb.toString());
        writer.newLine();
      }
    }
    catch (IOException e)
    {
      Logger.error("error while writing into csv file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Schreiben in die CSV-Datei"));
    }
    finally
    {
      if (writer != null)
      {
        try
        {
          writer.close();
        }
        catch (Exception e)
        {
          // useless
        }
      }
    }
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return "CSV-Export";
  }

}


/**********************************************************************
 * $Log: CSVExporter.java,v $
 * Revision 1.2  2005/06/15 16:10:48  web0
 * @B javadoc fixes
 *
 * Revision 1.1  2005/06/08 16:48:54  web0
 * @N new Import/Export-System
 *
 * Revision 1.2  2005/06/06 10:37:01  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/06/02 21:48:44  web0
 * @N Exporter-Package
 * @N CSV-Exporter
 *
 **********************************************************************/