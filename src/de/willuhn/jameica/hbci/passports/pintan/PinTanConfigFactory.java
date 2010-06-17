/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/PinTanConfigFactory.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/06/17 11:38:15 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.passports.pintan;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;

import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.hbci.passports.pintan.server.PinTanConfigImpl;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Factory zum Laden, Erzeugen und Finden von PIN/TAN-Konfigurationen. 
 * @author willuhn
 */
public class PinTanConfigFactory
{
  private static Settings settings = new Settings(PinTanConfigFactory.class);
  private static I18N i18n;
  
  static
  {
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }


  /**
   * Erstellt eine neue PIN/Tan-Config.
   * @return neue Config.
   * @throws Exception
   */
  public static synchronized PinTanConfig create() throws Exception
  {
  	File f = createFilename();
  	HBCIPassport p = load(f);
    return new PinTanConfigImpl(p,f);
  }

  /**
   * Speichert die Konfiguration.
   * @param config
   * @throws Exception
   */
  public static synchronized void store(PinTanConfig config) throws Exception
  {
    if (config == null || config.getID() == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu speichernde Konfiguration aus"));

    Logger.info("storing pin/tan config");

    String[] existing = settings.getList("config",new String[0]);

		boolean found = false;
		if (existing != null && existing.length > 0)
		{
			for (int i=0;i<existing.length;++i)
			{
				if (existing[i].equals(config.getID()))
				{
					Logger.info("updating existing config");
					found = true;
					break;
				}
			}
		}

		if (!found)
		{
			Logger.info("adding new pin/tan config");
			String[] newList = new String[existing.length+1];
			System.arraycopy(existing,0,newList,0,existing.length);
			newList[existing.length] = config.getID();
			settings.setAttribute("config",newList);
		}

		HBCIPassport p = config.getPassport();
		if (found)
		{
	    Logger.info("clear bpd cache");
	    p.clearBPD();
		}
    Logger.info("saving passport config");
    p.saveChanges();
  }

  /**
   * Loescht die genannte Config.
   * @param config die zu loeschende Config.
   * @throws Exception
   */
  public static synchronized void delete(PinTanConfig config) throws Exception
  {
    if (config == null || config.getID() == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu löschende Konfiguration aus"));

    String[] existing = settings.getList("config",new String[0]);

    if (existing.length == 0)
    {
      Logger.info("no configs found, nothing to delete");
      return;
    }

    Logger.debug("number of configs: " + existing.length);
    ArrayList newList = new ArrayList();
    String id = config.getID();

    for (int i=0;i<existing.length;++i)
    {
      if (id.equals(existing[i]))
      {
        Logger.info("deleting config for file " + id);
        continue;
      }
      newList.add(existing[i]);
    }
    
    Logger.debug("new number of configs: " + newList.size());
    settings.setAttribute("config",(String[]) newList.toArray(new String[newList.size()]));
  }

  /**
   * Erzeugt ein Passport-Objekt basierend auf der uebergebenen Config.
   * @param f das HBCI4Java-Config-File.
   * @return Passport.
   */
  public static HBCIPassport load(File f)
  {
    HBCIUtils.setParam("client.passport.default","PinTan");
    HBCIUtils.setParam("client.passport.PinTan.filename",f.getAbsolutePath());
    HBCIUtils.setParam("client.passport.PinTan.init","1");

    HBCIUtils.setParam("client.passport.PinTan.checkcert","1");
    return AbstractHBCIPassport.getInstance("PinTan");
  }

  /**
   * Liefert die zum uebergebenen Konto gehoerende PIN/Tan-Config oder <code>null</code> wenn keine gefunden wurde.
   * @param konto Konto, fuer das die Config gesucht wird.
   * @return Pin/Tan-config des Kontos oder null wenn keine gefunden wurde.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static synchronized PinTanConfig findByKonto(Konto konto) throws RemoteException, ApplicationException
  {

		GenericIterator i = getConfigs();
		if (!i.hasNext())
			throw new ApplicationException(i18n.tr("Bitte legen Sie zuerst eine PIN/TAN-Konfiguration an"));

    Logger.info("searching config for konto " + konto.getKontonummer() + ", blz: " + konto.getBLZ());
    PinTanConfig config = null;

    ArrayList found = new ArrayList();
    while (i.hasNext())
    {
      config = (PinTanConfig) i.next();

      // BUGZILLA 173
      Konto[] verdrahtet = config.getKonten();
      if (konto != null && verdrahtet != null && verdrahtet.length > 0)
      {
        for (int j=0;j<verdrahtet.length;++j)
        {
          Konto k = verdrahtet[j];
          if (konto.equals(k))
          {
            Logger.info("found config via account. url: " + config.getURL());
            return config;
          }
        }
      }
      String blz = config.getBLZ();
      if (blz != null && blz.equals(konto.getBLZ()))
      {
        Logger.info("found config. url: " + config.getURL());
        found.add(config);
      }
    }

    if (found.size() == 1)
    {
      config = (PinTanConfig) found.get(0);
      Logger.info("using config. url: " + config.getURL());
      return config;
    }


    String text = i18n.tr("Mehrere zutreffende Konfigurationen gefunden. Bitte wählen Sie eine manuell aus.");
    
    if (found.size() == 0)
    {
      Logger.warn("no config found for this konto. Asking user");
      text = i18n.tr("Keine zutreffende Konfigurationen gefunden. Bitte wählen Sie eine manuell aus.");
    }
    
    // Wir haben mehrere zur Auswahl. Lassen wir den User entscheiden.
    SelectConfigDialog d = new SelectConfigDialog(SelectConfigDialog.POSITION_CENTER);
    d.setText(text);
    try
    {
      config = (PinTanConfig) d.open();
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("error while choosing config",e);
      throw new ApplicationException(i18n.tr("Fehler bei der Auswahl der PIN/TAN-Konfiguration"));
    }
    return config;
  }

  /**
   * Liefert die Liste der existierenden Pin/Tan-Konfigurationen.
   * @return Liste der Konfigurationen.
   * @throws RemoteException
   */
  public static synchronized GenericIterator getConfigs() throws RemoteException
  {
    migrateToRelative();
    String[] found = settings.getList("config",new String[0]);

    ArrayList configs = new ArrayList();
    for (int i=0;i<found.length;++i)
    {
      if (found[i] != null && found[i].length() > 0)
      {
      	File f = toAbsolutePath(found[i]);
        if (!f.exists())
          continue;
        
        try
        {
          HBCIPassport p = load(f);
          configs.add(new PinTanConfigImpl(p,f));
        }
        catch (Exception e)
        {
          Logger.error("unable to load config " + f.getAbsolutePath() + " - skipping",e);
        }
      }
    }
    return PseudoIterator.fromArray((PinTanConfig[]) configs.toArray(new PinTanConfig[configs.size()]));
  }

  /**
   * Erzeugt eine neue Config-Datei.
   * @return Passport-File.
   * @throws ApplicationException
   */
  public static File createFilename() throws ApplicationException
  {
    String wp = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath();
    File f = new File(wp + File.separator + "passports",System.currentTimeMillis() + ".pt");
    
    int retry = 0;
    while (f.exists())
    {
      if (retry > 20)
        throw new ApplicationException(i18n.tr("Configdatei {0} existiert bereits",f.getAbsolutePath()));
      f = new File(wp + File.separator + "passports",System.currentTimeMillis() + "-" + (++retry) + ".pt");
    }
    return f;
  }
  
  /**
   * Macht aus dem Dateinamen einer Passport-Datei eine absolute Pfadangabe.
   * Die Funktion erkennt selbst, ob es sich bereits um eine absolute Pfadangabe
   * handelt und liefert den Pfad in dem Fall unveraendert zurueck.
   * BUGZILLA 276
   * @param filename Dateiname.
   * @return Absolute Pfadangabe
   */
  public static File toAbsolutePath(String filename)
  {
    File f = new File(filename);
    if (f.canRead() && f.isFile()) // Ist bereits eine absolute Pfadangabe
      return f;
    
    String wp = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath();
    return new File(wp + File.separator + "passports",filename);
  }
  
  /**
   * Macht aus der Pfadangabe eine relative Angabe - enthaelt dann also nur noch den Dateinamen.
   * Die Funktion erkennt selbst, ob es sich bereits um einen Dateinamen ohne
   * Pfadangabe handelt und gibt den Namen in dem Fall unveraendert zurueck.
   * BUGZILLA 276
   * @param file Pfadangabe.
   * @return Dateiname.
   */
  public static String toRelativePath(String file)
  {
    return new File(file).getName();
  }
  
  private static boolean inMigration = false;
  
  /**
   * Migriert alle Pfadangaben zu relativen Angaben.
   * BUGZILLA 276
   */
  private static void migrateToRelative() throws RemoteException
  {
    if (inMigration)
      return;
    
    inMigration = true;
    try
    {
      if (settings.getString("migration.276", null) != null)
        return;
      
      Logger.info("migrating passport filenames to relative pathnames");
      
      // Schritt 1: Die Registry selbst
      String[] absolute = settings.getList("config",new String[0]);
      String[] relative = new String[absolute.length];
      for (int i=0;i<absolute.length;++i)
      {
        relative[i] = toRelativePath(absolute[i]);
        Logger.info("  " + absolute[i] + " -> " + relative[i]);
      }
      settings.setAttribute("config",relative);
      
      // Schritt 2: Die Einstellungen der einzelnen Passports
      Settings ppSettings = new Settings(PinTanConfig.class);
      absolute = ppSettings.getAttributes();
      for (int i=0;i<absolute.length;++i)
      {
        // Neuen Wert schreiben
        String rel = toRelativePath(absolute[i]);
        ppSettings.setAttribute(rel,ppSettings.getString(absolute[i],null));
        
        // Alten Wert loeschen
        ppSettings.setAttribute(absolute[i],(String) null);
      }
      
      settings.setAttribute("migration.276", HBCI.DATEFORMAT.format(new Date()));
    }
    finally
    {
      inMigration = false;
    }
  }
}

/*****************************************************************************
 * $Log: PinTanConfigFactory.java,v $
 * Revision 1.1  2010/06/17 11:38:15  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.21  2009/06/29 11:04:17  willuhn
 * @N Beim Speichern existierender Konfigurationen wird der BPD-Cache geloescht. Das soll Fehler bei VR-Banken vermeiden, nachdem dort die HBCI-Version geaendert wurde
 *
 * Revision 1.20  2008/01/22 15:00:44  willuhn
 * @B einzelne defekte Config ueberspringen
 *
 * Revision 1.19  2007/11/25 16:42:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2007/11/25 16:37:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2007/11/25 16:32:34  willuhn
 * @N Bug 276
 *
 * Revision 1.16  2007/11/25 16:20:11  willuhn
 * @N Bug 276
 *
 * Revision 1.15  2007/08/31 09:43:55  willuhn
 * @N Einer PIN/TAN-Config koennen jetzt mehrere Konten zugeordnet werden
 *
 * Revision 1.14  2007/08/30 23:35:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2007/07/05 09:59:00  willuhn
 * @N verdrahtete Konfigurationen aus der Liste von moeglichen Treffern streichen
 *
 * Revision 1.12  2006/01/10 22:34:07  willuhn
 * @B bug 173
 *
 * Revision 1.11  2005/07/18 12:53:30  web0
 * @B bug 96
 *
 * Revision 1.10  2005/06/23 22:33:22  web0
 * *** empty log message ***
 *
 * Revision 1.9  2005/06/21 20:19:04  web0
 * *** empty log message ***
 *
 * Revision 1.8  2005/06/09 23:06:02  web0
 * @N certificate checking activated
 *
 * Revision 1.7  2005/04/27 00:30:12  web0
 * @N real test connection
 * @N all hbci versions are now shown in select box
 * @C userid and customerid are changable
 *
 * Revision 1.6  2005/03/11 02:43:59  web0
 * @N PIN/TAN works ;)
 *
 * Revision 1.5  2005/03/11 00:49:30  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/03/10 18:38:48  web0
 * @N more PinTan Code
 *
 * Revision 1.3  2005/03/09 17:24:40  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/03/08 18:44:57  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/03/07 17:17:30  web0
 * *** empty log message ***
 *
*****************************************************************************/