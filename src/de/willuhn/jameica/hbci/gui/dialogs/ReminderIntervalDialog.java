/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/ReminderIntervalDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/10/20 16:20:05 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.ReminderIntervalInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.reminder.ReminderInterval;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog zur Auswahl eines Intervalls fuer die regelmaessige
 * Duplizierung von Auftraegen
 */
public class ReminderIntervalDialog extends AbstractDialog<ReminderInterval>
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private ReminderInterval interval = null;
  private Date start                = null;
  
  private ReminderIntervalInput input = null;
  private CheckboxInput checkbox      = null;
  private TablePart preview           = null;
  
  /**
   * ct.
   * @param interval das Intervall.
   * @param start Start-Datum zur Berechnung der Vorschau auf die naechsten Termine.
   * @param position die Dialog-Position.
   */
  public ReminderIntervalDialog(ReminderInterval interval, Date start, int position)
  {
    super(position);
    this.interval = interval;
    this.setDate(start);
    
    this.setTitle(i18n.tr("Auswahl des Intervalls"));
    this.setSize(370,400);
  }
  
  /**
   * Speichert das Start-Datum fuer die Berechnung der Vorschau auf die naechsten Termine.
   * @param date das Start-Datum.
   */
  public void setDate(Date date)
  {
    this.start = date != null ? date : new Date();
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container ct = new SimpleContainer(parent,true);
    ct.addText(i18n.tr("Bei einer regelm‰ﬂigen Wiederholung wird der Auftrag " +
                       "im angegebenen Intervall (beginnend mit dem ersten " +
                       "F‰lligkeitstermin) automatisch durch Hibiscus " +
                       "dupliziert"),true);

    final Listener listener = new Listener() {
      public void handleEvent(Event event)
      {
        updatePreview();
      }
    };
    ////////////////////////////////////////////////////////////////////////////
    // Intervalle
    this.input = new ReminderIntervalInput(this.interval);
    this.input.addListener(listener);
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Checkbox
    this.checkbox = new CheckboxInput(this.interval != null);
    this.checkbox.setName(i18n.tr("Auftrag regelm‰ﬂig wiederholen"));
    this.checkbox.addListener(listener);
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Preview
    this.preview = new TablePart(null);
    this.preview.addColumn(i18n.tr("Vorschau auf die ersten 10 Folge-Termine"),null);
    this.preview.setSummary(false);
    this.preview.setFormatter(new TableFormatter() {
      public void format(TableItem item)
      {
        Date d = (Date) item.getData();
        item.setText(d != null ? HBCI.DATEFORMAT.format(d) : "-");
      }
    });
    ////////////////////////////////////////////////////////////////////////////
    
    this.updatePreview(); // einmal initial aktualisieren

    ct.addInput(this.checkbox);
    ct.addInput(this.input);
    ct.addPart(this.preview);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("‹bernehmen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        boolean enabled = ((Boolean)checkbox.getValue()).booleanValue();
        interval = (enabled) ? (ReminderInterval) input.getValue() : null;
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
    ct.addButtonArea(buttons);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected ReminderInterval getData() throws Exception
  {
    return this.interval;
  }
  
  /**
   * Aktualisiert die Preview basierend auf den aktuellen Daten.
   */
  private void updatePreview()
  {
    try
    {
      boolean enabled = ((Boolean)checkbox.getValue()).booleanValue();

      this.preview.setEnabled(enabled);
      this.input.setEnabled(enabled);
      
      // Erstmal alle Datensaetze entfernen
      this.preview.removeAll();

      // Wenn die Checkbox aus ist, bleibt die Tabelle leer
      if (!enabled)
        return;
      
      // Vorschau-Termine berechnen.
      ReminderInterval ri = (ReminderInterval) this.input.getValue();
      
      // Wir beginnen das Zeitfenster einen Tick hinter dem aktuellen Datum,
      // damit die Vorschau erst bei den Folge-Terminen und nicht schon beim
      // Termin des Auftrages selbst beginnt.
      Date from = new Date(start.getTime() + (60 * 1000L));
      Calendar cal = Calendar.getInstance();
      cal.setTime(from);
      cal.add(Calendar.YEAR,11);
      List<Date> dates = ri.getDates(start,from,cal.getTime());
      
      // Wir schreiben nur maximal 10 Termine in die Liste
      for (int i=0;i<dates.size();++i)
      {
        this.preview.addItem(dates.get(i));
        if (i >= 9)
          break;
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to calculate next dates",e);
    }
  }

}



/**********************************************************************
 * $Log: ReminderIntervalDialog.java,v $
 * Revision 1.1  2011/10/20 16:20:05  willuhn
 * @N BUGZILLA 182 - Erste Version von client-seitigen Dauerauftraegen fuer alle Auftragsarten
 *
 **********************************************************************/