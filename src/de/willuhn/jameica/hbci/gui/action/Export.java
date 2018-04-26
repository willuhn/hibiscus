/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ExportDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Generische Action zum Starten eines Exports.
 */
public class Export implements Action
{
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private Class type = null;
  private Object data = null;
  
  /**
   * ct.
   * @param type der zu exportierende Typ.
   */
  public Export(Class type)
  {
    this.type = type;
  }
  
  /**
   * ct.
   * @param type der zu exportierende Typ.
   * @param data die zu exportierenden Daten. Wenn angegeben, haben sie Vorrang vor denen in handleAction.
   */
  public Export(Class type, Object data)
  {
    this(type);
    this.data = data;
  }
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    Object export = this.data != null ? this.data : context;
    
    if (export == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierenden Daten aus"));

    Object[] objects = null;
    
    if (export instanceof Object[])
      objects = (Object[]) export;
    else
      objects = new Object[]{export};

    try
    {
      ExportDialog d = new ExportDialog(objects, type);
      d.open();
		}
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
    }
		catch (ApplicationException ae)
		{
			throw ae;
		}
		catch (Exception e)
		{
			Logger.error("export failed",e);
			throw new ApplicationException(i18n.tr("Export fehlgeschlagen: {0}",e.getMessage()),e);
		}
  }

}
