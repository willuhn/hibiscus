/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCICallbackSWT.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/09 22:09:40 $
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

import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.Application;

/**
 * Dieser HBCICallbackSWT implementiert den HBCICallbackSWT des HBCI-Systems und
 * schreibt die Log-Ausgaben in das Jameica-Log.
 */
public class HBCICallbackSWT implements HBCICallback
{

  /**
   * ct.
   */
  public HBCICallbackSWT()
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
				l.warn(msg);
  	}
  }

  /* (non-Javadoc)
   * @see org.kapott.hbci.callback.HBCICallback#callback(org.kapott.hbci.passport.HBCIPassport, int, java.lang.String, int, java.lang.StringBuffer)
   */
  public void callback(HBCIPassport passport, int reason, String msg, int datatype, StringBuffer retData) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.kapott.hbci.callback.HBCICallback#status(org.kapott.hbci.passport.HBCIPassport, int, java.lang.Object[])
   */
  public void status(HBCIPassport passport, int statusTag, Object[] o) {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.kapott.hbci.callback.HBCICallback#status(org.kapott.hbci.passport.HBCIPassport, int, java.lang.Object)
   */
  public void status(HBCIPassport passport, int statusTag, Object o) {
    // TODO Auto-generated method stub
    
  }
}


/**********************************************************************
 * $Log: HBCICallbackSWT.java,v $
 * Revision 1.1  2004/02/09 22:09:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/09 13:06:03  willuhn
 * @C misc
 *
 **********************************************************************/