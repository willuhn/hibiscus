/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/UmsatzImportControl.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/05/09 17:39:49 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.PluginLoader;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.controller.AbstractControl;
import de.willuhn.jameica.gui.parts.TextPart;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.util.I18N;

/**
 */
public class UmsatzImportControl extends AbstractControl {

	private I18N i18n;

	private TextPart log		= null;
	private boolean running = false;
	private Process process = null;

  /**
   * ct.
   * @param view
   */
  public UmsatzImportControl(AbstractView view) {
    super(view);
    i18n = PluginLoader.getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liefert ein Text-Feld mit dem Log.
	 * @return Text-Feld.
	 */
	public TextPart getLog()
	{
		if (log != null)
			return log;
		log = new TextPart();
		log.setWordWrap(false);
		log.setAutoscroll(true);
		return log;
	}

	/**
	 * Prueft, ob die Synchronisierung gerade laeuft.
	 * @return true, wenn sie laeuft.
	 */
	public synchronized boolean isRunning()
	{
		return running;  
	}

	/**
   * Startet den Import der Buchungen.
   */
  public void start()
	{
		final String program = Settings.getImportProgram();
		if (program == null)
		{
			GUI.getView().setErrorText(i18n.tr("Kein Import-Programm definiert"));
			return;
		}

		GUI.getStatusBar().setStatusText(i18n.tr("Starte Import..."));
		GUI.getStatusBar().startProgress();
		GUI.startSync(new Runnable() {
      public void run() {
      	running = true;
      	getLog().clear();
				log("\n" + i18n.tr("[START]") + " " + HBCI.LONGDATEFORMAT.format(new Date()));
				try {
					process = Runtime.getRuntime().exec(program);
					BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String currentLine = null;
					while ((currentLine = br.readLine()) != null)
					{
						log(currentLine);
					}
					br.close();
					GUI.getStatusBar().setSuccessText(i18n.tr("...Import beendet"));
				}
				catch (Throwable t)
				{
					Application.getLog().error("error while executing import programm",t);
					GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Ausführen des Programms"));
				}
				finally
				{
					running = false;
					log("\n" + i18n.tr("[ENDE]") + " " + HBCI.LONGDATEFORMAT.format(new Date()));
				}
    	}
    });
		GUI.getStatusBar().stopProgress();
	}

	/**
	 * Schreibt den uebergeben Text in das Log auf der GUI.
	 * @param s anzuzeigender Text.
	 */
	public void log(final String s)
	{
		GUI.getDisplay().syncExec(new Runnable() {
			public void run() {
				getLog().appendText(s + "\n");
			}
		});
	}

	/**
	 * Bricht die Synchronisierung ab.
	 */
	public synchronized void cancel()
	{
		try {
			process.destroy();
		}
		catch (Throwable t)
		{
			Application.getLog().error("error while destroying import process",t);
		}

		log("\n" + i18n.tr("[Abbruch]") + " " + HBCI.LONGDATEFORMAT.format(new Date())); // make log empty
		GUI.getStatusBar().stopProgress();
		GUI.getStatusBar().setErrorText(i18n.tr("Import abgebrochen"));
		running = false;
	}

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleDelete()
   */
  public void handleDelete() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCancel()
   */
  public void handleCancel() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleStore()
   */
  public void handleStore() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleCreate()
   */
  public void handleCreate() {
  }

  /**
   * @see de.willuhn.jameica.gui.controller.AbstractControl#handleOpen(java.lang.Object)
   */
  public void handleOpen(Object o) {
  }

}


/**********************************************************************
 * $Log: UmsatzImportControl.java,v $
 * Revision 1.1  2004/05/09 17:39:49  willuhn
 * *** empty log message ***
 *
 **********************************************************************/