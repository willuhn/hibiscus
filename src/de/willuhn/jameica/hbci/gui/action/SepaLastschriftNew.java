/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/AuslandsUeberweisungNew.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/03/13 00:25:12 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Erstellen einer neuen SEPA-Lastschrift.
 */
public class SepaLastschriftNew implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    SepaLastschrift u = null;

    try
    {
      if (context instanceof SepaLastschrift)
      {
        u = (SepaLastschrift) context;
      }
      else if (context instanceof Konto)
      {
        Konto k = (Konto) context;
        u = (SepaLastschrift) Settings.getDBService().createObject(SepaLastschrift.class,null);
        if (!k.hasFlag(Konto.FLAG_DISABLED) && !k.hasFlag(Konto.FLAG_OFFLINE) && StringUtils.trimToNull(k.getIban()) != null)
          u.setKonto(k);
      }
      else if (context instanceof Address)
      {
        Address e = (Address) context;
        u = (SepaLastschrift) Settings.getDBService().createObject(SepaLastschrift.class,null);
        u.setGegenkonto(e);
      }
      else if (context instanceof Umsatz)
      {
        Umsatz umsatz = (Umsatz) context;
        u = (SepaLastschrift) Settings.getDBService().createObject(SepaLastschrift.class,null);
        u.setBetrag(Math.abs(umsatz.getBetrag())); // negative Betraege automatisch in positive umwandeln
        u.setGegenkontoBLZ(umsatz.getGegenkontoBLZ());
        u.setGegenkontoName(umsatz.getGegenkontoName());
        u.setGegenkontoNummer(umsatz.getGegenkontoNummer());
        u.setKonto(umsatz.getKonto());
        u.setTermin(new Date());
        
        // die weiteren Verwendungszweck-Zeilen gibts bei SEPA-Ueberweisungen nicht.
        // Daher landen die alle in einer Zeile
        u.setZweck(VerwendungszweckUtil.toString(umsatz));
      }
      else if (context instanceof SepaSammelLastBuchung)
      {
        try
        {
         u=cloneFromSammelLastBuchung((SepaSammelLastBuchung)context);
        }
        catch (RemoteException re)
        {
          Logger.error("error while creating transfer",re);
          // Dann halt nicht
        }
      }
      
      
    }
    catch (RemoteException e)
    {
      Logger.error("error while creating transfer",e);
      // Dann halt nicht
    }

    GUI.startView(de.willuhn.jameica.hbci.gui.views.SepaLastschriftNew.class,u);
  }

  @SuppressWarnings("javadoc")
  public static SepaLastschrift cloneFromSammelLastBuchung(SepaSammelLastBuchung context) throws RemoteException{
    SepaSammelLastBuchung b = (SepaSammelLastBuchung) context;
    SepaSammelLastschrift st = (SepaSammelLastschrift) b.getSammelTransfer();
    SepaLastschrift u = (SepaLastschrift) Settings.getDBService().createObject(SepaLastschrift.class,null);
    u.setBetrag(b.getBetrag());
    u.setGegenkontoBLZ(b.getGegenkontoBLZ());
    u.setGegenkontoName(b.getGegenkontoName());
    u.setGegenkontoNummer(b.getGegenkontoNummer());
    u.setZweck(b.getZweck());
    u.setCreditorId(b.getCreditorId());
    u.setEndtoEndId(b.getEndtoEndId());
    u.setMandateId(b.getMandateId());
    u.setSignatureDate(b.getSignatureDate());
    
    if (st != null)
    {
      u.setKonto(st.getKonto());
      u.setTermin(st.getTermin());
      
      u.setSequenceType(st.getSequenceType());
      u.setTargetDate(st.getTargetDate());
      u.setType(st.getType());
    }
    return u;
  }

}
