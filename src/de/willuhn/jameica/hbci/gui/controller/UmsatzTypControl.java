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
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.UmsatzTypInput;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.UmsatzTypUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Controller fuer die Umsatz-Kategorien.
 */
public class UmsatzTypControl extends AbstractControl
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private UmsatzTyp ut              = null;

  private TextInput name            = null;
  private TextInput nummer          = null;
  private TextInput pattern         = null;
  private CheckboxInput regex       = null;
  private SelectInput art           = null;
  private UmsatzTypInput parent     = null;
  private TextInput kommentar       = null;
  private KontoInput konto          = null;
  private CheckboxInput skipReport  = null;
  
  private ColorInput color          = null;
  private CheckboxInput customColor = null;
  
  /**
   * @param view
   */
  public UmsatzTypControl(AbstractView view)
  {
    super(view);
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
   * Erzeugt das Eingabe-Feld fuer den Kommentar.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public TextInput getKommentar() throws RemoteException
  {
    if (this.kommentar != null)
      return this.kommentar;
    
    this.kommentar = new TextAreaInput(this.getUmsatzTyp().getKommentar(),1000);
    return this.kommentar;
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
      this.nummer.setHint(i18n.tr("Wird auch f�r die Sortierung verwendet"));
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
      this.pattern = new TextInput(getUmsatzTyp().getPattern(),UmsatzTyp.MAXLENGTH_PATTERN);
      this.pattern.setHint(i18n.tr("F�r automatische Zuordnung anhand von Suchbegriffen"));
      this.pattern.addListener(new Listener()
      {
      
        @Override
        public void handleEvent(Event event)
        {
          // Wir testen sofort, ob der regulaere Ausdruck vielleicht
          // ungueltig ist
          try
          {
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
                GUI.getView().setErrorText(i18n.tr("Regul�rer Ausdruck ung�ltig: {0}",pse.getDescription()));
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
      @Override
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
    if (this.parent != null)
      return this.parent;
    
    this.parent = new UmsatzTypInput((UmsatzTyp)getUmsatzTyp().getParent(),getUmsatzTyp(),UmsatzTyp.TYP_EGAL, false);
    this.parent.setComment("");
    return this.parent;
  }
  
  /**
   * Liefert ein Auswahlfeld fuer das Konto.
   * @return Auswahl-Feld.
   * @throws RemoteException
   */
  public KontoInput getKonto() throws RemoteException
  {
    if (this.konto != null)
      return this.konto;
    
    final String kat = this.getUmsatzTyp().getKontoKategorie();
    final Konto k = this.getUmsatzTyp().getKonto();
    this.konto = new KontoInput(k,KontoFilter.ALL);
    this.konto.setComment(null);
    this.konto.setPleaseChoose("<" + i18n.tr("keine Einschr�nkung") + ">");
    this.konto.setSupportGroups(true);
    this.konto.setName(i18n.tr("Nur f�r Konto/Gruppe"));
    if (kat != null && kat.length() > 0)
      this.konto.setValue(kat);
    return this.konto;
  }
  
  /**
   * Liefert eine Checkbox, mit der festgelegt werden kann, ob eine Kategorie in den Auswertungen ignoriert werden soll.
   * @return Checkbox.
   * @throws RemoteException
   */
  public CheckboxInput getSkipReport() throws RemoteException
  {
    if (this.skipReport != null)
      return this.skipReport;
    
    this.skipReport = new CheckboxInput(this.getUmsatzTyp().hasFlag(UmsatzTyp.FLAG_SKIP_REPORTS));
    this.skipReport.setName(i18n.tr("In Auswertungen ignorieren"));
    return this.skipReport;
  }

  /**
   * Speichert die Einstellungen.
   * @return true, wenn das Speichern erfolgreich war.
   */
  public synchronized boolean handleStore()
  {
    try
    {
      UmsatzTypObject t = (UmsatzTypObject) getArt().getValue();
      
      UmsatzTyp ut = getUmsatzTyp();
      ut.setTyp(t == null ? UmsatzTyp.TYP_EGAL : t.typ);
      ut.setName((String)getName().getValue());
      ut.setKommentar((String)getKommentar().getValue());
      ut.setNummer((String)getNummer().getValue());
      ut.setPattern((String)getPattern().getValue());
      ut.setRegex(((Boolean)getRegex().getValue()).booleanValue());
      ut.setParent((UmsatzTyp)getParent().getValue());

      //////////////////////////////////////////////////////////////
      // Konto-Zuordnung
      Object k = this.getKonto().getValue();
      if (k == null)
      {
        ut.setKonto(null);
        ut.setKontoKategorie(null);
      }
      else
      {
        if (k instanceof Konto)
          ut.setKonto((Konto) k);
        else
          ut.setKontoKategorie((String) k);
      }
      //
      //////////////////////////////////////////////////////////////
      
      //////////////////////////////////////////////////////////////
      // Skip-Reports-Flag
      final boolean skip = ((Boolean)this.getSkipReport().getValue()).booleanValue();
      final int flags = ut.getFlags();
      final boolean have = ut.hasFlag(UmsatzTyp.FLAG_SKIP_REPORTS);
      
      if (skip && !have)
        ut.setFlags(flags | UmsatzTyp.FLAG_SKIP_REPORTS);
      else if (!skip && have)
        ut.setFlags(flags ^ UmsatzTyp.FLAG_SKIP_REPORTS);
      //
      //////////////////////////////////////////////////////////////

      
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
      return true;
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
    return false;
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

    @Override
    public boolean equals(GenericObject other) throws RemoteException
    {
      if (other == this)
        return true;
      if (other == null || !(other instanceof UmsatzTypObject))
        return false;
      return this.typ == ((UmsatzTypObject)other).typ;
    }

    @Override
    public Object getAttribute(String arg0) throws RemoteException
    {
      return UmsatzTypUtil.getNameForType(this.typ);
    }

    @Override
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"name"};
    }

    @Override
    public String getID() throws RemoteException
    {
      return String.valueOf(this.typ);
    }

    @Override
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }
  }
}
