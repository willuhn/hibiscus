/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.swt.SWTException;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.exceptions.NeedKeyAckException;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.hbci.gui.dialogs.NewInstKeysDialog;
import de.willuhn.jameica.hbci.gui.dialogs.NewKeysDialog;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Nachricht;
import de.willuhn.jameica.hbci.synchronize.SynchronizeSession;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.Base64;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Dieser HBCICallbackSWT implementiert den HBCICallbackSWT des HBCI-Systems und
 * schreibt die Log-Ausgaben in das Jameica-Log.
 */
@Lifecycle(Type.CONTEXT)
public class HBCICallbackSWT extends AbstractHibiscusHBCICallback
{
	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
	
	private Map<HBCIPassport,AccountContainer> accountCache = new HashMap<HBCIPassport,AccountContainer>();
  private PassportHandle currentHandle = null;
  
  @Resource private HBCISynchronizeBackend backend = null;
  
  /**
   * @see org.kapott.hbci.callback.HBCICallback#log(java.lang.String, int, java.util.Date, java.lang.StackTraceElement)
   */
  public void log(String msg, int level, Date date, StackTraceElement trace)
  {
    SynchronizeSession session = this.backend.getCurrentSession();

    boolean mon = true;
    String type = null;
    
  	switch (level)
  	{
  	  case HBCIUtils.LOG_INTERN:
  		case HBCIUtils.LOG_DEBUG2:
        Logger.trace(msg);
        mon = false;
        break;
  		  
			case HBCIUtils.LOG_DEBUG:
  			Logger.debug(msg);
        mon = false;
  			break;

			case HBCIUtils.LOG_INFO:
	      this.updateProgress();
				Logger.info(msg);
				break;

			case HBCIUtils.LOG_WARN:
	      this.updateProgress();
        // Die logge ich mit DEBUG - die nerven sonst
        type = "Hinweis";
        if (msg != null && (msg.startsWith("konnte folgenden nutzerdefinierten Wert nicht in Nachricht einsetzen:") || msg.startsWith("could not insert the following user-defined data")))
        {
          Logger.debug(msg);
          mon = false;
          break;
        }
        if (msg != null && msg.matches(".* Algorithmus .* nicht implementiert"))
        {
          Logger.debug(msg);
          mon = false;
          break;
        }
        Logger.warn(msg);
				break;

  		case HBCIUtils.LOG_ERR:
        this.updateProgress();
  		  if (session != null && session.getStatus() == ProgressMonitor.STATUS_CANCEL)
  		  {
  		    mon = false;
  		    break;
  		  }
  		  else
  		  {
  		    if (session != null && msg != null)
  		      session.getErrors().add(msg.replace("HBCI error code: ",""));
  		    
          type = "Fehler";
          Logger.error(msg + " " + trace.toString());
          break;
  		  }

			default:
				Logger.warn(msg);
  	}
    
    if (mon && session != null)
    {
      ProgressMonitor monitor = session.getProgressMonitor();
      if (type != null)
        monitor.log("    [" + type + "] " + msg);
      else
        monitor.log("    " + msg);
    }
  }

  /**
   * @see org.kapott.hbci.callback.HBCICallback#callback(org.kapott.hbci.passport.HBCIPassport, int, java.lang.String, int, java.lang.StringBuffer)
   */
  public void callback(HBCIPassport passport, int reason, String msg, int datatype, StringBuffer retData)
  {
    this.updateProgress();
    SynchronizeSession session = this.backend.getCurrentSession();

    try
    {
      
      if (currentHandle != null && currentHandle.callback(passport,reason,msg,datatype,retData))
      {
        Logger.debug("callback [reason " + reason + "] handled by " + currentHandle.getClass());
        return;
      }

			AccountContainer container = accountCache.get(passport);
			
			switch (reason)
			{
        
			  // Hier kommen nur noch die PIN/TAN und DDV-Passports an. Die von RDH werden
			  // im PassportHandle verarbeitet
				case NEED_PASSPHRASE_LOAD:
				case NEED_PASSPHRASE_SAVE:
          
          // Passwort aus dem Wallet laden
          Wallet w = Settings.getWallet();
          String pw = (String) w.get("hbci.passport.password." + passport.getClass().getName());
          if (pw != null && pw.length() > 0)
          {
            Logger.debug("using passport key from wallet, passport: " + passport.getClass().getName());
            retData.replace(0,retData.length(),pw);
            break;
          }
            
          // noch kein Passwort definiert. Dann erzeugen wir ein zufaelliges.
          Logger.debug("creating new random passport key, passport: " + passport.getClass().getName());
          byte[] pass = new byte[8];
          SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
          random.nextBytes(pass);
          pw = Base64.encode(pass);

          // Und speichern es im Wallet.
          w.set("hbci.passport.password." + passport.getClass().getName(),pw);
          retData.replace(0,retData.length(),pw);
          break;

				case NEED_CONNECTION:
          if (!Settings.getOnlineMode())
            Application.getCallback().notifyUser(i18n.tr("Bitte stellen Sie eine Internetverbindung her und klicken Sie anschließend auf OK."));
					break;
				case CLOSE_CONNECTION:
					if (!Settings.getOnlineMode())
					  Application.getCallback().notifyUser(i18n.tr("Sie können die Internetverbindung nun wieder trennen."));
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
			    NewInstKeysDialog nikd = new NewInstKeysDialog(passport);
			    Boolean b = (Boolean) nikd.open();
			    retData.replace(0,retData.length(),b.booleanValue() ? "" : "ERROR");
					break;

				case HAVE_NEW_MY_KEYS:
	        NewKeysDialog nkd = new NewKeysDialog(passport);
	        try
	        {
	          nkd.open();
	        }
	        catch (OperationCanceledException e)
	        {
	          // Den INI-Brief kann der User auch noch spaeter ausdrucken
	          Logger.warn(e.getMessage());
	        }
					break;

				case HAVE_INST_MSG:
          try
          {
            Konto k = session.getKonto();
            boolean sm = true;
            if (k != null)
            {
              SynchronizeOptions o = new SynchronizeOptions(k);
              sm = o.getSyncMessages();
            }
            
            if (sm)
            {
              Nachricht n = (Nachricht) Settings.getDBService().createObject(Nachricht.class,null);
              n.setBLZ(passport.getBLZ());
              n.setNachricht(msg);
              n.setDatum(new Date());
              n.store();
              String text = i18n.tr("Neue Institutsnachricht empfangen");
              Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,StatusBarMessage.TYPE_INFO));
              Application.getMessagingFactory().sendMessage(new ImportMessage(n));
              session.getProgressMonitor().setStatusText(text);
            }
          }
          catch (Exception e)
          {
            Logger.error("unable to store system message",e);
            // Im Fehlerfall zeigen wir einfach den Dialog an
            Application.getCallback().notifyUser(msg);
          }
					break;
          
        case HAVE_IBAN_ERROR:
				case HAVE_CRC_ERROR:
				  if (Settings.getKontoCheck())
            Logger.error("IBAN/CRC error: " + msg+ " ["+retData.toString()+"]: ");
          break;
          
        case WRONG_PIN:
          Logger.error("detected wrong PIN: " + msg+ " ["+retData.toString()+"]: ");
          break;
          
        case USERID_CHANGED:
          Logger.info("got changed user/account data (code 3072) - saving in persistent data for later handling");
          ((AbstractHBCIPassport)passport).setPersistentData(PassportHandle.CONTEXT_USERID_CHANGED,retData.toString());
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
		  session.cancel();
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
		  if (session != null) // kann er u.U. abbrechen, bevor die Session existierte
		    session.cancel();
			throw oce;
		}
		catch (Throwable t)
		{
      if (t instanceof SWTException) // von SWT verpackt
        t = ((SWTException) t).throwable;

      // Siehe oben. Wir wollen sichergehen, dass die OperationCanceledException
      // nicht nochmal verpackt ist.
		  Throwable th = HBCIProperties.getCause(t,OperationCanceledException.class);
			if (th != null)
			{
	      session.cancel();
				throw (OperationCanceledException) th;
			}
			
			// Ansonsten durchwerfen
			if (t instanceof RuntimeException)
				throw (RuntimeException) t;
			throw new HBCI_Exception(t);
		}
  }

  /**
   * @see de.willuhn.jameica.hbci.AbstractHibiscusHBCICallback#status(java.lang.String)
   */
  protected void status(String text)
	{
    Logger.info(text);

    // Im Progress-Monitor brauchen wir die ganzen Daten aber nur bei DEBUG
    // oder hoeher
    if (!Logger.isLogging(Level.DEBUG))
      return;

    SynchronizeSession session = this.backend.getCurrentSession();
    this.updateProgress();

    if (session != null)
    {
      ProgressMonitor monitor = session.getProgressMonitor();
      monitor.log(text + "\n");
    }
	}
  
  /**
   * Schreibt den Fortschrittsbalken etwas weiter.
   */
  private void updateProgress()
  {
    final SynchronizeSession session = this.backend.getCurrentSession();
    if (session == null)
      return;
    
    ProgressMonitor m = session.getProgressMonitor();
    if (m == null)
      return;
    
    // Das ist die Gesamt-Zahl der Prozentpunkte, die wir zur Verfuegung haben
    double window = session.getProgressWindow();
    if (window <= 1d)
      return;

    // Wir wissen nicht wirklich, wie oft wir aufgerufen werden.
    // Daher machen wir das hier nur geschaetzt
    m.addPercentComplete(1);
    session.setProgressWindow(window - 0.2d);
    
    
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
}
