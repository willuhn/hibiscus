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

import java.lang.reflect.Method;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.MultiInput;
import de.willuhn.jameica.gui.input.ScaleInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.JameicaCompat;
import de.willuhn.jameica.hbci.SynchronizeSchedulerSettings;
import de.willuhn.jameica.hbci.rmi.SynchronizeSchedulerService;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
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
  private CheckboxInput minimizeToSystray = null;
  private ScaleInput interval = null;
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

    c1.addInput(this.getInterval());
    c1.addText(i18n.tr("Mehr als 4 Synchronisierungen pro Tag können dazu führen, dass die Bank eine TAN verlangt."),true,Color.COMMENT);

    c1.addHeadline(i18n.tr("Wochentage einschränken"));
    c1.addText(i18n.tr("Die automatische Synchronisierung wird nur an den angegebenen Wochentagen ausgeführt. " +
                       "An den nicht ausgewählten Tagen wird sie pausiert."),true,Color.COMMENT);
    
    for (CheckboxInput c:this.getWeekdays())
    {
      c1.addPart(c);
    }
    
    c1.addHeadline(i18n.tr("Uhrzeit einschränken"));
    c1.addText(i18n.tr("Die automatische Synchronisierung wird nur im angegebenen Zeitfenster ausgeführt. " +
                       "In der übrigen Zeit wird sie pausiert."),true,Color.COMMENT);
    
    final MultiInput multi = new MultiInput(this.getTimeFrom(),this.getTimeTo());
    c1.addInput(multi);

    final Input sysCheck = this.getMinimizeToSystray();
    if (sysCheck != null)
    {
      c1.addHeadline(i18n.tr("System-Tray"));
      c1.addPart(sysCheck);
      c1.addText(i18n.tr("Sie finden diese Option auch in \"Datei->Einstellungen->Look and Feel\""),true,Color.COMMENT);
    }
    
    final ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"),a -> {
      
      if (sysCheck != null)
      {
        // Wir schalten hier beide Optionen gleichzeitig
        setSystrayParameter("setEnabled");
        setSystrayParameter("setMinimizeToSystray");
      }

      final boolean enabled = ((Boolean)getEnabled().getValue()).booleanValue();
      SynchronizeSchedulerSettings.setEnabled(enabled);
      SynchronizeSchedulerSettings.setSchedulerInterval(((Integer)getInterval().getValue()));
      SynchronizeSchedulerSettings.setSchedulerStartTime(((Integer)getTimeFrom().getValue()));
      SynchronizeSchedulerSettings.setSchedulerEndTime(((Integer)getTimeTo().getValue()));

      final boolean stop = ((Boolean)getStopOnError().getValue()).booleanValue();
      SynchronizeSchedulerSettings.setStopSchedulerOnError(stop);

      for (CheckboxInput ci:getWeekdays())
      {
        final Integer day = (Integer) ci.getData("day");
        SynchronizeSchedulerSettings.setSchedulerIncludeDay(day,((Boolean)ci.getValue()).booleanValue());
      }
      
      // Je nachdem, ob die Synchronisierung aktiviert oder deaktiviert wurde, muss der Service beendet oder gestartet werden
      try
      {
        final SynchronizeSchedulerService scheduler = (SynchronizeSchedulerService) Application.getServiceFactory().lookup(HBCI.class,"synchronizescheduler");
        if (enabled)
        {
          if (!scheduler.isStarted())
            scheduler.start();
        }
        else
        {
          if (scheduler.isStarted())
            scheduler.stop(true);
        }
      }
      catch (ApplicationException ae)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
      }
      catch (Exception e)
      {
        Logger.error("error while loading synchronize scheduler status",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Status der automatischen Synchronisierung nicht ermittelbar: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
      }
      
      
      close();
      
      // Startseite neu laden
      GUI.getCurrentView().reload();
      
    },null,true,"ok.png");
    buttons.addButton(new Cancel());
    
    c1.addButtonArea(buttons);
    
    final Point size = getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT);
    getShell().setMinimumSize(size);
    this.setSize(size.x,size.y);
  }
  
  /**
   * Liefert die Instanz des Systray-Service, falls die Jameica-Version hinreichend aktuell ist (mindestens 2.10.4).
   * @return die Instanz des Systray-Service oder NULL, wenn sie nicht existiert.
   */
  private Object getSystrayService()
  {
    try
    {
      final Class c = Application.getClassLoader().load("de.willuhn.jameica.services.SystrayService");
      return Application.getBootLoader().getBootable(c);
    }
    catch (Exception e)
    {
      Logger.info("systray service not found - jameica version too old");
    }
    return null;
  }
  
  /**
   * Setzt den Parameter im Systray-Service.
   * Wir können das leider nicht per JameicaCompat machen, weil dort primitive Paremeter-Typen nicht unterstützt werden.
   * @param name der Name des Parameters.
   */
  private void setSystrayParameter(String name)
  {
    if (name == null || name.length() == 0)
      return;
    
    final Object systray = this.getSystrayService();
    final Input input = this.getMinimizeToSystray();
    if (systray == null || input == null)
      return;
    
    final boolean value = ((Boolean)input.getValue()).booleanValue();
    try
    {
      Method m = systray.getClass().getMethod(name,boolean.class);
      m.invoke(systray,value);
    }
    catch (Exception e)
    {
      Logger.error("unable to apply systray service settings",e);
    }
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
        getInterval().setEnabled(b);
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
   * Liefert den Schieberegler für das Intervall.
   * @return der Schieberegler.
   */
  public ScaleInput getInterval()
  {
    if (this.interval != null)
      return this.interval;
    
    this.interval = new ScaleInput(SynchronizeSchedulerSettings.getSchedulerInterval());
    this.interval.setScaling(60,1440,60,60);
    this.interval.setName(i18n.tr("Ausführungsintervall"));
    this.interval.setComment("");
    final Listener l = new Listener() {
      
      @Override
      public void handleEvent(Event event)
      {
        final int i = ((Integer) interval.getValue()).intValue();
        if (i % 60 == 0)
        {
          if (i == 60)
            interval.setComment(i18n.tr("1 Stunde"));
          else
            interval.setComment(i18n.tr("{0} Stunden",Integer.toString(i / 60)));
        }
        else
        {
          interval.setComment(i18n.tr("{0} Minuten",Integer.toString(i)));
        }
      }
    };
    this.interval.addListener(l);
    l.handleEvent(null);
    return this.interval;
  }
  
  /**
   * Liefert eine Checkbox, mit der eingestellt werden kann, ob die Anwendung in das Systray minimiert werden soll.
   * @return eine Checkbox, mit der eingestellt werden kann, ob die Anwendung in das Systray minimiert werden soll.
   */
  public Input getMinimizeToSystray()
  {
    if (this.minimizeToSystray != null)
      return this.minimizeToSystray;
    
    final Object service = this.getSystrayService();
    if (service == null)
      return null;
    
    try
    {
      final Boolean b = (Boolean) JameicaCompat.get(service,"isEnabled",null);
      if (b == null)
        return null;
      
      this.minimizeToSystray = new CheckboxInput(b.booleanValue());
      this.minimizeToSystray.setName(i18n.tr("Fenster beim Minimieren in System-Tray verschieben"));
      return this.minimizeToSystray;
    }
    catch (Exception e)
    {
      Logger.info("systray service not found - jameica version too old");
    }
    return null;
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


