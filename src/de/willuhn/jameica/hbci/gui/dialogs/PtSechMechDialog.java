/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/PtSechMechDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/02/21 22:51:36 $
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

import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum zur Auswahl des Pin/Tan Scurity-Mechanismus.
 * BUGZILLA 200
 */
public class PtSechMechDialog extends AbstractDialog
{
  private I18N i18n        = null;
  private SelectInput type = null;
  private String options   = null;
  private Type choosen     = null;
  
  /**
   * ct.
   * @param options die zur Verfuegung stehenden Optionen.
   */
  public PtSechMechDialog(String options)
  {
    super(PtSechMechDialog.POSITION_CENTER);
    this.options = options;
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setTitle(i18n.tr("Auswahl des PIN/TAN-Verfahrens"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Verfügbare Verfahren"));
    
    group.addText(i18n.tr("Bitte wählen Sie das gewünschte PIN/TAN-Verfahren"),true);
    
    group.addLabelPair(i18n.tr("Bezeichnung"), getType());
    
    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("Übernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          choosen = (Type) getType().getValue();
          close();
        }
        catch (RemoteException e)
        {
          Logger.error("unable to apply data",e);
          throw new ApplicationException(i18n.tr("Fehler beim Übernehmen des PIN/TAN-Verfahrens"));
        }
      }
    },null,true);
    buttons.addButton(i18n.tr("Abbrechen"),new Action() {
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
    if (choosen == null)
      return null;
    return choosen.getID();
  }

  /**
   * Erzeugt eine Combo-Box mit der Auswahl der verfuegbaren Verfahren.
   * @return Auwahl-Feld.
   * @throws RemoteException
   */
  private SelectInput getType() throws RemoteException
  {
    if (this.type != null)
      return this.type;

    String[] s = this.options.split("\\|");
    Type[] types = new Type[s.length];
    for (int i=0;i<s.length;++i)
    {
      types[i] = new Type(s[i]);
    }
    this.type = new SelectInput(PseudoIterator.fromArray(types),null);
    return this.type;
  }

  /**
   * Hilfs-Objekt zur Anzeige der Optionen in einer Combo-Box.
   */
  private class Type implements GenericObject
  {
    private String id   = null;
    private String name = null;
    
    private Type(String text)
    {
      String[] s = text.split(":");
      this.id   = s[0];
      this.name = s[1];
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
      return new String[]{"name"};
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
      return this.getID().equals(arg0.getID());
    }
    
  }
}


/*********************************************************************
 * $Log: PtSechMechDialog.java,v $
 * Revision 1.1  2006/02/21 22:51:36  willuhn
 * @B bug 200
 *
 **********************************************************************/