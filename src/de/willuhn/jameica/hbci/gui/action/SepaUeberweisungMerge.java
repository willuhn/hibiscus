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

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.SepaTransferMergeDialog;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectDeletedMessage;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisungBuchung;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Zusammenfassen von SEPA-Ueberweisungen zu ein oder mehreren SEPA-Sammelueberweisungen.
 */
public class SepaUeberweisungMerge implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof AuslandsUeberweisung) && !(context instanceof AuslandsUeberweisung[]))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Aufträge aus"));

    AuslandsUeberweisung[] source = null;
    
    if (context instanceof AuslandsUeberweisung)
      source = new AuslandsUeberweisung[]{(AuslandsUeberweisung) context};
    else
      source = (AuslandsUeberweisung[]) context;
    
    if (source.length == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Aufträge aus"));

    
    SepaSammelUeberweisung tx = null;
    
    try
    {
      HBCIDBService service = Settings.getDBService();
      Map<String,SepaSammelUeberweisung> map = new HashMap<String,SepaSammelUeberweisung>();
      boolean inDb = false;
      ////////////////////////////////////////
      // 1. Iterieren ueber die Auftraege, um herauszufinden, wieviele Sammel-Auftraege es werden
      for (AuslandsUeberweisung l:source)
      {
        inDb |= !l.isNewObject();
        String key = this.createKey(l);
        SepaSammelUeberweisung s = map.get(key);
        if (s == null)
        {
          s = (SepaSammelUeberweisung) service.createObject(SepaSammelUeberweisung.class,null);
          s.setKonto(l.getKonto());
          s.setBezeichnung(i18n.tr("SEPA-Sammelüberweisung vom {0}",HBCI.LONGDATEFORMAT.format(new Date())));
          map.put(key,s);
        }
      }
      
      // Abfrage anzeigen, ob die Einzelauftraege geloescht werden sollen
      // a) wenn mindestens einer in der DB existierte
      // b) oder mehr als ein Sammelauftrag entsteht.
      boolean delete = false;
      int count = map.size();
      if (count > 1 || inDb)
      {
        SepaTransferMergeDialog dialog = new SepaTransferMergeDialog(SepaTransferMergeDialog.POSITION_CENTER,count,inDb);
        Object o = dialog.open();
        if (o != null)
          delete = ((Boolean)o).booleanValue();
      }
      
      // OK, wir duerfen weiter machen. Erstmal die Sammelauftraege anlegen
      for (SepaSammelUeberweisung s : map.values())
      {
        if (tx == null)
        {
          tx = s;
          tx.transactionBegin();
        }
        
        s.store();
        Application.getMessagingFactory().sendMessage(new ImportMessage(s));
      }
      
      // jetzt iterieren wir nochmal ueber die Einzelauftraege und ordnen sie den
      // Sammelauftraegen zu
      for (AuslandsUeberweisung l:source)
      {
        String key = this.createKey(l);
        SepaSammelUeberweisung s = map.get(key);
        
        
        if (s == null) // WTF?
        {
          Logger.error("unable to find sepa transfer for key " + key);
          continue;
        }
        
        SepaSammelUeberweisungBuchung b = s.createBuchung();
        b.setBetrag(l.getBetrag());
        b.setEndtoEndId(l.getEndtoEndId());
        b.setGegenkontoBLZ(l.getGegenkontoBLZ());
        b.setGegenkontoName(l.getGegenkontoName());
        b.setGegenkontoNummer(l.getGegenkontoNummer());
        b.setZweck(l.getZweck());
        b.store();
        Application.getMessagingFactory().sendMessage(new ImportMessage(b));
        Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(s));
        
        if (delete && !l.isNewObject())
        {
          l.delete();
          Application.getMessagingFactory().sendMessage(new ObjectDeletedMessage(l));
        }
      }
      
      tx.transactionCommit();

      if (count > 1)
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("{0} Sammelaufträge erzeugt",String.valueOf(count)), StatusBarMessage.TYPE_SUCCESS));
      else
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Sammelauftrag erzeugt"), StatusBarMessage.TYPE_SUCCESS));
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
      throw new ApplicationException(i18n.tr("Zusammenfassen der Überweisungen fehlgeschlagen: {0}",e.getMessage()));
		}
  }
  
  /**
   * Generiert einen Lookup-Key fuer den Auftrag, um ihn dem Sammelauftrag zuzuordnen.
   * @param l der Auftrag.
   * @return der Lookup-Key.
   * @throws RemoteException
   */
  private String createKey(AuslandsUeberweisung l) throws RemoteException
  {
    return l.getKonto().getID();
  }
}
