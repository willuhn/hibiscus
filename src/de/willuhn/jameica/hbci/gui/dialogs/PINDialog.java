/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/PINDialog.java,v $
 * $Revision: 1.23 $
 * $Date: 2011/05/24 09:06:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Dialog für die PIN-Eingabe.
 * Es muss weder Text, noch Titel oder LabelText gesetzt werden.
 * Das ist alles schon drin.
 */
public class PINDialog extends PasswordDialog
{

	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
	private HBCIPassport passport  = null;

  /**
   * ct.
   * @param passport Passport, fuer den die PIN-Abfrage gemacht wird. Grund: Der
   * PIN-Dialog hat eine eingebaute Checksummen-Pruefung um zu checken, ob die
   * PIN richtig eingegeben wurde. Da diese Checksumme aber pro Passport gespeichert
   * wird, benoetigt der Dialoig eben jenen.
   */
  public PINDialog(HBCIPassport passport)
  {
    super(PINDialog.POSITION_CENTER);
    setSize(550,SWT.DEFAULT);
    this.passport = passport;

    // BUGZILLA 71 http://www.willuhn.de/bugzilla/show_bug.cgi?id=71
    String suffix = this.passport.getCustomerId();
  
    Konto konto = HBCIFactory.getInstance().getCurrentKonto();
    if (konto != null)
    {
      try
      {
        suffix += "." + konto.getKontonummer();
      }
      catch (RemoteException e)
      {
        Logger.error("unable to append account number to pin wallet entry",e);
      }
    }

    setLabelText(i18n.tr("Ihre PIN"));
    String s = null;
    try
    {
      s = konto.getBezeichnung();
      s += " [" + i18n.tr("Nr.") + " " + konto.getKontonummer();
      String name = HBCIUtils.getNameForBLZ(konto.getBLZ());
      if (name != null && name.length() > 0)
        s += " - " + name;
      s += "]";
    }
    catch (Exception e)
    {
      // ignore
    }
    if (s != null)
    {
      setTitle(i18n.tr("PIN-Eingabe. Konto: {0}",s));
      setText(i18n.tr("Bitte geben Sie Ihre PIN ein.\nKonto: {0}",s));
    }
    else
    {
      setTitle(i18n.tr("PIN-Eingabe"));
      setText(i18n.tr("Bitte geben Sie Ihre PIN ein."));
    }
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    super.paint(parent);
    getShell().pack();
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#checkPassword(java.lang.String)
   */
  protected boolean checkPassword(String password)
	{
    // BUGZILLA 28 http://www.willuhn.de/bugzilla/show_bug.cgi?id=28
		if (password == null || password.length() < HBCIProperties.HBCI_PIN_MINLENGTH || password.length() > HBCIProperties.HBCI_PIN_MAXLENGTH)
		{
			setErrorText(i18n.tr("Länge der PIN ungültig ({0}-{1} Zeichen)",Integer.toString(HBCIProperties.HBCI_PIN_MINLENGTH),Integer.toString(HBCIProperties.HBCI_PIN_MAXLENGTH)) + " " + getRetryString());
			return false;
		}

    return true;
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
 * $Log: PINDialog.java,v $
 * Revision 1.23  2011/05/24 09:06:11  willuhn
 * @C Refactoring und Vereinfachung von HBCI-Callbacks
 *
 **********************************************************************/