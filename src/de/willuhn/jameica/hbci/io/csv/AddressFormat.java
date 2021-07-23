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

import java.util.List;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.io.ser.DefaultSerializer;
import de.willuhn.jameica.hbci.io.ser.Serializer;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung des CSV-Formats fuer den Adressbuch-Import.
 */
public class AddressFormat implements Format<HibiscusAddress>
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
      
      Serializer s = new DefaultSerializer();
      List<Column> list = this.profile.getColumns();
      int i = 0;
      list.add(new Column("name",i18n.tr("Name des Kontoinhabers"),i++,s));
      list.add(new Column("kontonummer",i18n.tr("Kontonummer"),i++,s));
      list.add(new Column("blz",i18n.tr("Bankleitzahl"),i++,s));
      list.add(new Column("iban",i18n.tr("IBAN"),i++,s));
      list.add(new Column("bic",i18n.tr("BIC"),i++,s));
      list.add(new Column("kommentar",i18n.tr("Notiz"),i++,s));
    }
    return this.profile;
  }

  @Override
  public Class<HibiscusAddress> getType()
  {
    return HibiscusAddress.class;
  }

  @Override
  public ImportListener getImportListener()
  {
    if (this.listener == null)
    {
      this.listener = new ImportListener(){
        
        private AddressbookService addressbook = null;

        @Override
        public void beforeStore(ImportEvent event) throws OperationCanceledException
        {
          try
          {
            Object data = event.data;
            if (data == null || !(data instanceof HibiscusAddress))
              return;
            
            if (this.addressbook == null)
              this.addressbook = (AddressbookService) Application.getServiceFactory().lookup(HBCI.class,"addressbook");

            HibiscusAddress t = (HibiscusAddress) data; 
            if (this.addressbook.contains(t) != null)
              throw new OperationCanceledException(i18n.tr("Adresse (Kto {0}, BLZ {1}) existiert bereits, überspringe Zeile", new String[]{t.getKontonummer(),t.getBlz()}));
          }
          catch (OperationCanceledException oce)
          {
            throw oce;
          }
          catch (Exception e)
          {
            Logger.write(Level.WARN,"error while checking address",e);
          }
        }
        
      };
    }
    return this.listener;
  }
}



/**********************************************************************
 * $Log: AddressFormat.java,v $
 * Revision 1.3  2010/03/17 10:01:10  willuhn
 * @B grr, koennen die sich bei Eclipse mal bitte andere Klassennamen ausdenken? ;)
 *
 * Revision 1.2  2010/03/16 13:43:56  willuhn
 * @N CSV-Import von Ueberweisungen und Lastschriften
 * @N Versionierbarkeit von serialisierten CSV-Profilen
 *
 * Revision 1.1  2010/03/16 00:44:18  willuhn
 * @N Komplettes Redesign des CSV-Imports.
 *   - Kann nun erheblich einfacher auch fuer andere Datentypen (z.Bsp.Ueberweisungen) verwendet werden
 *   - Fehlertoleranter
 *   - Mehrfachzuordnung von Spalten (z.Bsp. bei erweitertem Verwendungszweck) moeglich
 *   - modulare Deserialisierung der Werte
 *   - CSV-Exports von Hibiscus koennen nun 1:1 auch wieder importiert werden (Import-Preset identisch mit Export-Format)
 *   - Import-Preset wird nun im XML-Format nach ~/.jameica/hibiscus/csv serialisiert. Damit wird es kuenftig moeglich sein,
 *     CSV-Import-Profile vorzukonfigurieren und anschliessend zu exportieren, um sie mit anderen Usern teilen zu koennen
 *
 **********************************************************************/