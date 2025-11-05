/**********************************************************************
 *
 * Copyright (c) 2019 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.kapott.hbci.GV_Result.GVRVoP.VoPResult;
import org.kapott.hbci.GV_Result.GVRVoP.VoPResultItem;
import org.kapott.hbci.GV_Result.GVRVoP.VoPStatus;

import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransferBuchung;
import de.willuhn.jameica.hbci.server.AbstractHibiscusTransferImpl;
import de.willuhn.jameica.hbci.server.AbstractSepaSammelTransferImpl;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Abfrage des VoP-Ergebnisses.
 */
public class VoPResultDialog extends AbstractDialog<Boolean>
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private final static int WINDOW_WIDTH  = 800;
  private final static int WINDOW_HEIGHT = 600;

  private String text            = null;
  private VoPResult result       = null;
  private TablePart entries      = null;
  private Button apply           = null;
  private Button save           = null;
  private Boolean choice         = Boolean.FALSE;
  private String id              = null;
  private String typ             = null;

  /**
   * ct.
   * @param text der von der Bank gemeldete Text.
   * @param result das Ergebnis der VoP-Prüfung.
   * @param indentifyer typ und id des Jobs 
   */
  public VoPResultDialog(String text, VoPResult result, String indentifyer)
  {
    super(VoPResultDialog.POSITION_CENTER);
    this.text = text != null && text.length() > 0 ? text : result.getText();
    this.result = result;
    typ = indentifyer.substring(0,indentifyer.indexOf(":"));
    id = indentifyer.substring(indentifyer.indexOf(":")+1);
    this.setTitle(i18n.tr(this.result.getItems().size() > 1 ? "Namensabgleich der Empfänger" : "Namensabgleich des Empfängers"));
    this.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent,true,1);
    container.addText(i18n.tr(this.getIntroText()) + "\n",true);
    
    if (this.text != null && this.text.trim().length() > 0)
    {
      container.addHeadline(i18n.tr("Informationstext der Bank"));
      boolean showPlain = true;
      if (this.text.matches(".*?<[^>]+>.*?"))
      {
        final FormTextPart formText = new CustomFormTextPart();
        final String s = this.formatText(this.text);
        formText.setText(s);
        try
        {
          formText.paint(container.getComposite());
          showPlain = false;
        }
        catch (Exception e)
        {
          Logger.error("unable to show formatted text - fallback to plain text: " + s,e);
        }
      }

      // Nur anzeigen, wenn wir entweder keinen formatierten Text haben oder die Formatierung fehlschlug
      if (showPlain)
        container.addText(this.text,true);
    }

    container.addHeadline(i18n.tr("Ergebnis des Namensabgleichs"));
    container.addPart(this.getEntries());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(getApply());
    
    //Save Button nur anzeigen, wenn mindestens ein CloseMatch existiert
    for(VoPResultItem item:this.result.getItems())
    {
      if(item.getStatus().equals(VoPStatus.CLOSE_MATCH))
      {
        buttons.addButton(getSaveChanges());
        break;
      }
    }
    buttons.addButton(new Cancel());
    container.addButtonArea(buttons);
    
    this.getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,WINDOW_HEIGHT));
  }
  
  /**
   * Liefert den Apply-Button.
   * @return der Apply-Button.
   */
  private Button getApply()
  {
    if (this.apply != null)
      return this.apply;
    
    this.apply = new Button(i18n.tr("Auftrag dennoch ausführen"),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        choice = Boolean.TRUE;
        close();
      }
    },null,false,"ok.png");
    
    return this.apply;
  }

  /**
   * Liefert den Änderungen-Übernehmen-Button.
   * @return der Änderungen-Übernehmen-Button.
   */
  private Button getSaveChanges()
  {
    if (this.save != null)
      return this.save;
    
    this.save = new Button(i18n.tr("Änderungen übernehmen und Abbrechen"),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          YesNoDialog yesNoDialog = new YesNoDialog(POSITION_CENTER);
          yesNoDialog.setTitle("Änderungen speichern");
          yesNoDialog.setText(
              "Sind Sie sicher, dass sie den Änderungsvorschlag in den Auftrag übernehmen\n"
              + "und die Ausführung anschließend abbrechen möchten?\n"
              + "Der Auftrag muss anschließend erneut an die Bank gesendet werden.");
          if(!(boolean) yesNoDialog.open())
          {
            return;
          }
          Object o = Settings.getDBService().createObject((Class<? extends DBObject>) Class.forName(typ), id);
          if (o instanceof AbstractHibiscusTransferImpl)
          {
            AbstractHibiscusTransferImpl job = (AbstractHibiscusTransferImpl) o;

            VoPResultItem item = result.getItems().get(0);
            if (!item.getName().isBlank()
                && job.getGegenkontoNummer().equals(item.getIban()) 
                && job.getGegenkontoName().equals(item.getOriginal()) 
                && job.getBetrag() == item.getAmount().doubleValue())
            {
              job.setGegenkontoName(item.getName());
              job.store();
            }
          } 
          else if (o instanceof AbstractSepaSammelTransferImpl)
          {
            AbstractSepaSammelTransferImpl job = (AbstractSepaSammelTransferImpl) o;
            for ( Object b : job.getBuchungen())
            {
              SepaSammelTransferBuchung sammelJob = (SepaSammelTransferBuchung) b;
              for (VoPResultItem item : result.getItems())
              {
                if (!item.getName().isBlank()
                    && sammelJob.getGegenkontoNummer().equals(item.getIban()) 
                    && sammelJob.getGegenkontoName().equals(item.getOriginal()) 
                    && sammelJob.getBetrag() == item.getAmount().doubleValue())
                {
                  sammelJob.setGegenkontoName(item.getName());
                  sammelJob.store();
                  break;
                }
              }
            }
          }
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Änderungen gespeichert"),StatusBarMessage.TYPE_SUCCESS));
          GUI.getCurrentView().reload();
          throw new OperationCanceledException();
        } 
        catch(OperationCanceledException oce)
        {
          throw oce;
        }
        catch (Exception e)
        {
          Logger.error("unable to get task object", e);
        }
      }
    },null,false,"document-save.png");
    
    return this.save;
  }
  
  /**
   * Liefert die Liste der Namen.
   * @return Liste der abhaengigen Daten.
   * @throws RemoteException
   */
  private TablePart getEntries() throws RemoteException
  {
    if (this.entries != null)
      return this.entries;
    
    this.entries = new CustomTablePart(this.result.getItems(),null);
    this.entries.setRememberColWidths(true);
    this.entries.setRememberState(false);
    this.entries.setRememberOrder(true);
    this.entries.setMulti(false);
    this.entries.removeFeature(FeatureSummary.class);
    this.entries.addColumn(i18n.tr("Status"),"status",o -> ((VoPStatus)o).getDescription());
    this.entries.addColumn(i18n.tr("Name laut Auftrag"),"original");
    this.entries.addColumn(i18n.tr("Name laut Bank"),"name");
    this.entries.addColumn(i18n.tr("Bemerkung"),"text");
    this.entries.addColumn(i18n.tr("IBAN"),"iban");
    this.entries.addColumn(i18n.tr("Betrag"),"amount",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT),false,Column.ALIGN_RIGHT);
    this.entries.addColumn(i18n.tr("Verwendungszweck"),"usage");
    
    this.entries.setFormatter(new TableFormatter() {
      
      @Override
      public void format(TableItem item)
      {
        VoPResultItem i = (VoPResultItem) item.getData();
        if (i == null)
          return;
        
        final VoPStatus status = i.getStatus();
        
        Font font = Font.DEFAULT;
        Color fg = Color.FOREGROUND;
        String icon = null;
        
        if (status == VoPStatus.MATCH)
        {
          fg = Color.SUCCESS;
          icon = "emblem-default.png";
        }
        else if (status == VoPStatus.NO_MATCH)
        {
          font = Font.BOLD;
          fg = Color.ERROR;
          icon = "emblem-important.png";
        }
        else if (status == VoPStatus.CLOSE_MATCH)
        {
          icon = "gtk-info.png";
        }

        item.setFont(font.getSWTFont());
        item.setForeground(fg.getSWTColor());
        item.setImage(0,icon != null ? SWTUtil.getImage(icon) : null);
      }
    });
    
    return this.entries;
  }
  
  /**
   * Überschrieben, um die Höhe des Part zu beeinflussen.
   */
  private class CustomFormTextPart extends FormTextPart
  {
    /**
     * @see de.willuhn.jameica.gui.parts.FormTextPart#paint(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void paint(Composite parent) throws RemoteException
    {
      super.paint(parent);
      final GridData gd = (GridData) parent.getLayoutData();
      gd.heightHint = 200;
    }
  }
  
  /**
   * Überschrieben, um die Höhe des Part zu beeinflussen.
   */
  private class CustomTablePart extends TablePart
  {
    /**
     * ct.
     * @param list
     * @param action
     */
    public CustomTablePart(List list, Action action)
    {
      super(list, action);
    }

    /**
     * @see de.willuhn.jameica.gui.parts.TablePart#paint(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public synchronized void paint(Composite parent) throws RemoteException
    {
      super.paint(parent);
      final GridData gd = (GridData) parent.getLayoutData();
      gd.heightHint = 400;
    }
  }
  
  
  /**
   * Liefert den anzuzeigenden Intro-Text.
   * @return der Intro-Text.
   */
  private String getIntroText()
  {
    if (this.result.getItems().size() > 1)
    {
      return "Nicht alle Empfängernamen des Auftrages konnten eindeutig geprüft werden. " + 
              "Bitte überprüfen Sie die Abweichungen und entscheiden Sie, ob der Auftrag dennoch ausgeführt werden soll.";
    }
    return "Der Empfängername des Auftrages konnten nicht eindeutig geprüft werden. " + 
            "Bitte überprüfen Sie die Abweichung und entscheiden Sie, ob der Auftrag dennoch ausgeführt werden soll.";
  }

  /**
   * Übernimmt eine Vor-Formatierung des Textes.
   * @param text der Text.
   * @return der formatierte Text.
   */
  private String formatText(String text)
  {
    text = text.replaceAll("<br>","<br/>");
    text = text.replaceAll("<p>","<br/><br/>"); // wir haben kein schließendes "<p>" - daher setzen gegen 2 Zeilenumbrüche
    text = text.replaceAll("<ol>",""); // <ol> und <ul> unterstützen wir nicht sondern nur "<li>".
    text = text.replaceAll("</ol>","");
    text = text.replaceAll("<ul>","");
    text = text.replaceAll("</ul>","");
    text = text.replaceAll("<i>","");
    text = text.replaceAll("</i>","");
    text = text.replaceAll("<b>","");
    text = text.replaceAll("</b>","");
    text = text.replaceAll("&","&amp;");
    
    return "<form>" + text + "</form>";
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected Boolean getData() throws Exception
  {
    return choice;
  }
}
