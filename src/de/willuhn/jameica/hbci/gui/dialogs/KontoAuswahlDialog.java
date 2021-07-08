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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.ColorUtil;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, ueber den man ein Konto auswaehlen kann.
 */
public class KontoAuswahlDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private final static int WINDOW_WIDTH = 500;

  private String text        = null;
  private Konto choosen      = null;
  private Konto preselected  = null;
	private KontoFilter filter = null;

	private Button apply        = null;
	private KontoInput auswahl  = null;
	private LabelInput institut = null;
  private LabelInput name     = null;
	private LabelInput saldo    = null;

  /**
   * ct.
   * @param position
   */
  public KontoAuswahlDialog(int position)
  {
    this(null,position);
  }

  /**
   * ct.
   * @param preselected vorausgewaehltes Konto.
   * @param position
   */
  public KontoAuswahlDialog(Konto preselected, int position)
  {
    this(preselected,null,position);
  }

  /**
   * ct.
   * @param preselected vorausgewaehltes Konto.
   * @param filter Konto-Filter.
   * @param position
   */
  public KontoAuswahlDialog(Konto preselected, KontoFilter filter, int position)
  {
    super(position);
    this.preselected = preselected;
    this.filter      = filter;
    this.setTitle(i18n.tr("Konto-Auswahl"));
    setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent);
    group.addText(text != null && text.length() > 0 ? text : i18n.tr("Bitte wählen Sie das gewünschte Konto aus."),true);
    group.addInput(getKontoAuswahl());
    group.addInput(getInstitut());
    group.addInput(getName());
    group.addInput(getSaldo());

    // Button-Area
		ButtonArea b = new ButtonArea();
		b.addButton(this.getApplyButton());
		b.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        choosen = null;
				throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
		group.addButtonArea(b);

    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
    getKontoAuswahl().focus(); // damit wir direkt mit dem Cursor die Auswahl treffen koennen
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

  /**
   * Liefert die Auswahlbox fuer das Konto.
   * @return die Auswahlbox fuer das Konto.
   * @throws RemoteException
   */
  private KontoInput getKontoAuswahl() throws RemoteException
  {
    if (this.auswahl != null)
      return this.auswahl;

    this.auswahl = new KontoInput(this.preselected,this.filter);
    this.auswahl.setComment(null);
    auswahl.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        preselected = (Konto) auswahl.getValue();
        getApplyButton().setEnabled(preselected != null);
        try
        {
          updateInstitute();
          updateName();
          updateSaldo();
        }
        catch (Exception e)
        {
          // Nicht weiter wild - daher nur loggen
          Logger.error("unable to update account information",e);
        }
      }
    });
    return this.auswahl;
  }

  /**
   * Liefert ein Label mit dem Namen der Bank.
   * @return Label mit dem Namen der Bank.
   * @throws RemoteException
   */
  private LabelInput getInstitut() throws RemoteException
  {
    if (this.institut == null)
    {
      this.institut = new LabelInput("");
      this.institut.setName(i18n.tr("Institut"));
      updateInstitute();
    }
    return this.institut;
  }

  /**
   * Aktualisiert die Anzeige des Institut.
   */
  private void updateInstitute() throws RemoteException
  {
    getInstitut().setValue("");
    if (this.preselected != null)
    {
      String name = HBCIProperties.getNameForBank(this.preselected.getBLZ());
      if (name != null && name.length() > 0)
        getInstitut().setValue(name);
    }
  }

  /**
   * Liefert den Namen des Kontos.
   * @return Label mit dem Namen des Kontos.
   * @throws RemoteException
   */
  private LabelInput getName() throws RemoteException
  {
    if (this.name == null)
    {
      this.name = new LabelInput("");
      this.name.setName(i18n.tr("Konto"));
      updateName();
    }
    return this.name;
  }

  /**
   * Aktualisiert den Namen des Kontos.
   * @throws RemoteException
   */
  private void updateName() throws RemoteException
  {
    getName().setValue("");
    getName().setComment("");

    if (this.preselected != null)
    {
      String name = this.preselected.getBezeichnung();
      if (name != null && name.length() > 0)
        getName().setValue(name);

      String owner = this.preselected.getName();
      if (owner != null && owner.length() > 0)
        getName().setComment(owner);
    }
  }

  /**
   * Liefert ein Label mit dem Saldo des Kontos.
   * @return ein Label mit dem Saldo des Kontos.
   * @throws RemoteException
   */
  private LabelInput getSaldo() throws RemoteException
  {
    if (this.saldo == null)
    {
      this.saldo = new LabelInput("");
      this.saldo.setName("Saldo");
      updateSaldo();
    }
    return this.saldo;
  }

  /**
   * Aktualisiert den Saldo.
   * @throws RemoteException
   */
  private void updateSaldo() throws RemoteException
  {
    getSaldo().setValue("");
    getSaldo().setComment("");
    if (this.preselected != null)
    {
      Date date = this.preselected.getSaldoDatum();
      if (date != null)
      {
        double saldo = this.preselected.getSaldo();
        getSaldo().setColor(ColorUtil.getColor(saldo,Color.ERROR,Color.SUCCESS,Color.FOREGROUND));

        String curr = this.preselected.getWaehrung();
        if (curr == null || curr.length() == 0)
          curr = HBCIProperties.CURRENCY_DEFAULT_DE;

        getSaldo().setValue(HBCI.DECIMALFORMAT.format(saldo) + " " + curr);
        getSaldo().setComment(i18n.tr("aktualisiert am {0}",HBCI.LONGDATEFORMAT.format(date)));
      }
      else
      {
        getSaldo().setColor(Color.COMMENT);
        getSaldo().setValue(i18n.tr("Kein Saldo verfügbar"));
      }
    }
  }

  /**
   * Liefert den Uebernehmen-Button.
   * @return der Uebernehmen-Button.
   */
  public Button getApplyButton()
  {
    if (this.apply != null)
      return this.apply;

    this.apply = new Button(i18n.tr("Übernehmen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        choosen = preselected;
        close();
      }
    },null,true,"ok.png");
    apply.setEnabled(this.preselected != null);
    return this.apply;
  }

  /**
   * Optionale Angabe des anzuzeigenden Textes.
   * Wird hier kein Wert gesetzt, wird ein Standard-Text angezeigt.
   * @param text anzuzeigender Text.
   */
  public void setText(String text)
  {
    this.text = text;
  }

}

/**********************************************************************
 * $Log: KontoAuswahlDialog.java,v $
 * Revision 1.10  2012/04/23 21:03:41  willuhn
 * @N BUGZILLA 1227
 *
 * Revision 1.9  2011-05-06 12:35:48  willuhn
 * @N Neuer Konto-Auswahldialog mit Combobox statt Tabelle. Ist ergonomischer.
 *
 **********************************************************************/