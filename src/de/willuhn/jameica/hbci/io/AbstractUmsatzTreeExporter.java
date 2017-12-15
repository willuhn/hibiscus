/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.Date;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Klasse der PDF-Exporter fuer den Umsatz-Tree.
 * BUGZILLA 1333
 */
public abstract class AbstractUmsatzTreeExporter implements Exporter
{
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.hbci.io.IO#getIOFormats(java.lang.Class)
   */
  public IOFormat[] getIOFormats(Class objectType)
  {
    // Wir unterstuetzen nur Umsatz-Trees
    if (!UmsatzTree.class.equals(objectType))
      return null;

    IOFormat myFormat = new IOFormat() {
    
      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getName()
       */
      public String getName()
      {
        return AbstractUmsatzTreeExporter.this.getName();
      }
    
      public String[] getFileExtensions()
      {
        return new String[]{"pdf"};
      }
    
    };
    return new IOFormat[]{myFormat};
  }

  /**
   * Generiert ein Label mit dem Untertitel der Auswertung.
   * @param tree der zu exportierende Tree.
   * @return der Untertitel.
   * @throws RemoteException
   */
  protected String getSubTitle(UmsatzTree tree) throws RemoteException
  {
    String titel  = tree.getTitle();
    Date start    = tree.getStart();
    Date end      = tree.getEnd();
    DateFormat df = HBCI.DATEFORMAT;
    
    String tt = titel != null ? titel : i18n.tr("alle Konten");
    String st = start != null ? df.format(start) : null;
    String et = end != null ? df.format(end) : null;

    if (st == null && end == null) return i18n.tr("gesamter Zeitraum, {0}",tt);
    if (start == null)             return i18n.tr("Zeitraum: bis {0}, {1}",et,tt);
    if (end == null)               return i18n.tr("Zeitraum: ab {0}, {1}",st,tt);
    return i18n.tr("Zeitraum: {0} - {1}, {2}",st,et,tt);
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


