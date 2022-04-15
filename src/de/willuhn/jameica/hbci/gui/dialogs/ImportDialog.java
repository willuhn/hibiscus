/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;

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
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
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
  private final static int WINDOW_WIDTH = 420;

	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private Input importerListe     = null;
  private GenericObject context   = null;	
  private Class type              = null;
  
  private Settings  settings      = null;
  private BackgroundTask task     = null;

  /**
   * ct.
   * @param context Context.
   * @param type die Art der zu importierenden Objekte.
   */
  public ImportDialog(GenericObject context, Class type)
  {
    super(POSITION_CENTER);

    this.context = context;
    this.type = type;
    
		this.setTitle(i18n.tr("Daten-Import"));
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);

    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
		Container group = new SimpleContainer(parent);
		group.addText(i18n.tr("Bitte wählen Sie das gewünschte Dateiformat für den Import aus"),true);

    Input formats = getImporterList();
		group.addLabelPair(i18n.tr("Verfügbare Formate:"),formats);

		ButtonArea buttons = new ButtonArea();
		Button button = new Button(i18n.tr("Import starten"),new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				doImport();
			}
		},null,true,"ok.png");
    button.setEnabled(!(formats instanceof LabelInput));
    buttons.addButton(button);
		buttons.addButton(i18n.tr("Abbrechen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				throw new OperationCanceledException();
			}
		},null,false,"process-stop.png");
		group.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
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

    settings.setAttribute("lastformat",imp.format.getName());

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

    this.task = new BackgroundTask()
    {
      private boolean interrupted = false;
      
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
       */
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          InputStream is = new BufferedInputStream(new FileInputStream(file));
          importer.doImport(context,format,is,monitor,task);
          monitor.setPercentComplete(100);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          GUI.getStatusBar().setSuccessText(i18n.tr("Daten importiert aus {0}",s));
          GUI.getCurrentView().reload();
        }
        catch (OperationCanceledException oce)
        {
          throw oce;
        }
        catch (ApplicationException ae)
        {
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

      /**
       * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
       */
      public void interrupt()
      {
        this.interrupted = true;
      }
      
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
       */
      public boolean isInterrupted()
      {
        return this.interrupted;
      }
    };

    Application.getController().start(task);
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

    int size          = 0;
    ArrayList<Imp> l  = new ArrayList<>();
    String lastFormat = settings.getString("lastformat",null);
    Imp selected      = null;

    for (Importer imp : importers)
    {
      if (imp == null)
        continue;
      IOFormat[] formats = imp.getIOFormats(type);
      if (formats == null || formats.length == 0)
      {
        Logger.debug("importer " + imp.getName() + " provides no import formats for " + type.getName() + ", skipping");
        continue;
      }
      for (IOFormat format : formats)
      {
        size++;
        Imp im = new Imp(imp, format);
        l.add(im);

        String lf = im.format.getName();
        if (lastFormat != null && lf != null && lf.equals(lastFormat))
          selected = im;
      }
    }

		if (size == 0)
		{
			importerListe = new LabelInput(i18n.tr("Keine Import-Filter verfügbar"));
			return importerListe;
		}

    Collections.sort(l);
		Imp[] imp = (Imp[]) l.toArray(new Imp[size]);
		importerListe = new SelectInput(PseudoIterator.fromArray(imp),selected);
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
  private class Imp implements GenericObject, Comparable
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

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
      if (o == null || !(o instanceof Imp))
        return -1;
      try
      {
        return this.format.getName().compareTo(((Imp)o).format.getName());
      }
      catch (Exception e)
      {
        // Tss, dann halt nicht
      }
      return 0;
    }
	}
}
