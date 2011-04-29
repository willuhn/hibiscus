/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/PassportTree.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/04/29 11:38:58 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.passport.Configuration;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Fertig konfigurierter Tree, der die HBCI-Sicherheitsmedien anzeigt.
 */
public class PassportTree extends TreePart
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * ct.
   * @throws RemoteException
   */
  public PassportTree() throws RemoteException
  {
    super(init(),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || (context instanceof Object[]))
          return;
        Action a = new PassportDetail();

        if (context instanceof PassportObject)
          a.handleAction(((PassportObject) context).passport);
        else if (context instanceof ConfigObject)
          a.handleAction(((ConfigObject) context).config);
      }
    });
    this.addColumn(i18n.tr("Bezeichnung"),"name");
    
    this.setFormatter(new TreeFormatter() {
      public void format(TreeItem item)
      {
        Object data = item.getData();
        if (data instanceof ConfigObject)
          item.setImage(SWTUtil.getImage("seahorse-preferences.png"));
      }
    });
    this.setMulti(false);
  }

  /**
   * Liefert den selektierten Passport.
   * @return der selektierte Passport oder NULL.
   */
  public Passport getPassport()
  {
    Object o = this.getSelection();

    if (o instanceof PassportObject)
      return ((PassportObject) o).passport;
    
    return null;
  }
  
  /**
   * Initialisiert die Daten des Tree.
   * @return der Tree.
   * @throws RemoteException
   */
  private static GenericIterator init() throws RemoteException
  {
    try
    {
      Passport[] passports = PassportRegistry.getPassports();
      List<PassportObject> list = new ArrayList<PassportObject>();
      for (Passport p:passports)
      {
        list.add(new PassportObject(p));
      }
      return PseudoIterator.fromArray(list.toArray(new PassportObject[list.size()]));
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to create passport tree",e);
    }
  }
  
  /**
   * Hilfsklasse, um die Passports als Baum anzuzeigen.
   */
  private static class PassportObject implements GenericObjectNode
  {
    private Passport passport        = null;
    private GenericIterator children = null;
    
    /**
     * ct.
     * @param passport der Passport.
     */
    private PassportObject(Passport passport)
    {
      this.passport = passport;
    }
    
    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject o) throws RemoteException
    {
      if (o == null || !(o instanceof PassportObject))
        return false;
      PassportObject other = (PassportObject) o;
      return this.getID().equals(other.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) throws RemoteException
    {
      return BeanUtil.get(passport,name);
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return this.passport.getClass().getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "description";
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getChildren()
     */
    public GenericIterator getChildren() throws RemoteException
    {
      if (this.children != null)
        return this.children;
      
      List<? extends Configuration> configs = this.passport.getConfigurations();
      List<ConfigObject> list = new ArrayList<ConfigObject>();
      for (Configuration c:configs)
      {
        list.add(new ConfigObject(c));
      }
      this.children = PseudoIterator.fromArray(list.toArray(new ConfigObject[list.size()]));
      return this.children;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getParent()
     */
    public GenericObjectNode getParent() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getPath()
     */
    public GenericIterator getPath() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#getPossibleParents()
     */
    public GenericIterator getPossibleParents() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObjectNode#hasChild(de.willuhn.datasource.GenericObjectNode)
     */
    public boolean hasChild(GenericObjectNode children) throws RemoteException
    {
      return this.getChildren().contains(children) != null;
    }
  }
  
  /**
   * Hilfsklasse, um die Passport-Konfigurationen im Baum anzuzeigen.
   */
  private static class ConfigObject implements GenericObject
  {
    private Configuration config = null;
    
    /**
     * ct.
     * @param config
     */
    private ConfigObject(Configuration config)
    {
      this.config = config;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject o) throws RemoteException
    {
      if (o == null || !(o instanceof PassportObject))
        return false;
      ConfigObject other = (ConfigObject) o;
      return this.getID().equals(other.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) throws RemoteException
    {
      if ("name".equals(name))
        return this.config.getDescription();
      
      return BeanUtil.get(this.config,name);
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      if (this.config instanceof GenericObject)
        return ((GenericObject) this.config).getID();
      
      return this.config.getClass().getName() + ":" + this.config.getDescription();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "description";
    }
    
  }

}



/**********************************************************************
 * $Log: PassportTree.java,v $
 * Revision 1.1  2011/04/29 11:38:58  willuhn
 * @N Konfiguration der HBCI-Medien ueberarbeitet. Es gibt nun direkt in der Navi einen Punkt "Bank-Zugaenge", in der alle Medien angezeigt werden.
 *
 **********************************************************************/