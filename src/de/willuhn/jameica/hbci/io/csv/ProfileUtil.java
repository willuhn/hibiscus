/**********************************************************************
 *
 * Copyright (c) 2018 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.csv;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.CSVProfileStoreDialog;
import de.willuhn.jameica.hbci.io.JAXBUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Hilfeklasse zum Laden und Speichern von CSV-Import-Profilen.
 */
public class ProfileUtil
{
  private final static Settings settings = new Settings(ProfileUtil.class);
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Laedt die vorhandenen Profile fuer das Format.
   * @param format das Format.
   * @return die Liste der Profile.
   * @throws ApplicationException
   */
  public static List<Profile> read(Format format) throws ApplicationException
  {
    List<Profile> result = new ArrayList<Profile>();

    if (format == null)
    {
      Logger.warn("no format given");
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Kein Format ausgewählt"),StatusBarMessage.TYPE_ERROR));
      return result;
    }

    final Profile dp = format.getDefaultProfile();
    result.add(dp); // System-Profil wird immer vorn einsortiert
    
    // 1. Mal schauen, ob wir gespeicherte Profil fuer das Format haben
    File dir = new File(Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath(),"csv");
    if (!dir.exists())
      return result;
    
    final String name = format.getClass().getName();
    File file = new File(dir,name + ".xml");
    if (!file.exists() || !file.canRead())
      return result;
    
    Logger.info("reading csv profile " + file);
    
    try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file)))
    {
      // Checken, ob wir die Daten schon migriert haben
      if (settings.getString("profile-migration." + name,null) != null)
      {
        Logger.info("reading in new format");
        // Ist bereits migriert. Wir können per JAXB deserialisieren
        final Unmarshaller u = JAXBUtil.createUnmarshaller(ProfileConfig.class);
        final ProfileConfig c = (ProfileConfig) u.unmarshal(input);
        result.addAll(c.getProfiles());
      }
      else
      {
        // Ansonsten per Decoder.
        Logger.info("reading in old format");
        for (Profile p:LegacyProfileXmlDecoder.decode(input))
        {
          result.add(p);
        }
      }

      Logger.info("read " + (result.size() - 1) + " profiles from " + file);
      Collections.sort(result);
    }
    catch (Exception e)
    {
      Logger.error("unable to read profile " + file,e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Laden der Profile fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
    return result;
  }
  
  /**
   * Speichert die Profile.
   * @param format das Format.
   * @param profiles die zu speichernden Profile.
   * @throws ApplicationException 
   */
  public static void store(Format format, List<Profile> profiles) throws ApplicationException
  {
    if (format == null)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Kein Format ausgewählt"),StatusBarMessage.TYPE_ERROR));
      return;
    }

    if (profiles == null)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Keine Profile angegeben"),StatusBarMessage.TYPE_ERROR));
      return;
    }

    // 2. Mal schauen, ob wir ein gespeichertes Profil fuer das Format haben
    File dir = new File(Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath(),"csv");
    if (!dir.exists())
    {
      Logger.info("creating dir: " + dir);
      if (!dir.mkdirs())
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Ordner {0} kann nicht erstellt werden",dir.getAbsolutePath()),StatusBarMessage.TYPE_ERROR));
        return;
      }
    }

    final String name = format.getClass().getName();
    final File file = new File(dir,name + ".xml");
    
    Logger.info("writing csv profile " + file);
    
    final ProfileConfig conf = new ProfileConfig();
    for (Profile p:profiles)
    {
      // Das System-Profil wird nicht mit gespeichert
      if (p.isSystem())
        continue;
      
      // Ebenso Profile ohne Namen.
      if (StringUtils.trimToNull(p.getName()) == null)
        continue;

      conf.getProfiles().add(p);
    }
    
    try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file)))
    {
      final Marshaller m = JAXBUtil.createMarshaller(ProfileConfig.class);
      m.marshal(conf,os);
      settings.setAttribute("profile-migration." + name,HBCI.LONGDATEFORMAT.format(new Date()));
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to store profile " + file, e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Speichern des Profils fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Fuegt ein neues Profil hinzu.
   * @param format das Format.
   * @param profile das zu speichernde Profil.
   * @return das hinzugefuegte oder geaenderte Profil. NULL, wenn nicht gespeichert wurde.
   */
  public static Profile add(Format format, Profile profile)
  {
    try
    {
      if (profile == null || format == null)
      {
        Application.getCallback().notifyUser(i18n.tr("Kein Profil angegeben"));
        return null;
      }
      
      // Der Dialog uebernimmt auch gleich das Speichern.
      CSVProfileStoreDialog d = new CSVProfileStoreDialog(format,profile);
      return (Profile) d.open();
    }
    catch (OperationCanceledException oce)
    {
      // ignore
    }
    catch (Exception e)
    {
      Logger.error("unable to delete profile",e);
    }
    return null;
  }

  /**
   * Loescht das angegebene Profil.
   * @param format das Format.
   * @param profile das zu speichernde Profil.
   * @return true, wenn das Profil geloescht wurde.
   * @throws ApplicationException 
   */
  public static boolean delete(Format format, Profile profile) throws ApplicationException
  {
    if (profile == null || format == null)
      return false;
    
    try
    {
      if (profile.isSystem())
      {
        Application.getCallback().notifyUser(i18n.tr("Das Default-Profil darf nicht gelöscht werden"));
        return false;
      }
      
      List<Profile> profiles = ProfileUtil.read(format);
      boolean found = false;
      for (Profile p:profiles)
      {
        if (p.isSystem())
          continue;
        
        if (p.getName().equals(profile.getName()))
        {
          profiles.remove(p);
          found = true;
          break;
        }
      }
      
      // Nichts zum Loeschen gefunden
      if (!found)
        return false;
      
      // Speichern
      ProfileUtil.store(format,profiles);
      return true;
    }
    catch (OperationCanceledException oce)
    {
      // ignore
    }
    catch (Exception e)
    {
      Logger.error("unable to delete profile",e);
    }
    return false;
  }
}

