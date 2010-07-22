/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCICallbackSWT.java,v $
 * $Revision: 1.67 $
 * $Date: 2010/07/22 22:36:24 $
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
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.exceptions.NeedKeyAckException;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIUtilsInternal;
import org.kapott.hbci.passport.AbstractRDHSWFileBasedPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.rmi.Nachricht;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.messaging.QueryMessage;
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
public class HBCICallbackSWT extends AbstractHibiscusHBCICallback
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
    String type = null;
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
        type = "warn";
        if (msg != null && (msg.startsWith("konnte folgenden nutzerdefinierten Wert nicht in Nachricht einsetzen:") ||
                            msg.startsWith("could not insert the following user-defined data"))
           )
        {
          Logger.debug(msg);
          log = false;
          break;
        }
        if (msg != null && msg.matches(".* Algorithmus .* nicht implementiert"))
        {
          Logger.debug(msg);
          log = false;
          break;
        }
        Logger.warn(msg);
				break;

  		case HBCIUtils.LOG_ERR:
        type = "error";
				Logger.error(msg + " " + trace.toString());
				break;

			default:
				Logger.warn(msg);
  	}
    
    if (log && HBCIFactory.getInstance().inProgress())
    {
      ProgressMonitor monitor = HBCIFactory.getInstance().getProgressMonitor();
      if (type != null)
        monitor.log("[" + type + "] " + msg);
      else
        monitor.log(msg);
      monitor.addPercentComplete(1);
    }
  }

  /**
   * @see org.kapott.hbci.callback.HBCICallback#callback(org.kapott.hbci.passport.HBCIPassport, int, java.lang.String, int, java.lang.StringBuffer)
   */
  public void callback(HBCIPassport passport, int reason, String msg, int datatype, StringBuffer retData) {
    
    cacheData(passport);

		try {
      
      if (currentHandle != null && currentHandle.callback(passport,reason,msg,datatype,retData))
      {
        Logger.debug("callback [reason " + reason + "] handled by " + currentHandle.getClass());
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

          boolean isRDH = (passport instanceof AbstractRDHSWFileBasedPassport);
          
          String pw = null;
          
          if (!forceAsk && !isRDH)
          {
            // Wir laden ein ggf. automatisch erzeugtes Passwort
            Wallet w = Settings.getWallet();
            pw = (String) w.get("hbci.passport.password." + passport.getClass().getName());

            if (pw != null && pw.length() > 0)
            {
              Logger.debug("using passport key from wallet, passport: " + passport.getClass().getName());
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
          
        case NEED_INFOPOINT_ACK:
          QueryMessage qm = new QueryMessage(msg,retData);
          Application.getMessagingFactory().getMessagingQueue("hibiscus.infopoint").sendSyncMessage(qm);
          retData.replace(0,retData.length(),qm.getData() == null ? "" : "false");
          break;
          
          
        case HAVE_IBAN_ERROR:
				case HAVE_CRC_ERROR:
				  if (Settings.getKontoCheck())
            Logger.error("IBAN/CRC error: " + msg+ " ["+retData.toString()+"]: ");
          break;
          
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
				throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg("STATUS_INVALID",String.valueOf(statusTag)));
		}
    
  }
}


/**********************************************************************
 * $Log: HBCICallbackSWT.java,v $
 * Revision 1.67  2010/07/22 22:36:24  willuhn
 * @N Code-Cleanup
 *
 * Revision 1.66  2010-07-22 11:35:50  willuhn
 * @C changed log level
 *
 * Revision 1.65  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.64  2010/05/27 09:36:23  willuhn
 * @C CRC-Fehler nur loggen, wenn KTO/BLZ-Pruefung aktiv ist
 *
 * Revision 1.63  2009/04/14 13:38:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.62  2009/03/30 22:54:15  willuhn
 * @C Checksummen-Speicherung geaendert:
 *  1) Es wird SHA1 statt MD5 verwendet
 *  2) Es wird die Checksumme der Checksumme der Checksumme erstellt
 *  3) ein zufaellig erzeugter Salt wird eingefuegt
 *  4) es werden nur noch die ersten 3 Zeichen der Checksumme gespeichert
 *
 * Revision 1.61  2009/03/18 22:08:59  willuhn
 * *** empty log message ***
 *
 * Revision 1.60  2008/11/04 11:55:16  willuhn
 * @N Update auf HBCI4Java 2.5.9
 *
 * Revision 1.59  2008/09/17 23:44:29  willuhn
 * @B SQL-Query fuer MaxUsage-Abfrage korrigiert
 *
 * Revision 1.58  2008/05/30 12:31:41  willuhn
 * @N Erster Code fuer gecachte BPD/UPD
 *
 * Revision 1.57  2008/05/30 12:01:37  willuhn
 * @N Gemeinsame Basisimplementierung des HBCICallback in Hibiscus und Payment-Server
 *
 * Revision 1.56  2008/02/25 22:21:15  willuhn
 * @R undo
 **********************************************************************/