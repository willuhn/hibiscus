/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.pintan;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.kapott.hbci.manager.FlickerRenderer;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Dialog f�r die ChipTAN-Eingabe.
 */
public class ChipTANDialog extends TANDialog
{
  private final static Settings settings = new Settings(ChipTANDialog.class);
  
  private String code = null;
  
  /**
   * ct.
   * @param config die PINTAN-Config.
   * @param code der Flicker-Code.
   * @throws RemoteException
   * @throws ApplicationException wenn der Flicker-Code nicht geparst werden konnte.
   */
  public ChipTANDialog(PinTanConfig config, String code) throws RemoteException, ApplicationException
  {
    super(config);
    this.code = code;
    this.setSideImage(null); // den Platz haben wir hier nicht.
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent);
    container.addHeadline(i18n.tr("Flicker-Grafik"));
    container.addText(i18n.tr("Klicken Sie \"-\" bzw. \"+\", um die Breite anzupassen."),true);
    
    FlickerPart flicker = new FlickerPart(this.code);
    flicker.paint(parent);
    
    // Hier stehen dann noch die Anweisungen von der Bank drin
    super.paint(parent);
    getShell().setMinimumSize(getShell().computeSize(SWT.DEFAULT,SWT.DEFAULT));
  }

  /**
   * Implementiert die Flicker-Grafik.
   */
  private class FlickerPart extends FlickerRenderer implements Part
  {
    private Composite comp = null;
    private Canvas canvas  = null;
    private boolean[] bits = new boolean[5];
    
    private Color black = GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK);
    private Color white = GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE);
    
    /**
     * ct.
     * @param code der anzuzeigende Flicker-Code.
     * @throws ApplicationException
     */
    private FlickerPart(String code) throws ApplicationException
    {
      super(code);
      setFrequency(settings.getInt("freq",FlickerRenderer.FREQUENCY_DEFAULT));
    }
    
    /**
     * @see org.kapott.hbci.manager.FlickerRenderer#paint(boolean, boolean, boolean, boolean, boolean)
     */
    public void paint(boolean b1, boolean b2, boolean b3, boolean b4, boolean b5)
    {
      this.bits[0] = b1;
      this.bits[1] = b2;
      this.bits[2] = b3;
      this.bits[3] = b4;
      this.bits[4] = b5;
      
      if (canvas == null || canvas.isDisposed())
        return;
      
      // Redraw ausloesen
      // Wir sind hier nicht im Event-Dispatcher-Thread von SWT. Daher uebergeben wir das an diesen
      canvas.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          // Wir muessen hier nochmal checken, weil das inzwischen disposed sein kann.
          if (canvas.isDisposed())
            return;
            
          // Neu zeichnen
          canvas.redraw();
        }
      });
    }

    /**
     * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
     */
    public void paint(final Composite parent) throws RemoteException
    {
      if (this.canvas != null)
        return;

      ////////////////////////////////////////////////////////////////////////
      // die beiden Buttons zum Vergroessern und Verkleinern
      {
        Composite buttonComp = new Composite(parent,SWT.NONE);
        GridData buttonGd = new GridData();
        buttonGd.horizontalAlignment = SWT.CENTER;
        buttonComp.setLayoutData(buttonGd);
        buttonComp.setLayout(new GridLayout(5,false));
        
        final Label label1 = new Label(buttonComp,SWT.NONE);
        label1.setLayoutData(new GridData());
        label1.setText(i18n.tr("Breite"));
        
        Button smaller = new Button(buttonComp,SWT.PUSH);
        smaller.setToolTipText(i18n.tr("Flicker-Code verkleinern"));
        smaller.setLayoutData(new GridData());
        smaller.setText(" - ");
        smaller.addSelectionListener(new SelectionAdapter()
        {
          /**
           * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
           */
          public void widgetSelected(SelectionEvent e)
          {
            GridData gd = (GridData) comp.getLayoutData();
            if (gd.widthHint < SWTUtil.mm2px(20)) // unplausibel
              return;
            gd.widthHint -= 5;
            
            Point newSize = new Point(gd.widthHint,comp.getSize().y);
            comp.setSize(newSize);
            parent.layout(); // zentriert den Flicker-Code wieder
          }
        });
        Button larger = new Button(buttonComp,SWT.PUSH);
        larger.setToolTipText(i18n.tr("Flicker-Code vergr��ern"));
        larger.setLayoutData(new GridData());
        larger.setText(" + ");
        larger.addSelectionListener(new SelectionAdapter()
        {
          /**
           * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
           */
          public void widgetSelected(SelectionEvent e)
          {
            GridData gd = (GridData) comp.getLayoutData();
            
            // Eigentlich sollten 120mm als Grenze reichen. Wenn aber ein HighDPI-System falsche
            // DPI-Zahlen zurueckliefert, koennte das zu wenig sein. Siehe BUGZILLA 1565
            if (gd.widthHint > SWTUtil.mm2px(400))
              return;
            gd.widthHint += 5;

            Point newSize = new Point(gd.widthHint,comp.getSize().y);
            comp.setSize(newSize);
            parent.layout(); // zentriert den Flicker-Code wieder
          }
        });
        
        final Label label2 = new Label(buttonComp,SWT.NONE);
        label2.setLayoutData(new GridData());
        label2.setText(i18n.tr("Geschwindigkeit"));
        
        final Spinner spinner = new Spinner(buttonComp,SWT.BORDER);
        spinner.setToolTipText(i18n.tr("Geschwindigkeit in Hz (1/Sekunde)"));
        spinner.setLayoutData(new GridData());
        spinner.setSelection(settings.getInt("freq",14));
        spinner.setMinimum(FlickerRenderer.FREQUENCY_MIN);
        spinner.setMaximum(FlickerRenderer.FREQUENCY_MAX);
        spinner.setIncrement(1);
        spinner.setTextLimit(2);
        spinner.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e)
          {
            int freq = spinner.getSelection();
            settings.setAttribute("freq",freq);
            setFrequency(freq);
          }
        });
      }
      //
      ////////////////////////////////////////////////////////////////////////
      

      //////////////////////////////////////////////////////////////////////////
      // Das Composite fuer den Flicker-Code
      {
        this.comp = new Composite(parent,SWT.BORDER);
        this.comp.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));

        final GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        int width = SWTUtil.mm2px(60); // das muesste ca. die Breite von ReinerSCT-Geraeten sein
        if (width == -1) width = 206;  // falls die Umrechnung nicht klappte
        gd.widthHint = settings.getInt("width",width);

        int height = SWTUtil.mm2px(40);
        if (height == -1) width = 140;
        gd.heightHint = height;
        
        this.comp.setLayoutData(gd);

        // Wir lassen etwas Abstand zum Rand
        GridLayout gl = new GridLayout();
        gl.marginHeight = 20;
        gl.marginWidth = 20;
        this.comp.setLayout(gl);
        
        // Beim Disposen stoppen wir den Flicker-Thread.
        this.comp.addDisposeListener(new DisposeListener()
        {
          public void widgetDisposed(DisposeEvent e)
          {
            // Wir merken uns die Groesse des Canvas.
            Logger.info("saving width of flickercode: " + gd.widthHint + " px");
            settings.setAttribute("width",gd.widthHint);
          }
        });
      }
      //
      //////////////////////////////////////////////////////////////////////////

      //////////////////////////////////////////////////////////////////////////
      // Das eigentliche Canvas mit dem Flicker-Code.
      this.canvas = new Canvas(this.comp,SWT.NONE);
      this.canvas.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
      this.canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
      
      // Bei jedem Paint-Event aktualisieren wir die Balken
      this.canvas.addPaintListener(new PaintListener()
      {
        public void paintControl(PaintEvent e)
        {
          update(e.gc);
        }
      });
      this.canvas.addDisposeListener(new DisposeListener() {
        public void widgetDisposed(DisposeEvent e)
        {
          // Update-Thread stoppen
          stop();
        }
      });
      //
      //////////////////////////////////////////////////////////////////////////


      
      // Und los gehts
      this.start();
    }

    /**
     * Aktualisiert die Grafik basierend auf dem aktuellen Code.
     * @ctx der Graphics-Context.
     */
    private void update(GC ctx)
    {
      int barwidth = canvas.getSize().x / 5;
      int margin = barwidth / 4;
      barwidth -= margin; // Rand noch abziehen
      barwidth += margin/4; // und dann nochmal ein Viertel des Randes dran, damit wir am Ende keinen schwarzen Rand haben
      
      Point size = canvas.getSize();
      for (int i=0;i<this.bits.length;++i)
      {
        ctx.setBackground(this.bits[i] ? white : black);
        ctx.fillRectangle(i*(barwidth+margin),12,barwidth,size.y-12); // die 12 sind der Abstand von oben (wegen den Dreiecken)
      }
      
      // Die Kalibrierungs-Dreiecke noch reinmalen
      ctx.setBackground(white);
      
      // Breite und Hoehe der Dreiecke
      int width  = 12;
      int height = 6;
      
      // Abstand vom Rand -> halbe Balkenbreite minus halbe Dreieck-Breite
      int gap = barwidth / 2 - (width/2);
      
      int[] left  = new int[]{gap,0,               gap+width,0,    gap+(width/2),height};
      int[] right = new int[]{size.x-gap-width,0,  size.x-gap,0,   size.x-gap-(width/2),height};
      ctx.fillPolygon(left);
      ctx.fillPolygon(right);
    }
  }
}

