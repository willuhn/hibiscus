/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

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
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Objekt vom Typ <code>Konto</code>,<code>Umsatz</code> oder <code>null</code>.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
		// Check, ob das wirklich ein Konto ist
    if (context != null && !(context instanceof Konto))
      context = null;

    try
    {
      if (context == null)
      {
        // Immer noch kein Konto? Dann User fragen
        KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_CENTER);
        d.setText(i18n.tr("Bitte w�hlen Sie das Konto, in dem die Ums�tze gespeichert werden sollen"));
        context = (Konto) d.open();
      }

      ImportDialog d = new ImportDialog((Konto) context, Umsatz.class);
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
			Logger.error("error while importing umsaetze",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Importieren der Ums�tze"));
		}
  }

}


/**********************************************************************
 * $Log: UmsatzImport.java,v $
 * Revision 1.5  2011/05/11 10:20:28  willuhn
 * @N OCE fangen
 *
 * Revision 1.4  2011-05-03 16:43:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2010/03/16 00:44:18  willuhn
 * @N Komplettes Redesign des CSV-Imports.
 *   - Kann nun erheblich einfacher auch fuer andere Datentypen (z.Bsp.Ueberweisungen) verwendet werden
 *   - Fehlertoleranter
 *   - Mehrfachzuordnung von Spalten (z.Bsp. bei erweitertem Verwendungszweck) moeglich
 *   - modulare Deserialisierung der Werte
 *   - CSV-Exports von Hibiscus koennen nun 1:1 auch wieder importiert werden (Import-Preset identisch mit Export-Format)
 *   - Import-Preset wird nun im XML-Format nach ~/.jameica/hibiscus/csv serialisiert. Damit wird es kuenftig moeglich sein,
 *     CSV-Import-Profile vorzukonfigurieren und anschliessend zu exportieren, um sie mit anderen Usern teilen zu koennen
 *
 * Revision 1.2  2006/04/20 08:44:21  willuhn
 * @C s/Childs/Children/
 *
 * Revision 1.1  2006/01/18 00:51:01  willuhn
 * @B bug 65
 *
 **********************************************************************/