/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCICallbackSWT.java,v $
 * $Revision: 1.50 $
 * $Date: 2007/02/27 10:20:34 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Hashtable;

import org.eclipse.swt.SWTException;
import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.callback.AbstractHBCICallback;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.exceptions.NeedKeyAckException;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIUtilsInternal;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.HBCIPassportRDH;
import org.kapott.hbci.passport.HBCIPassportSIZRDHFile;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.rmi.Nachricht;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.Base64;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Dieser HBCICallbackSWT implementiert den HBCICallbackSWT des HBCI-Systems und
 * schreibt die Log-Ausgaben in das Jameica-Log.
 */
public class HBCICallbackSWT extends AbstractHBCICallback
{

	private I18N i18n;
	private Hashtable accountCache = new Hashtable();
  private PassportHandle currentHandle = null;
  
  /**
   * ct.
   */
  public HBCICallbackSWT()
  {
		super();
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see org.kapott.hbci.callback.HBCICallback#log(java.lang.String, int, java.util.Date, java.lang.StackTraceElement)
   */
  public void log(String msg, int level, Date date, StackTraceElement trace)
  {
    boolean log = true;
  	switch (level)
  	{
  		case HBCIUtils.LOG_DEBUG2:
			case HBCIUtils.LOG_DEBUG:
  			Logger.debug(msg);
        log = false;
  			break;

			case HBCIUtils.LOG_INFO:
				Logger.info(msg);
				break;

			case HBCIUtils.LOG_WARN:
        // Die logge ich mit DEBUG - die nerven sonst
        if (msg != null && msg.startsWith("konnte folgenden nutzerdefinierten Wert nicht in Nachricht einsetzen:"))
        {
          Logger.debug(msg);
          log = false;
        }
        else
          Logger.warn(msg);
				break;

  		case HBCIUtils.LOG_ERR:
				Logger.error(msg + " " + trace.toString());
				break;

			default:
				Logger.warn(msg);
  	}
    
    if (log && HBCIFactory.getInstance().inProgress())
    {
      ProgressMonitor monitor = HBCIFactory.getInstance().getProgressMonitor();
      monitor.log(msg);
      monitor.addPercentComplete(1);
    }
  }

  /**
   * @see org.kapott.hbci.callback.HBCICallback#callback(org.kapott.hbci.passport.HBCIPassport, int, java.lang.String, int, java.lang.StringBuffer)
   */
  public void callback(HBCIPassport passport, int reason, String msg, int datatype, StringBuffer retData) {

		try {
      
      if (currentHandle != null && currentHandle.callback(passport,reason,msg,datatype,retData))
      {
        Logger.info("callback [reason " + reason + "] handled by " + currentHandle.getClass());
        return;
      }

			AccountContainer container = (AccountContainer) accountCache.get(passport);

      String text = null;
      
			switch (reason) {
        
				case NEED_PASSPHRASE_LOAD:
				case NEED_PASSPHRASE_SAVE:
          
          // BUGZILLA 289, 148
          // Neu: Wir behandeln hier nur noch PIN/TAN und Chipkarte
          // und verwalten das Passwort fuer diese selbst. Die Abfragen
          // fuer RDH sind im Passport implementiert.

          HBCI plugin = (HBCI) Application.getPluginLoader().getPlugin(HBCI.class);
          boolean forceAsk = plugin.getResources().getSettings().getBoolean("hbcicallback.askpassphrase.force",false);

          boolean isRDH = (passport instanceof HBCIPassportSIZRDHFile) || (passport instanceof HBCIPassportRDH);
          
          String pw = null;
          
          if (!forceAsk && !isRDH)
          {
            // Wir laden ein ggf. automatisch erzeugtes Passwort
            Wallet w = Settings.getWallet();
            pw = (String) w.get("hbci.passport.password." + passport.getClass().getName());

            if (pw != null && pw.length() > 0)
            {
              Logger.info("using passport key from wallet, passport: " + passport.getClass().getName());
              retData.replace(0,retData.length(),pw);
              break;
            }
            
            if (pw == null)
            {
              // noch kein Passwort definiert. Dann erzeugen wir
              // ein zufaelliges.
              Logger.info("creating new random passport key, passport: " + passport.getClass().getName());
              byte[] pass = new byte[8];
              SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
              random.nextBytes(pass);
              pw = Base64.encode(pass);

              // Und speichern es im Wallet.
              w.set("hbci.passport.password." + passport.getClass().getName(),pw);
              retData.replace(0,retData.length(),pw);
              break;
            }
          }

          if (reason == NEED_PASSPHRASE_LOAD)
            pw = DialogFactory.importPassport(passport);
          else
            pw = DialogFactory.exportPassport(passport);
          retData.replace(0,retData.length(),pw);
					break;


				case NEED_SOFTPIN:
				case NEED_PT_PIN:
          retData.replace(0,retData.length(),DialogFactory.getPIN(passport));
					break;

        // BUGZILLA 62, 200:
        // Die Callbacks NEED_PT_TAN und NEED_PT_SECHMECH werden hier nicht mehr
        // behandelt sondern direkt im PassportHandle des PIN/TAN-Passports.

        // Die folgenden Callbacks werden direkt im Chipcard-PassportHandle
        // behandelt
        // NEED_CHIPCARD, HAVE_CHIPCARD, NEED_HARDPIN, HAVE_HARDPIN, NEED_REMOVE_CHIPCARD:

				case NEED_CONNECTION:
				  DialogFactory.getConnection();
					break;
				case CLOSE_CONNECTION:
					if (!Settings.getOnlineMode())
						DialogFactory.openSimple(i18n.tr("Internet-Verbindung"),i18n.tr("Sie können die Internetverbindung nun wieder trennen."));
					break;

				case NEED_COUNTRY:
					if (container == null) container = DialogFactory.getAccountData(passport);
					accountCache.put(passport,container);
					retData.replace(0,retData.length(),container.country);
					break;

				case NEED_BLZ:
					if (container == null) container = DialogFactory.getAccountData(passport);
					accountCache.put(passport,container);
					retData.replace(0,retData.length(),container.blz);
					break;

				case NEED_HOST:
					if (container == null) container = DialogFactory.getAccountData(passport);
					accountCache.put(passport,container);
					retData.replace(0,retData.length(),container.host);
					break;

				case NEED_PORT:
					if (container == null) container = DialogFactory.getAccountData(passport);
					accountCache.put(passport,container);
					retData.replace(0,retData.length(),container.port+"");
					break;

				case NEED_FILTER:
					if (container == null) container = DialogFactory.getAccountData(passport);
					accountCache.put(passport,container);
					retData.replace(0,retData.length(),container.filter);
					break;

				case NEED_USERID:
					if (container == null) container = DialogFactory.getAccountData(passport);
					accountCache.put(passport,container);
					retData.replace(0,retData.length(),container.userid);
					break;

				case NEED_CUSTOMERID:
					if (container == null) container = DialogFactory.getAccountData(passport);
					accountCache.put(passport,container);
					retData.replace(0,retData.length(),container.customerid);
					break;

				case NEED_NEW_INST_KEYS_ACK:
					retData.replace(0,retData.length(),DialogFactory.getNewInstKeys(passport));
					break;

				case HAVE_NEW_MY_KEYS:
					DialogFactory.newKeys(passport);
					break;

				case HAVE_INST_MSG:
          // BUGZILLA 68 http://www.willuhn.de/bugzilla/show_bug.cgi?id=68
          try
          {
            Nachricht n = (Nachricht) Settings.getDBService().createObject(Nachricht.class,null);
            n.setBLZ(passport.getBLZ());
            n.setNachricht(msg);
            n.setDatum(new Date());
            n.store();
            text = i18n.tr("System-Nachricht empfangen");
            GUI.getStatusBar().setSuccessText(text);
            HBCIFactory.getInstance().getProgressMonitor().setStatusText(text);
          }
          catch (Exception e)
          {
            Logger.error("unable to store system message",e);
            // Im Fehlerfall zeigen wir einfach den Dialog an
            DialogFactory.openSimple(i18n.tr("Instituts-Nachricht"),msg);
          }
					break;

				case HAVE_CRC_ERROR:
				case HAVE_ERROR:
					Logger.error("NOT IMPLEMENTED: " + msg+ " ["+retData.toString()+"]: ");
					throw new HBCI_Exception("reason not implemented");

				default:
				  Logger.error("unknown reason " + reason + ", datatype: " + datatype + ": " + msg);
          throw new HBCI_Exception("unknown reason " + reason + ": " + msg);
	
			}

		}
		catch (NeedKeyAckException e)
		{
			// Die wird bei HAVE_NEW_MY_KEYS geworfen.
			// Wir brechen ohne Anzeigen eines Fehlers ab.
			HBCIFactory.getInstance().markCancelled();
		}
		catch (OperationCanceledException oce)
		{
			// Die wird geworfen, wenn der User selbst abgebrochen hat.
			// Wuerde ich die jetzt weiterwerfen, muesste ich mir bei
			// der anschliessenden Abfrage nach der Fehlerquelle in
			// meinem HBCI-Job durch einen Stapel von ineinander
			// verpackte HBCI_Exceptions wuehlen, um diese hier
			// wiederzufinden. Das ist mir zu aufwaendig. Deswegen
			// teile ich der Factory gleich selbst mit, dass der
			// User die Aktion selbst abgebrochen hat.
			HBCIFactory.getInstance().markCancelled();
			throw oce;
		}
		catch (Throwable t)
		{
			// Siehe oben. Wir wollen sichergehen, dass die OperationCanceledException
			// nicht nochmal verpackt ist.
			Throwable th = t.getCause();
			
			if (t instanceof SWTException)
			{
				th = ((SWTException) t).throwable;
			}
			if (th != null && th instanceof OperationCanceledException)
			{
				HBCIFactory.getInstance().markCancelled();
				throw (OperationCanceledException) th;
			}
			if (t instanceof RuntimeException)
				throw (RuntimeException) t;
			throw new HBCI_Exception(t);
		}
  }

	/**
	 * Protokolliert die Status-Info aus dem HBCI-Kernel mit INFO-Level.
   * @param text zu loggender Text.
   */
  private void status(String text)
	{
    Logger.info(text);
    if (HBCIFactory.getInstance().inProgress())
    {
      ProgressMonitor monitor = HBCIFactory.getInstance().getProgressMonitor();
      monitor.log(text + "\n");
      monitor.addPercentComplete(1);
    }
	}
	
	/**
	 * Protokolliert die Status-Info aus dem HBCI-Kernel mit DEBUG-Level.
   * @param text zu loggender Text.
   */
  private void debug(String text)
	{
		Logger.debug(text);
	}

  /**
   * Speichert das aktuelle Handle.
   * Haesslicher Workaround.
   * @param handle
   */
  public void setCurrentHandle(PassportHandle handle)
  {
    this.currentHandle = handle;
  }
  
  /**
   * @see org.kapott.hbci.callback.HBCICallback#status(org.kapott.hbci.passport.HBCIPassport, int, java.lang.Object[])
   */
  public void status(HBCIPassport passport, int statusTag, Object[] o) {
		switch (statusTag) {

			case STATUS_INST_BPD_INIT:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_INST_DATA"));
				break;

			case STATUS_INST_BPD_INIT_DONE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_INST_DATA_DONE",passport.getBPDVersion()));
				break;

			case STATUS_INST_GET_KEYS:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_INST_KEYS"));
				break;

			case STATUS_INST_GET_KEYS_DONE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_INST_KEYS_DONE"));
				break;

			case STATUS_SEND_KEYS:
				status(HBCIUtilsInternal.getLocMsg("STATUS_SEND_MY_KEYS"));
				break;

			case STATUS_SEND_KEYS_DONE:
				// status(HBCIUtilsInternal.getLocMsg("STATUS_SEND_MY_KEYS_DONE") + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				status(HBCIUtilsInternal.getLocMsg("STATUS_SEND_MY_KEYS_DONE"));
				break;

			case STATUS_INIT_SYSID:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_SYSID"));
				break;

			case STATUS_INIT_SYSID_DONE:
				// status(HBCIUtilsInternal.getLocMsg("STATUS_REC_SYSID_DONE",o[1].toString()) + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_SYSID_DONE",o[1].toString()));
				break;

			case STATUS_INIT_SIGID:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_SIGID"));
				break;

			case STATUS_INIT_SIGID_DONE:
				// status(HBCIUtilsInternal.getLocMsg("STATUS_REC_SIGID_DONE",o[1].toString()) + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_SIGID_DONE",o[1].toString()));
				break;

			case STATUS_INIT_UPD:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_USER_DATA"));
				break;

			case STATUS_INIT_UPD_DONE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_USER_DATA_DONE",passport.getUPDVersion()));
				break;

			case STATUS_LOCK_KEYS:
				status(HBCIUtilsInternal.getLocMsg("STATUS_USR_LOCK"));
				break;

			case STATUS_LOCK_KEYS_DONE:
				// status(HBCIUtilsInternal.getLocMsg("STATUS_USR_LOCK_DONE") + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				status(HBCIUtilsInternal.getLocMsg("STATUS_USR_LOCK_DONE"));
				break;

			case STATUS_DIALOG_INIT:
				status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_INIT"));
				break;

			case STATUS_DIALOG_INIT_DONE:
				// status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_INIT_DONE",o[1]) + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_INIT_DONE",o[1]));
				break;

			case STATUS_SEND_TASK:
				status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_NEW_JOB",((HBCIJob)o[0]).getName()));
				break;

			case STATUS_SEND_TASK_DONE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_JOB_DONE",((HBCIJob)o[0]).getName()));
				break;

			case STATUS_DIALOG_END:
				status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_END"));
				break;

			case STATUS_DIALOG_END_DONE:
				// status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_END_DONE") + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_END_DONE"));
				break;

			case STATUS_MSG_CREATE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_CREATE",o[0].toString()));
				break;

			case STATUS_MSG_SIGN:
				status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_SIGN"));
				break;

			case STATUS_MSG_CRYPT:
				status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_CRYPT"));
				break;

			case STATUS_MSG_SEND:
				status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_SEND"));
				break;

			case STATUS_MSG_RECV:
				status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_RECV"));
				break;

			case STATUS_MSG_PARSE:
				debug(HBCIUtilsInternal.getLocMsg("STATUS_MSG_PARSE",o[0].toString()+")"));
				break;

			case STATUS_MSG_DECRYPT:
				status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_DECRYPT"));
				break;

			case STATUS_MSG_VERIFY:
				status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_VERIFY"));
				break;

			default:
				throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg("STATUS_INVALID",Integer.toString(statusTag)));
		}
    
  }
}


/**********************************************************************
 * $Log: HBCICallbackSWT.java,v $
 * Revision 1.50  2007/02/27 10:20:34  willuhn
 * @C "konnte folgenden nutzerdef..." nicht in Progressmonitor anzeigen
 *
 * Revision 1.49  2007/01/03 11:14:20  willuhn
 * @Nachrichten "konnte folgenden nutzerdefinierten Wert nicht in Nachricht einsetzen:.*" werden nur noch mit DEBUG geloggt
 *
 * Revision 1.48  2006/10/23 15:16:12  willuhn
 * @B Passwort-Handling ueberarbeitet
 *
 * Revision 1.47  2006/10/23 11:38:57  willuhn
 * @B auto password handling
 *
 * Revision 1.46  2006/10/23 10:48:04  willuhn
 * @B check for pw=null before generating a new password
 *
 * Revision 1.45  2006/10/18 16:17:08  willuhn
 * @N additional log infos
 *
 * Revision 1.44  2006/10/12 13:07:56  willuhn
 * @B bug 289 + Callback NEED_SIZENTRY_SELECT
 *
 * Revision 1.43  2006/10/06 13:18:06  willuhn
 * @B Bug 185, 211 (zusaetzlicher Suffix mit BLZ)
 *
 * Revision 1.42  2006/10/06 13:08:01  willuhn
 * @B Bug 185, 211
 *
 * Revision 1.41  2006/08/21 12:29:54  willuhn
 * @N HBCICallbackSWT.setCurrentHandle
 *
 * Revision 1.40  2006/08/06 13:26:48  willuhn
 * @B bug 257
 *
 * Revision 1.39  2006/08/03 15:32:35  willuhn
 * @N Bug 62
 *
 * Revision 1.38  2006/07/13 22:10:24  willuhn
 * @B bug 249
 *
 * Revision 1.37  2006/04/03 12:30:18  willuhn
 * @N new InternetConnectionDialog
 *
 * Revision 1.36  2006/03/28 17:52:23  willuhn
 * @B bug 218
 *
 * Revision 1.35  2006/02/23 22:14:58  willuhn
 * @B bug 200 (Speichern der Auswahl)
 *
 * Revision 1.34  2006/02/21 22:51:36  willuhn
 * @B bug 200
 *
 * Revision 1.33  2006/02/20 11:43:56  willuhn
 * @B bug 200
 *
 * Revision 1.32  2006/02/06 15:40:44  willuhn
 * @B bug 150
 *
 * Revision 1.31  2005/11/14 11:37:00  willuhn
 * @B bug 148
 *
 * Revision 1.30  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.29  2005/07/26 23:57:18  web0
 * @N Restliche HBCI-Jobs umgestellt
 *
 * Revision 1.28  2005/07/26 23:00:03  web0
 * @N Multithreading-Support fuer HBCI-Jobs
 *
 * Revision 1.27  2005/06/21 20:11:10  web0
 * @C cvs merge
 *
 * Revision 1.26  2005/05/09 17:26:56  web0
 * @N Bugzilla 68
 *
 * Revision 1.25  2005/05/06 14:05:04  web0
 * *** empty log message ***
 *
 * Revision 1.24  2005/02/28 15:30:47  web0
 * @B Bugzilla #15
 *
 * Revision 1.23  2005/02/03 23:57:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.22  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 * Revision 1.21  2005/02/01 17:15:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.20  2005/01/15 16:48:17  willuhn
 * *** empty log message ***
 *
 * Revision 1.19  2005/01/09 23:21:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.18  2004/11/12 18:25:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2004/10/24 17:19:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2004/10/14 23:14:20  willuhn
 * @N new hbci4java (2.5pre)
 * @B fixed locales
 *
 * Revision 1.14  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.13  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/05/05 21:27:13  willuhn
 * @N added TAN-Dialog
 *
 * Revision 1.10  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.8  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.7  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 * Revision 1.6  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.5  2004/02/13 00:41:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/12 00:47:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/09 22:09:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/09 13:06:03  willuhn
 * @C misc
 *
 **********************************************************************/