/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/HBCICallbackSWT.java,v $
 * $Revision: 1.9 $
 * $Date: 2004/03/06 18:25:10 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.StringTokenizer;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.callback.AbstractHBCICallback;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.INILetter;
import org.kapott.hbci.status.HBCIMsgStatus;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.util.I18N;

/**
 * Dieser HBCICallbackSWT implementiert den HBCICallbackSWT des HBCI-Systems und
 * schreibt die Log-Ausgaben in das Jameica-Log.
 */
public class HBCICallbackSWT extends AbstractHBCICallback
{

	private I18N i18n;

  /**
   * ct.
   */
  public HBCICallbackSWT()
  {
    super();
    i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
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

  /**
   * @see org.kapott.hbci.callback.HBCICallback#callback(org.kapott.hbci.passport.HBCIPassport, int, java.lang.String, int, java.lang.StringBuffer)
   */
  public void callback(HBCIPassport passport, int reason, String msg, int datatype, StringBuffer retData) {

		try {
				INILetter iniletter;
				Date      date;
            
			switch (reason) {
				case NEED_PASSPHRASE_LOAD:
				case NEED_PASSPHRASE_SAVE:
					retData.replace(0,retData.length(),Settings.getPassphrase());
					break;
	
				case NEED_CHIPCARD:
					GUI.setActionText(i18n.tr("Bitte legen Sie Ihre HBCI-Chipkarte in das Lesegerät."));
					break;

				case HAVE_CHIPCARD:
					GUI.setActionText(i18n.tr("HBCI-Chipkarte wird ausgelesen."));
					break;
	
				case NEED_HARDPIN:
					Application.getLog().info("not implemented");
					break;

				case NEED_SOFTPIN:
					String p = DialogFactory.getPIN();
					retData.replace(0,retData.length(),p);
					break;
				case NEED_PT_PIN:
				case NEED_PT_TAN:
						System.out.print(msg+": ");
						break;

				case HAVE_HARDPIN:


				case NEED_COUNTRY:
				case NEED_BLZ:
				case NEED_HOST:
				case NEED_PORT:
				case NEED_FILTER:
				case NEED_USERID:
				case NEED_CUSTOMERID:
						System.out.print(msg+" ["+retData.toString()+"]: ");
						break;

				case NEED_NEW_INST_KEYS_ACK:
						System.out.println(msg);
						iniletter=new INILetter(passport,INILetter.TYPE_INST);
						System.out.println(HBCIUtils.getLocMsg("EXPONENT")+": "+HBCIUtils.data2hex(iniletter.getKeyExponent()));
						System.out.println(HBCIUtils.getLocMsg("MODULUS")+": "+HBCIUtils.data2hex(iniletter.getKeyModulus()));
						System.out.println(HBCIUtils.getLocMsg("HASH")+": "+HBCIUtils.data2hex(iniletter.getKeyHash()));
						System.out.print("<ENTER>=OK, \"ERR\"=ERROR: ");
						System.out.flush();
						retData.replace(0,retData.length(),
														(new BufferedReader(new InputStreamReader(System.in))).readLine());
						break;

				case HAVE_NEW_MY_KEYS:
						iniletter=new INILetter(passport,INILetter.TYPE_USER);
						date=new Date();
						System.out.println(HBCIUtils.getLocMsg("DATE")+": "+HBCIUtils.date2String(date));
						System.out.println(HBCIUtils.getLocMsg("TIME")+": "+HBCIUtils.time2String(date));
						System.out.println(HBCIUtils.getLocMsg("BLZ")+": "+passport.getBLZ());
						System.out.println(HBCIUtils.getLocMsg("USERID")+": "+passport.getUserId());
						System.out.println(HBCIUtils.getLocMsg("KEYNUM")+": "+passport.getMyPublicSigKey().num);
						System.out.println(HBCIUtils.getLocMsg("KEYVERSION")+": "+passport.getMyPublicSigKey().version);
						System.out.println(HBCIUtils.getLocMsg("EXPONENT")+": "+HBCIUtils.data2hex(iniletter.getKeyExponent()));
						System.out.println(HBCIUtils.getLocMsg("MODULUS")+": "+HBCIUtils.data2hex(iniletter.getKeyModulus()));
						System.out.println(HBCIUtils.getLocMsg("HASH")+": "+HBCIUtils.data2hex(iniletter.getKeyHash()));
						System.out.println(msg);
						break;

				case HAVE_INST_MSG:
						System.out.println(msg);
						System.out.println(HBCIUtils.getLocMsg("CONTINUE"));
						new BufferedReader(new InputStreamReader(System.in)).readLine();
						break;

				case NEED_REMOVE_CHIPCARD:
						System.out.println(msg);
						break;

				case HAVE_CRC_ERROR:
						System.out.println(msg);

						int idx=retData.indexOf("|");
						String blz=retData.substring(0,idx);
						String number=retData.substring(idx+1);

						System.out.print(HBCIUtils.getLocMsg("BLZ")+" ["+blz+"]: ");
						System.out.flush();
						String s=(new BufferedReader(new InputStreamReader(System.in))).readLine();
						if (s.length()==0)
								s=blz;
						blz=s;

						System.out.print(HBCIUtils.getLocMsg("ACCNUMBER")+" ["+number+"]: ");
						System.out.flush();
						s=(new BufferedReader(new InputStreamReader(System.in))).readLine();
						if (s.length()==0)
								s=number;
						number=s;

						retData.replace(0,retData.length(),blz+"|"+number);
						break;

				case HAVE_ERROR:
						System.out.println(msg);
						System.out.print("<ENTER>=OK, \"ERR\"=ERROR: ");
						System.out.flush();
						retData.replace(0,retData.length(),
														(new BufferedReader(new InputStreamReader(System.in))).readLine());
						break;
                    
				case NEED_SIZENTRY_SELECT:
						StringTokenizer tok=new StringTokenizer(retData.toString(),"|");
						while (tok.hasMoreTokens()) {
								String entry=tok.nextToken();
								StringTokenizer tok2=new StringTokenizer(entry,";");
                        
								String tempblz;
								System.out.println(tok2.nextToken()+": "+
																	 HBCIUtils.getLocMsg("BLZ")+"="+(tempblz=tok2.nextToken())+
																	 " ("+HBCIUtils.getNameForBLZ(tempblz)+") "+
																	 HBCIUtils.getLocMsg("USERID")+"="+tok2.nextToken());
						}
						System.out.print(HBCIUtils.getLocMsg("CALLB_SELECT_ENTRY")+": ");
						System.out.flush();
						retData.replace(0,retData.length(),
														(new BufferedReader(new InputStreamReader(System.in))).readLine());
						break;

				case NEED_CONNECTION:
					if (!Settings.getOnlineMode())
						DialogFactory.openSimple("Internet-Verbindung","Bitte stellen Sie sicher, dass eine Internetverbindung verfügbar ist.");
					break;
				case CLOSE_CONNECTION:
					if (!Settings.getOnlineMode())
						DialogFactory.openSimple("Internet-Verbindung","Sie können die Internetverbindung nun wieder trennen.");
					break;

				default:
						throw new HBCI_Exception(HBCIUtils.getLocMsg("EXCMSG_CALLB_UNKNOWN",Integer.toString(reason)));
	
			}

		}
		catch (Exception e)
		{
			throw new HBCI_Exception(HBCIUtils.getLocMsg("EXCMSG_CALLB_ERR"),e);
		}
  }

	private void status(String text)
	{
		Application.getLog().debug(text);
	}
	
  /**
   * @see org.kapott.hbci.callback.HBCICallback#status(org.kapott.hbci.passport.HBCIPassport, int, java.lang.Object[])
   */
  public void status(HBCIPassport passport, int statusTag, Object[] o) {
		switch (statusTag) {

			case STATUS_INST_BPD_INIT: 
				status(HBCIUtils.getLocMsg("STATUS_REC_INST_DATA"));
				break;

			case STATUS_INST_BPD_INIT_DONE:
				status(HBCIUtils.getLocMsg("STATUS_REC_INST_DATA_DONE",passport.getBPDVersion()));
				break;

			case STATUS_INST_GET_KEYS:
				status(HBCIUtils.getLocMsg("STATUS_REC_INST_KEYS"));
				break;

			case STATUS_INST_GET_KEYS_DONE:
				status(HBCIUtils.getLocMsg("STATUS_REC_INST_KEYS_DONE"));
				break;

			case STATUS_SEND_KEYS:
				status(HBCIUtils.getLocMsg("STATUS_SEND_MY_KEYS"));
				break;

			case STATUS_SEND_KEYS_DONE:
				status(HBCIUtils.getLocMsg("STATUS_SEND_MY_KEYS_DONE") + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				break;

			case STATUS_INIT_SYSID:
				status(HBCIUtils.getLocMsg("STATUS_REC_SYSID"));
				break;

			case STATUS_INIT_SYSID_DONE:
				status(HBCIUtils.getLocMsg("STATUS_REC_SYSID_DONE",o[1].toString()) + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				break;

			case STATUS_INIT_SIGID:
				status(HBCIUtils.getLocMsg("STATUS_REC_SIGID"));
				break;

			case STATUS_INIT_SIGID_DONE:
				status(HBCIUtils.getLocMsg("STATUS_REC_SIGID_DONE",o[1].toString()) + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				break;

			case STATUS_INIT_UPD:
				status(HBCIUtils.getLocMsg("STATUS_REC_USER_DATA"));
				break;

			case STATUS_INIT_UPD_DONE:
				status(HBCIUtils.getLocMsg("STATUS_REC_USER_DATA_DONE",passport.getUPDVersion()));
				break;

			case STATUS_LOCK_KEYS:
				status(HBCIUtils.getLocMsg("STATUS_USR_LOCK"));
				break;

			case STATUS_LOCK_KEYS_DONE:
				status(HBCIUtils.getLocMsg("STATUS_USR_LOCK_DONE") + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				break;

			case STATUS_DIALOG_INIT:
				status(HBCIUtils.getLocMsg("STATUS_DIALOG_INIT"));
				break;

			case STATUS_DIALOG_INIT_DONE:
				status(HBCIUtils.getLocMsg("STATUS_DIALOG_INIT_DONE",o[1]) + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				break;

			case STATUS_SEND_TASK:
				status(HBCIUtils.getLocMsg("STATUS_DIALOG_NEW_JOB",((HBCIJob)o[0]).getName()));
				break;

			case STATUS_SEND_TASK_DONE:
				status(HBCIUtils.getLocMsg("STATUS_DIALOG_JOB_DONE",((HBCIJob)o[0]).getName()));
				break;

			case STATUS_DIALOG_END:
				status(HBCIUtils.getLocMsg("STATUS_DIALOG_END"));
				break;

			case STATUS_DIALOG_END_DONE:
				status(HBCIUtils.getLocMsg("STATUS_DIALOG_END_DONE") + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				break;

			case STATUS_MSG_CREATE:
				status(HBCIUtils.getLocMsg("STATUS_MSG_CREATE",o[0].toString()));
				break;

			case STATUS_MSG_SIGN:
				status(HBCIUtils.getLocMsg("STATUS_MSG_SIGN"));
				break;

			case STATUS_MSG_CRYPT:
				status(HBCIUtils.getLocMsg("STATUS_MSG_CRYPT"));
				break;

			case STATUS_MSG_SEND:
				status(HBCIUtils.getLocMsg("STATUS_MSG_SEND"));
				break;

			case STATUS_MSG_RECV:
				status(HBCIUtils.getLocMsg("STATUS_MSG_RECV"));
				break;

			case STATUS_MSG_PARSE:
				status(HBCIUtils.getLocMsg("STATUS_MSG_PARSE",o[0].toString()+")"));
				break;

			case STATUS_MSG_DECRYPT:
				status(HBCIUtils.getLocMsg("STATUS_MSG_DECRYPT"));
				break;

			case STATUS_MSG_VERIFY:
				status(HBCIUtils.getLocMsg("STATUS_MSG_VERIFY"));
				break;

			default:
				throw new HBCI_Exception(HBCIUtils.getLocMsg("STATUS_INVALID",Integer.toString(statusTag)));
		}
    
  }

}


/**********************************************************************
 * $Log: HBCICallbackSWT.java,v $
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