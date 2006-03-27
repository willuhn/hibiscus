/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/Adressbuch.java,v $
 * $Revision: 1.4 $
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
import de.willuhn.jameica.hbci.gui.action.EmpfaengerNew;
import de.willuhn.jameica.hbci.gui.parts.EmpfaengerList;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Box zur Anzeige der Adressen.
 */
public class Adressbuch extends AbstractBox implements Box
{

  private I18N i18n = null;
  
  /**
   * ct.
   */
  public Adressbuch()
  {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return i18n.tr("Adressbuch");
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 3;
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
    EmpfaengerList l = new EmpfaengerList(new EmpfaengerNew());
    l.setSummary(false);
    l.paint(parent);
  }

}


/*********************************************************************
 * $Log: Adressbuch.java,v $
 * Revision 1.4  2006/03/27 21:34:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2006/03/20 00:35:53  willuhn
 * @N new box "Konten-Übersicht"
 *
 * Revision 1.2  2005/11/09 01:14:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/11/09 01:13:53  willuhn
 * @N chipcard modul fuer AMD64 vergessen
 * @N Startseite jetzt frei konfigurierbar
 *
 **********************************************************************/