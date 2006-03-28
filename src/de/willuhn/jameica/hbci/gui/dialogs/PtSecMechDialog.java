/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/PtSecMechDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/03/28 17:52:23 $
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
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum zur Auswahl des Pin/Tan Scurity-Mechanismus.
 * BUGZILLA 200
 */
public class PtSecMechDialog extends AbstractDialog
{
  private I18N i18n          = null;
  private SelectInput type   = null;
  private CheckboxInput save = null;
  private String options     = null;
  
  private Type choosen       = null;
  
  /**
   * ct.
   * @param options die zur Verfuegung stehenden Optionen.
   */
  public PtSecMechDialog(String options)
  {
    super(PtSecMechDialog.POSITION_CENTER);
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
    group.addCheckbox(getSave(),i18n.tr("Auswahl speichern"));
    
    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("Übernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          choosen = (Type) getType().getValue();
          
          if (choosen != null)
          {
            Boolean b = (Boolean) getSave().getValue();
            if (b.booleanValue())
            {
              // BUGZILLA 218
              try
              {
                Application.getCallback().notifyUser(
                    i18n.tr("Sie können diese Vorauswahl später in der PIN/TAN-Konfiguration\n" +
                             "über die Option \"Automatische Auswahl des TAN-Verfahrens löschen\"\n" +
                             "wieder rückgängig machen."));
              }
              catch (Exception e)
              {
                Logger.error("unable to notify user",e);
              }
              Konto k = HBCIFactory.getInstance().getCurrentKonto();
              Settings.setSecMech(k,choosen.getID());
            }
          }
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
   * Liefert eine Checkbox, mit der der User entscheiden kann, ob seine Auswahl gespeichert werden soll.
   * @return Checkbox.
   */
  private CheckboxInput getSave()
  {
    if (this.save != null)
      return this.save;
    
    this.save = new CheckboxInput(false);
    return this.save;
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
 * $Log: PtSecMechDialog.java,v $
 * Revision 1.2  2006/03/28 17:52:23  willuhn
 * @B bug 218
 *
 * Revision 1.1  2006/02/23 22:14:58  willuhn
 * @B bug 200 (Speichern der Auswahl)
 *
 * Revision 1.1  2006/02/21 22:51:36  willuhn
 * @B bug 200
 *
 **********************************************************************/