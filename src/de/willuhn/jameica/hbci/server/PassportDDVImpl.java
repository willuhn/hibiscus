/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/PassportDDVImpl.java,v $
 * $Revision: 1.12 $
 * $Date: 2004/04/19 22:05:51 $
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

import de.willuhn.jameica.hbci.rmi.PassportDDV;
import de.willuhn.jameica.hbci.rmi.hbci.PassportHandle;
import de.willuhn.jameica.hbci.server.hbci.PassportHandleDDVImpl;

/**
 * Implementierung der Persistenz des Passports vom Typ "Chipkarte" (DDV).
 */
public class PassportDDVImpl
  extends PassportImpl
  implements PassportDDV {

  /**
   * @throws RemoteException
   */
  public PassportDDVImpl() throws RemoteException {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#getPort()
   */
  public int getPort() throws RemoteException {
		try {
			return new Integer(getParam(PassportDDV.PORT)).intValue();
		}
		catch (NumberFormatException e)
		{
			// Scheinbar noch nicht definiert. Wir liefern einen Default-Wert
			return 0;
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#setPort(int)
   */
  public void setPort(int port) throws RemoteException {
  	setParam(PassportDDV.PORT,""+port);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#getCTNumber()
   */
  public int getCTNumber() throws RemoteException {
		try {
			return new Integer(getParam(PassportDDV.CTNUMBER)).intValue();
		}
		catch (NumberFormatException e)
		{
			// Scheinbar noch nicht definiert. Wir liefern einen Default-Wert
			return 0;
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#setCTNumber(int)
   */
  public void setCTNumber(int ctNumber) throws RemoteException {
		setParam(PassportDDV.CTNUMBER,""+ctNumber);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#useBIO()
   */
  public boolean useBIO() throws RemoteException {
		return "1".equals(getParam(PassportDDV.USEBIO));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#setBIO(boolean)
   */
  public void setBIO(boolean bio) throws RemoteException {
		setParam(PassportDDV.USEBIO,bio ? "1" : "0");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#useSoftPin()
   */
  public boolean useSoftPin() throws RemoteException {
		return "1".equals(getParam(PassportDDV.SOFTPIN));
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#setSoftPin(boolean)
   */
  public void setSoftPin(boolean softPin) throws RemoteException {
		setParam(PassportDDV.SOFTPIN,softPin ? "1" : "0");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#getEntryIndex()
   */
  public int getEntryIndex() throws RemoteException {
		try {
			return new Integer(getParam(PassportDDV.ENTRYIDX)).intValue();
		}
		catch (NumberFormatException e)
		{
			// Scheinbar noch nicht definiert. Wir liefern einen Default-Wert
			return 1;
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.PassportDDV#setEntryIndex(int)
   */
  public void setEntryIndex(int index) throws RemoteException {
		setParam(PassportDDV.ENTRYIDX,""+index);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Passport#getHandle()
   */
  public PassportHandle getHandle() throws RemoteException {
    return new PassportHandleDDVImpl(this);
  }

}


/**********************************************************************
 * $Log: PassportDDVImpl.java,v $
 * Revision 1.12  2004/04/19 22:05:51  willuhn
 * @C HBCIJobs refactored
 *
 * Revision 1.11  2004/04/14 23:53:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/04/13 23:14:22  willuhn
 * @N datadir
 *
 * Revision 1.9  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/30 22:07:50  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.6  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.5  2004/02/24 22:47:04  willuhn
 * @N GUI refactoring
 *
 * Revision 1.4  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.3  2004/02/13 00:41:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/12 00:38:41  willuhn
 * *** empty log message ***
 *
 **********************************************************************/