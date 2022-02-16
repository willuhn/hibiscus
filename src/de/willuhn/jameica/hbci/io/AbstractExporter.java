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

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Abstrakte Basis-Klasse fuer Exporter.
 */
public abstract class AbstractExporter implements Exporter
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  @Override
  public void doExport(Object[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    try
    {
      this.setup(objects,format,os,monitor);
      
      
      double factor = ((double)(100 - monitor.getPercentComplete())) / objects.length;
      monitor.setStatusText(i18n.tr("Exportiere Daten"));
      
      for (int i=0;i<objects.length;++i)
      {
        monitor.setPercentComplete((int)((i) * factor));
        monitor.log(i18n.tr("Speichere Datensatz {0}",Integer.toString(i+1)));
        
        this.exportObject(objects[i],i,os);
      }
      this.commit(objects,format,os,monitor);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Daten exportiert"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (ApplicationException | OperationCanceledException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      Logger.error("unable to export data",e);
      throw new ApplicationException(i18n.tr("Fehler beim Export der Daten: {0}",e.getMessage()),e);
    }
    finally
    {
      IOUtil.close(os);
    }
  }
  
  /**
   * Initialisiert den Export fuer die Objekte.
   * @param objects die zu exportierenden Objekte.
   * @param format das vom User ausgewaehlte Export-Format.
   * @param os der Ziel-Ausgabe-Stream.
   * @param monitor ein Monitor, an den der Exporter Ausgaben ueber seinen Bearbeitungszustand ausgeben kann.
   * @throws Exception
   */
  void setup(Object[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws Exception
  {
    if (objects == null || objects.length == 0)
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie die zu exportierenden Daten aus"));
  }
  
  /**
   * Beendet den Export.
   * @param objects die zu exportierenden Objekte.
   * @param format das vom User ausgewaehlte Export-Format.
   * @param os der Ziel-Ausgabe-Stream.
   * @param monitor ein Monitor, an den der Exporter Ausgaben ueber seinen Bearbeitungszustand ausgeben kann.
   * @throws Exception
   */
  void commit(Object[] objects, IOFormat format, OutputStream os, ProgressMonitor monitor) throws Exception
  {
  }

  
  /**
   * Fuehrt den Export fuer ein einzelnes Objekt aus.
   * @param o das zu exportierende Objekt.
   * @param idx der Index des Objekts in der Liste. Beginnend bei 0.
   * @param os der OutputStream.
   * @throws Exception
   */
  abstract void exportObject(Object o, int idx, OutputStream os) throws Exception;
  
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
  
  @Override
  public IOFormat[] getIOFormats(Class objectType)
  {
    // Kein Typ angegeben?
    if (objectType == null)
      return null;

    Class[] supported = getSupportedObjectTypes();
    if (supported == null || supported.length == 0)
      return null;
    
    for (int i=0;i<supported.length;++i)
    {
      if (objectType.equals(supported[i]))
        return new IOFormat[] { new MyIOFormat(objectType) };
    }
    return null;
  }
  
  @Override
  public boolean suppportsExtension(String ext)
  {
    return false;
  }

  /**
   * Hilfsklasse, damit wir uns den Objekt-Typ merken koennen.
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

    @Override
    public String getName()
    {
      return AbstractExporter.this.getName();
    }

    @Override
    public String[] getFileExtensions()
    {
      return AbstractExporter.this.getFileExtensions();
    }
  }

}


