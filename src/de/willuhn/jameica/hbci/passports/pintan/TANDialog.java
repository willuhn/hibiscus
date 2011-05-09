/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/TANDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/05/09 17:27:39 $
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
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.messaging.StatusBarMessage;
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

  private final static int WINDOW_WIDTH = 550;

  private PinTanConfig config = null;
  
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
      Konto konto = HBCIFactory.getInstance().getCurrentKonto();
      if (konto != null)
      {
        s = konto.getBezeichnung();
        String name = HBCIUtils.getNameForBLZ(konto.getBLZ());
        if (name != null && name.length() > 0)
          s += " [" + name + "]";
      }
    }
    catch (Exception e)
    {
      // ignore
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
    
    // BUGZILLA 62
    // Wir checken, ob es die TAN schon gibt
    // Wir pruefen hier bewusst nicht, ob das Feature eingeschaltet
    // ist, weil es der User inzwischen ausgeschaltet haben kann
    // aber schon TANs existieren koennen

    if (this.config != null)
    {
      try
      {
        Date used = this.config.getTanUsed(password);
        if (used != null)
        {
          setErrorText(i18n.tr("TAN wurde bereits am {0} verwendet.",HBCI.LONGDATEFORMAT.format(used)) + " " + getRetryString());
          return false;
        }

        // Wir speichern die TAN
        if (this.config.getSaveUsedTan())
          this.config.saveUsedTan(password);
      }
      catch (RemoteException re)
      {
        Logger.error("unable to check/save tan",re);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Prüfen/Speichern der verbrauchten TAN"),StatusBarMessage.TYPE_ERROR));
      }
    }
    else
    {
      Logger.warn("unable to check if TAN was allready used. we have no config");
    }
		return true;
	}

  /**
   * BUGZILLA 150
   * @see PasswordDialog#setText(String)
   */
  public void setText(String text)
  {
    ////////////////////////////////////////////////////////////////////////////
    // Bezeichnung des Kontos ermitteln
    String s = null;
    try
    {
      Konto konto = HBCIFactory.getInstance().getCurrentKonto();
      if (konto != null)
      {
        s = konto.getBezeichnung();
        String name = HBCIUtils.getNameForBLZ(konto.getBLZ());
        if (name != null && name.length() > 0)
          s += " [" + name + "]";
      }
    }
    catch (Exception e)
    {
      // ignore
    }

    // TODO: Der Text kann ein "challenge" von der Bank enthalten. Dieser kann Formatierungen enthalten.
    // Z.Bsp. "<b>Text</b>", "<li>..." usw. Siehe
    // FinTS_3.0_Security_Sicherheitsverfahren_PINTAN_Rel_20101027_final_version.pdf
    // Seite 127. Das muss noch beachtet werden. Die Frage ist noch: Wie kann
    // ich das in SWT formatieren.
    
    if (s != null)
    {
      if (text == null || text.length() == 0)
        super.setText(i18n.tr("Bitte geben Sie eine TAN-Nummer ein.\nKonto: {0}",s));
      else
        super.setText(text + "\n" + i18n.tr("Konto: {0}",s));
    }
    else
    {
      if (text == null || text.length() == 0)
        super.setText(i18n.tr("Bitte geben Sie eine TAN-Nummer ein."));
      else
        super.setText(text);
    }
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
 * Revision 1.3  2011/05/09 17:27:39  willuhn
 * @N Erste Vorbereitungen fuer optisches chipTAN
 *
 * Revision 1.2  2011-05-09 09:25:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2010/06/17 11:38:15  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 **********************************************************************/