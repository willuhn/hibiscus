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

import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Exportieren von Kontoauszuegen.
 */
public class KontoauszugExport extends Export
{

  /**
   * ct.
   */
  public KontoauszugExport()
  {
    super(Kontoauszug.class);
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.action.Export#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof Kontoauszug) && !(context instanceof Kontoauszug[]))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierenden Kontoauszüge aus"));

    try
    {
      String note = i18n.tr("Bitte beachten Sie, dass die PDF-Dateien selbst nicht in der\n" +
                             "exportierten Datei enthalten sind. Kopieren Sie diese bitte manuell.\n\n" +
                             "Export fortsetzen?");
      if (!Application.getCallback().askUser(note,true))
        return;
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to notify user",e);
      throw new ApplicationException(i18n.tr("Export fehlgeschlagen: {0}",e.getMessage()));
    }
    super.handleAction(context);
  }

}
