/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/SammelUeberweisungBuchungImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/09/30 00:08:50 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;

/**
 * Implementierung einer einzelnen Buchung einer Sammel-Ueberweisung.
 * @author willuhn
 */
public class SammelUeberweisungBuchungImpl extends AbstractSammelTransferBuchungImpl implements SammelUeberweisungBuchung
{

  /**
   * @throws java.rmi.RemoteException
   */
  public SammelUeberweisungBuchungImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "sueberweisungbuchung";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  protected Class getForeignObject(String arg0) throws RemoteException
  {
    if ("sueberweisung_id".equals(arg0))
      return SammelUeberweisung.class;
    return null;
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#getSammelTransfer()
   */
  public SammelTransfer getSammelTransfer() throws RemoteException
  {
    return (SammelUeberweisung) getAttribute("sueberweisung_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.SammelTransferBuchung#setSammelTransfer(de.willuhn.jameica.hbci.rmi.SammelTransfer)
   */
  public void setSammelTransfer(SammelTransfer s) throws RemoteException
  {
    setAttribute("sueberweisung_id",s);
  }
}

/*****************************************************************************
 * $Log: SammelUeberweisungBuchungImpl.java,v $
 * Revision 1.1  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
*****************************************************************************/