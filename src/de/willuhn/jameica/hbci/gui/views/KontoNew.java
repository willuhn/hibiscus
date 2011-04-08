/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/KontoNew.java,v $
 * $Revision: 1.36 $
 * $Date: 2011/04/08 15:19:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
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
    Konto k = control.getKonto();
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
    left.addLabelPair(i18n.tr("Kundennummer"),             control.getKundennummer());
		left.addLabelPair(i18n.tr("Kontonummer"),              control.getKontonummer());
		left.addLabelPair(i18n.tr("Unterkontonummer"),         control.getUnterkonto());
		left.addLabelPair(i18n.tr("Bankleitzahl"),             control.getBlz());
		left.addLabelPair(i18n.tr("Sicherheitsmedium"),        control.getPassportAuswahl());

    SimpleContainer right = new SimpleContainer(columns.getComposite(),true);
    right.addHeadline(i18n.tr("IBAN/BIC"));
    right.addLabelPair(i18n.tr("IBAN"),                    control.getIban());
    right.addLabelPair(i18n.tr("BIC"),                     control.getBic());

    right.addHeadline(i18n.tr("Notizen"));
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
    folder.setBackground(Color.BACKGROUND.getSWTColor());

    TabGroup tab = new TabGroup(folder,i18n.tr("Umsätze der letzten {0} Tage",""+HBCIProperties.UMSATZ_DEFAULT_DAYS), false,1);
    control.getUmsatzList().paint(tab.getComposite());

    TabGroup tab2 = new TabGroup(folder,i18n.tr("Saldo im Verlauf"),false,1);
    control.getSaldoChart().paint(tab2.getComposite());

    boolean scripting = Application.getPluginLoader().isInstalled("de.willuhn.jameica.scripting.Plugin");

    ButtonArea buttons = new ButtonArea();

    Button fetch = null;

    int flags = control.getKonto().getFlags();
    if ((flags & Konto.FLAG_OFFLINE) == Konto.FLAG_OFFLINE)
    {
      fetch = new Button(i18n.tr("Umsatz anlegen"), new UmsatzDetailEdit(),control.getKonto(),false,"emblem-documents.png");
      
      if (scripting)
      {
        Button sync = new Button(i18n.tr("via Scripting synchronisieren"), new KontoSyncViaScripting(),control.getKonto(),false,"mail-send-receive.png");
        sync.setEnabled((flags & Konto.FLAG_DISABLED) != Konto.FLAG_DISABLED);
        buttons.addButton(sync);
      }
    }
    else
    {
      fetch = new Button(i18n.tr("Saldo und Umsätze abrufen"), new KontoFetchUmsaetze(),control.getKonto(),false,"mail-send-receive.png");
    }
    
    fetch.setEnabled((flags & Konto.FLAG_DISABLED) != Konto.FLAG_DISABLED);
    buttons.addButton(fetch);
    
    buttons.addButton(i18n.tr("Alle Umsätze anzeigen"),     new UmsatzList(),control.getKonto(),false,"text-x-generic.png");
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


/**********************************************************************
 * $Log: KontoNew.java,v $
 * Revision 1.36  2011/04/08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.35  2010-11-19 18:37:20  willuhn
 * @N Erste Version der Termin-View mit Appointment-Providern
 *
 * Revision 1.34  2010-08-11 16:06:05  willuhn
 * @N BUGZILLA 783 - Saldo-Chart ueber alle Konten
 *
 * Revision 1.33  2010-07-25 23:11:59  willuhn
 * @N Erster Code fuer Scripting-Integration
 *
 * Revision 1.32  2010/06/17 12:32:56  willuhn
 * @N BUGZILLA 530
 *
 * Revision 1.31  2010/04/22 16:40:57  willuhn
 * @N Manuelles Anlegen neuer Umsaetze fuer Offline-Konten moeglich
 *
 * Revision 1.30  2010/04/22 16:21:27  willuhn
 * @N HBCI-relevante Buttons und Aktionen fuer Offline-Konten sperren
 *
 * Revision 1.29  2010/04/22 12:42:03  willuhn
 * @N Erste Version des Supports fuer Offline-Konten
 **********************************************************************/