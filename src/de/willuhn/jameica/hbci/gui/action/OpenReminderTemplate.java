/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/OpenReminderTemplate.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/20 16:20:05 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Versucht die Kopier-Vorlage des angegebenen Auftrages zu oeffnen.
 */
public class OpenReminderTemplate implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof HibiscusDBObject))
      return;
    
    try
    {
      HibiscusDBObject object = (HibiscusDBObject) context;
      String id = MetaKey.REMINDER_TEMPLATE.get(object);
      if (id == null)
        return;
      
      // Checken, ob wir den Datensatz laden koennen
      DBObject o = Settings.getDBService().createObject(object.getClass(),id);
      
      // Wir versuchen, ihn zu oeffnen
      new Open().handleAction(o);
    }
    catch (ObjectNotFoundException oe)
    {
      throw new ApplicationException(i18n.tr("Datensatz nicht gefunden"));
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to open template",e);
      throw new ApplicationException(i18n.tr("Fehlgeschlagen: {0}",e.getMessage()));
    }
  }

}



/**********************************************************************
 * $Log: OpenReminderTemplate.java,v $
 * Revision 1.1  2011/10/20 16:20:05  willuhn
 * @N BUGZILLA 182 - Erste Version von client-seitigen Dauerauftraegen fuer alle Auftragsarten
 *
 **********************************************************************/