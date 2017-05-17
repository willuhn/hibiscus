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
import de.willuhn.jameica.hbci.rmi.SepaLastSequenceType;
import de.willuhn.jameica.hbci.rmi.SepaLastType;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.system.Application;

/**
 * Importer fuer SEPA-Einzellastschriften.
 */
public class SepaLastschriftImporter extends AbstractSepaImporter
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
    SepaLastschrift u = (SepaLastschrift) service.createObject(SepaLastschrift.class,null);
    u.setKonto(this.findKonto(prop.getProperty(ISEPAParser.Names.SRC_IBAN.getValue())));
    u.setGegenkontoName(prop.getProperty(ISEPAParser.Names.DST_NAME.getValue()));
    u.setGegenkontoNummer(prop.getProperty(ISEPAParser.Names.DST_IBAN.getValue()));
    u.setGegenkontoBLZ(prop.getProperty(ISEPAParser.Names.DST_BIC.getValue()));
    u.setZweck(prop.getProperty(ISEPAParser.Names.USAGE.getValue()));
    
    u.setBetrag(this.parseValue(prop.getProperty(ISEPAParser.Names.VALUE.getValue())));

    String date = StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.TARGETDATE.getValue()));
    
    if (date != null && !SepaUtil.DATE_UNDEFINED.equals(date))
      u.setTargetDate(ISO_DATE.parse(date));

    u.setEndtoEndId(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.ENDTOENDID.getValue())));
    u.setPmtInfId(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.PMTINFID.getValue())));
    u.setPurposeCode(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.PURPOSECODE.getValue())));

    u.setCreditorId(prop.getProperty(ISEPAParser.Names.CREDITORID.getValue()));
    u.setMandateId(prop.getProperty(ISEPAParser.Names.MANDATEID.getValue()));
    
    String mandDate = StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.MANDDATEOFSIG.getValue()));
    if (mandDate != null && !SepaUtil.DATE_UNDEFINED.equals(mandDate))
      u.setSignatureDate(ISO_DATE.parse(mandDate));

    u.setSequenceType(SepaLastSequenceType.valueOf(prop.getProperty(ISEPAParser.Names.SEQUENCETYPE.getValue(),SepaLastSequenceType.FRST.name())));
    u.setType(SepaLastType.valueOf(prop.getProperty(ISEPAParser.Names.LAST_TYPE.getValue(),SepaLastType.CORE.name())));

    u.store();
    Application.getMessagingFactory().sendMessage(new ImportMessage(u));
  }

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractImporter#getSupportedObjectTypes()
   */
  @Override
  Class[] getSupportedObjectTypes()
  {
    return new Class[]{SepaLastschrift.class};
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractSepaImporter#getSupportedPainType()
   */
  @Override
  PainVersion.Type getSupportedPainType()
  {
    return PainVersion.Type.PAIN_008;
  }

}
