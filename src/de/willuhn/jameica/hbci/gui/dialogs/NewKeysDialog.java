/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/NewKeysDialog.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/02/03 18:57:42 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog, der den neu erzeugten Schluessel anzeigt und den Benutzer
 * auffordert, den Ini-Brief an seine Bank zu senden.
 */
public class NewKeysDialog extends AbstractDialog
{
	private HBCIPassport passport;
	private I18N i18n;
	private Boolean choosen;

  /**
   */
  public NewKeysDialog(HBCIPassport p)
  {
    super(NewKeysDialog.POSITION_CENTER);
		this.passport = p;
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
		setTitle(i18n.tr("Ini-Brief erzeugen"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
		LabelGroup group = new LabelGroup(parent,i18n.tr("Ini-Brief"));
		group.addText(i18n.tr(
      "Bitte drucken Sie den Ini-Brief aus und senden Ihn an Ihre Bank.\n" +      "Nach der Freischaltung durch Ihr Geldinstitut kann dieser Schlüssel\n" +      "verwendet werden."),true);

		// TODO Ausdruck des Ini-Briefs
		ButtonArea buttons = new ButtonArea(parent,2);
		buttons.addButton(i18n.tr("OK"),new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				close();
			}
		},null,true);
		buttons.addButton(i18n.tr("Abbrechen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				close();
			}
		});
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

}


/**********************************************************************
 * $Log: NewKeysDialog.java,v $
 * Revision 1.3  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/02/02 18:19:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 **********************************************************************/