/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/UmsatzTypVerlauf.java,v $
 * $Revision: 1.11 $
 * $Date: 2012/04/05 21:44:18 $
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

    for (int i=0;i<this.data.size();++i)
    {
      UmsatzTreeNode group = (UmsatzTreeNode) this.data.get(i); 
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
      this.chart.setStacked(false); // TODO Stacked Graph für "Umsätze nach Kategorieren" BUGZILLA 749
      this.chart.setTitle(i18n.tr("Umsätze der Kategorien im zeitlichen Verlauf"));
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

      while(monat.before(stop)) {
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


/*********************************************************************
 * $Log: UmsatzTypVerlauf.java,v $
 * Revision 1.11  2012/04/05 21:44:18  willuhn
 * @B BUGZILLA 1219
 *
 * Revision 1.10  2011/05/10 11:52:32  willuhn
 * @R Marker geaendert
 *
 * Revision 1.9  2011-01-20 17:13:21  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 *
 * Revision 1.8  2010-11-24 16:27:17  willuhn
 * @R Eclipse BIRT komplett rausgeworden. Diese unsaegliche Monster ;)
 * @N Stattdessen verwenden wir jetzt SWTChart (http://www.swtchart.org). Das ist statt den 6MB von BIRT sagenhafte 250k gross
 *
 * Revision 1.7  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 * Revision 1.6  2010/03/22 10:00:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2010/03/05 15:24:53  willuhn
 * @N BUGZILLA 686
 *
 * Revision 1.4  2009/08/27 13:37:28  willuhn
 * @N Der grafische Saldo-Verlauf zeigt nun zusaetzlich  eine Trendkurve an
 *
 * Revision 1.3  2009/05/08 13:58:30  willuhn
 * @N Icons in allen Menus und auf allen Buttons
 * @N Fuer Umsatz-Kategorien koennen nun benutzerdefinierte Farben vergeben werden
 *
 * Revision 1.2  2008/02/26 01:12:30  willuhn
 * @R nicht mehr benoetigte Funktion entfernt
 *
 * Revision 1.1  2008/02/26 01:01:16  willuhn
 * @N Update auf Birt 2 (bessere Zeichen-Qualitaet, u.a. durch Anti-Aliasing)
 * @N Neuer Chart "Umsatz-Kategorien im Verlauf"
 * @N Charts erst beim ersten Paint-Event zeichnen. Dadurch laesst sich z.Bsp. die Konto-View schneller oeffnen, da der Saldo-Verlauf nicht berechnet werden muss
 *
 **********************************************************************/