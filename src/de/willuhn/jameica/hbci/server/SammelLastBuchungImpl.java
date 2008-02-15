/*****************************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/SammelLastBuchungImpl.java,v $
 * $Revision: 1.7 $
 * $Date: 2008/02/15 17:39:10 $
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
import de.willuhn.jameica.hbci.rmi.Transfer;

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
    return null;
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
  
  /**
   * @see de.willuhn.jameica.hbci.rmi.Transfer#getTransferTyp()
   */
  public int getTransferTyp() throws RemoteException
  {
    return Transfer.TYP_SLAST_BUCHUNG;
  }

}

/*****************************************************************************
 * $Log: SammelLastBuchungImpl.java,v $
 * Revision 1.7  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 *
 * Revision 1.6  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.5  2005/08/22 12:23:18  willuhn
 * @N bug 107
 *
 * Revision 1.4  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/05/19 23:31:07  web0
 * @B RMI over SSL support
 * @N added handbook
 *
 * Revision 1.2  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.1  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
*****************************************************************************/