/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/listener/Attic/UeberweisungCreate.java,v $
 * $Revision: 1.2 $
 * $Date: 2004/07/20 21:48:00 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.listener;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.views.UeberweisungNeu;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;

/**
 * Listener, der eine neue Ueberweisung erzeugt und im Detail-Dialog anzeigt.
 */
public class UeberweisungCreate implements Listener
{

  /**
   * ct.
   */
  public UeberweisungCreate()
  {
    super();
  }

  /**
   * Im <code>data</code>-Member des Events kann sich ein Konto oder ein Empfaenger
   * befinden. Abhaengig davon wird das eine oder andere Feld in der neuen Ueberweisung
   * vorausgefuellt.
   * Wenn nichts angegeben ist, wird eine leere Ueberweisung erstellt und angezeigt.
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  public void handleEvent(Event event)
  {
  	Ueberweisung u = null;

  	if (event != null && event.data != null)
  	{
			try {
				Konto k = (Konto) event.data;
				u = (Ueberweisung) Settings.getDatabase().createObject(Ueberweisung.class,null);
				u.setKonto(k);
			}
			catch (Exception e)
			{
				// Mal mit 'nem Empfaenger versuchen
				try {
					Empfaenger empf = (Empfaenger) event.data;
					u = (Ueberweisung) Settings.getDatabase().createObject(Ueberweisung.class,null);
					u.setEmpfaenger(empf);
				}
				catch (Exception e2)
				{
					// ignore
				}
			}
  	}
		GUI.startView(UeberweisungNeu.class.getName(),u);
  }

}


/**********************************************************************
 * $Log: UeberweisungCreate.java,v $
 * Revision 1.2  2004/07/20 21:48:00  willuhn
 * @N ContextMenus
 *
 * Revision 1.1  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 **********************************************************************/