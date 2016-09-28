package de.willuhn.jameica.hbci.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

/**
 * Util-Klasse fuer Event-Behandlung
 * */
public class EventUtil
{

  /**
   * @param event Event, das geprüft werden soll
   * @return ist Event vom Typ Fokus bekommen/verloren
   */
  public static boolean isFocusEvent(Event event){
    return event!=null && (event.type==SWT.FocusIn || event.type==SWT.FocusOut);
  }
}
