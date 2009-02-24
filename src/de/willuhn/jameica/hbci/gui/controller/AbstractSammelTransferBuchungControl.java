/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/AbstractSammelTransferBuchungControl.java,v $
 * $Revision: 1.16 $
 * $Date: 2009/02/24 23:51:01 $
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
import de.willuhn.jameica.hbci.gui.dialogs.VerwendungszweckDialog;
import de.willuhn.jameica.hbci.gui.input.AddressInput;
import de.willuhn.jameica.hbci.gui.input.BLZInput;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.messaging.StatusBarMessage;
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
	private Address gegenKonto				 = null;
	
	// Eingabe-Felder
	private Input betrag							 = null;
	private TextInput zweck            = null;
	private DialogInput zweck2				 = null;
  VerwendungszweckDialog zweckDialog = null;

  private AddressInput gkName        = null;
	private TextInput gkNummer 			   = null;
	private TextInput gkBLZ						 = null;

	private CheckboxInput storeAddress = null;

	private I18N i18n                  = null;

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
   * Liefert das Eingabe-Feld fuer den Namen des Kontoinhabers des Gegenkontos.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AddressInput getGegenkontoName() throws RemoteException
  {
    if (gkName != null)
      return gkName;
    gkName = new AddressInput(getBuchung().getGegenkontoName());
    gkName.setMandatory(true);
    gkName.setEnabled(!getBuchung().getSammelTransfer().ausgefuehrt());
    gkName.addListener(new GegenkontoListener());
    return gkName;
  }


  /**
	 * Liefert das Eingabe-Feld fuer das Gegenkonto.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public TextInput getGegenKonto() throws RemoteException
	{
		if (gkNummer != null)
			return gkNummer;

    gkNummer = new TextInput(getBuchung().getGegenkontoNummer(),HBCIProperties.HBCI_KTO_MAXLENGTH_SOFT);
    gkNummer.setValidChars(HBCIProperties.HBCI_KTO_VALIDCHARS + " ");
    gkNummer.setMandatory(true);
    gkNummer.setEnabled(!getBuchung().getSammelTransfer().ausgefuehrt());
    gkNummer.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        String s = (String)gkNummer.getValue();
        if (s == null || s.length() == 0 || s.indexOf(" ") == -1)
          return;
        gkNummer.setValue(s.replaceAll(" ",""));
      }
    });
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
  public DialogInput getZweck2() throws RemoteException
  {
    if (zweck2 != null)
      return zweck2;
    final String buttonText = "weitere Zeilen ({0})...";
    this.zweckDialog = new VerwendungszweckDialog(getBuchung(),VerwendungszweckDialog.POSITION_MOUSE);
    this.zweckDialog.addCloseListener(new Listener() {
      public void handleEvent(Event event)
      {
        try
        {
          String[] newLines = (String[]) zweckDialog.getData();
          if (newLines != null) // andernfalls wurde "Abbrechen" gedrueckt
            zweck2.setButtonText(i18n.tr(buttonText,String.valueOf(newLines.length)));
        }
        catch (Exception e)
        {
          Logger.error("unable to update line count",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren der Zeilen-Anzahl"), StatusBarMessage.TYPE_ERROR));
        }
      }
    
    });
    zweck2 = new DialogInput(getBuchung().getZweck2(),this.zweckDialog);
    zweck2.setButtonText(i18n.tr(buttonText,String.valueOf(getBuchung().getWeitereVerwendungszwecke().length)));
    zweck2.setMaxLength(HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
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
    
    SammelTransferBuchung b = getBuchung();
    double d = b.getBetrag();
    if (d == 0.0d) d = Double.NaN;

    betrag = new DecimalInput(d,HBCI.DECIMALFORMAT);
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
				getGegenKonto().setValue(gegenKonto.getKontonummer());
				getGegenkontoBLZ().setValue(gegenKonto.getBlz());
				getGegenkontoName().setText(gegenKonto.getName());
				// Wenn die Adresse aus dem Adressbuch kommt, deaktivieren wir die Checkbox
				getStoreAddress().setValue(Boolean.FALSE);
        
        // BUGZILLA 408
        // Verwendungszweck automatisch vervollstaendigen
        try
        {
          String zweck = (String) getZweck().getValue();
          String zweck2 = (String) getZweck2().getText();
          if ((zweck != null && zweck.length() > 0) || (zweck2 != null && zweck2.length() > 0))
            return;
          
          DBIterator list = getBuchung().getList();
          list.addFilter("gegenkonto_nr = ?",new Object[]{gegenKonto.getKontonummer()});
          list.addFilter("gegenkonto_blz = ?",  new Object[]{gegenKonto.getBlz()});
          list.setOrder("order by id desc");
          if (list.hasNext())
          {
            SammelTransferBuchung t = (SammelTransferBuchung) list.next();
            getZweck().setValue(t.getZweck());
            getZweck2().setText(t.getZweck2());
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
 * Revision 1.16  2009/02/24 23:51:01  willuhn
 * @N Auswahl der Empfaenger/Zahlungspflichtigen jetzt ueber Auto-Suggest-Felder
 *
 * Revision 1.15  2008/12/04 23:20:37  willuhn
 * @N BUGZILLA 310
 *
 * Revision 1.14  2008/12/01 23:54:42  willuhn
 * @N BUGZILLA 188 Erweiterte Verwendungszwecke in Exports/Imports und Sammelauftraegen
 *
 * Revision 1.13  2008/11/17 23:30:00  willuhn
 * @C Aufrufe der depeicated BLZ-Funktionen angepasst
 *
 * Revision 1.12  2008/10/27 09:23:38  willuhn
 * @B Beim Duplizieren wurde der Betrag nicht uebernommen
 *
 * Revision 1.11  2008/10/15 21:40:32  willuhn
 * @N BUGZILLA 448
 *
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