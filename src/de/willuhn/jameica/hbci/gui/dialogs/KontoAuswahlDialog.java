/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/KontoAuswahlDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/10/17 16:28:46 $
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, ueber den man ein Konto auswaehlen kann.
 */
public class KontoAuswahlDialog extends AbstractDialog
{

	private I18N i18n;
	private Konto choosen = null;

  /**
   * ct.
   * @param position
   */
  public KontoAuswahlDialog(int position)
  {
    super(position);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		this.setTitle(i18n.tr("Konto-Auswahl"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
		LabelGroup group = new LabelGroup(parent,i18n.tr(""));
			
		group.addText(i18n.tr("Bitte wählen Sie das gewünschte Konto aus.") + "\n",true);

		final SelectInput kto = new SelectInput(Settings.getDBService().createList(Konto.class),null);
		kto.setAttribute("longname");
		group.addLabelPair(i18n.tr("Konto"),kto);

		ButtonArea b = group.createButtonArea(2);
		b.addCustomButton(i18n.tr("OK"), new Listener()
		{
			public void handleEvent(Event event)
			{
				choosen = (Konto) kto.getValue();
				close();
			}
		});
		b.addCustomButton(i18n.tr("Abbrechen"), new Listener()
		{
			public void handleEvent(Event event)
			{
				close();
			}
		});
  }

  /**
   * Liefert das ausgewaehlte Konto zurueck oder <code>null</code> wenn der
   * Abbrechen-Knopf gedrueckt wurde.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return choosen;
  }

}


/**********************************************************************
 * $Log: KontoAuswahlDialog.java,v $
 * Revision 1.1  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 **********************************************************************/