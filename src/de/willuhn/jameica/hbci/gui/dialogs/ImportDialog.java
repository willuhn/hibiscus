/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/ImportDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/01/18 00:51:01 $
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
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

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
		group.addText(i18n.tr("Bitte wählen Sie das gewünschte Dateiformat aus für den Import aus"),true);

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
		buttons.addButton(i18n.tr("Schliessen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				close();
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
    fd.setFilterNames(new String[]{"*." + imp.format.getFileExtension()});

    String path = settings.getString("lastdir",System.getProperty("user.home"));
    if (path != null && path.length() > 0)
      fd.setFilterPath(path);

    String s = fd.open();
    

    if (s == null || s.length() == 0)
    {
      close();
      return;
    }

    File file = new File(s);
    if (!file.exists() || !file.isFile())
      throw new ApplicationException(i18n.tr("Datei existiert nicht oder ist nicht lesbar"));
    
    // Wir merken uns noch das Verzeichnis vom letzten mal
    settings.setAttribute("lastdir",file.getParent());

    try
    {
      Importer importer = imp.importer;

      InputStream is = new BufferedInputStream(new FileInputStream(file));
      importer.doImport(context,imp.format,is);

      // Dialog schliessen
      close();
      GUI.getStatusBar().setSuccessText(i18n.tr("Daten importiert aus {0}",s));
    }
    catch (Exception e)
    {
      Logger.error("error while reading objects from " + s,e);
      throw new ApplicationException(i18n.tr("Fehler beim Importieren der Daten aus {0}",s),e);
    }
    finally
    {
      // Dialog schliessem
      close();
    }
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
 * Revision 1.1  2006/01/18 00:51:01  willuhn
 * @B bug 65
 *
 **********************************************************************/