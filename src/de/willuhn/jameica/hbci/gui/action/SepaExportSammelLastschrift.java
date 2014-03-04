/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.GV.SepaUtil;
import org.kapott.hbci.GV.generators.ISEPAGenerator;
import org.kapott.hbci.GV.generators.SEPAGeneratorFactory;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.sepa.PainVersion;
import org.kapott.hbci.sepa.PainVersion.Type;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.dialogs.SepaExportDialog;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaLastType;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Exportieren einer SEPA-Sammellastschrift als SEPA-XML-Datei.
 */
public class SepaExportSammelLastschrift implements Action
{
  private final static DateFormat ISO_DATE = new SimpleDateFormat("yyyy-MM-dd");

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private SepaSammelLastschrift u = null;
  
  /**
   * ct.
   */
  public SepaExportSammelLastschrift()
  {
  }

  /**
   * ct.
   * @param u
   */
  public SepaExportSammelLastschrift(SepaSammelLastschrift u)
  {
    this();
    this.u = u;
  }
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context instanceof SepaSammelLastschrift)
      this.u = (SepaSammelLastschrift) context;

    if (this.u == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierende SEPA-Sammellastschrift aus"));
    
    OutputStream os = null;
    
    try
    {
      if (u.isNewObject())
        u.store();
      
      Konto k = u.getKonto();
      
      SepaExportDialog d = new SepaExportDialog(Type.PAIN_008);
      d.open();
      
      File target         = d.getFile();
      PainVersion version = d.getPainVersion();
      
      SepaLastType type = u.getType();
      if (type == null)
        type = SepaLastType.DEFAULT;
      

      Properties props = new Properties();
      props.setProperty("src.bic",      StringUtils.trimToEmpty(k.getBic()));
      props.setProperty("src.iban",     StringUtils.trimToEmpty(k.getIban()));
      props.setProperty("src.name",     StringUtils.trimToEmpty(k.getName()));
      props.setProperty("sequencetype", u.getSequenceType().name());
      props.setProperty("targetdate",   u.getTargetDate() != null ? ISO_DATE.format(u.getTargetDate()) : "1999-01-01");
      props.setProperty("type",         type.name());
      props.setProperty("sepaid",       Long.toString(System.currentTimeMillis()));
      
      List<SepaSammelLastBuchung> buchungen = u.getBuchungen();
      for (int i=0;i<buchungen.size();++i)
      {
        SepaSammelLastBuchung b = buchungen.get(i);
        props.setProperty(SepaUtil.insertIndex("dst.bic",i),      StringUtils.trimToEmpty(b.getGegenkontoBLZ()));
        props.setProperty(SepaUtil.insertIndex("dst.iban",i),     StringUtils.trimToEmpty(b.getGegenkontoNummer()));
        props.setProperty(SepaUtil.insertIndex("dst.name",i),     StringUtils.trimToEmpty(b.getGegenkontoName()));
        props.setProperty(SepaUtil.insertIndex("btg.value",i),    HBCIUtils.value2String(b.getBetrag()));
        props.setProperty(SepaUtil.insertIndex("btg.curr",i),     k.getWaehrung() != null ? k.getWaehrung() : HBCIProperties.CURRENCY_DEFAULT_DE);
        props.setProperty(SepaUtil.insertIndex("usage",i),        StringUtils.trimToEmpty(b.getZweck()));
        props.setProperty(SepaUtil.insertIndex("endtoendid",i),   StringUtils.trimToEmpty(b.getEndtoEndId()));
        
        props.setProperty(SepaUtil.insertIndex("creditorid",i),   StringUtils.trimToEmpty(b.getCreditorId()));
        props.setProperty(SepaUtil.insertIndex("mandateid",i),    StringUtils.trimToEmpty(b.getMandateId()));
        props.setProperty(SepaUtil.insertIndex("manddateofsig",i),ISO_DATE.format(b.getSignatureDate()));
      }

      // Hier muss immer CORE angegeben sein, weil es nur einen Generator fuer CORE/COR1/B2B gibt.
      // Im Property "type" ist aber der korrekte Typ hinterlegt.
      ISEPAGenerator gen = SEPAGeneratorFactory.get(SepaLastType.CORE.getJobName(),version);
      os = new BufferedOutputStream(new FileOutputStream(target));
      gen.generate(props,os,false);
      os.close();
      
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("SEPA-Datei gespeichert"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (OperationCanceledException oce)
    {
      // ignore
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while choosing sepa version + target file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Export der SEPA-Sammellastschrift"));
    }
    finally
    {
      IOUtil.close(os);
    }
  }
  
}


