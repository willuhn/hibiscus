/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/SammelLastschriftImpl.java,v $
 * $Revision: 1.16 $
 * $Date: 2010/01/31 14:14:14 $
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
    list.setOrder("order by gegenkonto_name,id");
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
