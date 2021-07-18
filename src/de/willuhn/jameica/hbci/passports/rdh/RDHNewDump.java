/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh;

import java.io.CharConversionException;
import java.io.FileInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Kleine dreckige Testklasse, um eine HBCI4Java-Schluesseldatei
 * zu entschluesseln und den XML-Inhalt unverschluesselt auf die
 * Console zu schreiben. Das Tool dient nur zu Debugging-Zwecken,
 * um mal nachschauen zu koennen, was genau in der Schluesseldatei
 * drin steht.
 * Der Code ist aus "HBCIPassportRDHNew" (HBCI4Java) zusammenkopiert.
 * Aufruf:
 * {@code
 * java -cp hbci4java-....jar \
 *   de.willuhn.jameica.hbci.passports.rdh.RDHNewDump \
 *   <Schluesseldatei> <Passwort>
 * }
 */
public class RDHNewDump
{

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception
  {
    if (args == null || args.length != 2)
    {
      System.err.println("Usage:");
      System.err.println("java -cp hbci4java-....jar \\" +
                         "\n  de.willuhn.jameica.hbci.passports.rdh.RDHNewDump \\" + 
                         "\n  <Schluesseldatei> <Passwort>\n");
      System.exit(1);
    }

    final byte[] CIPHER_SALT={(byte)0x26,(byte)0x19,(byte)0x38,(byte)0xa7,(byte)0x99,(byte)0xbc,(byte)0xf1,(byte)0x55};
    final int CIPHER_ITERATIONS=987;

    SecretKeyFactory fac=SecretKeyFactory.getInstance("PBEWithMD5AndDES");
    PBEKeySpec keyspec=new PBEKeySpec(args[1].toCharArray());
    SecretKey passportKey=fac.generateSecret(keyspec);
    keyspec.clearPassword();

    PBEParameterSpec paramspec=new PBEParameterSpec(CIPHER_SALT,CIPHER_ITERATIONS);
    Cipher cipher=Cipher.getInstance("PBEWithMD5AndDES");
    cipher.init(Cipher.DECRYPT_MODE,passportKey,paramspec);

    DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
    dbf.setValidating(false);
    DocumentBuilder db=dbf.newDocumentBuilder();

    Element root = null;
    CipherInputStream ci = null;
    try
    {
      ci = new CipherInputStream(new FileInputStream(args[0]),cipher);
      root=db.parse(ci).getDocumentElement();
    }
    catch (CharConversionException e1)
    {
      System.out.println("Passwort falsch (JDK 1.5+)");
    }
    catch (SAXException e)
    {
      System.out.println("Passwort falsch (bis JDK 1.4)");
      return;
    }
    finally
    {
      if (ci != null)
        ci.close();
    }

    TransformerFactory tfac=TransformerFactory.newInstance();
    Transformer tform=tfac.newTransformer();

    tform.setOutputProperty(OutputKeys.METHOD,"xml");
    tform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"no");
    tform.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
    tform.setOutputProperty(OutputKeys.INDENT,"yes");

    tform.transform(new DOMSource(root),new StreamResult(System.out));
  }

}
