/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/SammelUeberweisungBuchungControl.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/09/30 00:08:51 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungBuchungNew;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer den Dialog "Buchung einer Sammellastschrift bearbeiten".
 * @author willuhn
 */
public class SammelUeberweisungBuchungControl extends AbstractSammelTransferBuchungControl
{

	// Fach-Objekte
	private SammelTransferBuchung buchung	  = null;
	
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
					if (!((Boolean) d.open()).booleanValue()) return;
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
}

/*****************************************************************************
 * $Log: SammelUeberweisungBuchungControl.java,v $
 * Revision 1.1  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
*****************************************************************************/