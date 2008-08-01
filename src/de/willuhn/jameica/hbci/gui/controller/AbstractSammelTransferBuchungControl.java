/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/AbstractSammelTransferBuchungControl.java,v $
 * $Revision: 1.10 $
 * $Date: 2008/08/01 11:05:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.dialogs.AdresseAuswahlDialog;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Abstrakter Controller fuer die Dialoge "Buchung einer Sammellastschrift/-ueberweisung bearbeiten".
 * @author willuhn
 */
public abstract class AbstractSammelTransferBuchungControl extends AbstractControl
{

	// Fach-Objekte
	private Address gegenKonto							= null;
	
	// Eingabe-Felder
	private Input betrag										= null;
	private TextInput zweck									= null;
	private TextInput zweck2								= null;

	private DialogInput gkNummer 						= null;
	private TextInput gkName								= null;
	private TextInput gkBLZ									= null;

	private CheckboxInput storeAddress		 	= null;

	private I18N i18n                       = null;

  /**
   * ct.
   * @param view
   */
  public AbstractSammelTransferBuchungControl(AbstractView view)
  {
    super(view);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liefert die aktuelle Buchung.
   * @return die Buchung.
   */
  public abstract SammelTransferBuchung getBuchung();

	/**
	 * Liefert das Eingabe-Feld fuer das Gegenkonto.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public DialogInput getGegenKonto() throws RemoteException
	{
		if (gkNummer != null)
			return gkNummer;

    AdresseAuswahlDialog d = new AdresseAuswahlDialog(AdresseAuswahlDialog.POSITION_MOUSE);
    d.addCloseListener(new GegenkontoListener());
    gkNummer = new DialogInput(getBuchung().getGegenkontoNummer(),d);
    // BUGZILLA 280
    gkNummer.setMaxLength(HBCIProperties.HBCI_KTO_MAXLENGTH_HARD);
    gkNummer.setValidChars(HBCIProperties.HBCI_KTO_VALIDCHARS);
    gkNummer.setMandatory(true);
    gkNummer.setEnabled(!getBuchung().getSammelTransfer().ausgefuehrt());
		return gkNummer;
	}

	/**
	 * Liefert das Eingabe-Feld fuer die BLZ.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getGegenkontoBLZ() throws RemoteException
	{
		if (gkBLZ != null)
			return gkBLZ;
		gkBLZ = new BLZInput(getBuchung().getGegenkontoBLZ());
    gkBLZ.setMandatory(true);
    gkBLZ.setEnabled(!getBuchung().getSammelTransfer().ausgefuehrt());
		return gkBLZ;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Namen des Kontoinhabers des Gegenkontos.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getGegenkontoName() throws RemoteException
	{
		if (gkName != null)
			return gkName;
		gkName = new TextInput(getBuchung().getGegenkontoName(),HBCIProperties.HBCI_TRANSFER_NAME_MAXLENGTH);
    // BUGZILLA 163
    gkName.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    gkName.setMandatory(true);
    gkName.setEnabled(!getBuchung().getSammelTransfer().ausgefuehrt());
		return gkName;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den Verwendungszweck.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getZweck() throws RemoteException
	{
		if (zweck != null)
			return zweck;
		zweck = new TextInput(getBuchung().getZweck(),HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
		zweck.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    zweck.setMandatory(true);
    zweck.setEnabled(!getBuchung().getSammelTransfer().ausgefuehrt());
		return zweck;
	}

	/**
	 * Liefert das Eingabe-Feld fuer den "weiteren" Verwendungszweck.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getZweck2() throws RemoteException
	{
		if (zweck2 != null)
			return zweck2;
		zweck2 = new TextInput(getBuchung().getZweck2(),HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
		zweck2.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
    zweck2.setEnabled(!getBuchung().getSammelTransfer().ausgefuehrt());
		return zweck2;
	}


	/**
	 * Liefert das Eingabe-Feld fuer den Betrag.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getBetrag() throws RemoteException
	{
		if (betrag != null)
			return betrag;
		betrag = new DecimalInput(getBuchung().getBetrag(),HBCI.DECIMALFORMAT);
    betrag.setMandatory(true);
    betrag.setEnabled(!getBuchung().getSammelTransfer().ausgefuehrt());

		// wir loesen den KontoListener aus, um die Waehrung sofort anzuzeigen
		
		try
		{
			String curr = getBuchung().getSammelTransfer().getKonto().getWaehrung();
			if (curr != null)
				betrag.setComment(curr);
		}
		catch (Exception e)
		{
			Logger.error("error while reading currency name",e);
		}
		return betrag;
	}
  
  /**
   * Liefert ein Auswahlfeld fuer den Textschluessel.
   * @return Auswahlfeld.
   * @throws RemoteException
   */
  public abstract SelectInput getTextSchluessel() throws RemoteException;

	/**
	 * Liefert eine CheckBox ueber die ausgewaehlt werden kann,
	 * ob die Adresse (Gegenkonto) mitgespeichert werden soll.
	 * @return CheckBox.
	 * @throws RemoteException
	 */
	public CheckboxInput getStoreAddress() throws RemoteException
	{
		if (storeAddress != null)
			return storeAddress;

		// Nur bei neuen Buchungen aktivieren
		storeAddress = new CheckboxInput(getBuchung().isNewObject());
    storeAddress.setEnabled(!getBuchung().getSammelTransfer().ausgefuehrt());

		return storeAddress;
	}

  /**
	 * Speichert den Geld-Transfer.
   * @param next legt fest, ob nach dem Speichern gleich zur naechsten Buchung gesprungen werden soll.
	 */
	public abstract void handleStore(boolean next);

	/**
	 * Listener, der bei Auswahl des Gegenkontos die restlichen Daten vervollstaendigt.
	 */
	private class GegenkontoListener implements Listener
	{

		/**
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			if (event == null)
				return;
			gegenKonto = (Address) event.data;
			if (gegenKonto == null)
				return;
			try {
				getGegenKonto().setText(gegenKonto.getKontonummer());
				getGegenkontoBLZ().setValue(gegenKonto.getBLZ());
				getGegenkontoName().setValue(gegenKonto.getName());
				// Wenn die Adresse aus dem Adressbuch kommt, deaktivieren wir die Checkbox
				getStoreAddress().setValue(Boolean.FALSE);
        
        // BUGZILLA 408
        // Verwendungszweck automatisch vervollstaendigen
        try
        {
          String zweck = (String) getZweck().getValue();
          String zweck2 = (String) getZweck2().getValue();
          if ((zweck != null && zweck.length() > 0) || (zweck2 != null && zweck2.length() > 0))
            return;
          
          DBIterator list = getBuchung().getList();
          list.addFilter("gegenkonto_nr = ?",new Object[]{gegenKonto.getKontonummer()});
          list.addFilter("gegenkonto_blz = ?",  new Object[]{gegenKonto.getBLZ()});
          list.setOrder("order by id desc");
          if (list.hasNext())
          {
            SammelTransferBuchung t = (SammelTransferBuchung) list.next();
            getZweck().setValue(t.getZweck());
            getZweck2().setValue(t.getZweck2());
          }
        }
        catch (Exception e)
        {
          Logger.error("unable to autocomplete subject",e);
        }

			}
			catch (RemoteException er)
			{
				Logger.error("error while choosing address",er);
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei der Auswahl der Adresse"));
			}
		}
	}

}

/*****************************************************************************
 * $Log: AbstractSammelTransferBuchungControl.java,v $
 * Revision 1.10  2008/08/01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.9  2008/05/19 22:35:53  willuhn
 * @N Maximale Laenge von Kontonummern konfigurierbar (Soft- und Hardlimit)
 * @N Laengenpruefungen der Kontonummer in Dialogen und Fachobjekten
 *
 * Revision 1.8  2007/11/01 22:04:24  willuhn
 * @N Bugzilla 408
 *
 * Revision 1.7  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.6  2007/04/09 22:45:12  willuhn
 * @N Bug 380
 *
 * Revision 1.5  2006/12/28 15:38:43  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.4  2006/10/06 16:00:42  willuhn
 * @B Bug 280
 *
 * Revision 1.3  2006/03/27 16:46:21  willuhn
 * @N GUI polish
 *
 * Revision 1.2  2006/02/06 16:03:50  willuhn
 * @B bug 163
 *
 * Revision 1.1  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
*****************************************************************************/