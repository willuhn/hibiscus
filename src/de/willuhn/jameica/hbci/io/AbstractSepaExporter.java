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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.GV.generators.ISEPAGenerator;
import org.kapott.hbci.GV.generators.SEPAGeneratorFactory;
import org.kapott.hbci.sepa.SepaVersion;
import org.kapott.hbci.sepa.SepaVersion.Type;

import de.willuhn.jameica.hbci.gui.dialogs.KontoAuswahlDialog;
import de.willuhn.jameica.hbci.gui.dialogs.PainVersionDialog;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;
import de.willuhn.logging.Logger;
import de.willuhn.util.ProgressMonitor;


/**
 * Abstrakte Basis-Klasse fuer SEPA-Export.
 */
public abstract class AbstractSepaExporter extends AbstractExporter
{
  private final static String SYSPROP_FORMATTED = "sepa.pain.formatted";

  private Map<OutputStream,JobContext> jobs = Collections.synchronizedMap(new HashMap<OutputStream,JobContext>());

  @Override
  public String getName()
  {
    return i18n.tr("SEPA-XML");
  }

  @Override
  String[] getFileExtensions()
  {
    return new String[]{"*.xml"};
  }

  @Override
  void setup(Object[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws Exception
  {
    super.setup(objects, format, os, monitor);

    // Checken, ob die Auftraege zum selben Konto gehoeren
    Set<String> ids = new HashSet<String>();
    for (Object o:objects)
    {
      Konto k = this.getKonto(o);
      if (k == null)
      {
        Logger.warn("unable to determine konto for object " + o);
        continue;
      }
      ids.add(k.getID());
      if (ids.size() > 1)
        break;
    }

    // Wir haben unterschiedliche Konten. Die Auftraege koennen aber nur einem Konto zugeordnet sein.
    // Wir fragen daher den User.
    Konto konto;
    if (ids.size() > 1)
    {
      KontoAuswahlDialog d = new KontoAuswahlDialog(null,KontoFilter.FOREIGN,KontoAuswahlDialog.POSITION_CENTER);
      d.setText(i18n.tr("Die Aufträge sind unterschiedlichen Konten zugeordnet.\n" +
      		              "Eine SEPA XML-Datei kann jedoch nur Aufträge eines Kontos enthalten.\n\n" +
      		              "Bitte wählen Sie das Konto, dem die Aufträge in der XML-Datei\n" +
      		              "zugeordnet werden sollen"));
      konto = (Konto) d.open();
    }
    else
    {
      konto = this.getKonto(objects[0]); // Ansonsten das Konto des ersten Objektes
    }

    JobContext ctx = new JobContext();
    this.jobs.put(os,ctx); // dem Stream zuordnen

    // User nach der SEPA-Version fragen, die verwendet werden soll.
    PainVersionDialog d = new PainVersionDialog(this.getPainType());
    SepaVersion version = (SepaVersion) d.open();
    ctx.version = version;

    // Header-Infos zuweisen
    ctx.props.setProperty("src.bic",    StringUtils.trimToEmpty(konto.getBic()));
    ctx.props.setProperty("src.iban",   StringUtils.trimToEmpty(konto.getIban()));
    ctx.props.setProperty("src.name",   StringUtils.trimToEmpty(konto.getName()));
    ctx.props.setProperty("sepaid",     Long.toString(System.currentTimeMillis()));
  }

  @Override
  void commit(Object[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws Exception
  {
    String backup = System.getProperty(SYSPROP_FORMATTED);
    try
    {
      System.setProperty(SYSPROP_FORMATTED,"true");

      // Daten schreiben
      JobContext ctx = jobs.get(os); // ich pruefe hier nicht, ob das NULL ist, dann ist eh irgendwas ganz faul
      ISEPAGenerator gen = SEPAGeneratorFactory.get(this.getJobName(),ctx.version);
      gen.generate(ctx.props,os,false);
    }
    finally
    {
      if (backup != null)
        System.setProperty(SYSPROP_FORMATTED,backup);
      else
        System.clearProperty(SYSPROP_FORMATTED);

      jobs.remove(os);
      super.commit(objects, format, os, monitor);
    }
  }

  @Override
  void exportObject(Object o, int idx, OutputStream os) throws Exception
  {
    this.exportObject(o,idx,jobs.get(os));
  }

  /**
   * Liefert das Konto fuer das angegebene Objekt.
   * @param o das Objekt.
   * @return das Konto.
   * @throws Exception
   */
  private Konto getKonto(Object o) throws Exception
  {
    if (o instanceof HibiscusTransfer)
      return ((HibiscusTransfer) o).getKonto();

    if (o instanceof SepaSammelTransfer)
      return ((SepaSammelTransfer) o).getKonto();

    return null;
  }

  /**
   * Schreibt die Eigenschaften des Auftrages in die Properties. 
   * @param o das zu exportierende Objekt.
   * @param idx der Index in der Liste der Objekte.
   * @param ctx der Auftragskontext.
   * @throws Exception
   */
  protected abstract void exportObject(Object o, int idx, JobContext ctx) throws Exception;

  /**
   * Liefert den zu verwendenden SEPA PAIN-Type.
   * @return der zu verwendende SEPA PAIN-Type.
   */
  protected abstract Type getPainType();

  /**
   * Liefert den zu verwendenden HBCI4Java-Jobname.
   * @return der zu verwendende HBCI4Java-Jobname.
   */
  protected abstract String getJobName();

  /**
   * Container, der den Job-Context haelt.
   */
  protected class JobContext
  {
    protected SepaVersion version = null;
    protected Properties props = new Properties();
    protected Map meta = new HashMap();

  }

}
