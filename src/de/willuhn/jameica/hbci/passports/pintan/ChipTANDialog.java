/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/ChipTANDialog.java,v $
 * $Revision: 1.8 $
 * $Date: 2011/05/27 10:51:02 $
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
import org.kapott.hbci.manager.FlickerRenderer;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
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
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent);
    container.addHeadline(i18n.tr("Flicker-Grafik"));
    container.addText(i18n.tr("Ändern Sie die Größe des Fensters so, dass die Flicker-Grafik von Ihrem TAN-Generator gelesen werden kann."),true);
    container.addSeparator();
    
    FlickerPart flicker = new FlickerPart(this.code);
    flicker.paint(parent);
    
    // Hier stehen dann noch die Anweisungen von der Bank drin
    super.paint(parent);
    
    // Muessen wir ueberschreiben, damit der User den Flicker-Balken passend ziehen kann.
    getShell().setMinimumSize(100,100);
    
    // Geht leider nur mit dem Listener. Alles andere wird ignoriert
    addShellListener(new ShellAdapter() {
      public void shellActivated(ShellEvent e)
      {
        // Muesste ungefaehr die passende Groesse fuer den TAN-Generator von Reiner SCT sein.
        final int x = settings.getInt("size.x",264);
        final int y = settings.getInt("size.y",368);
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
  private class FlickerPart extends FlickerRenderer implements Part
  {
    private Canvas canvas = null;
    private boolean[] bits = new boolean[5];
    
    /**
     * ct.
     * @param code der anzuzeigende Flicker-Code.
     * @throws ApplicationException
     */
    private FlickerPart(String code) throws ApplicationException
    {
      super(code);
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
      
      // Redraw ausloesen
      if (canvas != null)
      {
        // Wir sind hier nicht im Event-Dispatcher-Thread von SWT. Daher uebergeben wir das an diesen
        canvas.getDisplay().syncExec(new Runnable() {
          public void run()
          {
            // Wir sind fertig.
            if (canvas.isDisposed())
              return;
            
            // Neu zeichnen
            canvas.redraw();
          }
        });
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

      // Bei jedem Paint-Event aktualisieren wir die Balken
      this.canvas.addPaintListener(new PaintListener()
      {
        public void paintControl(PaintEvent e)
        {
          update(e.gc);
        }
      });
      
      // Beim Disposen stoppen wir den Flicker-Thread.
      this.canvas.addDisposeListener(new DisposeListener()
      {
        public void widgetDisposed(DisposeEvent e)
        {
          stop();
        }
      });
      
      // Und los gehts
      this.start();
    }

    /**
     * Aktualisiert die Grafik basierend auf dem aktuellen Code.
     * @ctx der Graphics-Context.
     */
    private void update(GC ctx)
    {
      int margin = 4;
      int barwidth = canvas.getSize().x / 5;
      
      for (int i=0;i<this.bits.length;++i)
      {
        int color = this.bits[i] ? SWT.COLOR_WHITE : SWT.COLOR_BLACK;
        ctx.setBackground(canvas.getDisplay().getSystemColor(color));
        ctx.fillRectangle(i*barwidth+margin,margin,barwidth-2*margin,canvas.getSize().y-2*margin);
      }
    }
  }
}


/**********************************************************************
 * $Log: ChipTANDialog.java,v $
 * Revision 1.8  2011/05/27 10:51:02  willuhn
 * @N Erster Support fuer optisches chipTAN
 *
 * Revision 1.7  2011-05-26 10:13:18  willuhn
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