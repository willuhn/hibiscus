/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/io/XMLSammelTransferExporter.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/02/13 14:17:01 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Exportiert Sammel-Auftraege im XML-Format.
 */
public class XMLSammelTransferExporter extends XMLExporter
{
  /**
   * @see de.willuhn.jameica.hbci.io.XMLExporter#doExport(java.lang.Object[], de.willuhn.jameica.hbci.io.IOFormat, java.io.OutputStream, de.willuhn.util.ProgressMonitor)
   */
  public void doExport(Object[] objects, IOFormat format,OutputStream os, final ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    SammelTransfer[] transfers = (SammelTransfer[]) objects;
    List all = new ArrayList();
    for (int i=0;i<transfers.length;++i)
    {
      all.add(transfers[i]);
      DBIterator buchungen = transfers[i].getBuchungen();
      while (buchungen.hasNext())
        all.add(buchungen.next());
    }
    super.doExport(all.toArray(),format,os,monitor);
  }

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (objectType == null)
      return null;
    
    if (!SammelTransfer.class.isAssignableFrom(objectType))
      return null; // Nur fuer Sammel-Auftraege anbieten - fuer alle anderen tut es die Basis-Implementierung

    return new IOFormat[]{new IOFormat() {
      public String getName()
      {
        return i18n.tr("XML-Format");
      }
    
      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      public String[] getFileExtensions()
      {
        return new String[]{"xml"};
      }
    }};
  }
}


/*********************************************************************
 * $Log: XMLSammelTransferExporter.java,v $
 * Revision 1.1  2009/02/13 14:17:01  willuhn
 * @N BUGZILLA 700
 *
 **********************************************************************/