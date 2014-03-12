/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Generische Aktion zum Oeffnen von Datensaetzen.
 * Die Klasse versucht selbst herauszufinden, womit sie es zu tun hat und
 * die passende Aktion auszufuehren.
 */
public class Open implements Action
{
  private final static Map<Class,Class<? extends Action>> actionMap = new HashMap<Class,Class<? extends Action>>();
  
  static
  {
    actionMap.put(Ueberweisung.class,           UeberweisungNew.class);
    actionMap.put(Lastschrift.class,            LastschriftNew.class);
    actionMap.put(Dauerauftrag.class,           DauerauftragNew.class);
    actionMap.put(AuslandsUeberweisung.class,   AuslandsUeberweisungNew.class);
    actionMap.put(SepaLastschrift.class,        SepaLastschriftNew.class);
    actionMap.put(SepaSammelLastschrift.class,  SepaSammelLastschriftNew.class);
    actionMap.put(SepaSammelUeberweisung.class, SepaSammelUeberweisungNew.class);
    actionMap.put(SammelUeberweisung.class,     SammelUeberweisungNew.class);
    actionMap.put(SammelLastschrift.class,      SammelLastschriftNew.class);
    actionMap.put(Konto.class,                  KontoNew.class);
    actionMap.put(Umsatz.class,                 UmsatzDetail.class);
    actionMap.put(UmsatzTyp.class,              UmsatzTypNew.class);
  }
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null)
      return;
    
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    Class type          = context.getClass();
    
    Iterator<Class> keys = actionMap.keySet().iterator();
    while (keys.hasNext())
    {
      Class key = keys.next();
      if (key.isAssignableFrom(type))
      {
        Action a = service.get(actionMap.get(key));
        a.handleAction(context);
        return;
      }
    }
    
    Logger.warn("dont know, how to handle " + context.getClass());
  }

}
