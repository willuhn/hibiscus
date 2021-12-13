/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


// BUGZILLA #80 http://www.willuhn.de/bugzilla/show_bug.cgi?id=80
/**
 * Ein Dialog zur Auswahl des zu verwendenden Schluessels.
 */
public class SelectKeyDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private TablePart table = null;
  private RDHKey selected = null;

  /**
   * @param position
   */
  public SelectKeyDialog(int position)
  {
    super(position);
    this.setTitle(i18n.tr("Schl�sselauswahl"));
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent);
    container.addText(i18n.tr("Bitte w�hlen Sie den zu verwendenden Schl�ssel aus"),true);

    final Button apply = new Button(i18n.tr("�bernehmen"), new Action() {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        new Apply().handleAction(table.getSelection());
      }
    },null,true,"ok.png");
    apply.setEnabled(false); // initial deaktivieren
    
    GenericIterator list = RDHKeyFactory.getKeys();
    List<KeyObject> l = new ArrayList<KeyObject>();
    while (list.hasNext())
    {
      RDHKey key = (RDHKey) list.next();
      l.add(new KeyObject(key));
    }
    
    this.table = new TablePart(l, new Action() {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        new Apply().handleAction(context);
      }
    });
    table.setFormatter(new TableFormatter() {
      @Override
      public void format(TableItem item)
      {
        if (item == null || item.getData() == null)
          return;
        try
        {
          KeyObject o = (KeyObject) item.getData();
          if (!o.key.isEnabled())
            item.setForeground(Color.COMMENT.getSWTColor());
        }
        catch (Exception e)
        {
          Logger.error("error while formatting line",e);
        }
      }
    });
    
    table.addSelectionListener(new Listener() {
      @Override
      public void handleEvent(Event event)
      {
        apply.setEnabled(table.getSelection() != null);
      }
    });

    table.addColumn(i18n.tr("Dateiname"),"filename");
    table.addColumn(i18n.tr("Alias-Name"),"alias");
    table.setMulti(false);
    table.setSummary(false);
    table.paint(parent);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(apply);
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
    buttons.paint(parent);
  }
  
  /**
   * Die Action zum Uebernehmen der Auswahl.
   */
  private class Apply implements Action
  {
    @Override
    public void handleAction(Object context) throws ApplicationException
    {
      if (context == null || !(context instanceof KeyObject))
      {
        Logger.warn("no key choosen");
        return;
      }

      KeyObject key = (KeyObject) context;
      try
      {
        if (!key.key.isEnabled())
        {
          Logger.warn("choosen key not enabled");
          return;
        }
      }
      catch (RemoteException e)
      {
        Logger.error("error while checking if key is enabled",e);
      }
      selected = key.key;
      close();
    }
  }

  @Override
  protected Object getData() throws Exception
  {
    return selected;
  }

  private class KeyObject implements GenericObject
  {

    private RDHKey key = null;
    
    private KeyObject(RDHKey key)
    {
      this.key = key;
    }

    @Override
    public Object getAttribute(String attribute) throws RemoteException
    {
      if ("filename".equals(attribute))
        return key.getFilename();
      if ("alias".equals(attribute))
        return key.getAlias();
      return null;
    }

    @Override
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[] {"filename","alias"};
    }

    @Override
    public String getID() throws RemoteException
    {
      return key.getFilename();
    }

    @Override
    public String getPrimaryAttribute() throws RemoteException
    {
      return "filename";
    }

    @Override
    public boolean equals(GenericObject arg0) throws RemoteException
    {
      if (arg0 == null)
        return false;
      return this.getID().equals(arg0.getID());
    }
    
  }
}
