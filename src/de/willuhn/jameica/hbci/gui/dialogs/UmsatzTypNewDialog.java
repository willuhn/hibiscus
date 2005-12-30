/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/UmsatzTypNewDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/12/30 00:14:45 $
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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
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
  private UmsatzTyp typ    = null;
  private TextInput name   = null;
  private SelectInput art  = null;
  private LabelInput check = null;
  
  /**
   * @param position
   */
  public UmsatzTypNewDialog(int position)
  {
    super(position);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setTitle(i18n.tr("Umsatz-Filter speichern"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Eigenschaften"));
    group.addLabelPair(i18n.tr("Bezeichnung"), getName());
    group.addLabelPair(i18n.tr("Art des Umsatzes"), getArt());
    group.addLabelPair("", getCheck());
    
    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("Übernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          String s = (String) getName().getValue();
          if (s == null || s.length() == 0)
          {
            getCheck().setValue(i18n.tr("Bitte geben Sie eine Bezeichnung ein."));
            return;
          }

          typ = (UmsatzTyp) Settings.getDBService().createObject(UmsatzTyp.class,null);
          typ.setName(s);
          s = (String) getArt().getValue();
          typ.setEinnahme(i18n.tr("Einnahme").equals(s));
          close();
        }
        catch (RemoteException e)
        {
          Logger.error("unable to apply data",e);
          throw new ApplicationException(i18n.tr("Fehler beim Speichern des Umsatz-Filters"));
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
    return typ;
  }

  /**
   * Erzeugt das Eingabe-Feld fuer den Namen.
   * @return Eingabe-Feld.
   */
  private TextInput getName()
  {
    if (this.name == null)
      this.name = new TextInput("");
    return this.name;
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
  
  /**
   * Liefert eine Auswahl-Box fuer die Art des Umsatzes.
   * @return Auswahl-Box.
   */
  private SelectInput getArt()
  {
    if (this.art == null)
      this.art = new SelectInput(new String[]{"Ausgabe","Einnahme"},null);
    return this.art;
  }
}


/*********************************************************************
 * $Log: UmsatzTypNewDialog.java,v $
 * Revision 1.1  2005/12/30 00:14:45  willuhn
 * @N first working pie charts
 *
 **********************************************************************/