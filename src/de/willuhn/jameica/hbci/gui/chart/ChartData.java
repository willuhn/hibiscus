/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.chart;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Basis-Interface, welches die zu zeichnenden Datenreihen enthaelt.
 */
public interface ChartData
{
  /**
   * Liefert die zu zeichnende Datenreihe.
   * @return Datenreihe.
   * @throws RemoteException
   */
  public List getData() throws RemoteException;

  /**
   * Liefert das Label der Datenreihe.
   * @return Label der Datenreihe.
   * @throws RemoteException
   */
  public String getLabel() throws RemoteException;

  /**
   * Liefert den Namen des Attributs, welches fuer die Werte
   * verwendet werden soll. Der Wert des Attributes muss vom Typ java.lang.Number sein.
   * @return Name des Werte-Attributs.
   * @throws RemoteException
   */
  public String getDataAttribute() throws RemoteException;

  /**
   * Liefert den Namen des Attributs fuer die Beschriftung.
   * @return Name des Attributs fuer die Beschriftung.
   * Der Wert des Attributes muss vom Typ java.lang.Date sein.
   * @throws RemoteException
   */
  public String getLabelAttribute() throws RemoteException;
}

/*********************************************************************
 * $Log: ChartData.java,v $
 * Revision 1.3  2010/11/24 16:27:17  willuhn
 * @R Eclipse BIRT komplett rausgeworden. Diese unsaegliche Monster ;)
 * @N Stattdessen verwenden wir jetzt SWTChart (http://www.swtchart.org). Das ist statt den 6MB von BIRT sagenhafte 250k gross
 *
 * Revision 1.2  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 **********************************************************************/