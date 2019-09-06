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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.kapott.hbci.manager.MatrixCode;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;

/**
 * Dialog für die PhotoTAN-Eingabe.
 */
public class PhotoTANDialog extends TANDialog
{
  private final static Settings SETTINGS = new Settings(PhotoTANDialog.class);
  
  private String hhduc = null;
  private int initialSize = 0;
  private Image image = null;
  private Label imageLabel = null;
  
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

    Composite buttonComp = new Composite(container.getComposite(),SWT.NONE);
    GridData buttonGd = new GridData();
    buttonGd.horizontalAlignment = SWT.CENTER;
    buttonComp.setLayoutData(buttonGd);
    buttonComp.setLayout(new GridLayout(2,true));
    
    Button smaller = new Button(buttonComp,SWT.PUSH);
    smaller.setToolTipText(i18n.tr("Bild verkleinern"));
    smaller.setImage(SWTUtil.getImage("list-remove.png"));
    smaller.setLayoutData(new GridData());
    smaller.addSelectionListener(new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetSelected(SelectionEvent e)
      {
        resize(-30);
      }
    });
    Button larger = new Button(buttonComp,SWT.PUSH);
    larger.setImage(SWTUtil.getImage("list-add.png"));
    larger.setToolTipText(i18n.tr("Bild vergrößern"));
    larger.setLayoutData(new GridData());
    larger.addSelectionListener(new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetSelected(SelectionEvent e)
      {
        resize(30);
      }
    });
    

    MatrixCode code = new MatrixCode(this.hhduc);
    InputStream stream = new ByteArrayInputStream(code.getImage());
    
    this.image = SWTUtil.getImage(stream);
    this.initialSize = this.image.getBounds().width;
    
    // Breite des Dialogs ermitteln (+ ein paar Pixel Toleranz am Rand)
    int width = image.getBounds().width + 250;
    
    this.imageLabel = new Label(container.getComposite(),SWT.NONE);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.horizontalAlignment = SWT.CENTER;
    this.imageLabel.setLayoutData(gd);
    this.imageLabel.setImage(image);
    
    this.setSize(width,SWT.DEFAULT);

    // Hier stehen dann noch die Anweisungen von der Bank drin
    super.paint(parent);
    getShell().setMinimumSize(getShell().computeSize(width,SWT.DEFAULT));
    
    // Checken, ob wir gespeicherte Resize-Werte haben, wenn ja gleicht resizen
    int scaled = SETTINGS.getInt("resize." + this.initialSize,0);
    if (scaled != 0)
      this.resize(scaled);
    
  }
  
  /**
   * Aendert die Groesse der Matrix-Grafik um die angegebene Anzahl Pixel.
   * @param resize die Anzahl der Pixel, um die die Grafik vergroessert/verkleinert werden soll.
   */
  private void resize(int resize)
  {
    final Rectangle rect = this.image.getBounds();
    
    int width = rect.width + resize;
    int height = rect.height + resize;
    Image scaled = new Image(GUI.getDisplay(), width, height);
    final GC gc = new GC(scaled);
    gc.setAntialias(SWT.ON);
    gc.setInterpolation(SWT.HIGH);
    gc.drawImage(this.image, 0, 0, rect.width, rect.height, 0, 0, width, height);
    gc.dispose();
    
    if (!this.image.isDisposed())
      this.image.dispose();

    this.image = scaled;

    this.imageLabel.setImage(this.image);
    this.imageLabel.setSize(width,height);
    this.imageLabel.getParent().layout(true);

    // Dialog-Groesse mit anpassen
    final Shell shell = this.getShell();
    final Point sh = shell.getSize();
    shell.setSize(sh.x,sh.y + resize);
    
    // Neue Groesse abspeichern - pro Ausgangsproesse
    SETTINGS.setAttribute("resize." + this.initialSize,width - this.initialSize);
  }
}
