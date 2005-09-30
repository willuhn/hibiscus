/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/SammelLastBuchung.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/09/30 00:08:50 $
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
 * Interface fuer eine einzelne Buchung einer Sammellastschrift.
 */
public interface SammelLastBuchung extends SammelTransferBuchung, Duplicatable
{
}


/**********************************************************************
 * $Log: SammelLastBuchung.java,v $
 * Revision 1.4  2005/09/30 00:08:50  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.3  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.2  2005/02/28 16:28:24  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.1  2005/02/27 17:11:49  web0
 * @N first code for "Sammellastschrift"
 * @C "Empfaenger" renamed into "Adresse"
 *
 **********************************************************************/