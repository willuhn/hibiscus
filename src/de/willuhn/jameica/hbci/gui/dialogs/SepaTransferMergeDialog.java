/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Sicherheitsabfrage vor dem Zusammenfassen von SEPA-Einzelauftraegen in ein
 * oder mehrere SEPA-Sammelauftraege.
 */
public class SepaTransferMergeDialog extends AbstractDialog
{
	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
	
  private int count           = 1;
  private boolean canDelete   = false;
  private Boolean delete      = null;

  private CheckboxInput check = null;


  /**
   * ct.
   * @param position die Position des Dialogs.
   * @param count Anzahl der Sammel-Auftraege, die erzeugt werden.
   * @param canDelete true, wenn eine Checkbox angezeigt werden soll, ob die
   * Einzel-Auftraege geloescht werden koennen.
   */
  public SepaTransferMergeDialog(int position, int count, boolean canDelete)
  {
    super(position);

    this.count     = count;
    this.canDelete = canDelete;
    this.setTitle(i18n.tr("Zu SEPA-Sammelauftrag zusammenführen"));
    this.setSize(420,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return delete;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    SimpleContainer container = new SimpleContainer(parent);
    if (this.count > 1)
      container.addText(i18n.tr("Die Einzelaufträge werden zu {0} Sammelaufträgen zusammengefasst.",String.valueOf(this.count)),true);
    else
      container.addText(i18n.tr("Die Einzelaufträge werden zu einem Sammelauftrag zusammengefasst."),true);

    if (this.canDelete)
    {
      this.check = new CheckboxInput(false);
      this.check.setName(i18n.tr("Einzelaufträge nach der Übernahme löschen"));
      container.addInput(this.check);
    }
 
    ButtonArea b = new ButtonArea();
		b.addButton(i18n.tr("Fortsetzen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        if (check != null)
          delete = (Boolean) check.getValue();
				close();
      }
    },null,false,"ok.png");
		b.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
				throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");

		container.addButtonArea(b);
		getShell().setMinimumSize(getShell().computeSize(350,SWT.DEFAULT));
  }
}
