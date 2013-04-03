/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.synchronize.hbci;

import java.util.Arrays;
import java.util.List;

import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;


/**
 * Abstrakte Basis-Klasse unserer Job-Provider.
 */
public abstract class AbstractHBCISynchronizeJobProvider implements HBCISynchronizeJobProvider
{
  /**
   * Liefert eine Liste der Konten, fuer die die Synchronisierung ausgefuehrt
   * werden.
   * Die Funktion macht nichts anderes, als:
   *  - alle zur Synchronisierung aktiven zurueckzuliefern, wenn k=null ist
   *  - eine Liste mit nur dem angegebenen Konto zurueckzuliefern, wenn k!=null ist.
   * @param k das Konto.
   * @return die Liste der Konten.
   */
  protected List<Konto> getKonten(Konto k)
  {
    if (k == null)
      return SynchronizeOptions.getSynchronizeKonten();
    
    return Arrays.asList(k);
  }
}


