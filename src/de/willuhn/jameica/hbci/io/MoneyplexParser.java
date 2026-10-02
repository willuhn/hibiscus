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

import de.willuhn.jameica.util.SafeXMLParser;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.StdXMLReader;

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
    SafeXMLParser parser = new SafeXMLParser();
    parser.setReader(new StdXMLReader(new InputStreamReader(input,encoding)));
    return (IXMLElement) parser.parse();
  }
}
