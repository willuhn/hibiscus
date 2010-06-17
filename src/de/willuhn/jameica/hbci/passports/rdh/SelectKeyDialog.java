/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/SelectKeyDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/06/17 11:26:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
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
  private RDHKey selected = null;

  private I18N i18n       = null;

  /**
   * @param position
   */
  public SelectKeyDialog(int position)
  {
    super(position);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setTitle(i18n.tr("Schlüsselauswahl"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Schlüsseldatei"));
    group.addText(i18n.tr("Bitte wählen Sie den zu verwendenden Schlüssel aus"),true);
    
    GenericIterator list = RDHKeyFactory.getKeys();
    RDHKey current = null;
    ArrayList l = new ArrayList();
    while (list.hasNext())
    {
      current = (RDHKey) list.next();
      l.add(new KeyObject(current));
    }
    list = PseudoIterator.fromArray((KeyObject[]) l.toArray(new KeyObject[l.size()]));
    final TablePart table = new TablePart(list, new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof KeyObject))
          return;
        KeyObject o = (KeyObject) context;
        try
        {
          if (!o.key.isEnabled())
            return;
        }
        catch (RemoteException e)
        {
          Logger.error("error while checking if key is enabled",e);
        }
        selected = o.key;
        close();
      }
    });
    table.setFormatter(new TableFormatter() {
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
    table.addColumn(i18n.tr("Dateiname"),"filename");
    table.addColumn(i18n.tr("Alias-Name"),"alias");
    table.setMulti(false);
    table.setSummary(false);
    table.paint(parent);
    
    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("Übernehmen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        Object o = table.getSelection();
        if (o == null || !(o instanceof KeyObject))
          return;

        KeyObject key = (KeyObject) o;
        try
        {
          if (!key.key.isEnabled())
            return;
        }
        catch (RemoteException e)
        {
          Logger.error("error while checking if key is enabled",e);
        }
        selected = key.key;
        close();
      }
    });
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
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

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String attribute) throws RemoteException
    {
      if ("filename".equals(attribute))
        return key.getFilename();
      if ("alias".equals(attribute))
        return key.getAlias();
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[] {"filename","alias"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return key.getFilename();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "filename";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject arg0) throws RemoteException
    {
      if (arg0 == null)
        return false;
      return this.getID().equals(arg0.getID());
    }
    
  }
}


/*********************************************************************
 * $Log: SelectKeyDialog.java,v $
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.6  2008/07/24 23:36:20  willuhn
 * @N Komplette Umstellung der Schluessel-Verwaltung. Damit koennen jetzt externe Schluesselformate erheblich besser angebunden werden.
 * ACHTUNG - UNGETESTETER CODE - BITTE NOCH NICHT VERWENDEN
 *
 * Revision 1.5  2005/11/14 12:33:52  willuhn
 * @B bug 148
 *
 * Revision 1.4  2005/11/14 12:22:31  willuhn
 * @B bug 148
 *
 * Revision 1.3  2005/08/01 22:15:34  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/06/27 15:30:47  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/06/21 21:45:06  web0
 * @B bug 80
 *
 **********************************************************************/