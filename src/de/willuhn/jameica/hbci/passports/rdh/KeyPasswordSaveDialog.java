/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/KeyPasswordSaveDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/09/08 07:06:12 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.rdh;

import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.HBCIPassportRDHXFile;

import de.willuhn.jameica.gui.dialogs.NewPasswordDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Dialog für die Eingabe des Passwortes beim Speichern eines Schluessels.
 */
public class KeyPasswordSaveDialog extends NewPasswordDialog
{
	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
	
  /**
   * ct.
   * @param position Position des Dialogs.
   * @param passport optionale Angabe des Passport.
   */
  public KeyPasswordSaveDialog(int position, HBCIPassport passport)
  {
    super(position);

    setTitle(i18n.tr("Passwort-Eingabe"));
    setLabelText(i18n.tr("Ihr Passwort"));
    
    String text = i18n.tr("Bitte vergeben Sie ein Passwort, mit dem der zu speichernde\nSchlüssel geschützt werden soll.");
    if (passport instanceof HBCIPassportRDHXFile)
      text += "\nGeben Sie bitte mindestens 8 Zeichen ein.";
    setText(text);
  }
}


/**********************************************************************
 * $Log: KeyPasswordSaveDialog.java,v $
 * Revision 1.2  2011/09/08 07:06:12  willuhn
 * @N Mindestens 8 Zeichen Passwort-Laenge bei RDH2 siehe Mail von "silentspeak" vom 08.09.2011
 *
 * Revision 1.1  2011-05-24 09:06:11  willuhn
 * @C Refactoring und Vereinfachung von HBCI-Callbacks
 *
 * Revision 1.6  2010-11-22 11:30:51  willuhn
 * @C Cleanup
 *
 **********************************************************************/