/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/UmsatzDaysInput.java,v $
 * $Revision: 1.7 $
 * $Date: 2011/05/04 12:04:40 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.ScaleInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Auswahlfeld fuer die Anzahl der anzuzeigenden Tage.
 */
public class UmsatzDaysInput extends ScaleInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static Settings settings = new Settings(UmsatzDaysInput.class);
  
  private Listener listener = new RangeListener();

  /**
   * ct.
   * @throws RemoteException
   */
  public UmsatzDaysInput() throws RemoteException
  {
    // BUGZILLA 258
    super(getDefaultDays() == -1 ? 1000 : getDefaultDays()); // wir muessen das "-1" wieder zurueck auf 1000 mappen
    this.setName(i18n.tr("Zeitraum"));
    this.setComment(""); // Damit wir das Datum noch hinzufuegen koennen
    this.setScaling(1,1000,1,10);
    this.addListener(this.listener);
    this.listener.handleEvent(null); // einmal initial ausloesen
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.ScaleInput#getControl()
   */
  public Control getControl()
  {
    Control c = super.getControl();
    c.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        settings.setAttribute("days",(Integer) getValue());
      }
    });
    return c;
  }

  /**
   * Ueberschrieben, damit wir "-1" fuer "Alle Umsaetze" liefern koennen.
   * @see de.willuhn.jameica.gui.input.ScaleInput#getValue()
   */
  public Object getValue()
  {
    int i = (Integer) super.getValue();
    if (i > 999)
      return -1;
    return i;
  }

  /**
   * Liefert die Anzahl der standardmaessig anzuzeigenden Tage.
   * @return Standardmaessig anzuzeigende Tage.
   */
  public final static int getDefaultDays()
  {
    return settings.getInt("days",HBCIProperties.UMSATZ_DEFAULT_DAYS);
  }

  /**
   * Hilfsklasse zum Aktualisieren des Kommentars hinter dem Zeitraum.
   */
  private class RangeListener implements Listener
  {
    public void handleEvent(Event event)
    {
      try
      {
        int start = ((Integer)getValue()).intValue();
        if (start == 1)
        {
          setComment(i18n.tr("seit gestern"));
        }
        else if (start == -1)
        {
          setComment(i18n.tr("Alle Umsätze"));
        }
        else if (start > 0)
        {
          long d = start * 24l * 60l * 60l * 1000l;
          Date date = DateUtil.startOfDay(new Date(System.currentTimeMillis() - d));
          setComment(i18n.tr("ab {0} ({1} Tage)",HBCI.DATEFORMAT.format(date),Integer.toString(start)));
        }
        else
        {
          setComment("");
        }
      }
      catch (Exception e)
      {
        Logger.error("unable to update comment",e);
      }
    }
  }
}


/*********************************************************************
 * $Log: UmsatzDaysInput.java,v $
 * Revision 1.7  2011/05/04 12:04:40  willuhn
 * @N Zeitraum in Umsatzliste und Saldo-Chart kann jetzt freier und bequemer ueber einen Schieberegler eingestellt werden
 * @B Dispose-Checks in Umsatzliste
 *
 * Revision 1.6  2010-08-11 16:06:04  willuhn
 * @N BUGZILLA 783 - Saldo-Chart ueber alle Konten
 *********************************************************************/