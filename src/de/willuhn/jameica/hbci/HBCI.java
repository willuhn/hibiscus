/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCI.java,v $
 * $Revision: 1.6 $
 * $Date: 2004/03/03 22:26:40 $
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

import de.willuhn.datasource.db.EmbeddedDatabase;
import de.willuhn.jameica.AbstractPlugin;
import de.willuhn.jameica.Application;
import de.willuhn.util.Logger;

/**
 * 
 */
public class HBCI extends AbstractPlugin
{

	public static DateFormat LONGDATEFORMAT   = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	public static DateFormat DATEFORMAT       = new SimpleDateFormat("dd.MM.yyyy");
	public static DateFormat FASTDATEFORMAT   = new SimpleDateFormat("ddMMyyyy");
	public static DecimalFormat DECIMALFORMAT = (DecimalFormat) NumberFormat.getNumberInstance(Application.getConfig().getLocale());
  
	// Mapper von HBCI4Java nach jameica Loglevels
	private static int[][] logMapping = new int[][]
	{
		{Logger.LEVEL_DEBUG, 5},
		{Logger.LEVEL_ERROR, 1},
		{Logger.LEVEL_WARN,  2},
		{Logger.LEVEL_INFO,  3}
	};

	static {
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
		HBCIUtils.init(null,null,new HBCICallbackSWT());
		int logLevel = logMapping[Application.getLog().getLevelByName(Application.getConfig().getLogLevel())][1];
		HBCIUtils.setParam("log.loglevel.default",""+logLevel);

		try {
			Settings.setDatabase(getResources().getDatabase().getDBService());
			Settings.setPath(getResources().getPath());
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
		EmbeddedDatabase db = getResources().getDatabase();
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
				db.executeSQLScript(new File(getResources().getPath() + "/sql/create.sql"));
			}
			catch (Exception e)
			{
				Application.getLog().error("unable to create sql tables",e);
				return false;
			}
			try
			{
				db.executeSQLScript(new File(getResources().getPath() + "/sql/init.sql"));
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
 * Revision 1.6  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.5  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.4  2004/02/12 00:38:40  willuhn
 * *** empty log message ***
 *
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