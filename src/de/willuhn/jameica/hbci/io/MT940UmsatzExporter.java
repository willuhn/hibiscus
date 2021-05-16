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
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.ext.ExportSaldoExtension;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil.Tag;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Exportiert Umsaetze im MT940-Format.
 */
public class MT940UmsatzExporter implements Exporter
{
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  protected final static String NL            = "\r\n";
  protected final static DateFormat DF_YYMMDD = new SimpleDateFormat("yyMMdd");
  protected final static DateFormat DF_MMDD   = new SimpleDateFormat("MMdd");
  
  protected final static DecimalFormat DECF = new DecimalFormat("###,###,##0.00",new DecimalFormatSymbols(Locale.GERMANY));
  
  static
  {
    DECF.setGroupingUsed(false);
  }
  
  /**
   * MT940-Zeichensatz.
   * Ist eigentlich nicht noetig, weil Swift nur ein Subset von ISO-8859
   * zulaesst, welches so klein ist, dass es im Wesentlichen US-ASCII ist
   * und damit der Zeichensatz so ziemlich egal ist. Aber wir tolerieren
   * die Umlaute wenigstens beim Import.
   */
  public final static String CHARSET  = "iso-8859-1";

  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(java.lang.Object[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
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
      List<Umsatz> list = new LinkedList<Umsatz>();
      for (Object o : objects)
      {
        if (o == null || !(o instanceof Umsatz))
          continue;

        list.add((Umsatz) o);
      }
      sort(list);
      //////////////////////////////////////////////////////////////////////////


      Boolean b         = (Boolean) Exporter.SESSION.get(ExportSaldoExtension.KEY_SALDO_SHOW);
      boolean showSaldo = (b == null || b.booleanValue());
      
      for (int i=0;i<list.size();++i)
      {
        if (monitor != null)  
        	monitor.setPercentComplete((int)((i) * factor));
        
        Umsatz u = list.get(i);
        Object name = BeanUtil.toString(u);
        
        if (name != null && monitor != null)
          monitor.log(i18n.tr("Speichere Datensatz {0}",name.toString()));
        
    		Konto k     = u.getKonto();
    		String curr = k.getWaehrung();

    		if (i > 0)
    		  out.write(NL);
    		
        out.write(":20:Hibiscus" + NL);
    		out.write(":25:" + k.getBLZ() + "/" + k.getKontonummer() + NL);
        out.write(":28C:1" + NL); // Auszugsnummer. Belegen wir hart mit "1", damit das Feld vorhanden ist. SAP braucht das fuer den Import
    		
    		if (showSaldo)
    		{
          //(Schlusssaldo - Umsatzbetrag) > 0 -> Soll-Haben-Kennung für den Anfangssaldo = C
          //(Credit), sonst D (Debit)
          double anfangsSaldo = u.getSaldo() - u.getBetrag();
          
          //Anfangssaldo aus dem Schlusssaldo ermitteln sowie Soll-Haben-Kennung
          //Valuta Datum des Kontosaldos leider nicht verfügbar, deswegen wird Datum der Umsatzwertstellung genommen
          out.write(":60F:");
          out.write(anfangsSaldo >= 0.0d ? "C" : "D");
          out.write(DF_YYMMDD.format(u.getDatum()) + curr + DECF.format(anfangsSaldo).replace("-","") + NL);
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
    		
    		//Verwendungszweck
    		String[] lines = VerwendungszweckUtil.rewrap(65,VerwendungszweckUtil.toArray(u));
    		int m = 0;
    		for (m=0;m<lines.length;++m)
    		{
      		// in MT940 sind nur max. 10 Zeilen zugelassen. Die restlichen muessen wir
    		  // ignorieren. Siehe FinTS_3.0_Messages_Finanzdatenformate_2010-08-06_final_version.pdf
    		  // (Seite 179, strukturierte Belegung des Feldes 86)
    		  if (m > 9)
    		    break;
          out.write("?2" + Integer.toString(m) + lines[m]);
    		}
    		
        m = addRef(out,m,VerwendungszweckUtil.Tag.EREF,u.getEndToEndId());
        m = addRef(out,m,VerwendungszweckUtil.Tag.KREF,u.getCustomerRef());
        m = addRef(out,m,VerwendungszweckUtil.Tag.MREF,u.getMandateId());

        String blz = StringUtils.trimToNull(u.getGegenkontoBLZ());
        String kto = StringUtils.trimToNull(u.getGegenkontoNummer());
        String nam = StringUtils.trimToNull(u.getGegenkontoName());
        String add = StringUtils.trimToNull(u.getAddKey());
        if (blz != null) out.write("?30" + blz);
        if (kto != null) out.write("?31" + kto);
        if (nam != null) out.write("?32" + nam);
        if (add != null) out.write("?34" + add);

        out.write(NL);
    		

        if (showSaldo)
        {
          out.write(":62F:");
          //Soll-Haben-Kennung für den Schlusssaldo ermitteln
          double schlussSaldo = u.getSaldo();
          out.write(schlussSaldo >= 0.0d ? "C" : "D");
          out.write(DF_YYMMDD.format(u.getDatum()) + curr + DECF.format(schlussSaldo).replace("-","") + NL);
        }
    		
        out.write("-");
      }
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
  
  /**
   * Fuegt das Tag hinzu, insofern noch Platz ist.
   * @param out der OutputStreamWriter.
   * @param m der Counter.
   * @param tag das Tag.
   * @param text der Text.
   * @return der neue Counter-Wert.
   * @throws IOException
   */
  protected int addRef(OutputStreamWriter out, int m, Tag tag, String text) throws IOException
  {
    // Kein Platz mehr
    if (m > 9)
      return m;
    
    // Feld hat keinen Wert.
    text = StringUtils.trimToNull(text);
    if (text == null)
      return m;
    
    out.write("?2" + Integer.toString(m) + tag.name() + "+" + text);
    m++;
    return m;
  }
  
  /**
   * Sortiert die Buchungen chronologisch - aeltestest zuerst.
   * @param list die zu sortierenden Buchungen.
   */
  protected void sort(List<Umsatz> list)
  {
    Collections.sort(list,new Comparator<Umsatz>() {
      /**
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      @Override
      public int compare(Umsatz o1, Umsatz o2)
      {
        try
        {
          Date d1 = (Date) o1.getAttribute("datum_pseudo");
          Date d2 = (Date) o2.getAttribute("datum_pseudo");
          if (d1 == d2)
            return 0;
          if (d1 == null)
            return -1;
          if (d2 == null)
            return 1;
          return d1.compareTo(d2);
        }
        catch (RemoteException re)
        {
          Logger.error("unable to sort data",re);
          return 0;
        }
      }
    });
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (!Umsatz.class.equals(objectType))
      return null;

    return new IOFormat[]{new IOFormat() {
      public String getName()
      {
        return MT940UmsatzExporter.this.getName();
      }
    
      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      public String[] getFileExtensions()
      {
        return new String[]{"sta"};
      }
    }};
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("Swift MT940-Format (pro Buchung eine logische Datei)");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#suppportsExtension(java.lang.String)
   */
  @Override
  public boolean suppportsExtension(String ext)
  {
    return ext != null && ExportSaldoExtension.KEY_SALDO_SHOW.equals(ext);
  }
  
  /**
   * Ableitung von OutputStreamWriter, um die Umlaute umzuschreiben.
   */
  protected class MyOutputStreamWriter extends OutputStreamWriter
  {
    private String[] search  = new String[]{"Ü", "Ö", "Ä", "ü", "ö", "ä", "ß"};
    private String[] replace = new String[]{"UE","OE","AE","ue","oe","ae","ss"};
    private boolean doReplace = true;
    
    /**
     * ct.
     * @param out
     * @throws UnsupportedEncodingException
     */
    public MyOutputStreamWriter(OutputStream out) throws UnsupportedEncodingException
    {
      super(out,CHARSET);
      
      // Umlaute ersetzen. Sind gemaess "FinTS_3.0_Messages_Finanzdatenformate_2010-08-06_final_version.pdf"
      // in SWIFT nicht zulaessig. Wir machen das mal konfigurierbar. Dann kann es
      // der User bei Bedarf deaktivieren
      doReplace = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings().getBoolean("export.mt940.replaceumlauts",true);
    }

    /**
     * @see java.io.Writer#write(java.lang.String)
     */
    public void write(String str) throws IOException
    {
      if (doReplace)
        str = StringUtils.replaceEach(str,search,replace);
      super.write(str);
    }
  }
}
