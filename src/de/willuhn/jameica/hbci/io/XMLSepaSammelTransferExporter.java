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

import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Exportiert SEPA-Sammel-Auftraege im XML-Format.
 */
public class XMLSepaSammelTransferExporter extends XMLExporter
{
  @Override
  public void doExport(Object[] objects, IOFormat format,OutputStream os, final ProgressMonitor monitor) throws RemoteException, ApplicationException
  {
    SepaSammelTransfer[] transfers = (SepaSammelTransfer[]) objects;
    List all = new ArrayList();
    for (SepaSammelTransfer t:transfers)
    {
      all.add(t);
      all.addAll(t.getBuchungen());
    }
    super.doExport(all.toArray(),format,os,monitor);
  }

  @Override
  public IOFormat[] getIOFormats(Class objectType)
  {
    if (objectType == null)
      return null;
    
    if (!SepaSammelTransfer.class.isAssignableFrom(objectType))
      return null; // Nur fuer SEPA-Sammel-Auftraege anbieten - fuer alle anderen tut es die Basis-Implementierung

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
