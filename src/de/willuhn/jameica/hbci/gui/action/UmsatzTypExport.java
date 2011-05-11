/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UmsatzTypExport.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/05/11 10:20:29 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ExportDialog;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, ueber die Umsatz-Kategorien exportiert werden koennen.
 * Als Parameter kann eine einzelnes Umsatztyp-Objekt oder ein Array uebergeben werden.
 */
public class UmsatzTypExport implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>UmsatzTyp</code> oder <code>UmsatzTyp[]</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
			throw new ApplicationException(i18n.tr("Bitte wählen Sie mindestens eine Umsatz-Kategorie aus"));

		if (!(context instanceof UmsatzTyp) &&
        !(UmsatzTyp[].class.isAssignableFrom(context.getClass())))
			throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Umsatz-Kategorien aus"));

    Object[] u = null;
		try {

			if (context instanceof UmsatzTyp)
			{
				u = new UmsatzTyp[1];
        u[0] = (UmsatzTyp) context;
			}
      else
      {
        u = (Object[])context;
      }

      ExportDialog d = new ExportDialog(u, UmsatzTyp.class);
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
			Logger.error("error while exporting categories",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Exportieren der Umsatz-Kategorien"));
		}
  }

}


/**********************************************************************
 * $Log: UmsatzTypExport.java,v $
 * Revision 1.2  2011/05/11 10:20:29  willuhn
 * @N OCE fangen
 *
 * Revision 1.1  2008/02/13 23:44:27  willuhn
 * @R Hibiscus-Eigenformat (binaer-serialisierte Objekte) bei Export und Import abgeklemmt
 * @N Import und Export von Umsatz-Kategorien im XML-Format
 * @B Verzaehler bei XML-Import
 *
 **********************************************************************/