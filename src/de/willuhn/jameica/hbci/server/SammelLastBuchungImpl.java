/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/SammelLastBuchungImpl.java,v $
 * $Revision: 1.8 $
 * $Date: 2010/04/27 11:02:32 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;

/**
 * Implementierung einer einzelnen Buchung einer Sammellastschrift.
 * @author willuhn
 */
public class SammelLastBuchungImpl extends AbstractSammelTransferBuchungImpl implements SammelLastBuchung
{

  /**
   * @throws java.rmi.RemoteException
   */
  public SammelLastBuchungImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "slastbuchung";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String arg0) throws RemoteException
  {
    if ("slastschrift_id".equals(arg0))
      return SammelLastschrift.class;

    return super.getForeignObject(arg0);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#getSammelTransfer()
   */
  public SammelTransfer getSammelTransfer() throws RemoteException
  {
    return (SammelLastschrift) getAttribute("slastschrift_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setSammelTransfer(de.willuhn.jameica.hbci.rmi.SammelTransfer)
   */
  public void setSammelTransfer(SammelTransfer s) throws RemoteException
  {
    setAttribute("slastschrift_id",s);
  }
}
