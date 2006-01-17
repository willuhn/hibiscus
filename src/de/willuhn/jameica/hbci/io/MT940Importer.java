/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/MT940Importer.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/01/17 00:22:36 $
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
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Saldo;
import org.kapott.hbci.structures.Value;
import org.kapott.hbci.swift.Swift;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

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
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(de.willuhn.jameica.hbci.io.IOFormat,java.io.InputStream)
   */
  public void doImport(IOFormat format, InputStream is) throws RemoteException, ApplicationException
  {
    // Quick&Dirty-Loesung.
    // Code kopiert von. GVKUmsAll aus HBCI4Java.
    // Das koennte man in der Tat mal schoen machen ;)

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");

    try
    {

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      int count = 0;
      byte[] buf = new byte[8192];

      while (count != -1)
      {
        count = is.read(buf);
        bos.write(buf, 0, count);
      }
      bos.close();

      StringBuffer buffer = new StringBuffer(Swift.decodeUmlauts(bos.toString()));
      String booked = buffer.toString();

      // split into "buchungstage"
      while (booked.length() != 0)
      {
        String st_tag = Swift.getOneBlock(booked);
        if (st_tag == null)
        {
          break;
        }

        // extract konto data
        String konto_info = Swift.getTagValue(st_tag, "25", 0);
        int pos = konto_info.indexOf("/");
        String blz    = null;
        String number = null;

        if (pos != -1)
        {
          blz = konto_info.substring(0, pos);
          number = konto_info.substring(pos + 1);

          for (pos = number.length(); pos > 0; pos--)
          {
            char ch = number.charAt(pos - 1);

            if (ch >= '0' && ch <= '9')
              break;
          }

          if (pos < number.length())
          {
            number = number.substring(0, pos);
          }
        }
        else
        {
          number = konto_info;
        }

        DBIterator konten = Settings.getDBService().createList(de.willuhn.jameica.hbci.rmi.Konto.class);
        konten.addFilter("kontonummer = '" + number + "'");
        konten.addFilter("blz = '" + blz + "'");
        
        de.willuhn.jameica.hbci.rmi.Konto konto = null;
        if (konten.size() != 1)
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
        else
        {
          konto = (de.willuhn.jameica.hbci.rmi.Konto) konten.next();
        }

        Umsatz umsatz = (Umsatz) Settings.getDBService().createObject(Umsatz.class,null);
        umsatz.setKonto(konto);
        
        // extract "anfangssaldo"
        GVRKUms.BTag btag=new GVRKUms.BTag();
        btag.start = new Saldo();
        
        // TODO: Swift-Import Noch nicht fertig!

        String st_start = Swift.getTagValue(st_tag, "60F", 0);
        btag.starttype = 'F';
        if (st_start == null)
        {
          st_start = Swift.getTagValue(st_tag, "60M", 0);
          btag.starttype = 'M';
        }
        btag.start.cd = st_start.substring(0, 1);

        int next = 0;
        if (st_start.charAt(2) > '9')
        {
          btag.start.timestamp = null;
          next = 2;
        } else
        {
          btag.start.timestamp = dateFormat.parse(st_start.substring(1, 7));
          next = 7;
        }

        btag.start.value.curr = st_start.substring(next, next + 3);
        btag.start.value.value = HBCIUtils.string2Value(st_start.substring(next + 3).replace(',', '.'));

        // looping to get all "umsaetze"
        long saldo = Math.round(btag.start.value.value * 100);
        if (btag.start.cd.equals("D"))
        {
          saldo = -saldo;
        }
        int ums_counter = 0;

        while (true)
        {
          String st_ums = Swift.getTagValue(st_tag, "61", ums_counter);
          if (st_ums == null)
            break;

          GVRKUms.UmsLine line = new GVRKUms.UmsLine();

          // extract valuta
          line.valuta = dateFormat.parse(st_ums.substring(0, 6));
          umsatz.setValuta(line.valuta);

          // extract bdate
          next = 0;
          if (st_ums.charAt(6) > '9')
          {
            line.bdate = line.valuta;
            next = 6;
          }
          else
          {
            line.bdate = dateFormat.parse(st_ums.substring(0, 2) + st_ums.substring(6, 10));

            // wenn das buchungsdatum scheinbar *nach* dem wert-
            // stellungsdatum liegt, wurde für die erzeugung des
            // buchungsdatums das falsche jahr verwendet - das
            // muss also korrigiert werden
            // bsp: bdate=29.12.; valuta=01.01.2005
            // --> alt: bdate=29.12.2005, also korrektur um -1
            if (line.bdate.after(line.valuta))
            {
              Calendar cal = Calendar.getInstance();
              cal.setTime(line.bdate);
              cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);
              line.bdate = cal.getTime();
            }

            next = 10;
          }
          umsatz.setDatum(line.bdate);

          // extract credit/debit
          if (st_ums.charAt(next) == 'C' || st_ums.charAt(next) == 'D')
          {
            line.cd = st_ums.substring(next, next + 1);
            next++;
          } else
          {
            line.cd = st_ums.substring(next, next + 2);
            next += 2;
          }

          // skip part of currency
          char currpart = st_ums.charAt(next);
          if (currpart > '9')
            next++;

          line.value = new Value();
          line.value.curr = btag.start.value.curr;

          // extract value and skip code
          int npos = st_ums.indexOf("N", next);
          line.value.value = HBCIUtils.string2Value(st_ums
              .substring(next, npos).replace(',', '.'));
          next = npos + 4;

          // update saldo
          if (line.cd.charAt(line.cd.length() - 1) == 'C')
          {
            saldo += Math.round(line.value.value * 100);
          } else
          {
            saldo -= Math.round(line.value.value * 100);
          }
          line.saldo = new Saldo();
          line.saldo.cd = (saldo >= 0) ? "C" : "D";
          line.saldo.timestamp = line.bdate;
          line.saldo.value = new Value(Math.abs(saldo / 100.0),
              btag.start.value.curr);

          // extract customerref
          npos = st_ums.indexOf("//", next);
          if (npos == -1)
            npos = st_ums.indexOf("\r\n", next);
          if (npos == -1)
            npos = st_ums.length();
          line.customerref = st_ums.substring(next, npos);
          next = npos;

          // check for instref
          if (next < st_ums.length()
              && st_ums.substring(next, next + 2).equals("//"))
          {
            // extract instref
            next += 2;
            npos = st_ums.indexOf("\r\n", next);
            if (npos == -1)
              npos = st_ums.length();
            line.instref = st_ums.substring(next, npos);
            next = npos + 2;
          }
          if (line.instref == null)
            line.instref = "";

          // check for additional information
          if (next < st_ums.length() && st_ums.charAt(next) == '\r')
          {
            next += 2;

            // extract orig Value
            pos = st_ums.indexOf("/OCMT/", next);
            if (pos != -1)
            {
              line.orig_value = new Value();
              line.orig_value.curr = st_ums.substring(pos + 6, pos + 9);

              int slashpos = st_ums.indexOf("/", pos + 9);
              if (slashpos == -1)
                slashpos = st_ums.length();

              line.orig_value.value = HBCIUtils.string2Value(st_ums.substring(
                  pos + 9, slashpos).replace(',', '.'));
            }

            // extract charge Value
            pos = st_ums.indexOf("/CHGS/", next);
            if (pos != -1)
            {
              line.charge_value = new Value();
              line.charge_value.curr = st_ums.substring(pos + 6, pos + 9);

              int slashpos = st_ums.indexOf("/", pos + 9);
              if (slashpos == -1)
                slashpos = st_ums.length();

              line.charge_value.value = HBCIUtils.string2Value(st_ums
                  .substring(pos + 9, slashpos).replace(',', '.'));
            }
          }

          String st_multi = Swift.getTagValue(st_tag, "86", ums_counter);
          if (st_multi != null)
          {
            line.gvcode = st_multi.substring(0, 3);
            st_multi = Swift.packMulti(st_multi.substring(3));

            if (!line.gvcode.equals("999"))
            {
              line.text = Swift.getMultiTagValue(st_multi, "00");
              line.primanota = Swift.getMultiTagValue(st_multi, "10");
              for (int i = 0; i < 10; i++)
              {
                line.addUsage(Swift.getMultiTagValue(st_multi, Integer
                    .toString(20 + i)));
              }

              Konto acc = new Konto();
              acc.blz = Swift.getMultiTagValue(st_multi, "30");
              acc.number = Swift.getMultiTagValue(st_multi, "31");
              acc.name = Swift.getMultiTagValue(st_multi, "32");
              acc.name2 = Swift.getMultiTagValue(st_multi, "33");
              if (acc.blz != null || acc.number != null || acc.name != null
                  || acc.name2 != null)
              {

                if (acc.blz == null)
                  acc.blz = "";
                if (acc.number == null)
                  acc.number = "";
                if (acc.name == null)
                  acc.name = "";
                line.other = acc;
              }

              line.addkey = Swift.getMultiTagValue(st_multi, "34");
              for (int i = 0; i < 4; i++)
              {
                line.addUsage(Swift.getMultiTagValue(st_multi, Integer
                    .toString(60 + i)));
              }
            } else
              line.additional = st_multi;
          }

          btag.addLine(line);
          ums_counter++;
        }

        // extract "schlusssaldo"
        btag.end = new Saldo();

        String st_end = Swift.getTagValue(st_tag, "62F", 0);
        btag.endtype = 'F';
        if (st_end == null)
        {
          st_end = Swift.getTagValue(st_tag, "62M", 0);
          btag.endtype = 'M';
        }
        btag.end.cd = st_end.substring(0, 1);

        next = 0;
        if (st_end.charAt(2) > '9')
        {
          btag.end.timestamp = null;
          next = 2;
        } else
        {
          btag.end.timestamp = dateFormat.parse(st_end.substring(1, 7));
          next = 7;
        }

        // set default values for optional non-given bdates
        if (btag.start.timestamp == null)
        {
          btag.start.timestamp = btag.end.timestamp;
        }
        for (int j = 0; j < btag.lines.length; j++)
        {
          if (btag.lines[j].bdate == null)
          {
            btag.lines[j].bdate = btag.end.timestamp;
          }
        }

        btag.end.value.curr = st_end.substring(next, next + 3);
        btag.end.value.value = HBCIUtils.string2Value(st_end
            .substring(next + 3).replace(',', '.'));

        buffer.delete(0, st_tag.length());
        booked = buffer.toString();
      }
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
 * Revision 1.1  2006/01/17 00:22:36  willuhn
 * @N erster Code fuer Swift MT940-Import
 *
 ******************************************************************************/