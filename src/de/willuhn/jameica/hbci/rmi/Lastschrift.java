/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Lastschrift.java,v $
 * $Revision: 1.6 $
 * $Date: 2008/08/01 11:05:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.rmi;


/**
 * Ne Lastschrift ist ja auch nur ne Ueberweisung. Nur andersrum ;).
 */
public interface Lastschrift extends BaseUeberweisung, Duplicatable
{
}


/**********************************************************************
 * $Log: Lastschrift.java,v $
 * Revision 1.6  2008/08/01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.5  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.4  2005/03/02 17:59:30  web0
 * @N some refactoring
 *
 * Revision 1.3  2005/02/19 16:49:32  willuhn
 * @B bugs 3,8,10
 *
 * Revision 1.2  2005/02/04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 * Revision 1.1  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 **********************************************************************/