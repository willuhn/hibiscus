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
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Exportieren eines SEPA-Ueberweisung als SEPA-XML-Datei.
 */
public class SepaExportUeberweisung implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private AuslandsUeberweisung u = null;
  
  /**
   * ct.
   */
  public SepaExportUeberweisung()
  {
  }

  /**
   * ct.
   * @param u
   */
  public SepaExportUeberweisung(AuslandsUeberweisung u)
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
    if (context instanceof AuslandsUeberweisung)
      this.u = (AuslandsUeberweisung) context;

    if (this.u == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierende SEPA-Überweisung aus"));
    
    OutputStream os = null;
    
    try
    {
      if (u.isNewObject())
        u.store();
      
      Konto k = u.getKonto();
      
      SepaExportDialog d = new SepaExportDialog(Type.PAIN_001);
      d.open();
      
      File target         = d.getFile();
      PainVersion version = d.getPainVersion();
      
      Properties props = new Properties();
      props.setProperty("src.bic",    StringUtils.trimToEmpty(k.getBic()));
      props.setProperty("src.iban",   StringUtils.trimToEmpty(k.getIban()));
      props.setProperty("src.name",   StringUtils.trimToEmpty(k.getName()));
      props.setProperty("dst.bic",    StringUtils.trimToEmpty(u.getGegenkontoBLZ()));
      props.setProperty("dst.iban",   StringUtils.trimToEmpty(u.getGegenkontoNummer()));
      props.setProperty("dst.name",   StringUtils.trimToEmpty(u.getGegenkontoName()));
      props.setProperty("btg.value",  HBCIUtils.value2String(u.getBetrag()));
      props.setProperty("btg.curr",   k.getWaehrung() != null ? k.getWaehrung() : HBCIProperties.CURRENCY_DEFAULT_DE);
      props.setProperty("usage",      StringUtils.trimToEmpty(u.getZweck()));
      props.setProperty("sepaid",     Long.toString(System.currentTimeMillis()));
      props.setProperty("endtoendid", StringUtils.trimToEmpty(u.getEndtoEndId()));
      
      ISEPAGenerator gen = SEPAGeneratorFactory.get("UebSEPA",version);
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
      throw new ApplicationException(i18n.tr("Fehler beim Export der SEPA-Überweisung"));
    }
    finally
    {
      IOUtil.close(os);
    }
    
  }

}


