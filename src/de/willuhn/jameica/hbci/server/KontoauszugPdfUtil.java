/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.GV_Result.GVRKontoauszug.Format;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.io.FileCopy;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.messaging.MessagingAvailableConsumer;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectDeletedMessage;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.hbci.rmi.Protokoll;
import de.willuhn.jameica.hbci.server.BPDUtil.Support;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.VelocityService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.TypedProperties;

/**
 * Hilfsklasse mit verschiedenen Util-Funktionen fuer die Kontoauszuege.
 */
public class KontoauszugPdfUtil
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private final static String CHANNEL = "hibiscus.kontoauszuege";
  
  /**
   * Liefert das File-Objekt fuer diesen Kontoauszug.
   * Wenn er direkt im Filesystem gespeichert ist, wird dieses geliefert.
   * Wurde er jedoch per Messaging gespeichert, dann ruft die Funktion ihn
   * vom Archiv ab und erzeugt eine Temp-Datei mit dem Kontoauszug.
   * @param ka der Kontoauszug.
   * @return die Datei.
   * @throws ApplicationException
   */
  public static File getFile(Kontoauszug ka) throws ApplicationException
  {
    if (ka == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie den zu öffnenden Kontoauszug"));

    try
    {
      // Wenn ein Pfad und Dateiname angegeben ist, dann sollte die Datei
      // dort auch liegen
      final String path = StringUtils.trimToNull(ka.getPfad());
      final String name = StringUtils.trimToNull(ka.getDateiname());
      
      if (path != null && name != null)
      {
        File file = new File(path,name);
        
        Logger.info("trying to open pdf file from: " + file);
        if (!file.exists())
        {
          Logger.error("file does not exist (anymore): " + file);
          throw new ApplicationException(i18n.tr("Datei \"{0}\" existiert nicht mehr. Wurde sie gelöscht?",file.getAbsolutePath()));
        }
        
        if (!file.canRead())
        {
          Logger.error("cannot read file: " + file);
          throw new ApplicationException(i18n.tr("Datei \"{0}\" nicht lesbar",file.getAbsolutePath()));
        }
        
        return file;
      }
      
      final String uuid = StringUtils.trimToNull(ka.getUUID());

      Logger.info("trying to open pdf file using messaging, uuid: " + uuid);

      // Das kann eigentlich nicht sein. Dann wuerde ja alles fehlen
      if (uuid == null)
        throw new ApplicationException(i18n.tr("Ablageort des Kontoauszuges unbekannt"));
      
      QueryMessage qm = new QueryMessage(uuid,null);
      Application.getMessagingFactory().getMessagingQueue("jameica.messaging.get").sendSyncMessage(qm);
      byte[] data = (byte[]) qm.getData();
      if (data == null)
      {
        Logger.error("got no data from messaging for uuid: " + uuid);
        throw new ApplicationException(i18n.tr("Datei existiert nicht mehr im Archiv. Wurde sie gelöscht?"));
      }
      Logger.info("got " + data.length + " bytes from messaging for uuid: " + uuid);
      
      File file = File.createTempFile("kontoauszug-" + RandomStringUtils.randomAlphanumeric(5),".pdf");
      file.deleteOnExit();
      
      OutputStream os = null;
      
      try
      {
        os = new BufferedOutputStream(new FileOutputStream(file));
        IOUtil.copy(new ByteArrayInputStream(data),os);
      }
      finally
      {
        IOUtil.close(os);
      }
      
      Logger.info("copied messaging data into temp file: " + file);
      return file;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to open file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Öffnen des Kontoauszuges: {0}",e.getMessage()));
    }
  }

  /**
   * Speichert den Kontoauszug in einer Datei.
   * @param ka der Kontoauszug.
   * @param target die Datei, in der der Kontoauszug gespeichert werden soll.
   * @throws ApplicationException
   */
  public static void store(Kontoauszug ka, File target) throws ApplicationException
  {
    if (ka == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie den zu speichernden Kontoauszug"));

    if (target == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die Zieldatei aus"));

    try
    {
      // Wenn ein Pfad und Dateiname angegeben ist, dann sollte die Datei
      // dort auch liegen
      final String path = StringUtils.trimToNull(ka.getPfad());
      final String name = StringUtils.trimToNull(ka.getDateiname());
      
      if (path != null && name != null)
      {
        File file = new File(path,name);
        
        Logger.info("trying to open pdf file from: " + file);
        if (!file.exists())
        {
          Logger.error("file does not exist (anymore): " + file);
          throw new ApplicationException(i18n.tr("Datei \"{0}\" existiert nicht mehr. Wurde sie gelöscht?",file.getAbsolutePath()));
        }
        
        if (!file.canRead())
        {
          Logger.error("cannot read file: " + file);
          throw new ApplicationException(i18n.tr("Datei \"{0}\" nicht lesbar",file.getAbsolutePath()));
        }
        
        FileCopy.copy(file,target,true);
        Logger.info("copied " + file + " to " + target);
        return;
      }
      
      final String uuid = StringUtils.trimToNull(ka.getUUID());

      Logger.info("trying to open pdf file using messaging, uuid: " + uuid);

      // Das kann eigentlich nicht sein. Dann wuerde ja alles fehlen
      if (uuid == null)
        throw new ApplicationException(i18n.tr("Ablageort des Kontoauszuges unbekannt"));
      
      QueryMessage qm = new QueryMessage(uuid,null);
      Application.getMessagingFactory().getMessagingQueue("jameica.messaging.get").sendSyncMessage(qm);
      byte[] data = (byte[]) qm.getData();
      if (data == null)
      {
        Logger.error("got no data from messaging for uuid: " + uuid);
        throw new ApplicationException(i18n.tr("Datei existiert nicht mehr im Archiv. Wurde sie gelöscht?"));
      }
      Logger.info("got " + data.length + " bytes from messaging for uuid: " + uuid);
      
      OutputStream os = null;
      
      try
      {
        os = new BufferedOutputStream(new FileOutputStream(target));
        IOUtil.copy(new ByteArrayInputStream(data),os);
      }
      finally
      {
        IOUtil.close(os);
      }
      
      Logger.info("copied messaging data into file: " + target);
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to open file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Öffnen des Kontoauszuges: {0}",e.getMessage()));
    }
  }

  /**
   * Speichert den Kontoauszug im Dateisystem bzw. Messaging.
   * @param k der Kontoauszug. Er muss eine ID besitzen - also bereits gespeichert worden sein.
   * @param data die rohen Binaer-Daten.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static void receive(Kontoauszug k, byte[] data) throws RemoteException, ApplicationException
  {
    if (k == null)
      throw new ApplicationException(i18n.tr("Kein Kontoauszug angegeben"));
    
    if (data == null || data.length == 0)
      throw new ApplicationException(i18n.tr("Kein Daten angegeben"));
    
    final Konto konto = k.getKonto();
    if (konto == null)
      throw new ApplicationException(i18n.tr("Kein Konto angegeben"));

    // Per Messaging speichern?
    if (MessagingAvailableConsumer.haveMessaging() && Boolean.parseBoolean(MetaKey.KONTOAUSZUG_STORE_MESSAGING.get(konto)))
    {
      QueryMessage qm = new QueryMessage(CHANNEL,data);
      Application.getMessagingFactory().getMessagingQueue("jameica.messaging.put").sendSyncMessage(qm);
      k.setUUID(qm.getData().toString());
      k.store();
      Logger.info("stored account statement data in messaging archive [id: " + k.getID() + ", uuid: " + k.getUUID() + "]");
      return;
    }
    
    // Im Dateisystem speichern
    String path = createPath(konto,k);
    try
    {
      File file = new File(path).getCanonicalFile();
      Logger.info("storing account statement data in file [id: " + k.getID() + ", file: " + file + "]");
      
      File dir = file.getParentFile();
      if (!dir.exists())
      {
        Logger.info("auto-creating parent dir: " + dir);
        if (!dir.mkdirs())
          throw new ApplicationException(i18n.tr("Erstellen des Ordners fehlgeschlagen. Ordner-Berechtigungen korrekt?"));
      }
      
      if (!dir.canWrite())
        throw new ApplicationException(i18n.tr("Kein Schreibzugriff in {0}",dir.toString()));
      
      OutputStream os = null;
      
      try
      {
        File target = file;
        
        int i=0;
        while (i< 10000)
        {
          // Checken, ob die Datei schon existiert. Wenn ja, haengen wir einen Zaehler hinten drin.
          // Um sicherzugehen, dass wir die Datei nicht ueberschreiben.
          if (!target.exists())
            break;
          
          // OK, die Datei gibts schon. Wir haengen den Counter hinten an
          i++;
          target = indexedFile(file,i);
        }
        os = new BufferedOutputStream(new FileOutputStream(target));
        os.write(data);
        k.setPfad(target.getParent());
        k.setDateiname(target.getName());
        k.store();
      }
      finally
      {
        IOUtil.close(os);
      }
    }
    catch (IOException e)
    {
      Logger.error("unable to store account statement data in file: " + path,e);
      throw new ApplicationException(i18n.tr("Speichern des Kontoauszuges fehlgeschlagen: {0}",e.getMessage()));
    }
  }
  
  /**
   * Haengt die Nummer an den Dateinamen an.
   * @param f die Datei.
   * @param i die Nummer.
   * @return die neue Datei.
   */
  private static File indexedFile(File f, int i)
  {
    String name = f.getName();
    int dot     = name.lastIndexOf('.');
    
    name = name.substring(0,dot) + "-" + String.format("%05d",i) + name.substring(dot);
    return new File(f.getParentFile(),name);
  }

  /**
   * Erzeugt den Pfad fuer den zu speichernden Kontoauszug.
   * @param k das Konto.
   * @param ka der Kontoauszug. Optional. Wenn er fehlt, werden Default-Werte verwendet.
   * @return der Pfad.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static String createPath(Konto k, Kontoauszug ka) throws RemoteException, ApplicationException
  {
    if (k == null)
      throw new ApplicationException(i18n.tr("Kein Konto angegeben"));
    
    final String path   = MetaKey.KONTOAUSZUG_STORE_PATH.get(k);
    final String folder = MetaKey.KONTOAUSZUG_TEMPLATE_PATH.get(k);
    final String name   = MetaKey.KONTOAUSZUG_TEMPLATE_NAME.get(k);
    return createPath(k,ka,path,folder,name);
  }

  /**
   * Prueft den Pfad fuer den zu speichernden Kontoauszug.
   * @param k das Konto.
   * @param folder Template fuer den Unterordner.
   * @param name Template fuer den Dateinamen.
   * @return true, wenn der Pfad OK ist.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static boolean testPath(Konto k, String folder, String name) throws RemoteException, ApplicationException
  {
    String s = createPath(k,null,null,folder,name,true);
    return !s.contains("{") &&
           !s.contains("}") && 
           !s.contains("$");
  }

  /**
   * Erzeugt den Pfad fuer den zu speichernden Kontoauszug.
   * @param k das Konto.
   * @param ka der Kontoauszug. Optional. Wenn er fehlt, werden Default-Werte verwendet.
   * @param path Ordner, in dem die Kontoauszuege gespeichert werden.
   * @param folder Template fuer den Unterordner.
   * @param name Template fuer den Dateinamen.
   * @return der Pfad.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static String createPath(Konto k, Kontoauszug ka, String path, String folder, String name) throws RemoteException, ApplicationException
  {
    return createPath(k,ka,path,folder,name,false);
  }

  /**
   * Erzeugt den Pfad fuer den zu speichernden Kontoauszug.
   * @param k das Konto.
   * @param ka der Kontoauszug. Optional. Wenn er fehlt, werden Default-Werte verwendet.
   * @param path Ordner, in dem die Kontoauszuege gespeichert werden.
   * @param folder Template fuer den Unterordner.
   * @param name Template fuer den Dateinamen.
   * @param test true, wenn es nur ein Test ist.
   * @return der Pfad.
   * @throws RemoteException
   * @throws ApplicationException
   */
  private static String createPath(Konto k, Kontoauszug ka, String path, String folder, String name, boolean test) throws RemoteException, ApplicationException
  {
    if (k == null)
      throw new ApplicationException(i18n.tr("Kein Konto angegeben"));

    Map<String,Object> ctx = new HashMap<String,Object>();
    
    {
      String iban = StringUtils.trimToNull(k.getIban());
      if (iban == null)
        iban = StringUtils.trimToEmpty(k.getKontonummer());
      
      ctx.put("iban",iban.replaceAll(" ",""));
    }
    
    {
      String bic = StringUtils.trimToNull(k.getBic());
      if (bic == null)
        bic = StringUtils.trimToEmpty(k.getBLZ());

      ctx.put("bic",bic.replaceAll(" ",""));
    }

    {
      Calendar cal = Calendar.getInstance();
      if (ka != null)
      {
        if (ka.getErstellungsdatum() != null)
          cal.setTime(ka.getErstellungsdatum());
        else if (ka.getAusfuehrungsdatum() != null)
          cal.setTime(ka.getAusfuehrungsdatum());
      }
      
      Integer i = ka != null && ka.getJahr() != null ? ka.getJahr() : null;
      ctx.put("jahr",i != null ? i.toString() : Integer.toString(cal.get(Calendar.YEAR)));
      ctx.put("monat",String.format("%02d",cal.get(Calendar.MONTH) + 1));
      ctx.put("tag",String.format("%02d",cal.get(Calendar.DATE)));
      ctx.put("stunde",String.format("%02d",cal.get(Calendar.HOUR_OF_DAY)));
      ctx.put("minute",String.format("%02d",cal.get(Calendar.MINUTE)));
    }
    
    {
      Integer i = ka != null && ka.getNummer() != null ? ka.getNummer() : null;
      ctx.put("nummer",String.format("%03d",i != null ? i.intValue() : 1));
    }

    VelocityService velocity = Application.getBootLoader().getBootable(VelocityService.class);
    StringBuilder sb = new StringBuilder();

    /////////////////////////////
    // Pfad
    if (!test)
    {
      if (path == null || path.length() == 0)
        path = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath();
      sb.append(path);
      
      if (!path.endsWith(File.separator))
        sb.append(File.separator);
    }
    //
    /////////////////////////////

    /////////////////////////////
    // Unter-Ordner
    {
      if (folder != null && folder.length() > 0)
      {
        try
        {
          // Velocity-Escaping machen wir. Das sollte der User nicht selbst machen muessen
          // Eigentlich wird hier nur "\$" gegen "\\$" ersetzt. Die zusaetzlichen
          // Die extra Escapings sind fuer Java selbst in String-Literalen.
          folder = folder.replace("\\$","\\\\$");
          folder = velocity.merge(folder,ctx);
        }
        catch (Exception e)
        {
          Logger.error("folder template invalid: \"" + folder + "\"",e);
        }
        sb.append(folder);
        if (!folder.endsWith(File.separator))
          sb.append(File.separator);
      }
    }
    //
    /////////////////////////////
    
    /////////////////////////////
    // Dateiname
    {
      if (name == null || name.length() == 0 && ka != null)
        name = ka.getDateiname();
      
      if (name == null || name.length() == 0)
        name = MetaKey.KONTOAUSZUG_TEMPLATE_NAME.getDefault();
      
      try
      {
        name = velocity.merge(name,ctx);
      }
      catch (Exception e)
      {
        Logger.error("name template invalid: \"" + name + "\"",e);
      }
      sb.append(name);
      
      // Dateiendung noch anhaengen.
      Format f = Format.find(ka != null ? ka.getFormat() : null);
      if (f == null)
        f = Format.PDF;
      
      sb.append(".");
      sb.append(f.getExtention());
    }

    return sb.toString();
  }
  
  /**
   * Liefert die Liste der noch ungelesenen Kontoauszuege.
   * @return die Liste der noch ungelesenen Kontoauszuege, chronologisch nach Erstellungsdatum sortiert.
   * Neueste zuerst.
   * @throws RemoteException
   */
  public static GenericIterator<Kontoauszug> getUnread() throws RemoteException
  {
    HBCIDBService service = (HBCIDBService) Settings.getDBService();
    DBIterator it = service.createList(Kontoauszug.class);
    it.addFilter("gelesen_am is null");
    it.setOrder("order by " + service.getSQLTimestamp("erstellungsdatum") + " desc");
    return it;
  }
  
  /**
   * Liefert eine gefilterte Liste von Kontoauszuegen.
   * @param konto das optionale Konto. Kann auch der Name einer Kontogruppe sein.
   * @param from das optionale Start-Datum.
   * @param to das optionale End-Datum.
   * @param unread true, wenn nur ungelesene Kontoauszuege geliefert werden sollen.
   * @param inclusive true, wenn auch Kontoauszuege geliefert werden sollen, die nur in den Datumsbereich hineinreichen.
   * @return die Liste der passenden Kontoauszuege.
   * @throws RemoteException
   */
  public static GenericIterator<Kontoauszug> getList(Object konto, Date from, Date to, boolean unread, boolean inclusive) throws RemoteException
  {
    HBCIDBService service = (HBCIDBService) Settings.getDBService();
    DBIterator it = service.createList(Kontoauszug.class);

    final boolean haveFrom = from != null;
    final boolean haveTo = to != null;
    
    java.sql.Date f = haveFrom ? new java.sql.Date(DateUtil.startOfDay(from).getTime()) : null;
    java.sql.Date t = haveTo ? new java.sql.Date(DateUtil.endOfDay(to).getTime()) : null;
    
    // Bei HKEKP in Segment-Version 1 wird gar kein Zeitraum mitgeliefert.
    // Daher nehmen wir dort das Abrufdatum
    
    if (inclusive && (haveFrom || haveTo)) // Wenigstens eines der beiden Daten muss vorhanden sein
    {
      if (haveFrom && haveTo)
      {
        it.addFilter("("
            + "(von >= ? AND von <= ?)"
            + " OR "
            + "(bis >= ? AND bis <= ?)"
            + " OR "
            + "(von <= ? AND bis >= ?)"
            + " OR "
            + "(erstellungsdatum >= ? AND erstellungsdatum <= ?)"
            + " OR "
            + "(von IS NULL AND bis IS NULL AND erstellungsdatum IS NULL AND ausgefuehrt_am >= ? AND ausgefuehrt_am <= ?)"
            + ")", f, t, f, t, f, t, f, t, f, t);
      }
      else if (haveFrom)
      {
        // Kontoauszug endet nach dem From-Datum
        it.addFilter("((bis >= ? OR erstellungsdatum >= ?) OR (bis IS NULL AND erstellungsdatum IS NULL AND ausgefuehrt_am >= ?))", f, f, f);
      }
      else if (haveTo)
      {
        // Kontoauszug beginnt wenigstens vor dem To-Datum
        it.addFilter("((von <= ? OR erstellungsdatum <= ?) OR (bis IS NULL AND erstellungsdatum IS NULL AND ausgefuehrt_am <= ?))", t, t, t);        
      }
    }
    else 
    {
      if (haveFrom)
      {
        it.addFilter("(von >= ? OR erstellungsdatum >= ? OR (von IS NULL AND erstellungsdatum IS NULL AND ausgefuehrt_am >= ?))", f, f, f);
      }
      if (haveTo)
      {
        it.addFilter("(bis <= ? OR erstellungsdatum <= ? OR (bis IS NULL AND erstellungsdatum IS NULL AND ausgefuehrt_am <= ?))", t, t, t);
      }
    }
    
    if (konto != null && (konto instanceof Konto))
      it.addFilter("konto_id = " + ((Konto) konto).getID());
    else if (konto != null && (konto instanceof String))
      it.addFilter("konto_id in (select id from konto where kategorie = ?)", (String) konto);

    if (unread)
      it.addFilter("gelesen_am is null");
    
    it.setOrder("order by jahr desc, nummer desc, " + 
                service.getSQLTimestamp("erstellungsdatum") + " desc, " + 
                service.getSQLTimestamp("von") + " desc, " + 
                service.getSQLTimestamp("ausgefuehrt_am") + " desc");
    return it;
  }
  
  /**
   * Liefert den aktuellsten Kontoauszug mit Nummer.
   * @param k das Konto.
   * @return der Kontoauszug oder NULL, wenn er nicht existiert.
   * @throws RemoteException
   */
  public static Kontoauszug getNewestWithNumber(Konto k) throws RemoteException
  {
    HBCIDBService service = (HBCIDBService) Settings.getDBService();
    DBIterator<Kontoauszug> it = service.createList(Kontoauszug.class);
    it.addFilter("konto_id = " + k.getID());
    it.addFilter("nummer is not null");
    
    it.setOrder("order by jahr desc, nummer desc, " + 
                service.getSQLTimestamp("erstellungsdatum") + " desc, " + 
                service.getSQLTimestamp("von") + " desc, " + 
                service.getSQLTimestamp("ausgefuehrt_am") + " desc");
    
    return it.hasNext() ? it.next() : null;
  }
  
  /**
   * Loescht die angegebenen Kontoauszuege und bei Bedarf auch die Dateien.
   * @param deleteFiles true, wenn auch die Dateien geloescht werden sollen.
   * @param list die zu loeschenden Kontoauszuege.
   */
  public static void delete(boolean deleteFiles, Kontoauszug... list)
  {
    if (list == null || list.length == 0)
      return;
    
    Kontoauszug tx = null;
    
    int count = 0;
    
    try
    {
      for (Kontoauszug k:list)
      {
        if (tx == null)
        {
          tx = k;
          tx.transactionBegin();
        }

        if (deleteFiles)
        {
          String uuid = k.getUUID();
          if (uuid != null && uuid.length() > 0)
          {
            QueryMessage qm = new QueryMessage(uuid,null);
            Application.getMessagingFactory().getMessagingQueue("jameica.messaging.del").sendSyncMessage(qm);
          }
          else
          {
            final String pfad = k.getPfad();
            final String name = k.getDateiname();
            if (pfad == null || pfad.length() == 0 || name == null || name.length() == 0)
            {
              Logger.warn("filename or path missing for account statements, skipping");
            }
            else
            {
              File file = new File(pfad,name);
              if (file.exists() && file.canWrite())
              {
                if (!file.delete())
                  Logger.warn("deleting of file failed: " + file);
              }
              else
              {
                Logger.info("file does not exist, skipping: " + file);
              }
            }
          }
        }
        
        Konto konto = k.getKonto();
        konto.addToProtokoll(i18n.tr("Elektronischen Kontoauszug gelöscht"),Protokoll.TYP_SUCCESS);
        final String id = k.getID();
        k.delete();
        Application.getMessagingFactory().sendMessage(new ObjectDeletedMessage(k,id));
        count++;
      }
      
      if (tx != null)
        tx.transactionCommit();
      
      if (count == 1)
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Kontoauszug gelöscht."),StatusBarMessage.TYPE_SUCCESS));
      else
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("{0} Kontoauszüge gelöscht.",Integer.toString(count)),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      Logger.error("deleting account statements failed",e);
      
      if (tx != null)
      {
        try
        {
          tx.transactionRollback();
        }
        catch (RemoteException re)
        {
          Logger.error("tx rollback failed",re);
        }
      }
    }
  }
  
  /**
   * Markiert die Liste der angegebenen Kontoauszuege als gelesen.
   * Jedoch nur, wenn sie nicht bereits als gelesen markiert sind.
   * @param read true, wenn die Kontoauszuege als gelesen werden sollen. Sonst false.
   * @param list die Liste der als gelesen zu markierenden Kontoauszuege.
   */
  public static void markRead(boolean read, Kontoauszug... list)
  {
    if (list == null || list.length == 0)
      return;
    
    Kontoauszug tx = null;
    
    try
    {
      for (Kontoauszug k:list)
      {
        if (k.isNewObject())
          continue;
        
        if (tx == null)
        {
          tx = k;
          tx.transactionBegin();
        }
        
        Date d = k.getGelesenAm();
        
        if (d == null && !read)
        {
          Logger.info("account statement already marked as unread, skipping [id: " + k.getID()+ "]");
          continue;
        }
        if (d != null && read)
        {
          Logger.info("account statement already marked as read, skipping [id: " + k.getID()+ ", date: " + d + "]");
          continue;
        }
        
        d = read ? new Date() : null;
        Logger.info("mark account statements as " + (read ? "read" : "unread" )+  " [id: " + k.getID()+ ", date: " + d + "]");
        k.setGelesenAm(d);
        k.store();
        Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(k));
      }
      
      if (tx != null)
        tx.transactionCommit();
    }
    catch (Exception e)
    {
      Logger.error("marking account statements as read failed",e);
      
      if (tx != null)
      {
        try
        {
          tx.transactionRollback();
        }
        catch (RemoteException re)
        {
          Logger.error("tx rollback failed",re);
        }
      }
    }
  }
  
  /**
   * Prueft, ob elektronische Kontoauszuege im PDF-Format fuer dieses Konto unterstuetzt werden.
   * @param k das zu pruefende Konto.
   * @return true, wenn es unterstuetzt wird.
   */
  public static boolean supported(Konto k)
  {
    if (k == null)
    {
      Logger.warn("no account given");
      return false;
    }

    try
    {
      Logger.debug("checking HKEKP/HKEKA support for account: " + (k != null ? k.getIban() : "<none>"));
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine iban");
    }
    
    // Wenn HKEKP unterstuetzt wird, haben wir auf jeden Fall PDF
    Support support = BPDUtil.getSupport(k,BPDUtil.Query.KontoauszugPdf);
    if (support != null && support.isSupported())
    {
      Logger.debug("HKEKP supported");
      return true;
    }

    boolean ignoreSupport = false;
    try
    {
      ignoreSupport = Boolean.parseBoolean(MetaKey.KONTOAUSZUG_IGNORE_FORMAT.get(k));
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine account meta key value",re);
    }

    // Checken, ob wir HKEKA mit dem Format BPD haben
    support = BPDUtil.getSupport(k,BPDUtil.Query.Kontoauszug);

    if (support == null || !support.getBpdSupport())
      return conditionalSupport("HKEKA not supported according to BPD",ignoreSupport);

    if (!support.getUpdSupport())
      return conditionalSupport("HKEKA not supported according to UPD",ignoreSupport);
    
    // Wenn nur HKEKA unterstuetzt wird, muessen wir uns die angeboteten Dateiformate anschauen
    TypedProperties props = support.getBpd();
    if (props == null || props.size() == 0)
       return conditionalSupport("no BPD cache data found for HKEKA",ignoreSupport);
     
    List<Format> formats = getFormats(props);
    if (formats.size() == 0)
      return conditionalSupport("BPD cache contains no information regarding supported formats of HKEKA",ignoreSupport);
    
    if (formats.contains(Format.PDF))
    {
      Logger.debug("HKEKA with PDF supported");
      return true;
    }
     
    return conditionalSupport("HKEKA does not support PDF according to BPD",ignoreSupport);
  }
  
  /**
   * Ermittelt die Liste der unterstuetzten Formate aus den BPD.
   * @param bpd die BPD.
   * @return die Liste der Formate. Nie NULL sondern hoechstens eine leere Liste.
   */
  public static List<Format> getFormats(TypedProperties bpd)
  {
    List<Format> result = new ArrayList<Format>();
    if (bpd == null || bpd.size() == 0)
      return result;
    
    String[] formats = bpd.getList("format",null);
    
    // Checken, ob eventuell nur ein Format drin steht
    if (formats == null || formats.length == 0)
    {
      String format = bpd.getProperty("format",null);
      if (format != null)
        formats = new String[]{format};
    }
      
    if (formats == null || formats.length == 0)
      return result;
    
    // Checken, ob PDF dabei ist
    for (String f:formats)
    {
      Format gf = Format.find(f);
      if (gf != null)
        result.add(gf);
    }
    
    return result;
  }
  
  /**
   * Forciert den Support per optionalem Setting, auch wenn das Konto es laut BPD nicht kann.
   * @param reason ein Begruendungstext, warum der Support eigentlich nicht vorhanden ist.
   * @param ignoreSupport true, wenn die Kontoauszuege auch dann abgerufen werden sollen, wenn es eigentlich nicht unterstuetzt wird.
   * @return true, wenn der forcierte Support aktiviert ist, sonst false.
   */
  private static boolean conditionalSupport(String reason, boolean ignoreSupport)
  {
    Logger.debug(reason + " - support forced: " + ignoreSupport);
    return ignoreSupport;
  }
  
  /**
   * Liefert eine String-Repraesentation des Kontoauszuges.
   * @param k der Kontoauszug.
   * @return die String-Repraesentation.
   * @throws RemoteException
   */
  public static String toString(Kontoauszug k) throws RemoteException
  {
    if (k == null)
      return "";
    
    Date von = k.getVon();
    Date bis = k.getBis();
    if (von != null && bis != null)
      return i18n.tr("Kontoauszug {0} - {1}",HBCI.DATEFORMAT.format(von),HBCI.DATEFORMAT.format(bis));
    
    Integer jahr = k.getJahr();
    Integer nr   = k.getNummer();
    
    if (jahr != null && nr != null)
      return i18n.tr("Kontoauszug {0}/{1}",jahr.toString(),nr.toString());

    Date erstellt = k.getErstellungsdatum();
    return i18n.tr("Kontoauszug vom {0}",HBCI.DATEFORMAT.format(erstellt != null ? erstellt : k.getAusfuehrungsdatum()));
  }
}


