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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.serialize.ObjectFactory;
import de.willuhn.datasource.serialize.Reader;
import de.willuhn.datasource.serialize.XmlReader;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Action zum Einspielen eines XML-Backups.
 */
public class BackupRestore implements Action
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    // Wir checken vorher, ob die Datenbank leer ist. Ansonsten koennen wir das
    // eh nicht sinnvoll importieren.
    try
    {
      if (Settings.getDBService().createList(Konto.class).size() > 0)
      {
        String text = i18n.tr("Die Hibiscus-Installation enthält bereits Daten.\n" +
                              "Das Backup kann nur in eine neue Hibiscus-Installation importiert werden.");
        Application.getCallback().notifyUser(text);
        return;
      }
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      return;
    }
    catch (Exception e)
    {
      Logger.error("unable to notify user",e);
      throw new ApplicationException(i18n.tr("Datenbank-Import fehlgeschlagen"));
    }
    
    FileDialog fd = new FileDialog(GUI.getShell(),SWT.OPEN);
    fd.setFileName("hibiscus-backup-" + BackupCreate.DATEFORMAT.format(new Date()) + ".xml");
    fd.setFilterExtensions(new String[]{"*.xml"});
    fd.setText("Bitte wählen Sie die Backup-Datei aus");
    String f = fd.open();
    if (f == null || f.length() == 0)
      return;
    
    final File file = new File(f);
    if (!file.exists())
      return;

    Application.getController().start(new BackgroundTask() {
      private boolean cancel = false;
    
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
       */
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        monitor.setStatusText(i18n.tr("Importiere Backup"));
        Logger.info("importing backup " + file.getAbsolutePath());
        final ClassLoader loader = Application.getPluginLoader().getManifest(HBCI.class).getClassLoader();

        Reader reader = null;
        try
        {
          InputStream is = new BufferedInputStream(new FileInputStream(file));
          reader = new XmlReader(is,new ObjectFactory() {
          
            public GenericObject create(String type, String id, Map values) throws Exception
            {
              AbstractDBObject object = (AbstractDBObject) Settings.getDBService().createObject((Class<AbstractDBObject>)loader.loadClass(type),null);
              object.setID(id);
              for (Object key : values.keySet())
              {
                String name = (String) key;
                object.setAttribute(name,values.get(name));
              }
              return object;
            }
          
          });
          
          long count = 1;
          GenericObject o = null;
          while ((o = reader.read()) != null)
          {
            try
            {
              ((AbstractDBObject)o).insert();
            }
            catch (Exception e)
            {
              if (o instanceof Protokoll)
              {
                // Bei den Protokollen kann das passieren. Denn beim Import der Datei werden vorher 
                // die Konten importiert. Und deren Anlage fuehrt auch bereits zur Erstellung von
                // Protokollen, deren IDs dann im Konflikt zu diesen hier stehen.
                Logger.write(Level.DEBUG,"unable to import " + o.getClass().getName() + ":" + o.getID() + ", skipping",e);
              }
              else
              {
                Logger.error("unable to import " + o.getClass().getName() + ":" + o.getID() + ", skipping",e);
                monitor.log("  " + i18n.tr("{0} fehlerhaft ({1}), überspringe",new String[]{BeanUtil.toString(o),e.getMessage()}));
              }
            }
            if (count++ % 100 == 0)
              monitor.addPercentComplete(1);
          }
          
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setStatusText("Backup importiert");
          monitor.setPercentComplete(100);
        }
        catch (Exception e)
        {
          Logger.error("error while importing data",e);
          throw new ApplicationException(e.getMessage());
        }
        finally
        {
          if (reader != null)
          {
            try
            {
              reader.close();
              Logger.info("backup imported");
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
}


/*********************************************************************
 * $Log: BackupRestore.java,v $
 * Revision 1.5  2012/03/28 22:47:18  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.4  2010/03/03 11:00:19  willuhn
 * @N Erst Status-Code setzen und dann erst den Text - sonst wird der Text nicht gruen gefaerbt
 *
 * Revision 1.3  2008/09/02 18:14:25  willuhn
 * @N Diagnose-Backup erweitert
 *
 * Revision 1.2  2008/04/30 09:01:23  willuhn
 * @C Fehlerhafte Objekte beim Restore ueberspringen
 *
 * Revision 1.1  2008/01/22 13:34:45  willuhn
 * @N Neuer XML-Import/-Export
 *
 **********************************************************************/