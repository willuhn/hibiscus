/**********************************************************************
 *
 * Copyright (c) 2024 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Export den Umsatzbaum im Banking4-Format.
 */
public class Banking4UmsatzTypExporter extends AbstractBanking4UmsatzTypIO implements Exporter
{

  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#doExport(java.lang.Object[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  @Override
  public void doExport(Object[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    BufferedWriter writer = null;

    try
    {
      final String q = "Beim Export im Banking4-Format werden immer alle Umsatzkategorien exportiert, unabhängig davon, welche markiert wurden. Vorgang fortsetzen?";
      if (!Application.getCallback().askUser(i18n.tr(q),true))
        return;

      String encoding = settings.getString("banking4.encoding","ISO-8859-15");
      Logger.info("banking4 encoding: " + encoding);

      writer = new BufferedWriter(new OutputStreamWriter(os,encoding));

      final DBService service = Settings.getDBService();

      final GenericIterator<UmsatzTyp> all = service.createList(UmsatzTyp.class);
      final int size = all.size();
      if (size == 0)
      {
        Logger.info("no categories to export");
        return;
      }

      if (monitor != null)
        monitor.setStatusText(i18n.tr("Schreibe Datei"));
      
      writer.write("(nicht auswerten):Buchung wird nicht ausgewertet::exclude");
      writer.newLine();

      double factor = 100d / (double) size;

      final UmsatzTyp ut = service.createObject(UmsatzTyp.class,null);
      final GenericIterator<UmsatzTyp> root = ut.getTopLevelList();
      final AtomicInteger i = new AtomicInteger(0);

      while (root.hasNext())
      {
        final UmsatzTyp t = root.next();
        write(writer,t,monitor,factor,i,0);
      }
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
      Logger.warn("operation cancelled");
      throw new ApplicationException(i18n.tr("Export abgebrochen"));
    }
    catch (Exception e)
    {
      Logger.error("error while writing file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Export der Datei"));
    }
    finally
    {
      IOUtil.close(writer);
    }
  }
  
  /**
   * Schreibt eine Umsatzkategorie rekursiv in die Datei.
   * @param writer der Writer.
   * @param ut die Kategorie.
   * @param monitor der Monitor.
   * @param factor der Faktor.
   * @param i der aktuelle Zähler.
   * @param indent das Einrückungslevel.
   * @throws IOException
   */
  private void write(BufferedWriter writer, UmsatzTyp ut, ProgressMonitor monitor, double factor, AtomicInteger i, int indent) throws IOException
  {
    if (monitor != null)
      monitor.setPercentComplete((int)((i.get()+1) * factor));
    
    final StringBuilder sb = new StringBuilder();
    sb.append(StringUtils.repeat("\t",indent));
    sb.append(ut.getName().replace(SEP,""));
    sb.append(SEP);
    sb.append(StringUtils.trimToEmpty(ut.getKommentar()));
    sb.append(SEP);
    if (ut.isRegex())
      sb.append(StringUtils.trimToEmpty(ut.getPattern()));
    else
      sb.append(StringUtils.trimToEmpty(ut.getPattern()).replace(", ",",").replace(","," "));
    
    sb.append(SEP);
    writer.write(sb.toString());
    
    i.incrementAndGet();
    writer.newLine();
    
    final GenericIterator<UmsatzTyp> children = ut.getChildren();
    if (children.size() > 0)
    {
      indent++;
      while (children.hasNext())
      {
        final UmsatzTyp child = children.next();
        write(writer,child,monitor,factor,i,indent);
      }
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.io.Exporter#suppportsExtension(java.lang.String)
   */
  @Override
  public boolean suppportsExtension(String ext)
  {
    return false;
  }
}


