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
import java.util.List;

import org.eclipse.swt.widgets.TreeItem;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.passport.Configuration;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
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
        {
          ConfigObject o = (ConfigObject) data;
          Passport p = o.passport;
          String icon = "system-users.png";
          if (p instanceof de.willuhn.jameica.hbci.passports.pintan.rmi.Passport)
            icon = "hbci-pintan.png";
          else if (p instanceof de.willuhn.jameica.hbci.passports.rdh.rmi.Passport)
            icon = "dialog-password.png";
          else if (p instanceof de.willuhn.jameica.hbci.passports.ddv.rmi.Passport)
            icon = "gcr-smart-card.png";
          
          item.setImage(SWTUtil.getImage(icon));
        }
      }
    });
    this.setMulti(false);
    
    ContextMenu menu = new ContextMenu();
    menu.addItem(new CheckedSingleContextMenuItem(i18n.tr("Öffnen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof ConfigObject))
          return;

        new PassportDetail().handleAction(((ConfigObject) context).config);
      }
    },"document-open.png")
    {
      @Override
      public boolean isEnabledFor(Object o)
      {
        return (o instanceof ConfigObject) && super.isEnabledFor(o);
      }
    });
    menu.addItem(new ContextMenuItem(i18n.tr("Neuer Bank-Zugang..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        Object o = getSelection();
        Passport p = (o instanceof PassportObject) ? ((PassportObject)o).passport : null;
        new PassportDetail().handleAction(p);
      }
    },"list-add.png"));
    
    menu.addItem(ContextMenuItem.SEPARATOR);
    menu.addItem(new CheckedSingleContextMenuItem(i18n.tr("Löschen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof ConfigObject))
          return;
        
        Configuration config = ((ConfigObject) context).config;
        if (config == null)
          return;

        try
        {
          if (!Application.getCallback().askUser(i18n.tr("Wollen Sie diesen Bank-Zugang wirklich löschen?\nDie Konten, Aufträge und Umsätze bleiben erhalten.")))
            return;
        }
        catch (OperationCanceledException oce)
        {
          Logger.info("operation cancelled");
          return;
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (Exception e)
        {
          Logger.error("unable to delete config",e);
          throw new ApplicationException(i18n.tr("Löschen fehlgeschlagen: {0}",e.getMessage()));
        }
        
        config.delete();
        
        // View neu laden
        GUI.startView(GUI.getCurrentView(),null);
      }
    },"user-trash-full.png")
    {
      @Override
      public boolean isEnabledFor(Object o)
      {
        return (o instanceof ConfigObject) && super.isEnabledFor(o);
      }
    });
    
    this.setContextMenu(menu);
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
    if (o instanceof ConfigObject)
      return ((ConfigObject) o).passport;
    
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
    
    @Override
    public boolean equals(GenericObject o) throws RemoteException
    {
      if (o == null || !(o instanceof PassportObject))
        return false;
      PassportObject other = (PassportObject) o;
      return this.getID().equals(other.getID());
    }

    @Override
    public Object getAttribute(String name) throws RemoteException
    {
      return BeanUtil.get(passport,name);
    }

    @Override
    public String[] getAttributeNames() throws RemoteException
    {
      return null;
    }

    @Override
    public String getID() throws RemoteException
    {
      return this.passport.getClass().getName();
    }

    @Override
    public String getPrimaryAttribute() throws RemoteException
    {
      return "description";
    }

    @Override
    public GenericIterator getChildren() throws RemoteException
    {
      if (this.children != null)
        return this.children;
      
      List<? extends Configuration> configs = this.passport.getConfigurations();
      List<ConfigObject> list = new ArrayList<ConfigObject>();
      for (Configuration c:configs)
      {
        list.add(new ConfigObject(this.passport, c));
      }
      this.children = PseudoIterator.fromArray(list.toArray(new ConfigObject[list.size()]));
      return this.children;
    }

    @Override
    public GenericObjectNode getParent() throws RemoteException
    {
      return null;
    }

    @Override
    public GenericIterator getPath() throws RemoteException
    {
      return null;
    }

    @Override
    public GenericIterator getPossibleParents() throws RemoteException
    {
      return null;
    }

    @Override
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
    private Passport passport    = null;
    private Configuration config = null;
    
    /**
     * ct.
     * @param passport
     * @param config
     */
    private ConfigObject(Passport passport, Configuration config)
    {
      this.passport = passport;
      this.config = config;
    }

    @Override
    public boolean equals(GenericObject o) throws RemoteException
    {
      if (o == null || !(o instanceof PassportObject))
        return false;
      ConfigObject other = (ConfigObject) o;
      return this.getID().equals(other.getID());
    }

    @Override
    public Object getAttribute(String name) throws RemoteException
    {
      if ("name".equals(name))
        return this.config.getDescription();
      
      return BeanUtil.get(this.config,name);
    }

    @Override
    public String[] getAttributeNames() throws RemoteException
    {
      return null;
    }

    @Override
    public String getID() throws RemoteException
    {
      if (this.config instanceof GenericObject)
        return ((GenericObject) this.config).getID();
      
      return this.config.getClass().getName() + ":" + this.config.getDescription();
    }

    @Override
    public String getPrimaryAttribute() throws RemoteException
    {
      return "description";
    }
    
  }

}



/**********************************************************************
 * $Log: PassportTree.java,v $
 * Revision 1.2  2011/06/17 08:49:18  willuhn
 * @N Contextmenu im Tree mit den Bank-Zugaengen
 * @N Loeschen von Bank-Zugaengen direkt im Tree
 *
 * Revision 1.1  2011-04-29 11:38:58  willuhn
 * @N Konfiguration der HBCI-Medien ueberarbeitet. Es gibt nun direkt in der Navi einen Punkt "Bank-Zugaenge", in der alle Medien angezeigt werden.
 *
 **********************************************************************/