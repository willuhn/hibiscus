/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/PrintServiceImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/02/03 18:57:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.HashMap;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;

import de.willuhn.datasource.Service;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * @author willuhn
 */
public class PrintServiceImpl implements Service
{

  private PrinterData printer;
  private int xDpi;
  private int yDpi;
  private GC gc;
  private HashMap colors;
  private int xoff;
  private int yoff;

  private boolean started = false;
  private boolean startable = true;

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public void start() throws RemoteException
  {
    if (!startable)
      throw new RemoteException("Service currently not startable");

    if (started)
    {
      Logger.warn("skipping start request, allready started");
      return;
    }

    Logger.info("checking for default printer");
    this.printer = Printer.getDefaultPrinterData();
    if (this.printer != null)
    {
      Logger.info("found " + this.printer.name);
    }
    else
    {
      Logger.info("no default printer found. looking for installed printers");
      PrinterData[] avail = Printer.getPrinterList();
      for (int i=0;i<avail.length;++i)
      {
        Logger.info("using " + avail[i].name);
        this.printer = avail[i];
        break;
      }
    }
    started = true;
  }

  /**
   * @see de.willuhn.datasource.Service#isStarted()
   */
  public boolean isStarted() throws RemoteException
  {
    return started;
  }

  /**
   * @see de.willuhn.datasource.Service#isStartable()
   */
  public boolean isStartable() throws RemoteException
  {
    return startable;
  }

  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  public void stop(boolean arg0) throws RemoteException
  {
    if (!started)
    {
      Logger.warn("Service allready shut down");
      return;
    }
    started = false;
    startable = arg0;
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "Print Service for Hibiscus";
  }


  public PrintJob createJob() throws RemoteException
  {
    if (!isStarted())
      throw new RemoteException("Print Service not started");
    return new PrintJobImpl(this.printer);
  }
  /**
   * @see de.bbvag.dhl.easylog.hubs.Service#open()
   */
  public void open() throws RemoteException
  {
    checkPrinter();

    this.printer = new Printer(new PrinterData("winspool",this.printerDriver));

    this.initialized = true;

    try {
      Application.getLog().info("starting print job to printer " + printerDriver + ", client: " + getClientHost());
    }
    catch (ServerNotActiveException sne) {}

    colors = new HashMap();

    Point pt = printer.getDPI();
    xDpi = pt.x;
    yDpi = pt.y;

    printer.startJob("de.bbvag.prt1");
    printer.startPage();
    gc = new GC(printer);
    gc.setLineWidth(1);
    setColor(0, 0, 0);
  }

  /**
   * @see de.bbvag.dhl.easylog.hubs.Service#close()
   */
  public void close() throws RemoteException
  {
    // not needed.
  }

  /**
   * @see de.bbvag.prt1.PrintDC#end()
   */
  public void end() throws RemoteException
  {
    if (!initialized)
      throw new RemoteException("printer not initialized. please call init() first.");

    printer.endPage();
    printer.endJob();
    while (colors.size() > 0)
    {
      Object k = colors.keySet().iterator().next();
      Color c = (Color) colors.remove(k);
      c.dispose();
    }
  }

  /**
   * @see de.bbvag.prt1.PrintDC#nextPage()
   */
  public void nextPage() throws RemoteException
  {
    if (!initialized)
      throw new RemoteException("printer not initialized. please call init() first.");

    printer.endPage();
    printer.startPage();
  }

  /**
   * @see de.bbvag.prt1.PrintDC#setFont(java.lang.String, int, int)
   */
  public void setFont(String fontName, int style, int size) throws RemoteException
  {
    if (!initialized)
      throw new RemoteException("printer not initialized. please call init() first.");

    // SWT needs font size in pixels at 72 DPI
    size = (size * 144 + 1) / 508;
    Font font = new Font(printer, fontName, size, style);
    gc.setFont(font);
  }

  /**
   * @see de.bbvag.prt1.PrintDC#drawString(java.lang.String, int, int)
   */
  public void drawString(String text, int x, int y) throws RemoteException
  {
    if (!initialized)
      throw new RemoteException("printer not initialized. please call init() first.");

    gc.drawString(text, x2Device(x) + xoff, y2Device(y) + yoff, true);
  }

  /**
   * @see de.bbvag.prt1.PrintDC#drawLine(int, int, int, int)
   */
  public void drawLine(int x0, int y0, int x1, int y1) throws RemoteException
  {
    if (!initialized)
      throw new RemoteException("printer not initialized. please call init() first.");

    x0 = x2Device(x0) + xoff;
    y0 = y2Device(y0) + yoff;
    x1 = x2Device(x1) + xoff;
    y1 = y2Device(y1) + yoff;
    gc.drawLine(x0, y0, x1, y1);
  }

  /**
   * @see de.bbvag.prt1.PrintDC#fillRect(int, int, int, int)
   */
  public void fillRect(int x, int y, int cx, int cy) throws RemoteException
  {
    if (!initialized)
      throw new RemoteException("printer not initialized. please call init() first.");

    x = x2Device(x) + xoff;
    y = y2Device(y) + yoff;
    cx = x2Device(cx);
    cy = y2Device(cy);
    gc.fillRectangle(x, y, cx, cy);
  }

  /**
   * @see de.bbvag.prt1.PrintDC#drawImage(java.lang.String, int, int, int, int)
   */
  public void drawImage(String fileName, int x, int y, int cx, int cy) throws RemoteException
  {
    if (!initialized)
      throw new RemoteException("printer not initialized. please call init() first.");

    x = x2Device(x);
    y = y2Device(y);
    cx = x2Device(cx);
    cy = y2Device(cy);

    Image img = new Image(printer, fileName);
    ImageData id = img.getImageData();
    gc.drawImage(img, 0, 0, id.width, id.height, x + xoff, y + yoff, cx, cy);
  }

  /**
   * @see de.bbvag.prt1.PrintDC#drawBarcode(int, int, int, int, int)
   */
  public void drawBarcode(
    int x0,
    int y0,
    int height,
    int unitWidth,
    int[] data) throws RemoteException
  {
    if (!initialized)
      throw new RemoteException("printer not initialized. please call init() first.");

    x0 = x2Device(x0) + xoff;
    y0 = y2Device(y0) + yoff;
    height = y2Device(height);

    int mw = x2Device(unitWidth) / 10;

    int rw1 = mw*2540 / xDpi;
    if (rw1 < unitWidth)
    {
      int rw2 = (mw + 1)*2540 / xDpi;
      if (rw2 - unitWidth < unitWidth - rw1)
        ++mw;
    }

    if (mw == 0)
      mw = 1;

    for (int i = 0; i < data.length; ++i)
    {
      int cx = mw * data[i];
      if (i % 2 == 0)
      {
        gc.fillRectangle(x0, y0, cx, height);
      }
      x0 += cx;
    }

  }

  /**
   * Map the x mm to the device pixel offset.
   * X DPI depend on printer.
   * @param x a x position in 1/10mm
   * @return the corresponding value in pixels
   * @author bebbo
   */
  private int x2Device(int x)
  {
    return (x * xDpi * 2 + 1)/ 508;
  }
  /**
   * Map the y mm to the device pixel offset.
   * Y DPI depend on printer.
   * @param y a y position in 1/10mm
   * @return the corresponding value in pixels
   * @author bebbo
   */
  private int y2Device(int y)
  {
    return (y * yDpi * 2 + 1)/ 508;
  }

  /**
   * @see de.bbvag.prt1.PrintDC#setColor(int, int, int)
   */
  public void setColor(int r, int g, int b) throws RemoteException
  {
    if (!initialized)
      throw new RemoteException("printer not initialized. please call init() first.");

    String key = r + ":" + g + ":" + b;
    Color color = (Color)colors.get(key);
    if (color == null)
    {
      color = new Color(printer, r, g, b);
      colors.put(key, color);
    }
    gc.setForeground(color);
    gc.setBackground(color);
  }

  /**
   * Specify a page offset.
   * @param xoff offset from left in 1/10mm
   * @param yoff offset from top in 1/10mm
   */
  public void setOffset(int xoff, int yoff) throws RemoteException
  {
    if (!initialized)
      throw new RemoteException("printer not initialized. please call init() first.");

    this.xoff = x2Device(xoff);
    this.yoff = y2Device(yoff);
  }


}

/*****************************************************************************
 * $Log: PrintServiceImpl.java,v $
 * Revision 1.1  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
*****************************************************************************/