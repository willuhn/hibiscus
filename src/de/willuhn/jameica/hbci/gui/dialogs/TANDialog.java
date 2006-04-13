/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/TANDialog.java,v $
 * $Revision: 1.11 $
 * $Date: 2006/04/13 10:36:13 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Dialog für die TAN-Eingabe.
 * Es muss weder Text, noch Titel oder LabelText gesetzt werden.
 * Das ist alles schon drin.
 */
public class TANDialog extends PasswordDialog
{

	private I18N i18n;
  /**
   * ct.
   */
  public TANDialog()
  {
    super(TANDialog.POSITION_CENTER);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    // Deaktivierung der Anzeige von Sternen im TAN-Dialog.
    setShowPassword(Settings.getShowTan());
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
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#checkPassword(java.lang.String)
   */
  protected boolean checkPassword(String password)
	{
		if (password == null || password.length() == 0)
		{
			setErrorText(i18n.tr("Fehler: Bitte geben Sie eine TAN ein.") + " " + getRetryString());
			return false;
		}
		return true;
	}

	/**
   * BUGZILLA 150
	 * @see de.willuhn.jameica.gui.dialogs.SimpleDialog#setText(java.lang.String)
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

    if (s != null)
    {
      if (text == null || text.length() == 0)
        super.setText(i18n.tr("Bitte geben Sie eine TAN-Nummer ein. Konto: {0}",s));
      else
        super.setText(text + ". " + i18n.tr("Konto: {0}",s));
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
 * Revision 1.11  2006/04/13 10:36:13  willuhn
 * @B bug 150
 *
 * Revision 1.10  2006/02/06 15:40:44  willuhn
 * @B bug 150
 *
 * Revision 1.9  2005/08/02 20:33:12  web0
 * *** empty log message ***
 *
 * Revision 1.8  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.7  2005/07/26 23:00:03  web0
 * @N Multithreading-Support fuer HBCI-Jobs
 *
 * Revision 1.6  2005/06/06 09:54:39  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/06/02 22:57:34  web0
 * @N Export von Konto-Umsaetzen
 *
 * Revision 1.4  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 * Revision 1.3  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.2  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/05/05 21:27:13  willuhn
 * @N added TAN-Dialog
 *
 **********************************************************************/