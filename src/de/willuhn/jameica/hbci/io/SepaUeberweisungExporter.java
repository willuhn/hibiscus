/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.GV.SepaUtil;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.sepa.PainVersion.Type;

import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Exporter fuer SEPA-Ueberweisungen.
 */
public class SepaUeberweisungExporter extends AbstractSepaExporter
{
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractSepaExporter#exportObject(java.lang.Object, int, java.util.Properties)
   */
  @Override
  protected void exportObject(Object o, int idx, JobContext ctx) throws Exception
  {
    Properties props = ctx.props;
    
    AuslandsUeberweisung u = (AuslandsUeberweisung) o;
    Konto k = u.getKonto();
    
    props.setProperty(SepaUtil.insertIndex("dst.bic",idx),    StringUtils.trimToEmpty(u.getGegenkontoBLZ()));
    props.setProperty(SepaUtil.insertIndex("dst.iban",idx),   StringUtils.trimToEmpty(u.getGegenkontoNummer()));
    props.setProperty(SepaUtil.insertIndex("dst.name",idx),   StringUtils.trimToEmpty(u.getGegenkontoName()));
    props.setProperty(SepaUtil.insertIndex("btg.value",idx),  HBCIUtils.value2String(u.getBetrag()));
    props.setProperty(SepaUtil.insertIndex("btg.curr",idx),   k.getWaehrung() != null ? k.getWaehrung() : HBCIProperties.CURRENCY_DEFAULT_DE);
    props.setProperty(SepaUtil.insertIndex("usage",idx),      StringUtils.trimToEmpty(u.getZweck()));
    props.setProperty(SepaUtil.insertIndex("endtoendid",idx), StringUtils.trimToEmpty(u.getEndtoEndId()));
    props.setProperty(SepaUtil.insertIndex("purposecode",idx),   StringUtils.trimToEmpty(u.getPurposeCode()));
    props.setProperty("pmtinfid",StringUtils.trimToEmpty(u.getPmtInfId()));
  }

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractSepaExporter#getPainType()
   */
  @Override
  protected Type getPainType()
  {
    return Type.PAIN_001;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractSepaExporter#getJobName()
   */
  @Override
  protected String getJobName()
  {
    return "UebSEPA";
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractExporter#getSupportedObjectTypes()
   */
  @Override
  Class[] getSupportedObjectTypes()
  {
    return new Class[]{AuslandsUeberweisung.class};
  }
}


