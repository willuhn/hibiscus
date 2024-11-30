/**********************************************************************
 *
 * Copyright (c) 2018 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.w3c.dom.Document;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.Open;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Importer fuer Rechnungen im XRechnung-Format.
 */
public class XRechnungImporter implements Importer
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static DateFormat DF = new SimpleDateFormat("yyyyMMdd");

  @Override
  public String getName() 
  { 
    return i18n.tr("PDF-Rechnung im XRechnung-Format"); 
  }

  @Override
  public IOFormat[] getIOFormats(Class objectType) 
  { 
    if (!AuslandsUeberweisung.class.equals(objectType)) 
      return null; // Wir bieten uns nur fuer SEPA-Ueberweisungen an 

    IOFormat f = new IOFormat() { 
      @Override
      public String getName() 
      { 
        return XRechnungImporter.this.getName(); 
      } 

      @Override
      public String[] getFileExtensions() 
      { 
        return new String[] {"*.pdf"}; 
      } 
    }; 
    return new IOFormat[] { f }; 
  } 

  @Override
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor, BackgroundTask bgt) throws RemoteException, ApplicationException
  { 
    monitor.setStatusText(i18n.tr("Importiere XRechnung-Datei"));
    monitor.setPercentComplete(20);

    final AuslandsUeberweisung u = Settings.getDBService().createObject(AuslandsUeberweisung.class,null); 

    try
    {
      final Document doc = this.getDocument(is);
      if (doc == null)
        throw new ApplicationException(i18n.tr("Datei enthält keine XRechnung-konformen Daten"));

      final XPathFactory xpathFact = XPathFactory.newInstance();
      final XPath xpath = xpathFact.newXPath();
      
      monitor.setPercentComplete(40);

      // Die XPath-Queries stammen aus
      // https://github.com/ZUGFeRD/mustangproject/blob/master/library/src/main/java/org/mustangproject/ZUGFeRD/ZUGFeRDImporter.java
      
      ////////////////////////////////////////////////////////////
      // Betrag
      {
        String betrag = this.xpath(doc,xpath,"//*[local-name() = 'SpecifiedTradeSettlementHeaderMonetarySummation']/*[local-name() = 'DuePayableAmount']");
        if (betrag == null || betrag.isBlank())
          betrag = this.xpath(doc,xpath,"//*[local-name() = 'GrandTotalAmount']");
        
        BigDecimal d = null;
        try
        {
          d = new BigDecimal(betrag);
        }
        catch (Exception e)
        {
          Logger.error("unable to read '" + betrag + "' as amount",e);
        }
        
        if (d != null)
          u.setBetrag(d.setScale(2).doubleValue());
      }
      //
      ////////////////////////////////////////////////////////////
      
      ////////////////////////////////////////////////////////////
      // Referenz
      {
        String ref = this.xpath(doc,xpath,"//*[local-name() = 'ApplicableHeaderTradeSettlement']/*[local-name() = 'PaymentReference']");
        if (ref == null || ref.isBlank())
          ref = this.xpath(doc,xpath,"//*[local-name() = 'ApplicableSupplyChainTradeSettlement']/*[local-name() = 'PaymentReference']");
        if (ref == null || ref.isBlank())
          ref = this.xpath(doc,xpath,"//*[local-name() = 'ExchangedDocument']/*[local-name() = 'ID']");

        if (ref != null && !ref.isBlank())
        {
          u.setEndtoEndId(ref);
          u.setPmtInfId(ref);
          u.setZweck(ref);
        }
      }
      //
      ////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////////////
      // Name
      {
        final String name = this.xpath(doc,xpath,"//*[local-name() = 'SellerTradeParty']/*[local-name() = 'Name']");
        if (name != null && !name.isBlank())
          u.setGegenkontoName(name); 
      }
      //
      ////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////////////
      // Zieldatum
      {
        final Date due    = this.parseDate(this.xpath(doc,xpath,"//*[local-name() = 'SpecifiedTradePaymentTerms']/*[local-name() = 'DueDateDateTime']/*[local-name() = 'DateTimeString']"));
        final Date issued = this.parseDate(this.xpath(doc,xpath,"//*[local-name() = 'ExchangedDocument']/*[local-name() = 'IssueDateTime']/*[local-name() = 'DateTimeString']"));

        if (due != null && due.after(new Date())) // Fälligkeit muss noch in der Zukunft liegen
        {
          u.setTermin(due);
        }
        else if (issued != null)
        {
          // Checken, ob wir stattdessen ein Rechnungsdatum haben. Wenn ja, nehmen wir das und legen 1 Woche drauf
          Calendar cal = Calendar.getInstance();
          cal.setTime(issued);
          cal.add(Calendar.DATE,7);
          final Date d = cal.getTime();

          if (d.after(new Date())) // Fälligkeit muss noch in der Zukunft liegen
          u.setTermin(d);
        }
      }
      //
      ////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////////////
      // IBAN + BIC
      {
        final String iban = this.xpath(doc,xpath,"//*[local-name() = 'PayeePartyCreditorFinancialAccount']/*[local-name() = 'IBANID']");
        if (iban != null && !iban.isBlank())
          u.setGegenkontoNummer(iban.replace(" ", ""));
        
        final String bic = this.xpath(doc,xpath,"//*[local-name() = 'PayeeSpecifiedCreditorFinancialInstitution']/*[local-name() = 'BICID']");
        if (bic != null && !bic.isBlank())
          u.setGegenkontoBLZ(bic.replace(" ", ""));
      }
      //
      ////////////////////////////////////////////////////////////

      monitor.setStatus(ProgressMonitor.STATUS_DONE);
      monitor.setPercentComplete(100);
      monitor.setStatusText(i18n.tr("SEPA-Überweisung erstellt"));
  
      // Wir speichern den Auftrag nicht direkt sondern oeffnen ihn nur zur Bearbeitung.
      // Denn wir koennen nicht garantieren, dass alle noetigen Informationen enthalten sind, um den Auftrag speichern zu koennen.
      // Ausserdem haben wir gar kein Konto ausgewaehlt.
      new Open().handleAction(u);
    }
    catch (OperationCanceledException oce)
    {
      Logger.warn("operation cancelled");
      throw new ApplicationException(i18n.tr("Import abgebrochen"));
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while reading file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Import der XRechnung"));
    }
  }
  
  /**
   * Versucht, den Text als Datum zu lesen.
   * @param s der Text.
   * @return das Datum oder NULL.
   */
  private Date parseDate(String s)
  {
    if (s == null || s.isBlank())
      return null;

    // Ich nehme an, es gibt unterschiedliche Datumsformate. Ich habe aber keine Unterlagen zu den verwendeten Formaten gefunden.
    try
    {
      return DF.parse(s);
    }
    catch (Exception e)
    {
      Logger.error("unable to parse '" + s + "' as date, format unknown");
    }
    return null;
  }
  
  /**
   * Liefert einen Wert aus dem Dokument per XPath.
   * @param doc das Dokument.
   * @param xpath der XPath.
   * @param query das Query.
   * @return der Wert oder NULL, wenn er nicht gefunden wurde.
   */
  private String xpath(Document doc, XPath xpath, String query)
  {
    try
    {
      return xpath.evaluate(query, doc);
    }
    catch (final Exception e)
    {
      Logger.error("unable to execute xpath",e);
    }
    return null;
  }
  
  /**
  * Extrahiert das XML aus der PDF-Datei per iText.
  * @param reader der Reader.
  * @return das XML.
  * @throws Exception
  */
  private Document getDocument(InputStream is) throws Exception
  {
    PDDocument doc = null;
    
    try
    {
      doc = Loader.loadPDF(new RandomAccessReadBuffer(is));
      final PDDocumentCatalog root = doc != null ? doc.getDocumentCatalog() : null;
      final PDDocumentNameDictionary names = root != null ? new PDDocumentNameDictionary(root) : null;
      final PDEmbeddedFilesNameTreeNode files = names != null ? names.getEmbeddedFiles() : null;

      if (files == null)
      {
        Logger.info("pdf does not contain embedded files");
        return null;
      }

      final List<PDComplexFileSpecification> list = new ArrayList<>();
      final Map<String, PDComplexFileSpecification> map = files.getNames();
      if (map != null)
        list.addAll(map.values());
      
      // Die Dateien stecken unter Umständen auch in den Kindern
      final List<PDNameTreeNode<PDComplexFileSpecification>> kids = files.getKids();
      if (kids != null && !kids.isEmpty())
      {
        for (PDNameTreeNode<PDComplexFileSpecification> kid:kids)
        {
          final Map<String, PDComplexFileSpecification> kidMap = kid.getNames();
          list.addAll(kidMap.values());
        }
      }
      
      for (PDComplexFileSpecification spec:list)
      {
        final String filename = spec.getFilename();
        
        // Wir nehmen daher jede Datei, deren Name auf XML endet.
        if (filename == null || filename.isBlank())
          continue;
        
        if (!filename.toLowerCase().endsWith(".xml"))
          continue;
        
        final PDEmbeddedFile f = spec.getEmbeddedFile();
        final byte[] bytes = f.toByteArray();
        return this.parse(bytes);
      }
      return null;
    }
    finally
    {
      if (doc != null)
      {
        try
        {
          doc.close();
        }
        catch (Exception e)
        {
          Logger.error("error while closing reader",e);
        }
      }
    }
  }
  
//  /**
//   * Extrahiert das XML aus der PDF-Datei per iText.
//   * @param reader der Reader.
//   * @return das XML.
//   * @throws Exception
//   */
//  private Document getDocument(InputStream is) throws Exception
//  {
//    PdfReader reader = null;
//    
//    try
//    {
//      reader = new PdfReader(is);
//      final PdfDictionary root = reader.getCatalog();
//      final PdfDictionary names = root != null ? root.getAsDict(PdfName.NAMES) : null;
//      final PdfDictionary files = names != null ? names.getAsDict(PdfName.EMBEDDEDFILES) : null;
//
//      PdfArray specs = files != null ? files.getAsArray(PdfName.NAMES) : null;
//
//      // Die Dateien stecken u.U. nicht in "NAMES" sondern in "KIDS".
//      // Per iText weiss ich aber nicht, wie ich da rankomme.
//      // Ich lasse den Code hier dennoch mal auskommentiert stehen.
//      // Falls sich hier eine Lösung findet, bräuchte man PDFBox nicht mehr.
//      if (specs == null || specs.size() == 0)
//        specs = files != null ? files.getAsArray(PdfName.KIDS) : null;
//      
//      if (specs == null || specs.size() == 0)
//      {
//        Logger.info("pdf does not contain embedded files");
//        return null;
//      }
//      
//      for (int i=1;i<specs.size();++i) // erstes Element überspringen
//      {
//        final PdfDictionary spec = specs.getAsDict(i);
//        final PdfDictionary refs = spec.getAsDict(PdfName.EF);
//        final Iterator it = refs.getKeys().iterator();
//        while (it.hasNext())
//        {
//          final PdfName key = (PdfName) it.next();
//          if (!key.toString().equals("/F"))
//            continue;
//
//          final String filename = spec.getAsString(key).toString();
//          
//          // Man kann den Dateinamen auch wie folgt ermitteln. Bin aber skeptisch, dass die Schema-URN lange so bleibt
//          // final XMPMeta meta = XMPMetaFactory.parseFromBuffer(reader.getMetadata());
//          // final String filename = meta.getPropertyString("urn:factur-x:pdfa:CrossIndustryDocument:invoice:1p0#","fx:DocumentFileName");
//          // Wir nehmen daher jede Datei, deren Name auf XML endet.
//          if (filename == null || filename.isBlank())
//            continue;
//          
//          if (!filename.toLowerCase().endsWith(".xml"))
//            continue;
//          
//          final PRStream stream = (PRStream) PdfReader.getPdfObject(refs.getAsIndirectObject(key));
//          final byte[] bytes = PdfReader.getStreamBytes(stream);
//          
//          return this.parse(bytes);
//        }
//      }
//      return null;
//    }
//    finally
//    {
//      if (reader != null)
//      {
//        try
//        {
//          reader.close();
//        }
//        catch (Exception e)
//        {
//          Logger.error("error while closing reader",e);
//        }
//      }
//    }
//  }
  
  /**
   * Parst den Content als XML-File.
   * @param bytes die Daten.
   * @return das XML-Dokument.
   * @throws Exception
   */
  private Document parse(byte[] bytes) throws Exception
  {
    final String s = new String(bytes,StandardCharsets.UTF_8);
    Logger.trace("XML for debugging purpose: " + s);
    
    final DocumentBuilderFactory xmlFact = DocumentBuilderFactory.newInstance();
    xmlFact.setNamespaceAware(true);
    final DocumentBuilder builder = xmlFact.newDocumentBuilder();
    return builder.parse(new ByteArrayInputStream(bytes));
  }
}


