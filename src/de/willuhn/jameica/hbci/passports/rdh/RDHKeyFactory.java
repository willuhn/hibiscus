/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/RDHKeyFactory.java,v $
 * $Revision: 1.4 $
 * $Date: 2012/03/28 22:47:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.rdh;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;

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
   */
  public static synchronized void createKey(File f)
	{
    try
    {
      Logger.info("creating new key in " + f);
      if (f == null)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte wählen Sie eine Schlüsseldatei aus"),StatusBarMessage.TYPE_ERROR));
        return;
      }

      KeyFormatDialog d = new KeyFormatDialog(KeyFormatDialog.POSITION_CENTER,KeyFormat.FEATURE_CREATE);
      KeyFormat format = (KeyFormat) d.open();
      addKey(format.createKey(f));
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Schlüsseldatei erfolgreich erstellt"),StatusBarMessage.TYPE_SUCCESS));
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
			throw new ApplicationException(i18n.tr("Bitte erstellen Sie zuerst eine Schlüsseldiskette"));

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
        for (int j=0;j<verdrahtet.length;++j)
        {
          Konto k = verdrahtet[j];
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
      throw new ApplicationException(i18n.tr("Zu verwendende Schlüsseldiskette nicht eindeutig ermittelbar. Bitte fest verknüpfen"));

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
      throw new ApplicationException(i18n.tr("Fehler bei der Auswahl der Schlüsseldiskette"));
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

		ArrayList readable = new ArrayList();
		for (int i=0;i<found.length;++i)
		{
			if (found[i] == null || found[i].length() == 0)
			  continue;
      readable.add(new RDHKeyImpl(new File(found[i])));
		}
		return PseudoIterator.fromArray((RDHKey[]) readable.toArray(new RDHKey[readable.size()]));
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
      ArrayList newList = new ArrayList();
      
      if (existing.length == 0)
        return; // Nichts zu entfernen

      File file = new File(key.getFilename());
      for (int i=0;i<existing.length;++i)
      {
        File f = new File(existing[i]);
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


/**********************************************************************
 * $Log: RDHKeyFactory.java,v $
 * Revision 1.4  2012/03/28 22:47:18  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.3  2011-06-17 08:49:19  willuhn
 * @N Contextmenu im Tree mit den Bank-Zugaengen
 * @N Loeschen von Bank-Zugaengen direkt im Tree
 *
 * Revision 1.2  2011-04-26 12:15:51  willuhn
 * @B Potentielle Bugs gemaess Code-Checker
 *
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.38  2009/03/29 22:25:56  willuhn
 * @B Warte-Dialog wurde nicht angezeigt, wenn Schluesseldiskette nicht eingelegt
 *
 * Revision 1.37  2009/03/29 22:04:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.36  2008/10/01 12:03:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.35  2008/07/28 09:00:40  willuhn
 * @B NPE
 *
 * Revision 1.34  2008/07/28 08:35:44  willuhn
 * @N Finder-Methode fuer Schluesselformate in RDHKeyFactory verschoben
 *
 * Revision 1.33  2008/07/25 11:34:56  willuhn
 * @B Bugfixing
 *
 * Revision 1.32  2008/07/25 11:06:08  willuhn
 * @N RDH-2 Format
 * @C Haufenweise Code-Cleanup
 *
 * Revision 1.31  2008/07/24 23:36:20  willuhn
 * @N Komplette Umstellung der Schluessel-Verwaltung. Damit koennen jetzt externe Schluesselformate erheblich besser angebunden werden.
 * ACHTUNG - UNGETESTETER CODE - BITTE NOCH NICHT VERWENDEN
 *
 * Revision 1.30  2008/05/23 08:53:21  willuhn
 * @C Schluesseldateien beim Loeschen nicht mehr physisch loeschen
 *
 * Revision 1.29  2008/05/05 09:26:44  willuhn
 * @B currentHandle wurde nicht korrekt zurueckgesetzt
 *
 * Revision 1.28  2008/01/03 16:54:48  willuhn
 * @B Beim Editieren eine RDH-Passports wurde das zugehoerige PassportHandle nicht im Callback registriert - daher konnte der Benutzer nicht nach dem Passwort gefragt werden und Hibiscus hat nach einem dynamisch generierten Passwort gesucht, was fehlschlaegt
 *
 * Revision 1.27  2007/05/30 14:48:50  willuhn
 * @N Bug 314
 *
 * Revision 1.26  2006/10/12 11:29:17  willuhn
 * @B bug 289
 *
 * Revision 1.25  2006/10/10 23:24:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.24  2006/01/23 17:19:48  willuhn
 * @B bug 155
 *
 * Revision 1.23  2006/01/22 23:42:31  willuhn
 * @B bug 173
 *
 * Revision 1.22  2005/11/14 15:21:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.21  2005/11/14 12:22:31  willuhn
 * @B bug 148
 *
 * Revision 1.20  2005/11/14 11:00:18  willuhn
 * @B bug 148
 *
 * Revision 1.19  2005/06/23 22:39:44  web0
 * *** empty log message ***
 *
 * Revision 1.18  2005/06/21 21:45:06  web0
 * @B bug 80
 *
 * Revision 1.17  2005/06/21 20:18:48  web0
 * *** empty log message ***
 *
 * Revision 1.16  2005/06/13 09:56:25  web0
 * *** empty log message ***
 *
 * Revision 1.15  2005/06/13 09:34:21  web0
 * *** empty log message ***
 *
 * Revision 1.14  2005/06/06 22:57:53  web0
 * @B bug 72
 *
 * Revision 1.13  2005/03/11 02:44:17  web0
 * *** empty log message ***
 *
 * Revision 1.12  2005/03/09 01:07:16  web0
 * @D javadoc fixes
 *
 * Revision 1.11  2005/03/07 17:17:16  web0
 * *** empty log message ***
 *
 * Revision 1.10  2005/02/28 15:08:24  web0
 * @N autodetection of right key
 *
 * Revision 1.9  2005/02/20 19:04:21  willuhn
 * @B Bug 7
 *
 * Revision 1.8  2005/02/19 16:49:15  willuhn
 * @B bug 11
 *
 * Revision 1.7  2005/02/08 22:26:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.6  2005/02/08 18:34:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2005/02/07 22:06:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2005/02/02 16:15:35  willuhn
 * @N Erstellung neuer Schluessel
 * @N Schluessel-Import
 * @N Schluessel-Auswahl
 * @N Passport scharfgeschaltet
 *
 * Revision 1.3  2005/02/01 18:26:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/01/09 23:21:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/09 18:48:27  willuhn
 * @N native lib for sizrdh
 *
 **********************************************************************/