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
import org.kapott.hbci.manager.MatrixCode;

import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.util.ApplicationException;

/**
 * Dialog für die PhotoTAN-Eingabe.
 */
public class PhotoTANDialog extends TANDialog
{
  private String hhduc = null;
  
  /**
   * ct.
   * @param config die PINTAN-Config.
   * @param hhduc die HHDuc-Rohdaten.
   * @throws RemoteException
   * @throws ApplicationException wenn die Grafik nicht geparst werden konnte.
   */
  public PhotoTANDialog(PinTanConfig config, String hhduc) throws RemoteException, ApplicationException
  {
    super(config);
    this.hhduc = hhduc;
    this.setSideImage(null);
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent,true,1);
    container.addHeadline(i18n.tr("Matrixcode"));

    MatrixCode code = new MatrixCode(this.hhduc);
    InputStream stream = new ByteArrayInputStream(code.getImage());
    Image image = SWTUtil.getImage(stream);
    
    // Breite des Dialogs ermitteln (+ ein paar Pixel Toleranz am Rand)
    int width = image.getBounds().width + 200;
    
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
