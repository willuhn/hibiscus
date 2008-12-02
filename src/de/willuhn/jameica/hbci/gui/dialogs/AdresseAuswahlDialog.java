/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/AdresseAuswahlDialog.java,v $
 * $Revision: 1.4 $
 * $Date: 2008/12/02 10:52:23 $
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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.parts.EmpfaengerList;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, ueber den man eine Adresse auswaehlen kann.
 */
public class AdresseAuswahlDialog extends AbstractDialog
{

	private I18N i18n;
	private Address choosen = null;

  /**
   * ct.
   * @param position
   */
  public AdresseAuswahlDialog(int position)
  {
    super(position);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		this.setTitle(i18n.tr("Adressbuch"));
    this.setSize(SWT.DEFAULT,300);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    // Dialog bei Druck auf ESC automatisch schliessen
    parent.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        if (e.keyCode == SWT.ESC)
          throw new OperationCanceledException();
      }
    });

    Action a = new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof Address))
          return;
        choosen = (Address) context;
        close();
      }
    };    
		final EmpfaengerList empf = new EmpfaengerList(a);
    empf.setContextMenu(null);
    empf.setMulti(false);
    empf.setSummary(false);
    empf.paint(parent);

		ButtonArea b = new ButtonArea(parent,2);
		b.addButton(i18n.tr("Übernehmen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
				Object o = empf.getSelection();
        if (o == null || !(o instanceof Address))
          return;

        choosen = (Address) o;
        close();
      }
    });
		b.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
				throw new OperationCanceledException();
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
 * $Log: AdresseAuswahlDialog.java,v $
 * Revision 1.4  2008/12/02 10:52:23  willuhn
 * @B DecimalInput kann NULL liefern
 * @B Double.NaN beruecksichtigen
 *
 * Revision 1.3  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.2  2006/02/21 23:55:32  willuhn
 * @N Update auf hbci4java rc6
 *
 * Revision 1.1  2005/08/16 21:33:13  willuhn
 * @N Kommentar-Feld in Adressen
 * @N Neuer Adress-Auswahl-Dialog
 * @B Checkbox "in Adressbuch speichern" in Ueberweisungen
 *
 **********************************************************************/