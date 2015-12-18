/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.GV.SepaUtil;
import org.kapott.hbci.GV.parsers.ISEPAParser;
import org.kapott.hbci.sepa.PainVersion;

import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.system.Application;

/**
 * Importer fuer SEPA-Einzelueberweisungen.
 */
public class SepaUeberweisungImporter extends AbstractSepaImporter
{
  private final static DateFormat ISO_DATE = new SimpleDateFormat(SepaUtil.DATE_FORMAT);

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractImporter#importObject(java.lang.Object, int, java.util.Map)
   */
  @Override
  void importObject(Object o, int idx, Map ctx) throws Exception
  {
    DBService service = de.willuhn.jameica.hbci.Settings.getDBService();

    Properties prop = (Properties) o;
    AuslandsUeberweisung u = (AuslandsUeberweisung) service.createObject(AuslandsUeberweisung.class,null);
    u.setKonto(this.findKonto(prop.getProperty(ISEPAParser.Names.SRC_IBAN.getValue())));
    u.setGegenkontoName(prop.getProperty(ISEPAParser.Names.DST_NAME.getValue()));
    u.setGegenkontoNummer(prop.getProperty(ISEPAParser.Names.DST_IBAN.getValue()));
    u.setGegenkontoBLZ(prop.getProperty(ISEPAParser.Names.DST_BIC.getValue()));
    u.setZweck(prop.getProperty(ISEPAParser.Names.USAGE.getValue()));
    
    u.setBetrag(Double.valueOf(prop.getProperty(ISEPAParser.Names.VALUE.getValue())));

    String date = StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.DATE.getValue()));
    
    if (date != null && !SepaUtil.DATE_UNDEFINED.equals(date))
      u.setTermin(ISO_DATE.parse(date));

    u.setEndtoEndId(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.ENDTOENDID.getValue())));
    u.setPmtInfId(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.PMTINFID.getValue())));
    u.setPurposeCode(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.PURPOSECODE.getValue())));

    u.store();
    Application.getMessagingFactory().sendMessage(new ImportMessage(u));
  }

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractImporter#getSupportedObjectTypes()
   */
  @Override
  Class[] getSupportedObjectTypes()
  {
    return new Class[]{AuslandsUeberweisung.class};
  }

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractSepaImporter#getSupportedPainType()
   */
  @Override
  PainVersion.Type getSupportedPainType()
  {
    return PainVersion.Type.PAIN_001;
  }

}
