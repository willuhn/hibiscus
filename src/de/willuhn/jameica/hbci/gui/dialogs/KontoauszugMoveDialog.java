/**********************************************************************
 *
 * Copyright (c) 2018 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DirectoryInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.NotificationPanel;
import de.willuhn.jameica.gui.parts.NotificationPanel.Type;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.hbci.server.KontoauszugPdfUtil;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Action zum Verschieben von Kontoauszuegen.
 */
public class KontoauszugMoveDialog extends AbstractDialog<Kontoauszug>
{
  private final static Settings settings = new Settings(KontoauszugMoveDialog.class);
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static int WINDOW_WIDTH = 580;
  
  private Kontoauszug[] list;
  private DirectoryInput target = null;
  private CheckboxInput overwrite = null;
  private CheckboxInput delete = null;
  private NotificationPanel error = null;
  private Button apply = null;
  
  /**
   * ct.
   * @param list die Liste der zu verschiebenden Kontoauszuege.
   * @throws ApplicationException
   */
  public KontoauszugMoveDialog(Kontoauszug[] list) throws ApplicationException
  {
    super(KontoauszugMoveDialog.POSITION_CENTER);
    
    if (list == null || list.length == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Kontoauszüge aus"));
    
    this.list = list;
    
    this.setTitle(i18n.tr("Kontoauszüge verschieben"));
    setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }
  
  /**
   * Liefert das Eingabefeld fuer den Zielordner.
   * @return das Eingabefeld fuer den Zielordner.
   */
  private DirectoryInput getTarget()
  {
    if (this.target != null)
      return this.target;

    String fallback = System.getProperty("user.home");
    String s = settings.getString("lastdir",fallback);
    
    File dir = new File(s);
    if (!dir.exists() || !dir.canWrite() || !dir.isDirectory())
      dir = new File(fallback);
    this.target = new DirectoryInput(dir.getAbsolutePath());
    this.target.setName(i18n.tr("Zielverzeichnis"));

    final Listener l = new Listener() {
      
      @Override
      public void handleEvent(Event event)
      {
        check();
      }
    };
    this.target.addListener(l);
    return this.target;
  }
  
  /**
   * Liefert eine Checkbox, mit der eingestellt werden kann, ob vorhandene Dateien ueberschrieben werden sollen.
   * @return Checkbox, mit der eingestellt werden kann, ob vorhandene Dateien ueberschrieben werden sollen.
   */
  private CheckboxInput getOverwrite()
  {
    if (this.overwrite != null)
      return this.overwrite;
    
    this.overwrite = new CheckboxInput(settings.getBoolean("overwrite",false));
    this.overwrite.setName(i18n.tr("Gleichnamige Dateien überschreiben, wenn sie im Zielverzeichnis bereits existieren"));
    return this.overwrite;
  }
  
  /**
   * Liefert eine Checkbox, mit der eingestellt werden kann, ob die Dateien nach dem Verschieben geloescht werden sollen.
   * @return Checkbox, mit der eingestellt werden kann, ob die Dateien nach dem Verschieben geloescht werden sollen.
   */
  private CheckboxInput getDelete()
  {
    if (this.delete != null)
      return this.delete;
    
    this.delete = new CheckboxInput(settings.getBoolean("delete",false));
    this.delete.setName(i18n.tr("Quelldateien nach dem Kopieren löschen"));
    return this.delete;
  }
  
  /**
   * Prueft, ob die Auswahl korrekt ist.
   */
  private void check()
  {
    final String s = StringUtils.trimToNull((String) getTarget().getValue());
    
    String text = null;
    boolean enable = false;
    
    try
    {
      // Hier zeigen wir keinen Hinweistext an
      if (s == null)
        return;
      
      // Checken, ob es ein Ordner ist und er beschreibbar ist
      File dir = new File(s);
      if (!dir.exists())
      {
        text = "Zielverzeichnis existiert nicht";
        return;
      }
      if (!dir.isDirectory())
      {
        text = "Zielverzeichnis ist kein gültiges Verzeichnis";
        return;
      }
      if (!dir.canWrite())
      {
        text = "Keine Schreibrechte in Zielverzeichnis";
        return;
      }

      // Scheint alles ok zu sein
      enable = true;
    }
    finally
    {
      this.getError().setText(text != null ? Type.ERROR : Type.INVISIBLE,text != null ? i18n.tr(text) : "");
      this.getApplyButton().setEnabled(enable);
    }
  }
  
  /**
   * Liefert das Label fuer die Fehlermeldungen.
   * @return das Label fuer die Fehlermeldungen.
   */
  private NotificationPanel getError()
  {
    if (this.error != null)
      return this.error;
    
    this.error = new NotificationPanel();
    return this.error;
  }
  
  /**
   * Liefert den Apply-Button.
   * @return der Apply-Button.
   */
  private Button getApplyButton()
  {
    if (this.apply != null)
      return this.apply;
    
    this.apply = new Button(i18n.tr("Dateien jetzt verschieben"),new Action() {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        startMove();
      }
    },null,false,"ok.png");
    this.apply.setEnabled(false); // initial deaktiviert.
    
    return this.apply;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent);
    group.addText(i18n.tr("Bitte wählen Sie das Verzeichnis, in das die Dateien verschoben werden sollen und " +
                          "klicken Sie anschließend auf \"Dateien jetzt verschieben\", um den Vorgang zu starten."),true);

    group.addInput(this.getTarget());
    
    group.addHeadline(i18n.tr("Optionen"));
    group.addInput(this.getOverwrite());
    group.addInput(this.getDelete());

    group.addSeparator();
    group.addText(i18n.tr("Hinweise: Sie können diese Funktion auch verwenden, wenn Sie einige oder alle Dateien bereits selbst " +
                          "kopiert oder verschoben haben. In dem Fall werden lediglich die Verknüpfungen in Hibiscus aktualisiert.\n\n" +
                          "Beachten Sie, dass unterschiedliche Kontoauszüge verschiedener Konten dennoch identische Dateinamen besitzen können. " +
                          "Kopieren Sie diese in unterschiedliche Zielverzeichnisse, um sicherzustellen, dass keine Dateien verloren gehen oder " +
                          "versehentlich überschrieben werden."),true, Color.COMMENT);

    group.addPart(this.getError());

    ButtonArea b = new ButtonArea();
    b.addButton(this.getApplyButton());
    b.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,true,"process-stop.png");
    group.addButtonArea(b);
    
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
    this.check();
  }
  
  /**
   * Startet das Verschieben der Dateien.
   */
  private void startMove()
  {
    final boolean overwrite = ((Boolean) this.getOverwrite().getValue()).booleanValue();
    final boolean delete    = ((Boolean) this.getDelete().getValue()).booleanValue();
    final File dir          = new File((String) this.getTarget().getValue());
    final String pfad       = dir.getAbsolutePath();

    settings.setAttribute("lastdir",pfad);
    settings.setAttribute("overwrite",overwrite);
    settings.setAttribute("delete",delete);

    // Dialog schliessen, dann Hintergrund-Prozess starten
    this.close();

    final double factor = 100d / (double) list.length;

    Application.getController().start(new BackgroundTask() {
      
      private boolean stop = false;

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        if (monitor != null)
          monitor.setStatusText(i18n.tr(delete ? "Verschiebe Dateien" : "Kopiere Dateien"));
        
        for (int i=0;i<list.length;++i)
        {
          Kontoauszug k = list[i];

          try
          {
            if (stop)
              throw new OperationCanceledException();

            if (monitor != null)
            {
              monitor.log(i18n.tr("{0}/{1}: {2}",Integer.toString(i+1),Integer.toString(list.length),k.getDateiname()));
              monitor.setPercentComplete((int)((i+1) * factor));
            }

            Logger.info("processing kontoauszug [id: " + k.getID() + "], path: " + k.getPfad() + ", name: " + k.getDateiname() + ", uuid: " + k.getUUID());

            /////////////////////////////////////////////////////////////////////////////////////////
            // Quelldatei
            File source = null;
            try
            {
              // Wir kopieren auch Dateien, wenn sie per Archive-Server verwaltet wurde.
              source = KontoauszugPdfUtil.getFile(k);
            }
            catch (ApplicationException ae)
            {
              // Wir tolerieren diese Fehler. Es kann sein, dass der Benutzer die Dateien bereits verschoben hat.
              Logger.warn(ae.getMessage());
            }
            //
            /////////////////////////////////////////////////////////////////////////////////////////
            
            /////////////////////////////////////////////////////////////////////////////////////////
            // Zieldatei
            File target = new File(dir,k.getDateiname());
            //
            /////////////////////////////////////////////////////////////////////////////////////////
            
            /////////////////////////////////////////////////////////////////////////////////////////
            // Kopieren
            boolean sourceExists  = source != null;
            boolean targetExists = target.exists();
            boolean same         = sourceExists && targetExists && source.getCanonicalFile().equals(target.getCanonicalFile());
            
            // Wenn weder Quelle noch Ziel existieren, haben wir gar keine Datei
            if (!sourceExists && !targetExists)
              throw new ApplicationException(i18n.tr("Quell- und Zieldatei existieren nicht, kein Kopieren/Verschieben möglich"));
            
            if (same)
              Logger.info("skip copying, source and target are the same file");

            // Wir brauchen nur kopieren, wenn wir eine Quelle haben. Unabhaengig davon, ob das Ziel bereits existiert
            if (sourceExists && (!targetExists || overwrite) && !same)
            {
              Logger.info("copying " + source + " to " + target);
              FileInputStream is = null;
              FileOutputStream os = null;
              FileChannel src = null;
              FileChannel dst= null;
              try
              {
                if (targetExists)
                {
                  Logger.info("overwriting file " + target);
                  if (monitor != null)
                    monitor.log("    " + i18n.tr("Überschreibe Datei {0}",target.getName()));
                }
                  
                is = new FileInputStream(source);
                os = new FileOutputStream(target);
                src = is.getChannel();
                dst= os.getChannel();
                dst.transferFrom(src, 0, src.size());
              }
              finally
              {
                IOUtil.close(src);
                IOUtil.close(dst);
                IOUtil.close(is);
                IOUtil.close(os);
              }
            }
            //
            /////////////////////////////////////////////////////////////////////////////////////////

            /////////////////////////////////////////////////////////////////////////////////////////
            // Datensatz aktualisieren
            Logger.info("updating data");
            k.setPfad(pfad);
            k.setUUID(null); // UUID loeschen, Datei liegt jetzt nicht mehr im Archiv
            k.store();
            //
            /////////////////////////////////////////////////////////////////////////////////////////

            /////////////////////////////////////////////////////////////////////////////////////////
            // Quelldatei loeschen
            if (delete && source != null && !same)
            {
              String uuid = StringUtils.trimToNull(k.getUUID());
              if (uuid != null)
              {
                // Datei auf dem Archive-Server loeschen
                Logger.info("deleting " + uuid);
                QueryMessage qm = new QueryMessage(uuid,null);
                Application.getMessagingFactory().getMessagingQueue("jameica.messaging.del").sendSyncMessage(qm);
                
                // Die Temp-Datei "source" muessen wir nicht extra loeschen, die ist bereits mit "deleteOnExit" markiert
              }
              else
              {
                // Datei im Filesystem loeschen
                Logger.info("deleting " + source.getAbsolutePath());
                source.delete();
              }
            }
            //
            /////////////////////////////////////////////////////////////////////////////////////////
            
          }
          catch (ApplicationException ae)
          {
            if (monitor != null)
              monitor.log("    " + ae.getMessage());
          }
          catch (Exception e)
          {
            Logger.error("unable to copy entry",e);
            if (monitor != null)
              monitor.log("    " + i18n.tr("fehlgeschlagen: {0}",e.getMessage()));
          }
        }
        
        if (monitor != null)
        {
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setPercentComplete(100);
          monitor.setStatusText(i18n.tr("Vorgang abgeschlossen"));
        }
      }
      
      @Override
      public boolean isInterrupted()
      {
        return this.stop;
      }
      
      @Override
      public void interrupt()
      {
        this.stop = true;
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected Kontoauszug getData() throws Exception
  {
    return null;
  }

}


