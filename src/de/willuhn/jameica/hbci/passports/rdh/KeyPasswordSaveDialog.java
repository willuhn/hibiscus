/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
 * Dialog fÃ¼r die Eingabe des Passwortes beim Speichern eines Schluessels.
 */
public class KeyPasswordSaveDialog extends NewPasswordDialog
{
	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
	
	// BUGZILLA 1707
  private final static char[] INVALID_CHARS = new char[]
  {
      167, // §
      176, // °
      180, // <Forward Tick> (links neben der Backspace-Taste), habs nicht hingeschrieben, weil es nicht in Latin1 enthalten ist, die Java-Datei aber das Encoding verwendet
      196, // Ä
      214, // Ö
      220, // Ü
      223, // ß
      228, // ä
      246, // ö
      252, // ü
  };
	
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
      text += "\n\n" + i18n.tr("Geben Sie bitte mindestens 8 Zeichen ein.");
    else
      text += "\n\n" + i18n.tr("Die folgenden Zeichen dürfen nicht enthalten sein: {0}",String.copyValueOf(INVALID_CHARS));
    setText(text + "\n");
  }
  
  @Override
  protected boolean checkPassword(String password, String password2)
  {
    // Erstmal checken, ob die Passwoerter grundsaetzlich ok sind.
    if (!super.checkPassword(password, password2))
      return false;
    
    // Jetzt checken wir noch, ob eines der unerlaubten Zeichen enthalten ist.
    for (char c:password.toCharArray())
    {
      for (char test:INVALID_CHARS)
      {
        if (test == c)
        {
          this.setErrorText(i18n.tr("Das folgende Zeichen darf nicht enthalten sein: {0}",String.valueOf(test)));
          return false;
        }
      }
    }
    
    return true;
  }
  
}
