/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/TANDialog.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/05/23 10:47:29 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.pintan;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCIContext;
import de.willuhn.jameica.hbci.synchronize.SynchronizeSession;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Dialog für die TAN-Eingabe.
 * Es muss weder Text, noch Titel oder LabelText gesetzt werden.
 * Das ist alles schon drin.
 */
public class TANDialog extends PasswordDialog
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  protected final static int WINDOW_WIDTH = 550;

  private PinTanConfig config = null;
  private HibiscusDBObject context = null;
  
  /**
   * ct.
   * @param config die PINTAN-Config.
   * @throws RemoteException
   */
  public TANDialog(PinTanConfig config) throws RemoteException
  {
    super(TANDialog.POSITION_CENTER);
    
    this.config = config;
		this.setSize(WINDOW_WIDTH,SWT.DEFAULT);

    // Deaktivierung der Anzeige von Sternen im TAN-Dialog.
    setShowPassword(this.config != null && this.config.getShowTan());
    
    setLabelText(i18n.tr("TAN"));
    
    // Einmal aufrufen, damit der Text gesetzt wird.
    setText(null);

    String s = null;
    try
    {
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      SynchronizeSession session = service.get(HBCISynchronizeBackend.class).getCurrentSession();
      Konto konto = session != null ? session.getKonto() : null;
      
      if (konto != null)
      {
        s = konto.getBezeichnung();
        String name = HBCIProperties.getNameForBank(konto.getBLZ());
        if (name != null && name.length() > 0)
          s += " [" + name + "]";
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to determine current konto",e);
    }

    if (s != null) setTitle(i18n.tr("TAN-Eingabe - Konto {0}",s));
    else           setTitle(i18n.tr("TAN-Eingabe"));
  
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#paint(org.eclipse.swt.widgets.Composite)
   * BUGZILLA 738
   */
  protected void paint(Composite parent) throws Exception
  {
    super.paint(parent);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#checkPassword(java.lang.String)
   */
  protected boolean checkPassword(String password)
	{
		if (password == null || password.length() == 0)
		{
			setErrorText(i18n.tr("Bitte geben Sie eine TAN ein.") + " " + getRetryString());
			return false;
		}
		return true;
	}
  
  /**
   * Speichert den zugehoerigen Auftrag, insofrn ermittelbar.
   * @param context der zugehoerige Auftrag.
   */
  public void setContext(HibiscusDBObject context)
  {
    this.context = context;
  }
  
  /**
   * Liefert den zugehoerigen Auftrag, insofern ermittelbar.
   * @return transfer der zugehoerige Auftrag.
   */
  public HibiscusDBObject getContext()
  {
    return context;
  }

  /**
   * BUGZILLA 150
   * @see PasswordDialog#setText(String)
   */
  public void setText(String text)
  {
    if (text == null || text.length() == 0)
    {
      text = i18n.tr("Bitte geben Sie eine TAN-Nummer ein.");
    }
    else
    {
      // Der Text kann ein "challenge" von der Bank enthalten. Dieser kann Formatierungen enthalten.
      // Z.Bsp. "<b>Text</b>", "<li>..." usw. Siehe
      // FinTS_3.0_Security_Sicherheitsverfahren_PINTAN_Rel_20101027_final_version.pdf
      // Seite 127. Das muss noch beachtet werden
      // Eigentlich kann man in den BPD noch nachschauen, ob fuer das TAN-Verfahren "ischallengestructured=J",
      // aber das brauchen wir nicht. Wenn HTML-Tags drin stehen, ersetzen wir sie gegen Formatierungen.
      
      text = text.replaceAll("<br>","\n");
      text = text.replaceAll("<p>","\n\n");
      
      text = text.replaceAll("<p>","\n\n");
      
      text = text.replaceAll("<ul>","\n");
      text = text.replaceAll("</ul>","");
      text = text.replaceAll("<ol>","\n");
      text = text.replaceAll("</ol>","");
      text = text.replaceAll("</li>","\n");

      text = text.replaceAll("<li>","  - ");

      // Unterstuetzen wir noch nicht
      text = text.replaceAll("<b>","");
      text = text.replaceAll("</b>","");
      text = text.replaceAll("<i>","");
      text = text.replaceAll("</i>","");
      text = text.replaceAll("<u>","");
      text = text.replaceAll("</u>","");
    }

    String ctx = HBCIContext.toString(this.context);
    if (ctx != null)
    {
      text += ("\n\n" + i18n.tr("Auftrag:\n{0}",ctx));
    }
    else
    {
      String s = null;
      try
      {
        BeanService service = Application.getBootLoader().getBootable(BeanService.class);
        SynchronizeSession session = service.get(HBCISynchronizeBackend.class).getCurrentSession();
        Konto konto = session != null ? session.getKonto() : null;
        
        if (konto != null)
        {
          s = konto.getBezeichnung();
          String name = HBCIProperties.getNameForBank(konto.getBLZ());
          if (name != null && name.length() > 0)
            s += " [" + name + "]";
        }
      }
      catch (Exception e)
      {
        Logger.error("unable to determine current konto",e);
      }

      if (s != null)
        text += ("\n\n" + i18n.tr("Konto:\n{0}",s));
    }

    super.setText(text);
  }
  
  /**
	 * Liefert einen locale String mit der Anzahl der Restversuche.
	 * z.Bsp.: "Noch 2 Versuche.".
   * @return String mit den Restversuchen.
   */
  private String getRetryString()
	{
		String retries = getRemainingRetries() > 1 ? i18n.tr("Versuche") : i18n.tr("Versuch");
		return (i18n.tr("Noch") + " " + getRemainingRetries() + " " + retries + ".");
	}
}


/**********************************************************************
 * $Log: TANDialog.java,v $
 * Revision 1.5  2011/05/23 10:47:29  willuhn
 * @R BUGZILLA 62 - Speichern der verbrauchten TANs ausgebaut. Seit smsTAN/chipTAN gibt es zum einen ohnehin keine TAN-Listen mehr. Zum anderen kann das jetzt sogar Fehler ausloesen, wenn ueber eines der neuen TAN-Verfahren die gleiche TAN generiert wird, die frueher irgendwann schonmal zufaellig generiert wurde. TANs sind inzwischen fluechtige und werden dynamisch erzeugt. Daher ist es unsinnig, die zu speichern. Zumal es das Wallet sinnlos aufblaeht.
 *
 * Revision 1.4  2011-05-16 15:34:59  willuhn
 * @N TAN-Text kann formatiert sein
 *
 * Revision 1.3  2011-05-09 17:27:39  willuhn
 * @N Erste Vorbereitungen fuer optisches chipTAN
 *
 * Revision 1.2  2011-05-09 09:25:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2010/06/17 11:38:15  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 **********************************************************************/