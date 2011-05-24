/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/NewKeysDialog.java,v $
 * $Revision: 1.17 $
 * $Date: 2011/05/24 09:06:11 $
 * $Author: willuhn $
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
import java.util.Date;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.INILetter;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Platform;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog, der den neu erzeugten Schluessel anzeigt und den Benutzer
 * auffordert, den Ini-Brief an seine Bank zu senden.
 */
public class NewKeysDialog extends AbstractDialog
{
  private final static Settings settings = new Settings(NewKeysDialog.class);

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static int WINDOW_WIDTH = 540;

	private final static DocFlavor DOCFLAVOR = DocFlavor.STRING.TEXT_PLAIN;
	private final static PrintRequestAttributeSet PRINTPROPS = new HashPrintRequestAttributeSet();

	private HBCIPassport passport;
	private INILetter iniletter;
	
	private Input printerList = null;
	private LabelInput error = null;

	static
	{
		PRINTPROPS.add(MediaSizeName.ISO_A4);
	}

  /**
   * @param p
   */
  public NewKeysDialog(HBCIPassport p)
  {
    super(NewKeysDialog.POSITION_CENTER);
    this.passport = p;

		setTitle(i18n.tr("INI-Brief erzeugen/anzeigen"));
    setSize(WINDOW_WIDTH,SWT.DEFAULT);

		iniletter = new INILetter(passport,INILetter.TYPE_USER);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#onEscape()
   */
  protected void onEscape()
  {
    // Kein Escape in diesem Dialog
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
		SimpleContainer group = new SimpleContainer(parent);
		group.addText(i18n.tr("Bitte drucken Sie den INI-Brief aus und senden Ihn unterschrieben an Ihre Bank.\n" +                          "Nach der Freischaltung durch Ihr Geldinstitut kann dieser Schlüssel verwendet werden."),true);

    group.addHeadline(i18n.tr("Hashwert"));
    group.addText(HBCIUtils.data2hex(iniletter.getKeyHashDisplay()).toUpperCase(),true,Color.ERROR);
    
    group.addHeadline(i18n.tr("Exponent"));
    group.addText(HBCIUtils.data2hex(iniletter.getKeyExponentDisplay()).toUpperCase(),true);

    group.addHeadline(i18n.tr("Modulus"));
    group.addText(HBCIUtils.data2hex(iniletter.getKeyModulusDisplay()).toUpperCase(),true);

    Input printers = getPrinterList();
    group.addText("\n",true);
    group.addInput(printers);
    group.addInput(getError());
    
    ButtonArea buttons = new ButtonArea();
    Button print = new Button(i18n.tr("Drucken"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        print();
        close();
      }
    },null,true,"document-print.png");
    print.setEnabled((printers instanceof SelectInput)); // Drucken nur moeglich, wenn Drucker vorhanden.
		buttons.addButton(print);
		
    buttons.addButton(i18n.tr("Speichern unter..."),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        save();
      }
    },null,false,"document-save.png");
		buttons.addButton(i18n.tr("Abbrechen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				throw new OperationCanceledException("cancelled in ini letter dialog");
			}
		},null,false,"process-stop.png");
		group.addButtonArea(buttons);
		
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }

	/**
	 * Druckt den Ini-Brief aus.
   * @throws ApplicationException
   */
  private void print() throws ApplicationException
	{
		try
		{
		  PrintService service = (PrintService) getPrinterList().getValue();
			if (service == null)
			{
	      getError().setValue(i18n.tr("Kein Drucker gefunden."));
	      return;
			}

			DocPrintJob pj = service.createPrintJob();

			// BUGZILLA 24 http://www.willuhn.de/bugzilla/show_bug.cgi?id=24
			Doc doc = new SimpleDoc(iniletter.toString(),DOCFLAVOR,null);
			pj.print(doc,PRINTPROPS);
		}
		catch (Exception e)
		{
      Logger.error("error while printing ini letter",e);
      getError().setValue(i18n.tr("Fehler: {0}",e.getMessage()));
		}
	}

  /**
   * Speichert den Ini-Brief ab.
   */
  private void save()
  {
    FileDialog fd = new FileDialog(GUI.getShell(),SWT.SAVE);
    fd.setText(i18n.tr("Bitte geben Sie den Dateinamen an, in dem der INI-Brief gespeichert werden soll."));
    fd.setFileName(i18n.tr("hibiscus-inibrief-{0}.txt",HBCI.FASTDATEFORMAT.format(new Date())));
    fd.setOverwrite(true);
    
    String path = settings.getString("lastdir",System.getProperty("user.home"));
    if (path != null && path.length() > 0)
      fd.setFilterPath(path);

    String s = fd.open();
    
    if (s == null || s.length() == 0)
    {
      getError().setValue(i18n.tr("Bitte wählen Sie eine Datei für den INI-Brief aus."));
      return;
    }

    OutputStream os = null;
    try
    {
      File file = new File(s);
      os = new BufferedOutputStream(new FileOutputStream(file));
      os.write(iniletter.toString().getBytes());

      // Wir merken uns noch das Verzeichnis vom letzten mal
      settings.setAttribute("lastdir",file.getParent());
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("INI-Brief gespeichert in {0}",s),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      Logger.error("error while writing ini letter to " + s,e);
      getError().setValue(i18n.tr("Fehler: {0}",e.getMessage()));
    }
    finally
    {
      IOUtil.close(os);
    }
  }

	/**
	 * Liefert eine Liste der verfuegbaren Drucker.
   * @return Liste der Drucker.
	 * @throws Exception
   */
  private Input getPrinterList() throws Exception
	{
		if (this.printerList != null)
			return this.printerList;

		PrintService[] services = null;

		// Unter MacOS bleibt die Anwendung hier stehen.
		if (Application.getPlatform().getOS() != Platform.OS_MAC)
	    services = PrintServiceLookup.lookupPrintServices(DOCFLAVOR,PRINTPROPS);

    if (services != null && services.length > 0)
    {
      this.printerList = new SelectInput(services,null);
      ((SelectInput)this.printerList).setAttribute("name");
    }
    else
    {
      printerList = new LabelInput(i18n.tr("Kein Drucker gefunden"));
      getError().setValue(i18n.tr("Bitte speichern Sie den INI-Brief stattdessen."));
		}
    
    printerList.setName(i18n.tr("Drucker"));
		return this.printerList;
	}
  
  /**
   * Liefert ein Label fuer Fehlermeldungen.
   * @return Label fuer Fehlermeldungen.
   */
  private LabelInput getError()
  {
    if (this.error != null)
      return this.error;
    
    this.error = new LabelInput("");
    this.error.setColor(Color.ERROR);
    this.error.setName("");
    return this.error;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }
}


/**********************************************************************
 * $Log: NewKeysDialog.java,v $
 * Revision 1.17  2011/05/24 09:06:11  willuhn
 * @C Refactoring und Vereinfachung von HBCI-Callbacks
 *
 * Revision 1.16  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.15  2010/06/08 11:27:59  willuhn
 * @N SWT besitzt jetzt selbst eine Option im FileDialog, mit der geprueft werden kann, ob die Datei ueberschrieben werden soll oder nicht
 **********************************************************************/