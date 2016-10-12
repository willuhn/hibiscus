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

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractImporter#importObject(java.lang.Object, int)
   */
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
      ueb.setSequenceType(SepaLastSequenceType.valueOf(prop.getProperty(ISEPAParser.Names.SEQUENCETYPE.getValue())));
      ueb.setType(SepaLastType.valueOf(prop.getProperty(ISEPAParser.Names.LAST_TYPE.getValue())));
      ueb.setBezeichnung(i18n.tr("{0} {1} vom {2}",ueb.getSequenceType().getDescription(),ueb.getType().getDescription(), HBCI.LONGDATEFORMAT.format(new Date())));
      ueb.setKonto(this.findKonto(prop.getProperty(ISEPAParser.Names.SRC_IBAN.getValue())));
      ueb.setPmtInfId(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.PMTINFID.getValue())));
      
      String date = StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.TARGETDATE.getValue()));
      
      if (date != null && !SepaUtil.DATE_UNDEFINED.equals(date))
        ueb.setTargetDate(ISO_DATE.parse(date));
      
      ueb.store();
      ctx.put("ueb",ueb); // und im Context speichern
      Application.getMessagingFactory().sendMessage(new ImportMessage(ueb));
    }

    SepaSammelLastBuchung u = ueb.createBuchung();
    u.setGegenkontoName(prop.getProperty(ISEPAParser.Names.DST_NAME.getValue()));
    u.setGegenkontoNummer(prop.getProperty(ISEPAParser.Names.DST_IBAN.getValue()));
    u.setGegenkontoBLZ(prop.getProperty(ISEPAParser.Names.DST_BIC.getValue()));
    u.setZweck(prop.getProperty(ISEPAParser.Names.USAGE.getValue()));
    u.setBetrag(Double.valueOf(prop.getProperty(ISEPAParser.Names.VALUE.getValue())));
    u.setEndtoEndId(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.ENDTOENDID.getValue())));
    u.setPurposeCode(StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.PURPOSECODE.getValue())));

    u.setCreditorId(prop.getProperty(ISEPAParser.Names.CREDITORID.getValue()));
    u.setMandateId(prop.getProperty(ISEPAParser.Names.MANDATEID.getValue()));
    
    String mandDate = StringUtils.trimToNull(prop.getProperty(ISEPAParser.Names.MANDDATEOFSIG.getValue()));
    if (mandDate != null && !SepaUtil.DATE_UNDEFINED.equals(mandDate))
      u.setSignatureDate(ISO_DATE.parse(mandDate));

    setBicFromIbanIfAbsent(u);

    u.store();
    Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(ueb));

  }

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractImporter#getSupportedObjectTypes()
   */
  @Override
  Class[] getSupportedObjectTypes()
  {
    return new Class[]{SepaSammelLastschrift.class};
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
