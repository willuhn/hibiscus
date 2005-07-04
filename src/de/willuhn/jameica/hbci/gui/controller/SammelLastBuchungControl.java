/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/SammelLastBuchungControl.java,v $
 * $Revision: 1.6 $
 * $Date: 2005/07/04 12:41:39 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.ListDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer den Dialog "Buchung einer Sammellastschrift bearbeiten".
 * @author willuhn
 */
public class SammelLastBuchungControl extends AbstractControl
{

	// Fach-Objekte
	private Adresse gegenKonto							= null;
	private SammelLastBuchung buchung				= null;
	
	// Eingabe-Felder
	private Input betrag										= null;
	private TextInput zweck									= null;
	private TextInput zweck2								= null;

	private DialogInput gkNummer 						= null;
	private Input gkName 										= null;
	private Input gkBLZ	 										= null;

	private CheckboxInput storeAddress		 	= null;

	I18N i18n;

  /**
   * ct.
   * @param view
   */
  public SammelLastBuchungControl(AbstractView view)
  {
    super(view);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liefert die aktuelle Buchung.
   * @return die Buchung.
   */
  public SammelLastBuchung getBuchung()
	{
		if (this.buchung != null)
			return this.buchung;
		this.buchung = (SammelLastBuchung) this.getCurrentObject();
		return this.buchung;
	}

	/**
	 * Liefert das Eingabe-Feld fuer das Gegenkonto.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public DialogInput getGegenKonto() throws RemoteException
	{
		if (gkNummer != null)
			return gkNummer;

		ListDialog d = new ListDialog(Settings.getDBService().createList(Adresse.class),ListDialog.POSITION_MOUSE);
		d.addColumn(i18n.tr("Name"),"name");
		d.addColumn(i18n.tr("Kontonummer"),"kontonummer");
		d.addColumn(i18n.tr("BLZ"),"blz");
		d.setTitle(i18n.tr("Auswahl des Gegenkontos"));
		d.addCloseListener(new GegenkontoListener());

		gkNummer = new DialogInput(getBuchung().getGegenkontoNummer(),d);

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
		gkBLZ = new TextInput(getBuchung().getGegenkontoBLZ(),HBCIProperties.HBCI_BLZ_LENGTH);

		gkBLZ.setComment("");
		gkBLZ.addListener(new BLZListener());
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

		// wir loesen den KontoListener aus, um die Waehrung sofort anzuzeigen
		
		try
		{
			String curr = getBuchung().getSammelLastschrift().getKonto().getWaehrung();
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

		return storeAddress;
	}

  /**
	 * Speichert den Geld-Transfer.
	 * @return true, wenn das Speichern erfolgreich war.
	 */
	public synchronized boolean handleStore()
	{
		try {
  		
			getBuchung().transactionBegin();

      Double db = (Double)getBetrag().getValue();
      if (db != null)
        getBuchung().setBetrag(db.doubleValue());
			getBuchung().setZweck((String)getZweck().getValue());
			getBuchung().setZweck2((String)getZweck2().getValue());

			String kto  = ((DialogInput) getGegenKonto()).getText();
			String blz  = (String)getGegenkontoBLZ().getValue();
			String name = (String)getGegenkontoName().getValue();

			getBuchung().setGegenkontoNummer(kto);
			getBuchung().setGegenkontoBLZ(blz);
			getBuchung().setGegenkontoName(name);
			getBuchung().store();

			Boolean store = (Boolean) getStoreAddress().getValue();
			if (store.booleanValue())
			{

				// wir checken erstmal, ob wir den schon haben.
				DBIterator list = Settings.getDBService().createList(Adresse.class);
				list.addFilter("kontonummer = '" + kto + "'");
				list.addFilter("blz = '" + blz + "'");
				if (list.hasNext())
				{
					YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
					d.setTitle(i18n.tr("Adresse existiert"));
					d.setText(i18n.tr("Eine Adresse mit dieser Kontonummer und BLZ existiert bereits. " +
							"Möchten Sie sie dennoch zum Adressbuch hinzufügen?"));
					if (!((Boolean) d.open()).booleanValue()) return false;
				}
				Adresse e = (Adresse) Settings.getDBService().createObject(Adresse.class,null);
				e.setBLZ(blz);
				e.setKontonummer(kto);
				e.setName(name);
				e.store();
				GUI.getStatusBar().setSuccessText(i18n.tr("Buchung und Adresse gespeichert"));
			}
			else {
				GUI.getStatusBar().setSuccessText(i18n.tr("Buchung gespeichert"));
			}
			getBuchung().transactionCommit();
			return true;
		}
		catch (ApplicationException e)
		{
			try {
				getBuchung().transactionRollback();
			}
			catch (RemoteException re)
			{
				Logger.error("rollback failed",re);
			}
			GUI.getView().setErrorText(i18n.tr(e.getMessage()));
		}
		catch (Exception e2)
		{
			try {
				getBuchung().transactionRollback();
			}
			catch (RemoteException re)
			{
				Logger.error("rollback failed",re);
			}
			Logger.error("error while storing buchung",e2);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern der Buchung"));
		}
		return false;
	}


	/**
	 * Listener, der den Namen des Geldinstitutes bei BLZ-Auswahl dranschreibt.
	 */
	private class BLZListener implements Listener
	{
		/**
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			String name = HBCIUtils.getNameForBLZ((String)gkBLZ.getValue());
			gkBLZ.setComment(name);
		}
	}

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
			gegenKonto = (Adresse) event.data;
			if (gegenKonto == null)
				return;
			try {
				getGegenKonto().setText(gegenKonto.getKontonummer());
				getGegenkontoBLZ().setValue(gegenKonto.getBLZ());
				getGegenkontoName().setValue(gegenKonto.getName());
				// Wenn die Adresse aus dem Adressbuch kommt, deaktivieren wir die Checkbox
				getStoreAddress().setValue(Boolean.FALSE);
				
				// und jetzt noch das Geld-Institut dranpappen
				new BLZListener().handleEvent(null);
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
 * $Log: SammelLastBuchungControl.java,v $
 * Revision 1.6  2005/07/04 12:41:39  web0
 * @B bug 90
 *
 * Revision 1.5  2005/07/04 11:36:53  web0
 * @B bug 89
 *
 * Revision 1.4  2005/04/05 21:51:54  web0
 * @B Begrenzung aller BLZ-Eingaben auf 8 Zeichen
 *
 * Revision 1.3  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.2  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.1  2005/03/02 17:59:31  web0
 * @N some refactoring
 *
*****************************************************************************/