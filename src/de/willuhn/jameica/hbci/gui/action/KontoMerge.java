/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null)
      return;

    final boolean kontoCheck = Settings.getKontoCheck();
    
    try
    {
      // Die Pruefung der BIC/IBAN muessen wir waehrend des Merge kurzfristig abschalten - fuer
      // den Fall, dass die Bank selbst ungueltige Konten liefert. Die koennten wir sonst nicht
      // anlegen. Tritt z.Bsp. bei Demo-Konten auf
      Settings.setKontoCheck(false);
      
      List<Konto> konten = new ArrayList<Konto>();

      if (context instanceof Konto)        konten.add((Konto) context);
      else if (context instanceof Konto[]) konten.addAll(Arrays.asList((Konto[])context));
      else if (context instanceof List)    konten.addAll((List)context);

      if (konten.size() == 0)
        return;

      DBIterator existing = Settings.getDBService().createList(Konto.class);

      int created   = 0;
      int skipped   = 0;
      int nosupport = 0;
      for (Konto konto:konten)
      {
        Logger.info("merging konto " + konto.getKontonummer());
        // Wir checken, ob's das Konto schon gibt
        boolean found = false;
        Logger.info("  checking if already exists");

        while (existing.hasNext())
        {
          Konto check = (Konto) existing.next();

          // BLZ stimmt nicht ueberein
          if (!check.getBLZ().equals(konto.getBLZ()))
            continue;

          // Kontonummer stimmt nicht ueberein.
          if (!check.getKontonummer().equals(konto.getKontonummer()))
            continue;
          
          // Kundenkennung stimmt nicht ueberein
          if (!check.getKundennummer().equals(konto.getKundennummer()))
            continue;

          // Stimmen Passports ueberein?
          String pp = check.getPassportClass();
          if (pp == null || !pp.equals(konto.getPassportClass()))
            continue;

          // Hier haben wir vorher den Namen des Kontos verglichen.
          // Jetzt pruefen wir stattdessen den Kontotyp.
          Integer localType = check.getAccountType();
          Integer newType   = konto.getAccountType();
          boolean haveType  = (localType != null && newType != null);

          // Die Konten gelten bereits als identisch, wenn eines von beiden
          // keinen Typ hat (dann genuegen die o.g. Kriterien).
          // Andernfalls muss der Typ uebereinstimmen.
          if (!haveType || newType.equals(localType))
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
          catch (ApplicationException ae)
          {
            Logger.warn("konto not supported: " + ae.getMessage());
            nosupport++;
          }
          catch (Exception e)
          {
            // Wenn ein Konto fehlschlaegt, soll nicht gleich der ganze Vorgang abbrechen
            Logger.error("error while storing konto",e);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anlegen des Kontos {0}: {1}", new String[]{konto.getLongName(),e.getMessage()}),StatusBarMessage.TYPE_ERROR));
          }
        }
      }

      String[] values = new String[] {Integer.toString(created),Integer.toString(skipped), Integer.toString(nosupport)};
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Konten abgeglichen. Angelegt: {0}, Übersprungen: {1}, nicht unterstützt: {2}",values),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      Logger.error("error while merging accounts",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anlegen der Konten: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
    finally
    {
      // Einstellung wieder zuruecksetzen
      Settings.setKontoCheck(kontoCheck);
    }
  }

}
