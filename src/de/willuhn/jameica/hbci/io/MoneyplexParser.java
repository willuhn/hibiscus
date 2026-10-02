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

import java.io.InputStream;
import java.io.InputStreamReader;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.NonValidator;
import net.n3.nanoxml.StdXMLBuilder;
import net.n3.nanoxml.StdXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParseException;

/**
 * Data-only XML boundary for Moneyplex imports.
 */
final class MoneyplexParser
{
  private MoneyplexParser()
  {
  }

  /**
   * Parses a Moneyplex document without allowing document types or external
   * resources.
   * @param input the document.
   * @param encoding the configured Moneyplex encoding.
   * @return the XML root element.
   * @throws Exception if the document cannot be parsed safely.
   */
  static IXMLElement parse(InputStream input, String encoding) throws Exception
  {
    DtdRejectingParser parser = new DtdRejectingParser();
    parser.setReader(new StdXMLReader(new InputStreamReader(input,encoding)));
    return (IXMLElement) parser.parse();
  }

  /** Keep the import independent of the Jameica version installed by the user. */
  private static final class DtdRejectingParser extends StdXMLParser
  {
    private DtdRejectingParser()
    {
      setBuilder(new StdXMLBuilder());
      setValidator(new NonValidator());
    }

    @Override
    protected void processDocType() throws Exception
    {
      IXMLReader reader = getReader();
      String systemID = reader == null ? null : reader.getSystemID();
      int line = reader == null ? 0 : reader.getLineNr();
      throw new XMLParseException(systemID,line,"DOCTYPE declarations are not allowed");
    }
  }
}
