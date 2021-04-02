/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.UmsatzUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.logging.Logger;

/**
 * Ueber die Klasse koennen die in der aktuellen Session
 * abgerufenen Umsaetze ermittelt werden.
 */
public class NeueUmsaetze implements MessageConsumer
{
  private static String first = null;

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{ImportMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    // Wenn es keine Import-Message ist oder wir schon den ersten Umsatz haben,
    // ignorieren wir die folgenden
    if (first != null || message == null || !(message instanceof ImportMessage))
      return;
    
    GenericObject o = ((ImportMessage)message).getObject();
    
    if (o == null || !(o instanceof Umsatz) || o.getID() == null)
      return; // interessiert uns nicht
    
    
    first = o.getID();
  }
  
  /**
   * Liefert eine Liste mit allen in der aktuellen Sitzung hinzugekommenen Umsaetzen.
   * @return Liste der neuen Umsaetze.
   * @throws RemoteException
   */
  public static GenericIterator getNeueUmsaetze() throws RemoteException
  {
    if (first == null)
      return PseudoIterator.fromArray(new Umsatz[0]);

    DBIterator list = UmsatzUtil.getUmsaetzeBackwards();
    list.addFilter("id >= " + first);
    if (list.size() == 0)
      first = null; // Wenn nichts gefunden wurde, resetten wir uns
    return list;
  }
  
  /**
   * Liefert die ID des ersten in der aktuellen Sitzung eingetroffenen
   * Umsatzes oder <code>null</code>, wenn noch keine neuen Umsaetze hinzugekommen sind.
   * @return die ID des ersten neuen Umsatzes (alle Folge-Umsaetze haben groessere IDs) oder <code>null</code>.
   */
  public static String getID()
  {
    return first;
  }

  /**
   * Liefert true, wenn der Umsatz in der aktuellen Sitzung abgerufen wurde.
   * @param u der zu pruefende Umsatz.
   * @return true, wenn er neu ist.
   */
  public static boolean isNew(Umsatz u)
  {
    if (first == null || u == null)
      return false;

    try
    {
      return (((Integer)u.getAttribute("id-int")).compareTo(new Integer(first)) >= 0);
    }
    catch (Exception e)
    {
      Logger.error("unable to determine new state",e);
    }
    return false;
  }
  
  /**
   * Setzt den Ungelesen-Zaehler der Umsaetze zurueck.
   */
  public static void reset()
  {
    if (first == null)
      return;

    try
    {
      first = null;
      
      // Anzeige aktualisieren
      // Im Prinzip koennten wir fuer jeden Umsatz, der vorher als neu galt, eine ObjectChangedMessage schicken
      // Das funktioniert aber nicht ganz sauber, denn:
      // 1) In der Umsatzliste wird der Fettdruck zwar entfernt. Da sich dabei aber die Objekt-Referenzen aendern
      //    (die Umsaetze wuerden hier ja neu geladen werden), hat das u.U. zu Konsequenz, dass ein Umsatz nicht mehr
      //    verschwindet, wenn man ihn direkt danach loescht. Erst beim Neuladen der View ist er weg.
      // 2) Wenn auf der Startseite die View "Neue Umsätze" aktiv ist, würde dort nur die Fett-Markierung entfernt
      //    werden. Stattdessen müssen die Umsätze dort aber entfernt werden.
      
      // Daher schicken wir keine ObjectChangeMessage sondern laden die aktuelle View neu.
      // GUI.getCurrentView().reload() wird von vielen Views nicht implementiert. Daher starten wir die View neu
      // Achtung: Nicht die Instanz der View wiederverwenden. Bringt keinen Vorteil. Verlangt aber, dass die View
      // alle Resourcen sauber disposed und neu erstellt. KontoNew macht das z.Bsp. nicht, weil es den Controller
      // als Member haelt.
      GUI.startView(GUI.getCurrentView().getClass(),GUI.getCurrentView().getCurrentObject());
    }
    catch (Exception e)
    {
      Logger.error("unable to refresh view",e);
    }
    finally
    {
      GUI.getNavigation().setUnreadCount("hibiscus.navi.umsatz",0);
    }
  }
  
}
