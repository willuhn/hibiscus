/**********************************************************************
 *
 * Copyright (c) 2018 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.Open;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;
import io.konik.PdfHandler;
import io.konik.zugferd.Invoice;
import io.konik.zugferd.entity.CreditorFinancialAccount;
import io.konik.zugferd.entity.FinancialInstitution;
import io.konik.zugferd.entity.Header;
import io.konik.zugferd.entity.PaymentMeans;
import io.konik.zugferd.entity.TradeParty;
import io.konik.zugferd.entity.trade.MonetarySummation;
import io.konik.zugferd.entity.trade.Settlement;
import io.konik.zugferd.entity.trade.Trade;
import io.konik.zugferd.unqualified.Amount;

/**
 * Importer fuer Rechnungen im ZUGFeRD-Format.
 */
public class ZUGFeRDImporter implements Importer
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N(); 

  /** 
   * @see de.willuhn.jameica.hbci.io.IO#getName() 
   */ 
  @Override 
  public String getName() 
  { 
    return i18n.tr("PDF-Rechnung im ZUGFeRD-Format"); 
  } 

  /** 
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class) 
   */ 
  @Override 
  public IOFormat[] getIOFormats(Class objectType) 
  { 
    if (!AuslandsUeberweisung.class.equals(objectType)) 
      return null; // Wir bieten uns nur fuer SEPA-Ueberweisungen an 

    IOFormat f = new IOFormat()
    {
      public String getName() 
      { 
        return ZUGFeRDImporter.this.getName(); 
      } 

      /** 
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions() 
       */ 
      public String[] getFileExtensions() 
      { 
        return new String[] {"*.pdf"}; 
      } 
    }; 
    return new IOFormat[] { f }; 
  } 

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor, de.willuhn.jameica.system.BackgroundTask)
   */
  @Override 
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor, BackgroundTask bgt) throws RemoteException, ApplicationException
  { 
    monitor.setStatusText(i18n.tr("Importiere ZUGFeRD-Datei"));
    monitor.setPercentComplete(20);

    final AuslandsUeberweisung u = Settings.getDBService().createObject(AuslandsUeberweisung.class,null); 
     
    final PdfHandler handler = new PdfHandler();
    final Invoice invoice = handler.extractInvoice(is);
    
    if (invoice == null)
      throw new ApplicationException(i18n.tr("Datei enthält keine ZUGFeRD-konforme Rechnung"));
    
    final Header header = invoice.getHeader();
    
    final Trade trade = invoice.getTrade();
    if (trade == null)
      throw new ApplicationException(i18n.tr("Angaben zum Inhalt fehlen in der Rechnung"));
    
    final Settlement s = trade.getSettlement();
    if (s == null)
      throw new ApplicationException(i18n.tr("Angaben zur Zahlung fehlen in der Rechnung"));
    
    final MonetarySummation sum = s.getMonetarySummation();
    if (sum == null)
      throw new ApplicationException(i18n.tr("Angaben zum Betrag fehlen in der Rechnung"));
    
    final Amount am = sum.getGrandTotal();
    if (am == null)
      throw new ApplicationException(i18n.tr("Angaben zur Summe fehlen in der Rechnung"));
    
    List<PaymentMeans> pay = s.getPaymentMeans();
    PaymentMeans p = pay != null && pay.size() > 0 ? pay.get(0) : null;
    CreditorFinancialAccount acc = p != null ? p.getPayeeAccount() : null;

    monitor.setPercentComplete(40);

    u.setBetrag(am.getValue().doubleValue());
    
    String nr = s.getPaymentReference();
    if (nr == null)
      nr = header != null ? header.getInvoiceNumber() : null;
    
    u.setEndtoEndId(nr);
    u.setPmtInfId(nr);
    u.setZweck(nr);

    String name = acc != null ? StringUtils.trimToNull(acc.getAccountName()) : null;
    if (name == null)
    {
      final TradeParty seller = trade.getAgreement() != null ? trade.getAgreement().getSeller() : null;
      if (seller != null)
        name = seller.getName();
    }

    u.setGegenkontoName(name); 
    
    if (acc != null)
      u.setGegenkontoNummer(StringUtils.trimToEmpty(acc.getIban()).replace(" ", ""));
    
    FinancialInstitution inst = p != null ? p.getPayeeInstitution() : null;
    if (inst != null)
      u.setGegenkontoBLZ(StringUtils.trimToEmpty(inst.getBic()).replace(" ", ""));
    
    Date termin = header != null ? header.getContractualDueDate() : null;
    if (termin == null)
    {
      // Checken, ob wir stattdessen ein Rechnungsdatum haben. Wenn ja, nehmen wir das und legen
      // 1 Woche drauf
      Date issued = header != null ? header.getIssued() : null;
      if (issued != null)
      {
        Calendar cal = Calendar.getInstance();
        cal.setTime(issued);
        cal.add(Calendar.DATE,7);
        termin = cal.getTime();
      }
    }
    u.setTermin(termin != null ? termin : new Date());
    
    monitor.setStatus(ProgressMonitor.STATUS_DONE);
    monitor.setPercentComplete(100);
    monitor.setStatusText(i18n.tr("SEPA-Überweisung erstellt"));

    // Wir speichern den Auftrag nicht direkt sondern oeffnen ihn nur zur Bearbeitung.
    // Denn wir koennen nicht garantieren, dass alle noetigen Informationen enthalten sind, um den Auftrag speichern zu koennen.
    // Ausserdem haben wir gar kein Konto ausgewaehlt.
    new Open().handleAction(u);
  } 
}


