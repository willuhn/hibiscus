/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.MultiInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.SynchronizeSchedulerSettings;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum Konfigurieren der automatischen Synchronisierung.
 */
public class SynchronizeSchedulerOptionsDialog extends AbstractDialog<Void>
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private final static int WINDOW_WIDTH = 540;
  
  private CheckboxInput enabled = null;
  private CheckboxInput stopOnError = null;
  private List<CheckboxInput> weekdays = null;
  private SelectInput timeFrom = null;
  private SelectInput timeTo = null;

  /**
   * ct.
   * @param position
   */
  public SynchronizeSchedulerOptionsDialog(int position)
  {
    super(position);
    this.setTitle(i18n.tr("Automatische Synchronisierung konfigurieren"));
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    final Container c1 = new SimpleContainer(parent);
    
    c1.addPart(this.getEnabled());
    c1.addPart(this.getStopOnError());

    c1.addHeadline(i18n.tr("Wochentage einschränken"));
    c1.addText(i18n.tr("Die automatische Synchronisierung wird nur an den angegebenen Wochentagen ausgeführt. " +
                       "An den nicht ausgewählten Tagen wird sie pausiert."),true);
    for (CheckboxInput c:this.getWeekdays())
    {
      c1.addPart(c);
    }
    
    c1.addHeadline(i18n.tr("Uhrzeit einschränken"));
    c1.addText(i18n.tr("Die automatische Synchronisierung wird nur im angegebenen Zeitfenster ausgeführt. " +
                       "In der übrigen Zeit wird sie pausiert."),true);
    
    final MultiInput multi = new MultiInput(this.getTimeFrom(),this.getTimeTo());
    c1.addInput(multi);

    final ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"),a -> {
      
      SynchronizeSchedulerSettings.setEnabled(((Boolean)getEnabled().getValue()).booleanValue());
      SynchronizeSchedulerSettings.setSchedulerStartTime(((Integer)getTimeFrom().getValue()));
      SynchronizeSchedulerSettings.setSchedulerEndTime(((Integer)getTimeTo().getValue()));
      
      for (CheckboxInput ci:getWeekdays())
      {
        final Integer day = (Integer) ci.getData("day");
        SynchronizeSchedulerSettings.setSchedulerIncludeDay(day,((Boolean)ci.getValue()).booleanValue());
      }
      close();
      
      // Startseite neu laden
      GUI.getCurrentView().reload();
      
    },null,true,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
    
    c1.addButtonArea(buttons);
    
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }
  
  /**
   * Liefert die Checkbox, mit der das Feature aktiviert werden kann.
   * @return Checkbox, mit der das Feature aktiviert werden kann.
   */
  private CheckboxInput getEnabled()
  {
    if (this.enabled != null)
      return this.enabled;
    
    this.enabled = new CheckboxInput(SynchronizeSchedulerSettings.isEnabled());
    this.enabled.setName(i18n.tr("Automatische Synchronisierung aktivieren"));
    final Listener l = new Listener() {
      
      @Override
      public void handleEvent(Event event)
      {
        final boolean b = ((Boolean)getEnabled().getValue()).booleanValue();
        getStopOnError().setEnabled(b);
        getTimeFrom().setEnabled(b);
        getTimeTo().setEnabled(b);
        for (CheckboxInput i:getWeekdays())
        {
          i.setEnabled(b);
        }
      }
    };
    this.enabled.addListener(l);
    l.handleEvent(null);
    return this.enabled;
  }
  
  /**
   * Liefert die Checkbox, mit der eingestellt werden kann, ob die Synchronisierung im Fehlerfall stoppt.
   * @return Checkbox.
   */
  private CheckboxInput getStopOnError()
  {
    if (this.stopOnError != null)
      return this.stopOnError;
    
    this.stopOnError = new CheckboxInput(SynchronizeSchedulerSettings.getStopSchedulerOnError());
    this.stopOnError.setName(i18n.tr("Im Fehlerfall anhalten"));
    return this.stopOnError;
  }
  
  /**
   * Liefert die Checkboxen für die Wochentage.
   * @return die Checkboxen für die Wochentage.
   */
  private List<CheckboxInput> getWeekdays()
  {
    if (this.weekdays != null)
      return this.weekdays;
    
    final DateFormatSymbols symbols = new DateFormatSymbols(Application.getConfig().getLocale());
    String[] dayNames = symbols.getWeekdays();
    
    this.weekdays = new ArrayList<>();
    for (int i:Arrays.asList(Calendar.MONDAY,Calendar.TUESDAY,Calendar.WEDNESDAY,Calendar.THURSDAY,Calendar.FRIDAY,Calendar.SATURDAY,Calendar.SUNDAY))
    {
      final CheckboxInput c = new CheckboxInput(SynchronizeSchedulerSettings.getSchedulerIncludeDay(i));
      c.setName(dayNames[i]);
      c.setData("day",i);
      this.weekdays.add(c);
    }
    
    return this.weekdays;
  }
  
  /**
   * Liefert ein Auswahlfeld für die Start-Stunde.
   * @return Auswahlfeld.
   */
  private SelectInput getTimeFrom()
  {
    if (this.timeFrom != null)
      return this.timeFrom;    
    
    this.timeFrom = new SelectInput(this.getHours(),SynchronizeSchedulerSettings.getSchedulerStartTime());
    this.timeFrom.setEditable(false);
    this.timeFrom.setComment(i18n.tr("Uhr"));
    this.timeFrom.setName(i18n.tr("Nur in der Zeit von"));
    return this.timeFrom;
  }
  
  /**
   * Liefert ein Auswahlfeld für End-Stunde.
   * @return Auswahlfeld.
   */
  private SelectInput getTimeTo()
  {
    if (this.timeTo != null)
      return this.timeTo;    
    
    this.timeTo = new SelectInput(this.getHours(),SynchronizeSchedulerSettings.getSchedulerEndTime());
    this.timeTo.setEditable(false);
    this.timeTo.setComment(i18n.tr("Uhr"));
    this.timeTo.setName(i18n.tr("bis"));
    return this.timeTo;
  }

  /**
   * Liefert die Liste mit den Stunden des Tages.
   * @return die Liste mit den Stunden des Tages.
   */
  private List<Integer> getHours()
  {
    final List<Integer> result = new ArrayList<>();
    for (int i=0;i<24;++i)
    {
      result.add(i);
    }
    return result;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected Void getData() throws Exception
  {
    return null;
  }

}


