/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.OutputStream;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.GV.SepaUtil;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.sepa.PainVersion.Type;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaLastType;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ProgressMonitor;

/**
 * Exporter fuer SEPA-Lastschriften.
 */
public class SepaLastschriftExporter extends AbstractSepaExporter
{
  private final static DateFormat ISO_DATE = new SimpleDateFormat(SepaUtil.DATE_FORMAT);

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
      conflict.add(this.createKey((SepaLastschrift) o));
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
    
    SepaLastschrift u = (SepaLastschrift) o;
    Konto k = u.getKonto();
   
    // Wir nehmen die globalen Properties von der ersten Lastschrift
    if (idx == 0)
    {
      SepaLastType type = u.getType();
      if (type == null)
        type = SepaLastType.DEFAULT;

      props.setProperty("sequencetype", u.getSequenceType().name());
      props.setProperty("targetdate",   u.getTargetDate() != null ? ISO_DATE.format(u.getTargetDate()) : SepaUtil.DATE_UNDEFINED);
      props.setProperty("type",         type.name());
    }

    props.setProperty(SepaUtil.insertIndex("dst.bic",idx),       StringUtils.trimToEmpty(u.getGegenkontoBLZ()));
    props.setProperty(SepaUtil.insertIndex("dst.iban",idx),      StringUtils.trimToEmpty(u.getGegenkontoNummer()));
    props.setProperty(SepaUtil.insertIndex("dst.name",idx),      StringUtils.trimToEmpty(u.getGegenkontoName()));
    props.setProperty(SepaUtil.insertIndex("btg.value",idx),     HBCIUtils.value2String(u.getBetrag()));
    props.setProperty(SepaUtil.insertIndex("btg.curr",idx),      k.getWaehrung() != null ? k.getWaehrung() : HBCIProperties.CURRENCY_DEFAULT_DE);
    props.setProperty(SepaUtil.insertIndex("usage",idx),         StringUtils.trimToEmpty(u.getZweck()));
    props.setProperty(SepaUtil.insertIndex("endtoendid",idx),    StringUtils.trimToEmpty(u.getEndtoEndId()));
    props.setProperty(SepaUtil.insertIndex("creditorid",idx),    StringUtils.trimToEmpty(u.getCreditorId()));
    props.setProperty(SepaUtil.insertIndex("mandateid",idx),     StringUtils.trimToEmpty(u.getMandateId()));
    props.setProperty(SepaUtil.insertIndex("manddateofsig",idx), ISO_DATE.format(u.getSignatureDate()));
    props.setProperty(SepaUtil.insertIndex("purposecode",idx),   StringUtils.trimToEmpty(u.getPurposeCode()));
    props.setProperty("pmtinfid",StringUtils.trimToEmpty(u.getPmtInfId()));
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
    return new Class[]{SepaLastschrift.class};
  }
  
  /**
   * Generiert einen Lookup-Key fuer den Auftrag um zu checken, ob er in die SEPA XML-Datei passt.
   * @param l der Auftrag.
   * @return der Lookup-Key.
   * @throws RemoteException
   */
  private String createKey(SepaLastschrift l) throws RemoteException
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


