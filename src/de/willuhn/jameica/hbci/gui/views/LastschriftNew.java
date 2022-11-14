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
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.controller.LastschriftControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportLastschrift;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Lastschriften.
 */
public class LastschriftNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private MessageConsumer mc = new MyMessageConsumer();
  private Lastschrift transfer = null;

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

		final LastschriftControl control = new LastschriftControl(this);
    this.transfer = (Lastschrift) control.getTransfer();


		GUI.getView().setTitle(i18n.tr("Lastschrift bearbeiten"));
    GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportLastschrift(transfer)));
		
    Container c1 = new SimpleContainer(getParent());
    c1.addHeadline(i18n.tr("Konto"));
    c1.addInput(control.getKontoAuswahl());

    ColumnLayout cols = new ColumnLayout(getParent(),2);

    // Linke Seite
    {
      Container container = new SimpleContainer(cols.getComposite());
      container.addHeadline(i18n.tr("Zahlungspflichtiger"));
      container.addInput(control.getEmpfaengerName());
      container.addInput(control.getEmpfaengerKonto());
      container.addInput(control.getEmpfaengerBlz());
      container.addInput(control.getStoreEmpfaenger());
    }
    
    // Rechte Seite
    {
      Container container = new SimpleContainer(cols.getComposite());
      container.addHeadline(i18n.tr("Sonstige Informationen"));
      container.addInput(control.getTextSchluessel());
      container.addInput(control.getTermin());
      container.addInput(control.getReminderInterval());
    }
    
    Container container = new SimpleContainer(getParent());
    container.addHeadline(i18n.tr("Details"));
    container.addInput(control.getZweck());
    container.addInput(control.getZweck2());
    container.addInput(control.getBetrag());

		ButtonArea buttonArea = new ButtonArea();
		buttonArea.addButton(i18n.tr("Löschen"), new DBObjectDelete(),transfer,false,"user-trash-full.png");
		Button store = new Button(i18n.tr("Speichern"), new Action() {
      public void handleAction(Object context) throws ApplicationException {
      	control.handleStore();
      }
    },null,!transfer.ausgefuehrt(),"document-save.png");
    store.setEnabled(!transfer.ausgefuehrt());
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
        GUI.startView(LastschriftNew.this,transfer);
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
