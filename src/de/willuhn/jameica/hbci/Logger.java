/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/Attic/Logger.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/09 13:06:03 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import java.util.Date;

import org.kapott.hbci.callback.HBCICallbackConsole;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.Application;

/**
 * Dieser Logger implementiert den Logger des HBCI-Systems und
 * schreibt die Log-Ausgaben in das Jameica-Log.
 */
public class Logger extends HBCICallbackConsole
{

  /**
   * ct.
   */
  public Logger()
  {
    super();
  }

  /**
   * @see org.kapott.hbci.callback.HBCICallback#log(java.lang.String, int, java.util.Date, java.lang.StackTraceElement)
   */
  public void log(String msg, int level, Date date, StackTraceElement trace)
  {
		de.willuhn.util.Logger l = Application.getLog();
  	switch (level)
  	{
  		case HBCIUtils.LOG_CHIPCARD:
			case HBCIUtils.LOG_DEBUG:
  			l.debug(msg);
  			break;

			case HBCIUtils.LOG_INFO:
				l.info(msg);
				break;

			case HBCIUtils.LOG_WARN:
				l.warn(msg);
				break;

  		case HBCIUtils.LOG_ERR:
  			l.error(msg + " " + trace.toString());
				break;

			default:
				super.log(msg,level,date,trace);
  	}
  }
}


/**********************************************************************
 * $Log: Logger.java,v $
 * Revision 1.1  2004/02/09 13:06:03  willuhn
 * @C misc
 *
 **********************************************************************/