/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.hbci.messaging.NeueUmsaetze;
import de.willuhn.jameica.hbci.rmi.Flaggable;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * Action, um einen Umsatz als geprueft zu markieren.
 */
public class UmsatzMarkChecked extends FlaggableChange
{
  private Boolean assign = null;

  /**
   * ct.
   */
  public UmsatzMarkChecked()
  {
    super(Umsatz.FLAG_CHECKED,true);
  }
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    this.assign = null;
    super.handleAction(context);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.action.FlaggableChange#postProcess(de.willuhn.jameica.hbci.rmi.Flaggable)
   */
  @Override
  protected void postProcess(Flaggable o) throws Exception
  {
    if (!(o instanceof Umsatz))
      return;
    
    // Wir senden die Aenderung noch per Messaging, damit SynTAX das Geprueft-Flag bei Bedarf
    // synchronisieren kann
    Application.getMessagingFactory().getMessagingQueue("hibiscus.umsatz.markchecked").sendMessage(new QueryMessage(Boolean.TRUE.toString(),o));
    
    Umsatz u = (Umsatz) o;
    NeueUmsaetze.setRead(u);
    UmsatzTyp ut = u.getUmsatzTyp();
    
    // Wir haben gar keine Kategorie - dann eruebrigt sich die Frage.
    if (ut == null)
      return;

    // Wir checken das nur einmal pro Aufruf
    if (this.assign == null)
    {
      // Ermitteln, ob der User gefragt werden soll, ob er die Kategorien dabei fest zuordnen will
      String s = i18n.tr("Sollen Umsatz-Kategorien, die dynamisch per Suchbegriff zugeordnet wurden,\n" +
                         "hierbei fest mit den Umsätzen verbunden werden? Das ermöglicht eine spätere\n" +
                         "Änderung des Suchbegriffes in der Kategorie, ohne dass hierbei die Zuordnung\n" +
                         "ggf. wieder verloren geht.\n\n" +
                         "Kategorien hierbei fest zuordnen?");
      this.assign = Boolean.valueOf(Application.getCallback().askUser(s));
    }
    
    if (this.assign != null && this.assign.booleanValue())
      u.setUmsatzTyp(ut);
  }

}


