/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/AbstractSammelTransferControl.java,v $
 * $Revision: 1.7 $
 * $Date: 2009/01/04 16:18:22 $
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
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung des Controllers fuer die Dialog Liste der Sammellastschriften/Sammel-Überweisungen.
 * @author willuhn
 */
public abstract class AbstractSammelTransferControl extends AbstractControl
{

  private I18N i18n                     	= null;

  private Input kontoAuswahl				      = null;
  private Input name                    	= null;
  private DateInput termin              	= null;
  private Input comment                 	= null;
  private Input summe                     = null;

  /**
   * ct.
   * @param view
   */
  public AbstractSammelTransferControl(AbstractView view)
  {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
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
  public abstract Part getBuchungen() throws RemoteException;

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
    this.kontoAuswahl = new KontoInput(k);
    this.kontoAuswahl.setMandatory(true);
    
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

    termin = new DateInput(d,HBCI.DATEFORMAT);
    termin.setComment("");
    termin.setText(i18n.tr("Bitte geben Sie den Termin des Auftrages ein"));
    termin.setTitle(i18n.tr("Termin des Auftrages"));
    termin.addListener(new Listener() {
      public void handleEvent(Event event) {

        Date choosen = (Date) termin.getValue();
        if (choosen == null)
          return;

        try {
          // Wenn das neue Datum spaeter als das aktuelle ist,
          // nehmen wir den Kommentar weg
          if (bu.ueberfaellig() && choosen.after(new Date()));
            getComment().setValue("");
          if (choosen.before(new Date()))
            getComment().setValue(i18n.tr("Der Auftrag ist überfällig."));
        }
        catch (RemoteException e) {/*ignore*/}
      }
    });

    if (bu.ausgefuehrt())
      termin.disable();

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
   * Liefert ein Kommentar-Feld zu dieser Ueberweisung.
   * @return Kommentarfeld.
   * @throws RemoteException
   */
  public Input getComment() throws RemoteException
  {
    if (comment != null)
      return comment;
    comment = new LabelInput("");
    Terminable bu = (Terminable) getTransfer();
    if (bu.ausgefuehrt())
    {
      comment.setValue(i18n.tr("Der Auftrag wurde bereits ausgeführt"));
    }
    else if (bu.ueberfaellig())
    {
      comment.setValue(i18n.tr("Der Auftrag ist überfällig"));
    }
    return comment;
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
   * Speichert den Sammel-Auftrag.
   * @return true, wenn das Speichern erfolgreich war.
   */
  public abstract boolean handleStore();

}

/*****************************************************************************
 * $Log: AbstractSammelTransferControl.java,v $
 * Revision 1.7  2009/01/04 16:18:22  willuhn
 * @N BUGZILLA 404 - Kontoauswahl via SelectBox
 *
 * Revision 1.6  2006/12/28 15:38:43  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.5  2006/10/31 23:21:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2006/10/10 22:55:10  willuhn
 * @N Alle Datumseingabe-Felder auf DateInput umgestellt
 *
 * Revision 1.3  2006/10/09 23:56:13  willuhn
 * @N T O D O-Tags
 *
 * Revision 1.2  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.1  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
*****************************************************************************/