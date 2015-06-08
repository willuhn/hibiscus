/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
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
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.Duplicate;
import de.willuhn.jameica.hbci.gui.action.SepaSammelLastschriftExecute;
import de.willuhn.jameica.hbci.gui.controller.SepaSammelLastschriftControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportSepaSammelLastschrift;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der SEPA-Sammel-Lastschriften.
 */
public class SepaSammelLastschriftNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private MessageConsumer mc = new MyMessageConsumer();
  private SepaSammelLastschrift transfer = null;

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {

		final SepaSammelLastschriftControl control = new SepaSammelLastschriftControl(this);
    this.transfer = control.getTransfer();

		GUI.getView().setTitle(i18n.tr("SEPA-Sammellastschrift bearbeiten"));
    GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportSepaSammelLastschrift(transfer)));
		
    Container group = new SimpleContainer(getParent());
    group.addHeadline(i18n.tr("Eigenschaften"));
    group.addLabelPair(i18n.tr("Gutschriftskonto"),control.getKontoAuswahl());
    group.addLabelPair(i18n.tr("Bezeichnung"),control.getName());

    ColumnLayout cols = new ColumnLayout(getParent(),2);

    // Linke Seite
    {
      Container container = new SimpleContainer(cols.getComposite());
      container.addHeadline(i18n.tr("SEPA"));
      container.addText(i18n.tr("Bitte beachten Sie die Vorlauffristen."),true);
      container.addInput(control.getType());
      container.addInput(control.getSequenceType());
      container.addInput(control.getTargetDate());
      container.addInput(control.getBatchBook());
      container.addInput(control.getPmtInfId());
    }
		
    // Rechte Seite
    {
      Container container = new SimpleContainer(cols.getComposite());
      container.addHeadline(i18n.tr("Sonstige Informationen (nur Hibiscus-intern)"));
      container.addText(i18n.tr("Diese Daten werden nicht an die Bank übertragen."),true);
      container.addInput(control.getTermin());
      container.addInput(control.getReminderInterval());
    }

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Sammelauftrag löschen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new DBObjectDelete().handleAction(context);
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
        if (control.handleStore()) // BUGZILLA 1181
          new Duplicate().handleAction(transfer);
      }
    },null,false,"edit-copy.png");

    Button add = new Button(i18n.tr("Neue Buchungen hinzufügen"), new Action() {
      public void handleAction(Object context) throws ApplicationException {
        if (control.handleStore())
          new de.willuhn.jameica.hbci.gui.action.SepaSammelLastBuchungNew().handleAction(transfer);
      }
    },null,false,"text-x-generic.png");
    add.setEnabled(!transfer.ausgefuehrt());
    
		Button execute = new Button(i18n.tr("Jetzt ausführen..."), new Action() {
			public void handleAction(Object context) throws ApplicationException {
        if (control.handleStore())
  				new SepaSammelLastschriftExecute().handleAction(transfer);
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

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
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
  
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{ObjectChangedMessage.class};
    }
  
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      if (transfer == null)
        return;
  
      GenericObject o = ((ObjectChangedMessage) message).getObject();
      if (o == null)
        return;
      
      // View neu laden
      if (transfer.equals(o))
        GUI.startView(SepaSammelLastschriftNew.this,transfer);
    }
  
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
  }
}
