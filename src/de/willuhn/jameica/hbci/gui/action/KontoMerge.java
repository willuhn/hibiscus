/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/KontoMerge.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/09/29 23:43:34 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Erwartet ein Array, eine Liste von Konten oder ein einzelnes Konto und gleicht sie mit den vorhandenen
 * Konten ab. Die noch nicht in der Datenbank vorhandenen werden automatisch angelegt.
 */
public class KontoMerge implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null)
      return;

    try
    {
      List<Konto> konten = new ArrayList<Konto>();
      
      if (context instanceof Konto)        konten.add((Konto) context);
      else if (context instanceof Konto[]) konten.addAll(Arrays.asList((Konto[])context));
      else if (context instanceof List)    konten.addAll((List)context);
      
      if (konten.size() == 0)
        return;
      
      DBIterator existing = Settings.getDBService().createList(Konto.class);

      int created = 0;
      int skipped = 0;
      for (Konto konto:konten)
      {
        Logger.info("merging konto " + konto.getKontonummer());
        // Wir checken, ob's das Konto schon gibt
        boolean found = false;
        Logger.info("  checking if allready exists");

        while (existing.hasNext())
        {
          Konto check = (Konto) existing.next();
        
          // BLZ stimmt nicht ueberein
          if (!check.getBLZ().equals(konto.getBLZ()))
            continue;

          // Kontonummer stimmt nicht ueberein.
          if (!check.getKontonummer().equals(konto.getKontonummer()))
            continue;
          
          // Stimmen Passports ueberein?
          String pp = check.getPassportClass();
          if (pp == null || !pp.equals(konto.getPassportClass()))
            continue;
          
          
          // BUGZILLA 338: Checken, ob Bezeichnung (=Type) uebereinstimmt
          // Bezeichnung ist optional - wir checken nur, wenn auf beiden
          // Seiten ein Name vorhanden ist und sie sich unterscheiden
          String localType = check.getBezeichnung();
          String newType   = konto.getBezeichnung();
          boolean haveName = (localType != null && localType.length() > 0 &&
                             newType != null && newType.length() > 0);

          // Die Konten gelten bereits als identisch, wenn
          // eines von beiden keine Bezeichnung hat (dann genuegen
          // die o.g. Kriterien). Andernfalls muessen die Bezeichnungen
          // uebereinstimmen.
          if (!haveName || newType.equals(localType))
          {
            found = true;
            Logger.info("  konto exists, skipping");
            skipped++;
          }
        }
        
        existing.begin();
        if (!found)
        {
          // Konto neu anlegen
          Logger.info("saving new konto");
          try {
            konto.store();
            created++;
            Logger.info("konto saved successfully");
          }
          catch (Exception e)
          {
            // Wenn ein Konto fehlschlaegt, soll nicht gleich der ganze Vorgang abbrechen
            Logger.error("error while storing konto",e);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anlegen des Kontos {0}: {1}", new String[]{konto.getLongName(),e.getMessage()}),StatusBarMessage.TYPE_ERROR));
          }
        }
      }

      String[] values = new String[] {Integer.toString(created),Integer.toString(skipped)};
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Konten erfolgreich abgeglichen. Angelegt: {0}, Übersprungen: {1}",values),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      Logger.error("error while merging accounts",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anlegen der Konten: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

}



/**********************************************************************
 * $Log: KontoMerge.java,v $
 * Revision 1.1  2010/09/29 23:43:34  willuhn
 * @N Automatisches Abgleichen und Anlegen von Konten aus KontoFetchFromPassport in KontoMerge verschoben
 * @N Konten automatisch (mit Rueckfrage) anlegen, wenn das Testen der HBCI-Konfiguration erfolgreich war
 * @N Config-Test jetzt auch bei Schluesseldatei
 * @B in PassportHandleImpl#getKonten() wurder der Converter-Funktion seit jeher die falsche Passport-Klasse uebergeben. Da gehoerte nicht das Interface hin sondern die Impl
 *
 **********************************************************************/