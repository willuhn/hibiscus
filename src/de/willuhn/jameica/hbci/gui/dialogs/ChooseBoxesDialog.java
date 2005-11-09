/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/ChooseBoxesDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/11/09 01:13:53 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Vector;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Welcome;
import de.willuhn.jameica.hbci.gui.boxes.Box;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ClassFinder;
import de.willuhn.util.I18N;

/**
 * Dialog zum Konfigurieren der Boxen.
 */
public class ChooseBoxesDialog extends AbstractDialog
{
  private I18N i18n = null;
  private TablePart table = null;

  /**
   * @param position
   */
  public ChooseBoxesDialog(int position)
  {
    super(position);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setTitle(i18n.tr("Auswahl der anzuzeigenden Elemente"));
    setSize(350,300);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    ClassFinder finder = Application.getClassLoader().getClassFinder();
    Class[] boxes = finder.findImplementors(Box.class);
    Vector v1 = new Vector();
    for (int i=0;i<boxes.length;++i)
    {
      v1.add(boxes[i].newInstance());
    }
    Collections.sort(v1);
    Vector v = new Vector();
    for (int i=0;i<v1.size();++i)
    {
      v.add(new BoxObject((Box)v1.get(i)));
    }
    
    GenericIterator iterator = PseudoIterator.fromArray((BoxObject[]) v.toArray(new BoxObject[v.size()]));
    table = new TablePart(iterator,null);
    table.addColumn(i18n.tr("Bezeichnung"),"name");
    table.addColumn(i18n.tr("Status"),"active", new Formatter() {
      public String format(Object o)
      {
        if (o == null || !(o instanceof Boolean))
          return null;
        return ((Boolean) o).booleanValue() ? i18n.tr("Aktiv") : "-"; 
      }
    });
    table.setMulti(false);
    table.setSummary(false);
    table.setFormatter(new TableFormatter() {
      public void format(TableItem item)
      {
        if (item == null || item.getData() == null)
          return;
        BoxObject o = (BoxObject) item.getData();
        if (o.box.isEnabled())
          item.setForeground(Color.SUCCESS.getSWTColor());
      }
    });
    
    ContextMenu menu = new ContextMenu();
    menu.addItem(new MyMenuItem(true));
    menu.addItem(new MyMenuItem(false));
    table.setContextMenu(menu);

    table.paint(parent);

    ButtonArea buttons = new ButtonArea(parent,3);
    buttons.addButton(i18n.tr("Nach oben"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        BoxObject o = (BoxObject) table.getSelection();
        if (o == null)
          return;
        o.box.up();
        table.removeItem(o);
        try
        {
          table.addItem(o,o.box.getIndex());
          table.select(o);
        }
        catch (Exception e)
        {
          Logger.error("Fehler beim Verschieben des Elementes",e);
        }
      }
    });
    buttons.addButton(i18n.tr("Nach unten"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        BoxObject o = (BoxObject) table.getSelection();
        if (o == null)
          return;
        o.box.down();
        table.removeItem(o);
        try
        {
          table.addItem(o,o.box.getIndex());
          table.select(o);
        }
        catch (Exception e)
        {
          Logger.error("Fehler beim Verschieben des Elementes",e);
        }
      }
    });
    buttons.addButton(i18n.tr("Übernehmen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
        new Welcome().handleAction(context);
      }
    },null,true);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

  /**
   * Hilfs-Objekt, um Boxen zu GenericObjects zu machen.
   */
  private class BoxObject implements GenericObject
  {
    private Box box = null;
    
    /**
     * ct.
     * @param box
     */
    private BoxObject(Box box)
    {
      this.box = box;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      if ("active".equals(arg0))
        return Boolean.valueOf(box.isEnabled());
      return box.getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"name","active"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return box.getClass().getName();
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
      if (arg0 == null || !(arg0 instanceof BoxObject))
        return false;
      BoxObject other = (BoxObject) arg0;
      return this.getID().equals(other.getID());
    }
    
  }
  
  /**
   * Hilsklasse.
   */
  private class MyMenuItem extends CheckedContextMenuItem
  {
    private boolean state = false;
    
    /**
     * ct.
     * @param state
     */
    private MyMenuItem(boolean state)
    {
      super(state ? i18n.tr("Aktivieren") : i18n.tr("Deaktivieren"), new MyAction(state));
      this.state = state;
    }
    
    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o == null || !(o instanceof BoxObject))
        return false;
      BoxObject bo = (BoxObject) o;
      return state ^ bo.box.isEnabled();
    }
  }
  
  /**
   * Hilfsklasse.
   */
  private class MyAction implements Action
  {
    private boolean state = false;
    
    /**
     * @param state
     */
    private MyAction(boolean state)
    {
      this.state = state;
    }
    
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
      if (context == null || !(context instanceof BoxObject))
        return;
      BoxObject o = (BoxObject) context;
      o.box.setEnabled(state);
      
      // Element entfernen und wieder hinzufuegen, damit die Ansicht aktualisiert wird
      table.removeItem(o);
      try
      {
        table.addItem(o,o.box.getIndex());
        table.select(o);
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim Sortieren des Elementes",e);
      }
    }
    
  }
}


/*********************************************************************
 * $Log: ChooseBoxesDialog.java,v $
 * Revision 1.1  2005/11/09 01:13:53  willuhn
 * @N chipcard modul fuer AMD64 vergessen
 * @N Startseite jetzt frei konfigurierbar
 *
 **********************************************************************/