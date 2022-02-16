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
import net.sf.paperclips.LineBreakPrint;
import net.sf.paperclips.TextPrint;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer SEPA-Lastschriften.
 */
public class PrintSupportSepaLastschrift extends AbstractPrintSupportSepaTransfer<SepaLastschrift>
{
  /**
   * ct.
   * @param u die zu druckende Auslandsueberweisung.
   */
  public PrintSupportSepaLastschrift(SepaLastschrift u)
  {
    super(u);
  }

  @Override
  String getTitle() throws ApplicationException
  {
    return i18n.tr("SEPA-Lastschrift");
  }
  
  @Override
  void customize(GridPrint table) throws RemoteException, ApplicationException
  {
    SepaLastschrift a = this.getTransfer();
    
    // Leerzeile
    table.add(new LineBreakPrint(fontTiny));
    table.add(new LineBreakPrint(fontTiny));

    // Wir fuegen noch ein paar SEPA-spezifische Sachen hinzu.
    table.add(new TextPrint(i18n.tr("Sequenz-Typ"),fontNormal));
    table.add(new TextPrint(a.getSequenceType().getDescription(),fontNormal));
    table.add(new TextPrint(i18n.tr("Lastschrift-Art"),fontNormal));
    table.add(new TextPrint(a.getType().getDescription(),fontNormal));
    Date faellig = a.getTargetDate();
    table.add(new TextPrint(i18n.tr("F�lligkeitsdatum"),fontNormal));
    table.add(new TextPrint(faellig == null ? "-" : HBCI.DATEFORMAT.format(faellig),fontNormal));
  }
}
