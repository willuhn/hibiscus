/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/NewKeysDialog.java,v $
 * $Revision: 1.11 $
 * $Date: 2008/07/25 11:06:44 $
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
import java.rmi.RemoteException;
import java.util.ArrayList;
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

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
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

	private final static DocFlavor DOCFLAVOR = DocFlavor.STRING.TEXT_PLAIN;
	private final static PrintRequestAttributeSet PRINTPROPS = new HashPrintRequestAttributeSet();

	private HBCIPassport passport;
	private INILetter iniletter;
	private I18N i18n;
	
	private Input printerList = null;

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
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
		setTitle(i18n.tr("Ini-Brief erzeugen"));


		this.passport = p;
		iniletter = new INILetter(passport,INILetter.TYPE_USER);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
		LabelGroup group = new LabelGroup(parent,i18n.tr("Ini-Brief"));
		group.addText(i18n.tr(
      "Bitte drucken Sie den Ini-Brief aus und senden Ihn an Ihre Bank.\n" +      "Nach der Freischaltung durch Ihr Geldinstitut kann dieser Schlüssel\n" +      "verwendet werden."),true);

    Input printers = getPrinterList();
    
		group.addLabelPair(i18n.tr("Schlüssel-Hashwert"),new LabelInput(HBCIUtils.data2hex(iniletter.getKeyHash())));
		group.addLabelPair(i18n.tr("Drucker-Auswahl:"),printers);

    Button print = new Button(i18n.tr("Drucken"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        print();
      }
    },null,true);
    print.setEnabled((printers instanceof SelectInput)); // Drucken nur moeglich, wenn Drucker vorhanden.
		ButtonArea buttons = new ButtonArea(parent,3);
		buttons.addButton(print);
    buttons.addButton(i18n.tr("Speichern unter..."),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        save();
      }
    });
		buttons.addButton(i18n.tr("Schließen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				close();
			}
		});
  }

	/**
	 * Druckt den Ini-Brief aus.
   * @throws ApplicationException
   */
  private void print() throws ApplicationException
	{
		try
		{
			Printer p = (Printer) getPrinterList().getValue();
			if (p == null)
				throw new ApplicationException(i18n.tr("Kein Drucker gefunden."));

			PrintService service = p.service;

			DocPrintJob pj = service.createPrintJob();

			// BUGZILLA 24 http://www.willuhn.de/bugzilla/show_bug.cgi?id=24
			Doc doc = new SimpleDoc(iniletter.toString(),DOCFLAVOR,null);
			pj.print(doc,PRINTPROPS);
		}
		catch (Exception e)
		{
      Logger.error("error while printing ini letter",e);
			throw new ApplicationException(i18n.tr("Fehler beim Drucken des Ini-Briefs"),e);
		}
	}

  /**
   * Speichert den Ini-Brief ab.
   * @throws ApplicationException
   */
  private void save() throws ApplicationException
  {
    FileDialog fd = new FileDialog(GUI.getShell(),SWT.SAVE);
    fd.setText(i18n.tr("Bitte geben Sie den Dateinamen an , in dem der Ini-Brief gespeichert werden soll"));
    fd.setFileName(i18n.tr("hibiscus-inibrief-{0}.txt",HBCI.FASTDATEFORMAT.format(new Date())));
    
    Settings settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
    String path = settings.getString("lastdir",System.getProperty("user.home"));
    if (path != null && path.length() > 0)
      fd.setFilterPath(path);

    String s = fd.open();
    
    if (s == null || s.length() == 0)
    {
      close();
      return;
    }

    OutputStream os = null;
    try
    {
      File file = new File(s);
      String overwrite = i18n.tr("Die Datei {0} existiert bereits. Überschreiben?");
      if (file.exists() && !Application.getCallback().askUser(overwrite,new String[]{file.getAbsolutePath()}))
        throw new OperationCanceledException("interrupted, user did not want to overwrite " + file.getAbsolutePath());

      os = new BufferedOutputStream(new FileOutputStream(file));
      os.write(iniletter.toString().getBytes());

      // Wir merken uns noch das Verzeichnis vom letzten mal
      settings.setAttribute("lastdir",file.getParent());

      // Dialog schliessen
      close();
      GUI.getStatusBar().setSuccessText(i18n.tr("INI-Brief gespeichert in {0}",s));
    }
    catch (OperationCanceledException oce)
    {
      Logger.warn(oce.getMessage());
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while writing ini letter to " + s,e);
      throw new ApplicationException(i18n.tr("Fehler beim Speichern des INI-Briefs in {0}",s),e);
    }
    finally
    {
      if (os != null)
      {
        try
        {
          // Dialog schliessen
          close();
          os.close();
        }
        catch (Exception e)
        {
          // useless
        }
      }
    }
  }

	/**
	 * Liefert eine Liste der verfuegbaren Drucker.
   * @return Liste der Drucker.
	 * @throws Exception
   */
  private Input getPrinterList() throws Exception
	{
		if (printerList != null)
			return printerList;

		ArrayList l = new ArrayList();

		PrintService[] service = PrintServiceLookup.lookupPrintServices(DOCFLAVOR,PRINTPROPS);
		for (int i=0;i<service.length;++i)
		{
			l.add(new Printer(service[i]));
		}

		if (l.size() == 0)
		{
			printerList = new LabelInput(i18n.tr("Kein Drucker verfügbar"));
			return printerList;
		}

		this.printerList = new SelectInput(l,null);
		return this.printerList;
	}

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

	/**
	 * Hilfsklasse zur Anzeige der Drucker.
   */
  private class Printer implements GenericObject
	{
		private PrintService service = null;
		
		private Printer(PrintService service)
		{
			this.service = service;
		}

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      return this.service.getName();
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
 * $Log: NewKeysDialog.java,v $
 * Revision 1.11  2008/07/25 11:06:44  willuhn
 * @N Auswahl-Dialog fuer HBCI-Version
 * @N Code-Cleanup
 *
 * Revision 1.10  2008/07/15 11:18:12  willuhn
 * @B Druck-Button deaktivieren, wenn keine Drucker gefunden
 *
 * Revision 1.9  2008/04/15 16:16:34  willuhn
 * @B BUGZILLA 584
 *
 * Revision 1.8  2005/06/02 22:57:34  web0
 * @N Export von Konto-Umsaetzen
 *
 * Revision 1.7  2005/03/21 23:31:54  web0
 * @B bug 24
 *
 * Revision 1.6  2005/03/09 01:07:02  web0
 * @D javadoc fixes
 *
 * Revision 1.5  2005/03/05 19:11:25  web0
 * @N SammelLastschrift-Code complete
 *
 * Revision 1.4  2005/02/03 23:57:05  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2005/02/03 18:57:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2005/02/02 18:19:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2005/02/02 16:15:52  willuhn
 * @N Neue Dialoge fuer RDH
 *
 **********************************************************************/