/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/SammelLastschriftControl.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/02/28 16:28:24 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;

/**
 * Implementierung des Controllers fuer den Dialog "Liste der Sammellastschriften".
 * @author willuhn
 */
public class SammelLastschriftControl extends AbstractControl
{

  private SammelLastschrift lastschrift = null;
  private TablePart list = null;

  /**
   * ct.
   * @param view
   */
  public SammelLastschriftControl(AbstractView view)
  {
    super(view);
  }

  /**
   * Liefert die aktuelle Sammel-Lastschrift oder erstellt eine neue.
   * @return Sammel-Lastschrift.
   * @throws RemoteException
   */
  public SammelLastschrift getLastschrift() throws RemoteException
  {
    if (lastschrift != null)
      return lastschrift;

    if (getCurrentObject() != null)
    {
      lastschrift = (SammelLastschrift) getCurrentObject();
      return lastschrift;
    }

    lastschrift = (SammelLastschrift) Settings.getDBService().createObject(SammelLastschrift.class,null);
    return lastschrift;
  }

  /**
   * Liefert eine Tabelle mit den existierenden Sammellastschriften.
   * @return Liste der Sammellastschriften.
   * @throws RemoteException
   */
  public TablePart getListe() throws RemoteException
  {
    if (list != null)
      return list;

    return list;    
  }

}

/*****************************************************************************
 * $Log: SammelLastschriftControl.java,v $
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
*****************************************************************************/