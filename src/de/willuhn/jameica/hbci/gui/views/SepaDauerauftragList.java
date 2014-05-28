/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.parts.PanelButtonPrint;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.KontoFetchSepaDauerauftraege;
import de.willuhn.jameica.hbci.gui.controller.SepaDauerauftragControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportSepaDauerauftrag;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen SEPA-Dauerauftraegen an.
 */
public class SepaDauerauftragList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private MessageConsumer mc = new MyMessageConsumer();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    SepaDauerauftragControl control = new SepaDauerauftragControl(this);
    
    final de.willuhn.jameica.hbci.gui.parts.SepaDauerauftragList table = control.getDauerauftragListe();
    final PanelButtonPrint print = new PanelButtonPrint(new PrintSupportSepaDauerauftrag(table));
    table.addSelectionListener(new Listener() {
      public void handleEvent(Event event)
      {
        print.setEnabled(table.getSelection() != null);
      }
    });

    GUI.getView().setTitle(i18n.tr("Vorhandene SEPA-Daueraufträge"));
    GUI.getView().addPanelButton(print);
		
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("SEPA-Daueraufträge abrufen..."),  new KontoFetchSepaDauerauftraege(),null,false,"mail-send-receive.png");
    buttons.addButton(i18n.tr("Neuer SEPA-Dauerauftrag"),        new de.willuhn.jameica.hbci.gui.action.SepaDauerauftragNew(),null,false,"text-x-generic.png");
    buttons.paint(getParent());

    table.paint(getParent());
    print.setEnabled(table.getSelection() != null); // einmal initial ausloesen
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
    super.unbind();
    Application.getMessagingFactory().unRegisterMessageConsumer(this.mc);
  }

  /**
   * Nach dem erfolgreichen Abruf der Dauerauftraege wird eine ObjectChangedNachricht
   * fuer das betreffende Konto ausgeloest. Wir laden in dem Fall die View neu.
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
      GenericObject o = ((ObjectChangedMessage) message).getObject();
      if (o == null)
        return;
      
      // View neu laden
      if (o instanceof Konto)
        GUI.startView(SepaDauerauftragList.this,null);
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
