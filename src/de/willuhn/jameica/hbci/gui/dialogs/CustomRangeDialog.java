/**********************************************************************
 *
 * Copyright (c) 2024 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.input.UmsatzDaysInput;
import de.willuhn.jameica.hbci.server.Range;
import de.willuhn.jameica.hbci.server.Range.Category;
import de.willuhn.jameica.hbci.server.Range.CustomRange;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum Konfigurieren eines benutzerspezifischen Zeitraumes.
 */
public class CustomRangeDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private final static int WINDOW_WIDTH = 500;
  
  private Calendar cal = Calendar.getInstance();
  
  private Category category = null;
  private CustomRange range = null;

  private TextInput name = null;
  private UmsatzDaysInput daysPast = null;
  private UmsatzDaysInput daysFuture = null;

  private Button apply = null;

  /**
   * ct.
   * @param category die Kategorie.
   * @param range der Zeitraum.
   */
  public CustomRangeDialog(Category category,CustomRange range)
  {
    super(POSITION_CENTER);
    this.range = range;
    this.category = category;
    this.setTitle(i18n.tr("Benutzerdefinierter Zeitraum"));
    setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    final Container c = new SimpleContainer(parent);
    
    c.addText(i18n.tr("Bitte geben Sie einen Namen für den benutzerdefinierten Zeitraum ein und wählen Sie dann aus, wie weit dieser in die Vergangenheit und Zukunft reichen soll."),true);

    c.addInput(this.getName());
    c.addInput(this.getDaysPast());
    c.addInput(this.getDaysFuture());
    
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
        handleStore();
        close();
      }
    },null,true,"ok.png");
    
    return this.apply;
  }
  
  /**
   * Liefert das Eingabefeld für den Namen.
   * @return Eingabefeld für den Namen.
   */
  private TextInput getName()
  {
    if (this.name != null)
      return this.name;
    
    this.name = new TextInput(this.range.toString());
    this.name.setMandatory(true);
    this.name.setName(i18n.tr("Bezeichnung"));
    this.name.setMaxLength(100);
    this.name.setValue(this.range.toString());
    this.name.addListener(e -> this.getApply().setEnabled(StringUtils.trimToNull((String)this.getName().getValue()) != null));
    return this.name;
  }
  
  /**
   * Liefert den Schieberegler für die Tage in der Vergangenheit.
   * @return Schieberegler fuer die Tage in der Vergangenheit.
   */
  private UmsatzDaysInput getDaysPast()
  {
    if (this.daysPast != null)
      return this.daysPast;
    
    this.daysPast = new MyUmsatzDaysInput(false);
    this.daysPast.setName(i18n.tr("Beginn des Zeitraumes"));
    this.daysPast.setValue(this.range.getDaysPast());
    return this.daysPast;
  }
  
  /**
   * Liefert den Schieberegler für die Tage in der Zukunft.
   * @return Schieberegler fuer die Tage in der Zukunft.
   */
  private UmsatzDaysInput getDaysFuture()
  {
    if (this.daysFuture != null)
      return this.daysFuture;
    
    this.daysFuture = new MyUmsatzDaysInput(true);
    this.daysFuture.setName(i18n.tr("Ende des Zeitraumes"));
    this.daysFuture.setValue(this.range.getDaysFuture());
    return this.daysFuture;
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
    private boolean future = false;
    
    /**
     * ct.
     * @param future true, wenn der Regler für die Zukunft ist, false, wenn er für die Vergangenheit ist.
     */
    private MyUmsatzDaysInput(boolean future)
    {
      super();
      this.future = future;
      
      this.setScaling(1,getUndefinedMax()-1,1,7);
    }
    
    /**
     * @see de.willuhn.jameica.hbci.gui.input.UmsatzDaysInput#getUndefinedMax()
     */
    @Override
    protected int getUndefinedMax()
    {
      return (10 * 365) + 1;
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
        super.setComment(future ? i18n.tr("morgen") : i18n.tr("gestern"));
        return;
      }
      
      cal.setTime(DateUtil.startOfDay(new Date()));
      cal.add(Calendar.DATE,future ? days : -days);
      super.setComment(i18n.tr("{0} {1} Tagen ({2})",future ? "in" : "vor",Integer.toString(days),HBCI.DATEFORMAT.format(cal.getTime())));
    }
  }
  
  /**
   * Speichert die vorgenommenen Einstellungen.
   */
  private void handleStore()
  {
    try
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Einstellungen gespeichert"),StatusBarMessage.TYPE_SUCCESS));
      this.range.setName((String)this.getName().getValue());
      this.range.setDaysFuture((Integer)this.getDaysFuture().getValue());
      this.range.setDaysPast((Integer)this.getDaysPast().getValue());
      Range.saveCustomRange(this.category,this.range);
    }
    catch (Exception e)
    {
      Logger.error("error while saving settings",e);
    }
  }
}
