/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/VelocityLogger.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/06/30 23:52:42 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.io;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;

import de.willuhn.logging.Logger;

/**
 * Implementieren wir, um die Log-Ausgaben von Velocity zu uns umzuleiten.
 */
public class VelocityLogger implements LogSystem
{

  /**
   * @see org.apache.velocity.runtime.log.LogSystem#init(org.apache.velocity.runtime.RuntimeServices)
   */
  public void init(RuntimeServices arg0) throws Exception
  {
  }

  /**
   * @see org.apache.velocity.runtime.log.LogSystem#logVelocityMessage(int, java.lang.String)
   */
  public void logVelocityMessage(int arg0, String arg1)
  {
  	switch (arg0)
  	{
	  	case LogSystem.INFO_ID:
	  		Logger.debug(arg1);
	  		break;
	  	case LogSystem.WARN_ID:
	  		Logger.warn(arg1);
				break;
	  	case LogSystem.ERROR_ID:
	  		Logger.error(arg1);
				break;
			case LogSystem.DEBUG_ID:
				Logger.debug(arg1);
				break;
			default:
				Logger.debug(arg1);
  	}
  }

}


/**********************************************************************
 * $Log: VelocityLogger.java,v $
 * Revision 1.1  2005/06/30 23:52:42  web0
 * @N export via velocity
 *
 **********************************************************************/