/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/MT940UmsatzExporter.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/01/12 17:46:30 $
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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
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
  private final static String NL      = "\r\n";
  private final static String CHARSET = "iso-8859-1";
  
  private final static DateFormat DF_YYMMDD = new SimpleDateFormat("yyMMdd");
  private final static DateFormat DF_MMDD   = new SimpleDateFormat("MMdd");
  
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
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

      
      for (int i=0;i<objects.length;++i)
      {
        if (monitor != null)  
        	monitor.setPercentComplete((int)((i) * factor));
        
        if (objects[i] == null || !(objects[i] instanceof Umsatz))
          continue;

        Object name = BeanUtil.toString(objects[i]);
        
        if (name != null && monitor != null)
          monitor.log(i18n.tr("Speichere Datensatz {0}",name.toString()));
        
    		Umsatz u    = (Umsatz) objects[i];
    		Konto k     = u.getKonto();
    		String curr = k.getWaehrung();

    		out.write(":25:" + k.getBLZ() + "/" + k.getKontonummer() + curr + NL);
    		
    		//(Schlusssaldo - Umsatzbetrag) > 0 -> Soll-Haben-Kennung für den Anfangssaldo = C
    		//(Credit), sonst D (Debit)
    		double anfangsSaldo = u.getSaldo() - u.getBetrag();
    		
    		//Anfangssaldo aus dem Schlusssaldo ermitteln sowie Soll-Haben-Kennung
    		//Valuta Datum des Kontosaldos leider nicht verfügbar, deswegen wird Datum der Umsatzwertstellung genommen
        out.write(":60F:");
    		out.write(anfangsSaldo >= 0.0d ? "C" : "D");
    		out.write(DF_YYMMDD.format(u.getDatum()) + curr + HBCI.DECIMALFORMAT.format(anfangsSaldo).replace("-","") + NL);


        out.write(":61:" + DF_YYMMDD.format(u.getValuta()) + DF_MMDD.format(u.getDatum()));

        // Soll-Haben-Kennung für den Betrag ermitteln
    		double betrag = u.getBetrag();
    		if (betrag >= 0.0d)
    		  out.write("CR" + HBCI.DECIMALFORMAT.format(betrag));
    		else
          out.write("DR" + HBCI.DECIMALFORMAT.format(betrag).replace("-",""));
    		
    		out.write("NTRF" + notNull(u.getCustomerRef(),"NONREF") + NL);

    		out.write(":86:?00" + notNull(u.getArt(),"") + "?10" + notNull(u.getPrimanota(),""));
    		
    		//Verwendungszweck 1
    		String s1   = u.getZweck();
    		String s2   = u.getZweck2();
    		String[] s3 = u.getWeitereVerwendungszwecke();
    		
    		if (s1 != null && s1.length() > 0)
    		  out.write("?20" + s1);
    			
        if (s2 != null && s2.length() > 0)
          out.write("?21" + s2);

        if (s3 != null)
        {
          for (int j=0;j<s3.length;++j)
          {
            out.write("?2" + (j+2) + s3[j]);
          }
        }

        String blz = u.getGegenkontoBLZ();
        String kto = u.getGegenkontoNummer();
        String nam = u.getGegenkontoName();
        if (blz != null) out.write("?30" + blz);
        if (kto != null) out.write("?31" + kto);
        if (nam != null) out.write("?32" + nam);

        out.write(NL);
    		

        out.write(":62F:");
        //Soll-Haben-Kennung für den Schlusssaldo ermitteln
    		double schlussSaldo = u.getSaldo();
    		out.write(schlussSaldo >= 0.0d ? "C" : "D");
    		out.write(DF_YYMMDD.format(u.getDatum()) + curr + HBCI.DECIMALFORMAT.format(schlussSaldo).replace("-",""));
    		
    		out.write(NL + "-" + NL);
      }
    }
    catch (IOException e)
    {
      Logger.error("unable to write MT940 file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Export der Daten. " + e.getMessage()));
    }
    finally
    {
      if (monitor != null)
        monitor.setStatusText(i18n.tr("Schliesse Export-Datei"));

      if (out != null)
      {
        try
        {
          if (out != null) 
            out.close();
        }
        catch (Exception e)
        {
          Logger.error("unable to close file",e);
        }
      }
    }
  }

  /**
   * Prueft, ob der String NULL oder leer ist und liefert dann def zurueck. 
   * @param s der zu testendes String.
   * @param def der Default-Wert.
   * @return Ergebnis.
   */
  private String notNull(String s, String def)
  {
    if (s != null && s.length() > 0)
      return s;
    return def;
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
        return i18n.tr("Swift MT940-Format");
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
    return i18n.tr("MT940-Format");
  }

  
  /**
   * Ableitung von OutputStreamWriter, um die Umlaute umzuschreiben.
   */
  private class MyOutputStreamWriter extends OutputStreamWriter
  {
    // TODO: Die Umlaute muessen in SWIFT-Dateien noch gegen "UE", "AE", usw. ersetzt werden. Zu klaeren, ob das korrekt ist.
    private String[] search  = new String[]{"Ü", "Ö", "Ä", "ü", "ö", "ä", "ß"};
    private String[] replace = new String[]{"UE","OE","AE","ue","oe","ae","ss"};
    
    /**
     * ct.
     * @param out
     * @throws UnsupportedEncodingException
     */
    public MyOutputStreamWriter(OutputStream out) throws UnsupportedEncodingException
    {
      super(out,CHARSET);
    }

    /**
     * @see java.io.Writer#write(java.lang.String)
     */
    public void write(String str) throws IOException
    {
      // Umlaute ersetzen.
      String s = StringUtils.replaceEach(str,search,replace);
      super.write(s);
    }
  }
}


/*********************************************************************
 * $Log: MT940UmsatzExporter.java,v $
 * Revision 1.4  2011/01/12 17:46:30  willuhn
 * @B Zeiger im Array fehlte
 *
 * Revision 1.3  2011-01-12 17:39:46  willuhn
 * @B "-" entfernen
 *
 * Revision 1.2  2011-01-12 17:37:43  willuhn
 * @C MT940-Import und -Export sollten den gleichen Namen tragen
 *
 * Revision 1.1  2011-01-05 00:10:11  willuhn
 * @N BUGZILLA 669 - MT940-Exporter fuer Umsaetze - basierend auf dem Code von Andre. Noch zu testen!
 *
 **********************************************************************/