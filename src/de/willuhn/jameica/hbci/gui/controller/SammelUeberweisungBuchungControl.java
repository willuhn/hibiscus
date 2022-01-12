/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.TextSchluessel;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerAdd;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungBuchungNew;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer den Dialog "Buchung einer Sammellastschrift bearbeiten".
 */
public class SammelUeberweisungBuchungControl extends AbstractSammelTransferBuchungControl
{

	// Fach-Objekte
	private SammelTransferBuchung buchung	  = null;
  private SelectInput textschluessel      = null;
	
	private I18N i18n                       = null;

  /**
   * ct.
   * @param view
   */
  public SammelUeberweisungBuchungControl(AbstractView view)
  {
    super(view);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSammelTransferBuchungControl#getBuchung()
   */
  public SammelTransferBuchung getBuchung()
	{
		if (this.buchung != null)
			return this.buchung;
		this.buchung = (SammelUeberweisungBuchung) this.getCurrentObject();
		return this.buchung;
	}

	/**
	 * @see de.willuhn.jameica.hbci.gui.controller.AbstractSammelTransferBuchungControl#handleStore(boolean)
	 */
	public synchronized void handleStore(boolean next)
	{
		try {
  		
			getBuchung().transactionBegin();

      Double db = (Double)getBetrag().getValue();
      getBuchung().setBetrag(db == null ? Double.NaN : db.doubleValue());
			getBuchung().setZweck((String)getZweck().getValue());
      getBuchung().setZweck2((String)getZweck2().getText());  // "getText()" ist wichtig, weil das ein DialogInput ist
      
      TextSchluessel ts = (TextSchluessel) getTextSchluessel().getValue();
      getBuchung().setTextSchluessel(ts == null ? null : ts.getCode());

      String kto  = (String) getGegenKonto().getValue();
      String blz  = (String) getGegenkontoBLZ().getValue();
      String name = getGegenkontoName().getText();

			getBuchung().setGegenkontoNummer(kto);
			getBuchung().setGegenkontoBLZ(blz);
			getBuchung().setGegenkontoName(name);

      // Geaenderte Verwendungszwecke uebernehmen. Allerdings nur, wenn
      // der Dialog tatsaechlich geoffnet und auf "Uebernehmen" geklickt wurde
      String[] lines = (String[]) this.zweckDialog.getData();
      if (lines != null)
        getBuchung().setWeitereVerwendungszwecke(lines);
			
			getBuchung().store();

			Boolean store = (Boolean) getStoreAddress().getValue();
			if (store.booleanValue())
			{
        HibiscusAddress e = (HibiscusAddress) Settings.getDBService().createObject(HibiscusAddress.class,null);
        e.setBlz(blz);
        e.setKontonummer(kto);
        e.setName(name);
        
        // Zu schauen, ob die Adresse bereits existiert, ueberlassen wir der Action
        new EmpfaengerAdd().handleAction(e);
			}
			GUI.getStatusBar().setSuccessText(i18n.tr("Buchung gespeichert"));
			getBuchung().transactionCommit();

      if (getBuchung().getBetrag() > Settings.getUeberweisungLimit())
      {
        Konto k = getBuchung().getSammelTransfer().getKonto();
        String w = k != null ? k.getWaehrung() : HBCIProperties.CURRENCY_DEFAULT_DE;
        GUI.getView().setErrorText(i18n.tr("Warnung: Auftragslimit überschritten: {0} ",
            HBCI.DECIMALFORMAT.format(Settings.getUeberweisungLimit()) + " " + w));
      }

      
      // BUGZILLA 116 http://www.willuhn.de/bugzilla/show_bug.cgi?id=116
      if (next)
        new SammelUeberweisungBuchungNew().handleAction(getBuchung().getSammelTransfer());
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
	}
  
  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSammelTransferBuchungControl#getTextSchluessel()
   */
  public SelectInput getTextSchluessel() throws RemoteException
  {
    if (this.textschluessel != null)
      return this.textschluessel;
    this.textschluessel = new SelectInput(TextSchluessel.get(TextSchluessel.SET_SAMMELUEB),TextSchluessel.get(((SammelTransferBuchung)getBuchung()).getTextSchluessel()));
    this.textschluessel.setEnabled(!getBuchung().getSammelTransfer().ausgefuehrt());
    return this.textschluessel;
  }
}

/*****************************************************************************
 * $Log: SammelUeberweisungBuchungControl.java,v $
 * Revision 1.11  2011/05/10 11:41:29  willuhn
 * @N Text-Schluessel als Konstanten definiert - Teil aus dem Patch von Thomas vom 07.12.2010
 *
 * Revision 1.10  2009/02/24 23:51:01  willuhn
 * @N Auswahl der Empfaenger/Zahlungspflichtigen jetzt ueber Auto-Suggest-Felder
 *
 * Revision 1.9  2008/12/02 10:52:23  willuhn
 * @B DecimalInput kann NULL liefern
 * @B Double.NaN beruecksichtigen
 *
 * Revision 1.8  2008/12/01 23:54:42  willuhn
 * @N BUGZILLA 188 Erweiterte Verwendungszwecke in Exports/Imports und Sammelauftraegen
 *
 * Revision 1.7  2008/11/17 23:30:00  willuhn
 * @C Aufrufe der depeicated BLZ-Funktionen angepasst
 *
 * Revision 1.6  2008/08/01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.5  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.4  2007/04/20 14:49:05  willuhn
 * @N Support fuer externe Adressbuecher
 * @N Action "EmpfaengerAdd" "aufgebohrt"
 *
 * Revision 1.3  2006/08/23 09:45:14  willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 *
 * Revision 1.2  2006/06/26 13:25:20  willuhn
 * @N Franks eBay-Parser
 *
 * Revision 1.1  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
*****************************************************************************/