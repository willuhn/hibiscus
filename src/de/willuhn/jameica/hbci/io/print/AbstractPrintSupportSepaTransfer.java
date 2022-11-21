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
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.BaseUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import net.sf.paperclips.DefaultGridLook;
import net.sf.paperclips.EmptyPrint;
import net.sf.paperclips.GridPrint;
import net.sf.paperclips.LineBreakPrint;
import net.sf.paperclips.PagePrint;
import net.sf.paperclips.Print;
import net.sf.paperclips.TextPrint;

/**
 * Abstrakter Druck-Support fuer einzelne SEPA-Ueberweisungen und -Lastschriften.
 * @param <T> der konkrete Typ des SEPA-Auftrages.
 */
public abstract class AbstractPrintSupportSepaTransfer<T extends BaseUeberweisung> extends AbstractPrintSupport
{
  private T auftrag = null;
  
  /**
   * ct.
   * @param a der zu druckende Auftrag.
   */
  public AbstractPrintSupportSepaTransfer(T a)
  {
    this.auftrag = a;
  }
  
  @Override
  Print printContent() throws ApplicationException
  {
    if (this.auftrag == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Auftrag aus"));
    
    try
    {
      T a     = this.auftrag;
      Konto k = a.getKonto();
      
      // Die eigentlich Tabelle mit den Werten
      DefaultGridLook look = new DefaultGridLook(5,5);
      GridPrint table = new GridPrint("l:p:n, l:d:g",look);

      // Konto
      table.add(new TextPrint(i18n.tr("Konto"),fontNormal));
      table.add(new TextPrint(notNull(k != null ? k.getLongName() : null),fontNormal));
      table.add(new EmptyPrint());
      table.add(new TextPrint(i18n.tr("IBAN: {0}",HBCIProperties.formatIban(k.getIban())),fontNormal));
      
      // Leerzeile
      table.add(new LineBreakPrint(fontNormal));
      table.add(new LineBreakPrint(fontNormal));
      
      // Gegenkonto
      {
        table.add(new TextPrint(i18n.tr("Gegenkonto"),fontNormal));
        table.add(new TextPrint(i18n.tr("{0}\nIBAN: {1}\nBIC: {2}\nBank: {3}",a.getGegenkontoName(),HBCIProperties.formatIban(a.getGegenkontoNummer()),a.getGegenkontoBLZ(),HBCIProperties.getNameForBank(a.getGegenkontoBLZ())),fontNormal));
      }

      // Leerzeile
      table.add(new LineBreakPrint(fontTiny));
      table.add(new LineBreakPrint(fontTiny));
      
      // Verwendungszweck
      {
        table.add(new TextPrint(i18n.tr("Verwendungszweck"),fontNormal));
        table.add(new TextPrint(VerwendungszweckUtil.toString(a,"\n"),fontNormal));
      }

      // Betrag
      {
        double betrag = a.getBetrag();
        String curr = k != null ? k.getWaehrung() : HBCIProperties.CURRENCY_DEFAULT_DE;
        
        table.add(new TextPrint(i18n.tr("Betrag"),fontNormal));
        table.add(new TextPrint(betrag == 0.0d || Double.isNaN(betrag) ? "-" : (HBCI.DECIMALFORMAT.format(betrag) + " " + curr),fontBold));
      }

      // Leerzeile
      table.add(new LineBreakPrint(fontTiny));
      table.add(new LineBreakPrint(fontTiny));
      
      // Der Rest
      {
        boolean bankSide = false;
        if (a instanceof AuslandsUeberweisung)
        {
          AuslandsUeberweisung au = (AuslandsUeberweisung) a;
          bankSide = au.isTerminUeberweisung();
        }
        
        if (bankSide)
        {
          table.add(new TextPrint(i18n.tr("Auftragsart"),fontNormal));
          table.add(new TextPrint(i18n.tr("Bankseitiger Terminauftrag"),fontNormal));
        }

        Date termin = a.getTermin();
        table.add(new TextPrint(i18n.tr(bankSide ? "Ausführungstermin" : "Erinnerungstermin"),fontNormal));
        table.add(new TextPrint(termin == null ? "-" : HBCI.DATEFORMAT.format(termin),fontNormal));
        
        Date ausgefuehrt = a.getAusfuehrungsdatum();
        table.add(new TextPrint(i18n.tr(bankSide ? "Eingereicht" : "Ausgeführt"),fontNormal));
        if (ausgefuehrt != null)
          table.add(new TextPrint(HBCI.DATEFORMAT.format(ausgefuehrt),fontBold));
        else
          table.add(new TextPrint(a.ausgefuehrt() ? "Ja" : "Nein",fontBold));
        
        customize(table);
      } 
      return table;
    }
    catch (RemoteException re)
    {
      Logger.error("unable to print data",re);
      throw new ApplicationException(i18n.tr("Druck fehlgeschlagen: {0}",re.getMessage()));
    }
  }
  
  /**
   * Liefert den Auftrag.
   * @return der Auftrag.
   */
  protected T getTransfer()
  {
    return this.auftrag;
  }
  
  /**
   * Kann ueberschrieben werden, um noch Anpassungen vorzunehmen.
   * @param grid das Grid.
   * @throws RemoteException
   * @throws ApplicationException
   */
  void customize(GridPrint grid) throws RemoteException, ApplicationException
  {
    
  }
  
  @Override
  void customize(PagePrint page) throws ApplicationException
  {
    // Footer mit den Seitenzahlen entfernen. Macht bei einer Einzel-Ueberweisung keinen Sinn.
    page.setFooter(null);
  }
}
