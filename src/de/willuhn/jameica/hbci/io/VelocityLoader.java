/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/Attic/VelocityLoader.java,v $
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

/**
 * Wir implementieren einen eigenen ResourceLoader damit Velocity uns im Plugin findet ;).
 */
public class VelocityLoader
  extends org.apache.velocity.runtime.resource.loader.ResourceLoader
{

	private String velocityPath = ".";

  /**
   * ct.
   */
  public VelocityLoader()
  {
    super();
    try
    {
    	AbstractPlugin p = Application.getPluginLoader().getPlugin(HBCI.class);
			this.velocityPath = p.getResources().getPath() + File.separator + "lib" + File.separator + "velocity";
			Application.getClassLoader().add(new File(velocityPath));
    }
    catch (Throwable t)
    {
    	Logger.error("resourceloader init failed",t);
    }
  }

  /**
   * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#init(org.apache.commons.collections.ExtendedProperties)
   */
  public void init(ExtendedProperties configuration)
  {
  }

  /**
   * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getResourceStream(java.lang.String)
   */
  public InputStream getResourceStream(String source)
    throws ResourceNotFoundException
  {
  	try
  	{
			InputStream is = new FileInputStream(velocityPath + File.separator + source);
			return is;
  	}
  	catch (FileNotFoundException e)
  	{
  		Logger.error(e.getMessage(),e);
  		throw new ResourceNotFoundException(e.getMessage());
  	}
  }

  /**
   * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#isSourceModified(org.apache.velocity.runtime.resource.Resource)
   */
  public boolean isSourceModified(Resource resource)
  {
		return getLastModified(resource) > resource.getLastModified();
  }

  /**
   * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getLastModified(org.apache.velocity.runtime.resource.Resource)
   */
  public long getLastModified(Resource resource)
  {
		try
		{
			File f = new File(velocityPath + resource.getName());
			return f.lastModified();
		}
		catch (Exception e)
		{
			Logger.error("error while checking for last resource modification",e);
		}
		return 0;
  }

}


/**********************************************************************
 * $Log: VelocityLoader.java,v $
 * Revision 1.1  2005/06/30 23:52:42  web0
 * @N export via velocity
 *
 **********************************************************************/