/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.NotificationPanel;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.forecast.ForecastCreator;
import de.willuhn.jameica.hbci.forecast.SaldoLimit;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.UmsatzDaysInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum Konfigurieren der Konto-Limits.
 */
public class KontoLimitsDialog extends AbstractDialog
{
  private final static Settings settings = new Settings(KontoLimitsDialog.class);
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private final static int WINDOW_WIDTH = 500;

  private Konto konto = null;
  
  private SaldoLimit upper = null;
  private SaldoLimit lower = null;
  
  private KontoInput kontoAuswahl = null;
  private NotificationPanel hinweise = null;
  private Button apply = null;
  
  private CheckboxInput upperEnabled = null;
  private CheckboxInput lowerEnabled = null;
  
  private CheckboxInput upperNotify = null;
  private CheckboxInput lowerNotify = null;
  
  private DecimalInput upperValue = null;
  private DecimalInput lowerValue = null;
  
  private UmsatzDaysInput upperDays = null;
  private UmsatzDaysInput lowerDays = null;

  private Listener reloadListener = new ReloadListener();
  private Listener statusListener = new StatusListener();
  
  private Calendar cal = Calendar.getInstance();

  /**
   * ct.
   * @param konto das Konto.
   */
  public KontoLimitsDialog(Konto konto)
  {
    super(POSITION_CENTER);
    this.konto = konto;
    this.setTitle(i18n.tr("Konto-Limits konfigurieren"));
    setSize(settings.getInt("window.width",WINDOW_WIDTH),settings.getInt("window.height",SWT.DEFAULT));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    final Container c = new SimpleContainer(parent);
    
    c.addText(i18n.tr("Nach Auswahl des Kontos können Sie ein oberes bzw. unteres Limit für den Saldo festlegen. " + 
      "Sobald dieser den Wert innerhalb der angegebenen Tage über- bzw. unterschreitet, " +
      "erhalten Sie eine Benachrichtigung."
    ),true);
    
    c.addInput(this.getKontoAuswahl());
    
    c.addHeadline(SaldoLimit.Type.UPPER.getDescription());
    c.addInput(this.getUpperEnabled());
    c.addInput(this.getUpperValue());
    c.addInput(this.getUpperDays());
    c.addInput(this.getUpperNotify());
    
    c.addHeadline(SaldoLimit.Type.LOWER.getDescription());
    c.addInput(this.getLowerEnabled());
    c.addInput(this.getLowerValue());
    c.addInput(this.getLowerDays());
    c.addInput(this.getLowerNotify());

    c.addPart(this.getHinweise());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(this.getApply());
    buttons.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,false,"window-close.png");
    
    c.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
    
    getShell().addDisposeListener(new DisposeListener() {
      
      @Override
      public void widgetDisposed(DisposeEvent e)
      {
        Shell shell = getShell();
        if (shell == null || shell.isDisposed())
          return;
        
        Point size = shell.getSize();
        Logger.debug("saving window size: " + size.x + "x" + size.y);
        settings.setAttribute("window.width",size.x);
        settings.setAttribute("window.height",size.y);
      }
    });

  }
  
  /**
   * Liefert den Apply-Button.
   * @return der Apply-Button.
   */
  private Button getApply()
  {
    if (this.apply != null)
      return this.apply;
    
    this.apply = new Button(i18n.tr("Übernehmen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        if (handleStore())
          close();
      }
    },null,true,"ok.png");
    
    return this.apply;
  }
  
  /**
   * Liefert eine Auswahlbox fuer das Konto.
   * @return Auswahlbox.
   * @throws RemoteException
   */
  public KontoInput getKontoAuswahl() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;

    this.kontoAuswahl = new KontoInput(this.konto,KontoFilter.ACTIVE);
    this.kontoAuswahl.setSupportGroups(false);
    this.kontoAuswahl.setComment(null);
    this.kontoAuswahl.addListener(this.reloadListener);
    this.reloadListener.handleEvent(null);

    return this.kontoAuswahl;
  }
  
  /**
   * Liefert die Checkbox zum Aktivieren/Deaktivieren des unteren Limits.
   * @return Checkbox zum Aktivieren/Deaktivieren des unteren Limits.
   */
  private CheckboxInput getLowerEnabled()
  {
    if (this.lowerEnabled != null)
      return this.lowerEnabled;
    
    this.lowerEnabled = new CheckboxInput(false);
    this.lowerEnabled.setName(i18n.tr("Limit aktiviert"));
    this.lowerEnabled.addListener(this.statusListener);

    return this.lowerEnabled;
  }

  /**
   * Liefert die Checkbox zum Aktivieren/Deaktivieren des oberen Limits.
   * @return Checkbox zum Aktivieren/Deaktivieren des oberen Limits.
   */
  private CheckboxInput getUpperEnabled()
  {
    if (this.upperEnabled != null)
      return this.upperEnabled;
    
    this.upperEnabled = new CheckboxInput(false);
    this.upperEnabled.setName(i18n.tr("Limit aktiviert"));
    this.upperEnabled.addListener(this.statusListener);

    return this.upperEnabled;
  }
  
  /**
   * Liefert eine Checkbox, mit der konfiguriert werden kann, ob beim unteren Limit eine Benachrichtigung erfolgen soll.
   * @return Checkbox.
   */
  private CheckboxInput getLowerNotify()
  {
    if (this.lowerNotify != null)
      return this.lowerNotify;
    
    this.lowerNotify = new CheckboxInput(false);
    this.lowerNotify.setName(i18n.tr("Benachrichtigen, wenn Limit erreicht wird"));
    return this.lowerNotify;
  }
  
  /**
   * Liefert eine Checkbox, mit der konfiguriert werden kann, ob beim oberen Limit eine Benachrichtigung erfolgen soll.
   * @return Checkbox.
   */
  private CheckboxInput getUpperNotify()
  {
    if (this.upperNotify != null)
      return this.upperNotify;
    
    this.upperNotify = new CheckboxInput(false);
    this.upperNotify.setName(i18n.tr("Benachrichtigen, wenn Limit erreicht wird"));
    return this.upperNotify;
  }
  
  /**
   * Liefert das Eingabefeld fuer den Wert des unteren Limits.
   * @return das Eingabefeld fuer den Wert des unteren Limits.
   */
  private DecimalInput getLowerValue()
  {
    if (this.lowerValue != null)
      return this.lowerValue;
    
    this.lowerValue = new DecimalInput(HBCI.DECIMALFORMAT);
    this.lowerValue.setMandatory(true);
    this.lowerValue.setName(i18n.tr("Mindestsaldo"));
    this.lowerValue.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
    this.lowerValue.addListener(this.statusListener);
    return this.lowerValue;
  }
  
  /**
   * Liefert das Eingabefeld fuer den Wert des oberen Limits.
   * @return das Eingabefeld fuer den Wert des oberen Limits.
   */
  private DecimalInput getUpperValue()
  {
    if (this.upperValue != null)
      return this.upperValue;
    
    this.upperValue = new DecimalInput(HBCI.DECIMALFORMAT);
    this.upperValue.setMandatory(true);
    this.upperValue.setName(i18n.tr("Höchstsaldo"));
    this.upperValue.setComment(HBCIProperties.CURRENCY_DEFAULT_DE);
    this.upperValue.addListener(this.statusListener);
    return this.upperValue;
  }
  
  /**
   * Erstellt den Schieberegler fuer die Tage des unteren Limits.
   * @return Schieberegler fuer die Tage des unteren Limits.
   */
  private UmsatzDaysInput getLowerDays()
  {
    if (this.lowerDays != null)
      return this.lowerDays;
    
    this.lowerDays = new MyUmsatzDaysInput();
    this.lowerDays.setMandatory(true);
    this.lowerDays.setRememberSelection("limit.lower");
    this.lowerDays.setName(i18n.tr("Erreicht"));
    return this.lowerDays;
  }
  
  /**
   * Erstellt den Schieberegler fuer die Tage des oberen Limits.
   * @return Schieberegler fuer die Tage des oberen Limits.
   */
  private UmsatzDaysInput getUpperDays()
  {
    if (this.upperDays != null)
      return this.upperDays;
    
    this.upperDays = new MyUmsatzDaysInput();
    this.upperDays.setMandatory(true);
    this.upperDays.setRememberSelection("limit.upper");
    this.upperDays.setName(i18n.tr("Erreicht"));
    return this.upperDays;
  }
  
  /**
   * Liefert ein Label mit Hinweis-Texten zur Unterstützung des Geschaeftsvorfalls fuer das Konto.
   * @return Label.
   * @throws RemoteException
   */
  private NotificationPanel getHinweise()
  {
    if (this.hinweise != null)
      return this.hinweise;
    
    this.hinweise = new NotificationPanel();
    return this.hinweise;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected Object getData() throws Exception
  {
    return null;
  }
  
  /**
   * Ueberschrieben, um den Kommentar-Text anzupassen.
   */
  private class MyUmsatzDaysInput extends UmsatzDaysInput
  {
    /**
     * ct.
     */
    private MyUmsatzDaysInput()
    {
      super();
      this.setScaling(1,999,1,7);
    }
    
    /**
     * @see de.willuhn.jameica.gui.input.AbstractInput#setComment(java.lang.String)
     */
    @Override
    public void setComment(String comment)
    {
      int days = ((Integer)getValue()).intValue();
      if (days == 1)
      {
        super.setComment(i18n.tr("morgen"));
        return;
      }
      
      cal.setTime(DateUtil.startOfDay(new Date()));
      cal.add(Calendar.DATE,days);
      super.setComment(i18n.tr("in {0} Tagen ({1})",Integer.toString(days),HBCI.DATEFORMAT.format(cal.getTime())));
    }
  }
  
  /**
   * Laedt die Daten nach Auswahl des Kontos neu.
   */
  private class ReloadListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event)
    {
      try
      {
        Object o = getKontoAuswahl().getValue();
        final boolean haveKonto = (o instanceof Konto);
        
        getLowerEnabled().setEnabled(haveKonto);
        getUpperEnabled().setEnabled(haveKonto);
        getLowerDays().setEnabled(haveKonto);
        getUpperDays().setEnabled(haveKonto);
        getLowerValue().setEnabled(haveKonto);
        getUpperValue().setEnabled(haveKonto);
        getLowerNotify().setEnabled(haveKonto);
        getUpperNotify().setEnabled(haveKonto);
        getApply().setEnabled(haveKonto);
        
        if (!haveKonto)
        {
          getHinweise().setText(NotificationPanel.Type.INFO,i18n.tr("Bitte wählen Sie ein Konto aus"));
          getUpperEnabled().setValue(false);
          getLowerEnabled().setValue(false);
          getLowerValue().setValue(null);
          getUpperValue().setValue(null);
          getLowerDays().setValue(null);
          getUpperDays().setValue(null);
          getUpperNotify().setValue(false);
          getLowerNotify().setValue(false);
          return;
        }

        konto = (Konto) o;
        upper = ForecastCreator.getLimit(konto,SaldoLimit.Type.UPPER);
        lower = ForecastCreator.getLimit(konto,SaldoLimit.Type.LOWER);
        
        getUpperEnabled().setValue(upper.isEnabled());
        getLowerEnabled().setValue(lower.isEnabled());
        getUpperValue().setValue(upper.getValue());
        getLowerValue().setValue(lower.getValue());
        getUpperDays().setValue(upper.getDays());
        getLowerDays().setValue(lower.getDays());
        getUpperNotify().setValue(upper.isNotify());
        getLowerNotify().setValue(lower.isNotify());
        
        statusListener.handleEvent(null);
      }
      catch (RemoteException re)
      {
        Logger.error("error while reading data",re);
      }
    }
  }
  
  
  /**
   * Aktualisiert die Anzeige nach Aktivierung/Deaktivierung eines Limits.
   */
  private class StatusListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event)
    {
      final boolean haveUpper = ((Boolean) getUpperEnabled().getValue()).booleanValue();
      final boolean haveLower = ((Boolean) getLowerEnabled().getValue()).booleanValue();
      getUpperValue().setEnabled(haveUpper);
      getLowerValue().setEnabled(haveLower);
      getUpperDays().setEnabled(haveUpper);
      getLowerDays().setEnabled(haveLower);
      getUpperNotify().setEnabled(haveUpper);
      getLowerNotify().setEnabled(haveLower);
      
      final Double vu  = (Double) getUpperValue().getValue();
      final Double vl  = (Double) getLowerValue().getValue();
      getApply().setEnabled(vu != null && vl != null);
    }
  }

  /**
   * Speichert die vorgenommenen Einstellungen.
   * @return true, wenn die Einstellungen korrekt gespeichert wurden.
   */
  private boolean handleStore()
  {
    try
    {
      final Konto k = (Konto) this.getKontoAuswahl().getValue();
      if (k == null)
      {
        getHinweise().setText(NotificationPanel.Type.INFO,i18n.tr("Bitte wählen Sie ein Konto aus"));
        return false;
      }

      final Boolean eu = (Boolean) this.getUpperEnabled().getValue();
      final Boolean el = (Boolean) this.getLowerEnabled().getValue();
      final Double vu  = (Double) this.getUpperValue().getValue();
      final Double vl  = (Double) this.getLowerValue().getValue();
      
      if (eu && el)
      {
        if (vu != null && vl != null && vu.compareTo(vl) <= 0)
        {
          getHinweise().setText(NotificationPanel.Type.ERROR,i18n.tr("Das obere Limit muss größer als das untere Limit sein."));
          return false;
        }
      }
      
      upper.setEnabled(eu);
      upper.setDays((Integer) this.getUpperDays().getValue());
      upper.setValue(vu);
      upper.setNotify(((Boolean)this.getUpperNotify().getValue()));
      ForecastCreator.setLimit(upper);
      
      lower.setEnabled(el);
      lower.setDays((Integer) this.getLowerDays().getValue());
      lower.setValue(vl);
      lower.setNotify(((Boolean)this.getLowerNotify().getValue()));
      ForecastCreator.setLimit(lower);
      
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Einstellungen gespeichert"),StatusBarMessage.TYPE_SUCCESS));
      return true;
    }
    catch (Exception e)
    {
      Logger.error("error while saving settings",e);
      getHinweise().setText(NotificationPanel.Type.ERROR,e.getMessage());
      return false;
    }
  }
}
