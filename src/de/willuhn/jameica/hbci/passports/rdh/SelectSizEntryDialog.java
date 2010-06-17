/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/SelectSizEntryDialog.java,v $
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
import java.util.StringTokenizer;

import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Ein Dialog zur Auswahl des zu importierenden SizRDH-Schluessels.
 */
public class SelectSizEntryDialog extends AbstractDialog
{
  private String data     = null;
  private Entry selected  = null;

  private I18N i18n       = null;

  /**
   * ct.
   * @param position
   * @param data die vom Callback uebermittelten Schluesseldaten.
   */
  public SelectSizEntryDialog(int position, String data)
  {
    super(position);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.data = data;
    setTitle(i18n.tr("Schlüsselauswahl"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Schlüsseldatei"));
    group.addText(i18n.tr("Bitte wählen Sie den zu verwendenden Schlüssel aus"),true);
    
    ArrayList l = new ArrayList();

    StringTokenizer tok = new StringTokenizer(data,"|");
    while (tok.hasMoreTokens())
    {
      Entry e = new Entry(tok.nextToken());
      l.add(e);
    }
    
    GenericIterator list = PseudoIterator.fromArray((Entry[]) l.toArray(new Entry[l.size()]));
    final TablePart table = new TablePart(list, new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof Entry))
          return;
        Entry e = (Entry) context;
        selected = e;
        close();
      }
    });
    table.addColumn(i18n.tr("BLZ"),"blz");
    table.addColumn(i18n.tr("Benutzerkennung"),"userid");
    table.setMulti(false);
    table.setSummary(false);
    table.paint(parent);
    
    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("Übernehmen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        Object o = table.getSelection();
        if (o == null || !(o instanceof Entry))
          return;

        Entry e = (Entry) context;
        selected = e;
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
    return selected.getID();
  }

  private class Entry implements GenericObject
  {

    private String id     = null;
    private String userid = null;
    private String blz    = null;
    
    private Entry(String data)
    {
      StringTokenizer tok = new StringTokenizer(data,";");

      this.id     = tok.nextToken();
      this.blz    = tok.nextToken();
      this.userid = tok.nextToken();
      
      try
      {
        String bankName = HBCIUtils.getNameForBLZ(this.blz);
        if (bankName != null && bankName.length() > 0)
          this.blz += " [" + bankName + "]";
      }
      catch (Exception e)
      {
        // ignore
      }
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String attribute) throws RemoteException
    {
      if ("userid".equals(attribute))
        return this.userid;
      if ("blz".equals(attribute))
        return this.blz;
      return null;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[] {"userid","blz"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return this.id;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "userid";
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
 * $Log: SelectSizEntryDialog.java,v $
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.1  2006/10/12 12:53:01  willuhn
 * @B bug 289 + Callback NEED_SIZENTRY_SELECT
 *
 **********************************************************************/