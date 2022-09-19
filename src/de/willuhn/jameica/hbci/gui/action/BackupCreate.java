/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.GenericObjectNode;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.rmi.DBObjectNode;
import de.willuhn.datasource.serialize.Writer;
import de.willuhn.datasource.serialize.XmlWriter;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.server.AuslandsUeberweisungImpl;
import de.willuhn.jameica.hbci.server.DBPropertyImpl;
import de.willuhn.jameica.hbci.server.DBReminderImpl;
import de.willuhn.jameica.hbci.server.DauerauftragImpl;
import de.willuhn.jameica.hbci.server.HibiscusAddressImpl;
import de.willuhn.jameica.hbci.server.KontoImpl;
import de.willuhn.jameica.hbci.server.KontoauszugImpl;
import de.willuhn.jameica.hbci.server.LastschriftImpl;
import de.willuhn.jameica.hbci.server.NachrichtImpl;
import de.willuhn.jameica.hbci.server.ProtokollImpl;
import de.willuhn.jameica.hbci.server.SammelLastBuchungImpl;
import de.willuhn.jameica.hbci.server.SammelLastschriftImpl;
import de.willuhn.jameica.hbci.server.SammelUeberweisungBuchungImpl;
import de.willuhn.jameica.hbci.server.SammelUeberweisungImpl;
import de.willuhn.jameica.hbci.server.SepaDauerauftragImpl;
import de.willuhn.jameica.hbci.server.SepaLastschriftImpl;
import de.willuhn.jameica.hbci.server.SepaSammelLastBuchungImpl;
import de.willuhn.jameica.hbci.server.SepaSammelLastschriftImpl;
import de.willuhn.jameica.hbci.server.SepaSammelUeberweisungBuchungImpl;
import de.willuhn.jameica.hbci.server.SepaSammelUeberweisungImpl;
import de.willuhn.jameica.hbci.server.TurnusImpl;
import de.willuhn.jameica.hbci.server.UeberweisungImpl;
import de.willuhn.jameica.hbci.server.UmsatzImpl;
import de.willuhn.jameica.hbci.server.UmsatzTypImpl;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Action zum Erstellen eines Komplett-Backups im XML-Format.
 */
public class BackupCreate implements Action
{
  /**
   * Dateformat, welches fuer den Dateinamen genutzt wird.
   */
  public static DateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");

  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    FileDialog fd = new FileDialog(GUI.getShell(),SWT.SAVE);
    fd.setFilterPath(System.getProperty("user.home"));
    fd.setOverwrite(true);
    fd.setFileName("hibiscus-backup-" + DATEFORMAT.format(new Date()) + ".xml");
    fd.setFilterExtensions(new String[]{"*.xml"});
    fd.setText("Bitte wählen Sie die Datei, in der das Backup gespeichert wird");
    String f = fd.open();
    if (f == null || f.length() == 0)
      return;
    
    final File file = new File(f);
    Application.getController().start(new BackgroundTask() {
      private boolean cancel = false;
    
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
       */
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        Writer writer = null;
        try
        {
          Logger.info("creating xml backup to " + file.getAbsolutePath());

          writer = new XmlWriter(new BufferedOutputStream(new FileOutputStream(file)));

          monitor.setStatusText(i18n.tr("Speichere Turnus-Informationen"));
          backup(TurnusImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere Adressbuch"));
          backup(HibiscusAddressImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere Konten und Systemnachrichten"));
          backup(KontoImpl.class,writer,monitor);
          backup(NachrichtImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere Umsatz-Kategorien"));
          backupTree(UmsatzTypImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere Umsätze"));
          backup(UmsatzImpl.class,writer,monitor);
          monitor.addPercentComplete(20);
          
          monitor.setStatusText(i18n.tr("Speichere Daueraufträge"));
          backup(DauerauftragImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere SEPA-Daueraufträge"));
          backup(SepaDauerauftragImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere Lastschriften"));
          backup(LastschriftImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere Überweisungen"));
          backup(UeberweisungImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere SEPA-Überweisungen"));
          backup(AuslandsUeberweisungImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere SEPA-Lastschriften"));
          backup(SepaLastschriftImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere Sammel-Lastschriften"));
          backup(SammelLastschriftImpl.class,writer,monitor);
          backup(SammelLastBuchungImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere Sammel-Überweisungen"));
          backup(SammelUeberweisungImpl.class,writer,monitor);
          backup(SammelUeberweisungBuchungImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere SEPA-Sammellastschriften"));
          backup(SepaSammelLastschriftImpl.class,writer,monitor);
          backup(SepaSammelLastBuchungImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere SEPA-Sammelüberweisungen"));
          backup(SepaSammelUeberweisungImpl.class,writer,monitor);
          backup(SepaSammelUeberweisungBuchungImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere Kontoauszüge"));
          backup(KontoauszugImpl.class,writer,monitor);
          monitor.addPercentComplete(5);

          monitor.setStatusText(i18n.tr("Speichere Properties"));
          backup(DBPropertyImpl.class,writer,monitor);
          monitor.addPercentComplete(10);

          monitor.setStatusText(i18n.tr("Speichere Reminder"));
          backup(DBReminderImpl.class,writer,monitor);
          monitor.addPercentComplete(2);

          // Die Protokolle zum Schluss.
          monitor.setStatusText(i18n.tr("Speichere Protokolle"));
          backup(ProtokollImpl.class,writer,monitor);
          monitor.addPercentComplete(20);
          
          // Die Versionstabelle wird nicht mit kopiert
          
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setStatusText("Backup erstellt");
          monitor.setPercentComplete(100);
        }
        catch (Exception e)
        {
          throw new ApplicationException(e.getMessage());
        }
        finally
        {
          if (writer != null)
          {
            try
            {
              writer.close();
              Logger.info("backup created");
            }
            catch (Exception e) {/*useless*/}
          }
        }
      }
    
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
       */
      public boolean isInterrupted()
      {
        return this.cancel;
      }
    
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
       */
      public void interrupt()
      {
        this.cancel = true;
      }
    
    });
  }
  
  /**
   * Hilfsfunktion.
   * @param type der Typ der zu speichernden Objekte.
   * @param writer der Writer.
   * @param monitor der Monitor.
   * @throws Exception
   */
  private static void backup(Class<? extends DBObject> type, Writer writer, ProgressMonitor monitor) throws Exception
  {
    DBIterator list = Settings.getDBService().createList(type);
    list.setOrder("order by id");
    long count = 1;
    while (list.hasNext())
    {
      GenericObject o = null;
      try
      {
        o = list.next();
        writer.write(o);
        if (count++ % 200 == 0)
          monitor.addPercentComplete(1);
      }
      catch (Exception e)
      {
        Logger.error("error while writing object " + BeanUtil.toString(o) + " - skipping",e);
        monitor.log("  " + i18n.tr("{0} fehlerhaft ({1}), überspringe", BeanUtil.toString(o), e.getMessage()));
      }
    }
  }

  /**
   * Hilfsfunktion zum rekursiven Sichern von Baeumen.
   * Und zwar so, dass sie sich anschliessend korrekt wieder importieren lassen.
   * @param type der Knoten-Typ des Baumes.
   * @param writer der Writer.
   * @param monitor der Monitor.
   * @throws Exception
   */
  private static void backupTree(Class<? extends DBObjectNode> type, Writer writer, ProgressMonitor monitor) throws Exception
  {
    // Wir fangen auf der obersten Ebene an, anschliessen gehen wir in die Rekursion.
    DBIterator root = Settings.getDBService().createList(type);
    root.addFilter("parent_id is null");
    root.setOrder("order by id");

    while (root.hasNext())
    {
      GenericObjectNode o = null;
      try
      {
        o = (GenericObjectNode)root.next();
        backupNode(o,writer,monitor);
        monitor.addPercentComplete(1);
      }
      catch (Exception e)
      {
        Logger.error("error while writing object " + BeanUtil.toString(o) + " - skipping",e);
        monitor.log("  " + i18n.tr("{0} fehlerhaft ({1}), überspringe", BeanUtil.toString(o), e.getMessage()));
      }
    }
  }
  
  /**
   * Sichert den Knoten und dessen Kinder.
   * @param der Knoten.
   * @param writer der Writer.
   * @param monitor der Monitor.
   * @throws Exception
   */
  private static void backupNode(GenericObjectNode node, Writer writer, ProgressMonitor monitor) throws Exception
  {
    try
    {
      // erst der Knoten selbst
      writer.write(node);
      
      // und jetzt rekursiv die Kinder
      GenericIterator children = node.getChildren();
      if (children != null)
      {
        while (children.hasNext())
        {
          backupNode((GenericObjectNode) children.next(),writer,monitor);
        }
      }
    }
    catch (Exception e)
    {
      Logger.error("error while writing object " + BeanUtil.toString(node) + " - skipping",e);
      monitor.log("  " + i18n.tr("{0} fehlerhaft ({1}), überspringe", BeanUtil.toString(node), e.getMessage()));
    }
  }
  

}
