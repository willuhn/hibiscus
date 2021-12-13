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
import java.util.Collections;
import java.util.Comparator;
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
  @Override
  protected UmsatzTyp getGroup(Umsatz u) throws RemoteException
  {
    return u.getUmsatzTyp();
  }
  
  @Override
  protected String toString(UmsatzTyp t) throws RemoteException
  {
    return t != null ? t.getName() : i18n.tr("<Keine Kategorie>");
  }
  
  @Override
  protected void sort(List<UmsatzTyp> groups) throws RemoteException
  {
    try
    {
      Collections.sort(groups,new Comparator<UmsatzTyp>() {
        @Override
        public int compare(UmsatzTyp o1, UmsatzTyp o2)
        {
          try
          {
            return UmsatzTypUtil.compare(o1,o2);
          }
          catch (RemoteException re)
          {
            Logger.error("unable to compare categories",re);
          }
          return 0;
        }
      });
    }
    catch (Exception e)
    {
      // Wenn das Sortieren aus irgend einem Grund fehlschlaegt, lassen wir die Reihenfolge halt unveraendert
      Logger.error("unable to sort categories",e);
    }
  }
  
  @Override
  public String getName()
  {
    return i18n.tr("PDF-Format (gruppiert nach Kategorie)");
  }
}
