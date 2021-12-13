/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerNew;
import de.willuhn.jameica.hbci.gui.parts.EmpfaengerList;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Box zur Anzeige der Adressen.
 */
public class Adressbuch extends AbstractBox implements Box
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  @Override
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Adressbuch");
  }

  @Override
  public int getDefaultIndex()
  {
    return 3;
  }

  @Override
  public boolean getDefaultEnabled()
  {
    return false;
  }

  @Override
  public int getHeight()
  {
    return 200;
  }

  @Override
  public void paint(Composite parent) throws RemoteException
  {
    EmpfaengerList l = new EmpfaengerList(new EmpfaengerNew());
    l.setSummary(false);
    l.paint(parent);
  }

  @Override
  public boolean isActive()
  {
    return super.isActive() && !Settings.isFirstStart();
  }
}


/*********************************************************************
 * $Log: Adressbuch.java,v $
 * Revision 1.8  2010/08/12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 * Revision 1.7  2008/01/04 16:39:31  willuhn
 * @N Weitere Hoehen-Angaben von Komponenten
 *
 * Revision 1.6  2007/12/18 17:10:22  willuhn
 * @N Neues ExpandPart
 * @N Boxen auf der Startseite koennen jetzt zusammengeklappt werden
 *
 * Revision 1.5  2006/06/29 23:10:33  willuhn
 * @R Box-System aus Hibiscus in Jameica-Source verschoben
 * @C keine eigene Startseite mehr, jetzt alles ueber Jameica-Boxsystem geregelt
 *
 * Revision 1.4  2006/03/27 21:34:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2006/03/20 00:35:53  willuhn
 * @N new box "Konten-�bersicht"
 *
 * Revision 1.2  2005/11/09 01:14:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/11/09 01:13:53  willuhn
 * @N chipcard modul fuer AMD64 vergessen
 * @N Startseite jetzt frei konfigurierbar
 *
 **********************************************************************/