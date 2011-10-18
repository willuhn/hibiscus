/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/AbstractSammelTransferBuchungImpl.java,v $
 * $Revision: 1.22 $
 * $Date: 2011/10/18 09:28:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Duplicatable;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung einer einzelnen Buchung eines Sammel-Auftrages.
 * @author willuhn
 */
public abstract class AbstractSammelTransferBuchungImpl extends AbstractHibiscusDBObject implements SammelTransferBuchung, Duplicatable
{

  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @throws java.rmi.RemoteException
   */
  public AbstractSammelTransferBuchungImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "zweck";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException
  {
    try {
      if (getSammelTransfer() == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie den zugehörigen Sammel-Auftrag aus."));

      double betrag = getBetrag();
      if (betrag == 0.0 || Double.isNaN(betrag))
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen gültigen Betrag ein."));

      if (getGegenkontoNummer() == null || getGegenkontoNummer().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie die Kontonummer des Gegenkontos ein"));
      
      if (getGegenkontoBLZ() == null || getGegenkontoBLZ().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie die BLZ des Gegenkontos ein"));

      // BUGZILLA 280
      HBCIProperties.checkChars(getGegenkontoNummer(), HBCIProperties.HBCI_KTO_VALIDCHARS);
      HBCIProperties.checkChars(getGegenkontoBLZ(), HBCIProperties.HBCI_BLZ_VALIDCHARS);
      HBCIProperties.checkLength(getGegenkontoNummer(), HBCIProperties.HBCI_KTO_MAXLENGTH_HARD);

      if (getGegenkontoName() == null || getGegenkontoName().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie den Namen des Kontoinhabers des Gegenkontos ein"));

      int blzLen = getGegenkontoBLZ().length();
      if (blzLen != HBCIProperties.HBCI_BLZ_LENGTH)
        throw new ApplicationException(i18n.tr("Ungültige BLZ \"{0}\". Muss {1} Stellen lang sein.", new String[]{getGegenkontoBLZ(),""+HBCIProperties.HBCI_BLZ_LENGTH}));

      HBCIProperties.checkLength(getGegenkontoName(), HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);

      // BUGZILLA 163
      HBCIProperties.checkChars(getGegenkontoName(), HBCIProperties.HBCI_DTAUS_VALIDCHARS);

      if (!HBCIProperties.checkAccountCRC(getGegenkontoBLZ(),getGegenkontoNummer()))
        throw new ApplicationException(i18n.tr("Ungültige BLZ/Kontonummer. Bitte prüfen Sie Ihre Eingaben."));
        
      if (getZweck() == null || "".equals(getZweck()))
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen Verwendungszweck ein"));

      HBCIProperties.checkLength(getZweck(), HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
      HBCIProperties.checkLength(getZweck2(), HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);

      HBCIProperties.checkChars(getZweck(), HBCIProperties.HBCI_DTAUS_VALIDCHARS);
      HBCIProperties.checkChars(getZweck2(), HBCIProperties.HBCI_DTAUS_VALIDCHARS);
      
      VerwendungszweckUtil.checkMaxUsage(this);
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking sammeltransferbuchung",e);
      throw new ApplicationException(i18n.tr("Fehler beim Prüfen der Buchung."));
    }
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException
  {
    insertCheck();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoNummer()
   */
  public String getGegenkontoNummer() throws RemoteException
  {
    return (String) getAttribute("gegenkonto_nr");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoBLZ()
   */
  public String getGegenkontoBLZ() throws RemoteException
  {
    return (String) getAttribute("gegenkonto_blz");
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
    if ("this".equals(arg0))
      return this;
    return super.getAttribute(arg0);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoName()
   */
  public String getGegenkontoName() throws RemoteException
  {
    return (String) getAttribute("gegenkonto_name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setGegenkontoNummer(java.lang.String)
   */
  public void setGegenkontoNummer(String kontonummer) throws RemoteException
  {
    setAttribute("gegenkonto_nr",kontonummer);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setGegenkontoBLZ(java.lang.String)
   */
  public void setGegenkontoBLZ(String blz) throws RemoteException
  {
    setAttribute("gegenkonto_blz",blz);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setGegenkontoName(java.lang.String)
   */
  public void setGegenkontoName(String name) throws RemoteException
  {
    setAttribute("gegenkonto_name",name);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getBetrag()
   */
  public double getBetrag() throws RemoteException
  {
    Double d = (Double) getAttribute("betrag");
    if (d == null)
      return 0;
    return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getZweck()
   */
  public String getZweck() throws RemoteException
  {
    return (String) getAttribute("zweck");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getZweck2()
   */
  public String getZweck2() throws RemoteException
  {
    return (String) getAttribute("zweck2");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setBetrag(double)
   */
  public void setBetrag(double betrag) throws RemoteException
  {
    setAttribute("betrag", new Double(betrag));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setZweck(java.lang.String)
   */
  public void setZweck(String zweck) throws RemoteException
  {
    setAttribute("zweck",zweck);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setZweck2(java.lang.String)
   */
  public void setZweck2(String zweck2) throws RemoteException
  {
    setAttribute("zweck2",zweck2);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Duplicatable#duplicate()
   */
  public Duplicatable duplicate() throws RemoteException
  {
    SammelTransferBuchung b = (SammelTransferBuchung) getService().createObject(this.getClass(),null);
    b.setBetrag(getBetrag());
    b.setGegenkontoBLZ(getGegenkontoBLZ());
    b.setGegenkontoNummer(getGegenkontoNummer());
    b.setGegenkontoName(getGegenkontoName());
    b.setSammelTransfer(getSammelTransfer());
    b.setZweck(getZweck());
    b.setZweck2(getZweck2());
    b.setWeitereVerwendungszwecke(getWeitereVerwendungszwecke());
    b.setTextSchluessel(getTextSchluessel());
    return (Duplicatable) b;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#getTextSchluessel()
   */
  public String getTextSchluessel() throws RemoteException
  {
    return (String) getAttribute("typ");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setTextSchluessel(java.lang.String)
   */
  public void setTextSchluessel(String schluessel) throws RemoteException
  {
    setAttribute("typ",schluessel);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getWeitereVerwendungszwecke()
   */
  public String[] getWeitereVerwendungszwecke() throws RemoteException
  {
    return VerwendungszweckUtil.split((String)this.getAttribute("zweck3"));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setWeitereVerwendungszwecke(java.lang.String[])
   */
  public void setWeitereVerwendungszwecke(String[] list) throws RemoteException
  {
    setAttribute("zweck3",VerwendungszweckUtil.merge(list));
  }
}

/*****************************************************************************
 * $Log: AbstractSammelTransferBuchungImpl.java,v $
 * Revision 1.22  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 * Revision 1.21  2011-08-10 10:46:50  willuhn
 * @N Aenderungen nur an den DA-Eigenschaften zulassen, die gemaess BPD aenderbar sind
 * @R AccountUtil entfernt, Code nach VerwendungszweckUtil verschoben
 * @N Neue Abfrage-Funktion in DBPropertyUtil, um die BPD-Parameter zu Geschaeftsvorfaellen bequemer abfragen zu koennen
 *
 * Revision 1.20  2011-07-11 08:14:13  willuhn
 * @B Laengen-Pruefung der BLZ fehlte
 *
 * Revision 1.19  2010/03/04 09:39:40  willuhn
 * @B BUGZILLA 829
 *
 * Revision 1.18  2008/12/14 23:18:35  willuhn
 * @N BUGZILLA 188 - REFACTORING
 *
 * Revision 1.17  2008/12/02 10:52:23  willuhn
 * @B DecimalInput kann NULL liefern
 * @B Double.NaN beruecksichtigen
 *
 * Revision 1.16  2008/12/01 23:54:42  willuhn
 * @N BUGZILLA 188 Erweiterte Verwendungszwecke in Exports/Imports und Sammelauftraegen
 *
 * Revision 1.15  2008/11/26 00:39:36  willuhn
 * @N Erste Version erweiterter Verwendungszwecke. Muss dringend noch getestet werden.
 *
 * Revision 1.14  2008/05/19 22:35:53  willuhn
 * @N Maximale Laenge von Kontonummern konfigurierbar (Soft- und Hardlimit)
 * @N Laengenpruefungen der Kontonummer in Dialogen und Fachobjekten
 *
 * Revision 1.13  2008/04/27 22:22:56  willuhn
 * @C I18N-Referenzen statisch
 *
 * Revision 1.12  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 *
 * Revision 1.11  2007/10/14 23:26:59  willuhn
 * @N Textschluessel in Sammelauftraegen - wird noch nicht persistiert
 *
 * Revision 1.10  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.9  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 * Revision 1.8  2006/12/01 00:02:34  willuhn
 * @C made unserializable members transient
 *
 * Revision 1.7  2006/10/06 16:00:42  willuhn
 * @B Bug 280
 *
 * Revision 1.6  2006/08/23 09:45:13  willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 *
 * Revision 1.5  2006/06/26 13:25:20  willuhn
 * @N Franks eBay-Parser
 *
 * Revision 1.4  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.3  2006/05/11 10:57:35  willuhn
 * @C merged Bug 232 into HEAD
 *
 * Revision 1.2.2.1  2006/05/11 10:44:43  willuhn
 * @B bug 232
 *
 * Revision 1.2  2006/02/06 16:03:50  willuhn
 * @B bug 163
 *
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.5  2005/08/22 12:23:18  willuhn
 * @N bug 107
 *
 * Revision 1.4  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.2  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
*****************************************************************************/