/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/Controller.java,v $
 * $Revision: 1.6 $
 * $Date: 2010/07/13 11:36:52 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.ddv;

import java.io.File;
import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.HBCIPassportDDV;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.ListDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.AccountContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.PassportTest;
import de.willuhn.jameica.hbci.gui.dialogs.AccountContainerDialog;
import de.willuhn.jameica.hbci.gui.input.HBCIVersionInput;
import de.willuhn.jameica.hbci.passport.PassportHandle;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Passport;
import de.willuhn.jameica.hbci.passports.ddv.rmi.Reader;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Controller, der die Eingaben zur Konfiguration des Passports handelt.
 */
public class Controller extends AbstractControl
{

  // Fachobjekte
  private Passport passport = null;

  private GenericIterator readers = null;

  // Eingabe-Felder
  private Input name                = null;
  private SelectInput port          = null;
  private Input ctNumber            = null;
  private Input entryIndex          = null;
  private FileInput ctapi           = null;
  private CheckboxInput useBio      = null;
  private CheckboxInput useSoftPin  = null;
  private DialogInput readerPresets = null;
  private SelectInput hbciVersion   = null;
  private I18N i18n;

  /**
   * ct.
   * 
   * @param view
   */
  public Controller(AbstractView view)
  {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources()
        .getI18N();
  }

  /**
   * Liefert den Passport.
   * 
   * @return Passport.
   */
  public Passport getPassport()
  {
    if (passport != null)
      return passport;

    // Wir gehen einfach mal davon aus, dass wir den auf DDV
    // casten koennen. Waere es kein DDV, wuerden wir ja nicht
    // aufgerufen worden sein ;)
    passport = (Passport) getCurrentObject();

    return passport;
  }

  /**
   * Liefert eine Auswahl-Box fuer die HBCI-Version.
   * 
   * @return Auswahl-Box.
   * @throws RemoteException
   */
  public SelectInput getHBCIVersion() throws RemoteException
  {
    if (hbciVersion != null)
      return hbciVersion;
    try
    {
      String current = getPassport().getHBCIVersion();
      hbciVersion = new HBCIVersionInput(null,current);
      return hbciVersion;
    }
    catch (Exception e)
    {
      Logger.error("error while loading hbci versions from key", e);
      throw new RemoteException(i18n.tr("Fehler beim Lesen der unterstützten HBCI-Versionen"), e);
    }
  }

  /**
   * Liefert eine Auswahl von vorkonfigurierten und bereits bekannten
   * Chipkartenlesern.
   * 
   * @return Auswahl von vorkonfigurierten Lesern.
   * @throws RemoteException
   */
  public DialogInput getReaderPresets() throws RemoteException
  {
    if (readerPresets != null)
      return readerPresets;
    String name = i18n.tr("unbekannter Kartenleser");
    try
    {
      name = getPassport().getReaderPresets().getName();
    }
    catch (Throwable t)
    {
      Logger.error("unable to determine name of reader preset - you can ignore this error message",t);
    }
    ListDialog d = new ListDialog(getReaders(), ListDialog.POSITION_MOUSE);
    d.setTitle(i18n.tr("Vorkonfigurierte Leser"));
    d.addColumn(i18n.tr("Bezeichnung"), "name");
    d.addCloseListener(new PresetListener());

    readerPresets = new DialogInput(name, d);
    readerPresets.disableClientControl();
    return readerPresets;
  }

  /**
   * Liefert einen Iterator mit bekannten Readern.
   * 
   * @return Iterator mit bekannten Readern.
   * @throws RemoteException
   */
  private GenericIterator getReaders() throws RemoteException
  {
    if (readers != null)
      return readers;

    GenericObject[] list;
    try
    {
      Logger.info("searching for reader presets");
      Class[] found = Application.getClassLoader().getClassFinder().findImplementors(Reader.class);
      list = new GenericObject[found.length];
      for (int i = 0; i < found.length; ++i)
      {
        list[i] = (GenericObject) found[i].newInstance();
        Logger.info("  found " + found[i].getName());
      }
    }
    catch (Throwable t)
    {
      Logger.error("unable to find reader presets");
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ermittlen der vorkonfigurierten Kartenleser"),StatusBarMessage.TYPE_ERROR));
      list = new Reader[0];
    }
    readers = PseudoIterator.fromArray(list);
    return readers;
  }

  /**
   * Liefert eine Detai-Auswahl fuer den CTAPI-Treiber.
   * 
   * @return Auswahl-Feld.
   * @throws RemoteException
   */
  public Input getCTAPI() throws RemoteException
  {
    if (ctapi != null)
      return ctapi;
    ctapi = new FileInput(getPassport().getCTAPIDriver());
    ctapi.disableClientControl();
    return ctapi;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Port.
   * 
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public SelectInput getPort() throws RemoteException
  {
    if (port != null)
      return port;

    port = new SelectInput(Passport.PORTS, getPassport().getPort());
    port.setComment(i18n.tr("meist COM1 oder USB"));
    return port;
  }

  /**
   * Liefert das Eingabe-Feld fuer die Nummer des Lesers.
   * 
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getCTNumber() throws RemoteException
  {
    if (ctNumber != null)
      return ctNumber;

    ctNumber = new TextInput("" + getPassport().getCTNumber());
    ctNumber.setComment(i18n.tr("meist 0"));
    return ctNumber;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Namen des Lesers.
   * 
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getName() throws RemoteException
  {
    if (name != null)
      return name;

    name = new LabelInput(getPassport().getName());
    return name;
  }

  /**
   * Liefert das Eingabe-Feld fuer die Index-Nummer des HBCI-Zugangs.
   * 
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getEntryIndex() throws RemoteException
  {
    if (entryIndex != null)
      return entryIndex;

    entryIndex = new TextInput("" + getPassport().getEntryIndex());
    entryIndex.setComment(i18n.tr("meist 1"));
    return entryIndex;
  }

  /**
   * Liefert die Checkbox fuer die Auswahl biometrischer Verfahren.
   * 
   * @return Checkbox.
   * @throws RemoteException
   */
  public CheckboxInput getBio() throws RemoteException
  {
    if (useBio != null)
      return useBio;

    useBio = new CheckboxInput(getPassport().useBIO());
    return useBio;
  }

  /**
   * Liefert die Checkbox fuer die Auswahl der Tastatur als PIN-Eingabe.
   * 
   * @return Checkbox.
   * @throws RemoteException
   */
  public CheckboxInput getSoftPin() throws RemoteException
  {
    if (useSoftPin != null)
      return useSoftPin;

    useSoftPin = new CheckboxInput(getPassport().useSoftPin());
    return useSoftPin;
  }
  
  /**
   * Versucht, den Kartenleser automatisch zu ermitteln.
   */
  public void handleScan()
  {
    String ask = i18n.tr("Legen Sie Ihre HBCI-Chipkarte vor dem Test in das Lesegerät.\nBereits vorgenommene Einstellungen gehen hierbei verloren.\n\nDer Test kann einige Minuten in Anspruch nehmen. Vorgang fortsetzen?");
    
    try
    {
      if (!Application.getCallback().askUser(ask))
        return;
    }
    catch (Exception e)
    {
      Logger.error("unable to open confirm dialog",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim automatischen Erkennen des Kartenlesers"),StatusBarMessage.TYPE_ERROR));
      return;
    }

    BackgroundTask task = new BackgroundTask() {

      private boolean cancel = false;
      
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
       */
      public void run(final ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          GenericIterator list = getReaders();
          int factor = 100 / (list.size() * Passport.PORTS.length);
          
          while (list.hasNext())
          {
            final Reader reader = (Reader) list.next();

            monitor.setStatusText(i18n.tr("Teste {0}",reader.getName()));

            // Testen, ob der Kartenleser ueberhaupt unterstuetzt wird
            if (!reader.isSupported())
            {
              monitor.log("  " + i18n.tr("überspringe Kartenleser, wird von Ihrem System nicht unterstützt"));
              continue;
            }

            // Checken, ob der CTAPI-Treiber existiert
            final String s = reader.getCTAPIDriver();
            if (s != null && s.length() > 0)
            {
              File f = new File(s);
              if (!f.exists())
              {
                monitor.log("  " + i18n.tr("überspringe Kartenleser, CTAPI-Treiber {0} existiert nicht.",f.getAbsolutePath()));
                continue;
              }
            }
            else
            {
              monitor.log("  " + i18n.tr("überspringe Kartenleser, kein CTAPI-Treiber definiert."));
              continue;
            }

            final int ctNumber = reader.getCTNumber();
            getPassport().setCTNumber(ctNumber == -1 ? 0 : ctNumber);
            getPassport().setEntryIndex(1);
            getPassport().setReaderPresets(reader);
            getPassport().setBIO(reader.useBIO());
            getPassport().setSoftPin(reader.useSoftPin());
            getPassport().setCTAPIDriver(s);
            getPassport().setHBCIVersion("210");

            for (int i=0;i<Passport.PORTS.length;++i)
            {
              monitor.addPercentComplete(factor);
              final String port = Passport.PORTS[i];
              monitor.log("  " + i18n.tr("Port {0}",port));
              
              getPassport().setPort(port);

              try
              {
                PassportHandle handle = getPassport().getHandle();
                handle.open();
                handle.close(); // nein, nicht im finally, denn wenn das Oeffnen
                                // fehlschlaegt, ist nichts zum Schliessen da ;)

                GUI.getDisplay().asyncExec(new Runnable() {
                  public void run()
                  {
                    try
                    {
                      getReaderPresets().setValue(reader);
                      getReaderPresets().setText(reader.getName());

                      int ctNumber = reader.getCTNumber();
                      getCTNumber().setValue(new Integer(ctNumber == -1 ? 0 : ctNumber));
                      
                      getPort().setPreselected(port);
                      getPort().setValue(port);

                      getHBCIVersion().setValue("210");
                      getHBCIVersion().setPreselected("210");

                      getCTAPI().setValue(s);
                      getBio().setValue(new Boolean(reader.useBIO()));
                      getSoftPin().setValue(new Boolean(reader.useSoftPin()));
                      getEntryIndex().setValue(new Integer(1));
                    }
                    catch (RemoteException re2)
                    {
                      Logger.error("unable to apply settings",re2);
                      monitor.log("  " + i18n.tr("Fehler beim Übernehmen der Einstellungen"));
                    }
                  }
                
                });
                
                monitor.setStatusText(i18n.tr("OK. Kartenleser gefunden"));
                monitor.setStatus(ProgressMonitor.STATUS_DONE);
                monitor.setPercentComplete(100);
                return;
              }
              catch (ApplicationException ae)
              {
                monitor.log("  " + ae.getMessage());
              }
              catch (Exception e)
              {
                monitor.log("  " + i18n.tr("  nicht gefunden"));
              }
            }
          }
          throw new ApplicationException(i18n.tr("Kein Kartenleser gefunden. Bitte manuell konfigurieren"));
        }
        catch (RemoteException re)
        {
          Logger.error("unable to determine reader presets",re);
          throw new ApplicationException(i18n.tr("Fehler beim automatischen Erkennen des Kartenlesers"));
        }
      }
    
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
       */
      public boolean isInterrupted()
      {
        return cancel;
      }
    
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
       */
      public void interrupt()
      {
        this.cancel = true;
      }
    };
    Application.getController().start(task);
  }

  /**
   * Speichert die Einstellungen.
   */
  public void handleStore()
  {
    stored = false;
    try
    {
      try
      {
        getPassport().setCTNumber(Integer.parseInt((String) getCTNumber().getValue()));
      }
      catch (NumberFormatException e)
      {
        GUI.getView().setErrorText(i18n.tr("Bitte geben Sie im Feld \"Nummer des Chipkartenlesers\" eine gültige Zahl ein."));
        return;
      }

      try
      {
        getPassport().setEntryIndex(Integer.parseInt((String) getEntryIndex().getValue()));
      }
      catch (NumberFormatException e)
      {
        GUI.getView().setErrorText(i18n.tr("Bitte geben Sie im Feld \"Index des HBCI-Zugangs\" eine gültige Zahl ein."));
        return;
      }

      try
      {
        getPassport().setReaderPresets((Reader) getReaderPresets().getValue());
      }
      catch (Throwable t)
      {
        // Wenn das schiefgeht, dann steht beim naechsten Oeffnen des Dialogs
        // halt nicht mehr, auf welchem Preset die Einstellungen basieren - who
        // cares ;)
        Logger.error("error while saving name of reader preset - you can ignore this error message",t);
      }

      getPassport().setPort((String) getPort().getValue());
      getPassport().setBIO(((Boolean) getBio().getValue()).booleanValue());
      getPassport().setSoftPin(((Boolean) getSoftPin().getValue()).booleanValue());
      getPassport().setCTAPIDriver((String) getCTAPI().getValue());
      getPassport().setHBCIVersion((String) getHBCIVersion().getValue());

      GUI.getStatusBar().setSuccessText(i18n.tr("Einstellungen gespeichert"));
      stored = true;
    }
    catch (RemoteException e)
    {
      Logger.error("error while storing params", e);
      GUI.getStatusBar().setErrorText(
          i18n.tr("Fehler beim Speichern der Einstellungen"));
    }
  }

  private boolean stored = false;

  /**
   * Testet die Einstellungen.
   */
  public void handleTest()
  {

    // Speichern, damit sicher ist, dass wir vernuenftige Daten fuer den
    // Test haben und die auch gespeichert sind
    handleStore();
    if (!stored)
      return;

    try
    {
      new PassportTest().handleAction(getPassport());
    }
    catch (ApplicationException ae)
    {
      GUI.getStatusBar().setErrorText(ae.getMessage());
    }
  }
  
  /**
   * Aendert BLZ, Hostname usw. auf der Karte.
   */
  public void handleChangeBankData()
  {
    handleStore();
    if (!stored)
      return;

    Application.getController().start(new BackgroundTask() {
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        PassportHandle handle = null;
        try
        {
          handle = getPassport().getHandle();
          HBCIHandler h = handle.open();
          HBCIPassport passport = h.getPassport();

          AccountContainerDialog d = new AccountContainerDialog(passport);
          AccountContainer container = (AccountContainer) d.open();
          
          passport.setBLZ(container.blz);
          passport.setUserId(container.userid);
          passport.setCustomerId(container.customerid);
          passport.setHost(container.host);
          passport.setFilterType(container.filter);
          passport.setCountry(container.country);
          
          passport.saveChanges();
          ((HBCIPassportDDV)passport).saveBankData();
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
        catch (OperationCanceledException oce)
        {
          Logger.info("operation cancelled");
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Vorgang abgebrochen"),StatusBarMessage.TYPE_ERROR));
        }
        catch (Exception e)
        {
          if (HBCIFactory.getCause(e,OperationCanceledException.class) != null)
          {
            Logger.info("operation cancelled");
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Vorgang abgebrochen"),StatusBarMessage.TYPE_ERROR));
          }
          else
          {
            Logger.error("error while changing bank data",e);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ändern der Bankdaten: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
          }
        }
        finally
        {
          if (handle != null)
          {
            try
            {
              handle.close();
            }
            catch (Exception e)
            {
              Logger.error("unable to close passport",e);
            }
          }
        }
      }
      
      public boolean isInterrupted(){return false;}
      public void interrupt(){}
    });
  }

  private class PresetListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
      try {
        Reader r = (Reader) event.data;
        if (r == null)
          return;

        if (!r.isSupported())
        {
          GUI.getView().setErrorText(i18n.tr("Der ausgewählte Karten-Leser wird von Ihrem System nicht unterstützt"));
          return;
        }

        getReaderPresets().setText(r.getName());

        String s = r.getCTAPIDriver();
        if (s != null && s.length() > 0)
        {
          File f = new File(s);
          if (!f.exists())
            GUI.getView().setErrorText(i18n.tr("Warnung: Die ausgewählte CTAPI-Treiber-Datei existiert nicht."));
        }
        
        String port = r.getPort();
        if (port != null)
          getPort().setPreselected(port);
        
        int ctNumber = r.getCTNumber();
        if (ctNumber != -1)
          getCTNumber().setValue(new Integer(ctNumber));

        getBio().setValue(new Boolean(r.useBIO()));
     		getCTAPI().setValue(s);
     		getSoftPin().setValue(new Boolean(r.useSoftPin()));
        getPassport().setJNILib(r.getJNILib());
        handleStore();
    	}
    	catch (Throwable t)
    	{
    		Logger.error("error while reading presets from reader",t);
    		GUI.getView().setErrorText(i18n.tr("Fehler beim Lesen der Voreinstellungen des Lesers"));
    	}
    }
  }
}

/*******************************************************************************
 * $Log: Controller.java,v $
 * Revision 1.6  2010/07/13 11:36:52  willuhn
 * @C Fehlerhandling
 *
 * Revision 1.5  2010-07-13 10:55:29  willuhn
 * @N Erster Code zum Aendern der Bank-Daten direkt auf der Karte. Muss dringend noch getestet werden - das will ich aber nicht mit meiner Karte machen, weil ich mir schonmal meine Karte mit Tests zerschossen hatte und die aber taeglich brauche ;)
 *
 * Revision 1.4  2010/06/17 11:45:49  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.28  2008/07/29 08:29:16  willuhn
 * @B Compile-Fix
 *
 * Revision 1.27  2007/07/24 13:50:27  willuhn
 * @N BUGZILLA 61
 *
 * Revision 1.26  2006/08/03 22:13:49  willuhn
 * @N OmniKey 4000 Preset
 * Revision 1.25 2006/04/05 15:30:16 willuhn
 * 
 * @R removed unused imports
 * 
 * Revision 1.24 2006/04/05 15:15:42 willuhn
 * @N Alternativer Treiber fuer Towitoko Kartenzwerg
 * 
 * Revision 1.23 2005/06/27 11:24:29 web0
 * @N HBCI-Version aenderbar
 * 
 * Revision 1.22 2005/04/16 13:20:09 web0 *** empty log message ***
 * 
 * Revision 1.21 2005/03/09 01:07:27 web0
 * @D javadoc fixes
 * 
 * Revision 1.20 2005/01/15 16:47:24 willuhn *** empty log message ***
 * 
 * Revision 1.19 2005/01/05 15:06:15 willuhn *** empty log message ***
 * 
 * Revision 1.18 2004/11/12 18:25:31 willuhn *** empty log message ***
 * 
 * Revision 1.17 2004/10/20 12:08:15 willuhn
 * @C MVC-Refactoring (new Controllers)
 * 
 * Revision 1.16 2004/10/17 12:52:41 willuhn *** empty log message ***
 * 
 * Revision 1.15 2004/10/14 21:59:01 willuhn
 * @N refactoring
 * 
 * Revision 1.14 2004/09/16 22:47:18 willuhn *** empty log message ***
 * 
 * Revision 1.13 2004/07/27 23:39:29 willuhn
 * @N Reader presets
 * 
 * Revision 1.12 2004/07/27 22:56:18 willuhn
 * @N Reader presets
 * 
 * Revision 1.11 2004/07/25 15:05:40 willuhn
 * @C PluginLoader is no longer static
 * 
 * Revision 1.10 2004/07/21 23:54:19 willuhn
 * @C massive Refactoring ;)
 * 
 * Revision 1.9 2004/07/19 22:37:28 willuhn
 * @B gna - Chipcard funktioniert ja doch ;)
 * 
 * Revision 1.6 2004/07/08 23:20:23 willuhn
 * @C Redesign
 * 
 * Revision 1.5 2004/07/01 19:36:41 willuhn *** empty log message ***
 * 
 * Revision 1.4 2004/06/03 00:23:50 willuhn *** empty log message ***
 * 
 * Revision 1.3 2004/05/27 22:59:07 willuhn
 * @B forgotten to store path to ctapi driver
 * 
 * Revision 1.2 2004/05/05 22:14:34 willuhn *** empty log message ***
 * 
 * Revision 1.1 2004/05/04 23:24:34 willuhn
 * @N separated passports into eclipse project
 * 
 * Revision 1.2 2004/05/04 23:07:23 willuhn
 * @C refactored Passport stuff
 * 
 * Revision 1.1 2004/04/27 22:23:56 willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports
 *    verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch
 *    die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 * 
 * Revision 1.16 2004/04/25 18:17:14 willuhn *** empty log message ***
 * 
 * Revision 1.15 2004/04/19 22:05:52 willuhn
 * @C HBCIJobs refactored
 * 
 * Revision 1.14 2004/04/13 23:14:23 willuhn
 * @N datadir
 * 
 * Revision 1.13 2004/04/12 19:15:31 willuhn
 * @C refactoring
 * 
 * Revision 1.12 2004/03/30 22:07:50 willuhn *** empty log message ***
 * 
 * Revision 1.11 2004/03/11 08:55:42 willuhn
 * @N UmsatzDetails
 * 
 * Revision 1.10 2004/03/06 18:25:10 willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 * 
 * Revision 1.9 2004/03/04 00:26:24 willuhn
 * @N Ueberweisung
 * 
 * Revision 1.8 2004/03/03 22:26:40 willuhn
 * @N help texts
 * @C refactoring
 * 
 * Revision 1.7 2004/02/27 01:10:18 willuhn
 * @N passport config refactored
 * 
 * Revision 1.6 2004/02/24 22:47:05 willuhn
 * @N GUI refactoring
 * 
 * Revision 1.5 2004/02/23 20:30:47 willuhn
 * @C refactoring in AbstractDialog
 * 
 * Revision 1.4 2004/02/20 01:36:56 willuhn *** empty log message ***
 * 
 * Revision 1.3 2004/02/13 00:41:56 willuhn *** empty log message ***
 * 
 * Revision 1.2 2004/02/12 23:46:46 willuhn *** empty log message ***
 * 
 * Revision 1.1 2004/02/12 00:38:40 willuhn *** empty log message ***
 * 
 * Revision 1.1 2004/02/11 00:11:20 willuhn *** empty log message ***
 * 
 ******************************************************************************/
