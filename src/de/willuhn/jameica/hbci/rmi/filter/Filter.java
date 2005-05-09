/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/filter/Attic/Filter.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/09 23:47:24 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi.filter;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface fuer eine Gruppe von Filter-Kriterien.
 * Diese werden in der Filter-Engine mittels UND verknuepft.
 * Stimmen alle Kriterien ueberein, gilt dies als Treffer.
 */
public interface Filter extends Remote
{
  /**
   * Liste der Filter-Kriterien, die erfuellt sein muessen.
   * Dies kann bei Bedarf auch nur ein einzelner FilterPattern sein.
   * @return Liste der Filter-Kriterien.
   * @throws RemoteException
   */
  public Pattern[] getPattern() throws RemoteException;
}


/**********************************************************************
 * $Log: Filter.java,v $
 * Revision 1.1  2005/05/09 23:47:24  web0
 * @N added first code for the filter framework
 *
 **********************************************************************/