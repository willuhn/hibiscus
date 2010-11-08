/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UmsatzDetailEditControl.java,v $
 * $Revision: 1.7 $
 * $Date: 2010/11/08 10:45:21 $
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.input.AddressInput;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Controller fuer die Detailansicht eines Umsatzes zum Bearbeiten.
 */
public class UmsatzDetailEditControl extends UmsatzDetailControl
{
  private Input betrag = null;
  private Input saldo  = null;
  
	/**
   * ct.
   * @param view
   */
  public UmsatzDetailEditControl(AbstractView view) {
    super(view);
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#getEmpfaengerName()
   */
  public Input getEmpfaengerName() throws RemoteException
  {
    Input input = super.getEmpfaengerName();

    // Machen wir nur beim ersten mal
    if (!input.isEnabled())
    {
      input.setEnabled(true);
      input.addListener(new EmpfaengerListener());
    }
    return input;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#getEmpfaengerKonto()
   */
  public Input getEmpfaengerKonto() throws RemoteException
  {
    Input input = super.getEmpfaengerKonto();
    if (!input.isEnabled())
      input.setEnabled(true);
    return input;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#getEmpfaengerBLZ()
   */
  public Input getEmpfaengerBLZ() throws RemoteException
  {
    Input input = super.getEmpfaengerBLZ();
    if (!input.isEnabled())
      input.setEnabled(true);
    return input;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#getBetrag()
   */
  public Input getBetrag() throws RemoteException
  {
    if (this.betrag == null)
    {
      this.betrag = new DecimalInput(getUmsatz().getBetrag(),HBCI.DECIMALFORMAT);
      this.betrag.setMandatory(true);
      
      final Konto konto = getUmsatz().getKonto();
      
      this.betrag.setComment(konto == null ? "" : konto.getWaehrung());
      // Forciert das korrekte Formatieren des Betrages nach Focus-Wechsel
      this.betrag.addListener(new Listener() {
        public void handleEvent(Event event) {
          try
          {
            Double value = (Double) betrag.getValue();
            if (value == null)
              return;
            betrag.setValue(value);
            
            if (((konto.getFlags() & Konto.FLAG_OFFLINE) == Konto.FLAG_OFFLINE) && getUmsatz().isNewObject())
            {
              getSaldo().setValue(konto.getSaldo() + value);
            }
          }
          catch (Exception e)
          {
            Logger.error("unable to autoformat value",e);
          }
        }
      
      });
    }
    return this.betrag;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#getSaldo()
   */
  public Input getSaldo() throws RemoteException
  {
    if (this.saldo == null)
    {
      this.saldo = new DecimalInput(getUmsatz().getSaldo(),HBCI.DECIMALFORMAT);
      this.saldo.setMandatory(true);
      
      // Bei neuen Umsaetzen auf Offline-Konten automatisch den Saldo des Kontos uebernehmen
      Konto konto = getUmsatz().getKonto();
      if (((konto.getFlags() & Konto.FLAG_OFFLINE) == Konto.FLAG_OFFLINE) && getUmsatz().isNewObject())
        this.saldo.setValue(konto.getSaldo());
      
      this.saldo.setComment(konto == null ? "" : konto.getWaehrung());
      // Forciert das korrekte Formatieren des Betrages nach Focus-Wechsel
      this.saldo.addListener(new Listener() {
        public void handleEvent(Event event) {
          try
          {
            Double value = (Double) saldo.getValue();
            if (value == null)
              return;
            saldo.setValue(value);
          }
          catch (Exception e)
          {
            Logger.error("unable to autoformat value",e);
          }
        }
      
      });
    }
    return this.saldo;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#getZweck()
   */
  public Input getZweck() throws RemoteException
  {
    Input input = super.getZweck();
    if (!input.isEnabled())
      input.setEnabled(true);
    return input;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#getDatum()
   */
  public Input getDatum() throws RemoteException
  {
    Input input = super.getDatum();
    if (!input.isEnabled())
    {
      input.setMandatory(true);
      input.setEnabled(true);
    }
    return input;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#getValuta()
   */
  public Input getValuta() throws RemoteException
  {
    Input input = super.getValuta();
    if (!input.isEnabled())
    {
      input.setMandatory(true);
      input.setEnabled(true);
    }
    return input;
  }

	/**
	 * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#getPrimanota()
	 */
	public Input getPrimanota() throws RemoteException
	{
    Input input = super.getPrimanota();
    if (!input.isEnabled())
      input.setEnabled(true);
    return input;
	}

	/**
	 * Liefert ein Eingabe-Feld mit einem Text der Umsatz-Art.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getArt() throws RemoteException
	{
    Input input = super.getArt();
    if (!input.isEnabled())
      input.setEnabled(true);
    return input;
	}

	/**
	 * Liefert ein Eingabe-Feld mit der Kundenreferenz.
	 * @return Eingabe-Feld.
	 * @throws RemoteException
	 */
	public Input getCustomerRef() throws RemoteException
	{
    Input input = super.getCustomerRef();
    if (!input.isEnabled())
      input.setEnabled(true);
    return input;
	}

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#handleStore()
   */
  public synchronized void handleStore() {

    Umsatz u = getUmsatz();
    try {

      u.transactionBegin();

      u.setKommentar((String)getKommentar().getValue());
      u.setUmsatzTyp((UmsatzTyp)getUmsatzTyp().getValue());
      
      u.setGegenkontoName(((AddressInput)getEmpfaengerName()).getText());
      u.setGegenkontoNummer((String) getEmpfaengerKonto().getValue());
      u.setGegenkontoBLZ((String) getEmpfaengerBLZ().getValue());
      u.setZweck((String) getZweck().getValue());
      u.setArt((String)getArt().getValue());
      
      u.setBetrag((Double)getBetrag().getValue());
      
      Date du = (Date)getDatum().getValue();
      Double su = (Double)getSaldo().getValue();
      
      // BUGZILLA 586
      u.setSaldo(su);
      Konto k = u.getKonto();
      if ((k.getFlags() & Konto.FLAG_OFFLINE) == Konto.FLAG_OFFLINE)
      {
        // Ist ein Offline-Konto. Dann uebernehmen wir den Saldo - jedoch
        // nur dann, wenn das Datum des Umsatzes mindestens so aktuell wie
        // das Saldo-Datum des Kontos ist
        Date d = HBCIProperties.startOfDay(k.getSaldoDatum());
        if (d != null && du != null && !du.before(d))
        {
          k.setSaldo(su);
          k.store();
        }
        
      }
      
      u.setCustomerRef((String)getCustomerRef().getValue());
      u.setDatum(du);
      u.setPrimanota((String)getPrimanota().getValue());
      u.setValuta((Date)getValuta().getValue());
      
      String z = (String) getZweck().getValue();
      u.setZweck(null);
      u.setZweck2(null);
      u.setWeitereVerwendungszwecke(null);
      if (z != null && z.length() > 0)
      {
        String[] lines = z.split("\n");
        List<String> list = new ArrayList<String>();
        for (int i=0;i<lines.length;++i)
        {
          if (lines[i] == null)
            continue;
          String s = lines[i].trim();  // skip empty lines
          if (s.length() > 0)
            list.add(s);
        }
        if (list.size() > 0)
          u.setZweck(list.remove(0));
        if (list.size() > 0)
          u.setZweck2(list.remove(0));
        
        // Noch Zeilen uebrig?
        u.setWeitereVerwendungszwecke(list.toArray(new String[list.size()]));
      }
      
      getUmsatz().store();

      if (getEmpfaengerBLZ().hasChanged() ||
          getEmpfaengerKonto().hasChanged() ||
          getEmpfaengerName().hasChanged() ||
          getZweck().hasChanged() ||
          getBetrag().hasChanged() ||
          getSaldo().hasChanged() ||
          getDatum().hasChanged() ||
          getValuta().hasChanged() ||
          getPrimanota().hasChanged() ||
          getArt().hasChanged() ||
          getCustomerRef().hasChanged()
         )
      {
        String[] fields = new String[]
        {
          u.getGegenkontoName(),
          u.getGegenkontoNummer(),
          u.getGegenkontoBLZ(),
          HBCI.DATEFORMAT.format(u.getValuta()),
          u.getZweck(),
          u.getKonto().getWaehrung() + " " + HBCI.DECIMALFORMAT.format(u.getBetrag())
        };

        String msg = i18n.tr("Umsatz [Gegenkonto: {0}, Kto. {1} BLZ {2}], Valuta {3}, Zweck: {4}] {5} geändert",fields);
        getUmsatz().getKonto().addToProtokoll(msg,Protokoll.TYP_SUCCESS);
      }

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

  /**
   * BUGZILLA 132
   * Listener, der bei Auswahl des Empfaengers die restlichen Daten vervollstaendigt.
   */
  private class EmpfaengerListener implements Listener
  {

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {
      if (event == null || event.data == null || !(event.data instanceof Address))
        return;
      Address empfaenger = (Address) event.data;

      try {
        getEmpfaengerKonto().setValue(empfaenger.getKontonummer());

        String blz = empfaenger.getBlz();
        getEmpfaengerBLZ().setValue(blz);
        String name = HBCIUtils.getNameForBLZ(blz);
        getEmpfaengerBLZ().setComment(name);
      }
      catch (RemoteException er)
      {
        Logger.error("error while choosing empfaenger",er);
        GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei der Auswahl des Empfängers"));
      }
    }
  }
}


/**********************************************************************
 * $Log: UmsatzDetailEditControl.java,v $
 * Revision 1.7  2010/11/08 10:45:21  willuhn
 * @B BUGZILLA 945 - die Uhrzeit muss noch entfernt werden, damit das passt
 *
 * Revision 1.6  2010/05/15 20:01:39  willuhn
 * @N BUGZILLA 701
 *
 * Revision 1.5  2010/04/22 16:48:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2010/04/22 16:47:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2009/12/10 17:29:08  willuhn
 * @B ClassCastException
 *
 * Revision 1.2  2009/01/04 14:47:53  willuhn
 * @N Bearbeiten der Umsaetze nochmal ueberarbeitet - Codecleanup
 *
 * Revision 1.1  2009/01/04 01:25:47  willuhn
 * @N Checksumme von Umsaetzen wird nun generell beim Anlegen des Datensatzes gespeichert. Damit koennen Umsaetze nun problemlos geaendert werden, ohne mit "hasChangedByUser" checken zu muessen. Die Checksumme bleibt immer erhalten, weil sie in UmsatzImpl#insert() sofort zu Beginn angelegt wird
 * @N Umsaetze sind nun vollstaendig editierbar
 *
 **********************************************************************/