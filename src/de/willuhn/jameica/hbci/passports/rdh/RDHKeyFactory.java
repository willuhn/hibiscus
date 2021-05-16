/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.rdh;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.rdh.keyformat.KeyFormat;
import de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey;
import de.willuhn.jameica.hbci.passports.rdh.server.RDHKeyImpl;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.MultipleClassLoader;

/**
 * Diese Klasse verwaltet die RDH-Schluessel.
 */
public class RDHKeyFactory
{

	private static Settings settings = new Settings(RDHKeyFactory.class);

	private static I18N i18n;
	
	static
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
	}
  
  /**
   * Durchsucht den Classpath nach unterstuetzten Schluesselformaten.
   * @param neededFeature das benoetigte Feature der Datei.
   * @return Liste der gefundenen Schluesselformate.
   * @see KeyFormat#FEATURE_CREATE
   * @see KeyFormat#FEATURE_IMPORT
   */
  public static KeyFormat[] getKeyFormats(int neededFeature)
  {
    ArrayList list = new ArrayList();
    try
    {
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      MultipleClassLoader loader = Application.getPluginLoader().getManifest(HBCI.class).getClassLoader();
      Class[] classes = loader.getClassFinder().findImplementors(KeyFormat.class);
      for (Class c:classes)
      {
        try
        {
          KeyFormat format = (KeyFormat) service.get(c);
          if (!format.hasFeature(neededFeature))
            continue;
          list.add(format);
        }
        catch (Exception e)
        {
          Logger.error("unable to load key format " + c + " - skipping",e);
        }
      }
    }
    catch (ClassNotFoundException cne)
    {
      Logger.error("no key formats found",cne);
    }
    catch (Exception e)
    {
      Logger.error("error while loading key formats",e);
    }
    
    Collections.sort(list);
    return (KeyFormat[]) list.toArray(new KeyFormat[list.size()]);
  }

	/**
	 * Versucht, die angegebene Datei zu importieren.
   * @param f zu importierender Schluessel.
   */
  public static void importKey(File f)
	{

		Logger.info("import rdh key " + f);
    if (f == null)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte wählen Sie eine Schlüsseldatei aus"),StatusBarMessage.TYPE_ERROR));
      return;
    }
		if (!f.canRead() || !f.isFile())
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Schlüsseldatei {0} nicht lesbar",f.getAbsolutePath()),StatusBarMessage.TYPE_ERROR));
      return;
    }
    if (!f.canWrite())
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Schreibrechte fehlen auf Schlüsseldatei {0}",f.getAbsolutePath()),StatusBarMessage.TYPE_ERROR));
      return;
    }

    try
    {
      KeyFormatDialog d = new KeyFormatDialog(KeyFormatDialog.POSITION_CENTER,KeyFormat.FEATURE_IMPORT);
      KeyFormat format = (KeyFormat) d.open();
      Logger.info("registering key, type " + format.getName() + ", " + format.getClass().getName());
      addKey(format.importKey(f));
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Schlüsseldatei erfolgreich importiert"),StatusBarMessage.TYPE_SUCCESS));
		}
    catch (OperationCanceledException oce)
    {
      Logger.warn("operation cancelled; " + oce.getMessage());
      throw oce;
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr(ae.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
		catch (Throwable t)
		{
      Logger.error("error while importing key",t);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Import des Schlüssels: {0}",t.getMessage()),StatusBarMessage.TYPE_ERROR));
		}
	}

	/**
	 * Erstellt einen neuen Schluessel from Scratch.
   * @param f die Schluesseldatei.
   * @return true, wenn die Datei korrekt registriert werden konnte.
   */
  public static synchronized boolean createKey(File f)
	{
    try
    {
      Logger.info("creating new key in " + f);
      if (f == null)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte wählen Sie eine Schlüsseldatei aus"),StatusBarMessage.TYPE_ERROR));
        return false;
      }

      // Checken, ob sich der Ordner innerhalb des Programmordners befindet
      try
      {
        String path = f.getCanonicalPath();
        String systemPath = new File(".").getCanonicalPath();
        if (path.startsWith(systemPath))
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte wählen Sie eine Datei, die sich ausserhalb des Programm-Verzeichnisses befindet"),StatusBarMessage.TYPE_ERROR));
          return false;
        }
      }
      catch (Exception e)
      {
        Logger.error("unable to check canonical path",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Datei nicht auswählbar: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
        return false;
      }
      

      final int ft = KeyFormat.FEATURE_CREATE;
      KeyFormat[] formats = RDHKeyFactory.getKeyFormats(ft);
      
      KeyFormat format = null;
      if (formats != null && formats.length == 1)
      {
        format = formats[0];
        Logger.info("only have one key format, that supports creation of new keys, choosing this one automatically: " + format.getName());
      }
      else
      {
        Logger.info("asking user which key format to be used");
        KeyFormatDialog d = new KeyFormatDialog(KeyFormatDialog.POSITION_CENTER,ft);
        format = (KeyFormat) d.open();
      }
      
      addKey(format.createKey(f));
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Schlüsseldatei erfolgreich erstellt"),StatusBarMessage.TYPE_SUCCESS));
      return true;
    }
    catch (OperationCanceledException oce)
    {
      Logger.warn("operation cancelled; " + oce.getMessage());
      throw oce;
    }
    catch (final ApplicationException ae)
    {
      Logger.error(ae.getMessage());
      
      // Meldung wurde sonst nicht in der GUI angezeigt. Siehe http://www.onlinebanking-forum.de/forum/topic.php?p=106085#real106085
      GUI.getDisplay().asyncExec(new Runnable() {
        public void run()
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
      });
    }
    catch (Throwable t)
    {
      Logger.error("error while creating key",t);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Erzeugen des Schlüssels: {0}",t.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
    return false;
	}

  /**
   * Sucht nach dem Schluessel.
   * @param konto das Konto, zu dem der Schluessel gesucht wird.
   * @return Schluessel fuer dieses Konto.
   * @throws RemoteException
   * @throws ApplicationException
   * @throws OperationCanceledException
   */
  public static synchronized RDHKey findByKonto(Konto konto) throws RemoteException, ApplicationException, OperationCanceledException
	{
		GenericIterator i = getKeys();
		if (!i.hasNext())
			throw new ApplicationException(i18n.tr("Bitte erstellen Sie zuerst eine Schlüsseldatei"));

    RDHKey key = null;
    

    ArrayList keys = new ArrayList();
		while (i.hasNext())
		{
			key = (RDHKey) i.next();
			if (!key.isEnabled())
				continue;

      // BUGZILLA 173
      Konto[] verdrahtet = key.getKonten();
      if (konto != null && verdrahtet != null && verdrahtet.length > 0)
      {
        for (Konto k : verdrahtet)
        {
          if (konto.equals(k))
          {
            Logger.info("found config via account. url: " + key.getFilename());
            return key;
          }
        }
      }
      
			keys.add(key);
    }
    
		// Wir haben nur einen in Frage kommenden Schluessel
    if (keys.size() == 1)
      return (RDHKey) keys.get(0);

    if (Application.inServerMode())
      throw new ApplicationException(i18n.tr("Zu verwendende Schlüsseldatei nicht eindeutig ermittelbar. Bitte fest verknüpfen"));

    // Nicht eindeutig
    SelectKeyDialog d = new SelectKeyDialog(SelectKeyDialog.POSITION_CENTER);
    try
    {
      return (RDHKey) d.open();
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("error while choosing key",e);
      throw new ApplicationException(i18n.tr("Fehler bei der Auswahl der Schlüsseldatei"));
    }
	}

	/**
	 * Liefert die Liste der bekannten Schluessel.
   * @return Liste der importierten Schluessel.
   * @throws RemoteException
   */
  public static synchronized GenericIterator getKeys() throws RemoteException
	{
		String[] found = settings.getList("key",new String[0]);

		ArrayList<RDHKey> readable = new ArrayList<>();
		for (String key : found)
		{
			if (key == null || key.length() == 0)
			  continue;
			readable.add(new RDHKeyImpl(new File(key)));
		}
		return PseudoIterator.fromArray(readable.toArray(new RDHKey[readable.size()]));
	}

	/**
	 * Fuegt den Key zur Liste der bekannten Schluessel hinzu.
   * @param key hinzuzufuegender Schluessel.
	 * @throws Exception
   */
  public static void addKey(RDHKey key) throws Exception
	{
    if (key == null)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte wählen Sie eine Schlüsseldatei aus"),StatusBarMessage.TYPE_ERROR));
      return;
    }
    String file = key.getFilename();
    Logger.info("adding key " + file + " to list");
    
		String[] existing = settings.getList("key",new String[0]);
		if (existing.length == 0)
		{
			// Wir sind die ersten
			settings.setAttribute("key",new String[]{file});
			return;
		}

    String[] newList = new String[existing.length+1];
    System.arraycopy(existing,0,newList,0,existing.length);
    newList[existing.length] = file;
    settings.setAttribute("key",newList);
	}

  /**
   * Entfernt einen Key aus der Liste der bekannten Schluessel.
   * @param key zu entfernender Schluessel.
   * @throws ApplicationException
   */
  public static void removeKey(RDHKey key) throws ApplicationException
  {
    if (key == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie eine Schlüsseldatei aus"));

    try
    {
      Logger.warn("removing key " + key.getFilename() + " from key registry");
      String[] existing = settings.getList("key",new String[0]);
      ArrayList<String> newList = new ArrayList<>();
      
      if (existing.length == 0)
        return; // Nichts zu entfernen

      File file = new File(key.getFilename());
      for (String k : existing)
      {
        File f = new File(k);
        if (file.equals(f))
        {
          Logger.info("removing key " + f.getAbsolutePath() + " from list");
          continue;
        }
        newList.add(f.getAbsolutePath());
        
      }
      settings.setAttribute("key",(String[]) newList.toArray(new String[newList.size()]));
    }
    catch (RemoteException re)
    {
      Logger.error("unable to remove key",re);
      throw new ApplicationException(i18n.tr("Löschen fehlgeschlagen: {0}",re.getMessage()));
    }
  }
}
