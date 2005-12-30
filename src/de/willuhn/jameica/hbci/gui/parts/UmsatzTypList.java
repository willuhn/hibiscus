/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/UmsatzTypList.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/12/30 00:14:45 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit den existiernden Umsatz-Typen.
 */
public class UmsatzTypList extends TablePart implements Part
{

  private I18N i18n = null;

  /**
   * ct.
   * @param action
   * @throws RemoteException
   */
  public UmsatzTypList(Action action) throws RemoteException
  {
    super(Settings.getDBService().createList(UmsatzTyp.class), action);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    addColumn(i18n.tr("Bezeichnung"),"name");
    addColumn(i18n.tr("Zweck, Name oder Konto enthält"),"pattern");
    addColumn(i18n.tr("Umsatzart"),"iseinnahme",new Formatter() {
      public String format(Object o)
      {
        if (o == null)
          return i18n.tr("unbekannt");
        Integer i = (Integer) o;
        return i.intValue() == 1 ? i18n.tr("Einnahme") : i18n.tr("Ausgabe");
      }
    });
    setSummary(false);
    
    ContextMenu c = new ContextMenu();
    
    c.addItem(new CheckedContextMenuItem(i18n.tr("Löschen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null)
          return;
        UmsatzTyp typ = (UmsatzTyp) context;
        YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
        d.setTitle(i18n.tr("Wirklich löschen?"));
        d.setText(i18n.tr("Wollen Sie den Umsatz-Filter wirklich löschen?\nDie Umsätze selbst bleiben hierbei erhalten"));
        try
        {
          Boolean b = (Boolean) d.open();
          if (b.booleanValue())
          {
            typ.delete();
            removeItem(typ);
            GUI.getStatusBar().setSuccessText(i18n.tr("Umsatz-Filter gelöscht"));
          }
        }
        catch (ApplicationException ae)
        {
          GUI.getStatusBar().setErrorText(ae.getMessage());
        }
        catch (Exception e)
        {
          Logger.error("unable to delete umsatz type",e);
          GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Löschen des Umsatz-Filters"));
        }
      }
    }));
    this.setContextMenu(c);
  }
}


/**********************************************************************
 * $Log: UmsatzTypList.java,v $
 * Revision 1.3  2005/12/30 00:14:45  willuhn
 * @N first working pie charts
 *
 * Revision 1.2  2005/12/29 01:22:12  willuhn
 * @R UmsatzZuordnung entfernt
 * @B Debugging am Pie-Chart
 *
 * Revision 1.1  2005/12/05 20:16:15  willuhn
 * @N Umsatz-Filter Refactoring
 *
 **********************************************************************/