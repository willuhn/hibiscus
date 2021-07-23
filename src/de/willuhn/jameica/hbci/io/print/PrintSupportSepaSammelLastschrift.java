/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.print;

import java.rmi.RemoteException;
import java.util.Date;

import net.sf.paperclips.GridPrint;
import net.sf.paperclips.TextPrint;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.util.ApplicationException;


/**
 * Druck-Support fuer SEPA-Sammel-Lastschriften.
 */
public class PrintSupportSepaSammelLastschrift extends AbstractPrintSupportSepaSammelTransfer<SepaSammelLastschrift>
{
  /**
   * ct.
   * @param ctx die zu druckenden Daten.
   */
  public PrintSupportSepaSammelLastschrift(Object ctx)
  {
    super(ctx);
  }

  @Override
  String getTitle() throws ApplicationException
  {
    return i18n.tr("SEPA-Sammellastschrift");
  }
  
  @Override
  GridPrint createTransferTable(SepaSammelLastschrift a) throws RemoteException, ApplicationException
  {
    GridPrint table = super.createTransferTable(a);
    
    // Wir fuegen noch ein paar SEPA-spezifische Sachen hinzu.
    table.add(new TextPrint(i18n.tr("Sequenz-Typ"),fontNormal));
    table.add(new TextPrint(a.getSequenceType().getDescription(),fontNormal));
    table.add(new TextPrint(i18n.tr("Lastschrift-Art"),fontNormal));
    table.add(new TextPrint(a.getType().getDescription(),fontNormal));
    Date faellig = a.getTargetDate();
    table.add(new TextPrint(i18n.tr("Zieltermin"),fontNormal));
    table.add(new TextPrint(faellig == null ? "-" : HBCI.DATEFORMAT.format(faellig),fontNormal));

    return table;
  }
}
