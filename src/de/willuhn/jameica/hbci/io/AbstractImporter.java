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

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.io.IOUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Abstrakte Basis-Klasse fuer Importer.
 */
public abstract class AbstractImporter implements Importer
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor, de.willuhn.jameica.system.BackgroundTask)
   */
  @Override
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor, BackgroundTask t) throws RemoteException, ApplicationException
  {
    try
    {
      Object[] objects = this.setup(context,format,is,monitor);
      

      Map ctx = new HashMap();
      
      double factor = ((double)(100 - monitor.getPercentComplete())) / objects.length;
      monitor.setStatusText(i18n.tr("Importiere Daten"));

      int success = 0;
      int failed = 0;
      for (int i=0;i<objects.length;++i)
      {
        if (t != null && t.isInterrupted())
          throw new OperationCanceledException();

        monitor.setPercentComplete((int)((i) * factor));
        monitor.log(i18n.tr("Lese Datensatz {0}",Integer.toString(i+1)));
        try
        {
          this.importObject(objects[i],i,ctx);
          success++;
        }
        catch (Exception e)
        {
          String msg = e.getMessage();
          monitor.log("  " + i18n.tr("  Import fehlgeschlagen: {0}",msg != null ? msg : e.getClass().getSimpleName()));
          monitor.log("  " + i18n.tr("  Fehlerhafter Datensatz: {0}",BeanUtil.toString(objects[i])));
          failed++;
        }
      }
      this.commit(objects,format,is,monitor);
      monitor.log(i18n.tr("{0} importiert, {1} fehlerhaft",Integer.toString(success),Integer.toString(failed)));
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to import data",e);
      throw new ApplicationException(i18n.tr("Fehler beim Import der Daten: {0}",e.getMessage()),e);
    }
    finally
    {
      IOUtil.close(is);
    }
  }
  
  /**
   * Initialisiert den Import fuer die Objekte.
   * @param context Context, der dem Importer hilft, den Zusammenhang zu erkennen,
   * in dem er aufgerufen wurde. Das kann zum Beispiel ein Konto sein.
   * @param format das vom User ausgewaehlte Import-Format.
   * @param is der Stream, aus dem die Daten gelesen werden.
   * @param monitor ein Monitor, an den der Importer Ausgaben ueber seinen
   * @throws Exception
   */
  abstract Object[] setup(Object context, IOFormat format, InputStream is, ProgressMonitor monitor) throws Exception;
  
  /**
   * Beendet den Import.
   * @param objects die zu importierenden Objekte.
   * @param format das vom User ausgewaehlte Format.
   * @param is der InputStream.
   * @param monitor ein Monitor, an den der Importer Ausgaben ueber seinen Bearbeitungszustand ausgeben kann.
   * @throws Exception
   */
  void commit(Object[] objects, IOFormat format, InputStream is, ProgressMonitor monitor) throws Exception
  {
  }

  
  /**
   * Fuehrt den Import fuer ein einzelnes Objekt aus.
   * @param o das zu importierende Objekt.
   * @param idx der Index des Objekts in der Liste. Beginnend bei 0.
   * @param ctx generische Map, in der die Implementierung Context-Informationen speichern kann. Die Map bleibt
   * fuer den gesamten Import erhalten.
   * @throws Exception
   */
  abstract void importObject(Object o, int idx, Map ctx) throws Exception;
  
  /**
   * Liefert eine Liste von Objekt-Typen, die von diesem IO unterstuetzt werden.
   * @return Liste der unterstuetzten Formate.
   */
  abstract Class[] getSupportedObjectTypes();
  
  /**
   * Liefert die Datei-Endungen des Formats.
   * Zum Beispiel "*.csv" oder "*.txt".
   * @return Datei-Endung.
   */
  abstract String[] getFileExtensions();
  
  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  @Override
  public IOFormat[] getIOFormats(Class objectType)
  {
    // Kein Typ angegeben?
    if (objectType == null)
      return null;

    Class[] supported = getSupportedObjectTypes();
    if (supported == null || supported.length == 0)
      return null;

    for (Class supportedObjType : supported) {
      if (objectType.equals(supportedObjType))
        return new IOFormat[]{new MyIOFormat(objectType)};
    }
    return null;
  }

  /**
   * Hilfsklasse, damit wir uns den Objekt-Typ merken koennen.
   * @author willuhn
   */
  class MyIOFormat implements IOFormat
  {
    Class type = null;
    
    /**
     * ct.
     * @param type
     */
    private MyIOFormat(Class type)
    {
      this.type = type;
    }

    /**
     * @see de.willuhn.jameica.hbci.io.IOFormat#getName()
     */
    public String getName()
    {
      return AbstractImporter.this.getName();
    }

    /**
     * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
     */
    public String[] getFileExtensions()
    {
      return AbstractImporter.this.getFileExtensions();
    }
  }

}


