/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.PanelButtonPrint;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Duplicate;
import de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungDelete;
import de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungExecute;
import de.willuhn.jameica.hbci.gui.controller.SepaSammelUeberweisungControl;
import de.willuhn.jameica.hbci.gui.parts.PanelButtonNew;
import de.willuhn.jameica.hbci.io.print.PrintSupportSepaSammelUeberweisung;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der SEPA-Sammel-Ueberweisungen.
 */
public class SepaSammelUeberweisungNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private MessageConsumer mc = new MyMessageConsumer();
  private SepaSammelUeberweisung transfer = null;

  @Override
  public void bind() throws Exception
  {

		final SepaSammelUeberweisungControl control = new SepaSammelUeberweisungControl(this);
    this.transfer = control.getTransfer();

		GUI.getView().setTitle(i18n.tr("SEPA-Sammelüberweisung bearbeiten"));
    GUI.getView().addPanelButton(new PanelButtonNew(SepaSammelUeberweisung.class));
    GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportSepaSammelUeberweisung(transfer)));
		
    Container group = new SimpleContainer(getParent());
    group.addHeadline(i18n.tr("Eigenschaften"));
    group.addLabelPair(i18n.tr("Zu belastendes Konto"),control.getKontoAuswahl());
    group.addLabelPair(i18n.tr("Bezeichnung"),control.getName());

    ColumnLayout cols = new ColumnLayout(getParent(),2);
    
    // Linke Seite
    {
      Container container = new SimpleContainer(cols.getComposite());
      container.addHeadline(i18n.tr("SEPA"));
      container.addInput(control.getBatchBook());
      container.addInput(control.getPmtInfId());
    }
    
    // Rechte Seite
    {
      Container container = new SimpleContainer(cols.getComposite());
      container.addHeadline(i18n.tr("Auftragswiederholung (nur Hibiscus-intern)"));
      container.addText(i18n.tr("Diese Information wird nicht an die Bank übertragen."),true);
      container.addInput(control.getReminderInterval());
      container.addHeadline(i18n.tr("Sonstige Informationen"));
      container.addInput(control.getTyp());
      container.addInput(control.getTermin());
    }
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Sammelauftrag löschen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new SepaSammelUeberweisungDelete().handleAction(context);
        try
        {
          // Buchungen aus der Liste entfernen, wenn der Auftrag geloescht wurde
          if (transfer.getID() == null)
            control.getBuchungen().removeAll();
        }
        catch (RemoteException re)
        {
          Logger.error("unable to remove bookings",re);
        }
      }
    },transfer,false,"user-trash-full.png");
    buttons.addButton(i18n.tr("Duplizieren..."), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (control.handleStore())
          new Duplicate().handleAction(transfer);
      }
    },null,false,"edit-copy.png");

    Button add = new Button(i18n.tr("Neue Buchungen hinzufügen"), new Action() {
      public void handleAction(Object context) throws ApplicationException {
        if (control.handleStore())
          new de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungBuchungNew().handleAction(transfer);
      }
    },null,false,"text-x-generic.png");
    add.setEnabled(!transfer.ausgefuehrt());
    
		Button execute = new Button(i18n.tr("Jetzt ausführen..."), new Action() {
			public void handleAction(Object context) throws ApplicationException {
        if (control.handleStore())
  				new SepaSammelUeberweisungExecute().handleAction(transfer);
			}
		},null,false,"emblem-important.png");
    execute.setEnabled(!transfer.ausgefuehrt());
    
    Button store = new Button(i18n.tr("&Speichern"),new Action() {
      public void handleAction(Object context) throws ApplicationException {
        control.handleStore();
      }
    },null,!transfer.ausgefuehrt(),"document-save.png");
    store.setEnabled(!transfer.ausgefuehrt());
    
    buttons.addButton(add);
    buttons.addButton(execute);
    buttons.addButton(store);
    
    buttons.paint(getParent());

    new Headline(getParent(),i18n.tr("Enthaltene Buchungen"));
    control.getBuchungen().paint(getParent());
    
    Application.getMessagingFactory().registerMessageConsumer(this.mc);
  }

  @Override
  public void unbind() throws ApplicationException
  {
    super.unbind();
    this.transfer = null;
    Application.getMessagingFactory().unRegisterMessageConsumer(this.mc);
  }

  /**
   * Wird beanchrichtigt, wenn der Auftrag ausgefuehrt wurde und laedt die
   * View dann neu.
   */
  private class MyMessageConsumer implements MessageConsumer
  {
  
    @Override
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{ObjectChangedMessage.class};
    }
  
    @Override
    public void handleMessage(Message message) throws Exception
    {
      if (transfer == null)
        return;
  
      GenericObject o = ((ObjectChangedMessage) message).getObject();
      if (o == null)
        return;
      
      // View neu laden
      if (transfer.equals(o))
        GUI.startView(SepaSammelUeberweisungNew.this,transfer);
    }
  
    @Override
    public boolean autoRegister()
    {
      return false;
    }
  }
}
