/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/server/PassportHandleImpl.java,v $
 * $Revision: 1.3 $
 * $Date: 2010/09/29 23:43:34 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.passports.rdh.server;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCICallbackSWT;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.rdh.InsertKeyDialog;
import de.willuhn.jameica.hbci.passports.rdh.RDHKeyFactory;
import de.willuhn.jameica.hbci.passports.rdh.SelectSizEntryDialog;
import de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.Converter;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * @author willuhn
 */
public class PassportHandleImpl extends UnicastRemoteObject implements PassportHandle
{
  private PassportImpl passport = null;
	private HBCIPassport hbciPassport = null;
	private HBCIHandler handler = null;
	private RDHKey key = null;


  /**
   * @param passport
   * @throws RemoteException
   */
  protected PassportHandleImpl(PassportImpl passport) throws RemoteException
  {
    super();
    this.passport = passport;
  }

  /**
   * @param key
   * @throws RemoteException
   */
  public PassportHandleImpl(RDHKey key) throws RemoteException
  {
    super();
    this.key = key;
  }

  /**
   * @throws RemoteException
   */
  public PassportHandleImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.PassportHandle#open()
   */
  public HBCIHandler open() throws RemoteException, ApplicationException
  {
		if (isOpen())
			return handler;

    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		Logger.info("open rdh passport");
		try {
	
      RDHKey activeKey = this.key != null ? this.key : RDHKeyFactory.findByKonto(passport != null ? passport.getKonto() : null);
      
      if (activeKey == null)
        throw new ApplicationException(i18n.tr("Keine Schlüssel-Diskette für dieses Konto definiert"));

      String filename = activeKey.getFilename();
      
      File f = new File(filename);
      if (!f.exists())
      {
        InsertKeyDialog kd = new InsertKeyDialog(f);
        Boolean b = (Boolean) kd.open();
        if (b == null || !b.booleanValue())
          throw new OperationCanceledException(i18n.tr("Schlüsseldiskette nicht eingelegt oder nicht lesbar"));
      }
      
      Logger.info("using passport file " + filename);

      String hbciVersion = activeKey.getHBCIVersion();
      if (hbciVersion == null)
      {
        // Bei der Neuerstellung fragen wir immer den User nach der HBCI-Version
        // Wir fragen die HBCI-Version via Messaging ab, damit sie ggf. auch
        // (z.Bsp. vom Payment-Server) automatisch beantwortet werden kann.
        QueryMessage msg = new QueryMessage(passport);
        Application.getMessagingFactory().getMessagingQueue("hibiscus.passport.rdh.hbciversion").sendSyncMessage(msg);
        Object data = msg.getData();
        if (data == null || !(data instanceof String))
          throw new ApplicationException(i18n.tr("HBCI-Version nicht ermittelbar"));
        hbciVersion = (String) msg.getData();
      }

      hbciPassport = activeKey.load();
      
      // Wir speichern die verwendete PIN/TAN-Config im Passport. Dann wissen wir
      // spaeter in den HBCI-Callbacks noch, aus welcher Config der Passport
      // erstellt wurde. Wird z.Bsp. vom Payment-Server benoetigt.
      ((AbstractHBCIPassport)hbciPassport).setPersistentData(CONTEXT_CONFIG,activeKey);

			Logger.info("using HBCI version " + hbciVersion);
			handler = new HBCIHandler(hbciVersion,hbciPassport);
			return handler;
		}
		catch (RemoteException re)
		{
			close();
			throw re;
		}
    catch (ApplicationException ae)
    {
      close();
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      close();
      throw oce;
    }
		catch (Exception e)
		{
			close();
      Logger.error("error while opening key",e);
      throw new ApplicationException(i18n.tr("Fehler beim Öffnen des Schlüssels: {0}",e.getMessage()));
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.PassportHandle#close()
   */
  public void close() throws RemoteException
  {
		if (hbciPassport == null && handler == null)
			return;
		try {
			Logger.info("closing rdh passport");
			handler.close();
		}
		catch (Exception e) {/*useless*/}
		hbciPassport = null;
		handler = null;

    AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
    HBCICallback callback = ((HBCI)plugin).getHBCICallback();
    if (callback != null && (callback instanceof HBCICallbackSWT))
      ((HBCICallbackSWT)callback).setCurrentHandle(null);

    Logger.info("rdh passport closed");
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.PassportHandle#isOpen()
   */
  public boolean isOpen() throws RemoteException
  {
		return handler != null && hbciPassport != null;
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.PassportHandle#getKonten()
   */
  public Konto[] getKonten() throws RemoteException, ApplicationException
  {
		Logger.info("reading accounts from rdh passport");
		try {
			open();
			org.kapott.hbci.structures.Konto[] konten = hbciPassport.getAccounts();
			if (konten == null || konten.length == 0)
			{
				Logger.info("no accounts found");
				return new Konto[]{};
			}

			ArrayList result = new ArrayList();
			Konto k = null;
			for (int i=0;i<konten.length;++i)
			{
				k = Converter.HBCIKonto2HibiscusKonto(konten[i], PassportImpl.class);
				Logger.debug("found account " + k.getKontonummer());
				result.add(k);
			}
			return (Konto[]) result.toArray(new Konto[result.size()]);
		}
		catch (RemoteException e)
		{
			throw e;
		}
    catch (ApplicationException ae)
    {
      throw ae;
    }
		finally
		{
			try {
				close();
			}
			catch (RemoteException e2) {/*useless*/}
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.PassportHandle#callback(org.kapott.hbci.passport.HBCIPassport, int, java.lang.String, int, java.lang.StringBuffer)
   */
  public boolean callback(HBCIPassport p, int reason, String msg, int datatype, StringBuffer retData) throws Exception
  {
    switch (reason)
    {
      case HBCICallback.NEED_SIZENTRY_SELECT:

        SelectSizEntryDialog e = new SelectSizEntryDialog(SelectSizEntryDialog.POSITION_CENTER,retData.toString());
        retData.replace(0,retData.length(),(String)e.open());
        return true;

      // Ueberschrieben, damit IMMER nach dem Passwort gefragt wird.
      case HBCICallback.NEED_PASSPHRASE_LOAD:
        retData.replace(0,retData.length(),DialogFactory.importPassport(p));
        return true;

      case HBCICallback.NEED_PASSPHRASE_SAVE:
        retData.replace(0,retData.length(),DialogFactory.exportPassport(p));
        return true;
    }
    return false;
  }

}

/*****************************************************************************
 * $Log: PassportHandleImpl.java,v $
 * Revision 1.3  2010/09/29 23:43:34  willuhn
 * @N Automatisches Abgleichen und Anlegen von Konten aus KontoFetchFromPassport in KontoMerge verschoben
 * @N Konten automatisch (mit Rueckfrage) anlegen, wenn das Testen der HBCI-Konfiguration erfolgreich war
 * @N Config-Test jetzt auch bei Schluesseldatei
 * @B in PassportHandleImpl#getKonten() wurder der Converter-Funktion seit jeher die falsche Passport-Klasse uebergeben. Da gehoerte nicht das Interface hin sondern die Impl
 *
 * Revision 1.2  2010-09-08 15:04:52  willuhn
 * @N Config des Sicherheitsmediums als Context in Passport speichern
 *
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.31  2009/03/29 22:25:56  willuhn
 * @B Warte-Dialog wurde nicht angezeigt, wenn Schluesseldiskette nicht eingelegt
 *
 * Revision 1.30  2008/07/28 09:31:13  willuhn
 * @N Abfrage der HBCI-Version via Messaging
 *
 * Revision 1.29  2008/07/25 12:56:50  willuhn
 * @B Bugfixing
 *
 * Revision 1.28  2008/07/25 11:06:08  willuhn
 * @N RDH-2 Format
 * @C Haufenweise Code-Cleanup
 *
 * Revision 1.27  2008/07/24 23:36:20  willuhn
 * @N Komplette Umstellung der Schluessel-Verwaltung. Damit koennen jetzt externe Schluesselformate erheblich besser angebunden werden.
 * ACHTUNG - UNGETESTETER CODE - BITTE NOCH NICHT VERWENDEN
 *
 * Revision 1.26  2008/01/03 16:54:48  willuhn
 * @B Beim Editieren eine RDH-Passports wurde das zugehoerige PassportHandle nicht im Callback registriert - daher konnte der Benutzer nicht nach dem Passwort gefragt werden und Hibiscus hat nach einem dynamisch generierten Passwort gesucht, was fehlschlaegt
 *
 * Revision 1.25  2006/12/21 12:10:55  willuhn
 * @N new "Insert key" dialog
 *
 * Revision 1.24  2006/10/23 14:58:39  willuhn
 * @B reset current handle
 *
 * Revision 1.23  2006/10/23 14:07:55  willuhn
 * @N Password-handling - jetzt aber ;)
 *
 * Revision 1.22  2006/10/12 12:53:02  willuhn
 * @B bug 289 + Callback NEED_SIZENTRY_SELECT
 *
 * Revision 1.21  2006/08/21 12:27:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2006/08/06 13:15:45  willuhn
 * @B bug 256
 *
 * Revision 1.19  2006/08/03 13:51:37  willuhn
 * @N Bug 62
 * @C HBCICallback-Handling nach Zustaendigkeit auf Passports verteilt
 *
 * Revision 1.18  2006/01/22 23:42:31  willuhn
 * @B bug 173
 *
 * Revision 1.17  2005/11/14 13:52:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2005/11/14 12:22:31  willuhn
 * @B bug 148
 *
 * Revision 1.15  2005/08/01 23:28:13  web0
 * *** empty log message ***
 *
 * Revision 1.14  2005/08/01 22:15:34  web0
 * *** empty log message ***
 *
 * Revision 1.13  2005/06/24 12:47:32  web0
 * *** empty log message ***
 *
 * Revision 1.12  2005/06/23 22:39:44  web0
 * *** empty log message ***
 *
 * Revision 1.11  2005/06/21 21:45:06  web0
 * @B bug 80
 *
 * Revision 1.10  2005/06/21 20:18:48  web0
 * *** empty log message ***
 *
 * Revision 1.9  2005/04/04 11:34:20  web0
 * @B bug 36
 * @B bug 37
 *
 * Revision 1.8  2005/03/23 00:05:55  web0
 * @C RDH fixes
 *
 * Revision 1.7  2005/03/09 01:07:16  web0
 * @D javadoc fixes
 *
 * Revision 1.6  2005/02/20 19:04:21  willuhn
 * @B Bug 7
 *
 * Revision 1.5  2005/02/08 22:26:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2005/02/08 18:34:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2005/02/02 18:19:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/02/02 16:15:35  willuhn
 * @N Erstellung neuer Schluessel
 * @N Schluessel-Import
 * @N Schluessel-Auswahl
 * @N Passport scharfgeschaltet
 *
 * Revision 1.1  2005/01/05 15:32:28  willuhn
 * @N initial import
 *
*****************************************************************************/