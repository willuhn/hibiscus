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

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog, mit dem der Abruf der Umsaetze initial auf CAMT umgestellt werden kann.
 * Der Dialog liefert true oder false zurueck.
 */
public class CamtSetupDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private final static int WINDOW_WIDTH = 780;
  
  private Konto konto = null;
  private CheckboxInput switchAll = null;
  private Boolean value = null;
  
  /**
   * ct.
   * @param k das Konto.
   */
  public CamtSetupDialog(Konto k)
  {
    super(POSITION_CENTER);

    this.konto = k;
    this.setTitle(i18n.tr("Umsätze im SEPA CAMT-Format abrufen"));
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
    this.setSideImage(SWTUtil.getImage("camtsetup.png"));
    
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    final Container c = new SimpleContainer(parent);
    String text = "Hibiscus unterstützt beim Abruf der Konto-Umsätze jetzt auch das neue moderne XML-basierte CAMT-Format, welches " +
                  "für den SEPA-Zahlungsverkehr besser geeignet ist als das bisherige MT940-Format.\n\n" +
                  "Das Konto \"{0}\" kann jetzt auf CAMT umgestellt werden. Sie können diese Umstellung später in den Synchronisierungsoptionen " +
                  "des Kontos jederzeit wieder rückgängig machen.";
    c.addText(i18n.tr(text,this.konto.getLongName()),true);
    
    c.addHeadline(i18n.tr("Hinweis"));
    text = "Da sich die Datenformate von CAMT und MT940 grundlegend unterscheiden, kann es nach der Umstellung dazu kommen, dass " +
           "Umsatzbuchungen ggf. doppelt angezeigt werden. Sie können diese Duplikate einfach löschen. In der Regel sollte das jedoch " +
           "nur am Tag der Umstellung selbst auftreten.";
    c.addText(text,true);

    c.addInput(this.getSwitchAll());
    
    ButtonArea buttons = new ButtonArea();

    final Button yes = new Button(i18n.tr("Ja, auf CAMT umstellen"), new Action()
    {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        apply(true);
      }
    },null,true,"ok.png");
    buttons.addButton(yes);

    final Button no = new Button(i18n.tr("Nein, nicht umstellen"), new Action()
    {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        apply(false);
      }
    },null,true,"window-close.png");
    buttons.addButton(no);

    final Button later = new Button(i18n.tr("Beim nächsten Mal erinnern"), new Action()
    {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true,"preferences-system-time.png");
    buttons.addButton(later);
    
    c.addButtonArea(buttons);

    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }
  
  /**
   * Liefert eine Checkbox, mit der eingestellt werden kann, ob die Umstellung fuer alle Konten erfolgen soll.
   * @return Checkbox.
   */
  private CheckboxInput getSwitchAll()
  {
    if (this.switchAll != null)
      return this.switchAll;

    this.switchAll = new CheckboxInput(false);
    this.switchAll.setName(i18n.tr("Auch auf alle anderen Konten anwenden"));
    return this.switchAll;
  }
  
  /**
   * Uebernimmt die Einstellungen und schliesst den Dialog.
   * @param enabled true, wenn CAMT aktiviert werden soll.
   */
  private void apply(boolean enabled)
  {
    try
    {
      this.value        = enabled;
      final String s    = Boolean.toString(enabled);
      final Boolean all = (Boolean) this.getSwitchAll().getValue();
      
      List<Konto> konten = all.booleanValue() ? KontoUtil.getKonten(KontoFilter.ONLINE) : Arrays.asList(this.konto);
      for (Konto k:konten)
      {
        MetaKey.UMSATZ_CAMT.set(k,s);
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to save changes",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Übernehmen der Einstellungen fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
    finally
    {
      close();
    }
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected Object getData() throws Exception
  {
    return this.value;
  }
}

