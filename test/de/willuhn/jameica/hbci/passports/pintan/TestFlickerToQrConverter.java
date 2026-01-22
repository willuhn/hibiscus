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
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;
import org.hbci4java.hbci.manager.QRCode;

/**
 * Testet die Konvertierung des Flicker-Codes in den QR-Code.
 */
public class TestFlickerToQrConverter
{
  /**
   * Wandelt binäre Bilddaten aus Byte-Array in Bild um.
   * @param imageData Byte-Array der binären Bilddaten
   * @return aus Byte-Array erzeugtes Bild
   * @throws IOException
   */
  private static BufferedImage getImage(final byte[] imageData) throws IOException
  {
    final InputStream is = new ByteArrayInputStream(imageData);
    return ImageIO.read(is);
  }
  
  /**
   * Gibt Pixel-Array des Bildes zurück.
   * @param img Bild, dessen Pixel-Array ermittelt werden soll
   * @return in Bild enthaltenes Pixel-Array
   */
  private static byte[] getPixels(final BufferedImage img)
  {
    return ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
  }
  
  /**
   * Test vergleicht zunächst Bildeigenschaften und dann Bildinhalt auf Pixelebene
   * @param expected das erwartete Bild
   * @param actual das zu vergleichende Bild
   */
  private static void assertImageEquals(BufferedImage expected, BufferedImage actual)
  {
    Assert.assertEquals(expected.getHeight(), actual.getHeight());
    Assert.assertEquals(expected.getWidth(), actual.getWidth());
    Assert.assertEquals(expected.getTransparency(), actual.getTransparency());
    // Comparing pixels is only valid if types are equal since monochrome images store more than one pixel per byte.
    Assert.assertEquals(expected.getType(), actual.getType());
    Assert.assertArrayEquals(getPixels(expected), getPixels(actual));
  }

  /**
   * @throws Exception
   */
  @Test
  public void testBankauftragAllgemein() throws Exception
  {
    // Bankauftrag allgemein
    final String png = FlickerToQrConverter.convert("058201100F40");
    final QRCode code = new QRCode(png, "Bankauftrag allgemein");
    final BufferedImage imgActual = getImage(code.getImage());
    
    final byte[] pngData = FlickerToQrConverter.parseHexBinary("89504E470D0A1A0A0000000D49484452000000940000009401000000005D473D790000010D4944415478DACD96511283300844F706DCFF96B9C19685A0E9F4CFF5A341477DCED4082F58F0672CFC1503108BA888BAF058303744872E5C966717D2FE025BE8E04B8C57BCC0F4DAECE09197A74C65B9C651B7A74CE328D1E5D55396E7B9B5444B793059D6660D5322C2649AE4E434EF064D96475EA89EE331AC5A291532204CC644B8250F97C99BA3E895038B1D4D224A778FE521EE26165D73832167C9C3219A8C33D58AE83568B010C23854D25B2C275C89DD0EF53A7298A68DB1A87B98C372BF1D02DA2183690932BE1D72D868C369B726D3D84EDE8E1B4C3F8CED507D444DA62588ED90EA4E97F527AA1D9ABC988CED5069CE17D894BC6C7719397057C96355A3E939EA1826FBEFFFBB1FC496DF9420864B350000000049454E44AE426082");
    final BufferedImage imgExpected = getImage(pngData);
    
    assertImageEquals(imgExpected, imgActual);
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
    final BufferedImage imgActual = getImage(code.getImage());

    byte[] pngData = FlickerToQrConverter.parseHexBinary("89504E470D0A1A0A0000000D49484452000000940000009401000000005D473D79000001264944415478DACD96510EC33008437D03EE7F4B6EE0619C6E93F657F763895429AF5248C040C09FD1F82B06A0AA35D1BB085991054F2F52362668D4A5C513ECCCA7D86CAFD97C82CDB5AB39B3658D29DB185DE33B6E3799C60911EA4B5777D96C5A581171FCB05A8A98AE7F314A46212B6A67DAABE2215B492E92A966CEE6986D11CDFAF8E53EEBF6C9F58BCA9E904DA8E1A08B3518B289F855245A699D32A5B2FE68B56913B25179BB28AE29C72860FA1C692AA1AB42D6EF2CE17A9721531DC4A521384C09A384738AEC38B553367BA3DE1A62CC2C72D7B06D2B21D3B5B931374DD9D6586B12457CEAEE4D76B67566ABFD856C7BBC35B43267CA5414ADA0B157A827D8B679E5A27D9032555A3F41ACA1886D8FF79B06A766270CBEB89E707BF894FDF77BF705E26AE87C5D161C770000000049454E44AE426082");
    final BufferedImage imgExpected = getImage(pngData);
    
    assertImageEquals(imgExpected, imgActual);
  }

  /**
   * @throws Exception
   */
  @Test
  public void testInvalidLength() throws Exception
  {
    try
    {
      FlickerToQrConverter.convert("1DC80138323131323334354A30313233343536373839463130302C3030020");
      Assert.fail("Hätte fehlschlagen müssen");
    }
    catch (Exception e)
    {
      Assert.assertEquals(IllegalArgumentException.class,e.getClass());
    }
  }


  /**
   * @throws Exception
   */
  @Test
  public void testInvalidChar() throws Exception
  {
    try
    {
      FlickerToQrConverter.convert("1DC80138323131323334354A30313233343536373839463130302C30300ß");
      Assert.fail("Hätte fehlschlagen müssen");
    }
    catch (Exception e)
    {
      Assert.assertEquals(IllegalArgumentException.class,e.getClass());
    }
    
  }
}
