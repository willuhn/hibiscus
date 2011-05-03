/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/PassportLoadDialog.java,v $
 * $Revision: 1.8 $
 * $Date: 2011/05/03 10:13:15 $
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
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.HBCIPassportRDHNew;

import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Dialog für die Eingabe eines Passwortes beim Laden des Passports.
 */
public class PassportLoadDialog extends PasswordDialog {

	private I18N i18n = null;
  private String filename = null;

  /**
   * ct.
   * @param passport optionale Angabe des Passports.
   */
  public PassportLoadDialog(HBCIPassport passport)
  {
    super(POSITION_CENTER);
    setSize(550,SWT.DEFAULT);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    Konto konto = HBCIFactory.getInstance().getCurrentKonto();
    
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
    
    String text = null;
    if (s != null)
    {
      setTitle(i18n.tr("Schlüsseldiskette. Konto: {0}",s));
      text = i18n.tr("Bitte geben Sie das Passwort der Schlüsseldiskette ein.\nKonto: {0}",s);
    }
    else
    {
      setTitle(i18n.tr("Schlüsseldiskette"));
      text = i18n.tr("Bitte geben Sie das Passwort der Schlüsseldiskette ein.");
    }
    
    if (passport != null && (passport instanceof HBCIPassportRDHNew))
      this.filename = ((HBCIPassportRDHNew)passport).getFilename();

    setText(text);
    setLabelText(i18n.tr("Ihr Passwort"));
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#extend(de.willuhn.jameica.gui.util.Container)
   */
  protected void extend(Container container) throws Exception
  {
    if (this.filename == null)
      return;
    
    Part p = new Part() {
      public void paint(Composite parent) throws RemoteException
      {
        String text = i18n.tr("Schlüssel-Datei: {0}",filename);

        final Label comment = new Label(parent,SWT.WRAP);
        comment.setText(text);
        comment.setForeground(Color.COMMENT.getSWTColor());
        comment.setLayoutData(new GridData(GridData.FILL_BOTH));
        // Workaround fuer Windows, weil dort mehrzeilige
        // Labels nicht korrekt umgebrochen werden.
        comment.addControlListener(new ControlAdapter() {
          public void controlResized(ControlEvent e)
          {
            comment.setSize(comment.computeSize(comment.getSize().x,SWT.DEFAULT));
          }
        });
      }
    };
    container.addPart(p);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#checkPassword(java.lang.String)
   */
  protected boolean checkPassword(String password)
	{
		if (password == null || password.length() == 0)
		{
			setErrorText(i18n.tr("Fehler: Bitte geben Sie Ihr Passwort ein.") + " " + getRetryString());
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
 * $Log: PassportLoadDialog.java,v $
 * Revision 1.8  2011/05/03 10:13:15  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 *
 * Revision 1.7  2008/02/27 16:12:57  willuhn
 * @N Passwort-Dialog fuer Schluesseldiskette mit mehr Informationen (Konto, Dateiname)
 *
 * Revision 1.6  2007/01/05 17:23:24  jost
 * Zeilenumbruch korrigiert.
 *
 * Revision 1.5  2006/12/24 10:28:06  jost
 * Korrektur Tippfehler
 *
 * Revision 1.4  2005/02/07 22:06:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2005/02/06 19:03:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/02/01 17:15:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/01/09 23:21:05  willuhn
 * *** empty log message ***
 *
 **********************************************************************/