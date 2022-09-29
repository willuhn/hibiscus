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

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.PanelButtonPrint;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungDelete;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungExecute;
import de.willuhn.jameica.hbci.gui.action.Duplicate;
import de.willuhn.jameica.hbci.gui.controller.AuslandsUeberweisungControl;
import de.willuhn.jameica.hbci.gui.parts.PanelButtonNew;
import de.willuhn.jameica.hbci.io.print.PrintSupportAuslandsUeberweisung;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Auslands-Ueberweisungen.
 */
public class AuslandsUeberweisungNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private MessageConsumer mc = new MyMessageConsumer();
  private AuslandsUeberweisung transfer = null;

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		final AuslandsUeberweisungControl control = new AuslandsUeberweisungControl(this);
    this.transfer = control.getTransfer();

		GUI.getView().setTitle(i18n.tr("SEPA-Überweisung bearbeiten"));
    GUI.getView().addPanelButton(new PanelButtonNew(AuslandsUeberweisung.class));
    GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportAuslandsUeberweisung(transfer)));
		
    Container cl = new SimpleContainer(getParent());
    cl.addHeadline(i18n.tr("Konto"));
		cl.addInput(control.getKontoAuswahl());
		
    ColumnLayout cols = new ColumnLayout(getParent(),2);
		
    // Linke Seite
    {
      Container container = new SimpleContainer(cols.getComposite());
      container.addHeadline(i18n.tr("Empfänger"));
      container.addLabelPair(i18n.tr("Name"),                      control.getEmpfaengerName());
      container.addLabelPair(i18n.tr("IBAN"),                      control.getEmpfaengerKonto());    
      container.addLabelPair(i18n.tr("BIC"),                       control.getEmpfaengerBic());
      container.addInput(control.getStoreEmpfaenger());
      
      container.addHeadline(i18n.tr("Auftragswiederholung (nur Hibiscus-intern)"));
      container.addText(i18n.tr("Diese Information wird nicht an die Bank übertragen."),true);
      container.addInput(control.getReminderInterval());
    }
    
    // Rechte Seite
    {
      Container container = new SimpleContainer(cols.getComposite());
      container.addHeadline(i18n.tr("SEPA"));
      container.addInput(control.getEndToEndId());
      container.addInput(control.getPmtInfId());
      container.addInput(control.getPurposeCode());
      container.addHeadline(i18n.tr("Sonstige Informationen"));
      container.addInput(control.getTyp());
      container.addInput(control.getTermin());
    }

    Container container = new SimpleContainer(getParent());
    container.addHeadline(i18n.tr("Details"));
    container.addLabelPair(i18n.tr("Verwendungszweck"),					control.getZweck());
    container.addLabelPair(i18n.tr("Betrag"),                   control.getBetrag());

		ButtonArea buttonArea = new ButtonArea();
		buttonArea.addButton(i18n.tr("Löschen"),new AuslandsUeberweisungDelete(),transfer,false,"user-trash-full.png");
    buttonArea.addButton(i18n.tr("Duplizieren..."), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (control.handleStore()) // BUGZILLA 1181
          new Duplicate().handleAction(transfer);
      }
    },null,false,"edit-copy.png");

    Button execute = new Button(i18n.tr("Jetzt ausführen..."), new Action() {
      public void handleAction(Object context) throws ApplicationException {
				if (control.handleStore())
  				new AuslandsUeberweisungExecute().handleAction(transfer);
      }
    },null,false,"emblem-important.png");
    execute.setEnabled(!transfer.ausgefuehrt());
    
    Button store = new Button(i18n.tr("&Speichern"), new Action() {
      public void handleAction(Object context) throws ApplicationException {
      	control.handleStore();
      }
    },null,!transfer.ausgefuehrt(),"document-save.png");
    store.setEnabled(!transfer.ausgefuehrt());
    
    buttonArea.addButton(execute);
    buttonArea.addButton(store);
    
    buttonArea.paint(getParent());
    
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
        GUI.startView(AuslandsUeberweisungNew.this,transfer);
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
