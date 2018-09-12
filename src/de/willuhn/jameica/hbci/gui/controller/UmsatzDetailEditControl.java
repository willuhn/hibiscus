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
import java.util.Date;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetailEdit;
import de.willuhn.jameica.hbci.gui.input.AddressInput;
import de.willuhn.jameica.hbci.messaging.SaldoMessage;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil.Tag;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Controller fuer die Detailansicht eines Umsatzes zum Bearbeiten.
 */
public class UmsatzDetailEditControl extends UmsatzDetailControl
{
  private Input zweck  = null;
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
    if (this.betrag != null)
      return this.betrag;
    
    this.betrag = new DecimalInput(getUmsatz().getBetrag(),HBCI.DECIMALFORMAT);
    this.betrag.setMandatory(true);
    
    final Konto konto = getUmsatz().getKonto();
    
    this.betrag.setComment(konto == null ? "" : konto.getWaehrung());
    
    final Listener l = new Listener() {
      public void handleEvent(Event event) {
        try
        {
          Double value = (Double) betrag.getValue();
          if (value == null)
            return;

          if (konto.hasFlag(Konto.FLAG_OFFLINE) && getUmsatz().isNewObject())
            getSaldo().setValue(konto.getSaldo() + value);
        }
        catch (Exception e)
        {
          Logger.error("unable to autoformat value",e);
        }
      }
    };
    this.betrag.addListener(l);
    
    // BUGZILLA 1833 - Einmal initial ausloesen
    l.handleEvent(null);
    
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
      if (konto.hasFlag(Konto.FLAG_OFFLINE) && getUmsatz().isNewObject())
        this.saldo.setValue(konto.getSaldo());
      
      this.saldo.setComment(konto == null ? "" : konto.getWaehrung());
    }
    return this.saldo;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#getZweck()
   */
  public Input getZweck()
  {
    if (this.zweck != null)
      return this.zweck;

    final int max = HBCIProperties.HBCI_TRANSFER_USAGE_DB_MAXLENGTH;

    this.zweck = new TextAreaInput("")
    {
      /**
       * @see de.willuhn.jameica.gui.input.TextAreaInput#update()
       */
      @Override
      protected void update() throws OperationCanceledException
      {
        super.update();
        
        Control c = this.getControl();
        if (c.isDisposed())
          return;
        
        // Laenge des Verwendungszweck pruefen
        String[] lines = VerwendungszweckUtil.split((String)zweck.getValue());
        
        for (int i = 0; i < lines.length; i++)
        {
          String line = lines[i];
          if (line == null || line.length() <= max)
            continue;
          
          c.setBackground(Color.MANDATORY_BG.getSWTColor());
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Jede Zeile des Verwendungszwecks darf maximal {0} Zeichen lang sein. Zeile {1} ist {2} Zeichen lang.", ""+max , ""+(i+1), ""+line.length()) ,StatusBarMessage.TYPE_INFO));
          return;
        }
      }
    };
    return this.zweck;
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
      input.setMandatory(false);
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
	 * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#getEndToEndId()
	 */
	@Override
	public Input getEndToEndId() throws RemoteException
	{
	  Input input = super.getEndToEndId();
	  if (!input.isEnabled())
	    input.setEnabled(true);
	  return input;
	}

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#getGvCode()
   */
  public Input getGvCode() throws RemoteException
  {
    Input input = super.getGvCode();
    if (!input.isEnabled())
      input.setEnabled(true);
    return input;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#getZweckSwitch()
   */
  @Override
  public CheckboxInput getZweckSwitch() throws RemoteException
  {
    CheckboxInput input = super.getZweckSwitch();
    input.setEnabled(false);
    return input;
  }
  
  /**
   * Ueberschrieben, um den Verwendungszweck bei Bedarf umgebrochen anzuzeigen. 
   * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#getUsage(boolean)
   */
  protected String getUsage(boolean showAll)
  {
    try
    {
      Umsatz u = this.getUmsatz();
      
      if (showAll)
        return VerwendungszweckUtil.toString(u,"\n"); // Wir zeigen den rohen Verwendungszweck an

      String usage = (String) BeanUtil.get(u,Tag.SVWZ.name());
      
      // Achtung: Das Extrahieren des Tags SVWZ hat ein integriertes Fallback. Wenn naemlich
      // gar kein Tag vorhanden ist (das ist bei den alten Buchungen der Fall), dann wird
      // der komplette Verwendungszweck in einer Zeile zurueckgeliefert, um das neue SEPA-Verhalten
      // auch bei den alten Buchungen zu emulieren. Aus dem grund muss der Verwendungszweck
      // hier ggf. neu umgebrochen werden
      final int limit = HBCIProperties.HBCI_TRANSFER_USAGE_DB_MAXLENGTH;
      if (usage == null || usage.length() <= limit)
        return usage;
      
      return VerwendungszweckUtil.merge(VerwendungszweckUtil.rewrap(limit,usage));
    }
    catch (RemoteException re)
    {
      Logger.error("unable to display usage text",re);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anzeigen des Verwendungszweck: {0}",re.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
    
    return "";
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.UmsatzDetailControl#handleStore()
   */
  public boolean handleStore()
  {
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
      
      Double betrag = (Double) getBetrag().getValue();
      u.setBetrag(betrag != null ? betrag : 0.0d);
      
      Double su = (Double)getSaldo().getValue();
      
      // BUGZILLA 586
      u.setSaldo(su);
      Konto k = u.getKonto();
      if (k.hasFlag(Konto.FLAG_OFFLINE) && u.isNewObject()) // BUGZILLA 1232
      {
        k.setSaldo(su);
        k.store();
        Application.getMessagingFactory().sendMessage(new SaldoMessage(k));
      }
      
      u.setCustomerRef((String)getCustomerRef().getValue());
      u.setPrimanota((String)getPrimanota().getValue());
      u.setEndToEndId((String)getEndToEndId().getValue());
      
      Date valuta = (Date) getValuta().getValue();
      Date datum  = (Date) getDatum().getValue();
      
      if (valuta == null)
      {
        valuta = datum;
        getValuta().setValue(valuta);
      }
      if (datum == null)
      {
        datum = valuta;
        getDatum().setValue(datum);
      }
      
      u.setValuta(valuta);
      u.setDatum(datum);
      
      String gvcode = (String) getGvCode().getValue();
      String add = null;
      if (gvcode != null && gvcode.indexOf('/') != -1)
      {
        // Checken, ob wir den Add-Key raussplitten muessen
        String[] values = gvcode.split("/");
        if (values != null && values.length > 0)
        {
          gvcode = values[0];
          if (values.length > 1)
            add = values[1];
        }
      }
      u.setGvCode(gvcode);
      u.setAddKey(add);

      String z = (String) getZweck().getValue();
      
      // Erstmal die Zeilen loeschen
      u.setZweck(null);
      u.setZweck2(null);
      u.setWeitereVerwendungszwecke(null);
      
      // Und jetzt neu verteilen
      String[] lines = VerwendungszweckUtil.split(z);
      VerwendungszweckUtil.apply(u,lines);
      
      u.store();

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
          HBCI.DATEFORMAT.format(u.getDatum()),
          u.getZweck(),
          k.getWaehrung() + " " + HBCI.DECIMALFORMAT.format(u.getBetrag())
        };

        String msg = i18n.tr("Umsatz [Gegenkonto: {0}, Kto. {1} BLZ {2}], Datum {3}, Zweck: {4}] {5} geändert",fields);
        k.addToProtokoll(msg,Protokoll.TYP_SUCCESS);
      }

      u.transactionCommit();
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Umsatz gespeichert"),StatusBarMessage.TYPE_SUCCESS));
      return true;
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
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(e2.getMessage(),StatusBarMessage.TYPE_ERROR));
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
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Speichern des Umsatzes: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
    return false;
  }

  /**
   * Speichert den Umsatz und erstellt die naechste neue Buchung.
   */
  public void handleNext()
  {
    Umsatz u = getUmsatz();
    try
    {
      if (this.handleStore())
      {
        new UmsatzDetailEdit().handleAction(u.getKonto());
        // Wir muessen die Message nach dem Wechsel des Dialogs nochmal schicken, weil die Notifications nach dem Wechsel des Dialogs resettet werden
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Umsatz gespeichert, neuer Umsatz angelegt"),StatusBarMessage.TYPE_SUCCESS));
      }
    }
    catch (ApplicationException e2)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(e2.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (RemoteException e)
    {
      Logger.error("error while storing umsatz",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Speichern des Umsatzes: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
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
        String name = HBCIProperties.getNameForBank(blz);
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
