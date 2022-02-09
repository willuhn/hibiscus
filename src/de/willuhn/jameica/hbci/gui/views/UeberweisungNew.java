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
import de.willuhn.jameica.hbci.gui.controller.UeberweisungControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportUeberweisung;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Bearbeitung der Ueberweisungen.
 */
public class UeberweisungNew extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private MessageConsumer mc = new MyMessageConsumer();
  private Ueberweisung transfer = null;

  @Override
  public void bind() throws Exception {

		final UeberweisungControl control = new UeberweisungControl(this);
    this.transfer = (Ueberweisung) control.getTransfer();

		GUI.getView().setTitle(i18n.tr("�berweisung bearbeiten"));
		GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportUeberweisung(transfer)));

    Container c1 = new SimpleContainer(getParent());
    c1.addHeadline(i18n.tr("Konto"));
    c1.addInput(control.getKontoAuswahl());

		ColumnLayout cols = new ColumnLayout(getParent(),2);
		
		// Linke Seite
		{
      Container container = new SimpleContainer(cols.getComposite());
	    container.addHeadline(i18n.tr("Empf�nger"));
	    container.addInput(control.getEmpfaengerName());
	    container.addInput(control.getEmpfaengerKonto());    
	    container.addInput(control.getEmpfaengerBlz());    
	    container.addCheckbox(control.getStoreEmpfaenger(),i18n.tr("In Adressbuch �bernehmen"));
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
    container.addInput(control.getTyp());

		ButtonArea buttonArea = new ButtonArea();
		buttonArea.addButton(i18n.tr("L�schen"),new DBObjectDelete(),transfer,false,"user-trash-full.png");
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
        GUI.startView(UeberweisungNew.this,transfer);
    }
  
    @Override
    public boolean autoRegister()
    {
      return false;
    }
  }
}
