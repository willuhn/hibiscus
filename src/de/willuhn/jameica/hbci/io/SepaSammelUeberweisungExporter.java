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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.GV.SepaUtil;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.sepa.SepaVersion.Type;

import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisungBuchung;

/**
 * Exporter fuer SEPA-Sammel-Ueberweisungen.
 */
public class SepaSammelUeberweisungExporter extends AbstractSepaExporter
{
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractSepaExporter#exportObject(java.lang.Object, int, java.util.Properties)
   */
  @Override
  protected void exportObject(Object o, int idx, JobContext ctx) throws Exception
  {
    Properties props = ctx.props;

    SepaSammelUeberweisung u = (SepaSammelUeberweisung) o;
    Konto k = u.getKonto();

    // Wir nehmen das Flag von der ersten Sammel-Ueberweisung
    if (idx == 0)
    {
      String batchbook = MetaKey.SEPA_BATCHBOOK.get(u);
      if (batchbook != null)
        props.setProperty("batchbook", batchbook);
      
      props.setProperty("pmtinfid", StringUtils.trimToEmpty(u.getPmtInfId()));

    }
    
    Integer count = (Integer) ctx.meta.get("count");
    if (count == null)
      count = 0;
    
    List<SepaSammelUeberweisungBuchung> buchungen = u.getBuchungen();
    for (SepaSammelUeberweisungBuchung b : buchungen)
    {
      props.setProperty(SepaUtil.insertIndex("dst.bic",count),      StringUtils.trimToEmpty(b.getGegenkontoBLZ()));
      props.setProperty(SepaUtil.insertIndex("dst.iban",count),     StringUtils.trimToEmpty(b.getGegenkontoNummer()));
      props.setProperty(SepaUtil.insertIndex("dst.name",count),     StringUtils.trimToEmpty(b.getGegenkontoName()));
      props.setProperty(SepaUtil.insertIndex("btg.value",count),    HBCIUtils.value2String(b.getBetrag()));
      props.setProperty(SepaUtil.insertIndex("btg.curr",count),     k.getWaehrung() != null ? k.getWaehrung() : HBCIProperties.CURRENCY_DEFAULT_DE);
      props.setProperty(SepaUtil.insertIndex("usage",count),        StringUtils.trimToEmpty(b.getZweck()));
      props.setProperty(SepaUtil.insertIndex("endtoendid",count),   StringUtils.trimToEmpty(b.getEndtoEndId()));
      props.setProperty(SepaUtil.insertIndex("purposecode",count),  StringUtils.trimToEmpty(b.getPurposeCode()));
      count++;
    }
    
    if (u.isTerminUeberweisung())
    {
      SimpleDateFormat df = new SimpleDateFormat(SepaUtil.DATE_FORMAT);
      String date = StringUtils.trimToNull(df.format(u.getTermin()));
      if (date != null)
        props.setProperty("date",date);
    }
    
    // Weil wir eine Liste von Auftraegen mit Buchungen haben, muessen wir den Zaehler selbst zaehlen
    ctx.meta.put("count",count);

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
    return new Class[]{SepaSammelUeberweisung.class};
  }
}


