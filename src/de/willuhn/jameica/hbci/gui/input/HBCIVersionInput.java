/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/HBCIVersionInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/02/08 18:34:41 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Combo-Box, die die verfuegbaren HBCI-Versionen enthaelt.
 * @author willuhn
 */
public class HBCIVersionInput extends SelectInput implements Input
{

  private static HBCIVersionObject[] VERSIONS = new HBCIVersionObject[6];
  private static HBCIVersionObject DEFAULT    = null;
  
  private static I18N i18n;
  static
  {
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    DEFAULT = new HBCIVersionObject(null,i18n.tr("Version aus Sicherheitsmedium lesen"));

    VERSIONS[0] = DEFAULT;
    VERSIONS[1] = new HBCIVersionObject("201","HBCI 2.01");
    VERSIONS[2] = new HBCIVersionObject("210","HBCI 2.1");
    VERSIONS[3] = new HBCIVersionObject("220","HBCI 2.2");
    VERSIONS[4] = new HBCIVersionObject("plus","HBCI+");
    VERSIONS[5] = new HBCIVersionObject("300","FinTS 3.0");
  }

  
  /**
   * ct.
   * @param preselected ID der vorzuselektierenden Version. 
   */
  public HBCIVersionInput(String preselected) throws RemoteException
  {
    super(PseudoIterator.fromArray(VERSIONS),findByID(preselected));
  }

  /**
   * Findet das HBCIversion-Hilfsobjekt basierend auf der ID.
   * @param id ID.
   * @return Hilfs-Objekt.
   */
  private static GenericObject findByID(String id)
  {
    if (id == null)
      return DEFAULT;
    for (int i=0;i<VERSIONS.length;++i)
    {
      if (id.equals(VERSIONS[i].id))
        return VERSIONS[i];
    }
    return DEFAULT;
  }

  /**
   * Hilfs-Objekt.
   * @author willuhn
   */
  private static class HBCIVersionObject implements GenericObject
  {

    private String id   = null;
    private String name = null;

    /**
     * ct.
     * @param id ID der HBCI-Version.
     * @param name Name der HBCI-Version.
     */
    private HBCIVersionObject(String id, String name)
    {
      this.id   = id;
      this.name = name;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      return name;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[] {"name"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return id;
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
      if (arg0 == null)
        return false;

      boolean nullOwn   = getID() == null;
      boolean nullOther = arg0.getID() == null;

      // Die ID kann <null> sein.
      if (nullOwn && nullOther)  return true;
      if (nullOwn && !nullOther) return false;

      return this.getID().equals(arg0.getID());
    }
  }

  /**
   * Liefert die ID der ausgwaehlten HBCI-Version als <code>java.lang.String</code>.
   * Moegliche Rueckgabe-Werte:<br>
   * <ul>
   *   <li><code>null</code> (Version wird aus Sicherheitsmedium gelesen)</li>
   *   <li>201</li>
   *   <li>210</li>
   *   <li>220</li>
   *   <li>plus</li>
   *   <li>300</li>
   * </ul>
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    try
    {
      HBCIVersionObject o = (HBCIVersionObject) super.getValue();
      return o.getID();
    }
    catch (Exception e)
    {
      return null;
    }
  }
}

/*****************************************************************************
 * $Log: HBCIVersionInput.java,v $
 * Revision 1.1  2005/02/08 18:34:41  willuhn
 * *** empty log message ***
 *
*****************************************************************************/