/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/UmsatzExportDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2005/06/06 10:37:07 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.export.ExportRegistry;
import de.willuhn.jameica.hbci.export.Exporter;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog, ueber den die Umsaetze exportiert werden koennen.
 */
public class UmsatzExportDialog extends AbstractDialog
{

	private I18N i18n;

  private Input exporterListe = null;
  private Umsatz[] umsaetze = null;	
  
  /**
   * ct.
   * @param umsaetze Liste der zu exportierenden Umsaetze.
   */
  public UmsatzExportDialog(Umsatz[] umsaetze)
  {
    super(POSITION_CENTER);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
		setTitle(i18n.tr("Konto-Umsätze exportieren"));
    this.umsaetze = umsaetze;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
		LabelGroup group = new LabelGroup(parent,i18n.tr("Auswahl des Export-Filters"));
		group.addText(i18n.tr("Bitte wählen Sie das gewünschte Dateiformat aus für den Export aus"),true);

		group.addLabelPair(i18n.tr("Verfügbare Formate:"),getExporterList());

		ButtonArea buttons = new ButtonArea(parent,2);
		buttons.addButton(i18n.tr("Export starten"),new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				export();
			}
		},null,true);
		buttons.addButton(i18n.tr("Schliessen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				close();
			}
		});
  }

  /**
   * Exportiert die Umsaetze.
   * @throws ApplicationException
   */
  private void export() throws ApplicationException
  {
    FileDialog fd = new FileDialog(GUI.getShell(),SWT.SAVE);
    fd.setText(i18n.tr("Bitte geben Sie den Dateinamen ein, in der die Umsätze gespeichert werden sollen."));
    fd.setFileName(i18n.tr("hibiscus-umsaetze-{0}.csv",HBCI.FASTDATEFORMAT.format(new Date())));
    String s = fd.open();
    
    Settings settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    String path = settings.getString("lastdir",System.getProperty("user.home"));
    if (path != null && path.length() > 0)
      fd.setFilterPath(path);

    if (s == null || s.length() == 0)
    {
      close();
      return;
    }

    File file = new File(s);
    if (file.exists())
    {
      try
      {
        YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
        d.setTitle(i18n.tr("Datei existiert bereits"));
        d.setText(i18n.tr("Möchten Sie die Datei überschreiben?"));
        Boolean choice = (Boolean) d.open();
        if (!choice.booleanValue())
        {
          // Dialog schliessem
          close();
          return;
        }
      }
      catch (Exception e)
      {
        // Dialog schliessem
        close();
        Logger.error("error while saving ini letter",e);
        throw new ApplicationException(i18n.tr("Fehler beim Speichern der Export-Datei in {0}",s),e);
      }
    }
    
    OutputStream os = null;

    try
    {
      Exp exp = (Exp) getExporterList().getValue();
      if (exp == null || exp.exporter == null)
        throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Export-Format aus"));

      Exporter exporter = exp.exporter;

      os = new BufferedOutputStream(new FileOutputStream(file));
      exporter.export(umsaetze,os);

      // Wir merken uns noch das Verzeichnis vom letzten mal
      settings.setAttribute("lastdir",file.getParent());

      // Dialog schliessen
      close();
      GUI.getStatusBar().setSuccessText(i18n.tr("Umsätze exportiert nach {0}",s));
    }
    catch (Exception e)
    {
      Logger.error("error while writing umsaetze to " + s,e);
      throw new ApplicationException(i18n.tr("Fehler beim Exportieren der Umsätze in {0}",s),e);
    }
    finally
    {
      if (os != null)
      {
        try
        {
          os.close();
        }
        catch (Exception e)
        {
          // useless
        }
        // Dialog schliessem
        close();
      }
    }
  }

	/**
	 * Liefert eine Liste der verfuegbaren Exporter.
   * @return Liste der Exporter.
	 * @throws Exception
   */
  private Input getExporterList() throws Exception
	{
		if (exporterListe != null)
			return exporterListe;

    Exporter[] exporters = ExportRegistry.getExporters();

    ArrayList l = new ArrayList();

		for (int i=0;i<exporters.length;++i)
		{
			l.add(new Exp(exporters[i]));
		}

		if (l.size() == 0)
		{
			exporterListe = new LabelInput(i18n.tr("Keine Export-Filter verfügbar"));
			return exporterListe;
		}

		Exp[] exp = (Exp[]) l.toArray(new Exp[l.size()]);
		exporterListe = new SelectInput(PseudoIterator.fromArray(exp),null);
		return exporterListe;
	}

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

	/**
	 * Hilfsklasse zur Anzeige der Exporter.
   */
  private class Exp implements GenericObject
	{
		private Exporter exporter = null;
		
		private Exp(Exporter exporter)
		{
			this.exporter = exporter;
		}

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      return this.exporter.getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[] {"name"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return getClass().getName();
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject arg0) throws RemoteException
    {
    	if (arg0 == null)
	      return false;
	    return this.getID().equals(arg0.getID());
    }
	}
}


/**********************************************************************
 * $Log: UmsatzExportDialog.java,v $
 * Revision 1.2  2005/06/06 10:37:07  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/06/02 22:57:34  web0
 * @N Export von Konto-Umsaetzen
 *
 **********************************************************************/