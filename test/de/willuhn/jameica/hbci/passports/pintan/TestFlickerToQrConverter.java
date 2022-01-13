/**********************************************************************
 *
 * Copyright (c) 2022 Oezguer Emir
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.pintan;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.junit.Assert;
import org.junit.Test;
import org.kapott.hbci.manager.QRCode;

/**
 * Testet die Konvertierung des Flicker-Codes in den QR-Code.
 */
public class TestFlickerToQrConverter
{
  /**
   * @throws Exception
   */
  @Test
  public void testBankauftragAllgemein() throws Exception
  {
    // Bankauftrag allgemein
    final String png = FlickerToQrConverter.convert("058201100F40");
    final QRCode code = new QRCode(png, "Bankauftrag allgemein");
    this.testPng(code);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testHundredBucks() throws Exception
  {
    // Startcode 82112345, Konto/IBAN 0123456789, Überweisung 100 Euro
    final String png = FlickerToQrConverter.convert("1DC80138323131323334354A30313233343536373839463130302C303002");
    final QRCode code = new QRCode(png, "Überweisung 100 Euro");
    this.testPng(code);
  }
  
  /**
   * Testet, ob das Bild in dem QR-Code als PNG gelesen werden kann.
   * @param code der Code.
   * @throws Exception
   */
  private void testPng(QRCode code) throws Exception
  {
    // Das Schliessen der Streams ist nicht noetig, da das ohnehin alles in-memory passiert
    
    // Testen, ob es sich um ein PNG handelt
    final ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(code.getImage()));
    final Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
    Assert.assertTrue(readers.hasNext());
    final ImageReader r = readers.next();
    Assert.assertEquals("png",r.getFormatName().toLowerCase());

    // Testen, ob es gelesen werden kann.
    stream.reset();
    r.setInput(stream);
    final BufferedImage img = r.read(0);
    Assert.assertNotNull(img);
  }
}
