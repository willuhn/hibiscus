/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.messaging;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Checkt das Auftragslimit fuer einen Auftrag und zeigt bei Ueberschreitung eine Warnung an.
 */
public class CheckLimitMessageConsumer implements MessageConsumer
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  @Override
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{QueryMessage.class};
  }

  /**
   * Erwartet ein Objekt vom Typ <code>HibiscusTransfer</code>.
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  @Override
  public void handleMessage(Message message) throws Exception
  {
    QueryMessage msg = (QueryMessage) message;
    Object o = msg.getData();
    if (!(o instanceof Transfer))
    {
      Logger.warn("got invalid data: " + o);
      return;
    }

    Transfer t = (Transfer) o;
    double limit = Settings.getUeberweisungLimit();

    String curr = HBCIProperties.CURRENCY_DEFAULT_DE;
    if (t instanceof HibiscusTransfer)
    {
      Konto k = ((HibiscusTransfer) t).getKonto();
      curr = k.getWaehrung();
    }
    if (t.getBetrag() > limit)
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Auftragslimit überschritten: {0} {1}", HBCI.DECIMALFORMAT.format(limit),curr),StatusBarMessage.TYPE_INFO));
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  @Override
  public boolean autoRegister()
  {
    return false; // per Manifest registriert
  }

}
