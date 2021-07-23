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
 * Dialog für die PhotoTAN und QRTAN-Eingabe.
 */
public class PhotoTANDialog extends TANDialog
{
  private final static Settings SETTINGS = new Settings(PhotoTANDialog.class);
  
  private byte[] bytes = null;
  private int initialSize = 0;
  private int currentSize = 0;
  private Image image = null;
  private Label imageLabel = null;
  
  private Button smaller = null;
  private Button larger = null;
  
  private final Listener resizeListener = new DelayedListener(new Listener()
  {
    @Override
    public void handleEvent(Event event)
    {
      storeSize();
    }
  });
  
  /**
   * ct.
   * @param config die PINTAN-Config.
   * @param bytes die Bytes des Bildes.
   * @throws RemoteException
   * @throws ApplicationException wenn die Grafik nicht geparst werden konnte.
   */
  public PhotoTANDialog(PinTanConfig config, byte[] bytes) throws RemoteException, ApplicationException
  {
    super(config);
    this.bytes = bytes;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.TANDialog#extendTop(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void extendTop(Container container) throws Exception
  {
    final Container c = new SimpleContainer(container.getComposite(),false,1);
    Composite buttonComp = new Composite(c.getComposite(),SWT.NONE);
    
    GridData buttonGd = new GridData();
    buttonGd.horizontalAlignment = SWT.CENTER;
    buttonComp.setLayoutData(buttonGd);
    buttonComp.setLayout(new GridLayout(3,true));
    
    this.smaller = new Button(buttonComp,SWT.PUSH);
    this.smaller.setToolTipText(i18n.tr("Bild verkleinern"));
    this.smaller.setImage(SWTUtil.getImage("list-remove.png"));
    this.smaller.setLayoutData(new GridData());
    this.smaller.addSelectionListener(new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetSelected(SelectionEvent e)
      {
        resize(currentSize - 30);
      }
    });
    this.smaller.setEnabled(currentSize > 30);

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

    this.larger = new Button(buttonComp,SWT.PUSH);
    this.larger.setImage(SWTUtil.getImage("list-add.png"));
    this.larger.setToolTipText(i18n.tr("Bild vergrößern"));
    this.larger.setLayoutData(new GridData());
    this.larger.addSelectionListener(new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetSelected(SelectionEvent e)
      {
        resize(currentSize + 30);
      }
    });
    this.larger.setEnabled(currentSize < 1000);
    
    this.image = SWTUtil.getImage(new ByteArrayInputStream(this.bytes));
    this.initialSize = this.image.getBounds().width;
    this.currentSize = this.initialSize;
    
    this.imageLabel = new Label(c.getComposite(),SWT.NONE);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.horizontalAlignment = SWT.CENTER;
    gd.grabExcessVerticalSpace = false;
    this.imageLabel.setLayoutData(gd);
    this.imageLabel.setImage(image);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.TANDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    super.paint(parent);
    
    // Checken, ob wir gespeicherte Resize-Werte haben, wenn ja gleich resizen
    int resize = SETTINGS.getInt("resize." + this.initialSize,this.initialSize);
    if (resize <= 0)
    {
      Logger.warn("ignoring invalid resize value: " + resize + ", using initial size instead: " + this.initialSize);
      resize = this.initialSize;
    }
    
    this.resize(resize);
    
    // Checken, ob wir eine gespeicherte Dialoggroesse haben
    int windowWidth = SETTINGS.getInt("width." + this.initialSize,0);
    int windowHeight = SETTINGS.getInt("height." + this.initialSize,0);
    if (windowWidth > 0 && windowHeight > 0)
    {
      this.setSize(windowWidth,windowHeight);
    }
    else
    {
      int width = resize + 100;
      final int displayHeight = GUI.getDisplay().getBounds().height;
      Point p = this.getShell().computeSize(width,SWT.DEFAULT);
      int height = Math.min(p.y, displayHeight);
      this.setSize(width,height);
    }
    
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
    

    Logger.debug("saving window size: " + p.x + "x" + p.y);
    SETTINGS.setAttribute("width." + initialSize,p.x);
    SETTINGS.setAttribute("height." + initialSize,p.y);
  }
  
  /**
   * Aendert die Groesse der Matrix-Grafik um die angegebene Anzahl Pixel.
   * @param newSize die neue Breite des Bildes.
   */
  private void resize(int newSize)
  {
    Image tmp = null;
    
    try
    {
      Logger.debug("resize phototan image to new size: " + newSize);
      
      this.smaller.setEnabled(newSize > 50);
      this.larger.setEnabled(newSize < 1000);

      if (newSize < 1 || newSize > 1000)
      {
        Logger.warn("got invalid size: " + newSize);
        return;
      }
      
      tmp = SWTUtil.getImage(new ByteArrayInputStream(this.bytes));
      final Rectangle rect = tmp.getBounds();
      final Image scaled = new Image(GUI.getDisplay(), newSize, newSize);
      final GC gc = new GC(scaled);
      gc.setAntialias(SWT.ON);
      gc.setInterpolation(SWT.HIGH);
      gc.drawImage(tmp, 0, 0, rect.width, rect.height, 0, 0, newSize, newSize);
      gc.dispose();
      
      this.imageLabel.setImage(scaled);
      this.imageLabel.setSize(newSize,newSize);
      this.imageLabel.getParent().layout(true);
      
      if (this.image != null && !this.image.isDisposed())
        this.image.dispose();
      
      this.image = scaled;

      // Dialog-Groesse mit anpassen
      final Shell shell = this.getShell();
      shell.setSize(shell.computeSize(shell.getSize().x,SWT.DEFAULT));

      this.currentSize = newSize;
      
      // Neue Groesse abspeichern - pro Ausgangsproesse
      SETTINGS.setAttribute("resize." + this.initialSize,this.currentSize);
    }
    catch (Exception e)
    {
      Logger.error("unable to resize photo tan dialog",e);
    }
    finally
    {
      if (tmp != null && !tmp.isDisposed())
        tmp.dispose();
    }
  }
}
