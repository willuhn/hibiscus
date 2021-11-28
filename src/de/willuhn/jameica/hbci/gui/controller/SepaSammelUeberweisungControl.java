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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungNew;
import de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungBuchungNew;
import de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungNew;
import de.willuhn.jameica.hbci.gui.input.TerminInput;
import de.willuhn.jameica.hbci.gui.parts.SepaSammelTransferBuchungList;
import de.willuhn.jameica.hbci.gui.parts.SepaSammelUeberweisungList;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaSammelUeberweisung;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung des Controllers fuer den Dialog "Liste der SEPA-Sammelueberweisungen".
 */
public class SepaSammelUeberweisungControl extends AbstractSepaSammelTransferControl<SepaSammelUeberweisung>
{
  private SepaSammelUeberweisung transfer  = null;
  private SepaSammelUeberweisungList table = null;
  private TablePart buchungen              = null;
  
  private SelectInput typ                  = null;
  private TerminInput termin               = null;
  private Listener terminListener          = new TerminListener();
  

  private Input name                       = null;


  /**
   * ct.
   * @param view
   */
  public SepaSammelUeberweisungControl(AbstractView view)
  {
    super(view);
  }

  /**
   * Ueberschrieben, um einen Namensvorschlag anzuzeigen.
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferControl#getName()
   */
  public Input getName() throws RemoteException
  {
    if (this.name != null)
      return this.name;
    
    this.name = super.getName();
    if (StringUtils.trimToNull((String)this.name.getValue()) == null)
      this.name.setValue(i18n.tr("SEPA-Sammelüberweisung vom {0}",HBCI.LONGDATEFORMAT.format(new Date())));
    return this.name;
  }
  
  /**
   * Liefert eine Combobox zur Auswahl des Auftragstyps.
   * Zur Wahl stehen Ueberweisung, Termin-Ueberweisung und Umbuchung.
   * @return die Combobox.
   * @throws RemoteException
   */
  public SelectInput getTyp() throws RemoteException
  {
    if (this.typ != null)
      return this.typ;
    final SepaSammelUeberweisung u = getTransfer();
    
    List<Typ> list = new ArrayList<Typ>();
    list.add(new Typ(false));
    list.add(new Typ(true));
    this.typ = new SelectInput(list,new Typ(u.isTerminUeberweisung()));
    this.typ.setName(i18n.tr("Auftragstyp"));
    this.typ.setAttribute("name");
    this.typ.setEnabled(!u.ausgefuehrt());
    this.typ.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        // Wir muessen die Entscheidung, ob es eine Termin-Ueberweisung ist,
        // sofort im Objekt speichern, denn die Information wird von
        // "getTermin()" gebraucht, um zu erkennen, ob der Auftrag faellig ist
        try
        {
          Typ t = (Typ) getTyp().getValue();
          u.setTerminUeberweisung(t.termin);
        }
        catch (Exception e)
        {
          Logger.error("unable to set flag",e);
        }
      }
    });
    
    this.typ.addListener(this.terminListener);
    this.terminListener.handleEvent(null); // einmal initial ausloesen
    return this.typ;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferControl#getTermin()
   */
  @Override
  public TerminInput getTermin() throws RemoteException
  {
    if (this.termin != null)
      return this.termin;
    
    this.termin = super.getTermin();
    this.termin.setName(this.termin.getName() + "  "); // ein kleines bisschen extra Platz lassen, damit auch "Ausführungstermin" hin passt
    this.termin.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        try
        {
          if (!termin.hasChanged())
            return;
          
          Date date = (Date) termin.getValue();
          if (date == null)
            return;
          
          // Wenn das Datum eine Woche in der Zukunft liegt, fragen wir den User, ob es vielleicht
          // eine Terminueberweisung werden soll. Muessen wir aber nicht fragen, wenn
          // der User nicht ohnehin schon eine Termin-Ueberweisung ausgewaehlt hat
          Typ typ = (Typ) getTyp().getValue();
          if (typ == null || typ.termin)
            return;

          Calendar cal = Calendar.getInstance();
          cal.setTime(DateUtil.startOfDay(new Date()));
          cal.add(Calendar.DATE,6);
          if (DateUtil.startOfDay(date).after(cal.getTime()))
          {
            String q = i18n.tr("Soll der Auftrag als bankseitig geführte SEPA-Sammelterminüberweisung ausgeführt werden?");
            if (Application.getCallback().askUser(q))
              getTyp().setValue(new Typ(true));
          }
        }
        catch (Exception e)
        {
          Logger.error("unable to check for termsammelueb",e);
        }
        
      }
    });
    
    return this.termin;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferControl#getTransfer()
   */
  public SepaSammelUeberweisung getTransfer() throws RemoteException
  {
    if (transfer != null)
      return transfer;

    transfer = (SepaSammelUeberweisung) getCurrentObject();
    if (transfer != null)
      return transfer;

    transfer = (SepaSammelUeberweisung) Settings.getDBService().createObject(SepaSammelUeberweisung.class,null);
    return transfer;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferControl#getListe()
   */
  public SepaSammelUeberweisungList getListe() throws RemoteException
  {
    if (table != null)
      return table;

    table = new SepaSammelUeberweisungList(new SepaSammelUeberweisungNew());
    return table;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferControl#getSynchronizeJobType()
   */
  @Override
  public Class<? extends SynchronizeJob> getSynchronizeJobType()
  {
    return SynchronizeJobSepaSammelUeberweisung.class;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferControl#getBuchungen()
   */
  public TablePart getBuchungen() throws RemoteException
  {
    if (this.buchungen != null)
      return this.buchungen;
    
    Action a = new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new SepaSammelUeberweisungBuchungNew().handleAction(context);
      }
    };
    
    this.buchungen = new SepaSammelTransferBuchungList(getTransfer(),a);
    this.buchungen.setMulti(true);

    ContextMenu ctx = new ContextMenu();
    ctx.addItem(new CheckedSingleContextMenuItem(i18n.tr("Buchung öffnen"), new SepaSammelUeberweisungBuchungNew(),"document-open.png"));
    ctx.addItem(new DeleteMenuItem());
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new CreateMenuItem(new SepaSammelUeberweisungBuchungNew()));
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new CheckedSingleContextMenuItem(i18n.tr("In Einzelüberweisung duplizieren"), new AuslandsUeberweisungNew(),"ueberweisung.png"));
    this.buchungen.setContextMenu(ctx);
    return this.buchungen;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferControl#store()
   */
  @Override
  public synchronized void store() throws Exception
  {
    SepaSammelUeberweisung t = this.getTransfer();
    if (t.ausgefuehrt())
      return;

    Typ typ = (Typ) getTyp().getValue();
    t.setTerminUeberweisung(typ.termin);
    
    super.store();
  }
  
  /**
   * Listener, der das Label vor dem Termin aendert, wenn es eine Bank-seitig gefuehrte Termin-Ueberweisung ist.
   */
  private class TerminListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event)
    {
      GUI.getDisplay().asyncExec(new Runnable() {
        @Override
        public void run()
        {
          try
          {
            TerminInput input = getTermin();
            Typ typ = (Typ) getTyp().getValue();
            if (typ != null && typ.termin)
            {
              input.setName(i18n.tr("Ausführungstermin"));
              
              // Pruefen, ob es sich um eine Termin-Ueberweisung handelt. Wenn
              // das Ausfuehrungsdatum in der Vergangenheit liegt, dann Hinweis-Text anzeigen
              Date date = (Date) input.getValue();
              if (date != null)
              {
                if (!DateUtil.startOfDay(date).after(DateUtil.startOfDay(new Date())))
                  Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Ausführungstermin der Sammelterminüberweisung liegt in der Vergangenheit"),StatusBarMessage.TYPE_INFO));
              }
            }
            else
            {
              input.setName(i18n.tr("Erinnerungstermin"));
            }
            
            // Kommentar vom Termin-Eingabefeld aktualisieren.
            input.updateComment();
          }
          catch (Exception e)
          {
            Logger.error("unable to update label",e);
          }
        }
      });
    }
  }
  
  /**
   * Hilfsklasse fuer den Auftragstyp.
   */
  public class Typ
  {
    private final boolean termin;
    
    /**
     * ct.
     * @param termin true bei Termin-Ueberweisung.
     */
    private Typ(boolean termin)
    {
      this.termin = termin;
    }
    
    /**
     * Liefert den sprechenden Namen des Typs.
     * @return sprechender Name des Typs.
     */
    public String getName()
    {
      if (this.termin) return i18n.tr("Bankseitige SEPA-Sammelterminüberweisung");
      return           i18n.tr("SEPA-Sammelüberweisung");
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o)
    {
      if (o == null || !(o instanceof Typ))
        return false;
      Typ other = (Typ) o;
      return other.termin == this.termin;
    }
  }

}
