/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.io.csv.Format;
import de.willuhn.jameica.hbci.io.csv.Profile;
import de.willuhn.jameica.hbci.io.csv.ProfileUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum Speichern eines CSV-Profils.
 */
public class CSVProfileStoreDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static int WINDOW_WIDTH = 500;
  
  private Format format          = null;
  private Profile profile        = null;
  private Button apply           = null;
  private TextInput name         = null;
  private LabelInput hint        = null;
  
  private List<Profile> profiles = null;
  
	/**
	 * Erzeugt einen neuen Text-Dialog.
	 * @param format das Format.
	 * @param profile das Profil.
	 * @see AbstractDialog#POSITION_MOUSE
	 * @see AbstractDialog#POSITION_CENTER
	 */
  public CSVProfileStoreDialog(Format format, Profile profile)
  {
    super(CSVProfileStoreDialog.POSITION_CENTER);
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
    this.setTitle(i18n.tr("Name des Profils"));
    this.setSideImage(SWTUtil.getImage("dialog-question-large.png"));
    
    this.profile = profile;
    
    // Wir machen neue Profile generell zu Nicht-System-Profilen
    this.profile.setSystem(false);
    
    this.format = format;
    this.profiles = ProfileUtil.read(format);
  }
  
  /**
   * Liefert das Eingabefeld fuer den Namen.
   * @return das Eingabefeld fuer den Namen.
   */
  private TextInput getName()
  {
    if (this.name != null)
      return this.name;
    
    this.name = new TextInput(this.suggestName());
    this.name.setName("");
    this.name.setMandatory(true);
    return this.name;
  }
  
  /**
   * Liefert einen Namensvorschlag fuer das Profil.
   * @return Namensvorschlag fuer das Profil.
   */
  private String suggestName()
  {
    // Wir versuchen einen Namensvorschlag für das Profil zu ermitteln
    // Hierzu checken wir, ob es mit einer Zahl endet. Wenn ja, erhoehen
    // wir sie um 1. Wenn nicht, haengen wir "2" hinten dran.
    String template = this.profile.getName();
    int space       = template.lastIndexOf(" ");

    // Checken, ob wir schon eine Nummer im Namen haben
    if (space > 0)
    {
      String s1 = StringUtils.trimToNull(template.substring(0,space));
      String s2 = StringUtils.trimToNull(template.substring(space));
      if (s1 != null && s2 != null && s2.matches("[0-9]{1,4}"))
        template = s1;
    }

    // Erste freie Nummer suchen
    for (int i=2;i<100;++i)
    {
      boolean found = false;
      String name = template + " " + i;
      for (Profile p:this.profiles)
      {
        if (name.equals(p.getName()))
        {
          found = true;
          break;
        }
      }
      
      // wir haben einen passenden Namen gefunden
      if (!found)
        return name;
    }
    
    // Ne, dann muss der User selbst einen neuen Namen vergeben
    return template;
  }
  
  /**
   * Zeigt einen Hinweis-Text an.
   * @return Hinweis-Text.
   */
  private LabelInput getHint()
  {
    if (this.hint != null)
      return this.hint;
    
    this.hint = new LabelInput("");
    this.hint.setName("");
    this.hint.setColor(Color.ERROR);
    return this.hint;
  }
  
  /**
   * Liefert den Apply-Button.
   * @return der Apply-Button.
   */
  private Button getApply()
  {
    if (this.apply != null)
      return this.apply;
    
    this.apply = new Button(i18n.tr("Übernehmen"),new Action() {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        // Name uebernehmen
        profile.setName((String) getName().getValue());
        
        // Wir koennen das Profil zur Liste hinzufuegen.
        // Checken, ob eines ueberschrieben werden soll.
        boolean found = false;
        for (Profile p:profiles)
        {
          if (p.isSystem())
            continue;
          
          if (p.getName().equals(profile.getName()))
          {
            profiles.set(profiles.indexOf(p),profile);
            found = true;
            break;
          }
        }
        
        if (!found)
          profiles.add(profile);
        
        ProfileUtil.store(format,profiles);
        close();
      }
    },null,true,"ok.png");
    
    return this.apply;
  }

  @Override
  protected void paint(Composite parent) throws Exception
	{
    Container c = new SimpleContainer(parent);
    c.addText(i18n.tr("Bitte geben Sie einen Namen für das Profil an.\n\n" +
                      "Verwenden Sie einen noch nicht benutzten Namen, um ein neues Profil hinzuzufügen. " +
                      "Vergeben Sie alternativ einen bereits verwendeten Namen, um dieses Profil zu überschreiben."),true);
    
    final Input name = this.getName();
    c.addInput(name);
    
    name.getControl().addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e)
      {
        updateUI();
      }
    });

    c.addInput(getHint());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(this.getApply());
    buttons.addButton(new Cancel());
    
    c.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
    
    // UI initialisieren
    updateUI();
	}
  
  /**
   * Aktualisiert die UI nach Zeicheneingabe
   */
  private void updateUI()
  {
    // Apply-Button deaktivieren, wenn nichts drin steht.
    final String text = StringUtils.trimToNull((String) name.getValue());
    getApply().setEnabled(text != null);
    
    // Hier brauchen wir nichts weiter checken
    if (text == null)
      return;

    getHint().setValue("");

    boolean found  = false;
    boolean system = false;
    
    // Jetzt noch checken, ob wir ein existierendes Profil ueberschreiben und einen Warnhinweis anzeigen
    // Ausserdem einen Hinweis, wenn der User versucht, das System-Profil zu ueberschreiben
    for (Profile p:this.profiles)
    {
      if (p.getName().equals(text))
      {
        found = true;
        system = p.isSystem();
      }
    }
    
    if (!found)
    {
      getHint().setColor(Color.SUCCESS);
      getHint().setValue(i18n.tr("Ein neues Profil wird angelegt."));
    }

    if (found && !system)
    {
      getHint().setColor(Color.LINK);
      getHint().setValue(i18n.tr("Existierendes Profil wird überschrieben."));
    }
    
    if (system)
    {
      getHint().setColor(Color.ERROR);
      getHint().setValue(i18n.tr("Das Default-Profil darf nicht überschrieben werden."));
      getApply().setEnabled(false);
    }
  }

  @Override
  protected Object getData() throws Exception
  {
    return this.profile;
  }
}
