/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UmsatzDetailControl.java,v $
 * $Revision: 1.21 $
 * $Date: 2005/06/27 14:18:49 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Detailansicht eines Umsatzes.
 */
public class UmsatzDetailControl extends AbstractControl {

  I18N i18n = null;

  // Fachobjekte
  private Umsatz umsatz = null;
	
	// Eingabe-Felder
	private Input konto				 		= null;
	private Input empfaengerName  = null;
	private Input empfaengerKonto = null;
  private Input empfaengerBlz   = null;
	private Input betrag					= null;
	private Input zweck						= null;
	private Input datum						= null;
	private Input valuta					= null;

	private Input saldo						= null;
	private Input primanota				= null;
	private Input art							= null;
	private Input customerRef			= null;
  
  private Input kommentar       = null;

  private boolean changeEN = false;
  private boolean changeEK = false;
  private boolean changeEB = false;
  private boolean changeZ1 = false;

  /**
   * ct.
   * @param view
   */
  public UmsatzDetailControl(AbstractView view) {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Liefert das Umsatz-Objekt, das auf dem Detail-Dialog angezeigt wird.
   * @return das Umsatz-Objekt.
   */
  public Umsatz getUmsatz()
  {
    if (umsatz != null)
      return umsatz;
    umsatz = (Umsatz) getCurrentObject();
    return umsatz;
  }

  /**
   * Liefert ein Eingabe-Feld fuer einen zusaetzlichen Kommentar
   * @return liefert ein Eingabefeld fuer einen zusaetzlichen Kommentar.
   * @throws RemoteException
   */
  public Input getKommentar() throws RemoteException
  {
    if (this.kommentar != null)
      return this.kommentar;
    this.kommentar = new TextAreaInput(this.getUmsatz().getKommentar());
    return this.kommentar;
  }

  /**
   * Liefert ein Eingabe-Feld mit dem Konto des Umsatzes.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getKonto() throws RemoteException
  {
    if (konto != null)
      return konto;
    Konto k = getUmsatz().getKonto();
    konto = new LabelInput(k.getKontonummer());
    String comment = k.getBezeichnung();
    String s = HBCIUtils.getNameForBLZ(k.getBLZ());
    if (s != null && s.length() > 0)
      comment += " [" + s + "]";
    konto.setComment(s);
    return konto;
  }

  /**
   * Liefert ein Eingabe-Feld mit dem Namen des Empfaengers.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getEmpfaengerName() throws RemoteException
  {
    if (empfaengerName != null)
      return empfaengerName;
    String name = getUmsatz().getEmpfaengerName();
    if (name == null || name.length() == 0)
    {
      empfaengerName = new TextInput(null,HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
      changeEN = true;
    }
    else
    {
      empfaengerName = new LabelInput(name);
    }
    return empfaengerName;
  }

  /**
   * Liefert ein Eingabe-Feld mit dem Konto des Empfaengers.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getEmpfaengerKonto() throws RemoteException
  {
    if (empfaengerKonto != null)
      return empfaengerKonto;

    String konto = getUmsatz().getEmpfaengerKonto(); 
    if (konto == null || konto.length() == 0)
    {
      empfaengerKonto = new TextInput(null);
      changeEK = true;
    }
    else
    {
      empfaengerKonto = new LabelInput(konto);
    }
    return empfaengerKonto;
  }
  
  /**
   * Liefert ein Eingabe-Feld mit der BLZ des Empfaengers.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getEmpfaengerBLZ() throws RemoteException
  {
    if (empfaengerBlz != null)
      return empfaengerBlz;

    String blz = getUmsatz().getEmpfaengerBLZ(); 
    if (blz == null || blz.length() == 0)
    {
      empfaengerBlz = new TextInput(null, HBCIProperties.HBCI_BLZ_LENGTH);
      empfaengerBlz.setComment("");
      empfaengerBlz.addListener(new Listener()
      {
        public void handleEvent(Event event)
        {
          if (empfaengerBlz == null)
            return;
          String s = (String) empfaengerBlz.getValue();
          if (s == null || s.length() == 0)
            return;
          empfaengerBlz.setComment(HBCIUtils.getNameForBLZ(s));
        }
      });
      changeEB = true;
    }
    else
    {
      empfaengerBlz= new LabelInput(blz);
      empfaengerBlz.setComment(HBCIUtils.getNameForBLZ(blz));
    }

    return empfaengerBlz;
  }

  /**
   * Liefert ein Eingabe-Feld mit Betrag der Buchung,
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getBetrag() throws RemoteException
  {
    if (betrag != null)
      return betrag;
    betrag = new LabelInput(HBCI.DECIMALFORMAT.format(getUmsatz().getBetrag()));
    betrag.setComment(getUmsatz().getKonto().getWaehrung());
    return betrag;
  }

  /**
   * Liefert ein Eingabe-Feld mit Zeile 1 des Verwendungszwecks.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getZweck() throws RemoteException
  {
    if (zweck != null)
      return zweck;
    String s = getUmsatz().getZweck();
    if (s == null || s.length() == 0)
    {
      zweck = new TextInput(null,HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
      changeZ1 = true;
    }
    else
    {
      zweck = new LabelInput(getUmsatz().getZweck());
    }
    return zweck;
  }

  /**
   * Liefert ein Eingabe-Feld mit dem Datum der Buchung.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getDatum() throws RemoteException
  {
    if (datum != null)
      return datum;
    datum = new LabelInput(HBCI.DATEFORMAT.format(getUmsatz().getDatum()));
    return datum;
  }

  /**
   * Liefert ein Eingabe-Feld mit dem Valuta der Buchung.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getValuta() throws RemoteException
  {
    if (valuta != null)
      return valuta;
    valuta = new LabelInput(HBCI.DATEFORMAT.format(getUmsatz().getValuta()));
    return valuta;
  }

	/**
	 * Liefert ein Eingabe-Feld mit dem Saldo nach der Buchung.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getSaldo() throws RemoteException
	{
		if (saldo != null)
			return saldo;
		saldo = new LabelInput(HBCI.DECIMALFORMAT.format(getUmsatz().getSaldo()));
		saldo.setComment(getUmsatz().getKonto().getWaehrung());
		return saldo;
	}

	/**
	 * Liefert ein Eingabe-Feld mit dem Primanota-Kennzeichen.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getPrimanota() throws RemoteException
	{
		if (primanota != null)
			return primanota;
		primanota = new LabelInput(getUmsatz().getPrimanota());
		return primanota;
	}

	/**
	 * Liefert ein Eingabe-Feld mit einem Text der Umsatz-Art.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getArt() throws RemoteException
	{
		if (art != null)
			return art;
		art = new LabelInput(getUmsatz().getArt());
		return art;
	}

	/**
	 * Liefert ein Eingabe-Feld mit der Kundenreferenz.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getCustomerRef() throws RemoteException
	{
		if (customerRef != null)
			return customerRef;
		customerRef = new LabelInput(getUmsatz().getCustomerRef());
		return customerRef;
	}

  /**
   * Speichert den Umsatz.
   */
  public synchronized void handleStore() {

    Umsatz u = getUmsatz();
    try {
      
      u.transactionBegin();
      u.setKommentar((String)getKommentar().getValue());
      
      // BUGZILLA 75 http://www.willuhn.de/bugzilla/show_bug.cgi?id=75
      // Und jetzt kommen noch die Felder, die evtl. bearbeitet werden duerfen.
      if (changeEB) u.setEmpfaengerBLZ((String) getEmpfaengerBLZ().getValue());
      if (changeEK) u.setEmpfaengerKonto((String) getEmpfaengerKonto().getValue());
      if (changeEN) u.setEmpfaengerName((String) getEmpfaengerName().getValue());
      if (changeZ1) u.setZweck((String) getZweck().getValue());
      
      if (changeEB || changeEK || changeEN || changeZ1)
      {
        String[] fields = new String[]
       {
         u.getEmpfaengerName(),
         u.getEmpfaengerKonto(),
         u.getEmpfaengerBLZ(),
         HBCI.DATEFORMAT.format(u.getValuta()),
         u.getZweck(),
         u.getKonto().getWaehrung() + " " + HBCI.DECIMALFORMAT.format(u.getBetrag())
       };
       String msg = i18n.tr("Umsatz [Gegenkonto: {0}, Kto. {1} BLZ {2}], Valuta {3}, Zweck: {4}] {5} geändert",fields);
       getUmsatz().getKonto().addToProtokoll(msg,Protokoll.TYP_SUCCESS);
      }

      getUmsatz().store();
      u.transactionCommit();
      GUI.getStatusBar().setSuccessText(i18n.tr("Umsatz gespeichert"));
    }
    catch (ApplicationException e2)
    {
      try
      {
        u.transactionRollback();
      }
      catch (RemoteException e1)
      {
        Logger.error("unable to rollback transaction",e1);
      }
      GUI.getView().setErrorText(e2.getMessage());
    }
    catch (RemoteException e)
    {
      try
      {
        u.transactionRollback();
      }
      catch (RemoteException e1)
      {
        Logger.error("unable to rollback transaction",e1);
      }
      Logger.error("error while storing umsatz",e);
      GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern des Umsatzes"));
    }
  }

}


/**********************************************************************
 * $Log: UmsatzDetailControl.java,v $
 * Revision 1.21  2005/06/27 14:18:49  web0
 * @B bug 75
 *
 * Revision 1.20  2005/06/21 20:11:10  web0
 * @C cvs merge
 *
 * Revision 1.19  2005/06/17 17:36:34  web0
 * @B bug 75
 *
 * Revision 1.18  2005/06/15 16:10:48  web0
 * @B javadoc fixes
 *
 * Revision 1.17  2005/06/13 23:11:01  web0
 * *** empty log message ***
 *
 * Revision 1.16  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.15  2004/10/20 12:08:18  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.14  2004/10/08 13:37:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.12  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.11  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/07/09 00:04:40  willuhn
 * @C Redesign
 *
 * Revision 1.9  2004/06/30 20:58:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/06/08 22:28:58  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.6  2004/04/25 18:17:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/04/19 22:53:52  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/04/13 23:14:23  willuhn
 * @N datadir
 *
 * Revision 1.3  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.2  2004/03/30 22:07:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/03/11 08:55:42  willuhn
 * @N UmsatzDetails
 *
 **********************************************************************/