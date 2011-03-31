/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UeberweisungImpl.java,v $
 * $Revision: 1.45 $
 * $Date: 2011/03/31 16:30:58 $
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
        // TODO: Siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=74505#74505
        // Nur zum Testen. Morgen wieder einkommentieren
//        String dest = getGegenkontoBLZ();
//        String src  = getKonto().getBLZ();
//        if (!dest.equals(src))
//          throw new ApplicationException(i18n.tr("Umbuchungen sind nur zu einem Konto bei Ihrer eigenen Bank möglich"));
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
 * Revision 1.45  2011/03/31 16:30:58  willuhn
 * @N BLZ-Check bei Umbuchungen testhalber mal deaktiviert
 *
 * Revision 1.44  2010/04/27 11:02:32  willuhn
 * @R Veralteten Verwendungszweck-Code entfernt
 *
 * Revision 1.43  2010/03/04 09:39:40  willuhn
 * @B BUGZILLA 829
 *
 * Revision 1.42  2009/05/12 22:53:33  willuhn
 * @N BUGZILLA 189 - Ueberweisung als Umbuchung
 *
 * Revision 1.41  2008/08/01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.40  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 *
 * Revision 1.39  2007/12/04 11:24:38  willuhn
 * @B Bug 509
 *
 * Revision 1.38  2007/11/30 18:37:08  willuhn
 * @B Bug 509
 *
 * Revision 1.37  2007/03/05 09:49:53  willuhn
 * @B Bug 370
 **********************************************************************/