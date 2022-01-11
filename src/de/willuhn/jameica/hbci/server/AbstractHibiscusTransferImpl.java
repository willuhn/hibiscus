/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil.Tag;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung von Geld-Transfers zwischen Konten.
 */
public abstract class AbstractHibiscusTransferImpl extends AbstractHibiscusDBObject implements HibiscusTransfer
{

  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @throws RemoteException
   */
  public AbstractHibiscusTransferImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
    if ("konto_id".equals(arg0))
      return getKonto();

    if ("mergedzweck".equals(arg0))
      return VerwendungszweckUtil.toString(this);

    Tag tag = Tag.byName(arg0);
    if (tag != null)
      return VerwendungszweckUtil.getTag(this,tag);

    return super.getAttribute(arg0);
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#overwrite(de.willuhn.datasource.rmi.DBObject)
   */
  public void overwrite(DBObject object) throws RemoteException
  {
    // Muessen wir ueberschreiben, weil wir fuer das Konto hier eine
    // Sonderbehandlung machen. Wuerden wir das hier nicht machen,
    // haetten wir nach dem Overwrite ploetzlich ein Konto-Objekt
    // statt der Konto-ID in den Properties. Das liegt eigentlich
    // nur daran, weil wir "konto_id" hier nicht als Foreign-Key
    // (wegen dem Cache) deklariert haben - bei "getAttribute("konto_id")"
    // aber trotzdem das Objekt (wegen KontoColumn) zurueckliefern.
    super.overwrite(object);
    
    // Jetzt ersetzen wir wieder das Konto-Objekt gegen die ID
    this.setKonto(((AbstractHibiscusTransferImpl)object).getKonto());
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
  	try {
			if (getKonto() == null)
				throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus."));
			if (getKonto().isNewObject())
				throw new ApplicationException(i18n.tr("Bitte speichern Sie zunächst das Konto"));

			if (getGegenkontoNummer() == null || getGegenkontoNummer().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie die Kontonummer des Gegenkontos ein"));
			
			if (getGegenkontoBLZ() == null || getGegenkontoBLZ().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie die BLZ des Gegenkontos ein"));

      // BUGZILLA 280
      HBCIProperties.checkChars(getGegenkontoNummer(), HBCIProperties.HBCI_KTO_VALIDCHARS);
      HBCIProperties.checkChars(getGegenkontoBLZ(), HBCIProperties.HBCI_BLZ_VALIDCHARS);
      HBCIProperties.checkLength(getGegenkontoNummer(), HBCIProperties.HBCI_KTO_MAXLENGTH_HARD);

      double betrag = getBetrag();
      if (betrag == 0.0 || Double.isNaN(betrag))
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen gültigen Betrag ein."));

      if (StringUtils.trimToNull(getGegenkontoName()) == null)
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
  		Logger.error("error while checking transfer",e);
  		throw new ApplicationException(i18n.tr("Fehler beim Prüfen des Auftrages."));
  	}
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		insertCheck();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#getKonto()
   */
  public Konto getKonto() throws RemoteException {
    Integer i = (Integer) super.getAttribute("konto_id");
    if (i == null)
      return null; // Kein Konto zugeordnet
   
    Cache cache = Cache.get(Konto.class,true);
    return (Konto) cache.get(i);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getBetrag()
   */
  public double getBetrag() throws RemoteException {
		Double d = (Double) getAttribute("betrag");
		if (d == null)
			return 0;
		return d.doubleValue();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getZweck()
   */
  public String getZweck() throws RemoteException {
    return (String) getAttribute("zweck");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getZweck2()
   */
  public String getZweck2() throws RemoteException {
		return (String) getAttribute("zweck2");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void setKonto(Konto konto) throws RemoteException {
    setAttribute("konto_id",(konto == null || konto.getID() == null) ? null : Integer.valueOf(konto.getID()));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setBetrag(double)
   */
  public void setBetrag(double betrag) throws RemoteException {
		setAttribute("betrag", Double.valueOf(betrag));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setZweck(java.lang.String)
   */
  public void setZweck(String zweck) throws RemoteException {
		setAttribute("zweck",zweck);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setZweck2(java.lang.String)
   */
  public void setZweck2(String zweck2) throws RemoteException {
		setAttribute("zweck2",zweck2);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoNummer()
   */
  public String getGegenkontoNummer() throws RemoteException {
    return (String) getAttribute("empfaenger_konto");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoBLZ()
   */
  public String getGegenkontoBLZ() throws RemoteException {
		return (String) getAttribute("empfaenger_blz");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getGegenkontoName()
   */
  public String getGegenkontoName() throws RemoteException {
		return (String) getAttribute("empfaenger_name");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkontoNummer(java.lang.String)
   */
  public void setGegenkontoNummer(String konto) throws RemoteException {
		setAttribute("empfaenger_konto",konto != null ? konto.toUpperCase() : null);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkontoBLZ(java.lang.String)
   */
  public void setGegenkontoBLZ(String blz) throws RemoteException {
		setAttribute("empfaenger_blz",blz);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkontoName(java.lang.String)
   */
  public void setGegenkontoName(String name) throws RemoteException {
		setAttribute("empfaenger_name",name);
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException
  {
    try
    {
      this.transactionBegin();

      super.delete();
      
      // und noch in's Protokoll schreiben.
      Konto k = this.getKonto();
      if (k != null)
        k.addToProtokoll(i18n.tr("Auftrag [Gegenkonto: {0}, Kto. {1}, BLZ {2}] {3} {4} gelöscht",getGegenkontoName(),getGegenkontoNummer(),getGegenkontoBLZ(),k.getWaehrung(),HBCI.DECIMALFORMAT.format(getBetrag())),Protokoll.TYP_SUCCESS);
      
      this.transactionCommit();
    }
    catch (RemoteException re)
    {
      this.transactionRollback();
      throw re;
    }
    catch (ApplicationException ae)
    {
      this.transactionRollback();
      throw ae;
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setGegenkonto(de.willuhn.jameica.hbci.rmi.Address)
   */
  public void setGegenkonto(Address e) throws RemoteException
  {
  	if (e == null)
  		return;
  	
    // Bei den Auftraegen wird generell nur noch IBAN und BIC verwendet.
  	// Kontonummer und BLZ gibt es bei denen ja nicht mehr
    this.setGegenkontoNummer(e.getIban());
    this.setGegenkontoBLZ(e.getBic());
  	this.setGegenkontoName(e.getName());
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getWeitereVerwendungszwecke()
   */
  public String[] getWeitereVerwendungszwecke() throws RemoteException
  {
    return VerwendungszweckUtil.split((String) this.getAttribute("zweck3"));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.HibiscusTransfer#setWeitereVerwendungszwecke(java.lang.String[])
   */
  public void setWeitereVerwendungszwecke(String[] list) throws RemoteException
  {
    setAttribute("zweck3",VerwendungszweckUtil.merge(list));
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#store()
   */
  public void store() throws RemoteException, ApplicationException
  {
    try
    {
      this.transactionBegin();
      super.store();
      
      Konto k = this.getKonto();
      String blz = getGegenkontoBLZ();
      if (blz != null)
      {
        String[] params = new String[] {
          getGegenkontoName(),
          getGegenkontoNummer(),
          getGegenkontoBLZ(),
          k.getWaehrung(),
          HBCI.DECIMALFORMAT.format(getBetrag())
        };
        k.addToProtokoll(i18n.tr("Auftrag [Gegenkonto: {0}, Kto. {1}, BLZ {2}] {3} {4} gespeichert",params),Protokoll.TYP_SUCCESS);
      }
      else
      {
        String[] params = new String[] {
          getGegenkontoName(),
          getGegenkontoNummer(),
          k.getWaehrung(),
          HBCI.DECIMALFORMAT.format(getBetrag())
        };
        k.addToProtokoll(i18n.tr("Auftrag [Gegenkonto: {0}, Kto. {1}] {2} {3} gespeichert",params),Protokoll.TYP_SUCCESS);
      }
      
      this.transactionCommit();
    }
    catch (RemoteException re)
    {
      try
      {
        this.transactionRollback();
      }
      catch (Exception e2)
      {
        Logger.error("unable to rollback transaction",e2);
      }
      throw re;
    }
    catch (ApplicationException ae)
    {
      try
      {
        this.transactionRollback();
      }
      catch (Exception e2)
      {
        Logger.error("unable to rollback transaction",e2);
      }
      throw ae;
    }
  }
}


/**********************************************************************
 * $Log: AbstractHibiscusTransferImpl.java,v $
 * Revision 1.22  2011/10/26 11:43:28  willuhn
 * @N In den Auftragslisten ebenfalls die weiteren Verwendungszweck-Zeilen anzeigen (so wie in der Umsatzliste bereits vorhanden)
 *
 * Revision 1.21  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 * Revision 1.20  2011/10/14 14:23:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2011-09-12 11:35:02  willuhn
 * @B "overwrite" musste ueberschrieben werden - siehe enthaltener Kommentar
 *
 * Revision 1.18  2011-08-10 10:46:50  willuhn
 * @N Aenderungen nur an den DA-Eigenschaften zulassen, die gemaess BPD aenderbar sind
 * @R AccountUtil entfernt, Code nach VerwendungszweckUtil verschoben
 * @N Neue Abfrage-Funktion in DBPropertyUtil, um die BPD-Parameter zu Geschaeftsvorfaellen bequemer abfragen zu koennen
 *
 * Revision 1.17  2010-08-30 14:25:37  willuhn
 * @B NPE, wenn Konto angegeben, jedoch ohne ID
 *
 * Revision 1.16  2010-08-27 09:24:58  willuhn
 * @B Generics-Deklaration im Cache hat javac nicht akzeptiert (der Eclipse-Compiler hats komischerweise gefressen)
 *
 * Revision 1.15  2010-08-26 12:53:08  willuhn
 * @N Cache nur befuellen, wenn das explizit gefordert wird. Andernfalls wuerde der Cache u.U. unnoetig gefuellt werden, obwohl nur ein Objekt daraus geloescht werden soll
 *
 * Revision 1.14  2010-08-26 11:31:23  willuhn
 * @N Neuer Cache. In dem werden jetzt die zugeordneten Konten von Auftraegen und Umsaetzen zwischengespeichert sowie die Umsatz-Kategorien. Das beschleunigt das Laden der Umsaetze und Auftraege teilweise erheblich
 *
 * Revision 1.13  2009/05/12 22:53:33  willuhn
 * @N BUGZILLA 189 - Ueberweisung als Umbuchung
 *
 * Revision 1.12  2009/03/17 23:44:15  willuhn
 * @N BUGZILLA 159 - Auslandsueberweisungen. Erste Version
 *
 * Revision 1.11  2008/12/14 23:18:35  willuhn
 * @N BUGZILLA 188 - REFACTORING
 *
 * Revision 1.10  2008/12/02 10:52:23  willuhn
 * @B DecimalInput kann NULL liefern
 * @B Double.NaN beruecksichtigen
 *
 * Revision 1.9  2008/11/26 00:39:36  willuhn
 * @N Erste Version erweiterter Verwendungszwecke. Muss dringend noch getestet werden.
 *
 * Revision 1.8  2008/11/17 23:30:00  willuhn
 * @C Aufrufe der depeicated BLZ-Funktionen angepasst
 *
 * Revision 1.7  2008/09/17 23:44:29  willuhn
 * @B SQL-Query fuer MaxUsage-Abfrage korrigiert
 *
 * Revision 1.6  2008/05/19 22:35:53  willuhn
 * @N Maximale Laenge von Kontonummern konfigurierbar (Soft- und Hardlimit)
 * @N Laengenpruefungen der Kontonummer in Dialogen und Fachobjekten
 *
 * Revision 1.5  2008/04/27 22:22:56  willuhn
 * @C I18N-Referenzen statisch
 *
 * Revision 1.4  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 **********************************************************************/