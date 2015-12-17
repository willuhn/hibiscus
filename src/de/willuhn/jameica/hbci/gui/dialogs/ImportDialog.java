/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/ImportDialog.java,v $
 * $Revision: 1.16 $
 * $Date: 2011/05/03 16:44:23 $
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
import de.willuhn.jameica.gui.input.CheckboxInput;
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
  private CheckboxInput forceBox  = null;
  private GenericObject context   = null;	
  private Class type              = null;
  
  private Settings  settings      = null;

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
		group.addText(i18n.tr("Bitte w�hlen Sie das gew�nschte Dateiformat f�r den Import aus"),true);

    Input formats = getImporterList();
		group.addLabelPair(i18n.tr("Verf�gbare Formate:"),formats);

    if (this.forceBox == null) {
      String newName = i18n.tr("Import erzwingen");
      this.forceBox = new CheckboxInput(false);
      this.forceBox.setName(newName);
    }
    group.addInput(this.forceBox);

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
      throw new ApplicationException(i18n.tr("Bitte w�hlen Sie ein Import-Format aus"));

    settings.setAttribute("lastformat",imp.format.getName());

    final boolean useForce = ((Boolean)this.forceBox.getValue()).booleanValue();

    FileDialog fd = new FileDialog(GUI.getShell(),SWT.OPEN);
    fd.setText(i18n.tr("Bitte w�hlen Sie die Datei aus, welche f�r den Import verwendet werden soll."));
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
          importer.doImport(context,format,is,monitor,useForce);
          monitor.setPercentComplete(100);
          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          GUI.getStatusBar().setSuccessText(i18n.tr("Daten importiert aus {0}",s));
          GUI.getCurrentView().reload();
        }
        catch (ApplicationException ae)
        {
          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          monitor.setStatusText(ae.getMessage());
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

    int size          = 0;
    ArrayList l       = new ArrayList();
    String lastFormat = settings.getString("lastformat",null);
    Imp selected      = null;

    for (int i=0;i<importers.length;++i)
		{
      Importer imp = importers[i];
      if (imp == null)
        continue;
      IOFormat[] formats = imp.getIOFormats(type);
      if (formats == null || formats.length == 0)
      {
        Logger.debug("importer " + imp.getName() + " provides no import formats for " + type.getName() + ", skipping");
        continue;
      }
      for (int j=0;j<formats.length;++j)
      {
        size++;
        Imp im = new Imp(imp,formats[j]);
        l.add(im);

        String lf = im.format.getName();
        if (lastFormat != null && lf != null && lf.equals(lastFormat))
          selected = im;
      }
		}

		if (size == 0)
		{
			importerListe = new LabelInput(i18n.tr("Keine Import-Filter verf�gbar"));
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


/**********************************************************************
 * $Log: ImportDialog.java,v $
 * Revision 1.16  2011/05/03 16:44:23  willuhn
 * @C GUI cleanup
 *
 * Revision 1.15  2011-01-12 18:23:04  willuhn
 * @N Letztes ausgewaehltes Import-Format merken
 *
 * Revision 1.14  2011-01-12 17:54:08  willuhn
 * @C Format-Namen sortieren
 *
 * Revision 1.13  2011-01-12 17:53:05  willuhn
 * @C Format-Namen sortieren
 *
 * Revision 1.12  2010/04/25 21:01:46  willuhn
 * @B BUGZILLA 851
 *
 * Revision 1.11  2010/03/16 00:44:18  willuhn
 * @N Komplettes Redesign des CSV-Imports.
 *   - Kann nun erheblich einfacher auch fuer andere Datentypen (z.Bsp.Ueberweisungen) verwendet werden
 *   - Fehlertoleranter
 *   - Mehrfachzuordnung von Spalten (z.Bsp. bei erweitertem Verwendungszweck) moeglich
 *   - modulare Deserialisierung der Werte
 *   - CSV-Exports von Hibiscus koennen nun 1:1 auch wieder importiert werden (Import-Preset identisch mit Export-Format)
 *   - Import-Preset wird nun im XML-Format nach ~/.jameica/hibiscus/csv serialisiert. Damit wird es kuenftig moeglich sein,
 *     CSV-Import-Profile vorzukonfigurieren und anschliessend zu exportieren, um sie mit anderen Usern teilen zu koennen
 *
 * Revision 1.10  2006/08/07 21:51:43  willuhn
 * @N Erste Version des DTAUS-Exporters
 *
 * Revision 1.9  2006/08/07 14:45:18  willuhn
 * @B typos
 *
 * Revision 1.8  2006/06/08 22:29:47  willuhn
 * @N DTAUS-Import fuer Sammel-Lastschriften und Sammel-Ueberweisungen
 * @B Eine Reihe kleinerer Bugfixes in Sammeltransfers
 * @B Bug 197 besser geloest
 *
 * Revision 1.7  2006/05/25 13:47:03  willuhn
 * @N Skeleton for DTAUS-Import
 *
 * Revision 1.6  2006/04/21 09:26:35  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2006/04/20 08:44:21  willuhn
 * @C s/Childs/Children/
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
