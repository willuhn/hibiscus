/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UmsatzTypControl.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/11/23 17:25:37 $
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
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Umsatz-Kategorien.
 * @author willuhn
 */
public class UmsatzTypControl extends AbstractControl
{
  private static String EINNAHME = null;
  private static String AUSGABE = null;

  private UmsatzTyp ut        = null;

  private Part list           = null;

  private TextInput name      = null;
  private TextInput pattern   = null;
  private CheckboxInput regex = null;
  private SelectInput art     = null;
  
  static
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    EINNAHME = i18n.tr("Einnahme");
    AUSGABE  = i18n.tr("Ausgabe");
  }

  /**
   * @param view
   */
  public UmsatzTypControl(AbstractView view)
  {
    super(view);
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
  private UmsatzTyp getUmsatzTyp() throws RemoteException
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
      this.name = new TextInput(getUmsatzTyp().getName());
    return this.name;
  }
  
  /**
   * Erzeugt das Eingabe-Feld fuer den Such-Pattern.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public TextInput getPattern() throws RemoteException
  {
    if (this.pattern == null)
      this.pattern = new TextInput(getUmsatzTyp().getPattern());
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
      String preselected = isEinnahme ? EINNAHME : AUSGABE;
      this.art = new SelectInput(new String[]{AUSGABE,EINNAHME},preselected);
    }
    return this.art;
  }

}


/*********************************************************************
 * $Log: UmsatzTypControl.java,v $
 * Revision 1.1  2006/11/23 17:25:37  willuhn
 * @N Umsatz-Kategorien - in PROGRESS!
 *
 *********************************************************************/