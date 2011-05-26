/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/ChipTANDialog.java,v $
 * $Revision: 1.7 $
 * $Date: 2011/05/26 10:13:18 $
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Dialog für die ChipTAN-Eingabe.
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

    // TODO: Entfernen, wenn optisches chipTAN fertig ist
    String msg = i18n.tr("Optisches chipTAN noch nicht unterstützt. Bitte wählen Sie ein anderes TAN-Verfahren.");
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(msg,StatusBarMessage.TYPE_ERROR));
    throw new OperationCanceledException(msg);
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent);
    container.addHeadline(i18n.tr("Flicker-Grafik"));
    container.addText(i18n.tr("Ändern Sie die Größe des Fensters so, dass die Flicker-Grafik von Ihrem TAN-Generator gelesen werden kann."),true);
    
    FlickerPart flicker = new FlickerPart(this.code);
    flicker.paint(parent);
    super.paint(parent);
    
    // Muessen wir ueberschreiben, damit der User den Flicker-Balken passend ziehen kann.
    getShell().setMinimumSize(100,100);
    
    // Geht leider nur mit dem Listener. Alles andere wird ignoriert
    addShellListener(new ShellAdapter() {
      public void shellActivated(ShellEvent e)
      {
        // Muesste ungefaehr die passende Groesse fuer den TAN-Generator von Reiner SCT sein.
        final int x = settings.getInt("size.x",269);
        final int y = settings.getInt("size.y",364);
        getShell().setSize(x,y);
      }
    });
    
    
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        // Wir merken uns die Groesse des Dialogs
        Point p = getShell().getSize();
        Logger.info("saving size of flicker dialog: " + p.x + "x" + p.y);
        settings.setAttribute("size.x",p.x);
        settings.setAttribute("size.y",p.y);
      }
    });
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
}


/**********************************************************************
 * $Log: ChipTANDialog.java,v $
 * Revision 1.7  2011/05/26 10:13:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2011-05-19 07:59:53  willuhn
 * @C optisches chipTAN voruebergehend deaktiviert, damit ich in Ruhe in hbci4Java an der Unterstuetzung weiterarbeiten kann
 *
 * Revision 1.5  2011-05-17 23:37:22  willuhn
 * @C Wir duerfen nicht einfach Zeichen entfernen
 *
 * Revision 1.4  2011-05-11 08:33:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2011-05-10 11:16:55  willuhn
 * @C Fallback auf normalen TAN-Dialog, wenn der Flicker-Code nicht lesbar ist
 *
 * Revision 1.2  2011-05-09 17:24:46  willuhn
 * @N ChipTAN-Dialog jetzt in echt
 *
 * Revision 1.1  2010-12-08 12:34:57  willuhn
 * @C Flicker-Code in Dialog verschoben
 *
 **********************************************************************/