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

import org.apache.commons.lang.StringUtils;

import de.jost_net.OBanToo.SEPA.IBAN;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.io.ClipboardSepaUeberweisungImporter;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisungBuchung;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action zum Erstellen einer neuen Auslandsueberweisung.
 */
public class AuslandsUeberweisungNew implements Action
{
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    AuslandsUeberweisung u = null;

    try
    {
      if (context instanceof AuslandsUeberweisung)
      {
        u = (AuslandsUeberweisung) context;
      }
      else if (context instanceof Konto)
      {
        Konto k = (Konto) context;
        u = (AuslandsUeberweisung) Settings.getDBService().createObject(AuslandsUeberweisung.class,null);
        if (!k.hasFlag(Konto.FLAG_DISABLED) && !k.hasFlag(Konto.FLAG_OFFLINE) && StringUtils.trimToNull(k.getIban()) != null)
          u.setKonto(k);
      }
      else if (context instanceof Address)
      {
        Address e = (Address) context;
        u = (AuslandsUeberweisung) Settings.getDBService().createObject(AuslandsUeberweisung.class,null);
        u.setGegenkonto(e);
      }
      else if (context instanceof Umsatz)
      {
        Umsatz umsatz = (Umsatz) context;
        u = (AuslandsUeberweisung) Settings.getDBService().createObject(AuslandsUeberweisung.class,null);
        u.setBetrag(Math.abs(umsatz.getBetrag())); // negative Betraege automatisch in positive umwandeln
        u.setGegenkontoName(umsatz.getGegenkontoName());
        u.setKonto(umsatz.getKonto());
        u.setTermin(new Date());

        // BUGZILLA 1437
        // Wenn wir ein Gegenkonto haben, dann pruefen wir, ob es wie eine IBAN aussieht.
        // Falls ja, uebernehmen wir sie. Falls nicht, schauen wir im Adressbuch, ob
        // wir die Adresse kennen und dort vielleicht BIC und IBAN haben
        String kto = StringUtils.trimToEmpty(umsatz.getGegenkontoNummer());
        String blz = StringUtils.trimToEmpty(umsatz.getGegenkontoBLZ());
        if (kto.length() <= 10 && kto.length() > 0 && blz.length() > 0) // aber nur, wenn wir auch was zum Suchen im Adressbuch haben
        {
          // kann keine IBAN sein. Die ist per Definition laenger
          
          // Also im Adressbuch schauen
          HibiscusAddress address = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
          address.setBlz(blz);
          address.setKontonummer(kto);
          AddressbookService book = (AddressbookService) Application.getServiceFactory().lookup(HBCI.class,"addressbook");
          Address a = book.contains(address);
          kto = a != null ? a.getIban() : null;
          blz = a != null ? a.getBic() : null;
        }
        
        
        // BUGZILLA 1835 - BIC nur uebernehmen, wenn es wirklich eine ist
        // Ansonsten ermitteln wir die BIC aus der IBAN
        if (blz == null || blz.length() != HBCIProperties.HBCI_BIC_MAXLENGTH)
        {
          IBAN iban = HBCIProperties.getIBAN(kto);
          if (iban != null)
            blz = iban.getBIC();
        }
        u.setGegenkontoBLZ(blz);
        u.setGegenkontoNummer(kto);

        
        // die weiteren Verwendungszweck-Zeilen gibts bei SEPA-Ueberweisungen nicht.
        // Daher landen die alle in einer Zeile
        u.setZweck(VerwendungszweckUtil.toString(umsatz));
      }
      else if (context instanceof SepaSammelUeberweisungBuchung)
      {
        try
        {
          SepaSammelUeberweisungBuchung b = (SepaSammelUeberweisungBuchung) context;
          SepaSammelUeberweisung st = (SepaSammelUeberweisung) b.getSammelTransfer();
          u = (AuslandsUeberweisung) Settings.getDBService().createObject(AuslandsUeberweisung.class,null);
          u.setBetrag(b.getBetrag());
          u.setGegenkontoBLZ(b.getGegenkontoBLZ());
          u.setGegenkontoName(b.getGegenkontoName());
          u.setGegenkontoNummer(b.getGegenkontoNummer());
          u.setZweck(b.getZweck());
          u.setEndtoEndId(b.getEndtoEndId());
          
          if (st != null)
          {
            u.setKonto(st.getKonto());
            u.setTermin(st.getTermin());
          }
        }
        catch (RemoteException re)
        {
          Logger.error("error while creating transfer",re);
          // Dann halt nicht
        }
      }
      else 
      {
        ClipboardSepaUeberweisungImporter i = new ClipboardSepaUeberweisungImporter();
        u = i.getUeberweisung();
      }

      // Bei neu angelegten Aufträgen per Default als Echtzeitüberweisung anlegen - nicht aber bei duplizierten Aufträgen
      if (u.isNewObject() && MetaKey.DUPLICATE_ID.get(u) == null)
        u.setInstantPayment(true);

    }
    catch (ApplicationException | OperationCanceledException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      Logger.error("error while creating transfer",e);
    }
    
    GUI.startView(de.willuhn.jameica.hbci.gui.views.AuslandsUeberweisungNew.class,u);
  }

}

