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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.NotificationPanel;
import de.willuhn.jameica.gui.parts.NotificationPanel.Type;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCITraceMessage;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCITraceMessageConsumer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog zum Auswaehlen des Kontos und der Zieldatei fuer den HBCI-Trace.
 */
public class HBCITraceDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static Settings settings = new Settings(HBCITraceDialog.class);
  private final static DateFormat DF = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
  
  private NotificationPanel panel = null;
	private Button apply            = null;
	private KontoInput auswahl      = null;
	private FileInput file          = null;
  
  /**
   * ct.
   * @param position
   */
  public HBCITraceDialog(int position)
  {
    super(position);
    this.setTitle(i18n.tr("HBCI-Protokoll speichern..."));
    this.setSize(400,SWT.DEFAULT);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    this.panel = new NotificationPanel();
    this.panel.paint(parent);
    this.panel.setText(Type.INFO,i18n.tr("Wählen Sie ein Konto aus, um nur dessen Protokoll zu speichern."));
    
    Container group = new SimpleContainer(parent);
    group.addHeadline(i18n.tr("Wichtiger Hinweis"));
    group.addText(i18n.tr("Das HBCI-Protokoll kann streng vertrauliche Informationen wie z.Bsp. Ihre PIN enthalten. Veröffentlichen Sie das Protokoll daher niemals " +
    		                  "in einem Forum bzw. versenden Sie es nicht per E-Mail. Öffnen Sie die Datei ggf. in einem Texteditor und schwärzen Sie darin enthaltene " +
    		                  "sensible Daten.\n"),true, Color.ERROR);
    
    group.addText(i18n.tr("Klicken Sie nach Auswahl des Kontos bitte auf \"Speichern\", um die HBCI-Protokolle des Kontos in der angegebenen Datei zu speichern."),true);
    group.addInput(this.getKontoAuswahl());
    group.addInput(this.getFile());
    
    // Button-Area
		ButtonArea b = new ButtonArea();
		b.addButton(this.getApplyButton());
		b.addButton(i18n.tr("Abbrechen"), new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
				throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
		group.addButtonArea(b);
		
    getShell().setMinimumSize(getShell().computeSize(400,SWT.DEFAULT));
    getKontoAuswahl().focus(); // damit wir direkt mit dem Cursor die Auswahl treffen koennen
  }

  @Override
  protected Object getData() throws Exception
  {
    return null;
  }
  
  /**
   * Liefert die Auswahlbox fuer das Konto.
   * @return die Auswahlbox fuer das Konto.
   * @throws RemoteException
   */
  private KontoInput getKontoAuswahl() throws RemoteException
  {
    if (this.auswahl != null)
      return this.auswahl;

    this.auswahl = new KontoInput(null,KontoFilter.ONLINE);
    this.auswahl.setSupportGroups(false);
    this.auswahl.setPleaseChoose("<" + i18n.tr("ohne Bezug zu einem Konto") + ">");
    this.auswahl.setComment(null);
    return this.auswahl;
  }
  
  /**
   * Liefert die Auswahl der Zieldatei.
   * @return die Auswahl der Zieldatei.
   */
  private FileInput getFile()
  {
    if (this.file != null)
      return this.file;
    
    // Letztes Verzeichnis ermitteln
    String dir  = settings.getString("lastdir",System.getProperty("user.home"));
    String file = "hbcitrace_" + DF.format(new Date()) + ".log";
    this.file = new FileInput(new File(dir,file).getPath(),true)
    {
      @Override
      protected void customize(FileDialog fd)
      {
        fd.setOverwrite(true);
      }
    };
    this.file.setMandatory(true);
    this.file.setName(i18n.tr("Protokoll speichern in"));
    return this.file;
  }
  
  /**
   * Exportiert das Protokoll.
   * @return true, wenn das Exportieren erfolgreich war.
   */
  private boolean export()
  {
    try
    {
      String file = (String) getFile().getValue();
      if (StringUtils.isEmpty(file))
        throw new ApplicationException(i18n.tr("Bitte wählen Sie eine Datei aus."));
      
      File f = new File(file);
      File dir = f.getParentFile();
      if (!dir.canWrite())
        throw new ApplicationException(i18n.tr("Keine Schreibrechte in diesem Ordner."));
      
      settings.setAttribute("lastdir",dir.getPath());
      
      Konto konto = (Konto) getKontoAuswahl().getValue();
      
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      HBCITraceMessageConsumer tracer = service.get(HBCITraceMessageConsumer.class);
      List<HBCITraceMessage> messages = tracer.getTrace(konto != null ? konto.getID() : null);
      if (messages == null || messages.size() == 0)
      {
        if (konto != null)
          throw new ApplicationException(i18n.tr("Keine HBCI-Protokolle zu diesem Konto vorhanden"));
        else
          throw new ApplicationException(i18n.tr("Keine HBCI-Protokolle ohne Konto-Bezug vorhanden"));
      }
      
      OutputStream os = null;
      String wrap = System.getProperty("line.separator","\n");
      try
      {
        os = new BufferedOutputStream(new FileOutputStream(f));
        for (HBCITraceMessage m:messages)
        {
          os.write(m.getData().getBytes());
          os.write(wrap.getBytes());
        }
      }
      finally
      {
        IOUtil.close(os);
      }
      
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("HBCI-Protokoll gespeichert"),StatusBarMessage.TYPE_SUCCESS));
      return true;
    }
    catch (ApplicationException ae)
    {
      panel.setText(Type.ERROR,ae.getMessage());
    }
    catch (OperationCanceledException oce)
    {
    }
    catch (Exception e)
    {
      panel.setText(Type.ERROR,i18n.tr("Fehler: {0}",e.getMessage()));
    }
    
    return false;
  }
  
  /**
   * Liefert den Uebernehmen-Button.
   * @return der Uebernehmen-Button.
   */
  private Button getApplyButton()
  {
    if (this.apply != null)
      return this.apply;
    
    this.apply = new Button(i18n.tr("Speichern"), new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        if (export())
          close();
      }
    },null,true,"document-save.png");
    return this.apply;
  }
}

