/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.controller;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.FileInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.parts.KontoauszugPdfList;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.hbci.server.KontoauszugPdfUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Anzeige der Kontoauszuege im PDF-Format.
 */
public class KontoauszugPdfControl extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private Kontoauszug auszug = null;
  private KontoauszugPdfList list = null;
  
  private Input datei = null;
  private Input abrufdatum = null;
  private TextAreaInput kommentar = null;
  private Input erstellungsdatum = null;
  private DateInput von = null;
  private DateInput bis = null;
  private Input jahr = null;
  private Input nummer = null;
  private Input name1 = null;
  private Input name2 = null;
  private Input name3 = null;
  private Input quittiertAm = null;
  
  /**
   * ct.
   * @param view
   */
  public KontoauszugPdfControl(AbstractView view)
  {
    super(view);
  }
  
  /**
   * Liefert den ausgewaehlten Kontoauszug.
   * @return der ausgewaehlte Kontoauszug.
   */
  private Kontoauszug getKontoauszug()
  {
    if (this.auszug != null)
      return this.auszug;
    
    this.auszug = (Kontoauszug) this.getCurrentObject();
    return this.auszug;
  }
  
  /**
   * Liefert die Tabelle mit den Kontoauszuegen.
   * @return die Tabelle mit den Kontoauszuegen.
   */
  public KontoauszugPdfList getList()
  {
    if (this.list != null)
      return this.list;
    
    this.list = new KontoauszugPdfList();
    return this.list;
  }
  
  /**
   * Liefert das Eingabefeld mit dem Abrufdatum.
   * @return Eingabefeld.
   * @throws RemoteException
   */
  public Input getAbrufdatum() throws RemoteException
  {
    if (this.abrufdatum != null)
      return this.abrufdatum;
    
    this.abrufdatum = new DateInput(this.getKontoauszug().getAusfuehrungsdatum());
    this.abrufdatum.setName(i18n.tr("Abgerufen am"));
    this.abrufdatum.setEnabled(false);
    return this.abrufdatum;
  }
  
  /**
   * Liefert das Eingabefeld mit dem Erstellungsdatum.
   * @return Eingabefeld.
   * @throws RemoteException
   */
  public Input getErstellungsdatum() throws RemoteException
  {
    if (this.erstellungsdatum != null)
      return this.erstellungsdatum;
    
    Date erstellt = this.getKontoauszug().getErstellungsdatum();
    this.erstellungsdatum = new TextInput(erstellt != null ? HBCI.DATEFORMAT.format(erstellt) : ("<" + i18n.tr("von der Bank nicht angegeben") + ">"));
    this.erstellungsdatum.setName(i18n.tr("Erstellt am"));
    this.erstellungsdatum.setEnabled(false);
    return this.erstellungsdatum;
  }
  
  /**
   * Liefert das Eingabefeld mit dem Quittierungsdatum.
   * @return Eingabefeld.
   * @throws RemoteException
   */
  public Input getQuittierungsdatum() throws RemoteException
  {
    if (this.quittiertAm != null)
      return this.quittiertAm;
    
    this.quittiertAm = new DateInput(this.getKontoauszug().getQuittiertAm());
    this.quittiertAm.setName(i18n.tr("Empfang quittiert am"));
    this.quittiertAm.setEnabled(false);
    return this.quittiertAm;
  }

  /**
   * Liefert das Eingabefeld mit dem Von-Datum.
   * @return Eingabefeld.
   * @throws RemoteException
   */
  public Input getVonDatum() throws RemoteException
  {
    if (this.von != null)
      return this.von;
    
    this.von = new DateInput(this.getKontoauszug().getVon());
    this.von.setTitle(i18n.tr("Startdatum"));
    this.von.setText(i18n.tr("Bitte wählen Sie das Startdatum des Zeitraumes aus."));
    this.von.setEnabled(true);
    return this.von;
  }

  /**
   * Liefert das Eingabefeld mit dem Bis-Datum.
   * @return Eingabefeld.
   * @throws RemoteException
   */
  public Input getBisDatum() throws RemoteException
  {
    if (this.bis != null)
      return this.bis;
    
    this.bis = new DateInput(this.getKontoauszug().getBis());
    this.bis.setName(i18n.tr("Bis"));
    this.bis.setTitle(i18n.tr("Enddatum"));
    this.bis.setText(i18n.tr("Bitte wählen Sie das Enddatum des Zeitraums aus."));
    this.bis.setEnabled(true);
    return this.bis;
  }

  /**
   * Liefert das Eingabefeld mit dem Jahr.
   * @return Eingabefeld.
   * @throws RemoteException
   */
  public Input getJahr() throws RemoteException
  {
    if (this.jahr != null)
      return this.jahr;
    
    Integer i = this.getKontoauszug().getJahr();
    this.jahr = new IntegerInput(i != null ? i.intValue() : -1);
    this.jahr.setName(i18n.tr("Jahr"));
    this.jahr.setEnabled(true);
    return this.jahr;
  }


  /**
   * Liefert das Eingabefeld mit der Auszugsnummer.
   * @return Eingabefeld.
   * @throws RemoteException
   */
  public Input getNummer() throws RemoteException
  {
    if (this.nummer != null)
      return this.nummer;
    
    Integer i = this.getKontoauszug().getNummer();
    this.nummer = new IntegerInput(i != null ? i.intValue() : -1);
    this.nummer.setName(i18n.tr("Nummer"));
    this.nummer.setEnabled(true);
    return this.nummer;
  }
  
  /**
   * Liefert das Eingabefeld fuer Name 1.
   * @return das Eingabefeld fuer Name 1.
   * @throws RemoteException
   */
  public Input getName1() throws RemoteException
  {
    if (this.name1 != null)
      return this.name1;
    
    this.name1 = new TextInput(this.getKontoauszug().getName1(),255);
    this.name1.setName(i18n.tr("Name 1"));
    return this.name1;
  }
  
  /**
   * Liefert das Eingabefeld fuer Name 2.
   * @return das Eingabefeld fuer Name 2.
   * @throws RemoteException
   */
  public Input getName2() throws RemoteException
  {
    if (this.name2 != null)
      return this.name2;
    
    this.name2 = new TextInput(this.getKontoauszug().getName2(),255);
    this.name2.setName(i18n.tr("Name 2"));
    return this.name2;
  }
  
  /**
   * Liefert das Eingabefeld fuer Name 3.
   * @return das Eingabefeld fuer Name 3.
   * @throws RemoteException
   */
  public Input getName3() throws RemoteException
  {
    if (this.name3 != null)
      return this.name3;
    
    this.name3 = new TextInput(this.getKontoauszug().getName3(),255);
    this.name3.setName(i18n.tr("Name 3"));
    return this.name3;
  }

  /**
   * Liefert das Eingabefeld mit dem Kommentar.
   * @return das Eingabefeld mit dem Kommentar.
   * @throws RemoteException
   */
  public TextAreaInput getKommentar() throws RemoteException
  {
    if (this.kommentar != null)
      return this.kommentar;
    
    this.kommentar = new TextAreaInput(this.getKontoauszug().getKommentar(),1000);
    this.kommentar.setName(i18n.tr("Notizen"));
    return this.kommentar;
  }
  
  /**
   * Liefert das Eingabefeld fuer die Datei.
   * @return das Eingabefeld fuer die Datei.
   * @throws RemoteException
   */
  public Input getDatei() throws RemoteException
  {
    if (this.datei != null)
      return this.datei;
    
    Kontoauszug k = this.getKontoauszug();
    if (StringUtils.trimToNull(k.getUUID()) != null)
    {
      this.datei = new LabelInput(i18n.tr("In Archiv-Server gespeichert"));
    }
    else
    {
      String name = k.getDateiname();
      String path = k.getPfad();
      File f = new File(path,name);
      this.datei = new FileInput(f.getAbsolutePath(),false);
    }
    this.datei.setName(i18n.tr("Datei"));
    return this.datei;
  }
  
  /**
   * Oeffnet die Datei des Kontoauszuges.
   * @throws ApplicationException
   */
  public void handleOpen() throws ApplicationException
  {
    // Wenn eine Datei angegeben ist, oeffnen wir die. Ansonsten das im Kontoauszug hinterlegte.
    // Grund: Falls der User den Pfad zur Datei geaendert hat, die Aenderungen aber noch nicht gespeichert
    // hat, wuerden wir sonst nicht die aktuell vom User ausgewaehlte Datei offnen.
    try
    {
      String file = StringUtils.trimToNull((String) this.getDatei().getValue());
      File f = file != null ? new File(file) : KontoauszugPdfUtil.getFile(this.getKontoauszug());
      
      if (!f.exists() || !f.canRead())
        throw new ApplicationException(i18n.tr("Datei existiert nicht oder ist nicht lesbar"));

      new Program().handleAction(f);
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to open file",e);
      throw new ApplicationException(i18n.tr("Fehlgeschlagen: {0}",e.getMessage()));
    }

  }
  
  /**
   * Uebernimmt die Aenderungen an dem Kontoauszug.
   * @return true, wenn das Speichern erfolgreich war.
   */
  public boolean handleStore()
  {
    try
    {
      final Kontoauszug k = this.getKontoauszug();
      k.setBis((Date) this.getBisDatum().getValue());
      k.setVon((Date) this.getVonDatum().getValue());
      k.setJahr((Integer) this.getJahr().getValue());
      k.setKommentar((String) this.getKommentar().getValue());
      k.setName1((String) this.getName1().getValue());
      k.setName2((String) this.getName2().getValue());
      k.setName3((String) this.getName3().getValue());
      k.setNummer((Integer) this.getNummer().getValue());

      String file = StringUtils.trimToNull((String) this.getDatei().getValue());

      File f = file != null ? new File(file) : null;
      String name = f != null ? f.getName() : null;
      String path = f != null ? f.getParent() : null;
      k.setDateiname(name);
      k.setPfad(path);
      
      k.store();
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Änderungen gespeichert."),StatusBarMessage.TYPE_SUCCESS));
      return true;
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("unable to save changes",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
    return false;
  }

}


