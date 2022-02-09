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

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
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
 * Default-Werten belegt. Alternativ kann auch ein existierender
 * Sammel-Auftrag ausgewaehlt werden, dem die Auftraege zugeordnet
 * werden sollen.
 * Rueckgabewert des Dialogs ist der Sammel-Auftrag. Entweder
 * der uebergebene (erweitert um Konto und Bezeichnung) oder der
 * existierende und vom User ausgewaehlte.
 */
public class TransferMergeDialog extends AbstractDialog
{

	private final static I18N i18n    = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
	private SammelTransfer transfer   = null;
  private Boolean delete            = Boolean.FALSE;

  private CheckboxInput useExisting = null;
  private SelectInput existing      = null;

  private KontoInput konto          = null;
  private TextInput bezeichnung     = null;

  /**
   * ct.
   * @param t der zugehoerige Sammel-Auftrag.
   * @param position
   */
  public TransferMergeDialog(SammelTransfer t, int position) {
    super(position);

    this.transfer = t;
    this.setTitle(i18n.tr("Aufträge zusammenführen"));
    this.setSize(550,SWT.DEFAULT);
  }

  @Override
  protected Object getData() throws Exception {
    return this.transfer;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {

    if (this.transfer.ausgefuehrt())
      throw new ApplicationException(i18n.tr("Der Auftrag wurde bereits ausgeführt"));

    SimpleContainer container = new SimpleContainer(parent);

    container.addInput(this.getUseExisting());
    container.addInput(this.getExistingList());
    container.addSeparator();
    container.addInput(this.getKonto());
    container.addInput(this.getBezeichnung());

    final CheckboxInput delBox = new CheckboxInput(this.delete.booleanValue());
    container.addCheckbox(delBox,i18n.tr("Einzelaufträge nach Übernahme in den Sammel-Auftrag löschen"));

    final LabelInput comment = new LabelInput("");
    comment.setColor(Color.ERROR);
    container.addLabelPair("",comment);

    ButtonArea b = new ButtonArea();
		b.addButton(i18n.tr("Übernehmen"), new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          delete = (Boolean) delBox.getValue();
          boolean use = ((Boolean) getUseExisting().getValue()).booleanValue();

          if (use)
          {
            // Wir verwenden einen existierenden Auftrag
            transfer = (SammelTransfer) getExistingList().getValue();
            if (transfer == null)
            {
              comment.setValue(i18n.tr("Bitte wählen Sie einen Auftrag aus."));
              return;
            }
          }
          else
          {
            // Wir erstellen einen neuen Auftrag
            // Checken, ob Konto ausgewaehlt wurde
            Konto konto = (Konto) getKonto().getValue();
            if (konto == null)
            {
              comment.setValue(i18n.tr("Bitte wählen Sie ein Konto aus."));
              return;
            }
            transfer.setKonto(konto);

            // Checken, ob Bezeichnung eingegeben wurde
            String text = (String) getBezeichnung().getValue();
            if (text == null || text.length() == 0)
            {
              comment.setValue(i18n.tr("Bitte geben Sie eine Bezeichnung ein."));
              return;
            }
            transfer.setBezeichnung(text);
            transfer.setTermin(new Date());
          }
        }
        catch (RemoteException e)
        {
          Logger.error("error while checking transfer",e);
          throw new ApplicationException(i18n.tr("Fehler beim Prüfen des Auftrages"));
        }
				close();
      }
    },null,false,"ok.png");
		b.addButton(i18n.tr("Abbrechen"), new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
				throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");

		container.addButtonArea(b);
		getShell().setMinimumSize(getShell().computeSize(550,SWT.DEFAULT));
  }

  /**
   * Liefert true, wenn der urspruengliche Auftrag nach der Uebernahme geloescht werden soll.
   * @return true, wenn der urspruengliche Auftrag nach der Uebernahme geloescht werden soll.
   */
  public boolean getDelete()
  {
    return this.delete.booleanValue();
  }

  /**
   * Liefert eine Checkbox, mit der zwischen "Neuer Sammel-Auftrag" und "Existierenden benutzen" umgeschaltet werden kann.
   * @return Checkbox.
   * @throws RemoteException
   */
  private CheckboxInput getUseExisting() throws RemoteException
  {
    if (this.useExisting != null)
      return this.useExisting;

    this.useExisting = new CheckboxInput(false);
    this.useExisting.setName(i18n.tr("Einem existierenden Sammel-Auftrag zuordnen"));
    this.useExisting.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        try
        {
          boolean use = ((Boolean)useExisting.getValue()).booleanValue();
          getExistingList().setEnabled(use);
          getKonto().setEnabled(!use);
          getBezeichnung().setEnabled(!use);
        }
        catch (Exception e)
        {
          Logger.error("error while switching state",e);
        }
      }
    });

    return this.useExisting;
  }

  /**
   * Liefert eine Selectbox mit den bereits existierenden Sammel-Auftraegen, die noch nicht ausgefuehrt wurden.
   * @return Selectbox.
   * @throws RemoteException
   */
  private SelectInput getExistingList() throws RemoteException
  {
    if (this.existing != null)
      return this.existing;

    HBCIDBService service = (HBCIDBService) Settings.getDBService();
    DBIterator list = this.transfer.getList();
    list.addFilter("ausgefuehrt = 0");
    list.setOrder("ORDER BY " + service.getSQLTimestamp("termin") + " DESC, id DESC");
    this.existing = new SelectInput(list,null);
    this.existing.setName(i18n.tr("Existierende Aufträge"));
    this.existing.setPleaseChoose(i18n.tr("Bitte wählen..."));
    this.existing.setEnabled(false);

    // Wenn wir keine existierenden Auftraege haben, koennen wir das gleich komplett deaktivieren
    if (list.size() == 0)
    {
      getUseExisting().setValue(Boolean.FALSE);
      getUseExisting().setEnabled(false);
    }

    return this.existing;
  }

  /**
   * Liefert eine Selectbox fuer das Konto bei Neuanlage des Sammelauftrages.
   * @return Selectbox.
   * @throws RemoteException
   */
  private SelectInput getKonto() throws RemoteException
  {
    if (this.konto == null)
    {
      this.konto = new KontoInput(this.transfer.getKonto(),KontoFilter.ONLINE);
      this.konto.setMandatory(true);
    }
    return this.konto;
  }

  /**
   * Liefert ein Textfeld fuer die Eingabe der Bezeichnung bei Neuanlage des Sammelauftrages.
   * @return Textfeld.
   * @throws RemoteException
   */
  private TextInput getBezeichnung() throws RemoteException
  {
    if (this.bezeichnung != null)
      return this.bezeichnung;

    String s = this.transfer.getBezeichnung();
    if (s == null || s.length() == 0)
      s = i18n.tr("Sammel-Auftrag vom {0}", HBCI.DATEFORMAT.format(new Date()));

    this.bezeichnung = new TextInput(s,255);
    this.bezeichnung.setMandatory(true);
    this.bezeichnung.setName(i18n.tr("Bezeichnung"));
    return this.bezeichnung;
  }

}
