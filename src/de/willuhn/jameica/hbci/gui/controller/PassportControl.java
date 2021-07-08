/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.hbci.gui.parts.PassportTree;

/**
 * Controller fuer die View der Bank-Zugaenge.
 */
public class PassportControl extends AbstractControl
{
  private PassportTree passports = null;

  /**
   * ct.
   * @param view
   */
  public PassportControl(AbstractView view)
  {
    super(view);
  }

  /**
   * Liefert einen Tree mit den existierenden Passports.
   * @return Tabelle mit den Passports.
   * @throws RemoteException
   */
  public PassportTree getPassports() throws RemoteException
  {
    if (this.passports == null)
      this.passports = new PassportTree();
    return this.passports;
  }
}

/**********************************************************************
 * $Log: PassportControl.java,v $
 * Revision 1.3  2011/04/29 11:38:57  willuhn
 * @N Konfiguration der HBCI-Medien ueberarbeitet. Es gibt nun direkt in der Navi einen Punkt "Bank-Zugaenge", in der alle Medien angezeigt werden.
 *
 **********************************************************************/