/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/ChipTANDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/12/08 12:34:57 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.pintan;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog für die ChipTAN-Eingabe.
 */
public class ChipTANDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static int WINDOW_WIDTH = 550;

  private PinTanConfig config = null;
  
  /**
   * ct.
   * @param config die PINTAN-Config.
   * @throws RemoteException
   */
  public ChipTANDialog(PinTanConfig config) throws RemoteException
  {
    super(ChipTANDialog.POSITION_CENTER);
    
    this.config = config;
		this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    FlickerPart flicker = new FlickerPart(null);
    flicker.paint(parent);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }
  
  
  /**
   * Implementiert die Flicker-Grafik.
   */
  private class FlickerPart implements Part
  {
    private int halfbyteid       = 0;
    private int clock            = 0;
    private List<int[]> bitarray = null;
    private Canvas canvas        = null;
    
    /**
     * ct.
     * @param code der anzuzeigende Flicker-Code.
     * @throws ApplicationException
     */
    private FlickerPart(String code) throws ApplicationException
    {
      // 11048714955205123456789F14302C303107 // aus der JS-Demo
      // 002624088715131306389726041,00       // von http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=60532
      // 100484652456044356435F14312C30304B   // von bankingportal.sparkasse-freiburg.de

      if (code == null || code.length() == 0)
        throw new ApplicationException(i18n.tr("Kein Flicker-Code angegeben"));

      // Sync-Identifier vorn dran haengen.
      code = "0FFF" + code;

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
      for (int i = 0; i < code.length(); i += 2) {
        bitarray.add(bits.get(Character.toString(code.charAt(i+1))));
        bitarray.add(bits.get(Character.toString(code.charAt(i))));
      }
    
    }
    
    /**
     * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
     */
    public void paint(Composite parent) throws RemoteException
    {
      if (this.canvas != null)
        return;
      
      this.canvas = new Canvas(parent,SWT.BORDER);
      this.canvas.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
      this.canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
      this.canvas.setSize(205,100);

      this.canvas.addPaintListener(new PaintListener()
      {
        public void paintControl(PaintEvent e)
        {
          step(e.gc);
        }
      });

      Thread thread = new Thread("Flicker Update-Thread")
      {
        public void run()
        {
          try
          {
            while (canvas != null && !canvas.isDisposed())
            {
              canvas.getDisplay().syncExec(new Runnable() {
                public void run()
                {
                  canvas.redraw();
                }
              });
              sleep(60L);
            }
          }
          catch (InterruptedException e) {/* Ende */}
        }
      };
      thread.start();
    }

    /**
     * Zeichnet den naechsten Schritt auf dem Canvas.
     * @ctx der Graphics-Context.
     */
    private void step(GC ctx)
    {
      int margin = 4;
      int barwidth = canvas.getSize().x / 5;
      bitarray.get(halfbyteid)[0] = clock;
      
      for (int i = 0; i < 5; i++)
      {
        int color = (bitarray.get(halfbyteid)[i] == 1) ? SWT.COLOR_WHITE : SWT.COLOR_BLACK;
        ctx.setBackground(canvas.getDisplay().getSystemColor(color));
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
  }

  /**
   * Implementierung des Flicker-Codes fuer optisches ChipTAN.
   * Dies ist eine 1:1 Portierung der Javascript-Implementierung von
   * http://6xq.net/media/00/20/flickercode.html
   */
  private class FlickerCode
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
    private boolean checksum() throws Exception
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
  }
}


/**********************************************************************
 * $Log: ChipTANDialog.java,v $
 * Revision 1.1  2010/12/08 12:34:57  willuhn
 * @C Flicker-Code in Dialog verschoben
 *
 **********************************************************************/