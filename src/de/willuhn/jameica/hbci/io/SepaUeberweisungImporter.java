/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
import org.kapott.hbci.sepa.SepaVersion;
import org.kapott.hbci.sepa.SepaVersion.Type;

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
    
    u.setBetrag(this.parseValue(prop.getProperty(ISEPAParser.Names.VALUE.getValue())));

    String date = StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.DATE.getValue()));
    
    if (date != null && !SepaUtil.DATE_UNDEFINED.equals(date))
    {
      u.setTermin(ISO_DATE.parse(date));
      if(ISO_DATE.parse(date).after(new Date())) //Only TerminUeberweiung when date later
        u.setTerminUeberweisung(true);
    }

    u.setEndtoEndId(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.ENDTOENDID.getValue())));
    u.setPmtInfId(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.PMTINFID.getValue())));
    u.setPurposeCode(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.PURPOSECODE.getValue())));

    u.store();
    Application.getMessagingFactory().sendMessage(new ImportMessage(u));
  }

  @Override
  Class[] getSupportedObjectTypes()
  {
    return new Class[]{AuslandsUeberweisung.class};
  }

  @Override
  Type getSupportedPainType()
  {
    return SepaVersion.Type.PAIN_001;
  }

}
