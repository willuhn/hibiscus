/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.services.TransportService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.ConsoleMonitor;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.transport.Transport;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum manuellen Loesen eines Captcha.
 */
public class CaptchaDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private byte[] image = null;
  private String data  = null;
  
  private TextInput solution = null;
  private Button applyButton = null;
  
  
  /**
   * ct.
   * @param url URL des Bildes.
   * @param position
   * @throws ApplicationException
   */
  public CaptchaDialog(String url, int position) throws ApplicationException
  {
    super(position,false);
    this.setTitle(i18n.tr("Captcha lösen"));
    this.setPanelText(i18n.tr("Bitte geben Sie den in der Grafik angezeigten Text ein."));
    
    try
    {
      TransportService ts = Application.getBootLoader().getBootable(TransportService.class);
      Transport t = ts.getTransport(new URL(url));
      
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      t.get(bos,new ConsoleMonitor());
      this.image = bos.toByteArray();
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to fetch image",e);
      throw new ApplicationException(i18n.tr("Download des Captcha-Bildes fehlgeschlagen: {0}",e.getMessage()));
    }
    
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent);
    
    createCaptcha(container);
    
    container.addInput(this.getSolution());
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(this.getApplyButton());
    buttons.addButton(i18n.tr("Abbrechen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException("Dialog abgebrochen");
      }
    },null,false,"process-stop.png");
    container.addButtonArea(buttons);

    // Apply-Button erst freischalten, wenn eine Loesung eingegeben wurde
    getSolution().getControl().addKeyListener(new KeyAdapter()
    {
      public void keyReleased(KeyEvent e)
      {
        String s = (String) getSolution().getValue();
        getApplyButton().setEnabled(StringUtils.trimToNull(s) != null);
      }
    });

    this.getShell().setMinimumSize(450,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.data;
  }

  /**
   * Erzeugt die Captcha-Grafik.
   * @param container der Container, in dem die Captcha-Grafik gezeichnet werden soll.
   */
  private void createCaptcha(Container container)
  {
    ////////////////////////////////////////////////////////////////////////////
    // Das Label
    final GridData labelGrid = new GridData(GridData.HORIZONTAL_ALIGN_END);
    labelGrid.verticalAlignment = GridData.BEGINNING;
    final Label label = GUI.getStyleFactory().createLabel(container.getComposite(),SWT.NONE);
    label.setText(i18n.tr("Captcha"));
    label.setLayoutData(labelGrid);
    //
    ////////////////////////////////////////////////////////////////////////////

    final Image image = new Image(GUI.getDisplay(),new ByteArrayInputStream(this.image));
    final Rectangle size = image.getBounds();

    final Composite comp = new Composite(container.getComposite(),SWT.BORDER);
    comp.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    final GridData gd = new GridData(GridData.FILL_BOTH);
    gd.widthHint  = size.width + 2;
    gd.heightHint = size.height + 2;
    comp.setLayoutData(gd);

    // Wir lassen etwas Abstand zum Rand
    final GridLayout gl = new GridLayout();
    gl.marginHeight = 2;
    gl.marginWidth = 2;
    comp.setLayout(gl);

    final Canvas captcha = new Canvas(comp,SWT.NONE);
    captcha.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    captcha.setLayoutData(new GridData(GridData.FILL_BOTH));
    captcha.addPaintListener(new PaintListener()
    {
      public void paintControl(PaintEvent e)
      {
        Point area = captcha.getSize();
        e.gc.drawImage(image,(area.x - size.width) / 2,(area.y - size.height) / 2);
      }
    });
  }
  
  /**
   * Liefert ein Eingabefeld, in dem die Loesung eingetragen wird.
   * @return Eingabefeld.
   */
  private TextInput getSolution()
  {
    if (this.solution != null)
      return this.solution;
    
    this.solution = new TextInput(null);
    this.solution.setName(i18n.tr("Lösung"));
    this.solution.setMandatory(true);
    return this.solution;
  }
  
  /**
   * Liefert den Apply-Button.
   * @return der Apply-Button.
   */
  private Button getApplyButton()
  {
    if (this.applyButton != null)
      return this.applyButton;
    
    this.applyButton = new Button(i18n.tr("Übernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        data = (String) getSolution().getValue();
        if (StringUtils.trimToNull(data) != null)
          close();
      }
    },null,true,"ok.png");
    this.applyButton.setEnabled(false);
    return this.applyButton;
  }

}
