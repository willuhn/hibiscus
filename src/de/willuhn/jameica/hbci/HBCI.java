/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCI.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/02/11 00:11:20 $
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

import org.kapott.hbci.manager.HBCIUtils;
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
  
	private static HBCIPassport passport = null;

	static {
		HBCIUtils.init(null,null,new HBCICallbackSWT());
		HBCIUtils.setParam("log.loglevel.default","5");
		HBCIUtils.setParam("client.passport.default","DDV");
		HBCIUtils.setParam("client.passport.DDV.path","/work/willuhn/eclipse/hbci/passports");
		HBCIUtils.setParam("client.passport.DDV.libname.ddv","/work/willuhn/eclipse/hbci/lib/libhbci4java-card-linux.so");
		HBCIUtils.setParam("client.passport.DDV.libname.ctapi","/work/willuhn/eclipse/hbci/lib/libtowitoko-2.0.7.so");
		HBCIUtils.setParam("client.passport.DDV.port","0");
		HBCIUtils.setParam("client.passport.DDV.ctnumber","0");
		HBCIUtils.setParam("client.passport.DDV.usebio","0");
		HBCIUtils.setParam("client.passport.DDV.softpin","1");
		HBCIUtils.setParam("client.passport.DDV.entryidx","1");

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

//		passport=AbstractHBCIPassport.getInstance();
//		HBCIHandler handler=new HBCIHandler("210",passport);

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
//  	passport.close();
  }

  /**
   * @see de.willuhn.jameica.AbstractPlugin#getPassword()
   */
  protected String getPassword()
  {
    return "Gd._s01)8L+";
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
 * Revision 1.3  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/09 22:09:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/09 13:06:03  willuhn
 * @C misc
 *
 **********************************************************************/