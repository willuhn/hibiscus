/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/UmsatzImport.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/04/20 08:44:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ImportDialog;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, ueber die Umsaetze importiert werden koennen.
 * Als Parameter kann ein Konto oder <code>null</code> uebergeben werden.
 */
public class UmsatzImport implements Action
{

  /**
   * Erwartet ein Objekt vom Typ <code>Konto</code>,<code>Umsatz</code> oder <code>null</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    // Falls der Context ein Umsatz ist, holen wir uns das Konto dazu
    if (context != null && (context instanceof Umsatz))
    {
      try
      {
        context = ((Umsatz) context).getKonto();
      }
      catch (RemoteException e)
      {
        Logger.error("unable to load konto from umsatz",e);
        // muessen wir nicht werfen
      }
    }

    // Nochmal der Check, ob das wirklich ein Konto ist
    if (context != null && !(context instanceof Konto))
      context = null;

    try
    {
      if (context == null)
      {
        // Immer noch kein Konto? Dann User fragen
        KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_CENTER);
        d.setText(i18n.tr("Bitte wählen Sie das Konto, in dem die Umsätze gespeichert werden sollen"));
        context = (Konto) d.open();
      }

      ImportDialog d = new ImportDialog((Konto) context, Umsatz.class);
      d.open();
		}
    catch (OperationCanceledException oce)
    {
      // ignore
    }
		catch (ApplicationException ae)
		{
			throw ae;
		}
		catch (Exception e)
		{
			Logger.error("error while importing umsaetze",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Importieren der Umsätze"));
		}
  }

}


/**********************************************************************
 * $Log: UmsatzImport.java,v $
 * Revision 1.2  2006/04/20 08:44:21  willuhn
 * @C s/Childs/Children/
 *
 * Revision 1.1  2006/01/18 00:51:01  willuhn
 * @B bug 65
 *
 **********************************************************************/