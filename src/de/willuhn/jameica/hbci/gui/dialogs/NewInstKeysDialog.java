/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/NewInstKeysDialog.java,v $
 * $Revision: 1.8 $
 * $Date: 2009/03/11 23:41:36 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.INILetter;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog, welcher dem Benutzer die neu uebertragenen Instituts-Schluessel
 * zur Verifizierung anzeigt.
 */
public class NewInstKeysDialog extends AbstractDialog
{

	private HBCIPassport passport;
	private I18N i18n;
	private Boolean choosen;

  /**
   * ct.
   * @param p Passport, fuer den die Schluessel angezeigt werden sollen.
   */
  public NewInstKeysDialog(HBCIPassport p)
  {
    super(NewInstKeysDialog.POSITION_CENTER);
		this.passport = p;
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
		setTitle(i18n.tr("Neue Bank-Schlüssel erhalten"));
    setSize(540,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
		LabelGroup group = new LabelGroup(parent,i18n.tr("Schlüsseldetails"));
		group.addText(i18n.tr(
      "Bitte vergleichen Sie die von der Bank übermittelten Hash-Wert (Checksumme)\n" +
      "mit denen in Ihren Unterlagen. Stimmen diese mit den folgenden Werten überein,\n" +      "dann bestätigen Sie bitte mit OK.\n" +
      "Andernfalls brechen Sie den Vorgang aus Sicherheitsgründen bitte ab."),true);

		INILetter iniletter = new INILetter(passport,INILetter.TYPE_INST);

    LabelInput hash = new LabelInput(HBCIUtils.data2hex(iniletter.getKeyHash()));
    hash.setColor(Color.ERROR);
		group.addLabelPair(i18n.tr("Hash-Wert") + ":",hash);

		ButtonArea buttons = new ButtonArea(parent,2);
		buttons.addButton(i18n.tr("OK"),new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				choosen = new Boolean(true);
				close();
			}
		},null,true);
		buttons.addButton(i18n.tr("Abbrechen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				choosen = Boolean.FALSE;
        close();
			}
		});
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return choosen;
  }

}


/**********************************************************************
 * $Log: NewInstKeysDialog.java,v $
 * Revision 1.8  2009/03/11 23:41:36  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2006/06/06 21:42:21  willuhn
 * @N Zeilenumbrueche in Dialogen (fuer Windows)
 *
 * Revision 1.6  2005/07/24 14:46:16  web0
 * *** empty log message ***
 *
 * Revision 1.5  2005/07/12 23:29:01  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/03/23 00:05:46  web0
 * @C RDH fixes
 *
 * Revision 1.3  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/02/02 18:19:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 **********************************************************************/