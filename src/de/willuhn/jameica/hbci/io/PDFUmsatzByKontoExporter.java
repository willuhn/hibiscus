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

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.KontoUtil;

/**
 * Exportiert Umsaetze im PDF-Format.
 * Der Exporter kann Umsaetze mehrerer Konten exportieren. Sie werden
 * hierbei nach Konto gruppiert.
 */
public class PDFUmsatzByKontoExporter extends AbstractPDFUmsatzExporter<Konto>
{
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractPDFUmsatzExporter#getGroup(de.willuhn.jameica.hbci.rmi.Umsatz)
   */
  @Override
  protected Konto getGroup(Umsatz u) throws RemoteException
  {
    return u.getKonto();
  }

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractPDFUmsatzExporter#toString(de.willuhn.datasource.GenericObject)
   */
  @Override
  protected String toString(Konto t) throws RemoteException
  {
    // NULL brauchen wir hier nicht beruecksichtigen. Es kann keinen Umsatz ohne Konto geben
    return KontoUtil.toString(t);
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getName()
   */
  public String getName()
  {
    return i18n.tr("PDF-Format (gruppiert nach Konto)");
  }
}
