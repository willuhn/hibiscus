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

/**
 * Gemeinsames Interface fuer Features, die zu einem Chart nachgeruestet werden koennen.
 */
public interface ChartFeature
{
  /**
   * Liste der Events, auf die ein Feature reagieren kann.
   */
  public enum Event
  {
    /**
     * Wird ausgeloest, wenn das Chart gezeichnet wird.
     */
    PAINT,
  }

  /**
   * Hilfsklasse mit Meta-Informationen fuer das Feature.
   */
  public class Context
  {
    /**
     * Das Chart selbst.
     */
    public Chart chart;

    /**
     * Das ausgeloeste Event.
     */
    public Event event;
  }

  /**
   * Liefert true, wenn das Feature auf das angegebene Event reagieren soll.
   * @param e das Event.
   * @return true, wenn es auf das angegebene Event reagieren soll.
   */
  public boolean onEvent(Event e);

  /**
   * Wird aufgerufen, wenn das angegebene Event ausgeloest wurde.
   * @param e das Event.
   * @param ctx Context-Infos.
   */
  public void handleEvent(Event e, Context ctx);
}
