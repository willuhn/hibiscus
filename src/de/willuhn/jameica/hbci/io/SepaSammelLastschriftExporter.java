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

import java.io.OutputStream;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.GV.SepaUtil;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.sepa.SepaVersion.Type;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaLastType;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ProgressMonitor;

/**
 * Exporter fuer SEPA-Sammel-Lastschriften.
 */
public class SepaSammelLastschriftExporter extends AbstractSepaExporter
{
  private final static DateFormat ISO_DATE = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * @see de.willuhn.jameica.hbci.io.AbstractSepaExporter#setup(java.lang.Object[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  @Override
  void setup(Object[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws Exception
  {
    // Wenn die Auftraege unterschiedliche Sequenz-Typen haben, koennen sie nicht in einer Datei zusammengefasst werden.
    Set<String> conflict = new HashSet<String>();
    for (Object o:objects)
    {
      conflict.add(this.createKey((SepaSammelLastschrift) o));
      if (conflict.size() > 1)
        break;
    }
    
    if (conflict.size() > 1)
    {
      String txt = i18n.tr("Die Lastschriften enthalten unterschiedliche Lastschrift-Arten, Sequenz-Typen oder Zieltermine.\n" +
      		                 "Sie können daher nicht in eine einzelne SEPA XML-Datei exportiert werden.");
      Application.getCallback().notifyUser(txt);
      throw new OperationCanceledException("conflicting sequencetype, targetdate or type");
    }
    
    super.setup(objects, format, os, monitor);
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractSepaExporter#exportObject(java.lang.Object, int, de.willuhn.jameica.hbci.io.AbstractSepaExporter.JobContext)
   */
  @Override
  protected void exportObject(Object o, int idx, JobContext ctx) throws Exception
  {
    Properties props = ctx.props;
    
    SepaSammelLastschrift u = (SepaSammelLastschrift) o;
    Konto k = u.getKonto();
   
    // Wir nehmen die globalen Properties von der ersten Lastschrift
    if (idx == 0)
    {
      SepaLastType type = u.getType();
      if (type == null)
        type = SepaLastType.DEFAULT;

      props.setProperty("pmtinfid",     StringUtils.trimToEmpty(u.getPmtInfId()));
      props.setProperty("sequencetype", u.getSequenceType().name());
      props.setProperty("targetdate",   u.getTargetDate() != null ? ISO_DATE.format(u.getTargetDate()) : SepaUtil.DATE_UNDEFINED);
      props.setProperty("type",         type.name());
      
      String batchbook = MetaKey.SEPA_BATCHBOOK.get(u);
      if (batchbook != null)
        props.setProperty("batchbook", batchbook);
    }
    
    Integer count = (Integer) ctx.meta.get("count");
    if (count == null)
      count = 0;

    List<SepaSammelLastBuchung> buchungen = u.getBuchungen();
    for (SepaSammelLastBuchung b : buchungen)
    {
      props.setProperty(SepaUtil.insertIndex("dst.bic",count),      StringUtils.trimToEmpty(b.getGegenkontoBLZ()));
      props.setProperty(SepaUtil.insertIndex("dst.iban",count),     StringUtils.trimToEmpty(b.getGegenkontoNummer()));
      props.setProperty(SepaUtil.insertIndex("dst.name",count),     StringUtils.trimToEmpty(b.getGegenkontoName()));
      props.setProperty(SepaUtil.insertIndex("btg.value",count),    HBCIUtils.value2String(b.getBetrag()));
      props.setProperty(SepaUtil.insertIndex("btg.curr",count),     k.getWaehrung() != null ? k.getWaehrung() : HBCIProperties.CURRENCY_DEFAULT_DE);
      props.setProperty(SepaUtil.insertIndex("usage",count),        StringUtils.trimToEmpty(b.getZweck()));
      props.setProperty(SepaUtil.insertIndex("endtoendid",count),   StringUtils.trimToEmpty(b.getEndtoEndId()));
      
      props.setProperty(SepaUtil.insertIndex("creditorid",count),   StringUtils.trimToEmpty(b.getCreditorId()));
      props.setProperty(SepaUtil.insertIndex("mandateid",count),    StringUtils.trimToEmpty(b.getMandateId()));
      props.setProperty(SepaUtil.insertIndex("manddateofsig",count),ISO_DATE.format(b.getSignatureDate()));
      props.setProperty(SepaUtil.insertIndex("purposecode",count),  StringUtils.trimToEmpty(b.getPurposeCode()));
      count++;
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
    return Type.PAIN_008;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractSepaExporter#getJobName()
   */
  @Override
  protected String getJobName()
  {
    // Hier muss immer CORE angegeben sein, weil es nur einen Generator fuer CORE/COR1/B2B gibt.
    // Im Property "type" ist aber der korrekte Typ hinterlegt.
    return SepaLastType.CORE.getJobName();
  }
  
  /**
   * @see de.willuhn.jameica.hbci.io.AbstractExporter#getSupportedObjectTypes()
   */
  @Override
  Class[] getSupportedObjectTypes()
  {
    return new Class[]{SepaSammelLastschrift.class};
  }
  
  /**
   * Generiert einen Lookup-Key fuer den Auftrag um zu checken, ob er in die SEPA XML-Datei passt.
   * @param l der Auftrag.
   * @return der Lookup-Key.
   * @throws RemoteException
   */
  private String createKey(SepaSammelLastschrift l) throws RemoteException
  {
    StringBuffer sb = new StringBuffer();
    sb.append(l.getSequenceType().name() + "-");
    
    SepaLastType type = l.getType();
    if (type == null)
      type = SepaLastType.DEFAULT;
    sb.append(type.name() + "-");
    
    Date target = l.getTargetDate();
    if (target != null)
      sb.append(HBCI.DATEFORMAT.format(target) + "-");
    
    return sb.toString();
  }

}


