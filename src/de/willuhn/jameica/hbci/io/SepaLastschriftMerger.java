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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.SepaTransferMergeDialog;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectDeletedMessage;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.SepaLastType;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Fasst SEPA-Lastschriften zu ein oder mehreren SEPA-Sammellastschriften zusammen.
 */
public class SepaLastschriftMerger
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Merged die Liste der Einzellastschriften zu ein oder mehreren sorten-reinen Sammellastschriften zusammen.
   * @param lastschriften die Liste der Einzellastschriften.
   * @return die Liste der erzeugten Sammel-Lastschriften. die Sammellastschriften sind bereits gespeichert.
   * Sie muessen vom Aufrufer also nicht nochmal extra gespeichert werden - es sei denn, der Aufrufer moechte
   * nochmal Aenderungen daran vornehmen.
   * @throws ApplicationException
   */
  public List<SepaSammelLastschrift> merge(List<SepaLastschrift> lastschriften) throws ApplicationException
  {
    if (lastschriften == null || lastschriften.size() == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Aufträge aus"));
    
    List<SepaSammelLastschrift> result = new ArrayList<SepaSammelLastschrift>();
    SepaSammelLastschrift tx = null;
    
    try
    {
      HBCIDBService service = Settings.getDBService();
      Map<String,SepaSammelLastschrift> map = new HashMap<String,SepaSammelLastschrift>();
      boolean inDb = false;
      ////////////////////////////////////////
      // 1. Iterieren ueber die Auftraege, um herauszufinden, wieviele Sammel-Auftraege es werden
      for (SepaLastschrift l:lastschriften)
      {
        inDb |= !l.isNewObject();
        String key = this.createKey(l);
        SepaSammelLastschrift s = map.get(key);
        if (s == null)
        {
          s = (SepaSammelLastschrift) service.createObject(SepaSammelLastschrift.class,null);
          s.setKonto(l.getKonto());
          s.setBezeichnung(i18n.tr("{0} {1} vom {2}",l.getSequenceType().getDescription(),l.getType().getDescription(), HBCI.LONGDATEFORMAT.format(new Date())));
          s.setSequenceType(l.getSequenceType());
          s.setType(l.getType());
          s.setTargetDate(l.getTargetDate());
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
      Iterator<SepaSammelLastschrift> list = map.values().iterator();
      while (list.hasNext())
      {
        SepaSammelLastschrift s = list.next();
        
        if (tx == null)
        {
          tx = s;
          tx.transactionBegin();
        }
        
        s.store();
        Application.getMessagingFactory().sendMessage(new ImportMessage(s));
        result.add(s);
      }
      
      // jetzt iterieren wir nochmal ueber die Einzelauftraege und ordnen sie den
      // Sammelauftraegen zu
      for (SepaLastschrift l:lastschriften)
      {
        String key = this.createKey(l);
        SepaSammelLastschrift s = map.get(key);
        
        
        if (s == null) // WTF?
        {
          Logger.error("unable to find sepa transfer for key " + key);
          continue;
        }
        
        SepaSammelLastBuchung b = s.createBuchung();
        b.setBetrag(l.getBetrag());
        b.setCreditorId(l.getCreditorId());
        b.setEndtoEndId(l.getEndtoEndId());
        b.setGegenkontoBLZ(l.getGegenkontoBLZ());
        b.setGegenkontoName(l.getGegenkontoName());
        b.setGegenkontoNummer(l.getGegenkontoNummer());
        b.setMandateId(l.getMandateId());
        b.setSignatureDate(l.getSignatureDate());
        b.setZweck(l.getZweck());
        b.store();
        Application.getMessagingFactory().sendMessage(new ImportMessage(b));
        Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(s));
        
        if (delete && !l.isNewObject())
        {
          final String id = l.getID();
          l.delete();
          Application.getMessagingFactory().sendMessage(new ObjectDeletedMessage(l,id));
        }
      }
      
      tx.transactionCommit();
      
      return result;
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
      throw new ApplicationException(i18n.tr("Zusammenfassen der Lastschriften fehlgeschlagen: {0}",e.getMessage()));
		}
  }
  
  /**
   * Generiert einen Lookup-Key fuer den Auftrag, um ihn dem Sammelauftrag zuzuordnen.
   * @param l der Auftrag.
   * @return der Lookup-Key.
   * @throws RemoteException
   */
  private String createKey(SepaLastschrift l) throws RemoteException
  {
    StringBuffer sb = new StringBuffer();
    sb.append(l.getKonto().getID() + "-");
    sb.append(l.getSequenceType().name() + "-");
    
    SepaLastType type = l.getType();
    if (type == null)
      type = SepaLastType.DEFAULT;
    sb.append(type.name() + "-");
    
    Date target = l.getTargetDate();
    if (target != null)
      sb.append(HBCI.DATEFORMAT.format(target) + "-");
    
    return sb.toString();
  }
}
