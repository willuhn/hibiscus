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
  @Override
  public void doExport(Object[] objects, IOFormat format,OutputStream os, final ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    SammelTransfer[] transfers = (SammelTransfer[]) objects;
    List<Object> all = new ArrayList<>();
    for (SammelTransfer transfer : transfers)
    {
      all.add(transfer);
      DBIterator buchungen = transfer.getBuchungen();
      while (buchungen.hasNext())
        all.add(buchungen.next());
    }
    super.doExport(all.toArray(),format,os,monitor);
  }

  @Override
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (objectType == null)
      return null;
    
    if (!SammelTransfer.class.isAssignableFrom(objectType))
      return null; // Nur fuer Sammel-Auftraege anbieten - fuer alle anderen tut es die Basis-Implementierung

    return new IOFormat[]{new IOFormat() {
      @Override
      public String getName()
      {
        return i18n.tr("Hibiscus-Format");
      }
    
      @Override
      public String[] getFileExtensions()
      {
        return new String[]{"xml"};
      }
    }};
  }
}
