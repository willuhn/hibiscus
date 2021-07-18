/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io.csv;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.io.ser.DateSerializer;
import de.willuhn.jameica.hbci.io.ser.DefaultSerializer;
import de.willuhn.jameica.hbci.io.ser.ExtendedUsageSerializer;
import de.willuhn.jameica.hbci.io.ser.Serializer;
import de.willuhn.jameica.hbci.io.ser.ValueSerializer;
import de.willuhn.jameica.hbci.rmi.BaseUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung des CSV-Formats fuer den Import von Ueberweisungen und Lastschriften.
 * @param <T> Konkreter Typ - also Ueberweisung oder Lastschrift
 */
public abstract class AbstractBaseUeberweisungFormat<T extends BaseUeberweisung> implements Format<T>
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private ImportListener listener        = null;
  private Profile profile                = null;
  
  private Konto konto = null;
  
  /**
   * @see de.willuhn.jameica.hbci.io.csv.Format#getDefaultProfile()
   */
  public synchronized Profile getDefaultProfile()
  {
    if (this.profile == null)
    {
      this.profile = new Profile();
      this.profile.setName(i18n.tr("Default-Profil"));
      this.profile.setSkipLines(1);
      this.profile.setSystem(true);
      
      Serializer ts = new DefaultSerializer();
      
      List<Column> list = this.profile.getColumns();
      int i = 0;

      list.add(new Column("_konto",i18n.tr("Eigene Kontonummer"),i++,ts));
      list.add(new Column("_blz",i18n.tr("Eigene BLZ"),i++,ts));
      i++; // Das ist im Hibiscus CSV-Export der Name des eigenen Kontos. Die brauchen wir nicht
      list.add(new Column("gegenkontoNummer",i18n.tr("Gegenkonto"),i++,ts));
      list.add(new Column("gegenkontoBLZ",   i18n.tr("Gegenkonto BLZ"),i++,ts));
      list.add(new Column("gegenkontoName",  i18n.tr("Gegenkonto Inhaber"),i++,ts));
      list.add(new Column("betrag",i18n.tr("Betrag"),i++,new ValueSerializer()));
      list.add(new Column("termin",i18n.tr("Erinnerungstermin"),i++,new DateSerializer()));
      list.add(new Column("zweck",i18n.tr("Verwendungszweck"),i++,ts));
      list.add(new Column("zweck2",i18n.tr("Verwendungszweck 2"),i++,ts));
      list.add(new Column("weitereVerwendungszwecke",i18n.tr("Weitere Verwendungszwecke"),i++,new ExtendedUsageSerializer()));
    }
    return this.profile;
  }

  // Das geht leider (noch) nicht ;)
  //  /**
  //   * @see de.willuhn.jameica.hbci.io.csv.Format#getType()
  //   */
  //  public Class<T> getType()
  //  {
  //    return T.class;
  //  }

  /**
   * @see de.willuhn.jameica.hbci.io.csv.Format#getImportListener()
   */
  public ImportListener getImportListener()
  {
    if (this.listener == null)
    {
      this.listener = new ImportListener()
      {
        
        /**
         * @see de.willuhn.jameica.hbci.io.csv.ImportListener#beforeSet(de.willuhn.jameica.hbci.io.csv.ImportEvent)
         */
        public void beforeSet(ImportEvent event) throws OperationCanceledException
        {
          Map<String,Object> values = (Map<String,Object>) event.data;
          
          // Wir suchen uns hier unsere Kontonummer und BLZ zusammen und
          // suchen das zugehoerige Konto. Die Dummy-Properties entfernen
          // wir bei der Gelegenheit wieder.
          String konto = (String) values.remove("_konto");
          String blz   = (String) values.remove("_blz");
          
          // Wenn es hier zu einem Fehler kommt, ist das nicht weiter tragisch.
          // Dann wird der User in beforeStore() nach dem zu verwendenden Konto
          // gefragt.
          try
          {
            Konto k = KontoUtil.find(konto,blz);
            if (k != null)
              values.put("konto",k);
          }
          catch (RemoteException re)
          {
            Logger.error("error while search for account",re);
          }
        }

        /**
         * @see de.willuhn.jameica.hbci.io.csv.ImportListener#beforeStore(de.willuhn.jameica.hbci.io.csv.ImportEvent)
         */
        public void beforeStore(ImportEvent event) throws OperationCanceledException
        {
          try
          {
            Object data = event.data;
            if (data == null || !(data instanceof BaseUeberweisung))
              return;

            BaseUeberweisung u = (BaseUeberweisung) data;

            if (u.getKonto() == null)
            {
              if (konto == null)
              {
                // Wir haben noch kein Konto - dann den User fragen
                KontoAuswahlDialog d = new KontoAuswahlDialog(KontoAuswahlDialog.POSITION_CENTER);
                d.setText(i18n.tr("Bitte wählen Sie das zu verwendende Konto aus."));
                konto = (Konto) d.open();
              }
              u.setKonto(konto);
            }
          }
          catch (OperationCanceledException oce)
          {
            throw new OperationCanceledException(i18n.tr("Kein Konto ausgewählt"));
          }
          catch (Exception e)
          {
            Logger.write(Level.WARN,"error while assigning account",e);
          }
        }
        
      };
    }
    return this.listener;
  }
}
