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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.util.ApplicationException;
import net.sf.paperclips.GridPrint;
import net.sf.paperclips.LineBreakPrint;
import net.sf.paperclips.TextPrint;

/**
 * Druck-Support fuer eine einzelne Buchung in einer SEPA-Sammellastschrift.
 */
public class PrintSupportSepaSammelLastBuchung extends AbstractPrintSupportSepaSammelTransferBuchung<SepaSammelLastBuchung>
{
  /**
   * ct.
   * @param u der zu druckende Auftrag.
   */
  public PrintSupportSepaSammelLastBuchung(SepaSammelLastBuchung u)
  {
    super(u);
  }

  @Override
  String getTitle() throws ApplicationException
  {
    return i18n.tr("Buchung einer SEPA-Sammellastschrift");
  }
  
  @Override
  void customize(GridPrint table) throws RemoteException, ApplicationException
  {
    SepaSammelLastBuchung a = this.getTransfer();
    SepaSammelLastschrift t = a.getSammelTransfer();
    
    // Leerzeile
    table.add(new LineBreakPrint(fontTiny));
    table.add(new LineBreakPrint(fontTiny));

    // Wir fuegen noch ein paar SEPA-spezifische Sachen hinzu.
    table.add(new TextPrint(i18n.tr("Sequenz-Typ"),fontNormal));
    table.add(new TextPrint(t.getSequenceType().getDescription(),fontNormal));
    table.add(new TextPrint(i18n.tr("Lastschrift-Art"),fontNormal));
    table.add(new TextPrint(t.getType().getDescription(),fontNormal));
    Date faellig = t.getTargetDate();
    table.add(new TextPrint(i18n.tr("Fälligkeitsdatum"),fontNormal));
    table.add(new TextPrint(faellig == null ? "-" : HBCI.DATEFORMAT.format(faellig),fontNormal));
  }
}
