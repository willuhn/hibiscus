/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/AbstractSammelTransferControl.java,v $
 * $Revision: 1.11 $
 * $Date: 2010/12/13 11:01:08 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung des Controllers fuer die Dialog Liste der Sammellastschriften/Sammel-Überweisungen.
 * @author willuhn
 */
public abstract class AbstractSammelTransferControl extends AbstractControl
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private Input kontoAuswahl				      = null;
  private Input name                    	= null;
  private DateInput termin              	= null;
  private Input summe                     = null;

  /**
   * ct.
   * @param view
   */
  public AbstractSammelTransferControl(AbstractView view)
  {
    super(view);
  }

  /**
   * Liefert den aktuellen Sammel-Auftrag.
   * @return Sammel-Auftrag.
   * @throws RemoteException
   */
  public abstract SammelTransfer getTransfer() throws RemoteException;

  /**
   * Liefert eine Tabelle mit den existierenden Sammel-Auftraegen.
   * @return Liste der Sammellastschriften.
   * @throws RemoteException
   */
  public abstract TablePart getListe() throws RemoteException;

  /**
   * Liefert eine Liste mit den in diesem Sammel-Auftrag enthaltenen Buchungen.
   * @return Liste der Buchungen.
   * @throws RemoteException
   */
  public abstract TablePart getBuchungen() throws RemoteException;

  /**
   * Liefert ein Auswahlfeld fuer das Konto.
   * @return Auswahl-Feld.
   * @throws RemoteException
   */
  public Input getKontoAuswahl() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;
    
    Konto k = getTransfer().getKonto();
    KontoListener kl = new KontoListener();
    this.kontoAuswahl = new KontoInput(k,KontoFilter.ACTIVE);
    this.kontoAuswahl.setMandatory(true);
    this.kontoAuswahl.addListener(kl);
    this.kontoAuswahl.setEnabled(!getTransfer().ausgefuehrt());
    
    // einmal ausloesen
    kl.handleEvent(null);

    
    return this.kontoAuswahl;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Termin.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public DateInput getTermin() throws RemoteException
  {
    final Terminable bu = (Terminable) getTransfer();

    if (termin != null)
      return termin;
    
    Date d = bu.getTermin();
    if (d == null)
      d = new Date();

    this.termin = new DateInput(d,HBCI.DATEFORMAT);
    this.termin.setEnabled(!bu.ausgefuehrt());
    this.termin.setTitle(i18n.tr("Termin"));
    this.termin.setText(i18n.tr("Bitte wählen Sie einen Termin"));

    if (bu.ausgefuehrt())
      this.termin.setComment(i18n.tr("Der Auftrag wurde bereits ausgeführt"));
    else if (bu.ueberfaellig())
      this.termin.setComment(i18n.tr("Der Auftrag ist überfällig"));
    else
      this.termin.setComment(""); // Platzhalter

    this.termin.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        try {
          Date date = (Date) termin.getValue();
          if (date != null && !date.after(new Date()))
            termin.setComment(i18n.tr("Der Auftrag ist überfällig"));
          else
            termin.setComment("");
        }
        catch (Exception e) {
          Logger.error("unable to check overdue",e);
        }
      }
    });
    return termin;
  }

  /**
   * Liefert ein Anzeige-Feld mit der Gesamt-Summe der Buchungen.
   * @return Anzeige-Feld.
   * @throws RemoteException
   */
  public Input getSumme() throws RemoteException
  {
    if (this.summe != null)
      return this.summe;
    this.summe = new LabelInput(HBCI.DECIMALFORMAT.format(getTransfer().getSumme()));
    Konto k = getTransfer().getKonto();
    this.summe.setComment(k != null ? k.getWaehrung() : "");
    return this.summe;
  }
  
  /**
   * Liefert ein Eingabe-Feld fuer den Namen des Sammel-Auftrages.
   * @return Name des Sammel-Auftrages.
   * @throws RemoteException
   */
  public Input getName() throws RemoteException
  {
    if (name != null)
      return name;
    name = new TextInput(getTransfer().getBezeichnung());
    name.setMandatory(true);
    if (getTransfer().ausgefuehrt())
      name.disable();
    return name;
  }

  /**
   * Speichert den Auftrag.
   * @return true, wenn der Auftrag erfolgreich gespeichert werden konnte.
   */
  public synchronized boolean handleStore()
  {
    try {
      getTransfer().setKonto((Konto)getKontoAuswahl().getValue());
      getTransfer().setBezeichnung((String)getName().getValue());
      getTransfer().setTermin((Date)getTermin().getValue());
      getTransfer().store();
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Sammel-Auftrag gespeichert"),StatusBarMessage.TYPE_SUCCESS));
      return true;
    }
    catch (ApplicationException e2)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(e2.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (RemoteException e)
    {
      Logger.error("error while storing sammeltransfer",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Speichern des Sammel-Auftrages"),StatusBarMessage.TYPE_ERROR));
    }
    return false;
  }

  /**
   * Listener, der die Auswahl des Kontos ueberwacht und die Waehrungsbezeichnung
   * hinter dem Betrag abhaengig vom ausgewaehlten Konto anpasst.
   */
  private class KontoListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {

      try {
        Object o = getKontoAuswahl().getValue();
        if (o == null || !(o instanceof Konto))
          return;

        Konto konto = (Konto) o;

        // Wird u.a. benoetigt, damit anhand des Auftrages ermittelt werden
        // kann, wieviele Zeilen Verwendungszweck jetzt moeglich sind
        getTransfer().setKonto(konto);
      }
      catch (RemoteException er)
      {
        Logger.error("error while updating konto",er);
      }
    }
  }

  /**
   * Ueberschreiben wir, damit das Item nur dann aktiv ist, wenn der
   * Auftrag noch nicht ausgefuehrt wurde.
   */
  class NotActiveMenuItem extends ContextMenuItem
  {
    
    /**
     * ct.
     * @param text anzuzeigender Text.
     * @param a auszufuehrende Action.
     * @param icon das Icon des Menueintrages.
     */
    public NotActiveMenuItem(String text, Action a, String icon)
    {
      super(text, a, icon);
    }
    
    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o == null)
        return false;
      try
      {
        SammelTransferBuchung u = (SammelTransferBuchung) o;
        return !u.getSammelTransfer().ausgefuehrt();
      }
      catch (Exception e)
      {
        Logger.error("error while enable check in menu item",e);
      }
      return false;
    }
  }



}

/*****************************************************************************
 * $Log: AbstractSammelTransferControl.java,v $
 * Revision 1.11  2010/12/13 11:01:08  willuhn
 * @B Wenn man einen Sammelauftrag in der Detailansicht loeschte, konnte man anschliessend noch doppelt auf die zugeordneten Buchungen klicken und eine ObjectNotFoundException ausloesen
 *
 * Revision 1.10  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.9  2009/10/07 23:08:56  willuhn
 * @N BUGZILLA 745: Deaktivierte Konten in Auswertungen zwar noch anzeigen, jedoch mit "[]" umschlossen. Bei der Erstellung von neuen Auftraegen bleiben sie jedoch ausgeblendet. Bei der Gelegenheit wird das Default-Konto jetzt mit ">" markiert
 *
 * Revision 1.8  2009/03/11 23:40:45  willuhn
 * @B Kleineres Bugfixing in Sammeltransfer-Control
 *
 * Revision 1.7  2009/01/04 16:18:22  willuhn
 * @N BUGZILLA 404 - Kontoauswahl via SelectBox
*****************************************************************************/