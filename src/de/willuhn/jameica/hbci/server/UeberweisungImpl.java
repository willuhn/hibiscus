/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UeberweisungImpl.java,v $
 * $Revision: 1.49 $
 * $Date: 2011/05/12 08:08:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.TextSchluessel;
import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Eine Ueberweisung.
 */
public class UeberweisungImpl extends AbstractBaseUeberweisungImpl implements Ueberweisung
{
  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @throws RemoteException
   */
  public UeberweisungImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "ueberweisung";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Duplicatable#duplicate()
   */
  public Duplicatable duplicate() throws RemoteException {
    Ueberweisung u = (Ueberweisung) getService().createObject(Ueberweisung.class,null);
    u.setBetrag(getBetrag());
    u.setGegenkontoBLZ(getGegenkontoBLZ());
    u.setGegenkontoNummer(getGegenkontoNummer());
    u.setGegenkontoName(getGegenkontoName());
    u.setKonto(getKonto());
    u.setZweck(getZweck());
    u.setZweck2(getZweck2());
    u.setWeitereVerwendungszwecke(getWeitereVerwendungszwecke());
    u.setTextSchluessel(getTextSchluessel());
    
    u.setTermin(isTerminUeberweisung() ? getTermin() : new Date());
    u.setTerminUeberweisung(isTerminUeberweisung());
    u.setUmbuchung(isUmbuchung());
    return u;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractHibiscusTransferImpl#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    super.insertCheck();
    
    // Wir checken noch die Plausi fuer Umbuchungen
    try
    {
      if (isUmbuchung())
      {
        // Sollte in der GUI gar nicht moeglich sein, da beide Werte in einer
        // Combo-Box stehen und da nur eines von beiden ausgewaehlt werden kann
        if (isTerminUeberweisung())
        {
          Logger.error("SUSPECT: the gui should block both - \"umbuchung\" and \"terminueberweisung\"");
          throw new ApplicationException(i18n.tr("Eine Umbuchung kann nicht als Termin-Auftrag gesendet werden"));
        }
        
        // Checken, ob Ziel-BLZ identisch mit Quell-BLZ
        // NULL-Checks brauchen wir hier nicht - das ist bereits in super.insertCheck() gemacht worden
        String dest = getGegenkontoBLZ();
        String src  = getKonto().getBLZ();
        if (!dest.equals(src))
          throw new ApplicationException(i18n.tr("Umbuchungen sind nur zu einem Konto bei Ihrer eigenen Bank möglich"));
      }
      
      String key = this.getTextSchluessel();
      if (key != null && key.equals(TextSchluessel.TS_BZU))
      {
        // Verwendungszweck-Zeile 1 darf nur 13 Zeichen lang sein und nur aus Zahlen bestehen
        String usage = this.getZweck();
        if (usage == null || usage.length() == 0)
          throw new ApplicationException(i18n.tr("Bitte geben Sie die {0}-stellige BZÜ-Prüfziffer ein.",Integer.toString(HBCIProperties.HBCI_TRANSFER_BZU_LENGTH)));
        if (!usage.matches("^[" + HBCIProperties.HBCI_BZU_VALIDCHARS + "]{" + HBCIProperties.HBCI_TRANSFER_BZU_LENGTH + "}$"))
          throw new ApplicationException(i18n.tr("Die BZÜ-Prüfziffer muss exakt {0} Ziffern enthalten",Integer.toString(HBCIProperties.HBCI_TRANSFER_BZU_LENGTH)));
      }
      else if (key != null && key.equals(TextSchluessel.TS_SPENDE))
      {
        // Laut HBCI-Spec sollte bei Spenden-Ueberweisung keine Pruefung der Gegenkonto-Checksumme erfolgen. Warum eigentlich?
        // Die ersten 3 Zeilen Verwendungszweck muessen gefuellt sein
        String usage  = this.getZweck();
        String usage2 = this.getZweck2();
        String[] wvz  = this.getWeitereVerwendungszwecke();
        if (usage == null || usage.trim().length() == 0)
          throw new ApplicationException(i18n.tr("Bitte geben Sie die Spenden-/Mitgliedsnummer oder den Namen des Spenders ein"));
        if (usage2 == null || usage2.trim().length() == 0)
          throw new ApplicationException(i18n.tr("Bitte geben Sie Postleitzahl und Strasse des Spenders ein"));
        if (wvz == null || wvz.length == 0)
          throw new ApplicationException(i18n.tr("Bitte geben Sie Name und Ort des Kontoinhabers/Einzahlers ein"));
      }
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking transfer",e);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen des Auftrages."));
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#isTerminUeberweisung()
   */
  public boolean isTerminUeberweisung() throws RemoteException
  {
    Integer i = (Integer) getAttribute("banktermin");
    return i != null && i.intValue() == 1;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setTerminUeberweisung(boolean)
   */
  public void setTerminUeberweisung(boolean termin) throws RemoteException
  {
    setAttribute("banktermin",termin ? new Integer(1) : null);
  }

  /**
   * @see de.willuhn.jameica.hbci.server.AbstractBaseUeberweisungImpl#ueberfaellig()
   */
  public boolean ueberfaellig() throws RemoteException
  {
    // BUGZILLA 370 Termin-Ueberweisungen gelten sofort als faellig
    if (isTerminUeberweisung())
      return !ausgefuehrt();
    
    return super.ueberfaellig();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#isUmbuchung()
   */
  public boolean isUmbuchung() throws RemoteException
  {
    Integer i = (Integer) getAttribute("umbuchung");
    return i != null && i.intValue() == 1;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Ueberweisung#setUmbuchung(boolean)
   */
  public void setUmbuchung(boolean b) throws RemoteException
  {
    setAttribute("umbuchung",b ? new Integer(1) : null);
  }
}


/**********************************************************************
 * $Log: UeberweisungImpl.java,v $
 * Revision 1.49  2011/05/12 08:08:27  willuhn
 * @N BUGZILLA 591
 *
 * Revision 1.48  2011-04-06 08:19:19  willuhn
 * @R UNDO
 **********************************************************************/