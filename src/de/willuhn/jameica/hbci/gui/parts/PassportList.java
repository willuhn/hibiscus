/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Sicherheitsmedien.
 */
public class PassportList extends TablePart implements Part
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @param action
   * @throws RemoteException
   */
  public PassportList(final Action action) throws RemoteException
  {
    super(init(),action);

    ContextMenu menu = new ContextMenu();
    menu.addItem(new CheckedSingleContextMenuItem(i18n.tr("Öffnen..."),new PassportDetail(),"document-open.png"));
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
 * Revision 1.9  2011/04/28 07:34:01  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.8  2010/04/14 16:53:01  willuhn
 * @N BUGZILLA 471
 **********************************************************************/