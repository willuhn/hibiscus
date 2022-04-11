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

import java.util.Date;
import java.util.List;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.io.ser.DateSerializer;
import de.willuhn.jameica.hbci.io.ser.DefaultSerializer;
import de.willuhn.jameica.hbci.io.ser.ExtendedUsageSerializer;
import de.willuhn.jameica.hbci.io.ser.Serializer;
import de.willuhn.jameica.hbci.io.ser.UmsatzTypSerializer;
import de.willuhn.jameica.hbci.io.ser.ValueSerializer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung des CSV-Formats fuer den Import von Kontoauszuegen.
 */
public class UmsatzFormat implements Format<Umsatz>
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private ImportListener listener        = null;
  private Profile profile                = null;
  
  @Override
  public synchronized Profile getDefaultProfile()
  {
    if (this.profile == null)
    {
      this.profile = new Profile();
      this.profile.setName(i18n.tr("Default-Profil"));
      this.profile.setSkipLines(1);
      this.profile.setSystem(true);
      
      Serializer ts = new DefaultSerializer();
      Serializer vs = new ValueSerializer();
      Serializer ds = new DateSerializer();
      
      List<Column> list = this.profile.getColumns();
      int i = 4; // wir fangen bei Spalte 4 an, weil die ersten 3 Spalten von Hibiscus
                 // zwar exportiert werden (#, Kontonummer, BLZ, Name des eigenen Kontos),
                 // diese Information beim Import aber nicht benoetigt wird (kriegen
                 // wir ueber den Kontext. Man koennte natuerlich auch bei Spalte
                 // 0 anfangen, wir wollen ja aber, dass wenigstens die von Hibiscus
                 // erzeugten CSV-Dateien 1:1 wieder importiert werden koennen, ohne
                 // dass der User das Profil anpassen muss.
      
      list.add(new Column("gegenkontoNummer",i18n.tr("Gegenkonto IBAN"),i++,ts));
      list.add(new Column("gegenkontoBLZ",   i18n.tr("Gegenkonto BIC"),i++,ts));
      list.add(new Column("gegenkontoName",  i18n.tr("Gegenkonto"),i++,ts));
      list.add(new Column("betrag",i18n.tr("Betrag"),i++,vs));
      list.add(new Column("valuta",i18n.tr("Valuta"),i++,ds));
      list.add(new Column("datum",i18n.tr("Datum"),i++,ds));
      list.add(new Column("zweck",i18n.tr("Verwendungszweck"),i++,ts));
      list.add(new Column("zweck2",i18n.tr("Verwendungszweck 2"),i++,ts));
      list.add(new Column("saldo",i18n.tr("Saldo"),i++,vs));
      list.add(new Column("primanota",i18n.tr("Primanota"),i++,ts));
      list.add(new Column("customerRef",i18n.tr("Kundenreferenz"),i++,ts));
      list.add(new Column("umsatzTyp",i18n.tr("Kategorie"),i++,new UmsatzTypSerializer()));
      list.add(new Column("kommentar",i18n.tr("Notiz"),i++,ts));
      list.add(new Column("weitereVerwendungszwecke",i18n.tr("Weitere Verwendungszwecke"),i++,new ExtendedUsageSerializer()));
      list.add(new Column("art",i18n.tr("Art der Buchung"),i++,ts));
      list.add(new Column("endToEndId",i18n.tr("End-to-End ID"),i++,ts));
    
    }
    return this.profile;
  }

  @Override
  public Class<Umsatz> getType()
  {
    return Umsatz.class;
  }

  @Override
  public ImportListener getImportListener()
  {
    if (this.listener == null)
    {
      this.listener = new ImportListener(){
        
        @Override
        public void beforeStore(ImportEvent event)
        {
          try
          {
            Object data = event.data;
            if (data == null || !(data instanceof Umsatz))
              return;

            Umsatz u = (Umsatz) data;

            // Hibiscus verlangt, dass Valuta UND Buchungsdatum vorhanden sind.
            // Oft ist es aber so, dass nur eines der beiden Fehler in der CSV-Datei
            // existiert. Da beide Werte meistens ohnehin identisch sind, uebernehmen
            // wir den einen jeweils in den anderen, falls einer von beiden fehlt.
            Date dd = u.getDatum();
            Date dv = u.getValuta();
            if (dd == null) u.setDatum(dv);
            if (dv == null) u.setValuta(dd);
            

            // Wir fuegen hier noch das Konto ein, falls es angegeben ist
            Object context = event.context;
            if (context != null && (context instanceof Konto))
              u.setKonto((Konto)context);
          }
          catch (Exception e)
          {
            Logger.error("error while assigning account",e);
          }
        }
        
      };
    }
    return this.listener;
  }
}
