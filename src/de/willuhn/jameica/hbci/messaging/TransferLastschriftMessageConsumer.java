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

import java.util.Date;
import java.util.Map;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.LastschriftNew;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Empfaengt Nachrichten mit generischen Parameter-Maps,
 * aus denen dann Lastschriften erstellt werden.
 * Damit koennen Fremd-Plugins Auftraege in Hibiscus
 * erzeugen, ohne eine Classpath-Abhaengigkeit zu
 * Hibiscus haben zu muessen.
 * 
 * Wird von JVerein verwendet.
 */
public class TransferLastschriftMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{QueryMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    if (message == null || !(message instanceof QueryMessage))
      return;

    Object data = ((QueryMessage)message).getData();
    if (data == null || !(data instanceof Map))
    {
      Logger.warn("got invalid or null message, skipping");
      return;
    }

    Lastschrift ls = null;

    try
    {
      Map params        = (Map) data;
      DBService service = Settings.getDBService();
      ls                = (Lastschrift) service.createObject(Lastschrift.class,null);

      Number betrag = (Number) params.get("value");
      if (betrag != null)
        ls.setBetrag(betrag.doubleValue());

      String type = (String) params.get("type");
      if (type != null)
        ls.setTextSchluessel(type);

      Date termin = (Date) params.get("date");
      ls.setTermin(termin != null ? termin : new Date());

      ls.setZweck((String) params.get("usage.1"));
      ls.setZweck2((String) params.get("usage.2"));
      ls.setGegenkontoName((String) params.get("other.name"));
      ls.setGegenkontoNummer((String) params.get("other.account"));
      ls.setGegenkontoBLZ((String) params.get("other.blz"));

      // Jetzt schauen wir noch, ob wir das Konto finden,
      // ueber das der Auftrag abgewickelt werden soll.
      String konto = (String) params.get("my.account");
      String blz   = (String) params.get("my.blz");
      boolean stored = false;
      if (konto != null && blz != null)
      {
        DBIterator list = service.createList(Konto.class);
        list.addFilter("kontonummer = ?", new Object[]{konto});
        list.addFilter("blz = ?",         new Object[]{blz});
        if (list.hasNext())
        {
          // Jepp, wir haben das Konto.
          ls.setKonto((Konto) list.next());

          // Nur, wenn wir das Konto haben, koennen wir
          // versuchen, den Auftrag zu speichern
          ls.store();
          stored = true;
        }
      }
      if (!stored)
        throw new ApplicationException(Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N().tr("Bitte vervollständigen Sie die Angaben in Ihrer Lastschrift"));
    }
    catch (ApplicationException ae)
    {
      // Es ist ein fachlicher Fehler aufgetreten -
      // z.Bsp. weil eine Pflichteingabe fehlte.
      // In dem Fall zeigen wir die Fehlermeldung
      // an sowie den Dialog zur Erfassung der Lastschrift
      // damit der User die restlichen Daten eingeben kann
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
      if (!Application.inServerMode())
        new LastschriftNew().handleAction(ls);
    }
    catch (Exception e)
    {
      // Es ist ein anderer, schwerwiegender Fehler
      // aufgetreten. Da wir nicht wissen, wie wir
      // den hier sinnvoll behandeln koennen, loggen
      // wir ihn und pappen ihn in die zugehoerige
      // QueryMessage. Dann kann der Versender der
      // Nachricht (vorausgesetzt, er hat sie synchron
      // gesendet) selbst zusehen, wie er damit klarkommt ;)
      Logger.error("unable to create lastschrift",e);
      ((QueryMessage)message).setData(e);
    }
  }

}

/**********************************************************************
 * $Log: TransferLastschriftMessageConsumer.java,v $
 * Revision 1.5  2011/10/17 13:55:22  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2008/08/01 11:05:14  willuhn
 * @N BUGZILLA 587
 *
 * Revision 1.3  2008/02/05 10:28:17  willuhn
 * @B
 *
 * Revision 1.2  2008/02/05 00:51:03  willuhn
 * @C NPE-Checks
 *
 * Revision 1.1  2008/02/05 00:48:43  willuhn
 * @N Generischer MessageConsumer zur Erstellung von Lastschriften (Siehe Mail an Markus vom 04.02.2008)
 *
 **********************************************************************/
