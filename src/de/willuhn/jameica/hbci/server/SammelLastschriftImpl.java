/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/SammelLastschriftImpl.java,v $
 * $Revision: 1.13 $
 * $Date: 2006/08/17 10:06:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung des Containers fuer Sammellastschrift-Buchungen.
 * @author willuhn
 */
public class SammelLastschriftImpl extends AbstractSammelTransferImpl
  implements SammelLastschrift
{

  /**
   * ct.
   * @throws java.rmi.RemoteException
   */
  public SammelLastschriftImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "slastschrift";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelLastschrift#getBuchungen()
   */
  public DBIterator getBuchungen() throws RemoteException
  {
    DBIterator list = this.getService().createList(SammelLastBuchung.class);
    list.addFilter("slastschrift_id = " + this.getID());
    return list;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransfer#createBuchung()
   */
  public SammelTransferBuchung createBuchung() throws RemoteException, ApplicationException
  {
    SammelLastBuchung b = (SammelLastBuchung) this.getService().createObject(SammelLastBuchung.class,null);
    if (this.isNewObject())
      store();
    b.setSammelTransfer(this);
    return b;
  }

}

/*****************************************************************************
 * $Log: SammelLastschriftImpl.java,v $
 * Revision 1.13  2006/08/17 10:06:32  willuhn
 * @B Fehler in HTML-Export von Sammeltransfers
 *
 * Revision 1.12  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.11  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.10  2005/08/22 10:36:38  willuhn
 * @N bug 115, 116
 *
 * Revision 1.9  2005/08/02 20:09:33  web0
 * @B bug 106
 *
 * Revision 1.8  2005/07/04 11:36:53  web0
 * @B bug 89
 *
 * Revision 1.7  2005/06/23 21:13:03  web0
 * @B bug 84
 *
 * Revision 1.6  2005/06/23 17:36:33  web0
 * @B bug 84
 *
 * Revision 1.5  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.3  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.2  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
*****************************************************************************/