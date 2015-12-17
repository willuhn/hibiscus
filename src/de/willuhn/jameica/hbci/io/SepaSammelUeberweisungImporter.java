/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.GV.SepaUtil;
import org.kapott.hbci.GV.parsers.ISEPAParser;
import org.kapott.hbci.sepa.PainVersion;

import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisungBuchung;
import de.willuhn.jameica.system.Application;

/**
 * Importer fuer SEPA-Sammelueberweisungen.
 */
public class SepaSammelUeberweisungImporter extends AbstractSepaImporter
{
  private final static DateFormat ISO_DATE = new SimpleDateFormat(SepaUtil.DATE_FORMAT);

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractImporter#importObject(java.lang.Object, int)
   */
  @Override
  void importObject(Object o, int idx, Map ctx) throws Exception
  {
    DBService service = de.willuhn.jameica.hbci.Settings.getDBService();

    Properties prop = (Properties) o;

    SepaSammelUeberweisung ueb = (SepaSammelUeberweisung) ctx.get("ueb");
    
    // erster Datensatz. Wir erstellen den Sammelauftrag
    if (ueb == null)
    {
      ueb = (SepaSammelUeberweisung) service.createObject(SepaSammelUeberweisung.class,null);
      ueb.setBezeichnung(i18n.tr("SEPA-Sammelüberweisung vom {0}",HBCI.LONGDATEFORMAT.format(new Date())));
      ueb.setKonto(this.findKonto(prop.getProperty(ISEPAParser.Names.SRC_IBAN.getValue())));
      ueb.setPmtInfId(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.PMTINFID.getValue())));
      
      String date = StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.DATE.getValue()));
      
      if (date != null && !SepaUtil.DATE_UNDEFINED.equals(date))
        ueb.setTermin(ISO_DATE.parse(date));
      
      ueb.store();
      ctx.put("ueb",ueb); // und im Context speichern
      Application.getMessagingFactory().sendMessage(new ImportMessage(ueb));
    }

    SepaSammelUeberweisungBuchung u = ueb.createBuchung();
    u.setGegenkontoName(prop.getProperty(ISEPAParser.Names.DST_NAME.getValue()));
    u.setGegenkontoNummer(prop.getProperty(ISEPAParser.Names.DST_IBAN.getValue()));
    u.setGegenkontoBLZ(prop.getProperty(ISEPAParser.Names.DST_BIC.getValue()));
    u.setZweck(prop.getProperty(ISEPAParser.Names.USAGE.getValue()));
    u.setBetrag(Double.valueOf(prop.getProperty(ISEPAParser.Names.VALUE.getValue())));
    u.setEndtoEndId(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.ENDTOENDID.getValue())));
    u.setPurposeCode(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.PURPOSECODE.getValue())));

    u.store();
    Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(ueb));

  }

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractImporter#getSupportedObjectTypes()
   */
  @Override
  Class[] getSupportedObjectTypes()
  {
    return new Class[]{SepaSammelUeberweisung.class};
  }

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractSepaImporter#getSupportedPainTypes()
   */
  @Override
  PainVersion.Type[] getSupportedPainTypes()
  {
    return new PainVersion.Type[]{PainVersion.Type.PAIN_001};
  }

}
