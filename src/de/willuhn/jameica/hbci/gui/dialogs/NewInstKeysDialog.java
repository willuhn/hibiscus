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
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.INILetter;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
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
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static int WINDOW_WIDTH = 540;

	private final HBCIPassport passport;
	private Boolean choosen       = null;

  /**
   * ct.
   * @param p Passport, fuer den die Schluessel angezeigt werden sollen.
   */
  public NewInstKeysDialog(HBCIPassport p)
  {
    super(NewInstKeysDialog.POSITION_CENTER);
		this.passport = p;
		setTitle(i18n.tr("Neue Bank-Schlüssel erhalten"));
    setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#onEscape()
   */
  protected void onEscape()
  {
    // Escape is nich. Der User soll explizit auf "Abbrechen" klicken
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
		Container group = new SimpleContainer(parent);
		group.addText(i18n.tr(
      "Bitte vergleichen Sie die von der Bank übermittelten Hash-Werte (Checksummen)\n" +
      "mit denen in Ihren Unterlagen. Stimmen diese mit den folgenden Werten überein,\n" +      "dann bestätigen Sie bitte mit OK.\n" +
      "Andernfalls brechen Sie den Vorgang aus Sicherheitsgründen bitte ab."),true);

		INILetter iniletter = new INILetter(passport,INILetter.TYPE_INST);

    group.addHeadline(i18n.tr("Hashwert"));
    group.addText(HBCIUtils.data2hex(iniletter.getKeyHashDisplay()).toUpperCase(),true,Color.ERROR);
    
    group.addHeadline(i18n.tr("Exponent"));
    group.addText(HBCIUtils.data2hex(iniletter.getKeyExponentDisplay()).toUpperCase(),true);

    group.addHeadline(i18n.tr("Modulus"));
    group.addText(HBCIUtils.data2hex(iniletter.getKeyModulusDisplay()).toUpperCase(),true);


		ButtonArea buttons = new ButtonArea();
		buttons.addButton(i18n.tr("OK"),new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				choosen = Boolean.TRUE;
				close();
			}
		},null,false,"ok.png");
		buttons.addButton(i18n.tr("Abbrechen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				choosen = Boolean.FALSE;
        close();
			}
		},null,false,"process-stop.png");
		
		group.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
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
 * Revision 1.12  2011/05/24 09:06:11  willuhn
 * @C Refactoring und Vereinfachung von HBCI-Callbacks
 *
 * Revision 1.11  2010/06/14 23:00:59  willuhn
 * @C Dialog-Groesse angepasst
 * @N Datei-Auswahldialog mit nativem Ueberschreib-Hinweis
 *
 * Revision 1.10  2009/07/27 13:43:45  willuhn
 * @N Neue HBCI4Java-Version (2.5.10) mit RDH-10-Support
 *
 * Revision 1.9  2009/03/11 23:41:52  willuhn
 * *** empty log message ***
 *
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