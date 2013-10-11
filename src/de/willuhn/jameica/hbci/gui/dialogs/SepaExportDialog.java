/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.sepa.PainVersion;
import org.kapott.hbci.sepa.PainVersion.Type;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum Exportieren eines Auftrages als SEPA-XML-Datei.
 */
public class SepaExportDialog extends AbstractDialog
{
  private final static int WINDOW_WIDTH = 600;

  private final static DateFormat DATEFORMAT = new SimpleDateFormat("yyyyMMdd");
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private Type type               = null;
  private PainVersion painVersion = null;
  private File file               = null;
  private Button ok               = null;

  /**
   * ct.
   * @param type der zu exportierende PAIN-Type.
   */
  public SepaExportDialog(Type type)
  {
    super(SepaExportDialog.POSITION_CENTER);
    this.setTitle(i18n.tr("SEPA-Datei exportieren"));
    this.type = type;
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
    
    this.addCloseListener(new Listener() {
      public void handleEvent(Event event)
      {
        if (file != null)
        {
          // Wir merken uns noch das Verzeichnis vom letzten mal
          ExportDialog.SETTINGS.setAttribute("lastdir",file.getParent());
        }
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container c = new SimpleContainer(parent);
    
    final SelectInput version = this.getPainVersionInput();
    final FileInput target    = this.getFileInput();
    final LabelInput msg      = this.getMessage();
    
    c.addInput(version);
    c.addInput(target);
    c.addInput(msg);
    
    ButtonArea buttons = new ButtonArea();
    this.ok = new Button(i18n.tr("Export starten"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        painVersion = (PainVersion) version.getValue();
        if (painVersion == null)
        {
          msg.setValue(i18n.tr("Bitte wählen Sie eine Schema-Version aus."));
          return;
        }
        
        String s = StringUtils.trimToNull((String) target.getValue());
        if (s == null)
        {
          msg.setValue(i18n.tr("Bitte wählen Sie eine Datei aus"));
          return;
        }
        
        file = new File(s);
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(ok);
    
    buttons.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
    
    c.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }
  
  /**
   * Liefert ein Auswahlfeld mit der zu verwendenden PAIN-Version.
   * @return Auswahlfeld mit der PAIN-Version.
   */
  private SelectInput getPainVersionInput()
  {
    List<PainVersion> list = PainVersion.getKnownVersions(type);
    final SelectInput select = new SelectInput(list,PainVersion.findGreatest(list));
    select.setAttribute("file");
    select.setName(i18n.tr("Schema-Version der SEPA-Datei"));
    select.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        if (ok != null)
          ok.setEnabled(select.getValue() != null);
      }
    });
    return select;
  }
  
  /**
   * Liefert ein Label fuer Fehlermeldungen.
   * @return ein Label fuer Fehlermeldungen.
   */
  private LabelInput getMessage()
  {
    LabelInput label = new LabelInput("");
    label.setColor(Color.ERROR);
    label.setName("");
    return label;
  }
  
  /**
   * Liefert ein Eingabefeld mit der Ziel-Datei, in die geschrieben wird.
   * @return Eingabefeld mit der Ziel-Datei.
   */
  private FileInput getFileInput()
  {
    final String path = ExportDialog.SETTINGS.getString("lastdir",System.getProperty("user.home"));
    String name = this.type.getName();
    name = name.replace(" ","-");
    name = "hibiscus-sepa-" + name + "-" + DATEFORMAT.format(new Date()) + ".xml";
    
    File f = new File(path,name);
    
    final FileInput input = new FileInput(f.getAbsolutePath(),true)
    {
      protected void customize(FileDialog fd)
      {
        fd.setOverwrite(true);
        
        if (path != null && path.length() > 0)
          fd.setFilterPath(path);
      }
    };
    input.setName(i18n.tr("SEPA XML-Datei"));
    input.setMandatory(true);
    input.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        if (ok != null)
        {
          String s = (String) input.getValue();
          ok.setEnabled(StringUtils.trimToNull(s) != null);
        }
      }
    });
    return input;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.getFile();
  }
  
  /**
   * Liefert die ausgewaehlte Ziel-Datei.
   * @return die ausgewaehlte Ziel-Datei.
   */
  public File getFile()
  {
    return this.file;
  }
  
  /**
   * Liefert die ausgewaehlte PAIN-Version.
   * @return die ausgewaehlte PAIN-Version.
   */
  public PainVersion getPainVersion()
  {
    return this.painVersion;
  }

}


