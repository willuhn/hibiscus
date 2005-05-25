/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/BetragDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/25 00:42:04 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Eingabe eines Geld-Betrages.
 */
public class BetragDialog extends AbstractDialog
{

	private I18N i18n    = null;

	private Input betrag = null;
  
  private String text  = null;
  private Double value = null;

  /**
   * ct.
   * @param position
   */
  public BetragDialog(int position)
  {
    super(position);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Speichert den anzuzeigenden Text.
   * @param text
   */
  public void setText(String text)
  {
    this.text = text;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
  	LabelGroup group = new LabelGroup(parent,i18n.tr("Betrag"));
  	group.addText(text == null ? i18n.tr("Bitte geben Sie den Betrag ein.") : text,true);
  	group.addLabelPair(i18n.tr("Betrag"),getBetrag());

		ButtonArea buttons = new ButtonArea(parent,2);
		buttons.addButton(i18n.tr("OK"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	value = (Double) getBetrag().getValue();
      	close();
      }
    },null,true);
    buttons.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	throw new OperationCanceledException();
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return value;
  }

	private Input getBetrag()
	{
		if (betrag == null)
    {
      betrag = new DecimalInput(0.0,HBCI.DECIMALFORMAT);
      betrag.setComment(i18n.tr("in der Währung des Kontos"));
    }
		return betrag;
	}

}


/**********************************************************************
 * $Log: BetragDialog.java,v $
 * Revision 1.1  2005/05/25 00:42:04  web0
 * @N Dialoge fuer OP-Verwaltung
 *
 **********************************************************************/