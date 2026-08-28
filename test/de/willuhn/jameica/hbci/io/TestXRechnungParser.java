/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

/**
 * Tests fuer den XRechnung-Parser.
 */
public class TestXRechnungParser
{
  private static final String UBL =
      "<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\" " +
      "xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\">" +
      "<cbc:ID>UBL-1</cbc:ID></Invoice>";

  private static final String CII =
      "<rsm:CrossIndustryInvoice " +
      "xmlns:rsm=\"urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100\" " +
      "xmlns:ram=\"urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100\">" +
      "<rsm:ExchangedDocument><ram:ID>CII-1</ram:ID></rsm:ExchangedDocument>" +
      "</rsm:CrossIndustryInvoice>";

  /**
   * Minimale UBL- und CII-Dokumente werden weiterhin gelesen.
   * @throws Exception
   */
  @Test
  public void testValidDialects() throws Exception
  {
    Assertions.assertEquals("UBL-1",id(parse(UBL)));
    Assertions.assertEquals("CII-1",id(parse(CII)));
  }

  /**
   * Ein gueltiges XML-Attachment wird aus einem PDF gelesen.
   * @throws Exception
   */
  @Test
  public void testValidPdfAttachment() throws Exception
  {
    Assertions.assertEquals("UBL-1",id(parsePdf(UBL)));
  }

  /**
   * Dokumente mit DOCTYPE werden vor der Entity-Aufloesung abgelehnt.
   */
  @Test
  public void testDoctypeRejected()
  {
    assertRejected("<!DOCTYPE Invoice><Invoice/>");
  }

  /**
   * Externe Datei-Entities werden nicht aufgeloest.
   * @param directory Testverzeichnis.
   * @throws Exception
   */
  @Test
  public void testFileEntityRejected(@TempDir Path directory) throws Exception
  {
    Path secret = directory.resolve("secret.txt");
    Files.writeString(secret,"FILE_ENTITY_EXPANDED");
    assertRejected("<!DOCTYPE Invoice [<!ENTITY xxe SYSTEM \"" + secret.toUri() + "\">]>" +
                   "<Invoice><ID>&xxe;</ID></Invoice>");
  }

  /**
   * Externe HTTP-Entities verursachen keinen Verbindungsaufbau.
   * @throws Exception
   */
  @Test
  public void testHttpEntityRejectedWithoutRequest() throws Exception
  {
    try (ServerSocket listener = new ServerSocket(0,1,InetAddress.getByName("127.0.0.1")))
    {
      String xml = "<!DOCTYPE Invoice [<!ENTITY xxe SYSTEM \"http://127.0.0.1:" +
                   listener.getLocalPort() + "/entity\">]>" +
                   "<Invoice><ID>&xxe;</ID></Invoice>";
      Assertions.assertTimeoutPreemptively(Duration.ofSeconds(1),() -> assertRejected(xml));
      listener.setSoTimeout(100);
      Assertions.assertThrows(SocketTimeoutException.class,() -> listener.accept());
    }
  }

  /**
   * Parameter-Entities und externe DTDs werden abgelehnt.
   * @param directory Testverzeichnis.
   * @throws Exception
   */
  @Test
  public void testExternalDtdAndParameterEntityRejected(@TempDir Path directory) throws Exception
  {
    Path dtd = directory.resolve("external.dtd");
    Files.writeString(dtd,"<!ENTITY injected 'EXTERNAL_DTD_LOADED'>");
    assertRejected("<!DOCTYPE Invoice SYSTEM \"" + dtd.toUri() + "\"><Invoice/>");
    assertRejected("<!DOCTYPE Invoice [<!ENTITY % external SYSTEM \"" + dtd.toUri() + "\">%external;]>" +
                   "<Invoice/>");
  }

  /**
   * XInclude bleibt deaktiviert und kann keine externe Datei laden.
   * @param directory Testverzeichnis.
   * @throws Exception
   */
  @Test
  public void testXIncludeDisabled(@TempDir Path directory) throws Exception
  {
    Path secret = directory.resolve("include.txt");
    Files.writeString(secret,"XINCLUDE_EXPANDED");
    Document doc = parse("<Invoice xmlns:xi=\"http://www.w3.org/2001/XInclude\">" +
                         "<xi:include href=\"" + secret.toUri() + "\" parse=\"text\"/>" +
                         "</Invoice>");
    Assertions.assertFalse(doc.getDocumentElement().getTextContent().contains("XINCLUDE_EXPANDED"));
    Assertions.assertEquals(1,doc.getElementsByTagNameNS("http://www.w3.org/2001/XInclude","include").getLength());
  }

  /**
   * Entity-Expansion wird ohne nennenswerten Ressourcenverbrauch abgelehnt.
   */
  @Test
  public void testEntityExpansionRejectedQuickly()
  {
    String xml = "<!DOCTYPE Invoice [" +
                 "<!ENTITY a '1234567890'>" +
                 "<!ENTITY b '&a;&a;&a;&a;&a;&a;&a;&a;&a;&a;'>" +
                 "<!ENTITY c '&b;&b;&b;&b;&b;&b;&b;&b;&b;&b;'>" +
                 "]><Invoice><ID>&c;</ID></Invoice>";
    Assertions.assertTimeoutPreemptively(Duration.ofSeconds(1),() -> assertRejected(xml));
  }

  /**
   * Auch XML-Attachments in PDFs verwenden den gehaerteten Parser.
   * @param directory Testverzeichnis.
   * @throws Exception
   */
  @Test
  public void testPdfAttachmentRejectsDoctype(@TempDir Path directory) throws Exception
  {
    Path secret = directory.resolve("pdf-secret.txt");
    Files.writeString(secret,"PDF_ENTITY_EXPANDED");
    String xml = "<!DOCTYPE Invoice [<!ENTITY xxe SYSTEM \"" + secret.toUri() + "\">]>" +
                 "<Invoice><ID>&xxe;</ID></Invoice>";
    Assertions.assertThrows(SAXParseException.class,() -> parsePdf(xml));
  }

  private static Document parse(String xml) throws Exception
  {
    return XRechnungParser.parse(xml.getBytes(StandardCharsets.UTF_8));
  }

  private static void assertRejected(String xml)
  {
    Assertions.assertThrows(SAXParseException.class,() -> parse(xml));
  }

  private static String id(Document doc)
  {
    return doc.getElementsByTagNameNS("*","ID").item(0).getTextContent();
  }

  private static Document parsePdf(String xml) throws Exception
  {
    byte[] pdf = createPdf(xml.getBytes(StandardCharsets.UTF_8));
    try (PDDocument doc = Loader.loadPDF(pdf))
    {
      return XRechnungParser.parse(doc);
    }
  }

  private static byte[] createPdf(byte[] xml) throws Exception
  {
    try (PDDocument doc = new PDDocument(); ByteArrayOutputStream bytes = new ByteArrayOutputStream())
    {
      doc.addPage(new PDPage());

      PDComplexFileSpecification specification = new PDComplexFileSpecification();
      specification.setFile("invoice.xml");
      PDEmbeddedFile embedded = new PDEmbeddedFile(doc,new ByteArrayInputStream(xml));
      embedded.setSubtype("application/xml");
      embedded.setSize(xml.length);
      specification.setEmbeddedFile(embedded);

      Map<String,PDComplexFileSpecification> attachments = new HashMap<>();
      attachments.put("invoice.xml",specification);
      PDEmbeddedFilesNameTreeNode tree = new PDEmbeddedFilesNameTreeNode();
      tree.setNames(attachments);

      PDDocumentNameDictionary names = new PDDocumentNameDictionary(doc.getDocumentCatalog());
      names.setEmbeddedFiles(tree);
      doc.getDocumentCatalog().setNames(names);
      doc.save(bytes);
      return bytes.toByteArray();
    }
  }
}
