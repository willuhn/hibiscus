/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/HBCIProgressDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/06/21 20:11:10 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ProgressBar;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Dialog, der waehrend der Ausfuehrung der HBCI-Geschaeftsvorfaelle Infos und
 * Fortschrittsinformationen angezeigt.
 */
public class HBCIProgressDialog extends AbstractDialog implements ProgressMonitor
{

  private ProgressBar progress = null;
  private I18N i18n;
  private Runnable runnable = null;

  /**
   * @param r
   */
  public HBCIProgressDialog(Runnable r)
  {
    super(AbstractDialog.POSITION_CENTER);
    this.progress = new ProgressBar();
    this.runnable = r;
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setTitle(i18n.tr("HBCI-Status"));
    setSideImage(SWTUtil.getImage("hbci.gif"));
    setSize(450,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setPercentComplete(int)
   */
  public void setPercentComplete(int arg0)
  {
    if (arg0 >= 100)
    {
      close();
      return;
    }
    this.progress.setPercentComplete(arg0);
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#addPercentComplete(int)
   */
  public void addPercentComplete(int arg0)
  {
    this.progress.addPercentComplete(arg0);
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#getPercentComplete()
   */
  public int getPercentComplete()
  {
    return this.progress.getPercentComplete();
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatus(int)
   */
  public void setStatus(int arg0)
  {
    this.progress.setStatus(arg0);
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setStatusText(java.lang.String)
   */
  public void setStatusText(final String arg0)
  {
    GUI.getDisplay().asyncExec(new Runnable() {
      public void run()
      {
        progress.setStatusText(arg0);
      }
    });
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#log(java.lang.String)
   */
  public void log(String arg0)
  {
    this.progress.log(arg0);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    this.progress.paint(parent);
    HBCIFactory.getInstance().setProgressMonitor(this);
    ButtonArea buttons = new ButtonArea(parent,1);
    buttons.addButton(i18n.tr("Abbrechen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        HBCIFactory.getInstance().markCancelled();
      }
    });
    GUI.startAsync(this.runnable);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

}


/**********************************************************************
 * $Log: HBCIProgressDialog.java,v $
 * Revision 1.3  2005/06/21 20:11:10  web0
 * @C cvs merge
 *
 * Revision 1.2  2005/06/15 16:10:48  web0
 * @B javadoc fixes
 *
 * Revision 1.1  2005/05/06 16:53:07  web0
 * *** empty log message ***
 *
 **********************************************************************/