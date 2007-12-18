/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/Konten.java,v $
 * $Revision: 1.5 $
 * $Date: 2007/12/18 17:10:22 $
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

import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
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
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Konten-Übersicht");
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 1;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
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
    KontoList l = new KontoList(new KontoNew());
    l.addColumn(i18n.tr("Saldo aktualisiert am"),"saldo_datum", new DateFormatter(HBCI.LONGDATEFORMAT));
    l.setSummary(false);
    l.paint(parent);
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
 * $Log: Konten.java,v $
 * Revision 1.5  2007/12/18 17:10:22  willuhn
 * @N Neues ExpandPart
 * @N Boxen auf der Startseite koennen jetzt zusammengeklappt werden
 *
 * Revision 1.4  2007/08/29 10:04:42  willuhn
 * @N Bug 476
 *
 * Revision 1.3  2006/06/29 23:10:33  willuhn
 * @R Box-System aus Hibiscus in Jameica-Source verschoben
 * @C keine eigene Startseite mehr, jetzt alles ueber Jameica-Boxsystem geregelt
 *
 * Revision 1.2  2006/03/27 21:34:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2006/03/20 00:35:54  willuhn
 * @N new box "Konten-Übersicht"
 *
 **********************************************************************/