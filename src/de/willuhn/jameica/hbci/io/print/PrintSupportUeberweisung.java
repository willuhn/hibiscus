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

import net.sf.paperclips.GridPrint;
import net.sf.paperclips.TextPrint;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.util.ApplicationException;

/**
 * Druck-Support fuer einzelne Ueberweisungen.
 */
public class PrintSupportUeberweisung extends AbstractPrintSupportBaseUeberweisung
{
  private Ueberweisung u = null;
  
  /**
   * ct.
   * @param u die zu druckende Ueberweisung.
   */
  public PrintSupportUeberweisung(Ueberweisung u)
  {
    super(u);
    this.u = u;
  }

  @Override
  String getTitle() throws ApplicationException
  {
    return i18n.tr("Überweisung");
  }

  @Override
  void customize(GridPrint grid) throws RemoteException, ApplicationException
  {
    super.customize(grid);

    if (this.u == null)
      return;
    
    String typ = i18n.tr("Überweisung");
    if (u.isTerminUeberweisung())
      typ = "Termin-Überweisung";
    else if (u.isUmbuchung())
      typ = "Umbuchung";
    grid.add(new TextPrint(i18n.tr("Auftragstyp"),fontNormal));
    grid.add(new TextPrint(typ,fontNormal));
  }
}



/**********************************************************************
 * $Log: PrintSupportUeberweisung.java,v $
 * Revision 1.6  2011/04/13 17:35:46  willuhn
 * @N Druck-Support fuer Kontoauszuege fehlte noch
 *
 * Revision 1.5  2011-04-11 14:36:37  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 **********************************************************************/