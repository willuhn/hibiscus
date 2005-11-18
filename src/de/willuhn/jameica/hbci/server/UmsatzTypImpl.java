/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/UmsatzTypImpl.java,v $
 * $Revision: 1.13 $
 * $Date: 2005/11/18 00:43:29 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.rmi.UmsatzZuordnung;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung eines Umsatz-Typs.
 */
public class UmsatzTypImpl extends AbstractPatternImpl implements UmsatzTyp {

  /**
   * ct.
   * @throws RemoteException
   */
  public UmsatzTypImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName() {
    return "umsatztyp";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
   */
  protected void insertCheck() throws ApplicationException {
		try {
			if (getName() == null || getName().length() == 0)
				throw new ApplicationException(i18n.tr("Bitte geben Sie eine Bezeichnung ein."));
		}
		catch (RemoteException e)
		{
			Logger.error("error while insert check",e);
			throw new ApplicationException(i18n.tr("Fehler beim Speichern des Umsatz-Typs."));
		}
    super.insertCheck();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
   */
  protected void updateCheck() throws ApplicationException {
		insertCheck();
  }

  /**
   * @see de.willuhn.datasource.rmi.DBObject#delete()
   */
  public void delete() throws RemoteException, ApplicationException {

		try {
			this.transactionBegin();

		
			// wir entfernen uns aus allen Umsaetzen
			DBIterator list = getUmsatzZuordnungen();
			UmsatzZuordnung u = null;
			while (list.hasNext())
			{
				u = (UmsatzZuordnung) list.next();
        u.delete();
			}

			// Jetzt koennen wir uns selbst loeschen
			super.delete();
			this.transactionCommit();
		}
		catch (RemoteException e)
		{
			this.transactionRollback();
			throw e;
		}
		catch (ApplicationException e2)
		{
			this.transactionRollback();
			throw e2;
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.UmsatzTyp#getUmsatzZuordnungen()
   */
  public DBIterator getUmsatzZuordnungen() throws RemoteException
  {
    DBIterator list = getService().createList(UmsatzZuordnung.class);
    list.addFilter("umsatztyp_id = " + getID());
    return list;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.filter.Pattern#getNameForField(java.lang.String)
   */
  public String getNameForField(String field) throws RemoteException
  {
    if (field == null)
      return null;
    if ("empfaenger_konto".equals(field))
      return i18n.tr("Kontonummer Gegenkonto");
    if ("empfaenger_blz".equals(field))
      return i18n.tr("BLZ Gegenkonto");
    if ("empfaenger_name".equals(field))
      return i18n.tr("Inhaber Gegenkonto");
    if ("zweck".equals(field))
      return i18n.tr("Verwendungszweck");
    throw new RemoteException("invalid field " + field);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.filter.Pattern#getValidFields()
   */
  public String[] getValidFields() throws RemoteException
  {
    return new String[]
      {
        "empfaenger_konto", 
        "empfaenger_blz",
        "empfaenger_name",
        "zweck"
      };
  }
}


/**********************************************************************
 * $Log: UmsatzTypImpl.java,v $
 * Revision 1.13  2005/11/18 00:43:29  willuhn
 * @B bug 21
 *
 * Revision 1.12  2005/11/14 23:47:20  willuhn
 * @N added first code for umsatz categories
 *
 * Revision 1.11  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.10  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.9  2004/11/12 18:25:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/08/18 23:13:51  willuhn
 * @D Javadoc
 *
 * Revision 1.7  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.6  2004/07/23 15:51:44  willuhn
 * @C Rest des Refactorings
 *
 * Revision 1.5  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/07/13 22:20:37  willuhn
 * @N Code fuer DauerAuftraege
 * @C paar Funktionsnamen umbenannt
 *
 * Revision 1.3  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/06/17 00:14:10  willuhn
 * @N GenericObject, GenericIterator
 *
 * Revision 1.1  2004/05/25 23:23:17  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 **********************************************************************/