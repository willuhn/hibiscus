/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/server/PassportImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/06/17 11:26:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.passports.rdh.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.rdh.RDHKeyFactory;
import de.willuhn.jameica.hbci.passports.rdh.View;
import de.willuhn.jameica.hbci.passports.rdh.rmi.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung des Passports fuer Schluesseldiskette.
 * @author willuhn
 */
public class PassportImpl extends UnicastRemoteObject implements Passport
{

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private Konto konto = null;

  /**
   * ct.
   * @throws RemoteException
   */
  public PassportImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getName()
   */
  public String getName() throws RemoteException
  {
    return i18n.tr("Schlüsseldiskette");
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getInfo()
   */
  public String getInfo() throws RemoteException
  {
    GenericIterator i = RDHKeyFactory.getKeys();
    return i18n.tr("vorhandene Schlüsseldisketten: {0}",Integer.toString(i.size()));
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getHandle()
   */
  public PassportHandle getHandle() throws RemoteException
  {
    return new PassportHandleImpl(this);
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#getConfigDialog()
   */
  public Class getConfigDialog() throws RemoteException
  {
    return View.class;
  }

  /**
   * @see de.willuhn.jameica.hbci.passport.Passport#init(de.willuhn.jameica.hbci.rmi.Konto)
   */
  public void init(Konto konto) throws RemoteException
  {
  	this.konto = konto;
  }

	/**
	 * Liefert das Konto, fuer das der Passport gerade zustaendig ist.
   * @return Konto.
   */
  protected Konto getKonto()
	{
		return konto;
	}
}

/*****************************************************************************
 * $Log: PassportImpl.java,v $
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.11  2010/04/22 12:08:39  willuhn
 * @R "Backend" wieder entfernt - Offline-Support geht im Konto mit einem "FLAG_OFFLINE" doch bequemer
 *
 * Revision 1.10  2010/04/21 23:14:59  willuhn
 * @N Ralfs Patch fuer Offline-Konten
 * @N Neue Funktion "getBackend()" und erweitertes Build-Script mit "deploy"-Target zu Hibiscus
 *
 * Revision 1.9  2010/04/14 16:58:01  willuhn
 * @N BUGZILLA 471
 *
 * Revision 1.8  2010/04/14 16:50:57  willuhn
 * @N BUGZILLA 471
 *
 * Revision 1.7  2006/01/22 23:42:31  willuhn
 * @B bug 173
 *
 * Revision 1.6  2005/11/14 12:22:31  willuhn
 * @B bug 148
 *
 * Revision 1.5  2005/03/09 01:07:16  web0
 * @D javadoc fixes
 *
 * Revision 1.4  2005/02/20 19:04:21  willuhn
 * @B Bug 7
 *
 * Revision 1.3  2005/02/06 17:46:22  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/01/05 15:35:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/05 15:32:28  willuhn
 * @N initial import
 *
*****************************************************************************/