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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ImportDialog;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Importieren von Kontoauszuegen.
 */
public class KontoauszugImport implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

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
        d.setText(i18n.tr("Bitte wählen Sie das Konto, dem die Kontoauszüge zugeordnet werden sollen."));
        context = (Konto) d.open();
      }

      ImportDialog d = new ImportDialog((Konto) context, Kontoauszug.class);
      d.open();
    }
    catch (OperationCanceledException oce)
    {
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while importing account statements",e);
      throw new ApplicationException(i18n.tr("Import der Kontoauszüge fehlgeschlagen: {0}",e.getMessage()));
    }
  }

}


