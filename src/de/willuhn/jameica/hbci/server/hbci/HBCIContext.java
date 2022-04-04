/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.hbci;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.SynchronizeOptions;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Hilfsklasse zum Erzeugen und Parsen der External-ID aus HBCI4Java. 
 */
public class HBCIContext
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Versucht den zugehoerigen Auftrag zu laden.
   * @param externalId die External-ID.
   * @return der Auftrag oder NULL, wenn er nicht ermittelbar war.
   */
  public static HibiscusDBObject unserialize(String externalId)
  {
    externalId = StringUtils.trimToNull(externalId);
    if (externalId == null)
      return null;
    
    try
    {
      // Ist das Typ und ID?
      int i = externalId.indexOf(":");
      if (i <= 0)
        return null;
      
      String className = externalId.substring(0,i);
      String id = externalId.substring(i+1);
      // OK, den ersten Teil nehmen wir als Klasse an, den zweiten als ID.
      HBCIDBService service = Settings.getDBService();
      ClassLoader loader = Application.getPluginLoader().getManifest(HBCI.class).getClassLoader();
      DBObject o = service.createObject((Class<DBObject>)loader.loadClass(className),id);
      if (o instanceof HibiscusDBObject)
        return (HibiscusDBObject) o;
    }
    catch (Exception e)
    {
      Logger.error("unable to unserialize transfer for external id: " + externalId,e);
    }
    
    return null;
  }
  
  /**
   * Serialisiert den Auftrag in eine External-ID.
   * @param context der Auftrag.
   * @return die External-ID.
   */
  public static String serialize(HibiscusDBObject context)
  {
    try
    {
      if (context == null || context.isNewObject())
        return null;
      
      return context.getClass().getName() + ":" + context.getID();
    }
    catch (Exception e)
    {
      Logger.error("unable to serialize transfer",e);
    }
    
    return null;
  }
  
  /**
   * Liefert einen lesbaren Text fuer das Objekt.
   * @param object das Objekt.
   * @return der Text oder den Text "Unbekanter Auftrag, wenn keiner ermittelbar ist.
   */
  public static String toString(Object object)
  {
    try
    {
      if (object instanceof AuslandsUeberweisung)
      {
        AuslandsUeberweisung ueb = (AuslandsUeberweisung) object;
        Konto k = ueb.getKonto();
        
        if (ueb.isTerminUeberweisung())
          return i18n.tr("{0}: ({1}) {2} {3} per {4} an {5} ({6}) überweisen",k.getLongName(),ueb.getZweck(),HBCI.DECIMALFORMAT.format(ueb.getBetrag()),k.getWaehrung(),HBCI.DATEFORMAT.format(ueb.getTermin()),ueb.getGegenkontoName(),HBCIProperties.formatIban(ueb.getGegenkontoNummer()));

        if (ueb.isUmbuchung())
          return i18n.tr("{0}: ({1}) {2} {3} an {4} ({5}) überweisen (Umbuchung)",k.getLongName(),ueb.getZweck(),HBCI.DECIMALFORMAT.format(ueb.getBetrag()),k.getWaehrung(),ueb.getGegenkontoName(),HBCIProperties.formatIban(ueb.getGegenkontoNummer()));
        if (ueb.isInstantPayment())
          return i18n.tr("{0}: ({1}) {2} {3} an {4} ({5}) überweisen (Echtzeitüberweisung)",k.getLongName(),ueb.getZweck(),HBCI.DECIMALFORMAT.format(ueb.getBetrag()),k.getWaehrung(),ueb.getGegenkontoName(),HBCIProperties.formatIban(ueb.getGegenkontoNummer()));

        return i18n.tr("{0}: ({1}) {2} {3} an {4} ({5}) überweisen",k.getLongName(),ueb.getZweck(),HBCI.DECIMALFORMAT.format(ueb.getBetrag()),k.getWaehrung(),ueb.getGegenkontoName(),HBCIProperties.formatIban(ueb.getGegenkontoNummer()));
      }
      
      if (object instanceof SepaSammelUeberweisung)
      {
        SepaSammelUeberweisung r = (SepaSammelUeberweisung) object;
        Konto k = r.getKonto();
        
        if (r.isTerminUeberweisung())
          return i18n.tr("{0}: ({1}) {2} {3} per {4} als SEPA-Sammelterminüberweisung absenden",k.getLongName(),r.getBezeichnung(),HBCI.DECIMALFORMAT.format(r.getSumme()),k.getWaehrung(),HBCI.DATEFORMAT.format(r.getTermin()));
        
        return i18n.tr("{0}: ({1}) {2} {3} als SEPA-Sammelüberweisung absenden",k.getLongName(),r.getBezeichnung(),HBCI.DECIMALFORMAT.format(r.getSumme()),k.getWaehrung());
      }

      if (object instanceof SepaDauerauftrag)
      {
        SepaDauerauftrag dauer = (SepaDauerauftrag) object;
        Konto k = dauer.getKonto();
        
        return i18n.tr("{0}: ({1}) {2} {3} an {4} ({5}), Turnus: {6}",k.getLongName(),dauer.getZweck(),HBCI.DECIMALFORMAT.format(dauer.getBetrag()),k.getWaehrung(),dauer.getGegenkontoName(),HBCIProperties.formatIban(dauer.getGegenkontoNummer()),dauer.getTurnus().getBezeichnung());
      }
      
      if (object instanceof SepaLastschrift)
      {
        SepaLastschrift last = (SepaLastschrift) object;
        Konto k = last.getKonto();
        return i18n.tr("{0}: ({1}) {2} {3} von {4} ({5}) einziehen",k.getLongName(),last.getZweck(),HBCI.DECIMALFORMAT.format(last.getBetrag()),k.getWaehrung(),last.getGegenkontoName(),HBCIProperties.formatIban(last.getGegenkontoNummer()));
      }
      
      if (object instanceof SepaSammelLastschrift)
      {
        SepaSammelLastschrift last = (SepaSammelLastschrift) object;
        Konto k = last.getKonto();
        return i18n.tr("{0}: ({1}) {2} {3} als SEPA-Sammellastschrift einziehen",k.getLongName(),last.getBezeichnung(),HBCI.DECIMALFORMAT.format(last.getSumme()),k.getWaehrung());
      }
      
      if (object instanceof Konto)
      {
        Konto k = (Konto) object;
        SynchronizeOptions o = new SynchronizeOptions(k);
        
        String s = "{0}: ";
        
        if (o.getSyncKontoauszuege())
          s += "Umsätze";
        if (o.getSyncSaldo())
        {
          if (o.getSyncKontoauszuege())
            s += "/";
          s += "Salden";
        }
        s += " abrufen";
        return i18n.tr(s,k.getLongName());
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to stringify object",e);
    }
    
    return i18n.tr("Unbekannter Auftrag");
  }
}


