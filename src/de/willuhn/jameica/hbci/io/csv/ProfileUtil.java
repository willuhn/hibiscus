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

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.CSVProfileStoreDialog;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Hilfeklasse zum Laden und Speichern von CSV-Import-Profilen.
 */
public class ProfileUtil
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * Laedt die vorhandenen Profile fuer das Format.
   * @param format das Format.
   * @return die Liste der Profile.
   */
  public static List<Profile> read(Format format)
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
    
    File file = new File(dir,format.getClass().getName() + ".xml");
    if (!file.exists() || !file.canRead())
      return result;
    
    Logger.info("reading csv profile " + file);
    try (XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(file))))
    {
      decoder.setExceptionListener(new ExceptionListener()
      {
        public void exceptionThrown(Exception e)
        {
          throw new RuntimeException(e);
        }
      });
      
      
      // Es ist tatsaechlich so, dass "readObject()" nicht etwa NULL liefert, wenn keine Objekte mehr in der
      // Datei sind sondern eine ArrayIndexOutOfBoundsException wirft.
      try
      {
        for (int i=0;i<1000;++i)
        {
          Profile p = (Profile) decoder.readObject();
          // Migration aus der Zeit vor dem Support mulitpler Profile:
          // Da konnte der User nur das eine existierende Profil aendern, es wurde automatisch gespeichert
          // Das hatte gar keinen Namen. Falls also ein Profil ohne Name existiert (inzwischen koennen keine
          // mehr ohne Name gespeichert werden), dann ist es das vom User geaenderte Profil. Das machen wir
          // automatisch zum ersten User-spezifischen Profil
          if (StringUtils.trimToNull(p.getName()) == null)
          {
            p.setName(dp.getName() + " 2");
            p.setSystem(false);
          }
          result.add(p);
        }
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
        // EOF
      }
      
      Logger.info("read " + (result.size() - 1) + " profiles from " + file);
      Collections.sort(result);
      
      // Der User hat beim letzten Mal eventuell nicht alle Spalten zugeordnet.
      // Die wuerden jetzt hier in dem Objekt fehlen. Daher nehmen wir
      // noch die Spalten aus dem Default-Profil und haengen die fehlenden noch an.
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
   */
  public static void store(Format format, List<Profile> profiles)
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
    
    File file = new File(dir,format.getClass().getName() + ".xml");
    
    Logger.info("writing csv profile " + file);
    try (XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file))))
    {
      encoder.setExceptionListener(new ExceptionListener()
      {
        public void exceptionThrown(Exception e)
        {
          throw new RuntimeException(e);
        }
      });
      
      for (Profile p:profiles)
      {
        // Das System-Profil wird nicht mit gespeichert
        if (p.isSystem())
          continue;
        
        // Ebenso Profile ohne Namen.
        if (StringUtils.trimToNull(p.getName()) == null)
          continue;
        
        encoder.writeObject(p);
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to store profile " + file, e);
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
   */
  public static boolean delete(Format format, Profile profile)
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


