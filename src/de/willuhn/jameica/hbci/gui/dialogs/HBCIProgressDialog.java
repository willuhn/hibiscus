/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/HBCIProgressDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/06 16:53:07 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.parts.ProgressBar;
import de.willuhn.util.ProgressMonitor;

/**
 * Dialog, der waehrend der Ausfuehrung der HBCI-Geschaeftsvorfaelle Infos und
 * Fortschrittsinformationen angezeigt.
 */
public class HBCIProgressDialog implements ProgressMonitor
{

  private ProgressBar progress = null;

  /**
   * ct.
   */
  public HBCIProgressDialog()
  {
    this.progress = new ProgressBar();
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    this.progress.paint(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#setPercentComplete(int)
   */
  public void setPercentComplete(int arg0)
  {
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
  public void setStatusText(String arg0)
  {
    this.progress.setStatusText(arg0);
  }

  /**
   * @see de.willuhn.util.ProgressMonitor#log(java.lang.String)
   */
  public void log(String arg0)
  {
    this.progress.log(arg0);
  }

}


/**********************************************************************
 * $Log: HBCIProgressDialog.java,v $
 * Revision 1.1  2005/05/06 16:53:07  web0
 * *** empty log message ***
 *
 **********************************************************************/