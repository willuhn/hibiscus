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

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Markiert das Navigations-Element "Auswertungen-&gt;Umsaetze", wenn neue Umsaetze vorhanden sind.
 */
public class MarkerUmsatzMessageConsumer implements MessageConsumer
{
  private DelayedListener listener = new DelayedListener(1000,new Worker());
  
  @Override
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{ImportMessage.class,ObjectDeletedMessage.class};
  }

  @Override
  public void handleMessage(Message message) throws Exception
  {
    if (Application.inServerMode())
      return;

    GenericObject o = ((ObjectMessage)message).getObject();
    
    if (o == null || !(o instanceof Umsatz))
      return; // interessiert uns nicht

    // wir machen das ganze verzoegert. Im Normalfall kommen ja eine ganze Reine
    // von Umsaetzen in einem Rutsch. Und wir wollen das Update ja nicht unnoetig
    // nach jedem Umsatz ausfuehren, wenn noch 100 weitere kommen.
    listener.handleEvent(null);
  }

  @Override
  public boolean autoRegister()
  {
    return true;
  }
  
  /**
   * Der eigentliche Worker.
   */
  private class Worker implements Listener
  {
    @Override
    public void handleEvent(Event event)
    {
      try
      {
        GenericIterator i = NeueUmsaetze.getNeueUmsaetze();
        final int size = i.size();
        GUI.getNavigation().setUnreadCount("hibiscus.navi.umsatz",size);
      }
      catch (Throwable t) // wir fangen hier alles - fuer den Fall, dass die Jameica-Version noch kein "setUnreadCount" hat
      {
        Logger.write(Level.DEBUG,"unable to update navigation",t);
      }
    }
    
  }
}
