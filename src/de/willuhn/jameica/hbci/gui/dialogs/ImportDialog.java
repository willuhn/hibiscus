/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/ImportDialog.java,v $
 * $Revision: 1.4.2.2 $
 * $Date: 2006/04/21 09:27:26 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.io.IOFormat;
import de.willuhn.jameica.hbci.io.IORegistry;
import de.willuhn.jameica.hbci.io.Importer;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Dialog, ueber den Daten importiert werden koennen.
 */
public class ImportDialog extends AbstractDialog
{

	private I18N i18n;

  private Input importerListe     = null;
  private GenericObject context   = null;	
  private Class type              = null;
  
  /**
   * ct.
   * @param context Context.
   * @param type die Art der zu importierenden Objekte.
   */
  public ImportDialog(GenericObject context, Class type)
  {
    super(POSITION_CENTER);
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
		setTitle(i18n.tr("Daten-Import"));
    this.context = context;
    this.type = type;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
		LabelGroup group = new LabelGroup(parent,i18n.tr("Auswahl des Import-Filters"));
		group.addText(i18n.tr("Bitte wählen Sie das gewünschte Dateiformat für den Import aus"),true);

		group.addLabelPair(i18n.tr("Verfügbare Formate:"),getImporterList());

		ButtonArea buttons = new ButtonArea(parent,2);
		buttons.addButton(i18n.tr("Import starten"),new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
        try
        {
          if (getImporterList() instanceof LabelInput)
            return;
        }
        catch (Exception e)
        {
          Logger.error("unable to check import format",e);
        }
				doImport();
			}
		},null,true);
		buttons.addButton(i18n.tr("Abbrechen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				throw new OperationCanceledException();
			}
		});
  }

  /**
   * Importiert die Daten.
   * @throws ApplicationException
   */
  private void doImport() throws ApplicationException
  {
    Imp imp = null;

    try
    {
      imp = (Imp) getImporterList().getValue();
    }
    catch (Exception e)
    {
      Logger.error("error while saving import file",e);
      throw new ApplicationException(i18n.tr("Fehler beim Starten des Imports"),e);
    }

    if (imp == null || imp.importer == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Import-Format aus"));

    Settings settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);

    FileDialog fd = new FileDialog(GUI.getShell(),SWT.OPEN);
    fd.setText(i18n.tr("Bitte wählen Sie die Datei aus, welche für den Import verwendet werden soll."));
    fd.setFilterNames(imp.format.getFileExtensions());

    String path = settings.getString("lastdir",System.getProperty("user.home"));
    if (path != null && path.length() > 0)
      fd.setFilterPath(path);

    final String s = fd.open();
    

    if (s == null || s.length() == 0)
    {
      close();
      return;
    }

    final File file = new File(s);
    if (!file.exists() || !file.isFile())
      throw new ApplicationException(i18n.tr("Datei existiert nicht oder ist nicht lesbar"));
    
    // Wir merken uns noch das Verzeichnis vom letzten mal
    settings.setAttribute("lastdir",file.getParent());

    // Dialog schliessen
    close();

    final Importer importer = imp.importer;
    final IOFormat format = imp.format;

    BackgroundTask t = new BackgroundTask() {
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          InputStream is = new BufferedInputStream(new FileInputStream(file));
          importer.doImport(context,format,is,monitor);
          monitor.setPercentComplete(100);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          GUI.getStatusBar().setSuccessText(i18n.tr("Daten importiert aus {0}",s));
          GUI.getCurrentView().reload();
        }
        catch (ApplicationException ae)
        {
          monitor.setStatusText(ae.getMessage());
          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          GUI.getStatusBar().setErrorText(ae.getMessage());
          throw ae;
        }
        catch (Exception e)
        {
          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          Logger.error("error while reading objects from " + s,e);
          ApplicationException ae = new ApplicationException(i18n.tr("Fehler beim Importieren der Daten aus {0}",s),e);
          monitor.setStatusText(ae.getMessage());
          GUI.getStatusBar().setErrorText(ae.getMessage());
          throw ae;
        }
      }

      public void interrupt() {}
      public boolean isInterrupted()
      {
        return false;
      }
    };

    Application.getController().start(t);
  }

	/**
	 * Liefert eine Liste der verfuegbaren Importer.
   * @return Liste der Importer.
	 * @throws Exception
   */
  private Input getImporterList() throws Exception
	{
		if (importerListe != null)
			return importerListe;

    Importer[] importers = IORegistry.getImporters();

    ArrayList l = new ArrayList();

    int size = 0;

    for (int i=0;i<importers.length;++i)
		{
      Importer imp = importers[i];
      if (imp == null)
        continue;
      IOFormat[] formats = imp.getIOFormats(type);
      if (formats == null || formats.length == 0)
      {
        Logger.warn("importer " + imp.getName() + " provides no import formats, skipping");
      }
      for (int j=0;j<formats.length;++j)
      {
        size++;
        l.add(new Imp(imp,formats[j]));
      }
		}

		if (size == 0)
		{
			importerListe = new LabelInput(i18n.tr("Keine Import-Filter verfügbar"));
			return importerListe;
		}

		Imp[] imp = (Imp[]) l.toArray(new Imp[size]);
		importerListe = new SelectInput(PseudoIterator.fromArray(imp),null);
		return importerListe;
	}

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

	/**
	 * Hilfsklasse zur Anzeige der Importer.
   */
  private class Imp implements GenericObject
	{
		private Importer importer = null;
    private IOFormat format   = null;
		
		private Imp(Importer importer, IOFormat format)
		{
			this.importer = importer;
      this.format = format;
		}

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      return this.format.getName();
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
      return this.importer.getClass().getName() + "#" + this.format.getClass().getName();
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
 * $Log: ImportDialog.java,v $
 * Revision 1.4.2.2  2006/04/21 09:27:26  willuhn
 * @B typo
 *
 * Revision 1.4.2.1  2006/04/21 09:15:10  willuhn
 * @B MT940-Import wieder aktiviert
 *
 * Revision 1.4  2006/01/23 23:07:23  willuhn
 * @N csv import stuff
 *
 * Revision 1.3  2006/01/23 12:16:57  willuhn
 * @N Update auf HBCI4Java 2.5.0-rc5
 *
 * Revision 1.2  2006/01/23 00:36:29  willuhn
 * @N Import, Export und Chipkartentest laufen jetzt als Background-Task
 *
 * Revision 1.1  2006/01/18 00:51:01  willuhn
 * @B bug 65
 *
 **********************************************************************/