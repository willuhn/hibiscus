/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UmsatzTypControl.java,v $
 * $Revision: 1.6 $
 * $Date: 2007/06/12 08:56:01 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypNew;
import de.willuhn.jameica.hbci.gui.parts.UmsatzTypList;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Umsatz-Kategorien.
 * @author willuhn
 */
public class UmsatzTypControl extends AbstractControl
{

  private I18N i18n             = null;

  private UmsatzTyp ut          = null;

  private Part list             = null;

  private TextInput name        = null;
  private TextInput nummer      = null;
  private TextInput pattern     = null;
  private CheckboxInput regex   = null;
  private SelectInput art       = null;
  
  /**
   * @param view
   */
  public UmsatzTypControl(AbstractView view)
  {
    super(view);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }
  
  /**
   * Liefert eine Liste der existierenden Umsatz-Kategorien.
   * @return Liste der Umsatz-Kategorien.
   * @throws RemoteException
   */
  public Part getUmsatzTypListe() throws RemoteException
  {
    if (this.list == null)
      this.list = new UmsatzTypList(new UmsatzTypNew());
    return this.list;
  }
  
  /**
   * Liefert den aktuellen Umsatz-Typ.
   * @return der aktuelle Umsatz-Typ.
   * @throws RemoteException
   */
  public UmsatzTyp getUmsatzTyp() throws RemoteException
  {
    if (this.ut != null)
      return this.ut;

    this.ut = (UmsatzTyp) getCurrentObject();
    if (this.ut != null)
      return this.ut;
    
    this.ut = (UmsatzTyp) Settings.getDBService().createObject(UmsatzTyp.class,null);
    return this.ut;
  }

  /**
   * Erzeugt das Eingabe-Feld fuer den Namen.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public TextInput getName() throws RemoteException
  {
    if (this.name == null)
    {
      this.name = new TextInput(getUmsatzTyp().getName());
      this.name.setMandatory(true);
    }
    return this.name;
  }

  /**
   * Erzeugt das Eingabe-Feld fuer die Nummer.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public TextInput getNummer() throws RemoteException
  {
    if (this.nummer == null)
    {
      this.nummer = new TextInput(getUmsatzTyp().getNummer(),5);
      this.nummer.setComment(i18n.tr("Wird für die Sortierung verwendet"));
      this.nummer.setMandatory(false);
    }
    return this.nummer;
  }
  
  /**
   * Erzeugt das Eingabe-Feld fuer den Such-Pattern.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public TextInput getPattern() throws RemoteException
  {
    if (this.pattern == null)
    {
      this.pattern = new TextInput(getUmsatzTyp().getPattern());
      this.pattern.setComment(i18n.tr("Für automatische Zuordnung anhand von Suchbegriffen"));
    }
    return this.pattern;
  }
  
  /**
   * Liefert eine Checkbox zur Aktivierung von regulaeren Ausdruecken.
   * @return Checkbox.
   * @throws RemoteException
   */
  public CheckboxInput getRegex() throws RemoteException
  {
    if (this.regex == null)
      this.regex = new CheckboxInput(getUmsatzTyp().isRegex());
    return this.regex;
  }

  /**
   * Liefert eine Auswahl-Box fuer die Art des Umsatzes.
   * @return Auswahl-Box.
   * @throws RemoteException
   */
  public SelectInput getArt() throws RemoteException
  {
    if (this.art == null)
    {
      boolean isEinnahme = getUmsatzTyp().isEinnahme();
      String preselected = isEinnahme ? UmsatzTyp.EINNAHME: UmsatzTyp.AUSGABE;
      this.art = new SelectInput(new String[]{UmsatzTyp.AUSGABE,UmsatzTyp.EINNAHME},preselected);
    }
    return this.art;
  }

  /**
   * Speichert die Einstellungen.
   */
  public synchronized void handleStore()
  {
    try {
      String s = (String) getArt().getValue();
      getUmsatzTyp().setEinnahme(UmsatzTyp.EINNAHME.equals(s));
      getUmsatzTyp().setName((String)getName().getValue());
      getUmsatzTyp().setNummer((String)getNummer().getValue());
      getUmsatzTyp().setPattern((String)getPattern().getValue());
      getUmsatzTyp().setRegex(((Boolean)getRegex().getValue()).booleanValue());
      getUmsatzTyp().store();
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Umsatz-Kategorie gespeichert"), StatusBarMessage.TYPE_SUCCESS));
    }
    catch (ApplicationException e2)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(e2.getMessage(), StatusBarMessage.TYPE_ERROR));
    }
    catch (RemoteException e)
    {
      Logger.error("error while storing umsatz type",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Speichern der Umsatz-Kategorie"), StatusBarMessage.TYPE_ERROR));
    }
  }
}


/*********************************************************************
 * $Log: UmsatzTypControl.java,v $
 * Revision 1.6  2007/06/12 08:56:01  willuhn
 * @B Bug 410
 *
 * Revision 1.5  2007/03/12 13:58:56  willuhn
 * @C Eindeutigkeit des Namens trotz UNIQUE-Key vorher in insertCheck pruefen - das spart das Parsen der SQLException
 *
 * Revision 1.4  2007/03/10 07:17:40  jost
 * Neu: Nummer fÃ¼r die Sortierung der Umsatz-Kategorien
 * Umsatzkategorien editierbar gemacht (Verlagerung vom Code -> DB)
 *
 * Revision 1.3  2006/12/28 15:38:43  willuhn
 * @N Farbige Pflichtfelder
 *
 * Revision 1.2  2006/11/23 23:24:17  willuhn
 * @N Umsatz-Kategorien: DB-Update, Edit
 *
 * Revision 1.1  2006/11/23 17:25:37  willuhn
 * @N Umsatz-Kategorien - in PROGRESS!
 *
 *********************************************************************/