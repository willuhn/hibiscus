/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/UmsatzDaysInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/03/09 18:24:05 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by  bbv AG
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
import de.willuhn.util.I18N;

/**
 * Combo-Box, fuer die Auswahl der anzuzeigenden Umsatz-Tage.
 * @author willuhn
 */
public class UmsatzDaysInput extends SelectInput
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @throws RemoteException
   */
  public UmsatzDaysInput() throws RemoteException
  {
    super(init(),new DayObject(HBCIProperties.UMSATZ_DEFAULT_DAYS,i18n.tr("{0} Tage",""+HBCIProperties.UMSATZ_DEFAULT_DAYS)));
  }

  /**
   * @return initialisiert die Liste der Optionen.
   * @throws RemoteException
   */
  private static GenericIterator init() throws RemoteException
  {

    ArrayList l = new ArrayList();
    l.add(new DayObject(30,i18n.tr("30 Tage")));
    l.add(new DayObject(60,i18n.tr("60 Tage")));
    l.add(new DayObject(120,i18n.tr("120 Tage")));
    l.add(new DayObject(-1,i18n.tr("alle Umsätze")));
    
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
    private DayObject(int days, String label)
    {
      this.days = days;
      this.label = label;
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
 * Revision 1.1  2006/03/09 18:24:05  willuhn
 * @N Auswahl der Tage in Umsatz-Chart
 *
 *********************************************************************/