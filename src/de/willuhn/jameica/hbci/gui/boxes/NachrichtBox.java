/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/NachrichtBox.java,v $
 * $Revision: 1.8 $
 * $Date: 2011/11/06 12:31:25 $
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

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.NachrichtOpen;
import de.willuhn.jameica.hbci.gui.parts.NachrichtList;
import de.willuhn.jameica.hbci.rmi.Nachricht;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * BUGZILLA 331
 * Zeigt neue System-nachrichten der Bank an.
 */
public class NachrichtBox extends AbstractBox implements Box
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

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
    return 0;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("System-Nachrichten der Bank");
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    DBIterator iterator = Settings.getDBService().createList(Nachricht.class);
    iterator.setOrder("order by datum desc"); // Neueste zuerst
    iterator.addFilter("gelesen is null or gelesen = 0");
    
    NachrichtList list = new NachrichtList(iterator,new NachrichtOpen());
    list.setSummary(false);
    list.paint(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#isActive()
   */
  public boolean isActive()
  {
    return super.isActive() && isEnabled(); // Nicht konfigurierbar
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isEnabled()
   */
  public boolean isEnabled()
  {
    try
    {
      DBIterator iterator = Settings.getDBService().createList(Nachricht.class);
      iterator.addFilter("gelesen is null or gelesen = 0");
      return iterator.hasNext(); // Wenn Nachrichten vorliegen, wird die Box automatisch aktiviert
    }
    catch (Exception e)
    {
      Logger.error("unable to check for new messages",e);
    }
    return super.isEnabled();
  }
  
  
}


/*********************************************************************
 * $Log: NachrichtBox.java,v $
 * Revision 1.8  2011/11/06 12:31:25  willuhn
 * @N BUGZILLA 1142
 *
 * Revision 1.7  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 * Revision 1.6  2010/03/18 11:37:59  willuhn
 * @N Ausfuehrlichere und hilfreichere Fehlermeldung, wenn Hibiscus-Datenbank defekt ist oder nicht geoeffnet werden konnte.
 *
 * Revision 1.5  2008/04/01 09:50:17  willuhn
 * @B Fehlendes XML-Escaping
 *
 * Revision 1.4  2008/04/01 09:46:15  willuhn
 * @R removed debug output
 *
 * Revision 1.3  2007/12/18 17:10:22  willuhn
 * @N Neues ExpandPart
 * @N Boxen auf der Startseite koennen jetzt zusammengeklappt werden
 *
 * Revision 1.2  2007/03/02 14:49:14  willuhn
 * @R removed old firststart view
 * @C do not show boxes on first start
 *
 * Revision 1.1  2006/11/16 22:29:46  willuhn
 * @N Bug 331
 *
 **********************************************************************/