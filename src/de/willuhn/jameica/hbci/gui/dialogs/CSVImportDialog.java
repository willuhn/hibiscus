/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/CSVImportDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/01/23 18:13:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.io.InputStream;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.io.CSVFile;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.io.CSVMapping;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog zum Importieren von CSV-Daten.
 */
public class CSVImportDialog extends AbstractDialog
{

  private I18N i18n           = null;
  private CSVMapping mapping  = null;
  private String[] line       = null;
  
  /**
   * ct.
   * @param file die zu importierende Datei.
   * @param mapping das CSV-Mapping.
   * @param position
   * @throws ApplicationException
   */
  public CSVImportDialog(InputStream file, CSVMapping mapping, int position) throws ApplicationException
  {
    super(position);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    CSVFile csv = null;
    try
    {
      csv = new CSVFile(file);
      if (!csv.hasNext())
        throw new ApplicationException(i18n.tr("CSV-Datei enthält keine Daten"));
      this.line = csv.next();
    }
    catch (Exception e)
    {
      Logger.error("unable to read csv file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Lesen der CSV-Datei"));
    }
    finally
    {
      if (csv != null)
      {
        try
        {
          csv.close();
        }
        catch (Exception e)
        {
          Logger.error("unable to close csv file",e);
        }
      }
    }

    this.mapping = mapping;
    setTitle(i18n.tr("Zuordnung der Spalten"));
    setSize(350,300);
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Spalten in der Import-Datei"));

    for (int i=0;i<line.length;++i)
    {
//      final SelectInput select = new SelectInput();
    }
    
    ButtonArea b = new ButtonArea(parent,2);
    b.addButton(i18n.tr("Übernehmen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        // Hier Daten uebernehmen
        close();
      }
    });
    b.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    });
  }

  /**
   * Liefert das angepasste Mapping zurueck.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.mapping;
  }

}


/*********************************************************************
 * $Log: CSVImportDialog.java,v $
 * Revision 1.1  2006/01/23 18:13:19  willuhn
 * @N first code for csv import
 *
 **********************************************************************/