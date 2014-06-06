/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

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
 * Abstrakte Basis-Klasse fuer Importer.
 */
public abstract class AbstractImporter implements Importer
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.io.Importer#doImport(java.lang.Object, de.willuhn.jameica.hbci.io.IOFormat, java.io.InputStream, de.willuhn.util.ProgressMonitor)
   */
  @Override
  public void doImport(Object context, IOFormat format, InputStream is, ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    try
    {
      Object[] objects = this.setup(context,format,is,monitor);
      

      Map ctx = new HashMap();
      
      double factor = ((double)(100 - monitor.getPercentComplete())) / objects.length;
      monitor.setStatusText(i18n.tr("Importiere Daten"));
      
      for (int i=0;i<objects.length;++i)
      {
        monitor.setPercentComplete((int)((i) * factor));
        monitor.log(i18n.tr("Lese Datensatz {0}",Integer.toString(i+1)));
        
        this.importObject(objects[i],i,ctx);
      }
      this.commit(objects,format,is,monitor);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Daten importiert"),StatusBarMessage.TYPE_SUCCESS));
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
    
    for (int i=0;i<supported.length;++i)
    {
      if (objectType.equals(supported[i]))
        return new IOFormat[] { new MyIOFormat(objectType) };
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


