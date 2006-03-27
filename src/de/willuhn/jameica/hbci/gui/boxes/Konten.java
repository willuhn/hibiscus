/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/Konten.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/03/27 21:34:16 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.parts.KontoList;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Box zur Anzeige der Konten.
 */
public class Konten extends AbstractBox implements Box
{

  private I18N i18n = null;
  
  /**
   * ct.
   */
  public Konten()
  {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return i18n.tr("Konten-Übersicht");
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 1;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    new Headline(parent,getName());
    KontoList l = new KontoList(new KontoNew());
    l.setSummary(false);
    l.paint(parent);
  }

}


/*********************************************************************
 * $Log: Konten.java,v $
 * Revision 1.2  2006/03/27 21:34:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2006/03/20 00:35:54  willuhn
 * @N new box "Konten-Übersicht"
 *
 **********************************************************************/