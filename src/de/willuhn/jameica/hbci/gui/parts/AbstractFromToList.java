/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/AbstractFromToList.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/04/24 17:15:51 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Abstrakte Tabelle mit Filter "von" und "bis".
 */
public abstract class AbstractFromToList extends TablePart implements Part
{

  protected I18N i18n   = null;
  
  private Input from    = null;
  private Input to      = null;
  
  private Date prevFrom = null;
  private Date prevTo   = null;

  private de.willuhn.jameica.system.Settings mySettings = null;

  /**
   * ct.
   * @param action
   */
  public AbstractFromToList(Action action)
  {
    super(action);
    
    this.i18n       = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.mySettings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();

    this.setRememberOrder(true);
    this.setRememberColWidths(true);
    this.setSummary(true);
  }

  /**
   * Ueberschrieben, um einen DisposeListener an das Composite zu haengen.
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    Listener l = new ChangedListener();

    LabelGroup group = new LabelGroup(parent,i18n.tr("Anzeige einschränken"));

    // Als End-Datum nehmen wir keines.
    // Es sei denn, es ist ein aktuelles gespeichert
    String sTo = mySettings.getString("filter.to",null);
    if (sTo != null && sTo.length() > 0)
    {
      try
      {
        prevTo = HBCI.DATEFORMAT.parse(sTo);
      }
      catch (Exception e)
      {
        Logger.error("unable to parse " + sTo,e);
      }
    }

    // Als Startdatum nehmen wir den ersten des aktuellen Monats
    // Es sei denn, es ist eines gespeichert
    String sFrom = mySettings.getString("filter.from",null);
    if (sFrom != null && sFrom.length() > 0)
    {
      try
      {
        prevFrom = HBCI.DATEFORMAT.parse(sFrom);
      }
      catch (Exception e)
      {
        Logger.error("unable to parse " + sFrom,e);
      }
    }
    if (prevFrom == null)
    {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.DAY_OF_MONTH,1);
      prevFrom = HBCIProperties.startOfDay(cal.getTime());
    }
    
    from = new DateInput(prevFrom, HBCI.DATEFORMAT);
    from.addListener(l);
    to = new DateInput(prevTo, HBCI.DATEFORMAT);
    to.addListener(l);
    
    group.addLabelPair(i18n.tr("Anzeige von"),from);
    group.addLabelPair(i18n.tr("Anzeige bis"),to);
   
    // Erstbefuellung
    GenericIterator items = getList(prevFrom,prevTo);
    if (items != null)
    {
      items.begin();
      while (items.hasNext())
        addItem(items.next());
    }

    super.paint(parent);
  }
  
  /**
   * Liefert die Liste der fuer diesen Zeitraum geltenden Daten.
   * @param from Start-Datum. Kann null sein.
   * @param to End-Datum. Kann null sein.
   * @return Liste der Daten dieses Zeitraumes.
   * @throws RemoteException
   */
  protected abstract GenericIterator getList(Date from, Date to) throws RemoteException;
  
  /**
   * Wird ausgeloest, wenn das Datum geaendert wird.
   */
  private class ChangedListener implements Listener
  {

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      try
      {
        Date dfrom = (Date) from.getValue();
        Date dto   = (Date) to.getValue();
        
        if (dfrom != null && dto != null && dfrom.after(dto))
        {
          GUI.getView().setErrorText(i18n.tr("End-Datum muss sich nach dem Start-Datum befinden"));
          return;
        }
        
        GUI.getView().setErrorText("");
        
        // Mal schauen, ob sich ueberhaupt was geaendert hat
        if (compare(dfrom,prevFrom) && compare(dto,prevTo))
          return; // Nichts geaendert

        // erstmal alles entfernen.
        removeAll();

        // Liste neu laden
        GenericIterator items = getList(dfrom,dto);
        if (items == null)
          return;
        
        items.begin();
        while (items.hasNext())
          addItem(items.next());
        
        // Sortierung wiederherstellen
        sort();
        
        prevFrom = dfrom;
        prevTo   = dto;

        // Speichern der Werte aus den beiden Eingabe-Feldern.
        // Das From-Datum speichern wir immer
        try
        {
          mySettings.setAttribute("filter.from",dfrom == null ? (String)null : HBCI.DATEFORMAT.format(dfrom));
          
          // Das End-Datum speichern wir nur, wenn es nicht das aktuelle Datum ist
          if (dto != null && !HBCIProperties.startOfDay(new Date()).equals(dto))
            mySettings.setAttribute("transferlist.filter.to",HBCI.DATEFORMAT.format(dto));
          else
            mySettings.setAttribute("filter.to",(String)null);
        }
        catch (Exception ex)
        {
          Logger.error("unable to save dates",ex);
        }

      }
      catch (RemoteException re)
      {
        Logger.error("unable to apply filter",re);
      }
    }
  }
  
  /**
   * Vergleich zwei Datums-Angaben Nullpointer-sicher.
   * @param d1 Datum 1.
   * @param d2 Datum 2.
   * @return true, wenn beide identisch oder null sind.
   */
  private static boolean compare(Date d1, Date d2)
  {
    if (d1 == d2)
      return true;
    
    if (d1 == null || d2 == null)
      return false;
    return d1.equals(d2);
  }
}


/**********************************************************************
 * $Log: AbstractFromToList.java,v $
 * Revision 1.2  2007/04/24 17:15:51  willuhn
 * @B Vergessen, "size" hochzuzaehlen, wenn Objekte vor paint() hinzugefuegt werden
 *
 * Revision 1.1  2007/04/24 16:55:00  willuhn
 * @N Aktualisierte Daten nur bei geaendertem Datum laden
 *
 **********************************************************************/