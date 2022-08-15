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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.BatchBookType;
import de.willuhn.jameica.hbci.rmi.SepaLastSequenceType;
import de.willuhn.jameica.hbci.rmi.SepaLastType;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.system.Application;

/**
 * Importer fuer SEPA-Sammellastschriften.
 */
public class SepaSammelLastschriftImporter extends AbstractSepaImporter
{
  private final static DateFormat ISO_DATE = new SimpleDateFormat(SepaUtil.DATE_FORMAT);

  @Override
  void importObject(Object o, int idx, Map ctx) throws Exception
  {
    DBService service = de.willuhn.jameica.hbci.Settings.getDBService();

    Properties prop = (Properties) o;

    SepaSammelLastschrift ueb = (SepaSammelLastschrift) ctx.get("ueb");
    
    // erster Datensatz. Wir erstellen den Sammelauftrag
    if (ueb == null)
    {
      ueb = (SepaSammelLastschrift) service.createObject(SepaSammelLastschrift.class,null);
      ueb.setSequenceType(SepaLastSequenceType.valueOf(prop.getProperty(ISEPAParser.Names.SEQUENCETYPE.getValue(),SepaLastSequenceType.FRST.name())));
      ueb.setType(SepaLastType.valueOf(prop.getProperty(ISEPAParser.Names.LAST_TYPE.getValue(),SepaLastType.CORE.name())));
      ueb.setBezeichnung(i18n.tr("{0} {1} vom {2}",ueb.getSequenceType().getDescription(),ueb.getType().getDescription(), HBCI.LONGDATEFORMAT.format(new Date())));
      ueb.setKonto(this.findKonto(prop.getProperty(ISEPAParser.Names.SRC_IBAN.getValue())));
      ueb.setPmtInfId(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.PMTINFID.getValue())));
      
      String date = StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.TARGETDATE.getValue()));
      
      if (date != null && !SepaUtil.DATE_UNDEFINED.equals(date))
        ueb.setTargetDate(ISO_DATE.parse(date));
      
      ueb.store();

      final BatchBookType batch = BatchBookType.byXmlValue(prop.getProperty(ISEPAParser.Names.BATCHBOOK.getValue()));
      MetaKey.SEPA_BATCHBOOK.set(ueb,batch != null ? batch.getValue() : null);

      ctx.put("ueb",ueb); // und im Context speichern
      Application.getMessagingFactory().sendMessage(new ImportMessage(ueb));
    }

    SepaSammelLastBuchung u = ueb.createBuchung();
    u.setGegenkontoName(prop.getProperty(ISEPAParser.Names.DST_NAME.getValue()));
    u.setGegenkontoNummer(prop.getProperty(ISEPAParser.Names.DST_IBAN.getValue()));
    u.setGegenkontoBLZ(prop.getProperty(ISEPAParser.Names.DST_BIC.getValue()));
    u.setZweck(prop.getProperty(ISEPAParser.Names.USAGE.getValue()));
    u.setBetrag(this.parseValue(prop.getProperty(ISEPAParser.Names.VALUE.getValue())));
    u.setEndtoEndId(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.ENDTOENDID.getValue())));
    u.setPurposeCode(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.PURPOSECODE.getValue())));

    u.setCreditorId(prop.getProperty(ISEPAParser.Names.CREDITORID.getValue()));
    u.setMandateId(prop.getProperty(ISEPAParser.Names.MANDATEID.getValue()));
    
    String mandDate = StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.MANDDATEOFSIG.getValue()));
    if (mandDate != null && !SepaUtil.DATE_UNDEFINED.equals(mandDate))
      u.setSignatureDate(ISO_DATE.parse(mandDate));

    u.store();
    Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(ueb));

  }

  @Override
  Class[] getSupportedObjectTypes()
  {
    return new Class[]{SepaSammelLastschrift.class};
  }

  @Override
  Type getSupportedPainType()
  {
    return SepaVersion.Type.PAIN_008;
  }

}
