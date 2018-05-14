/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.chart.LineChart;
import de.willuhn.jameica.hbci.gui.chart.LineChartData;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.UmsatzTreeNode;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Zeigt die Umsaetze von Kategorien im zeitlichen Verlauf.
 */
public class UmsatzTypVerlauf implements Part
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private List data       = null;
  private Date start      = null;
  private Date stop       = null;
  private LineChart chart = null;
  
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
      this.start = DateUtil.startOfDay(cal.getTime());
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

    int count = 0;
      
    for (int i=0;i<this.data.size();++i)
    {
      UmsatzTreeNode group = (UmsatzTreeNode) this.data.get(i); 
      ChartDataUmsatz cd = new ChartDataUmsatz(group);
      if (cd.hasData)
      {
        this.chart.addData(cd);
        count = cd.entries.size();
      }
    }
    
    if (count <= 1)
      this.chart.setTitle(i18n.tr("Bitte wählen Sie einen größeren Zeitraum (mindestens zwei Monate)"));
    else
      this.chart.setTitle(i18n.tr("Umsätze der Kategorien im Verlauf (gruppiert nach Monat)"));
    
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
      this.chart.setStacked(false); // TODO Stacked Graph für "Umsätze nach Kategorieren" BUGZILLA 749
      this.chart.setTitle(i18n.tr("Umsätze der Kategorien im Verlauf (gruppiert nach Monat)"));
      for (int i=0;i<this.data.size();++i)
      {
        UmsatzTreeNode group = (UmsatzTreeNode) this.data.get(i);
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
    private UmsatzTreeNode group = null;
    private List<Entry> entries  = new ArrayList<Entry>();
    private boolean hasData      = false;
    
    private List<Umsatz> getRecursiveUmsaetze(UmsatzTreeNode group) {
      List<Umsatz> result = new ArrayList<Umsatz>();
      result.addAll(group.getUmsaetze());
      for (UmsatzTreeNode unterkategorie: group.getSubGroups()) {
        result.addAll(getRecursiveUmsaetze(unterkategorie));
      }
      return result;
    }
    
    /**
     * Erzeugt eine Liste mit den aggregierten Daten für eine Linie des Charts 
     * @param group
     * @throws RemoteException
     */
    private ChartDataUmsatz(UmsatzTreeNode group) throws RemoteException
    {
      HashMap<Date,Double> verteilung = new HashMap<Date, Double>();
      Calendar calendar = Calendar.getInstance();
      this.group = group;
      this.entries.clear();

      for (Umsatz umsatz: getRecursiveUmsaetze(group)) {
        if (umsatz.getDatum() == null) {
          Logger.warn("no date found for umsatz, skipping record");
          continue;
        }
        this.hasData = true;
        calendar.setTime(DateUtil.startOfDay(umsatz.getDatum()));
        calendar.set(Calendar.DAY_OF_MONTH,1);
        double aggMonatsWert = verteilung.containsKey(calendar.getTime())?verteilung.get(calendar.getTime()):0;
        verteilung.put(calendar.getTime(), umsatz.getBetrag() + aggMonatsWert);
      }

      calendar.setTime(DateUtil.startOfDay(start));
      calendar.set(Calendar.DAY_OF_MONTH, 1);
      Date monat = calendar.getTime();

      // BUGZILLA 1604 - wir wollen im End-Datum kein oberes Jahr fest vorgeben.
      // Daher beenden wir hier die Iteration stattdessen nach maximal 1200 Monaten.
      // Das sind 100 Jahre.
      int limit = 0;
      while(monat.before(stop) && limit++ < 1200) {
        Entry aktuellerMonatswert = new Entry();
        aktuellerMonatswert.monat = monat;
        if (verteilung.containsKey(monat)) {
          aktuellerMonatswert.betrag = verteilung.get(monat);
        }
        entries.add(aktuellerMonatswert);

        calendar.setTime(monat);
        calendar.add(Calendar.MONTH, 1);
        monat = calendar.getTime();
      }
    }

    /**
     * @see de.willuhn.jameica.hbci.gui.chart.ChartData#getData()
     */
    public List getData() throws RemoteException
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
      UmsatzTyp ut = this.group.getUmsatzTyp();
      
      if (ut == null)
        return null; // "nicht zugeordnet"

      if (!ut.isCustomColor())
        return null; // keine benutzerdefinierte Farbe angegeben
      
      return ut.getColor();
    }
    
    /**
     * @see de.willuhn.jameica.hbci.gui.chart.LineChartData#isFilled()
     */
    public boolean isFilled() throws RemoteException
    {
      return true;
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
