/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCI.java,v $
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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.datasource.db.EmbeddedDatabase;
import de.willuhn.jameica.AbstractPlugin;
import de.willuhn.jameica.Application;

/**
 * 
 */
public class HBCI extends AbstractPlugin
{

	public static DateFormat DATEFORMAT       = new SimpleDateFormat("dd.MM.yyyy");
	public static DateFormat FASTDATEFORMAT   = new SimpleDateFormat("ddMMyyyy");
	public static DecimalFormat DECIMALFORMAT = (DecimalFormat) NumberFormat.getNumberInstance(Application.getConfig().getLocale());
  
	static {
		HBCIUtils.init(null,null,new Logger());
		HBCIUtils.setParam("log.loglevel.default","4");
		HBCIUtils.setParam("client.passport.default","DDV");
//		HBCIUtils.setParam("client.passport.DDV.path",Settings.get("ddv_path",".")+File.separator+
//											 Settings.get("ddv_prefix",""));
//		HBCIUtils.setParam("client.passport.DDV.libname.ddv",Settings.get("ddv_ddvlib",""));
//		HBCIUtils.setParam("client.passport.DDV.libname.ctapi",Settings.get("ddv_ctapilib",""));
//		HBCIUtils.setParam("client.passport.DDV.port",Settings.get("ddv_portnum",""));
//		HBCIUtils.setParam("client.passport.DDV.ctnumber",Settings.get("ddv_ctnum",""));
//		HBCIUtils.setParam("client.passport.DDV.usebio",Settings.get("ddv_usebio",""));
//		HBCIUtils.setParam("client.passport.DDV.softpin",Settings.get("ddv_softpin",""));
//		HBCIUtils.setParam("client.passport.DDV.entryidx",Settings.get("ddv_entryidx",""));
		HBCIPassport passport=AbstractHBCIPassport.getInstance();
		HBCIHandler handler=new HBCIHandler("210",passport);

		DECIMALFORMAT.applyPattern("#0.00");
	}

	private boolean freshInstall = false;

  /**
   * @param file
   */
  public HBCI(File file)
  {
    super(file);
  }

  /**
   * @see de.willuhn.jameica.Plugin#init()
   */
  public boolean init()
  {
		try {
			Settings.setDatabase(getDatabase().getDBService());
		}
		catch (RemoteException e)
		{
			Application.getLog().error("unable to open database",e);
			return false;
		}
		return true;
  }

  /**
   * @see de.willuhn.jameica.Plugin#install()
   */
  public boolean install()
  {
		EmbeddedDatabase db = getDatabase();
		if (!db.exists())
		{
			try {
				db.create();
			}
			catch (IOException e)
			{
				Application.getLog().error("unable to create database",e);
				return false;
			}
			try
			{
				db.executeSQLScript(new File(getPath() + "/sql/create.sql"));
			}
			catch (Exception e)
			{
				Application.getLog().error("unable to create sql tables",e);
				return false;
			}
			try
			{
				db.executeSQLScript(new File(getPath() + "/sql/init.sql"));
			}
			catch (Exception e)
			{
				Application.getLog().error("unable to insert init data",e);
				return false;
			}
      
		}
		freshInstall = true;
		return true;
  }

  /**
   * @see de.willuhn.jameica.Plugin#update(double)
   */
  public boolean update(double oldVersion)
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.Plugin#getWelcomeText()
   */
  public String getWelcomeText()
  {
    return "HBCI - Onlinebanking für Jameica " + getVersion();
  }

  /**
   * @see de.willuhn.jameica.Plugin#shutDown()
   */
  public void shutDown()
  {
  }

  /**
   * @see de.willuhn.jameica.AbstractPlugin#getPassword()
   */
  protected String getPassword()
  {
    return "&%Gds01)8L+";
  }

  /**
   * @see de.willuhn.jameica.AbstractPlugin#getUsername()
   */
  protected String getUsername()
  {
    return "hbcijameica";
  }

}


/**********************************************************************
 * $Log: HBCI.java,v $
 * Revision 1.1  2004/02/09 13:06:03  willuhn
 * @C misc
 *
 **********************************************************************/