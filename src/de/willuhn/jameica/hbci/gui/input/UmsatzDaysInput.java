/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/UmsatzDaysInput.java,v $
 * $Revision: 1.4 $
 * $Date: 2006/08/08 21:18:21 $
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
import java.util.ArrayList;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.I18N;

/**
 * Combo-Box, fuer die Auswahl der anzuzeigenden Umsatz-Tage.
 * @author willuhn
 */
public class UmsatzDaysInput extends SelectInput
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private static Settings settings = new Settings(UmsatzDaysInput.class);

  /**
   * ct.
   * @throws RemoteException
   */
  public UmsatzDaysInput() throws RemoteException
  {
    // BUGZILLA 258
    super(init(),new DayObject(settings.getInt("days",HBCIProperties.UMSATZ_DEFAULT_DAYS)));
  }

  /**
   * @return initialisiert die Liste der Optionen.
   * @throws RemoteException
   */
  private static GenericIterator init() throws RemoteException
  {

    ArrayList l = new ArrayList();
    l.add(new DayObject(30));
    l.add(new DayObject(60));
    l.add(new DayObject(120));
    l.add(new DayObject(365));
    l.add(new DayObject(-1));
    
    return PseudoIterator.fromArray((DayObject[])l.toArray(new DayObject[l.size()]));
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    DayObject o = (DayObject) super.getValue();
    if (o == null)
      return new Integer(-1);
    settings.setAttribute("days",o.days);
    return new Integer(o.days);
  }
  
  /**
   * Hilfs-Objekt zur Anzeige der Labels.
   * @author willuhn
   */
  private static class DayObject implements GenericObject
  {
    private int days = -1;
    private String label = null;

    /**
     * ct.
     * @param days Anzahl der Tage.
     * @param label Label.
     */
    private DayObject(int days)
    {
      this.days = days;
      
      switch (days)
      {
        case 1:
          this.label = i18n.tr("1 Tag");
          break;
        case -1:
          this.label = i18n.tr("alle Umsätze");
          break;
        default:
          this.label = i18n.tr("{0} Tage",""+days);
      }
    }
    
    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      return label;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"name"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return "" + days;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject arg0) throws RemoteException
    {
      if (arg0 == null || !(arg0 instanceof DayObject))
        return false;
      
      return this.getID().equals(arg0.getID());
    }
    
  }
}


/*********************************************************************
 * $Log: UmsatzDaysInput.java,v $
 * Revision 1.4  2006/08/08 21:18:21  willuhn
 * @B Bug 258
 *
 * Revision 1.3  2006/03/30 22:22:32  willuhn
 * @B bug 217
 *
 * Revision 1.2  2006/03/15 18:01:30  willuhn
 * @N AbstractHBCIJob#getName
 *
 * Revision 1.1  2006/03/09 18:24:05  willuhn
 * @N Auswahl der Tage in Umsatz-Chart
 *
 *********************************************************************/