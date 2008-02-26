/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/UmsatzTypVerlauf.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/02/26 01:12:30 $
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.chart.LineChart;
import de.willuhn.jameica.hbci.gui.chart.LineChartData;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.UmsatzGroup;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Zeigt die Umsaetze von Kategorien im zeitlichen Verlauf.
 */
public class UmsatzTypVerlauf implements Part
{
  private static DateFormat DATEFORMAT = new SimpleDateFormat("MM.yyyy");
  
  private List data       = null;
  private Date start      = null;
  private Date stop       = null;
  private LineChart chart = null;
  private I18N i18n       = null;
  
  private Formatter dateFormat = null;
  
  /**
   * ct.
   */
  public UmsatzTypVerlauf()
  {
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    
    this.dateFormat = new Formatter() {

      /**
       * @see de.willuhn.jameica.gui.formatter.Formatter#format(java.lang.Object)
       */
      public String format(Object o)
      {
        if (o == null)
          return "";
        if (!(o instanceof Date))
          return o.toString();
        return DATEFORMAT.format((Date)o);
      }
    };
  }

  /**
   * Speichert die anzuzeigenden Daten.
   * @param data Liste mit Objekten des Typs "UmsatzGroup".
   * @param start Start-Datum.
   * @param stop Stop-Datum.
   */
  public void setData(List data, Date start, Date stop)
  {
    this.data  = data;
    this.start = start;
    this.stop  = stop;
    
    // Wenn das Start-Datum nicht angegeben ist, nehmen wir das
    // aktuelle Jahr
    if (this.start == null)
    {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.MONTH,Calendar.JANUARY);
      cal.set(Calendar.DATE,1);
      this.start = HBCIProperties.startOfDay(cal.getTime());
    }
    if (this.stop == null || !this.stop.after(this.start))
      this.stop = new Date(); // Wenn das Stop-Datum ungueltig ist, machen wir heute draus
  }
  
  /**
   * Aktualisiert das Chart.
   * @throws RemoteException
   */
  public void redraw() throws RemoteException
  {
    if (chart == null)
      return;

    this.chart.removeAllData();

    for (int i=0;i<this.data.size();++i)
    {
      UmsatzGroup group = (UmsatzGroup) this.data.get(i); 
      ChartDataUmsatz cd = new ChartDataUmsatz(group);
      if (cd.hasData)
        this.chart.addData(cd);
    }
    this.chart.redraw();
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    try
    {
      this.chart = new LineChart();
      this.chart.setTitle(i18n.tr("Umsätze der Kategorien im zeitlichen Verlauf"));
      for (int i=0;i<this.data.size();++i)
      {
        UmsatzGroup group = (UmsatzGroup) this.data.get(i);
        ChartDataUmsatz cd = new ChartDataUmsatz(group);
        if (cd.hasData)
          this.chart.addData(cd);
      }
      this.chart.paint(parent);
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      Logger.error("unable to create chart",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Erzeugen des Diagramms"),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Hilfsklasse zum "Homogenisieren" der Betraege in den Umsatzgruppen.
   */
  private class ChartDataUmsatz implements LineChartData
  {
    private UmsatzGroup group       = null;
    private GenericIterator entries = null;
    private boolean hasData         = false;
    
    /**
     * ct
     * @param group
     * @throws RemoteException
     */
    private ChartDataUmsatz(UmsatzGroup group) throws RemoteException
    {
      this.group = group;
      
      GenericIterator umsaetze = this.group.getChildren();
      Calendar cal             = Calendar.getInstance();
      ArrayList list           = new ArrayList();

      // 1. des aktuellen Monats
      Date currentStart = HBCIProperties.startOfDay(start);
      
      // 1. des Folge-Monats
      cal.setTime(start);
      cal.add(Calendar.MONTH,1);
      cal.set(Calendar.DAY_OF_MONTH,1);
      Date currentStop  = HBCIProperties.startOfDay(cal.getTime());

      // Wir iterieren monatsweise ueber das gesamte Zeitfenster
      // und fuellen die Monate mit den Zahlungen auf.
      while (currentStart.before(stop))
      {
        Entry current = new Entry();
        current.monat = currentStart;
        umsaetze.begin();
        while (umsaetze.hasNext())
        {
          Umsatz u = (Umsatz) umsaetze.next();
          Date valuta = u.getValuta();
          if (valuta == null)
          {
            Logger.warn("no valuta found for umsatz, skipping record");
            continue;
          }
          
          // checken, ob sich der Umsatz im aktuellen Monat befindet
          if (valuta.equals(currentStart) || (valuta.after(currentStart) && valuta.before(currentStop)))
          {
            current.betrag += u.getBetrag();
            this.hasData = true;
          }
        }
        list.add(current);

        // einen Monat weiterruecken
        currentStart = currentStop;
        cal.setTime(currentStart);
        cal.add(Calendar.MONTH,1);
        cal.set(Calendar.DAY_OF_MONTH,1);
        currentStop = HBCIProperties.startOfDay(cal.getTime());
      }
      this.entries  = PseudoIterator.fromArray((Entry[])list.toArray(new Entry[list.size()]));
    
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getData()
     */
    public GenericIterator getData() throws RemoteException
    {
      return this.entries;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabel()
     */
    public String getLabel() throws RemoteException
    {
      return (String) this.group.getAttribute("name");
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getDataAttribute()
     */
    public String getDataAttribute() throws RemoteException
    {
      return "betrag";
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabelAttribute()
     */
    public String getLabelAttribute() throws RemoteException
    {
      return "monat";
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getLabelFormatter()
     */
    public Formatter getLabelFormatter() throws RemoteException
    {
      return dateFormat;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getCurve()
     */
    public boolean getCurve()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#getColor()
     */
    public int[] getColor() throws RemoteException
    {
      if (this.group.getUmsatzTyp() == null)
        return null; // "nicht zugeordnet"
      return this.group.getUmsatzTyp().getColor();
    }
  }
  
  /**
   * Hilfsobjekt zum Gruppieren pro Monat.
   */
  private class Entry implements GenericObject
  {
    private double betrag = 0;
    private Date monat;

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
      if (other == null || other.getID() == null)
        return false;
      return other.getID().equals(this.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) throws RemoteException
    {
      if ("betrag".equals(name))
        return new Double(betrag);
      if ("monat".equals(name))
        return monat;
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"betrag","monat"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      if (monat == null)
        return "foo";
      return monat.toString();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "betrag";
    }
    
  }
}


/*********************************************************************
 * $Log: UmsatzTypVerlauf.java,v $
 * Revision 1.2  2008/02/26 01:12:30  willuhn
 * @R nicht mehr benoetigte Funktion entfernt
 *
 * Revision 1.1  2008/02/26 01:01:16  willuhn
 * @N Update auf Birt 2 (bessere Zeichen-Qualitaet, u.a. durch Anti-Aliasing)
 * @N Neuer Chart "Umsatz-Kategorien im Verlauf"
 * @N Charts erst beim ersten Paint-Event zeichnen. Dadurch laesst sich z.Bsp. die Konto-View schneller oeffnen, da der Saldo-Verlauf nicht berechnet werden muss
 *
 **********************************************************************/