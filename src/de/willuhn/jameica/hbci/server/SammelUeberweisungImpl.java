/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/SammelUeberweisungImpl.java,v $
 * $Revision: 1.5 $
 * $Date: 2010/01/31 14:14:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung des Containers fuer Sammellastschrift-Buchungen.
 * @author willuhn
 */
public class SammelUeberweisungImpl extends AbstractSammelTransferImpl
  implements SammelUeberweisung
{

  /**
   * ct.
   * @throws java.rmi.RemoteException
   */
  public SammelUeberweisungImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "sueberweisung";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransfer#getBuchungen()
   */
  public DBIterator getBuchungen() throws RemoteException
  {
    DBIterator list = this.getService().createList(SammelUeberweisungBuchung.class);
    list.addFilter("sueberweisung_id = " + this.getID());
    list.setOrder("order by gegenkonto_name,id");
    return list;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransfer#createBuchung()
   */
  public SammelTransferBuchung createBuchung() throws RemoteException, ApplicationException
  {
    SammelUeberweisungBuchung b = (SammelUeberweisungBuchung) this.getService().createObject(SammelUeberweisungBuchung.class,null);
    if (this.isNewObject())
      store();
    b.setSammelTransfer(this);
    return b;
  }
}
