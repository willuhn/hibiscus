/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/PassportList.java,v $
 * $Revision: 1.8 $
 * $Date: 2010/04/14 16:53:01 $
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
import java.util.Arrays;
import java.util.List;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Sicherheitsmedien.
 */
public class PassportList extends TablePart implements Part
{
  private I18N i18n = null;

  /**
   * @param action
   * @throws RemoteException
   */
  public PassportList(final Action action) throws RemoteException
  {
    super(init(), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null)
          return;
        action.handleAction((Passport) context);
      }
    });
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    ContextMenu menu = new ContextMenu();
    menu.addItem(new CheckedContextMenuItem(i18n.tr("Öffnen..."),new PassportDetail(),"document-open.png"));
    this.setContextMenu(menu);

    this.setMulti(false);
    this.setSummary(false);
    addColumn(i18n.tr("Bezeichnung"),"name");
    addColumn(i18n.tr("Information"),"info");
  }

  /**
   * Initialisiert die Liste der Passports.
   * @return Liste der Passports.
   * @throws RemoteException
   */
  private static List<Passport> init() throws RemoteException
  {
    try
    {
      return Arrays.asList(PassportRegistry.getPassports());
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      Logger.error("error while loading the passport list",e);
      throw new RemoteException("unable to load the passport list",e);
    }
  }
}


/**********************************************************************
 * $Log: PassportList.java,v $
 * Revision 1.8  2010/04/14 16:53:01  willuhn
 * @N BUGZILLA 471
 *
 * Revision 1.7  2009/05/07 13:36:57  willuhn
 * @R Hilfsobjekt "PassportObject" entfernt
 * @C Cleanup in PassportInput (insb. der weisse Hintergrund hinter dem "Konfigurieren..."-Button hat gestoert
 *
 * Revision 1.6  2008/12/19 12:16:05  willuhn
 * @N Mehr Icons
 * @C Reihenfolge der Contextmenu-Eintraege vereinheitlicht
 *
 * Revision 1.5  2006/11/24 00:07:09  willuhn
 * @C Konfiguration der Umsatz-Kategorien in View Einstellungen verschoben
 * @N Redesign View Einstellungen
 *
 * Revision 1.4  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/07/04 21:57:08  web0
 * @B bug 80
 *
 * Revision 1.2  2005/06/27 15:35:27  web0
 * @B bug 84
 *
 * Revision 1.1  2005/06/21 21:48:24  web0
 * @B bug 80
 *
 **********************************************************************/