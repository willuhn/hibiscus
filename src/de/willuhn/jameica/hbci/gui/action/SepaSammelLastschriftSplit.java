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

import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.SepaSammelTransferSplitDialog;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.ObjectDeletedMessage;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Zerlegen von SEPA-Sammellastschriften zu mehreren SEPA-Einzellastschriften.
 */
public class SepaSammelLastschriftSplit implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof SepaSammelLastschrift) && !(context instanceof SepaSammelLastschrift[]))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Sammelaufträge aus"));

    SepaSammelLastschrift[] source = null;
    
    if (context instanceof SepaSammelLastschrift)
      source = new SepaSammelLastschrift[]{(SepaSammelLastschrift) context};
    else
      source = (SepaSammelLastschrift[]) context;
    
    if (source.length == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Aufträge aus"));

    
    // Handler fuer die Transaktion
    SepaLastschrift tx = null;
    
    // Die Liste der Buchungen
    List<SepaSammelLastBuchung> buchungen = new ArrayList<SepaSammelLastBuchung>();
        
    try
    {
      HBCIDBService service = Settings.getDBService();
      boolean inDb = false;
      ////////////////////////////////////////
      // 1. Iterieren ueber die Auftraege, um herauszufinden, wieviele Einzel-Auftraege es werden
      for (SepaSammelLastschrift l:source)
      {
        inDb |= !l.isNewObject();
        List<SepaSammelLastBuchung> b = l.getBuchungen();
        buchungen.addAll(b);
      }
      
      // Abfrage anzeigen, ob die Einzelauftraege geloescht werden sollen wenn mindestens einer in der DB existierte
      boolean delete = false;
      if (inDb)
      {
        SepaSammelTransferSplitDialog dialog = new SepaSammelTransferSplitDialog(SepaSammelTransferSplitDialog.POSITION_CENTER,buchungen.size(),inDb);
        Object o = dialog.open();
        if (o != null)
          delete = ((Boolean)o).booleanValue();
      }
      
      int count = 0;
      
      // OK, wir duerfen weiter machen
      for (SepaSammelLastBuchung b:buchungen)
      {
        SepaSammelLastschrift st = b.getSammelTransfer();
        
        SepaLastschrift u = (SepaLastschrift) service.createObject(SepaLastschrift.class,null);
        
        if (tx == null)
        {
          tx = u;
          tx.transactionBegin();
        }

        u.setKonto(st.getKonto());
        u.setBetrag(b.getBetrag());
        u.setGegenkontoBLZ(b.getGegenkontoBLZ());
        u.setGegenkontoName(b.getGegenkontoName());
        u.setGegenkontoNummer(b.getGegenkontoNummer());
        u.setZweck(b.getZweck());
        u.setEndtoEndId(b.getEndtoEndId());
        u.setPurposeCode(b.getPurposeCode());
        u.setTermin(st.getTermin());
        u.setCreditorId(b.getCreditorId());
        u.setEndtoEndId(b.getEndtoEndId());
        u.setMandateId(b.getMandateId());
        u.setSignatureDate(b.getSignatureDate());
        u.setTermin(st.getTermin());
        u.setSequenceType(st.getSequenceType());
        u.setTargetDate(st.getTargetDate());
        u.setType(st.getType());

        u.store();
        
        Application.getMessagingFactory().sendMessage(new ImportMessage(u));
        count++;
      }
      
      // Jetzt noch die Sammellastschriften loeschen
      if (delete)
      {
        for (SepaSammelLastschrift s:source)
        {
          if (s.isNewObject())
            continue;

          final String id = s.getID();
          s.delete();
          Application.getMessagingFactory().sendMessage(new ObjectDeletedMessage(s,id));
        }
      }

      if (tx != null)
        tx.transactionCommit();

      if (count > 1)
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("{0} Einzelaufträge erzeugt",String.valueOf(count)), StatusBarMessage.TYPE_SUCCESS));
      else
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Einzelauftrag erzeugt"), StatusBarMessage.TYPE_SUCCESS));
		}
		catch (Exception e)
		{
		  if (tx != null)
		  {
        try
        {
          tx.transactionRollback();
        }
        catch (Exception e2)
        {
          Logger.error("unable to rollback transaction",e);
        }
		  }
		  
		  if (e instanceof OperationCanceledException)
		    throw (OperationCanceledException) e;
		  
		  if (e instanceof ApplicationException)
		    throw (ApplicationException) e;
		  
      Logger.error("error while merging jobs",e);
      throw new ApplicationException(i18n.tr("Teilen der Sammellastschriften fehlgeschlagen: {0}",e.getMessage()));
		}
  }
}
