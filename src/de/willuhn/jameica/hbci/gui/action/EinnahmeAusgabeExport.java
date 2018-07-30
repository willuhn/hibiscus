/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.ExportDialog;
import de.willuhn.jameica.hbci.gui.dialogs.ExportDialog.ExpotFormat;
import de.willuhn.jameica.hbci.io.XMLExporter;
import de.willuhn.jameica.hbci.server.EinnahmeAusgabe;
import de.willuhn.jameica.hbci.server.EinnameAusgabeTreeNode;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action für die Ausgabe der Einnahmen/Ausgaben
 */
public class EinnahmeAusgabeExport implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Array mit Objekten des Typs <code>Einnahmeausgabe</code>
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      if(context instanceof EinnahmeAusgabe[])
      {
        ExportDialog d = new ExportDialog((EinnahmeAusgabe[]) context,EinnahmeAusgabe.class);
        filterFormatsAndOpenDialog(d);
        
      }else if(context instanceof EinnameAusgabeTreeNode[]){
        ExportDialog d = new ExportDialog((EinnameAusgabeTreeNode[]) context,EinnameAusgabeTreeNode.class);
        filterFormatsAndOpenDialog(d);
      }else{
        throw new ApplicationException(i18n.tr("Bitte wählen Sie die zu exportierenden Daten aus"));
      }
    }
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while writing report", e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler bei der Erstellung der Liste"),StatusBarMessage.TYPE_ERROR));
    }
  }

  //da die Objekte (angeblich) GenericObject implementieren, wird der XML-Export angeboten
  //das wollen wir nicht, vielleicht geht das auch eleganter
  private void filterFormatsAndOpenDialog(ExportDialog d) throws Exception
  {
    List exportFormatList = ((SelectInput)d.getExporterList()).getList();
    List filtered=new ArrayList();
    for (Object object : exportFormatList)
    {
      if(object instanceof ExpotFormat)
      {
        ExpotFormat f=(ExpotFormat)object;
        if(!(f.getExporter() instanceof XMLExporter)){
          filtered.add(object);
        }
      }
    }
    ((SelectInput)d.getExporterList()).setList(filtered);
    d.open();
  }
}

/*******************************************************************************
 * $Log: EinnahmeAusgabeExport.java,v $
 * Revision 1.5  2011/05/11 10:20:28  willuhn
 * @N OCE fangen
 *
 * Revision 1.4  2010-08-24 17:38:04  willuhn
 * @N BUGZILLA 896
 *
 * Revision 1.3  2009/04/05 21:16:22  willuhn
 * @B BUGZILLA 716
 ******************************************************************************/
