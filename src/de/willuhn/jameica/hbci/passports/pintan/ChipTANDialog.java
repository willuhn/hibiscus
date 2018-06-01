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

import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.kapott.hbci.manager.FlickerRenderer;
import org.kapott.hbci.smartcardio.ChipTanCardService;
import org.kapott.hbci.smartcardio.SmartCardService;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.JameicaCompat;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Customizing;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Dialog für die ChipTAN (OPTIC und USB).
 */
public class ChipTANDialog extends TANDialog
{
  private final static Settings settings = new Settings(ChipTANDialog.class);
  
  private String code = null;
  private boolean smallDisplay = false;
  private boolean usb = false;
  private ChipTanCardService service = null;
  
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
    this.checkUSB();
    Logger.info("using chiptan " + (this.usb ? "USB" : "OPTIC"));
    
    if (!this.usb)
      this.setSideImage(null); // den Platz haben wir hier nicht.
    
    this.smallDisplay = Customizing.SETTINGS.getBoolean("application.scrollview",false);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.passports.pintan.TANDialog#setText(java.lang.String)
   */
  @Override
  public void setText(String text)
  {
    // Ueberschrieben, um den im Fliesstext am Anfang enthaltenen Flicker-Code rauszuschneiden.
    text = StringUtils.trimToNull(text);
    if (text != null)
    {
      final String token2 = "CHLGTEXT";
      // Jetzt checken, ob die beiden Tokens enthalten sind
      int t1Start = text.indexOf("CHLGUC");
      int t2Start = text.indexOf(token2);
      if (t1Start == -1 || t2Start == -1 || t2Start <= t1Start)
      {
        // Ne, nicht enthalten
        super.setText(text);
        return;
      }
      
      // Alles bis zum Ende des zweiten Tocken abschneiden
      text = text.substring(t2Start + token2.length());

      // Jetzt noch diese 4-stellige Ziffer abschneiden, die auch noch hinten dran steht.
      // Aber nur, wenn auch noch relevant Text uebrig ist
      if (text.length() > 4)
      {
        String nums = text.substring(0,4);
        if (nums.matches("[0-9]{4}"))
          text = text.substring(4);
      }
    }
    
    super.setText(text);
    return;
  }
  
  /**
   * Findet heraus, ob alle Vorbedingungen fuer die Nutzung von ChipTAN USB erfuellt sind.
   * @throws RemoteException
   */
  private void checkUSB() throws RemoteException
  {
    // Checken, ob wir chipTAN USB ausgewaehlt haben
    PtSecMech mech = config != null ? config.getCurrentSecMech() : null;
    if (mech == null || !mech.useUSB())
      return; // brauchen wir gar nicht weiter ueberlegen
    
    // Versuchen, die Verbindung zum Kartenleser herzustellen
    try
    {
      this.service = SmartCardService.createInstance(ChipTanCardService.class,this.config != null ? StringUtils.trimToNull(this.config.getCardReader()) : null);

      // Wir haben grundsaetzlich einen Kartenleser.
      if (this.service != null && this.config != null)
      {
        if (config.isChipTANUSBAsked())
        {
          this.usb = this.config.isChipTANUSB();
        }
        else
        {
          // User fragen, ob er ihn auch nutzen moechte, wenn wir das noch nicht getan haben
          this.usb = Application.getCallback().askUser(i18n.tr("Es wurde ein USB-Kartenleser gefunden.\nMöchten Sie diesen zur Erzeugung der TAN verwenden?"),false);
          
          // Das Speichern der Antwort koennen wir nicht Jameica selbst ueberlassen, weil
          // die Entscheidung ja pro PIN/TAN-Config gelten soll und nicht global.
          this.config.setChipTANUSBAsked(true);
          this.config.setChipTANUSB(this.usb);
        }
      }
    }
    catch (Throwable t)
    {
      Logger.info("no chipcard reader found, chipTAN USB not available: " + t.getMessage());
      Logger.write(Level.DEBUG,"stacktrace for debugging purpose",t);
    }
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(final Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent);
    
    if (this.usb)
    {
      this.setShowPassword(true); // Bei ChipTAN USB immer die TAN anzeigen, damit der User vergleichen kann.
      container.addHeadline(i18n.tr("ChipTAN USB"));
      container.addText(i18n.tr("Legen Sie die Chipkarte ein und folgen Sie den Anweisungen des Kartenlesers. Klicken Sie auf \"OK\", wenn die TAN korrekt übertragen wurde."),true);

      ProgressBar progress = new ProgressBar(container.getComposite(), SWT.INDETERMINATE);
      final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      gd.horizontalSpan = 2;
      progress.setLayoutData(gd);

      final Thread usbThread = new Thread()
      {
        public void run()
        {
          try
          {
            Logger.info("trying to get TAN using USB cardreader");
            String s = StringUtils.trimToNull(service.getTan(code));
            if (s != null)
            {
              setPassword(s,parent);
            }
            else
            {
              throw new Exception(i18n.tr("Keine TAN übertragen"));
            }
          }
          catch (Exception e)
          {
            Logger.error("unable to get tan from chipcard",e);
            setErrorText(i18n.tr("Fehler bei TAN-Ermittlung: {0}",e.getMessage()));
          }
        }
      };
      usbThread.start();
      
      parent.addDisposeListener(new DisposeListener() {
        @Override
        public void widgetDisposed(DisposeEvent e)
        {
          // Nur fuer den Fall, dass der Thread noch laeuft.
          try
          {
            if (usbThread != null)
              usbThread.interrupt();
          }
          catch (Throwable th)
          {
            Logger.error("unable to interrupt USB thread",th);
          }
        }
      });
    }
    else
    {
      container.addHeadline(i18n.tr(this.usb ? "ChipTAN USB" : "Flicker-Grafik"));
      container.addText(i18n.tr("Klicken Sie \"-\" bzw. \"+\", um die Breite anzupassen."),true);
      FlickerPart flicker = new FlickerPart(this.code);
      flicker.paint(parent);
    }
    
    
    // Hier stehen dann noch die Anweisungen von der Bank drin
    super.paint(parent);

    if (!this.usb)
    {
      // Wir muessen das Fenster wenigstens gross genug machen, damit der Flickercode reinpasst, falls
      // der User den recht verbreitert hat
      int width = settings.getInt("width",-1);
      if (width > 0)
        width +=10; // Wenigstens noch 10 Pixel Rand hinzufuegen.
      // Wir nehmen den groesseren von beiden Werten
      
      getShell().setMinimumSize(getShell().computeSize(width > WINDOW_WIDTH ? width : WINDOW_WIDTH,smallDisplay ? 520 : SWT.DEFAULT));
    }
  }
  
  /**
   * Ueberschrieben, um Abwaertskompatibilitaet zu Jameica 2.8.0 herzustellen.
   * Dort war es noch nicht moeglich, das Passwort per Code zu setzen.
   * @param password das zu setzende Passwort.
   * @param parent das Composite.
   */
  private void setPassword(final String password, final Composite parent)
  {
    GUI.getDisplay().asyncExec(new Runnable() {
      
      @Override
      public void run()
      {
        setPassword(password);
        
        // Checken, ob es das Passwort-Eingabefeld schon als Member in der Klasse gibt
        try
        {
          Object input = JameicaCompat.get(this,null,"passwordInput");
          
          // OK, das Feld gibts. Dann ist die Version aktuell.
          if (input != null)
            return;

          // Feld gibts nicht. Dann muessen wir das Eingabefeld suchen
          applyPassword(password,parent.getChildren());
        }
        catch (Exception e)
        {
          Logger.error("unable to update password field",e);
        }
      }
    });
  }
  
  /**
   * Uebernimmt das Passwort.
   * @param password das Passwort.
   * @param controls die Controls.
   * @return true, wenn es uebernommen wurde.
   */
  private boolean applyPassword(String password, Control[] controls)
  {
    if (controls == null || controls.length == 0)
      return false;
    
    for (Control c:controls)
    {
      if (c instanceof Text)
      {
        Text t = (Text) c;
        if (!t.isDisposed())
        {
          // Das kann nur das Passwort-Feld sein.
          String value = StringUtils.trimToNull(t.getText());
          if (value == null)
          {
            t.setText(password);
            return true;
          }
        }
      }

      // Rekursion
      if (c instanceof Composite)
      {
        Composite comp = (Composite) c;
        boolean b = applyPassword(password, comp.getChildren());
        if (b)
          return true; // gefunden
      }
    }
    
    return false;
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
            gd.widthHint -= 8;
            
            Point newSize = new Point(gd.widthHint,comp.getSize().y);
            comp.setSize(newSize);
            parent.layout(); // zentriert den Flicker-Code wieder
          }
        });
        Button larger = new Button(buttonComp,SWT.PUSH);
        larger.setToolTipText(i18n.tr("Flicker-Code vergrößern"));
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
            gd.widthHint += 8;

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

        int height = SWTUtil.mm2px(smallDisplay ? 40 : 50);
        if (height == -1) width = smallDisplay ? 100 : 140;
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

