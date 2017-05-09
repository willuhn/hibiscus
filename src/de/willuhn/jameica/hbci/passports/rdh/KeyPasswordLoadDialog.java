/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/KeyPasswordLoadDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/05/24 09:06:11 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.rdh;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.HBCIPassportRDHNew;

import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.PasswordDialog;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.JameicaCompat;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.SynchronizeSession;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Dialog für die Eingabe eines Passwortes beim Laden des Schluessels.
 */
public class KeyPasswordLoadDialog extends PasswordDialog
{
	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private String filename = null;

  /**
   * ct.
   * @param passport optionale Angabe des Passports.
   * @param password das Vorgabe-Passwort.
   */
  public KeyPasswordLoadDialog(HBCIPassport passport, String password)
  {
    super(POSITION_CENTER);
    setSize(550,SWT.DEFAULT);
    
    if (password != null && password.length() > 0)
    {
      try
      {
        JameicaCompat.set(this,password,"setPassword","enteredPassword");
      }
      catch (Exception e)
      {
        Logger.error("unable to apply password",e);
      }
    }

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
 * $Log: KeyPasswordLoadDialog.java,v $
 * Revision 1.1  2011/05/24 09:06:11  willuhn
 * @C Refactoring und Vereinfachung von HBCI-Callbacks
 *
 * Revision 1.8  2011-05-03 10:13:15  willuhn
 * @R Hintergrund-Farbe nicht mehr explizit setzen. Erzeugt auf Windows und insb. Mac teilweise unschoene Effekte. Besonders innerhalb von Label-Groups, die auf Windows/Mac andere Hintergrund-Farben verwenden als der Default-Hintergrund
 **********************************************************************/