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
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
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
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Exportieren einer SEPA-Lastschrift als SEPA-XML-Datei.
 */
public class SepaExportLastschrift implements Action
{
  private final static DateFormat ISO_DATE = new SimpleDateFormat("yyyy-MM-dd");

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private SepaLastschrift u = null;
  
  /**
   * ct.
   */
  public SepaExportLastschrift()
  {
  }

  /**
   * ct.
   * @param u
   */
  public SepaExportLastschrift(SepaLastschrift u)
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
    if (context instanceof SepaLastschrift)
      this.u = (SepaLastschrift) context;

    if (this.u == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierende SEPA-Lastschrift aus"));
    
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
      props.setProperty("dst.bic",      StringUtils.trimToEmpty(u.getGegenkontoBLZ()));
      props.setProperty("dst.iban",     StringUtils.trimToEmpty(u.getGegenkontoNummer()));
      props.setProperty("dst.name",     StringUtils.trimToEmpty(u.getGegenkontoName()));
      props.setProperty("btg.value",    HBCIUtils.value2String(u.getBetrag()));
      props.setProperty("btg.curr",     k.getWaehrung() != null ? k.getWaehrung() : HBCIProperties.CURRENCY_DEFAULT_DE);
      props.setProperty("usage",        StringUtils.trimToEmpty(u.getZweck()));
      props.setProperty("sepaid",       Long.toString(System.currentTimeMillis()));
      props.setProperty("endtoendid",   StringUtils.trimToEmpty(u.getEndtoEndId()));
      
      props.setProperty("creditorid",   StringUtils.trimToEmpty(u.getCreditorId()));
      props.setProperty("mandateid",    StringUtils.trimToEmpty(u.getMandateId()));
      props.setProperty("manddateofsig",ISO_DATE.format(u.getSignatureDate()));
      props.setProperty("sequencetype", u.getSequenceType().name());
      props.setProperty("targetdate",   u.getTargetDate() != null ? ISO_DATE.format(u.getTargetDate()) : "1999-01-01");
      props.setProperty("type",         type.name());

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
      throw new ApplicationException(i18n.tr("Fehler beim Export der SEPA-Lastschrift"));
    }
    finally
    {
      IOUtil.close(os);
    }
    
  }

}


