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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

/**
 * Importer fuer Umsaetze im Moneyplex XML-Format.
 */
public class MoneyplexUmsatzImporter implements Importer
{
  private final static de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();

  private final static I18N i18n             = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static DateFormat DATEFORMAT = new SimpleDateFormat("dd.MM.yy");

  private Map<String,UmsatzTyp> cache = new HashMap<String,UmsatzTyp>();

  @Override
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor, BackgroundTask t) throws RemoteException, ApplicationException
  {
    cache.clear(); // Cache leeren

    if (is == null)
      throw new ApplicationException(i18n.tr("Keine zu importierende Datei ausgewählt"));
    
    if (format == null)
      throw new ApplicationException(i18n.tr("Kein Datei-Format ausgewählt"));
    
    try
    {
      
      Konto konto = null;
      
      if (context != null && context instanceof Konto)
        konto = (Konto) context;
      
      if (konto == null)
      {
        KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_CENTER);
        d.setText(i18n.tr("Bitte wählen Sie das zu verwendende Konto aus."));
        konto = (Konto) d.open();
      }

      if (monitor != null)
        monitor.setStatusText(i18n.tr("Lese Datei ein"));

      String encoding = settings.getString("moneyplex.encoding","ISO-8859-1");
      Logger.info("moneyplex encoding: " + encoding);
      IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
      parser.setReader(new StdXMLReader(new InputStreamReader(is,encoding)));
      IXMLElement root = (IXMLElement) parser.parse();
      Vector<IXMLElement> lines = root.getChildrenNamed("BUCHUNG");
      
      if (lines == null || lines.size() == 0)
        throw new ApplicationException(i18n.tr("Datei enthält keine Buchungen"));
      
      double factor = 100d / (double) lines.size();

      int created = 0;
      int error   = 0;

      for (int i=0;i<lines.size();++i)
      {
        if (monitor != null)
          monitor.setPercentComplete((int)((i+1) * factor));
        
        if (t != null && t.isInterrupted())
          throw new OperationCanceledException();

        try
        {
          int count = process(lines.get(i),konto);
          for (int c=0;c<count;++c)
            monitor.log(i18n.tr("Umsatz {0}", "" + (created+c+1)));

          created += count;
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
      monitor.setStatusText(i18n.tr("{0} Umsätze erfolgreich importiert, {1} fehlerhafte übersprungen", ""+created, ""+error));
      monitor.addPercentComplete(1);
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      Logger.warn("operation cancelled");
      throw new ApplicationException(i18n.tr("Import abgebrochen"));
    }
    catch (Exception e)
    {
      Logger.error("error while reading file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Import der Datei"));
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
   * Erstellt die Buchungen zum angegebenen XML-Element.
   * @param line das XML-Element.
   * @param konto das Konto.
   * @return Anzahl der angelegten Umsaetze.
   * @throws Exception
   */
  private int process(IXMLElement line, Konto konto) throws Exception
  {
    Umsatz umsatz = (Umsatz) Settings.getDBService().createObject(Umsatz.class,null);
    
    ////////////////////////////////////////////////////////////////////////////
    // Die gemeinsamen Daten
    Date valuta = parseDatum(line.getFirstChildNamed("VALUTA"));
    Date datum  = parseDatum(line.getFirstChildNamed("DATUM"));
    valuta = valuta != null ? valuta : (datum != null ? datum : new Date());
    datum  = datum != null ? datum : (valuta != null ? valuta : new Date());

    umsatz.setKonto(konto);
    umsatz.setDatum(datum);
    umsatz.setValuta(valuta);
    
    IXMLElement empfaenger = line.getFirstChildNamed("EMPFAENGER");
    if (empfaenger != null)
      umsatz.setGegenkontoName(getContent(empfaenger.getFirstChildNamed("NAME")));
    //
    ////////////////////////////////////////////////////////////////////////////
    
    // Checken, ob es eine Split-Buchung ist.
    IXMLElement split = line.getFirstChildNamed("SPLITT");
    if (split != null)
    {
      // Jepp, ist eine Splitt-Buchung. Dann duplizieren wir die Buchung anhand
      // der Anzahl von Splits
      Vector<IXMLElement> parts = split.getChildrenNamed("PART");
      if (parts == null || parts.size() == 0)
        throw new ApplicationException("Split-Auftrag ohne enthaltene Buchungen");
      for (IXMLElement p:parts)
      {
        Umsatz copy = umsatz.duplicate();
        String usage = getContent(p.getFirstChildNamed("ZWECK"));
        if (usage != null) VerwendungszweckUtil.apply(copy,usage.split("@")); // Moneyplex scheint das "@" als Trennzeichen zu nehmen
        copy.setUmsatzTyp(createTyp(p.getFirstChildNamed("KATEGORIE")));
        copy.setBetrag(parseBetrag(p.getFirstChildNamed("BETRAG")));
        copy.store();
        
        try
        {
          Application.getMessagingFactory().sendMessage(new ImportMessage(copy));
        }
        catch (Exception ex)
        {
          Logger.error("error while sending import message",ex);
        }
      }
      return parts.size();
    }

    
    // Ne, ist eine Einzel-Buchung
    String usage = getContent(line.getFirstChildNamed("ZWECK"));
    if (usage != null) VerwendungszweckUtil.apply(umsatz,usage.split("@"));
    umsatz.setUmsatzTyp(createTyp(line.getFirstChildNamed("KATEGORIE")));
    umsatz.setBetrag(parseBetrag(line.getFirstChildNamed("BETRAG")));
    umsatz.store();
    try
    {
      Application.getMessagingFactory().sendMessage(new ImportMessage(umsatz));
    }
    catch (Exception ex)
    {
      Logger.error("error while sending import message",ex);
    }
    return 1;
  }
  
  /**
   * Liefert den PCDATA-Body des XML-Elements oder NULL.
   * @param e das XML-Element.
   * @return der Wert des Elements oder NULL.
   */
  private String getContent(IXMLElement e)
  {
    if (e == null)
      return null;
    String s = e.getContent();
    if (s == null || s.length() == 0)
      return null;
    return s;
  }
  
  /**
   * Parst den Betrag.
   * @param e das XML-Element, aus dessen PCDATA der Wert gelesen werden soll.
   * @return der geparste Betrag oder NaN, wenn der Betrag nicht geparst werden konnte.
   */
  private double parseBetrag(IXMLElement e)
  {
    String s = getContent(e);
    if (s == null)
      return Double.NaN;
    
    try
    {
      return HBCI.DECIMALFORMAT.parse(s).doubleValue();
    }
    catch (Exception ex)
    {
      Logger.warn("unable to parse value " + s + ": " + ex.getMessage());
    }
    return Double.NaN;
  }
  
  /**
   * Parst das Datum.
   * @param e das XML-Element, aus dessen PCDATA der Wert gelesen werden soll.
   * @return das geparste Datum oder NULL.
   */
  private Date parseDatum(IXMLElement e)
  {
    String s = getContent(e);
    if (s == null)
      return null;
    
    try
    {
      return DATEFORMAT.parse(s);
    }
    catch (Exception ex)
    {
      Logger.warn("unable to parse date " + s + ": " + ex.getMessage());
    }
    return null;
  }
  
  /**
   * Sucht die Umsatz-Kategorie anhand des Namens und legt sie gleich an, wenn
   * sie noch nicht existiert.
   * @param e das XML-Element mit dem Namen der Kategorie.
   * @return die Umsatz-Kategorie oder NULL, wenn sie weder gefunden noch angelegt werden konnte.
   */
  private UmsatzTyp createTyp(IXMLElement e)
  {
    String s = getContent(e);
    if (s == null)
      return null; // Keine Kategorie angegeben
    
    // Haben wir die Kategorie schon im Cache?
    UmsatzTyp typ = this.cache.get(s);
    if (typ != null) // jepp, haben wir schon
      return typ;
    
    try
    {
      String[] names = s.split(":");
      
      if (names.length > 1)
      {
        //////////////////////////////////////////////////////////////////////////
        // Kategorie-Baum

        // a) Suche in Datenbank
        UmsatzTyp parent = null;
        boolean found = true;
        for (String name:names)
        {
          typ = findTyp(name,parent);
          if (typ == null)
          {
            found = false;
            break; // nicht gefunden
          }
          parent = typ; // naechster Durchlauf
        }

        // b) Neu anlegen
        if (!found)
        {
          parent = null;
          Logger.info("creating categories for path: " + s);
          for (String name:names)
          {
            // Checken, ob wir den schon haben
            typ = findTyp(name,parent);
            if (typ == null)
            {
              typ = (UmsatzTyp) Settings.getDBService().createObject(UmsatzTyp.class,null);
              typ.setParent(parent);
              typ.setName(name);
              typ.store();
            }
            parent = typ; // naechster Durchlauf
          }
        }
        cache.put(s,typ);
        return typ;
  
        //
        //////////////////////////////////////////////////////////////////////////
      }
      else
      {
        //////////////////////////////////////////////////////////////////////////
        // Kategorie ohne Parents
        typ = findTyp(s,null);
        if (typ == null)
        {
          Logger.info("creating category: " + s);
          typ = (UmsatzTyp) Settings.getDBService().createObject(UmsatzTyp.class,null);
          typ.setName(s);
          typ.store();
        }
        cache.put(s,typ);
        return typ;
        //
        //////////////////////////////////////////////////////////////////////////
      }
    }
    catch (Exception ex)
    {
      Logger.error("unable to load/create category " + s + ": ",ex);
    }
    return null;
  }
  
  /**
   * Sucht eine Kategorie anhand des Namens.
   * @param name Name der Kategorie.
   * @param parent optionale Angabe des Parent.
   * @return die Kategorie oder NULL, wenn sie nicht existiert.
   * @throws Exception
   */
  private UmsatzTyp findTyp(String name, UmsatzTyp parent) throws Exception
  {
    DBIterator i = Settings.getDBService().createList(UmsatzTyp.class);
    i.addFilter("name = ?", name);
    if (parent != null) i.addFilter("parent_id = " + parent.getID());

    if (i.hasNext())
      return (UmsatzTyp) i.next();
    
    return null;
  }
  
  @Override
  public String getName()
  {
    return i18n.tr("Moneyplex-Format");
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
        return MoneyplexUmsatzImporter.this.getName();
      }

      @Override
      public String[] getFileExtensions()
      {
        return new String[] {"*.xml"};
      }
    };
    return new IOFormat[] { f };
  }
}
