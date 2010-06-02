/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/UmsatzTypControl.java,v $
 * $Revision: 1.13 $
 * $Date: 2010/06/02 15:32:03 $
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
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.ColorInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.input.UmsatzTypInput;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.UmsatzTypUtil;
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

  private TextInput name        = null;
  private TextInput nummer      = null;
  private TextInput pattern     = null;
  private CheckboxInput regex   = null;
  private SelectInput art       = null;
  private UmsatzTypInput parent = null;
  
  private ColorInput color          = null;
  private CheckboxInput customColor = null;
  
  /**
   * @param view
   */
  public UmsatzTypControl(AbstractView view)
  {
    super(view);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
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
      this.pattern.addListener(new Listener()
      {
      
        public void handleEvent(Event event)
        {
          // Wir testen sofort, ob der regulaere Ausdruck vielleicht
          // ungueltig ist
          try
          {
            GUI.getView().setErrorText("");
            String p = (String) pattern.getValue();
            if (p == null || p.length() == 0)
              return;
            boolean b = ((Boolean)getRegex().getValue()).booleanValue();
            if (b)
            {
              try
              {
                Pattern.compile(p);
              }
              catch (PatternSyntaxException pse)
              {
                GUI.getView().setErrorText(i18n.tr("Regulärer Ausdruck ungültig: {0}",pse.getDescription()));
              }
            }
          }
          catch (Exception e)
          {
            Logger.error("unable to verify pattern",e);
          }
        }
      
      });
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
   * Liefert eine Checkbox zur Aktivierung von benutzerdefinierten Farben.
   * @return Checkbox.
   * @throws RemoteException
   */
  public CheckboxInput getCustomColor() throws RemoteException
  {
    if (this.customColor != null)
      return this.customColor;

    this.customColor = new CheckboxInput(getUmsatzTyp().isCustomColor());
    this.customColor.addListener(new Listener()
    {
      /**
       * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
       */
      public void handleEvent(Event event)
      {
        try
        {
          Boolean b = (Boolean) customColor.getValue();
          getColor().setEnabled(b.booleanValue());
        }
        catch (RemoteException re)
        {
          Logger.error("unable to update color input",re);
        }
      }
    });
    return this.customColor;
  }
  
  /**
   * Feld zur Auswahl der Farbe.
   * @return Auswahlfeld.
   * @throws RemoteException
   */
  public ColorInput getColor() throws RemoteException
  {
    if (this.color == null)
    {
      int[] rgb = getUmsatzTyp().getColor();
      if (rgb == null || rgb.length != 3)
        rgb = new int[]{0,0,0};
      this.color = new ColorInput(new Color(GUI.getDisplay(),new RGB(rgb[0],rgb[1],rgb[2])),true);
      this.color.setEnabled(getUmsatzTyp().isCustomColor());
    }
    return this.color;
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
      ArrayList list = new ArrayList();
      list.add(new UmsatzTypObject(UmsatzTyp.TYP_EGAL));
      list.add(new UmsatzTypObject(UmsatzTyp.TYP_EINNAHME));
      list.add(new UmsatzTypObject(UmsatzTyp.TYP_AUSGABE));
      
      this.art = new SelectInput(list,new UmsatzTypObject(getUmsatzTyp().getTyp()));
    }
    return this.art;
  }
  
  /**
   * Liefert eine Auswahlbox fuer die Eltern-Kategorie.
   * @return Auswahlbox.
   * @throws RemoteException
   */
  public UmsatzTypInput getParent() throws RemoteException
  {
    if (this.parent == null)
    {
      this.parent = new UmsatzTypInput((UmsatzTyp)getUmsatzTyp().getParent(),getUmsatzTyp(),UmsatzTyp.TYP_EGAL);
      this.parent.setAttribute("name");
      this.parent.setPleaseChoose(i18n.tr("<Keine>"));
    }
    return this.parent;
  }

  /**
   * Speichert die Einstellungen.
   */
  public synchronized void handleStore()
  {
    try {
      UmsatzTypObject t = (UmsatzTypObject) getArt().getValue();
      
      UmsatzTyp ut = getUmsatzTyp();
      ut.setTyp(t == null ? UmsatzTyp.TYP_EGAL : t.typ);
      ut.setName((String)getName().getValue());
      ut.setNummer((String)getNummer().getValue());
      ut.setPattern((String)getPattern().getValue());
      ut.setRegex(((Boolean)getRegex().getValue()).booleanValue());
      ut.setParent((UmsatzTyp)getParent().getValue());
      
      boolean b = ((Boolean)getCustomColor().getValue()).booleanValue();
      ut.setCustomColor(b);
      if (b)
      {
        Color c = (Color) getColor().getValue();
        if (c == null)
        {
          ut.setColor(null);
        }
        else
        {
          RGB rgb = c.getRGB();
          ut.setColor(new int[]{rgb.red,rgb.green,rgb.blue});
        }
      }
      
      
      ut.store();
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
  
  /**
   * Hilfsklasse fuer die Art der Kategorie.
   */
  public static class UmsatzTypObject implements GenericObject
  {
    private int typ = UmsatzTyp.TYP_EGAL;
    
    /**
     * ct
     * @param typ der Umsatz-Typ.
     */
    private UmsatzTypObject(int typ)
    {
      this.typ = typ;
    }
    
    /**
     * Liefert den Typ.
     * @return der Typ.
     */
    public int getTyp()
    {
      return this.typ;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
      if (other == this)
        return true;
      if (other == null || !(other instanceof UmsatzTypObject))
        return false;
      return this.typ == ((UmsatzTypObject)other).typ;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      return UmsatzTypUtil.getNameForType(this.typ);
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"name"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return String.valueOf(this.typ);
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }
  }
}


/*********************************************************************
 * $Log: UmsatzTypControl.java,v $
 * Revision 1.13  2010/06/02 15:32:03  willuhn
 * @N Unique-Constraint auf Spalte "name" in Tabelle "umsatztyp" entfernt. Eine Kategorie kann jetzt mit gleichem Namen beliebig oft auftreten
 * @N Auswahlbox der Oberkategorie in Einstellungen->Umsatz-Kategorien zeigt auch die gleiche Baumstruktur wie bei der Zuordnung der Kategorie in der Umsatzliste
 *
 * Revision 1.12  2010/03/05 15:24:53  willuhn
 * @N BUGZILLA 686
 *
 * Revision 1.11  2009/05/08 13:58:30  willuhn
 * @N Icons in allen Menus und auf allen Buttons
 * @N Fuer Umsatz-Kategorien koennen nun benutzerdefinierte Farben vergeben werden
 *
 * Revision 1.10  2009/02/23 23:44:50  willuhn
 * @N Etwas Code fuer Support fuer Unter-/Ober-Kategorien
 *
 * Revision 1.9  2008/09/17 23:44:29  willuhn
 * @B SQL-Query fuer MaxUsage-Abfrage korrigiert
 *
 * Revision 1.8  2008/08/29 16:46:23  willuhn
 * @N BUGZILLA 616
 *
 * Revision 1.7  2007/08/24 22:22:00  willuhn
 * @N Regulaere Ausdruecke vorm Speichern testen
 *
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