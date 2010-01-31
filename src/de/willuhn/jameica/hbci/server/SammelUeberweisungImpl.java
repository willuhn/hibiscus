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
    list.setOrder("order by gegenkonto_name");
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

/*****************************************************************************
 * $Log: SammelUeberweisungImpl.java,v $
 * Revision 1.5  2010/01/31 14:14:14  willuhn
 * @N Buchungen nach Name des Gegenkontoinhabers sortieren
 *
 * Revision 1.4  2006/08/25 10:13:43  willuhn
 * @B Fremdschluessel NICHT mittels PreparedStatement, da die sonst gequotet und von McKoi nicht gefunden werden. BUGZILLA 278
 *
 * Revision 1.3  2006/08/23 09:45:13  willuhn
 * @N Restliche DBIteratoren auf PreparedStatements umgestellt
 *
 * Revision 1.2  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.1  2005/09/30 00:08:50  willuhn
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