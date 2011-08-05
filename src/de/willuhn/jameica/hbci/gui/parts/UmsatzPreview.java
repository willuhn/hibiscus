/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/Attic/UmsatzPreview.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/08/05 12:10:16 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl;
import de.willuhn.jameica.hbci.rmi.Umsatz;

/**
 * Komponente, die eine Vorschau auf einen Umsatz als Snapin anzeigt.
 */
public class UmsatzPreview implements Part
{
  private Umsatz umsatz = null;
  
  /**
   * Speichert den aktuellen Umsatz.
   * @param umsatz der aktuelle Umsatz.
   */
  public void setUmsatz(Umsatz umsatz)
  {
    this.umsatz = umsatz;
  }
  

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    if (this.umsatz == null)
      return;
    
    // TODO: An der Umsatz-Preview mal noch weiterarbeiten
    MyUmsatzDetailControl control = new MyUmsatzDetailControl();
    control.getZweck().paint(parent);
  }
  
  /**
   * Abgeleitet, damit wir den Umsatz selbst setzen koennen.
   */
  private class MyUmsatzDetailControl extends UmsatzDetailControl
  {
    /**
     * ct.
     */
    public MyUmsatzDetailControl()
    {
      super(null);
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#getUmsatz()
     */
    public Umsatz getUmsatz()
    {
      return umsatz;
    }
  }
}



/**********************************************************************
 * $Log: UmsatzPreview.java,v $
 * Revision 1.2  2011/08/05 12:10:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2011-08-05 11:21:58  willuhn
 * @N Erster Code fuer eine Umsatz-Preview
 * @C Compiler-Warnings
 * @N DateFromInput/DateToInput - damit sind die Felder fuer den Zeitraum jetzt ueberall einheitlich
 *
 **********************************************************************/