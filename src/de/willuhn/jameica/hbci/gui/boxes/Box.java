/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/boxes/Attic/Box.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/11/09 01:13:53 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import de.willuhn.jameica.gui.Part;

/**
 * Eine Box ist eine GUI-Komponente, die auf der Welcome-Page von
 * Hibiscus angezeigt und vom User frei angeordnet werden koennen.
 * Implementierende Klassen muessen einen parameterlosen Konstruktor
 * mit dem Modifier <b>public</b> besitzen, damit sie vom Classloader
 * zur Laufzeit geladen werden koennen.
 */
public interface Box extends Part, Comparable
{
  /**
   * Liefert den Namen der Box.
   * @return Name der Box.
   */
  public String getName();
  
  /**
   * Prueft, ob die Box angezeigt werden soll.
   * @return true, wenn sie angezeigt werden soll.
   */
  public boolean isEnabled();
  
  /**
   * Aktiviert/Deaktiviert die Box.
   * @param enabled
   */
  public void setEnabled(boolean enabled);
  
  /**
   * Liefert den Default-Wert fuer die Aktivierung der Box.
   * @return Default-Wert.
   */
  public boolean getDefaultEnabled();
  
  /**
   * Liefert die Position, an der die Box angezeigt werden soll.
   * @return die Position.
   */
  public int getIndex();
  
  /**
   * Liefert die Positon der "Werkseinstellungen".
   * @return Default-Index.
   */
  public int getDefaultIndex();
  
  /**
   * Verschiebt die Box um eine Position nach oben.
   */
  public void up();
  
  /**
   * Verschiebt die Box um eine Position nach unten.
   */
  public void down();
}


/*********************************************************************
 * $Log: Box.java,v $
 * Revision 1.1  2005/11/09 01:13:53  willuhn
 * @N chipcard modul fuer AMD64 vergessen
 * @N Startseite jetzt frei konfigurierbar
 *
 **********************************************************************/