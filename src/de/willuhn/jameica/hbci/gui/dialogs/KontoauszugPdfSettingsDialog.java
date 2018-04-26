/**********************************************************************
 *
 * Copyright (c) 2018 Olaf Willuhn
 * GNU GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.GV_Result.GVRKontoauszug.Format;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DirectoryInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.messaging.MessagingAvailableConsumer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.KontoauszugInterval;
import de.willuhn.jameica.hbci.server.BPDUtil;
import de.willuhn.jameica.hbci.server.BPDUtil.Query;
import de.willuhn.jameica.hbci.server.BPDUtil.Support;
import de.willuhn.jameica.hbci.server.KontoauszugPdfUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum Konfigurieren der Abruf-Einstellungen fuer den Kontoauszug.
 */
public class KontoauszugPdfSettingsDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private final static int WINDOW_WIDTH = 600;

  private Konto konto = null;
  
  private KontoInput kontoAuswahl = null;
  private LabelInput hinweise = null;
  private LabelInput hinweise2 = null;
  private SelectInput interval = null;
  private CheckboxInput ignoreFormat = null;
  private CheckboxInput markRead = null;
  private CheckboxInput sendReceipt = null;
  private LabelInput nextFetch = null;
  private CheckboxInput messaging = null;
  private DirectoryInput path = null;
  private TextInput folder = null;
  private TextInput name = null;
  private LabelInput example = null;
  private LabelInput exampleText = null;
  
  private Button apply = null;
  
  private Listener reloadListener   = new ReloadListener();
  private Listener supportListener  = new SupportListener();
  private Listener exampleListener  = new ExampleListener();
  private Listener intervalListener = new IntervalListener();

  /**
   * ct.
   * @param konto das Konto.
   */
  public KontoauszugPdfSettingsDialog(Konto konto)
  {
    super(POSITION_CENTER);
    this.konto = konto;
    this.setTitle(i18n.tr("Einstellungen für den elektronischen Kontoauszug"));
    setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    Container c = new SimpleContainer(parent);
    c.addInput(this.getKontoAuswahl());
    
    c.addInput(this.getHinweise());
    c.addInput(this.getHinweise2());
    c.addInput(this.getIgnoreFormat());
    
    c.addHeadline(i18n.tr("Konto-Synchronisation"));
    c.addText(i18n.tr("Wählen Sie das Intervall, in dem die elektronischen Kontoauszüge bei der Konto-Synchronisation mit abgerufen werden sollen. " +
                      "Sie können außerdem festlegen, ob die Kontoauszüge auch dann abgerufen werden sollen, wenn die Bank kein PDF-Format anbietet (sondern nur MT940)."),true,Color.COMMENT);
    c.addInput(this.getIntervall());
    c.addInput(this.getNextFetch());
    c.addInput(this.getSendReceipt());
    c.addInput(this.getMarkRead());
    
    c.addText("",true);
    
    c.addHeadline(i18n.tr("Ablage-Ort der Kontoauszüge"));
    c.addText(i18n.tr("Sie können die folgenden Platzhalter zur Benennung der Unterordner und Dateinamen " +
                      "verwenden:\n\n${iban}, ${bic}, ${jahr}, ${monat}, ${tag}, ${stunde}, ${minute}, ${nummer}\n\n" +
                      "Unterordner werden bei Bedarf automatisch erstellt. Lassen Sie die Vorlage für Unterordner " +
                      "alternativ leer, wenn Sie diese nutzen möchten. " +
                      "Die passende Dateiendung wird automatisch an den Dateinamen angehängt."),true, Color.COMMENT);
    if (MessagingAvailableConsumer.haveMessaging())
      c.addInput(this.getMessaging());

    
    c.addInput(this.getPath());
    c.addInput(this.getFolder());
    c.addInput(this.getName());
    
    Container c2 = new SimpleContainer(parent);
    c2.addInput(this.getExampleText());
    c2.addInput(this.getExample());
    
    Container c3 = new SimpleContainer(parent);
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(this.getApply());
    buttons.addButton(i18n.tr("Schließen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,false,"window-close.png");
    c3.addButtonArea(buttons);
    
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

    this.kontoAuswahl = new KontoInput(this.konto,KontoFilter.ONLINE);
    this.kontoAuswahl.setSupportGroups(false);
    this.kontoAuswahl.setComment(null);
    this.kontoAuswahl.addListener(this.reloadListener);

    this.reloadListener.handleEvent(null);

    return this.kontoAuswahl;
  }
  
  /**
   * Liefert ein Label mit Hinweis-Texten zur Unterstützung des Geschaeftsvorfalls fuer das Konto.
   * @return Label.
   * @throws RemoteException
   */
  private LabelInput getHinweise()
  {
    if (this.hinweise != null)
      return this.hinweise;
    
    this.hinweise = new LabelInput("");
    this.hinweise.setColor(Color.ERROR);
    this.hinweise.setName("");
    return this.hinweise;
  }
  
  /**
   * Liefert ein Label mit weiteren Hinweis-Texten zur Unterstützung des Geschaeftsvorfalls fuer das Konto.
   * @return Label.
   * @throws RemoteException
   */
  private LabelInput getHinweise2()
  {
    if (this.hinweise2 != null)
      return this.hinweise2;
    
    this.hinweise2 = new LabelInput("");
    this.hinweise2.setColor(Color.ERROR);
    this.hinweise2.setName("");
    return this.hinweise2;
  }
  
  /**
   * Liefert die Auswahl fuer das Abruf-Intervall.
   * @return die Auswahl fuer das Abruf-Intervall.
   * @throws RemoteException
   */
  private SelectInput getIntervall()
  {
    if (this.interval != null)
      return this.interval;
    
    this.interval = new SelectInput(KontoauszugInterval.KNOWN,null);
    this.interval.setName(MetaKey.KONTOAUSZUG_INTERVAL.getDescription());
    this.interval.setAttribute("name");
    this.interval.setComment("");
    this.interval.addListener(this.intervalListener);
    return this.interval;
  }
  
  /**
   * Liefert eine Checkbox, mit der das Format ignoriert werden kann.
   * @return eine Checkbox, mit der das Format ignoriert werden kann.
   */
  private CheckboxInput getIgnoreFormat()
  {
    if (this.ignoreFormat != null)
      return this.ignoreFormat;
    
    this.ignoreFormat = new CheckboxInput(false);
    this.ignoreFormat.setName(MetaKey.KONTOAUSZUG_IGNORE_FORMAT.getDescription());
    this.ignoreFormat.addListener(this.supportListener);
    return this.ignoreFormat;
  }

  /**
   * Liefert eine Checkbox, mit der festgelegt werden kann, dass die Kontoauszuege automatisch als gelesen markiert werden.
   * @return eine Checkbox, mit der festgelegt werden kann, dass die Kontoauszuege automatisch als gelesen markiert werden.
   */
  private CheckboxInput getMarkRead()
  {
    if (this.markRead != null)
      return this.markRead;
    
    this.markRead = new CheckboxInput(false);
    this.markRead.setName(MetaKey.KONTOAUSZUG_MARK_READ.getDescription());
    return this.markRead;
  }

  /**
   * Liefert eine Checkbox, mit der festgelegt werden kann, ob die Empfangsquittung automatisch an die Bank gesendet werden soll.
   * @return eine Checkbox, mit der festgelegt werden kann, ob die Empfangsquittung automatisch an die Bank gesendet werden soll.
   */
  private CheckboxInput getSendReceipt()
  {
    if (this.sendReceipt != null)
      return this.sendReceipt;
    
    this.sendReceipt = new CheckboxInput(false);
    this.sendReceipt.setName(MetaKey.KONTOAUSZUG_SEND_RECEIPT.getDescription());
    return this.sendReceipt;
  }

  /**
   * Liefert ein Label mit dem Datum des naechsten Abrufs der Kontoauszuege.
   * @return Label mit dem Datum des naechsten Abrufs der Kontoauszuege.
   */
  private LabelInput getNextFetch()
  {
    if (this.nextFetch != null)
      return this.nextFetch;
    
    this.nextFetch = new LabelInput("");
    this.nextFetch.setName(i18n.tr("Nächste Abruf"));
    this.nextFetch.setComment("");
    return this.nextFetch;
  }
  
  /**
   * Liefert eine Checkbox, mit der ausgewaehlt werden kann, ob die Kontoauszuege
   * per Messaging gespeichert werden sollen.
   * @return Checkbox.
   */
  private CheckboxInput getMessaging()
  {
    if (this.messaging != null)
      return this.messaging;
    
    if (!MessagingAvailableConsumer.haveMessaging())
      return null;

    this.messaging = new CheckboxInput(false);
    this.messaging.setName(MetaKey.KONTOAUSZUG_STORE_MESSAGING.getDescription());
    
    Listener l = new Listener() {
      @Override
      public void handleEvent(Event event)
      {
        Boolean checked = (Boolean) getMessaging().getValue();
        getPath().setEnabled(!checked);
        getFolder().setEnabled(!checked);
        getName().setEnabled(!checked);
        
        if (checked)
        {
          getExample().setValue("");
          getExampleText().setValue("");
        }
        else
        {
          exampleListener.handleEvent(null);
        }
      }
    };
    this.messaging.addListener(l);
    l.handleEvent(null);
    
    return this.messaging;
  }
  
  /**
   * Liefert ein Eingabefeld fuer den Ordner, in dem die Kontoauszuege gespeichert werden sollen.
   * @return Eingabefeld fuer den Ordner, in dem die Kontoauszuege gespeichert werden sollen.
   */
  private DirectoryInput getPath()
  {
    if (this.path != null)
      return this.path;
    
    this.path = new DirectoryInput("");
    this.path.setName(MetaKey.KONTOAUSZUG_STORE_PATH.getDescription());
    this.path.addListener(this.exampleListener);
    return this.path;
  }
  
  /**
   * Liefert ein Eingabefeld fuer den Unter-Ordner.
   * @return Eingabefeld fuer den Unter-Ordner.
   */
  private TextInput getFolder()
  {
    if (this.folder != null)
      return this.folder;
    
    this.folder = new TextInput("");
    this.folder.setName(MetaKey.KONTOAUSZUG_TEMPLATE_PATH.getDescription());
    this.folder.addListener(this.exampleListener);
    return this.folder;
  }
  
  /**
   * Liefert ein Eingabefeld fuer den Dateinamen.
   * @return Eingabefeld fuer den Dateinamen.
   */
  private TextInput getName()
  {
    if (this.name != null)
      return this.name;
    
    this.name = new TextInput("");
    this.name.setName(MetaKey.KONTOAUSZUG_TEMPLATE_NAME.getDescription());
    this.name.addListener(this.exampleListener);
    return this.name;
  }
  
  /**
   * Liefert ein Label mit einem Beispiel-Dateipfad.
   * @return Label mit einem Beispiel-Dateipfad.
   */
  private LabelInput getExample()
  {
    if (this.example != null)
      return this.example;

    this.example = new LabelInput("");
    this.example.addListener(this.exampleListener);
    
    this.exampleListener.handleEvent(null);
    
    return this.example;
  }

  /**
   * Liefert ein Label mit einem Hinweis-Text zum Beispiel.
   * @return Label mit einem Hinweis-Text.
   */
  private LabelInput getExampleText()
  {
    if (this.exampleText != null)
      return this.exampleText;

    this.exampleText = new LabelInput("");
    this.exampleText.setName("");
    
    return this.getExampleText();
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
   * Markiert das Formular als ungueltig.
   * @param dirty true, wenn das Formular als ungueltig gelten soll.
   */
  private void markDirty(boolean dirty)
  {
    this.getApply().setEnabled(!dirty);
  }
  
  /**
   * Markiert das ganze Formular als unterstuetzt.
   * @param supported true, wenn alles als unterstuetzt markiert werden soll.
   * @param conditional true, wenn noch die Option "Format ignorieren" erlaubt sein soll
   */
  private void markSupported(boolean supported, boolean conditional)
  {
    boolean support = supported || (Boolean) getIgnoreFormat().getValue();
    this.getIntervall().setEnabled(support);
    
    this.getIgnoreFormat().setEnabled(conditional);

    CheckboxInput messaging = this.getMessaging();
    if (messaging != null && (Boolean) messaging.getValue())
    {
      // Wir haben Messaging aktiv. Dann bleiben die Pfade ohnehin deaktiviert, egal,
      // was eingestellt ist.
      this.getPath().setEnabled(false);
      this.getFolder().setEnabled(false);
      this.getName().setEnabled(false);
      this.getMarkRead().setEnabled(false);
      this.getSendReceipt().setEnabled(false);
    }
    else
    {
      this.getPath().setEnabled(support);
      this.getFolder().setEnabled(support);
      this.getName().setEnabled(support);
      this.getMarkRead().setEnabled(support);
      this.getSendReceipt().setEnabled(support);
    }
    
    if (messaging != null)
      messaging.setEnabled(support);
  }
  
  /**
   * Listener, der das Pfad-Beispiel ermittelt.
   */
  private class ExampleListener implements Listener
  {
    @Override
    public void handleEvent(Event event)
    {
      LabelInput label   = getExample();
      LabelInput comment = getExampleText();

      try
      {
        String path   = (String) getPath().getValue();
        String folder = (String) getFolder().getValue();
        String name   = (String) getName().getValue();
        
        String s = KontoauszugPdfUtil.createPath(konto,null,path,folder,name);

        label.setValue(s);
        
        
        if (s.contains("{") || s.contains("}") || s.contains("$"))
        {
          comment.setValue(i18n.tr("Definition der Platzhalter ungültig"));
          comment.setColor(Color.ERROR);
          label.setColor(Color.FOREGROUND);
          markDirty(true);
        }
        else
        {
          comment.setValue("");
          label.setColor(Color.SUCCESS);
          markDirty(false);
        }
      }
      catch (ApplicationException ae)
      {
        comment.setValue(ae.getMessage());
        comment.setColor(Color.ERROR);
        label.setValue("");
        label.setColor(Color.FOREGROUND);
        markDirty(true);
      }
      catch (RemoteException re)
      {
        Logger.error("unable to create path",re);
        comment.setValue(i18n.tr("Pfad-Angabe ungültig"));
        comment.setColor(Color.ERROR);
        label.setValue("");
        label.setColor(Color.FOREGROUND);
        markDirty(true);
      }
    }
  }
  
  /**
   * Listener, der das Datum des naechsten Intervall ermittelt.
   */
  private class IntervalListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event)
    {
      String s = null;
      Date last = null;
      try
      {
        s = MetaKey.KONTOAUSZUG_INTERVAL_LAST.get(konto);
        if (s != null && s.length() > 0)
          last = HBCI.LONGDATEFORMAT.parse(s);
      }
      catch (Exception e)
      {
        Logger.error("unable to parse date: " + s, e);
      }
      KontoauszugInterval i = (KontoauszugInterval) getIntervall().getValue();
      Date next = i.getNextInterval(last);
      LabelInput l = getNextFetch();
      
      if (next == null)
      {
        l.setColor(Color.COMMENT);
        l.setValue("-");
      }
      else if (!next.after(new Date()))
      {
        l.setColor(Color.SUCCESS);
        l.setValue(i18n.tr("Bei der nächsten Synchronisation"));
      }
      else
      {
        l.setColor(Color.SUCCESS);
        l.setValue(HBCI.DATEFORMAT.format(next));
      }
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
        if (!(o instanceof Konto))
        {
          final LabelInput hinweise = getHinweise();
          hinweise.setValue(i18n.tr("Bitte wählen Sie ein Konto aus"));
          getHinweise2().setValue("");
          markSupported(false,false);
          return;
        }

        getHinweise2().setValue("");

        konto = (Konto) o;
        
        getIntervall().setValue(KontoauszugInterval.find(MetaKey.KONTOAUSZUG_INTERVAL.get(konto)));
        getIgnoreFormat().setValue(Boolean.parseBoolean(MetaKey.KONTOAUSZUG_IGNORE_FORMAT.get(konto)));
        getMarkRead().setValue(Boolean.parseBoolean(MetaKey.KONTOAUSZUG_MARK_READ.get(konto)));
        getSendReceipt().setValue(Boolean.parseBoolean(MetaKey.KONTOAUSZUG_SEND_RECEIPT.get(konto)));
        
        String s = MetaKey.KONTOAUSZUG_INTERVAL_LAST.get(konto);
        getIntervall().setComment(i18n.tr("Letzter Abruf: {0}",s != null ? s : i18n.tr("nie")));
        
        CheckboxInput messaging = getMessaging();
        if (messaging != null)
          getMessaging().setValue(Boolean.parseBoolean(MetaKey.KONTOAUSZUG_STORE_MESSAGING.get(konto)));

        getPath().setValue(MetaKey.KONTOAUSZUG_STORE_PATH.get(konto));
        getFolder().setValue(MetaKey.KONTOAUSZUG_TEMPLATE_PATH.get(konto));
        getName().setValue(MetaKey.KONTOAUSZUG_TEMPLATE_NAME.get(konto));
        
        intervalListener.handleEvent(null);
        exampleListener.handleEvent(null);
        supportListener.handleEvent(null);
      }
      catch (RemoteException re)
      {
        Logger.error("error while reading data",re);
      }
    }
  }
  
  /**
   * Aktualisiert den Support je nach Auswahl der Parameter.
   */
  private class SupportListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event)
    {
      boolean ok = false;
      String text1 = null;
      String text2 = null;
      
      if (konto != null)
      {
        final Support hkekp = BPDUtil.getSupport(konto,Query.KontoauszugPdf);
        final Support hkeka = BPDUtil.getSupport(konto,Query.Kontoauszug);
        
        ok = (hkekp != null && hkekp.isSupported());
        if (ok)
          text1 = "HKEKP: Von Bank unterstützt und für Konto freigeschaltet";
        
        // Haben wir wenigstens PDF-Format bei HKEKA?
        if (!ok)
        {
          ok = hkeka != null && KontoauszugPdfUtil.getFormats(hkeka.getBpd()).contains(Format.PDF);
          if (ok)
            text2 = "HKEKA: Von Bank unterstützt, freigeschaltet und PDF-Format vorhanden";
        }
        
        if (!ok)
        {
          // Checken, ob wir den Grund genauer rausfinden koennen.
          
          // Haeufiger Grund bei Sparkassen. HKEKP wird zwar unterstuetzt, ist aber laut UPD nicht freigeschaltet
          if (hkekp != null)
          {
            if (!hkekp.getBpdSupport())
              text1 = "HKEKP: Bank unterstützt Geschäftsvorfall nicht";
            else
              text1 = "HKEKP: Von Bank unterstützt, jedoch nicht für Konto freigeschaltet";
          }

          if (hkeka != null)
          {
            if (!hkeka.getBpdSupport())
              text2 = "HKEKA: Bank unterstützt Geschäftsvorfall nicht";
            else if (!hkeka.getUpdSupport())
              text2 = "HKEKA: Von Bank unterstützt, jedoch nicht für Konto freigeschaltet";
            else if (!KontoauszugPdfUtil.getFormats(hkeka.getBpd()).contains(Format.PDF))
              text2 = "HKEKA: Von Bank unterstützt, jedoch nicht im PDF-Format";
          }
        }
        
      }

      final LabelInput hinweise  = getHinweise();
      final LabelInput hinweise2 = getHinweise2();

      markSupported(ok,true);
      
      hinweise.setColor(ok ? Color.SUCCESS : Color.ERROR);
      hinweise2.setColor(ok ? Color.SUCCESS : Color.ERROR);
      hinweise.setValue(StringUtils.trimToEmpty(text1));
      hinweise2.setValue(StringUtils.trimToEmpty(text2));
    }
  }
  
  /**
   * Speichert die vorgenommenen Einstellungen.
   */
  private void handleStore()
  {
    try
    {
      MetaKey.KONTOAUSZUG_IGNORE_FORMAT.set(this.konto,Boolean.toString((Boolean) this.getIgnoreFormat().getValue()));
      MetaKey.KONTOAUSZUG_INTERVAL.set(this.konto,((KontoauszugInterval) this.getIntervall().getValue()).getId());
      
      CheckboxInput messaging = this.getMessaging();
      if (messaging != null)
        MetaKey.KONTOAUSZUG_STORE_MESSAGING.set(this.konto,Boolean.toString((Boolean) messaging.getValue()));
      MetaKey.KONTOAUSZUG_STORE_PATH.set(this.konto, (String) this.getPath().getValue());
      MetaKey.KONTOAUSZUG_TEMPLATE_PATH.set(this.konto, (String) this.getFolder().getValue());
      MetaKey.KONTOAUSZUG_TEMPLATE_NAME.set(this.konto, (String) this.getName().getValue());

      MetaKey.KONTOAUSZUG_MARK_READ.set(this.konto,Boolean.toString((Boolean) getMarkRead().getValue()));
      MetaKey.KONTOAUSZUG_SEND_RECEIPT.set(this.konto,Boolean.toString((Boolean) getSendReceipt().getValue()));

      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Einstellungen gespeichert"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (RemoteException re)
    {
      Logger.error("error while saving settings",re);
    }
  }

}


