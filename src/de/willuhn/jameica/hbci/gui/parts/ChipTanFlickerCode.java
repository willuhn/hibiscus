package de.willuhn.jameica.hbci.gui.parts;
/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/Attic/ChipTanFlickerCode.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/09/02 22:35:37 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Implementierung des Flicker-Codes fuer optisches ChipTAN.
 * Dies ist eine 1:1 Portierung der Javascript-Implementierung von
 * http://6xq.net/media/00/20/flickercode.html
 */
public class ChipTanFlickerCode
{

  /**
   * Hilfsklasse zu Berechnung der Codes.
   */
  private static class FlickerCode
  {
    private String code = null;
    
    /**
     * ct.
     * @param code der Code.
     * @throws Exception wenn der Code falsch ist.
     */
    private FlickerCode(String code) throws Exception
    {
      this.code = code.toUpperCase().replaceAll("[^A-F0-9]","");
//      if (!this.checksum())
//        throw new Exception("Flicker-Code ungültig");
    }

    /**
     * Liefert den Code.
     * @return der Code.
     */
    private String getCode()
    {
      return this.code;
    }

    /**
     * Wandelt die Zahl in Hex um.
     * @param n die Zahl.
     * @param minLength Die Mindestlaenge. Der Hex-Code wird dann vorn
     * mit Nullen aufgefuellt.
     * @return der Hex-Code.
     */
    private String toHex(int n,int minLength)
    {
      String s = Integer.toString(n,16).toUpperCase();
      while (s.length() < minLength)
        s = "0" + s;
      return s;
    }
    
    /**
     * Berechnet die Quersumme.
     * @param n die Zahl, deren Quersumme errechnet werden soll.
     * @return
     */
    private int quersumme(int n)
    {
      int q = 0;
      while (n != 0)
      {
        q += n % 10;
        n = (int) Math.floor(n / 10);
      }
      return q;
    }

    /**
     * Liefert den Payload.
     * @return der Payload.
     */
    private String getPayload()
    {
      int i = 0;
      String payload = "";
      int len = Integer.parseInt(this.code.substring(0,2),16);
      i += 2;
      while (i < code.length()-2)
      {
        /* skip bcd identifier */
        i += 1;
        /* parse length */
        len = Integer.parseInt(this.code.substring(i,i+1),16);
        i += 1;
        payload += code.substring(i,i+len*2);
        i += len*2;
      }
      return payload;
    }
    
    /**
     * Prueft, ob die Checksumme korrekt ist.
     * @return true, wenn die Checksumme korrekt ist.
     */
    private boolean checksum()
    {
      try
      {
        String test = this.code;
        
        /* length check: first byte */
        int len = test.length() / 2 - 1;
        test = toHex(len,2) + test.substring(2);
        
        /* luhn checksum */
        String luhndata = getPayload();
        int luhnsum = 0;
        for (int i=0; i<luhndata.length(); i+=2)
        {
          luhnsum += (1*Integer.parseInt(Character.toString(luhndata.charAt(i)),16)) + quersumme(2*Integer.parseInt(Character.toString(luhndata.charAt(i+1)),16));
        }
        luhnsum = (10 - (luhnsum % 10)) % 10;
        test = test.substring(0,test.length()-2) + toHex(luhnsum,1) + test.substring(test.length()-1);
        
        /* xor checksum */
        int xorsum = 0;
        for (int i=0; i< test.length()-2; i++)
        {
          xorsum ^= Integer.parseInt(Character.toString(test.charAt(i)),16);
        }
        test = test.substring(0,test.length()-1) + toHex(xorsum,1);
        return test.equals(this.code);
      }
      catch (Exception e)
      {
        return false;
      }
    }
  }
  
  /**
   * Hilfsklasse fuer das Canvas.
   */
  private static class FlickerCanvas
  {
    private String code          = null;
    private int halfbyteid       = 0;
    private int clock            = 0;
    private List<int[]> bitarray = null;
    private Canvas canvas        = null;
    private boolean running      = false;
    
    /**
     * ct.
     * @param newcode
     */
    private FlickerCanvas(Canvas canvas)
    {
      this.canvas = canvas;
      this.canvas.addPaintListener(new PaintListener() {
        public void paintControl(PaintEvent e)
        {
          // solange wir noch keinen Code haben, muessen wir
          // nichts zeichnen
          if (code == null)
            return;
          step(e.gc);
        }
      });
    }
    
    /**
     * Initialisiert den Flicker-Code.
     * @param code der Flicker-Code.
     */
    private void setCode(FlickerCode code)
    {
      this.code = code.getCode();

      /* prepend synchronization identifier */
      this.code = "0FFF" + this.code;

      this.halfbyteid = 0;
      this.clock      = 0;

      Map<String,int[]> bits = new HashMap<String,int[]>();
      /* bitfield: clock, bits 2^1, 2^2, 2^3, 2^4 */
      bits.put("0",new int[]{0, 0, 0, 0, 0});
      bits.put("1",new int[]{0, 1, 0, 0, 0});
      bits.put("2",new int[]{0, 0, 1, 0, 0});
      bits.put("3",new int[]{0, 1, 1, 0, 0});
      bits.put("4",new int[]{0, 0, 0, 1, 0});
      bits.put("5",new int[]{0, 1, 0, 1, 0});
      bits.put("6",new int[]{0, 0, 1, 1, 0});
      bits.put("7",new int[]{0, 1, 1, 1, 0});
      bits.put("8",new int[]{0, 0, 0, 0, 1});
      bits.put("9",new int[]{0, 1, 0, 0, 1});
      bits.put("A",new int[]{0, 0, 1, 0, 1});
      bits.put("B",new int[]{0, 1, 1, 0, 1});
      bits.put("C",new int[]{0, 0, 0, 1, 1});
      bits.put("D",new int[]{0, 1, 0, 1, 1});
      bits.put("E",new int[]{0, 0, 1, 1, 1});
      bits.put("F",new int[]{0, 1, 1, 1, 1});


      this.bitarray = new ArrayList<int[]>();
      for (int i = 0; i < this.code.length(); i += 2) {
        bitarray.add(bits.get(Character.toString(this.code.charAt(i+1))));
        bitarray.add(bits.get(Character.toString(this.code.charAt(i))));
      }
    }

    /**
     * Zeichnet den naechsten Schritt auf dem Canvas.
     * @ctx der Graphics-Context.
     */
    private void step(GC ctx)
    {
      int margin = 7;
      int barwidth = canvas.getSize().x / 5;
      bitarray.get(halfbyteid)[0] = clock;
      
      for (int i = 0; i < 5; i++)
      {
        if (bitarray.get(halfbyteid)[i] == 1)
        {
          ctx.setBackground(canvas.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        }
        else
        {
          ctx.setBackground(canvas.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        }
        ctx.fillRectangle(i*barwidth+margin,margin,barwidth-2*margin,canvas.getSize().y-2*margin);
      }
      
      clock--;
      if (clock < 0)
      {
        clock = 1;
        halfbyteid++;
        if (halfbyteid >= bitarray.size())
        {
          halfbyteid = 0;
        }
      }
    }
    
    /**
     * Startet den Flicker-Code.
     */
    private synchronized void start()
    {
      if (running)
        return;
      running = true;

      Thread t = new Thread("Flicker Update-Thread") {
        
        /**
         * @see java.lang.Thread#run()
         */
        public void run()
        {
          try
          {
            while (running && canvas != null && !canvas.isDisposed())
            {
              canvas.getDisplay().syncExec(new Runnable() {
                /**
                 * @see java.lang.Runnable#run()
                 */
                public void run()
                {
                  canvas.redraw();
                }
              });
              sleep(100L);
            }
          }
          catch (InterruptedException e) {/* Ende */}
        }
      };
      t.start();
    }
    
    /**
     * Stoppt den Flicker-Code.
     */
    private void stop()
    {
      this.running = false;
    }
  }
  
  
  /**
   * @param args
   * @throws Exception
   */
  public final static void main(String[] args) throws Exception
  {
    Display display = Display.getDefault();
    Shell shell = new Shell(display);
    shell.setLayout(new FillLayout());
    shell.setText("Flicker-Test");
    shell.setSize(350,300);
    
    Composite comp = new Composite(shell,SWT.NONE);
    comp.setLayout(new GridLayout());
    
    Canvas canvas = new Canvas(comp,SWT.NONE);
    canvas.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
    canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
    canvas.setSize(205,100);
    
    final FlickerCanvas c = new FlickerCanvas(canvas);
    
    final Text text = new Text(comp,SWT.SINGLE | SWT.BORDER);
    text.setText("11 04 871 49552 05 123456789F 14 302C3031 07");
    text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    final Label error = new Label(comp,SWT.NONE);
    error.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Button start = new Button(comp,SWT.PUSH);
    start.setText("Start");
    start.setLayoutData(new GridData(GridData.BEGINNING));
    start.addSelectionListener(new SelectionAdapter() {

      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetSelected(SelectionEvent e)
      {
        try
        {
          String s = text.getText();
          if (s == null || s.length() == 0)
          {
            c.stop();
            return;
          }
          c.setCode(new FlickerCode(s));
          c.start();
        }
        catch (Exception ex)
        {
          error.setText(ex.getMessage());
        }
      }
    });
    
    Button stop = new Button(comp,SWT.PUSH);
    stop.setText("Stop");
    stop.setLayoutData(new GridData(GridData.BEGINNING));
    stop.addSelectionListener(new SelectionAdapter() {

      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetSelected(SelectionEvent e)
      {
        c.stop();
      }
    });
    
    shell.open();
    while (!shell.isDisposed())
    {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }
}



/**********************************************************************
 * $Log: ChipTanFlickerCode.java,v $
 * Revision 1.1  2010/09/02 22:35:37  willuhn
 * @N Test fuer ChipTAN-Flicker-Code
 *
 **********************************************************************/