/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/SynchronizeOptionsDialog.java,v $
 * $Revision: 1.6 $
 * $Date: 2009/03/17 23:44:15 $
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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, ueber den die Synchronisierungs-Optionen fuer ein Konto eingestellt werden koennen.
 */
public class SynchronizeOptionsDialog extends AbstractDialog
{
  private I18N i18n   = null;
  private Konto konto = null;

  /**
   * ct.
   * @param konto das Konto.
   * @param position
   */
  public SynchronizeOptionsDialog(Konto konto, int position)
  {
    super(position);
    this.konto = konto;
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.setTitle(i18n.tr("Synchronisierungsoptionen"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Optionen"));
    
    final SynchronizeOptions options = new SynchronizeOptions(konto);

    final CheckboxInput saldo  = new CheckboxInput(options.getSyncSaldo());
    final CheckboxInput auszug = new CheckboxInput(options.getSyncKontoauszuege());
    final CheckboxInput ueb    = new CheckboxInput(options.getSyncUeberweisungen());
    final CheckboxInput last   = new CheckboxInput(options.getSyncLastschriften());
    final CheckboxInput dauer  = new CheckboxInput(options.getSyncDauerauftraege());
    final CheckboxInput aueb   = new CheckboxInput(options.getSyncAuslandsUeberweisungen());

    group.addText(i18n.tr("Bitte wählen Sie aus, welche Geschäftsvorfälle bei\nder Synchronisierung des Kontos ausgeführt werden sollen."),false);
    group.addCheckbox(saldo ,i18n.tr("Saldo abrufen"));
    group.addCheckbox(auszug,i18n.tr("Kontoauszüge (Umsätze) abrufen"));
    group.addCheckbox(ueb   ,i18n.tr("Überfällige Überweisungen absenden"));
    group.addCheckbox(aueb  ,i18n.tr("Überfällige Auslandsüberweisungen absenden"));
    group.addCheckbox(last  ,i18n.tr("Überfällige Lastschriften einziehen"));
    group.addCheckbox(dauer ,i18n.tr("Daueraufträge synchronisieren"));
    
    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("Übernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        options.setSyncSaldo(((Boolean)saldo.getValue()).booleanValue());
        options.setSyncKontoauszuege(((Boolean)auszug.getValue()).booleanValue());
        options.setSyncUeberweisungen(((Boolean)ueb.getValue()).booleanValue());
        options.setSyncLastschriften(((Boolean)last.getValue()).booleanValue());
        options.setSyncDauerauftraege(((Boolean)dauer.getValue()).booleanValue());
        options.setSyncAuslandsUeberweisungen(((Boolean)aueb.getValue()).booleanValue());
        close();
      }
    });
    buttons.addButton(i18n.tr("Abbrechen"), new Action() {
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


/*********************************************************************
 * $Log: SynchronizeOptionsDialog.java,v $
 * Revision 1.6  2009/03/17 23:44:15  willuhn
 * @N BUGZILLA 159 - Auslandsueberweisungen. Erste Version
 *
 * Revision 1.5  2007/03/23 00:11:51  willuhn
 * @N Bug 346
 *
 * Revision 1.4  2006/10/09 21:43:26  willuhn
 * @N Zusammenfassung der Geschaeftsvorfaelle "Umsaetze abrufen" und "Saldo abrufen" zu "Kontoauszuege abrufen" bei der Konto-Synchronisation
 *
 * Revision 1.3  2006/04/18 22:38:16  willuhn
 * @N bug 227
 *
 * Revision 1.2  2006/03/27 21:34:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2006/03/21 00:43:14  willuhn
 * @B bug 209
 *
 **********************************************************************/