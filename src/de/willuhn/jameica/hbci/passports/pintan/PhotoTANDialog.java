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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.kapott.hbci.manager.MatrixCode;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Dialog für die PhotoTAN-Eingabe.
 */
public class PhotoTANDialog extends TANDialog
{
  private final static Settings SETTINGS = new Settings(PhotoTANDialog.class);
  
  private String hhduc = null;
  private int initialSize = 0;
  private int currentSize = 0;
  private Image image = null;
  private Label imageLabel = null;
  
  private final Listener resizeListener = new DelayedListener(new Listener() {
    @Override
    public void handleEvent(Event event)
    {
      storeSize();
    }
  });
  
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
    buttonComp.setLayout(new GridLayout(3,true));
    
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
        resize(currentSize - 30);
      }
    });
    Button reset = new Button(buttonComp,SWT.PUSH);
    reset.setToolTipText(i18n.tr("Originale Bildgröße wiederherstellen"));
    reset.setImage(SWTUtil.getImage("view-fullscreen.png"));
    reset.setLayoutData(new GridData());
    reset.addSelectionListener(new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetSelected(SelectionEvent e)
      {
        resize(initialSize);
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
        resize(currentSize + 30);
      }
    });
    

    MatrixCode code = new MatrixCode(this.hhduc);
    InputStream stream = new ByteArrayInputStream(code.getImage());
    
    this.image = SWTUtil.getImage(stream);
    this.initialSize = this.image.getBounds().width;
    this.currentSize = this.initialSize;
    
    this.imageLabel = new Label(container.getComposite(),SWT.NONE);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.horizontalAlignment = SWT.CENTER;
    this.imageLabel.setLayoutData(gd);
    this.imageLabel.setImage(image);
    
    super.paint(parent);
    
    // Checken, ob wir gespeicherte Resize-Werte haben, wenn ja gleich resizen
    int resized = SETTINGS.getInt("resize." + this.initialSize,0);
    if (resized != 0)
      this.resize(resized);

    // Checken, ob wir eine gespeicherte Dialoggroesse haben
    int width = this.initialSize + 250;
    int windowWidth = SETTINGS.getInt("width." + this.initialSize,0);
    int windowHeight = SETTINGS.getInt("height." + this.initialSize,0);
    if (windowWidth > 0 && windowHeight > 0)
      this.setSize(windowWidth,windowHeight);
    else
      this.setSize(width,SWT.DEFAULT);
    
    getShell().setMinimumSize(getShell().computeSize(width,SWT.DEFAULT));
    this.getShell().addListener(SWT.Resize,this.resizeListener);
  }
  
  /**
   * Speichert die aktuelle Dialog-Groesse.
   */
  private void storeSize()
  {
    final Shell shell = this.getShell();
    if (shell == null || shell.isDisposed())
      return;
    
    final Point p = shell.getSize();
    if (p == null)
      return;
    
    Logger.info("saving window size: " + p.x + "x" + p.y);
    SETTINGS.setAttribute("width." + initialSize,p.x);
    SETTINGS.setAttribute("height." + initialSize,p.y);
  }
  
  /**
   * Aendert die Groesse der Matrix-Grafik um die angegebene Anzahl Pixel.
   * @param newSize die neue Breite des Bildes.
   */
  private void resize(int newSize)
  {
    int diff = newSize - this.currentSize;
    this.currentSize = newSize;
    final Rectangle rect = this.image.getBounds();
    
    int width = rect.width + diff;
    int height = rect.height + diff;
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
    shell.setSize(sh.x,sh.y + diff);
    
    // Neue Groesse abspeichern - pro Ausgangsproesse
    SETTINGS.setAttribute("resize." + this.initialSize,this.currentSize);
  }
}
