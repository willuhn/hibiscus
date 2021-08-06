/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.rmi.RemoteException;
import java.util.List;

import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.UmsatzTypUtil;
import de.willuhn.logging.Logger;

/**
 * Exportiert Umsaetze gruppiert nach Kategorie im PDF-Format.
 */
public class PDFUmsatzByTypeExporter extends AbstractPDFUmsatzExporter<UmsatzTyp>
{
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractPDFUmsatzExporter#getGroup(de.willuhn.jameica.hbci.rmi.Umsatz)
   */
  @Override
  protected UmsatzTyp getGroup(Umsatz u) throws RemoteException
  {
    return u.getUmsatzTyp();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractPDFUmsatzExporter#toString(de.willuhn.datasource.GenericObject)
   */
  @Override
  protected String toString(UmsatzTyp t) throws RemoteException
  {
    return t != null ? t.getName() : i18n.tr("<Keine Kategorie>");
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractPDFUmsatzExporter#sort(java.util.List)
   */
  @Override
  protected void sort(List<UmsatzTyp> groups) throws RemoteException
  {
    try
    {
      groups.sort((typ1, typ2) -> {
        try
        {
          return UmsatzTypUtil.compare(typ1,typ2);
        }
        catch (RemoteException re)
        {
          Logger.error("unable to compare categories",re);
        }
        return 0;
      });
    }
    catch (Exception e)
    {
      // Wenn das Sortieren aus irgend einem Grund fehlschlaegt, lassen wir die Reihenfolge halt unveraendert
      Logger.error("unable to sort categories",e);
    }
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("PDF-Format (gruppiert nach Kategorie)");
  }
}
