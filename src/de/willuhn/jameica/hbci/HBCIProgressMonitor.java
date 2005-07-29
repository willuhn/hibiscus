/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/Attic/HBCIProgressMonitor.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/07/29 15:10:32 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.View;
import de.willuhn.jameica.gui.parts.Panel;
import de.willuhn.jameica.gui.parts.ProgressBar;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Progress-Monitors fuer die HBCI-Kommunikation.
 */
public class HBCIProgressMonitor extends ProgressBar
{

  private boolean started = false;
  
  private void check()
  {
    if (started)
      return;

    Logger.info("creating progress monitor for GUI");
    GUI.getDisplay().syncExec(new Runnable() {
      public void run()
      {
        final View view = GUI.getView();
        if (view.snappedIn())
          view.snapOut();
        
        try
        {
          I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
          Panel panel = new Panel(i18n.tr("Status der HBCI-Übertragung"), HBCIProgressMonitor.this, false);
          panel.addMinimizeListener(new Listener()
          {
            public void handleEvent(Event event)
            {
              view.snapOut();
            }
          });
          panel.paint(view.getSnapin());
          Logger.info("activating progress monitor");
          view.snapIn();
          started = true;
        }
        catch (RemoteException e)
        {
          Logger.error("unable to snapin progress monitor",e);
        }
      }
    });
  }
  
  /**
   * @see de.willuhn.util.ProgressMonitor#setPercentComplete(int)
   */
  public void setPercentComplete(int arg0)
  {
    check();
    super.setPercentComplete(arg0);
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#addPercentComplete(int)
   */
  public void addPercentComplete(int arg0)
  {
    check();
    super.addPercentComplete(arg0);
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#getPercentComplete()
   */
  public int getPercentComplete()
  {
    check();
    return super.getPercentComplete();
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatus(int)
   */
  public void setStatus(int arg0)
  {
    check();
    super.setStatus(arg0);
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatusText(java.lang.String)
   */
  public void setStatusText(String arg0)
  {
    check();
    super.setStatusText(arg0);
    log(arg0);
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#log(java.lang.String)
   */
  public void log(String arg0)
  {
    check();
    super.log("[" + HBCI.EXTRALONGDATEFORMAT.format(new Date()) + "] " + arg0);
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.ProgressBar#clearLog()
   */
  public void clearLog()
  {
    check();
    super.clearLog();
  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
  }
}


/*********************************************************************
 * $Log: HBCIProgressMonitor.java,v $
 * Revision 1.2  2005/07/29 15:10:32  web0
 * @N minimize hbci progress dialog
 *
 * Revision 1.1  2005/07/26 23:00:03  web0
 * @N Multithreading-Support fuer HBCI-Jobs
 *
 **********************************************************************/