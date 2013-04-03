/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/KontoNew.java,v $
 * $Revision: 1.39 $
 * $Date: 2012/05/18 13:25:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.action.KontoDelete;
import de.willuhn.jameica.hbci.gui.action.KontoFetchUmsaetze;
import de.willuhn.jameica.hbci.gui.action.KontoSyncViaScripting;
import de.willuhn.jameica.hbci.gui.action.ProtokollList;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetailEdit;
import de.willuhn.jameica.hbci.gui.action.UmsatzList;
import de.willuhn.jameica.hbci.gui.controller.KontoControl;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bankverbindung bearbeiten.
 */
public class KontoNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private KontoControl control = null;
  
  /**
   * ct,
   */
  public KontoNew()
  {
    this.control = new KontoControl(this);
  }
  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    final Konto k = control.getKonto();
    if (k != null && !k.isNewObject())
    {
      String s1 = k.getBezeichnung();
      if (s1 == null) s1 = "";

      String s2 = k.getKontonummer();
      if (s2 == null) s2 = "";

      GUI.getView().setTitle(i18n.tr("Konto-Details: {0} [Kto.-Nr.: {1}]",new String[]{s1,s2}));
    }
    else
  		GUI.getView().setTitle(i18n.tr("Konto-Details: Neues Konto"));

    final KontoInput quickSelect = new KontoInput(k,KontoFilter.ALL);
    quickSelect.setName(i18n.tr("Konto wechseln"));
    quickSelect.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        try
        {
          Konto choice = (Konto) quickSelect.getValue();
          if (choice == null)
            return;

          if (k.equals(choice))
            return; // kein Wechsel stattgefunden
          
          new de.willuhn.jameica.hbci.gui.action.KontoNew().handleAction(choice);
        }
        catch (OperationCanceledException oce)
        {
          // ignore
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
        catch (RemoteException re)
        {
          Logger.error("unable to switch konto",re);
        }
      }
    });
    quickSelect.paint(this.getParent());

    ColumnLayout columns = new ColumnLayout(getParent(),2);
    SimpleContainer left = new SimpleContainer(columns.getComposite());

    left.addHeadline(i18n.tr("Eigenschaften"));
    left.addLabelPair(i18n.tr("Bezeichnung des Kontos"),   control.getBezeichnung());
    left.addLabelPair(i18n.tr("Kontoinhaber"),             control.getName());
    left.addLabelPair(i18n.tr("Saldo"),                    control.getSaldo());
    
    Input avail = control.getSaldoAvailable();
    if (avail != null)
      left.addLabelPair(i18n.tr("Verfügbarer Betrag"),avail);

    left.addHeadline(i18n.tr("HBCI-Konfiguration"));
    left.addLabelPair(i18n.tr("Verfahren"),                control.getPassportAuswahl());
    left.addLabelPair(i18n.tr("Kundenkennung"),            control.getKundennummer());
		left.addLabelPair(i18n.tr("Kontonummer"),              control.getKontonummer());
		left.addLabelPair(i18n.tr("Unterkontonummer"),         control.getUnterkonto());
		left.addLabelPair(i18n.tr("Bankleitzahl"),             control.getBlz());

    SimpleContainer right = new SimpleContainer(columns.getComposite(),true);
    right.addHeadline(i18n.tr("IBAN/BIC"));
    right.addLabelPair(i18n.tr("IBAN"),                    control.getIban());
    right.addLabelPair(i18n.tr("BIC"),                     control.getBic());

    right.addHeadline(i18n.tr("Notiz"));
    right.addPart(control.getKommentar());
    right.addInput(control.getOffline());

    // und noch die Abschicken-Knoepfe
		ButtonArea buttonArea = new ButtonArea();
    buttonArea.addButton(control.getSynchronizeOptions());
    buttonArea.addButton(i18n.tr("Protokoll des Kontos"),new ProtokollList(),control.getKonto(),false,"dialog-information.png");
		buttonArea.addButton(i18n.tr("Konto löschen"),new KontoDelete(),control.getKonto(),false,"user-trash-full.png");
		buttonArea.addButton(i18n.tr("Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	control.handleStore();
      }
    },null,false,"document-save.png");
		buttonArea.paint(getParent());

    
    TabFolder folder = new TabFolder(getParent(), SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));

    TabGroup tab = new TabGroup(folder,i18n.tr("Umsätze der letzten {0} Tage",""+HBCIProperties.UMSATZ_DEFAULT_DAYS), false,1);
    control.getUmsatzList().paint(tab.getComposite());

    TabGroup tab2 = new TabGroup(folder,i18n.tr("Saldo im Verlauf"),false,1);
    control.getSaldoChart().paint(tab2.getComposite());

    boolean scripting = Application.getPluginLoader().isInstalled("de.willuhn.jameica.scripting.Plugin");

    ButtonArea buttons = new ButtonArea();

    Button fetch = null;

    Konto konto = control.getKonto();
    if (konto.hasFlag(Konto.FLAG_OFFLINE))
    {
      fetch = new Button(i18n.tr("Umsatz anlegen"), new UmsatzDetailEdit(),konto,false,"emblem-documents.png");
      
      if (scripting)
      {
        Button sync = new Button(i18n.tr("via Scripting synchronisieren"), new KontoSyncViaScripting(),konto,false,"mail-send-receive.png");
        sync.setEnabled(!konto.hasFlag(Konto.FLAG_DISABLED));
        buttons.addButton(sync);
      }
    }
    else
    {
      fetch = new Button(i18n.tr("Saldo und Umsätze abrufen"), new KontoFetchUmsaetze(),konto,false,"mail-send-receive.png");
    }
    fetch.setEnabled(!konto.hasFlag(Konto.FLAG_DISABLED));
    buttons.addButton(fetch);
    
    buttons.addButton(i18n.tr("Alle Umsätze anzeigen"),new UmsatzList(),konto,false,"text-x-generic.png");
    buttons.paint(getParent());
  }
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#reload()
   */
  public void reload() throws ApplicationException
  {
    control.handleReload();
    super.reload();
  }
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
    Application.getMessagingFactory().unRegisterMessageConsumer(control.getSaldoMessageConsumer());
  }

}
