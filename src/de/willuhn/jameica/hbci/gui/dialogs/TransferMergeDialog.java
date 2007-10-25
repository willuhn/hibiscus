/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/TransferMergeDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/10/25 15:47:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Oeffnet einen Dialog, mit dem mehrere Einzelauftraege zu einem
 * Sammelauftrag zusammengefasst werden koennen. Der Dialog fragt
 * hierzu noch das Konto ab, ueber den der Sammeltransfer abgewickelt
 * werden soll sowie eine Bezeichnung. Beide Felder sind mit sinnvollen
 * Default-Werten belegt.
 * Der Rueckgabe-Wert des Dialogs ist vom Typ Boolean und legt fest,
 * ob die urspruenglichen Einzel-Auftraege geloescht werden sollen,
 * wenn das Uebernehmen in den Sammel-Auftrag erfolgreich war.
 */
public class TransferMergeDialog extends AbstractDialog {

	private I18N i18n;
	private SammelTransfer transfer;
  private Boolean data = Boolean.TRUE;

  /**
   * ct.
   * @param t der zugehoerige Sammel-Auftrag.
   * @param position
   */
  public TransferMergeDialog(SammelTransfer t, int position) {
    super(position);

		this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    this.transfer = t;
    this.setTitle(i18n.tr("Aufträge zusammenführen"));
    
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception {
    return this.data;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception {

		LabelGroup group = new LabelGroup(parent,i18n.tr("Eigenschaften des Sammel-Auftrages"));

    if (this.transfer.ausgefuehrt())
      throw new ApplicationException(i18n.tr("Der Auftrag wurde bereits ausgeführt"));


    DBIterator konten = (DBIterator) Settings.getDBService().createList(Konto.class);

    final SelectInput kto = new SelectInput(konten,this.transfer.getKonto());
    kto.setAttribute("longname");
    kto.setPleaseChoose(i18n.tr("Bitte wählen..."));
    kto.setMandatory(true);
		group.addLabelPair(i18n.tr("Konto"),kto);

    String s = this.transfer.getBezeichnung();
    if (s == null || s.length() == 0)
      s = i18n.tr("Sammel-Auftrag vom {0}", HBCI.DATEFORMAT.format(new Date()));
		
    final TextInput name = new TextInput(s,255);
    name.setMandatory(true);
		group.addLabelPair(i18n.tr("Bezeichnung"),name);

    final CheckboxInput delete = new CheckboxInput(this.data.booleanValue());
    group.addCheckbox(delete,i18n.tr("Einzelaufträge nach Übernahme in den Sammel-Auftrag löschen"));
    
		group.addSeparator();
    
    final LabelInput comment = new LabelInput("");
    comment.setColor(Color.ERROR);
    group.addLabelPair("",comment);

    ButtonArea b = new ButtonArea(parent,2);
		b.addButton(i18n.tr("Übernehmen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        // Checken, ob Konto ausgewaehlt wurde
        try
        {
          Konto konto = (Konto) kto.getValue();
          if (konto == null)
          {
            comment.setValue(i18n.tr("Bitte wählen Sie ein Konto aus."));
            return;
          }
          transfer.setKonto(konto);
          
          // Checken, ob Bezeichnung eingegeben wurde
          String text = (String) name.getValue();
          if (text == null || text.length() == 0)
          {
            comment.setValue(i18n.tr("Bitte geben Sie eine Bezeichnung ein."));
            return;
          }
          transfer.setBezeichnung(text);
        }
        catch (RemoteException e)
        {
          Logger.error("error while checking transfer",e);
          throw new ApplicationException(i18n.tr("Fehler beim Prüfen des Auftrages"));
        }
        data = (Boolean) delete.getValue();
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

}


/**********************************************************************
 * $Log: TransferMergeDialog.java,v $
 * Revision 1.1  2007/10/25 15:47:21  willuhn
 * @N Einzelauftraege zu Sammel-Auftraegen zusammenfassen (BUGZILLA 402)
 *
 **********************************************************************/