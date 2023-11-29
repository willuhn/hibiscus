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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.hbci.gui.ext.ExportSaldoExtension;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Exportiert Umsaetze im MT940-Format, fasst hierbei jedoch alle Buchungen zu einer logischen MT940-Datei zusammen.
 */
public class MT940UmsatzExporterMerged extends MT940UmsatzExporter
{
  @Override
  public void doExport(Object[] objects, IOFormat format,OutputStream os, final ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    OutputStreamWriter out = null;
    
    try
    {
      out = new MyOutputStreamWriter(os);

      double factor = 1;
      if (monitor != null)
      {
        factor = ((double)(100 - monitor.getPercentComplete())) / objects.length;
        monitor.setStatusText(i18n.tr("Exportiere Daten"));
      }

      //////////////////////////////////////////////////////////////////////////
      // Wir sortieren die Buchungen vorher noch chronologisch. Und checken bei
      // der Gelegenheit auch gleich, dass die Buchungen alle vom selben Konto
      // stammen
      Konto k = null;
      List<Umsatz> list = new LinkedList<Umsatz>();
      for (int i=0;i<objects.length;++i)
      {
        if (objects[i] == null || !(objects[i] instanceof Umsatz))
          continue;
        
        Umsatz u = (Umsatz) objects[i];
        Konto konto = u.getKonto();
        
        if (k == null)
          k = konto;
        else if (!k.getID().equals(konto.getID()))
          throw new ApplicationException(i18n.tr("Die zu exportierenden Umsätze müssen vom selben Konto stammen"));
        list.add(u);
      }
      sort(list);
      //////////////////////////////////////////////////////////////////////////

      String curr         = k.getWaehrung();
      Boolean b           = (Boolean) Exporter.SESSION.get(ExportSaldoExtension.KEY_SALDO_SHOW);
      boolean showSaldo   = (b == null || b.booleanValue());

      out.write(":20:STARTUMS" + NL);
      out.write(":25:" + k.getBLZ() + "/" + k.getKontonummer() + NL);
      out.write(":28C:1" + NL); // Auszugsnummer. Belegen wir hart mit "1", damit das Feld vorhanden ist. SAP braucht das fuer den Import

      for (int i=0;i<list.size();++i)
      {
        if (monitor != null)  
        	monitor.setPercentComplete((int)((i) * factor));
        
        Umsatz u = list.get(i);
        Object name = BeanUtil.toString(u);
        
        if (name != null && monitor != null)
          monitor.log(i18n.tr("Speichere Datensatz {0}",name.toString()));
        
        // Anfangssaldo
    		if (showSaldo && i == 0)
    		{
          //(Schlusssaldo - Umsatzbetrag) > 0 -> Soll-Haben-Kennung für den Anfangssaldo = C
          //(Credit), sonst D (Debit)
          double saldo = u.getSaldo() - u.getBetrag();
          
          //Anfangssaldo aus dem Schlusssaldo ermitteln sowie Soll-Haben-Kennung
          //Valuta Datum des Kontosaldos leider nicht verfügbar, deswegen wird Datum der Umsatzwertstellung genommen
          out.write(":60F:");
          out.write(saldo >= 0.0d ? "C" : "D");
          out.write(DF_YYMMDD.format(u.getDatum()) + curr + DECF.format(saldo).replace("-","") + NL);
    		}

        out.write(":61:" + DF_YYMMDD.format(u.getValuta()) + DF_MMDD.format(u.getDatum()));

        // Soll-Haben-Kennung für den Betrag ermitteln
    		double betrag = u.getBetrag();
        out.write(betrag >= 0.0d ? "CR" : "DR");
        out.write(DECF.format(betrag).replace("-",""));
    		
        String ref = StringUtils.trimToNull(u.getCustomerRef());
    		out.write("NTRF" + (ref != null ? ref : "NONREF") + NL);

    		String gvcode = u.getGvCode();
    		
      	// Fallback, wenn wir keinen GV-Code haben. Das trifft u.a. bei Alt-Umsaetzen
    		// auf, als Hibiscus das Feld noch nicht unterstuetzte.
    		if (StringUtils.trimToNull(gvcode) == null)
      		gvcode = betrag >= 0.0d? "051" : "020";
    		
    		out.write(":86:" + gvcode + "?00" + StringUtils.trimToEmpty(u.getArt()) + "?10" + StringUtils.trimToEmpty(u.getPrimanota()));

        int m = 0;

        // Verwendungszweck
        // Nur dann mit dem Tag versehen, wenn das nicht bereits der Fall ist
        final String parsed = VerwendungszweckUtil.getTag(u,VerwendungszweckUtil.Tag.SVWZ);
        final String raw    = VerwendungszweckUtil.toString(u);
        final boolean hasTags = !Objects.equals(parsed,raw) && raw.contains(VerwendungszweckUtil.Tag.SVWZ.name());
        
        // in MT940 sind nur max. 10 Zeilen zugelassen. Die restlichen muessen wir
        // ignorieren. Siehe FinTS_3.0_Messages_Finanzdatenformate_2010-08-06_final_version.pdf
        // (Seite 179, strukturierte Belegung des Feldes 86)
        if (hasTags) // Tags sind schon im Verwendungszweck - dann übernehmen wir das so
        {
          String[] lines = VerwendungszweckUtil.rewrap(27,VerwendungszweckUtil.toArray(u));
          for (m=0;m<lines.length;++m)
          {
            if (m > 9)
              break;
            out.write("?2" + Integer.toString(m) + lines[m]);
          }
        }
        else // Keine Tags. Dann schreiben wir sie selbst rein
        {
          String[] lines = VerwendungszweckUtil.rewrap(27,parsed);
          for (int l=0;l<lines.length;++l)
          {
            if (l > 9)
              break;
            m = addRef(out,m,VerwendungszweckUtil.Tag.SVWZ,lines[l]);
          }
        }

        m = addRef(out,m,VerwendungszweckUtil.Tag.EREF,u.getEndToEndId());
        m = addRef(out,m,VerwendungszweckUtil.Tag.KREF,u.getCustomerRef());
        m = addRef(out,m,VerwendungszweckUtil.Tag.MREF,u.getMandateId());
        m = addRef(out,m,VerwendungszweckUtil.Tag.CRED,u.getCreditorId());
        
        String blz = StringUtils.trimToNull(u.getGegenkontoBLZ());
        String kto = StringUtils.trimToNull(u.getGegenkontoNummer());
        String nam = StringUtils.trimToNull(u.getGegenkontoName());
        String add = StringUtils.trimToNull(u.getAddKey());
        if (blz != null) out.write("?30" + blz);
        if (kto != null) out.write("?31" + kto);
        if (nam != null) out.write("?32" + nam);
        if (add != null) out.write("?34" + add);

        out.write(NL);
    		

        // Schluss-Saldo
        if (showSaldo && i >= list.size() - 1)
        {
          out.write(":62F:");
          //Soll-Haben-Kennung für den Schlusssaldo ermitteln
          double schlussSaldo = u.getSaldo();
          out.write(schlussSaldo >= 0.0d ? "C" : "D");
          out.write(DF_YYMMDD.format(u.getDatum()) + curr + DECF.format(schlussSaldo).replace("-","") + NL);
        }
    		
      }
      
      out.write("-");
      out.write(NL);
    }
    catch (IOException e)
    {
      Logger.error("unable to write MT940 file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Export der Daten. " + e.getMessage()));
    }
    finally
    {
      IOUtil.close(out);
    }
  }

  @Override
  public String getName()
  {
    return i18n.tr("Swift MT940-Format (alle Buchungen in einer logischen Datei)");
  }
}
