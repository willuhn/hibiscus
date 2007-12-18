/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/NeueUmsaetze.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/12/18 17:10:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.parts.UmsatzList;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * zeigt eine Liste mit neu hinzugekommenen Umsaetzen an.
 */
public class NeueUmsaetze extends AbstractBox
{

  private I18N i18n = null;
  
  /**
   * ct.
   */
  public NeueUmsaetze()
  {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 6;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Neue Umsätze");
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    GenericIterator list = de.willuhn.jameica.hbci.messaging.NeueUmsaetze.getNeueUmsaetze();
    UmsatzList umsaetze = new UmsatzList(list,new UmsatzDetail());
    umsaetze.setFilterVisible(false);
    umsaetze.paint(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#isActive()
   */
  public boolean isActive()
  {
    return super.isActive() && !Settings.isFirstStart();
  }
}


/*********************************************************************
 * $Log: NeueUmsaetze.java,v $
 * Revision 1.3  2007/12/18 17:10:22  willuhn
 * @N Neues ExpandPart
 * @N Boxen auf der Startseite koennen jetzt zusammengeklappt werden
 *
 * Revision 1.2  2007/03/02 14:49:14  willuhn
 * @R removed old firststart view
 * @C do not show boxes on first start
 *
 * Revision 1.1  2007/01/30 18:25:33  willuhn
 * @N Bug 302
 *
 **********************************************************************/