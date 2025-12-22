/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.passport.AbstractPinTanPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.dialogs.PasswordDialog;
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
 * Dialog für die PIN-Eingabe.
 * Es muss weder Text, noch Titel oder LabelText gesetzt werden.
 * Das ist alles schon drin.
 */
public class PINDialog extends PasswordDialog
{
	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
	
	private HBCIPassport passport = null;

  /**
   * ct.
   * @param pin die Vorgabe-PIN.
   */
  public PINDialog(String pin)
  {
    this(null,pin);
  }

	/**
   * ct.
   * @param passport der Passport.
   * @param pin die Vorgabe-PIN.
   */
  public PINDialog(HBCIPassport passport, String pin)
  {
    super(PINDialog.POSITION_CENTER);
    this.passport = passport;
    this.setSize(550,SWT.DEFAULT);
    this.setLabelText(i18n.tr("Ihre PIN"));

    if (pin != null && pin.length() > 0)
    {
      try
      {
        JameicaCompat.set(this,pin,"setPassword","enteredPassword");
      }
      catch (Exception e)
      {
        Logger.error("unable to apply pin",e);
      }
    }

    String s = null;

    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    SynchronizeSession session = service.get(HBCISynchronizeBackend.class).getCurrentSession();
    Konto konto = session != null ? session.getKonto() : null;
    
    if (konto != null)
    {
      try
      {
        s = konto.getBezeichnung();
        s += " [" + i18n.tr("Nr.") + " " + konto.getKontonummer();
        String name = HBCIProperties.getNameForBank(konto.getBLZ());
        if (name != null && name.length() > 0)
          s += " - " + name;
        s += "]";
      }
      catch (Exception e)
      {
        Logger.error("unable to determine account data",e);
      }
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
    final int min = this.getPinLengthMin();
    final int max = this.getPinLengthMax();
    
    Logger.info("PIN length limits [min: " + min + ", max: " + max + "]");
		if (password == null || password.length() < min || password.length() > max)
		{
			setErrorText(i18n.tr("Länge der PIN ungültig ({0}-{1} Zeichen)",Integer.toString(min),Integer.toString(max)) + " " + getRetryString());
			return false;
		}

    return true;
	}
  
  /**
   * Liefert die Mindest-Länge der PIN.
   * @return die Mindest-Länge der PIN.
   */
  private int getPinLengthMin()
  {
    final int defaultLen = HBCIProperties.HBCI_PIN_MINLENGTH;
    if (this.passport == null || !(this.passport instanceof AbstractPinTanPassport))
      return defaultLen;
    
    final Integer i = ((AbstractPinTanPassport)this.passport).getPinLengthMin();
    return i != null ? i.intValue() : defaultLen;
  }

  /**
   * Liefert die Höchst-Länge der PIN.
   * @return die Höchst-Länge der PIN.
   */
  private int getPinLengthMax()
  {
    final int defaultLen = HBCIProperties.HBCI_PIN_MAXLENGTH;
    if (this.passport == null || !(this.passport instanceof AbstractPinTanPassport))
      return defaultLen;
    
    final Integer i = ((AbstractPinTanPassport)this.passport).getPinLengthMax();
    return i != null ? i.intValue() : defaultLen;
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
