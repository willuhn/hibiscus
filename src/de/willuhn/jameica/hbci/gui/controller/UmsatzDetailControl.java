/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UmsatzDetailControl.java,v $
 * $Revision: 1.6 $
 * $Date: 2004/04/25 18:17:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.views.UmsatzListe;
import de.willuhn.jameica.hbci.rmi.Empfaenger;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
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
	private AbstractInput konto				 		= null;
	private AbstractInput empfaengerName  = null;
	private AbstractInput empfaengerKonto = null;
	private AbstractInput betrag					= null;
	private AbstractInput zweck						= null;
	private AbstractInput zweck2					= null;
	private AbstractInput datum						= null;
	private AbstractInput valuta					= null;

  /**
   * ct.
   * @param view
   */
  public UmsatzDetailControl(AbstractView view) {
    super(view);
    i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Liefert das Umsatz-Objekt, das auf dem Detail-Dialog angezeigt wird.
   * @return das Umsatz-Objekt.
   * @throws RemoteException
   */
  public Umsatz getUmsatz() throws RemoteException
  {
    if (umsatz != null)
      return umsatz;
    umsatz = (Umsatz) getCurrentObject();
    return umsatz;
  }

  /**
   * Liefert ein Eingabe-Feld mit dem Konto des Umsatzes.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AbstractInput getKonto() throws RemoteException
  {
    if (konto != null)
      return konto;
    Konto k = getUmsatz().getKonto();
    konto = new LabelInput(k.getKontonummer());
    konto.setComment(HBCIUtils.getNameForBLZ(k.getBLZ()));
    return konto;
  }

  /**
   * Liefert ein Eingabe-Feld mit dem Namen des Empfaengers.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AbstractInput getEmpfaengerName() throws RemoteException
  {
    if (empfaengerName != null)
      return empfaengerName;
    empfaengerName = new LabelInput(getUmsatz().getEmpfaengerName());
    return empfaengerName;
  }

  /**
   * Liefert ein Eingabe-Feld mit dem Konto des Empfaengers.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AbstractInput getEmpfaengerKonto() throws RemoteException
  {
    if (empfaengerKonto != null)
      return empfaengerKonto;
    empfaengerKonto = new LabelInput(getUmsatz().getEmpfaengerKonto());
    empfaengerKonto.setComment(HBCIUtils.getNameForBLZ(getUmsatz().getEmpfaengerBLZ()));
    return empfaengerKonto;
  }
  
  /**
   * Liefert ein Eingabe-Feld mit Betrag der Buchung,
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AbstractInput getBetrag() throws RemoteException
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
  public AbstractInput getZweck() throws RemoteException
  {
    if (zweck != null)
      return zweck;
    zweck = new LabelInput(getUmsatz().getZweck());
    return zweck;
  }

  /**
   * Liefert ein Eingabe-Feld mit Zeile 2 des Verwendungszwecks.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AbstractInput getZweck2() throws RemoteException
  {
    if (zweck2 != null)
      return zweck2;
    zweck2 = new LabelInput(getUmsatz().getZweck2());
    return zweck2;
  }
  
  /**
   * Liefert ein Eingabe-Feld mit dem Datum der Buchung.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public AbstractInput getDatum() throws RemoteException
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
  public AbstractInput getValuta() throws RemoteException
  {
    if (valuta != null)
      return valuta;
    valuta = new LabelInput(HBCI.DATEFORMAT.format(getUmsatz().getValuta()));
    return valuta;
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
    try {
      GUI.startView(UmsatzListe.class.getName(),getUmsatz().getKonto());
    }
    catch(RemoteException e)
    {
      Application.getLog().error("error while opening umsatz list",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Laden der Umsätze"));
    }
  }
  
  /**
   * Speichert den Empfaenger des aktuellen Umsatzes in der Adressliste.
   */
  public void handleAddEmpfaenger()
  {
    try {
      // wir checken erstmal, ob wir den schon haben.
      DBIterator list = Settings.getDatabase().createList(Empfaenger.class);
      list.addFilter("kontonummer = '" + getUmsatz().getEmpfaengerKonto() + "'");
      list.addFilter("blz = '" + getUmsatz().getEmpfaengerBLZ() + "'");
      if (list.hasNext())
      {
        YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
        d.setTitle(i18n.tr("Empfänger existiert"));
        d.setText(i18n.tr("Ein Empfänger mit dieser Kontonummer und BLZ existiert bereits. " +
        		"Möchten Sie den Empfänger dennoch zum Adressbuch hinzufügen?"));
        if (!((Boolean) d.open()).booleanValue()) return;
      }
      Empfaenger e = (Empfaenger) Settings.getDatabase().createObject(Empfaenger.class,null);
      e.setBLZ(getUmsatz().getEmpfaengerBLZ());
      e.setKontonummer(getUmsatz().getEmpfaengerKonto());
      e.setName(getUmsatz().getEmpfaengerName());
      e.store();
			GUI.getStatusBar().setSuccessText(i18n.tr("Adresse gespeichert"));
    }
    catch (ApplicationException ae)
    {
			GUI.getView().setErrorText(i18n.tr(ae.getMessage()));
    }
    catch (Exception re)
    {
      Application.getLog().error("error while storing empfaenger",re);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern des Empfängers"));
    }
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore() {
  }
  
  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
  }
  
  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o) {
  }
}


/**********************************************************************
 * $Log: UmsatzDetailControl.java,v $
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