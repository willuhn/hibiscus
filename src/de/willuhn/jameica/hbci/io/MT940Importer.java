/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/MT940Importer.java,v $
 * $Revision: 1.4 $
 * $Date: 2006/01/23 12:16:57 $
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.kapott.hbci.GV_Result.GVRKUms;
import org.kapott.hbci.manager.HBCIUtilsInternal;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Saldo;
import org.kapott.hbci.structures.Value;
import org.kapott.hbci.swift.Swift;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.hbci.server.FilterEngine;
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
    // Quick&Dirty-Loesung.
    // Code kopiert von. GVKUmsAll aus HBCI4Java.
    // Das koennte man in der Tat mal schoen machen ;)

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");

    try
    {
      
      de.willuhn.jameica.hbci.rmi.Konto konto = null;
      
      if (context != null && context instanceof de.willuhn.jameica.hbci.rmi.Konto)
        konto = (de.willuhn.jameica.hbci.rmi.Konto) context;
      
      // Wir erzeugen das HBCI4Java-Umsatz-Objekt selbst. Dann muessen wir
      // an der eigentlichen Parser-Routine nichts mehr aendern.
      GVRKUms umsaetze = new GVRKUms();

      if (monitor != null)
      {
        monitor.setStatusText(i18n.tr("Lese Datei ein"));
        monitor.addPercentComplete(1);
      }

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
      String booked = buffer.toString();

      int count = 1;
      // split into "buchungstage"
      while (booked.length() != 0)
      {

        String st_tag = Swift.getOneBlock(booked);
        if (st_tag == null)
        {
          break;
        }

        GVRKUms.BTag btag=new GVRKUms.BTag();
        
        ////////////////////////////////////////////////////////////////////////
        // 1) Konto ermitteln
        // extract konto data
        String konto_info=Swift.getTagValue(st_tag,"25",0);
        int pos=konto_info.indexOf("/");
        String blz    = null;
        String number = null;
        
        if (pos!=-1)
        {
          blz=konto_info.substring(0,pos);
          number=konto_info.substring(pos+1);
            
          for (pos=number.length();pos>0;pos--)
          {
            char ch=number.charAt(pos-1);
                
            if (ch>='0' && ch<='9')
              break;
          }
            
          if (pos<number.length())
          {
            number=number.substring(0,pos);
          }
        }
        else
        {
          number=konto_info;
        }
        
        btag.my=new Konto();
        btag.my.blz=blz;
        btag.my.number=number;
        

        if (konto == null)
        {
          if (monitor != null)
          {
            monitor.setStatusText(i18n.tr("Ermittle Konto"));
            monitor.addPercentComplete(1);
          }

          DBIterator konten = Settings.getDBService().createList(de.willuhn.jameica.hbci.rmi.Konto.class);
          konten.addFilter("kontonummer = '" + number + "'");
          konten.addFilter("blz = '" + blz + "'");
          
          // Wir nehmen das erste, das wir finden
          if (konten.size() > 0)
          {
            konto = (de.willuhn.jameica.hbci.rmi.Konto) konten.next();
          }
          else
          {
            KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_CENTER);
            d.setText(i18n.tr("Bitte wählen Sie das Konto aus, für das die Buchungen importiert werden sollen."));
            try
            {
              konto = (de.willuhn.jameica.hbci.rmi.Konto) d.open();
            }
            catch (OperationCanceledException oce)
            {
              throw oce;
            }
            catch (Exception e)
            {
              Logger.error("error while choosing konto",e);
              throw new ApplicationException(i18n.tr("Fehler bei der Auswahl des Kontos"));
            }
          }
        }

        // Konto immer noch null?
        if (konto == null)
          throw new ApplicationException(i18n.tr("Kein Konto ausgewählt"));

        ////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////////////////
        // extract "anfangssaldo"
        // extract "auszugsnummer"
        btag.counter=Swift.getTagValue(st_tag,"28C",0);
        
        // extract "anfangssaldo"
        btag.start=new Saldo();
        
        String st_start=Swift.getTagValue(st_tag,"60F",0);
        btag.starttype='F';
        if (st_start==null) {
            st_start=Swift.getTagValue(st_tag,"60M",0);
            btag.starttype='M';
        }
        String cd=st_start.substring(0,1);
        
        int next=0;
        if (st_start.charAt(2)>'9') {
            btag.start.timestamp=null;
            next=2;
        } else {
            btag.start.timestamp=dateFormat.parse(st_start.substring(1,7));
            next=7;
        }
        
        // hier aus dem CD-Indikator und dem absoluten Saldo-Betrag
        // einen String für den Saldo-Betrag zusamennbauen
        btag.start.value=new Value(
            (cd.equals("D")?"-":"")+st_start.substring(next+3).replace(',','.'),
            st_start.substring(next,next+3));
        
        // looping to get all "umsaetze"
        long saldo=Math.round(btag.start.value.getLongValue());
        int  ums_counter=0;
        
        if (monitor != null)
        {
          monitor.addPercentComplete(1);
        }
        
        while (true) {

          if (monitor != null)
          {
            monitor.log(i18n.tr("Datensatz {0}",""+(count++)));
            monitor.addPercentComplete(1);
          }

            String st_ums=Swift.getTagValue(st_tag,"61",ums_counter);
            if (st_ums==null)
                break;
            
            GVRKUms.UmsLine line=new GVRKUms.UmsLine();
            
            // extract valuta
            line.valuta=dateFormat.parse(st_ums.substring(0,6));
            
            // extract bdate
            next=0;
            if (st_ums.charAt(6)>'9') {
                line.bdate=line.valuta;
                next=6;
            } else {
                line.bdate=dateFormat.parse(st_ums.substring(0,2)+
                    st_ums.substring(6,10));
                
                // wenn bdate und valuta um mehr als einen monat voneinander
                // abweichen, dann ist das jahr des bdate falsch (1.1.2005 vs. 31.12.2004)
                // korrektur des bdate-jahres in die richtige richtung notwendig
                if (Math.abs(line.bdate.getTime()-line.valuta.getTime())>30L*24*3600*1000) {
                    int diff;
                    
                    if (line.bdate.before(line.valuta)) {
                        diff=+1;
                    } else {
                        diff=-1;
                    }
                    Calendar cal=Calendar.getInstance();
                    cal.setTime(line.bdate);
                    cal.set(Calendar.YEAR,cal.get(Calendar.YEAR)+diff);
                    line.bdate=cal.getTime();
                }
                
                next=10;
            }
            
            // extract credit/debit
            if (st_ums.charAt(next)=='C' || st_ums.charAt(next)=='D') {
                cd=st_ums.substring(next,next+1);
                next++;
            } else {
                cd=st_ums.substring(next+1,next+2);
                next+=2;
            }
            
            // skip part of currency
            char currpart=st_ums.charAt(next);
            if (currpart>'9')
                next++;
            
            line.value=new Value();
            line.value.setCurr(btag.start.value.getCurr());
            
            // extract value and skip code
            int npos=st_ums.indexOf("N",next);
            line.value.setValue(
                HBCIUtilsInternal.string2Long(
                    (cd.equals("D")?"-":"") + st_ums.substring(next,npos).replace(',','.'), 
                    100));
            next=npos+4;
            
            // update saldo
            saldo+=line.value.getLongValue();
            line.saldo=new Saldo();
            line.saldo.timestamp=line.bdate;
            line.saldo.value=new Value(saldo,btag.start.value.getCurr());
            
            // extract customerref
            npos=st_ums.indexOf("//",next);
            if (npos==-1)
                npos=st_ums.indexOf("\r\n",next);
            if (npos==-1)
                npos=st_ums.length();
            line.customerref=st_ums.substring(next,npos);
            next=npos;
            
            // check for instref
            if (next<st_ums.length() && st_ums.substring(next,next+2).equals("//")) {
                // extract instref
                next+=2;
                npos=st_ums.indexOf("\r\n",next);
                if (npos==-1)
                    npos=st_ums.length();
                line.instref=st_ums.substring(next,npos);
                next=npos+2;
            }
            if (line.instref==null)
                line.instref="";
            
            // check for additional information
            if (next<st_ums.length() && st_ums.charAt(next)=='\r') {
                next+=2;
                
                // extract orig Value
                pos=st_ums.indexOf("/OCMT/",next);
                if (pos!=-1) {
                    int slashpos=st_ums.indexOf("/",pos+9);
                    if (slashpos==-1)
                        slashpos=st_ums.length();
                    
                    line.orig_value=new Value(
                        st_ums.substring(pos+9,slashpos).replace(',','.'),
                        st_ums.substring(pos+6,pos+9));
                }
                
                // extract charge Value
                pos=st_ums.indexOf("/CHGS/",next);
                if (pos!=-1) {
                    int slashpos=st_ums.indexOf("/",pos+9);
                    if (slashpos==-1)
                        slashpos=st_ums.length();
                    
                    line.charge_value=new Value(
                        st_ums.substring(pos+9,slashpos).replace(',','.'),
                        st_ums.substring(pos+6,pos+9));
                }
            }
            
            String st_multi=Swift.getTagValue(st_tag,"86",ums_counter);
            if (st_multi!=null) {
                line.gvcode=st_multi.substring(0,3);
                st_multi=Swift.packMulti(st_multi.substring(3));
                
                if (!line.gvcode.equals("999")) {
                    line.text=Swift.getMultiTagValue(st_multi,"00");
                    line.primanota=Swift.getMultiTagValue(st_multi,"10");
                    for (int i=0;i<10;i++) {
                        line.addUsage(Swift.getMultiTagValue(st_multi,Integer.toString(20+i)));
                    }
                    
                    Konto acc=new Konto();
                    acc.blz=Swift.getMultiTagValue(st_multi,"30");
                    acc.number=Swift.getMultiTagValue(st_multi,"31");
                    acc.name=Swift.getMultiTagValue(st_multi,"32");
                    acc.name2=Swift.getMultiTagValue(st_multi,"33");
                    if (acc.blz!=null ||
                            acc.number!=null ||
                            acc.name!=null ||
                            acc.name2!=null) {
                        
                        if (acc.blz==null)
                            acc.blz="";
                        if (acc.number==null)
                            acc.number="";
                        if (acc.name==null)
                            acc.name="";
                        line.other=acc;
                    }
                    
                    line.addkey=Swift.getMultiTagValue(st_multi,"34");
                    for (int i=0;i<4;i++) {
                        line.addUsage(Swift.getMultiTagValue(st_multi,Integer.toString(60+i)));
                    }
                } else {
                    line.additional=st_multi;
                }
            }
            
            btag.addLine(line);
            ums_counter++;
        }
        
        // extract "schlusssaldo"
        btag.end=new Saldo();
        
        String st_end=Swift.getTagValue(st_tag,"62F",0);
        btag.endtype='F';
        if (st_end==null) {
            st_end=Swift.getTagValue(st_tag,"62M",0);
            btag.endtype='M';
        }
        cd=st_end.substring(0,1);
        
        next=0;
        if (st_end.charAt(2)>'9') {
            btag.end.timestamp=null;
            next=2;
        } else {
            btag.end.timestamp=dateFormat.parse(st_end.substring(1,7));
            next=7;
        }
        
        // set default values for optional non-given bdates
        if (btag.start.timestamp==null) {
            btag.start.timestamp=btag.end.timestamp;
        }
        for (int j=0;j<btag.lines.length;j++) {
            if (btag.lines[j].bdate==null) {
                btag.lines[j].bdate=btag.end.timestamp;
            }
        }
        
        btag.end.value=new Value(
            (cd.equals("D")?"-":"")+st_end.substring(next+3).replace(',','.'),
            st_end.substring(next,next+3));
        
        umsaetze.addTag(btag);
        buffer.delete(0, st_tag.length());
        booked = buffer.toString();
      }

      // Wir vergleichen noch mit den Umsaetzen, die wir schon haben und
      // speichern nur die neuen.
      // TODO: Der Code ist nahezu 1:1 aus HBCIUmsatzJob kopiert. Koennte man mal noch mergen

      if (monitor != null)
      {
        monitor.setStatusText(i18n.tr("Speichere Umsätze"));
        monitor.addPercentComplete(1);
      }

      DBIterator existing = konto.getUmsaetze();
      GVRKUms.UmsLine[] lines = umsaetze.getFlatData();
      
      if (lines.length == 0)
      {
        konto.addToProtokoll(i18n.tr("Keine Umsätze importiert"),Protokoll.TYP_ERROR);
        return;
      }
      
      Umsatz umsatz = null;
      
      int created = 0;
      int skipped = 0;
      // Eine Transaktion beim Speichern brauchen wir nicht, weil beim
      // naechsten Import die schon importierten erkannt und uebersprungen werden.
      for (int i=0;i<lines.length;++i)
      {
        if (monitor != null)
        {
          monitor.log(i18n.tr("Umsatz {0}", "" + (i+1)));
          monitor.addPercentComplete(1);
        }
        umsatz = Converter.HBCIUmsatz2HibiscusUmsatz(lines[i]);
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
              FilterEngine.getInstance().filter(umsatz,lines[i]);
            }
            catch (Exception e)
            {
              Logger.error("error while filtering umsatz",e);
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
    }
    catch (IOException ioe)
    {
      Logger.error("error while reading file",ioe);
      throw new ApplicationException(i18n.tr("Fehler beim Import der Swift-Datei"));
    }
    catch (ParseException pe)
    {
      Logger.error("error while parsing file",pe);
      throw new ApplicationException(i18n.tr("Fehler beim Lesen der Swift-Datei"));
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
    IOFormat f = new IOFormat() {
      public String getName()
      {
        return i18n.tr("Swift MT-940");
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtension()
       */
      public String getFileExtension()
      {
        return "sta";
      }
    };
    return new IOFormat[] { f };
  }
}

/*******************************************************************************
 * $Log: MT940Importer.java,v $
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