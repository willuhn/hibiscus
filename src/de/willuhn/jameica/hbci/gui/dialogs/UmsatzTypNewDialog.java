/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.UmsatzTypControl;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum Speichern eines neuen Umsatz-Typs.
 */
public class UmsatzTypNewDialog extends AbstractDialog
{
  private I18N i18n        = null;
  private LabelInput check = null;
  private UmsatzTypControl control = null;
  
  /**
   * @param position
   */
  public UmsatzTypNewDialog(int position)
  {
    super(position);
    this.control = new UmsatzTypControl(null);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setTitle(i18n.tr("Umsatz-Kategorie speichern"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Eigenschaften"));
    group.addLabelPair(i18n.tr("Bezeichnung"), control.getName());
    group.addLabelPair(i18n.tr("Art des Umsatzes"), control.getArt());
    group.addLabelPair("", getCheck());
    
    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("Übernehmen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          String s = (String) control.getName().getValue();
          if (s == null || s.length() == 0)
          {
            getCheck().setValue(i18n.tr("Bitte geben Sie eine Bezeichnung ein."));
            return;
          }

          UmsatzTyp typ = control.getUmsatzTyp();
          typ.setName(s);
          UmsatzTypControl.UmsatzTypObject uto = (UmsatzTypControl.UmsatzTypObject) control.getArt().getValue();
          typ.setTyp(uto.getTyp());
          close();
        }
        catch (RemoteException e)
        {
          Logger.error("unable to apply data",e);
          throw new ApplicationException(i18n.tr("Fehler beim Speichern des Umsatz-Filters"));
        }
      }
    },null,true);
    buttons.addButton(i18n.tr("Abbrechen"), new Action()
    {
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
    return control.getUmsatzTyp();
  }

  /**
   * Liefert ein Label, welches Fehler anzeigt.
   * @return Label.
   */
  private LabelInput getCheck()
  {
    if (this.check == null)
    {
      this.check = new LabelInput("");
      this.check.setColor(Color.ERROR);
    }
    return this.check;
  }
}


/*********************************************************************
 * $Log: UmsatzTypNewDialog.java,v $
 * Revision 1.4  2008/08/29 16:46:24  willuhn
 * @N BUGZILLA 616
 *
 * Revision 1.3  2006/11/23 23:24:17  willuhn
 * @N Umsatz-Kategorien: DB-Update, Edit
 *
 * Revision 1.1  2005/12/30 00:14:45  willuhn
 * @N first working pie charts
 *
 **********************************************************************/