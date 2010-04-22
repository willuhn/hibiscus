package de.willuhn.jameica.hbci.messaging;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Prüft eingehende Umsätze, ob diese als Gegenkonto ein eigenes Offlinekonto haben.
 * Wenn ja, wird der entsprechende Umsatz für das Offlinekonto angelegt.
 */
public class CheckOfflineUmsatzMessageConsumer implements MessageConsumer
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{ImportMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    // Wenn es keine Import-Message ist ignorieren wir die folgenden
    if (message == null || !(message instanceof ImportMessage))
      return;
    
    GenericObject o = ((ImportMessage)message).getObject();
    
    if (o == null || o.getID() == null || !(o instanceof Umsatz))
      return; // interessiert uns nicht
    
    // wir haben einen Umsatz, den es zu bearbeiten gilt...
    Umsatz u = (Umsatz) o;

//  // prüfen ob die Gegenbuchung zu diesem Umsatz bereits existiert.
//  // Dies ist vor allem wichtig, wenn eine Überweisung zwischen 2 Offlinekonten durchgeführt wurde
//  if (u.getGegenbuchung()!=null)
//    return false;
  
    // pruefen, ob das Gegenkonto ein eigenes Konto und dieses ein Offlinekonto ist.
    Konto gegenkonto = KontoUtil.find(u.getGegenkontoNummer(), u.getGegenkontoBLZ());
    if (gegenkonto == null || (gegenkonto.getFlags() & Konto.FLAG_OFFLINE) == 0)
      return;

    // Kopie der Buchung erzeugen
    Umsatz gegenbuchung = null;
    
    try
    {
      gegenbuchung = u.duplicate();

      // Betrag negieren
      gegenbuchung.setBetrag(-gegenbuchung.getBetrag());

      // Konten tauschen
      gegenbuchung.setKonto(gegenkonto);
      Konto k = u.getKonto();
      gegenbuchung.setGegenkontoNummer(k.getKontonummer());
      gegenbuchung.setGegenkontoBLZ(k.getBLZ());
      gegenbuchung.setGegenkontoName(k.getName());
      
      // Art des Umsatzes setzen, Laenge ggf. auf DB-Feldlaenge küuerzen
      String art = i18n.tr("Auto-Buchung Offline-Konto zu '{0}'",u.getArt());
      if (art.length()>100) art = art.substring(0, 100);
      gegenbuchung.setArt(art);
      
      // Saldo berechnen
      gegenbuchung.setSaldo(gegenkonto.getSaldo() + gegenbuchung.getBetrag());
      
      // Umsatztyp loeschen
      gegenbuchung.setUmsatzTyp(null);

      // Saldo am Offline-Konto anpassen
      gegenkonto.setSaldo(gegenbuchung.getSaldo());
      
      //////////////////////////////////////////////////////////////////////////
      // BEGIN Transaction
      gegenbuchung.transactionBegin();
      
      gegenbuchung.store(); // Umsatz speichern
      gegenkonto.store();   // Konto mit geaendertem Saldo speichern
      
      gegenbuchung.transactionCommit();
      // END Transaction
      //////////////////////////////////////////////////////////////////////////
      
      // neuen Umsatz bekannt geben
      Application.getMessagingFactory().sendMessage(new ImportMessage(gegenbuchung));
    }
    catch (Exception e)
    {
      if (gegenbuchung != null)
      {
        try
        {
          gegenbuchung.transactionRollback();
        }
        catch (Exception e2)
        {
          Logger.error("rollback failed",e2);
        }
      }
      throw e;
    }
  }

}
