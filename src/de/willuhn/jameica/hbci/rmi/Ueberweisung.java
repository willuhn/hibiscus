/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Ueberweisung.java,v $
 * $Revision: 1.12 $
 * $Date: 2005/02/04 18:27:54 $
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
 * Bildet eine Ueberweisung ab.
 */
public interface Ueberweisung extends BaseUeberweisung
{
}


/**********************************************************************
 * $Log: Ueberweisung.java,v $
 * Revision 1.12  2005/02/04 18:27:54  willuhn
 * @C Refactoring zwischen Lastschrift und Ueberweisung
 *
 * Revision 1.11  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2005/01/19 00:16:04  willuhn
 * @N Lastschriften
 *
 * Revision 1.9  2004/10/25 22:39:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/07/11 16:14:29  willuhn
 * @N erster Code fuer Dauerauftraege
 *
 * Revision 1.6  2004/04/24 19:04:51  willuhn
 * @N Ueberweisung.execute works!! ;)
 *
 * Revision 1.5  2004/04/05 23:28:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.3  2004/03/05 00:19:23  willuhn
 * @D javadoc fixes
 * @C Converter moved into server package
 *
 * Revision 1.2  2004/02/17 01:01:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 **********************************************************************/