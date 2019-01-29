/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.pintan;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.kapott.hbci.manager.QRCode;

import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.util.ApplicationException;

/**
 * Dialog für die QRTAN-Eingabe.
 */
public class QRTANDialog extends TANDialog
{
  private QRCode code  = null;
  private String hhduc = null;
  private Exception ex = null;
  
  
  /**
   * ct.
   * @param config die PINTAN-Config.
   * @param hhduc die HHDuc-Rohdaten.
   * @throws RemoteException
   * @throws ApplicationException wenn die Grafik nicht geparst werden konnte.
   */
  public QRTANDialog(PinTanConfig config, String hhduc) throws RemoteException, ApplicationException
  {
    super(config);
    this.setSideImage(null);
    this.hhduc = hhduc;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.TANDialog#setText(java.lang.String)
   * @param text Die Sparkassen verwenden QR-Code in HHD 1.3 und uebertragen dort (wie beim Flickercode auch)
   * die maschinenlesbaren Daten direkt in der Text-Nachricht per Base64-Codierung in den Tags
   * CHLGUC und CHLGTEXT. Wir haben daher diese Methode ueberschrieben, um den Code bei Bedarf dort
   * zu extrahieren.
   */
  @Override
  public void setText(String text)
  {
    try
    {
      // Eigentlich koennten wir "text" auch im Konstruktor uebergeben und alles dort parsen.
      // Aber "setText" darf erst nach "setContext" ausgerufen werden, damit der Dialog den anzuzeigenden Auftrag kennt
      this.code = new QRCode(this.hhduc,text);
      super.setText(this.code.getMessage());
    }
    catch (Exception e)
    {
      this.ex = e;
    }
  }
  
	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    if (this.ex != null)
      throw ex;
    
    Container container = new SimpleContainer(parent,true,1);
    container.addHeadline(i18n.tr("QR-Code"));

    InputStream stream = new ByteArrayInputStream(this.code.getImage());
    Image image = SWTUtil.getImage(stream);
    
    // Breite des Dialogs ermitteln (+ ein paar Pixel Toleranz am Rand)
    int width = image.getBounds().width + 300;
    
    Label imageLabel = new Label(container.getComposite(),SWT.NONE);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.horizontalAlignment = SWT.CENTER;
    imageLabel.setLayoutData(gd);
    imageLabel.setImage(image);
    
    this.setSize(width,SWT.DEFAULT);

    // Hier stehen dann noch die Anweisungen von der Bank drin
    super.paint(parent);
    getShell().setMinimumSize(getShell().computeSize(width,SWT.DEFAULT));
  }
}
