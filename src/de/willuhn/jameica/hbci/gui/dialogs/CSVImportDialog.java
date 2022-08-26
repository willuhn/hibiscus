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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.SpinnerInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.io.csv.Column;
import de.willuhn.jameica.hbci.io.csv.Format;
import de.willuhn.jameica.hbci.io.csv.Profile;
import de.willuhn.jameica.hbci.io.csv.ProfileUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog zum Importieren von CSV-Daten.
 */
public class CSVImportDialog extends AbstractDialog
{
  private final static Settings settings = new Settings(CSVImportDialog.class);
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static String[] CHARSETS = new String[]{"UTF-8","ISO-8859-15","ISO-8859-1","UTF-16"};

  private SelectInput profiles      = null;
  private Profile result            = null;
  private Format format             = null;
  private byte[] data               = null;

  private TextInput sepChar         = null;
  private TextInput quoteChar       = null;
  private SelectInput encoding      = null;
  private SpinnerInput skipLines    = null;
  private LabelInput error          = null;
  
  private List<SelectInput> selects = new ArrayList<SelectInput>();
  
  private Composite parent          = null;
  
  
  /**
   * ct.
   * @param data Die CSV-Datei.
   * @param format das ausgewaehlte Format.
   * @param position
   * @throws IOException
   * @throws ApplicationException
   */
  public CSVImportDialog(byte[] data, Format format, int position) throws IOException, ApplicationException
  {
    super(position);

    this.format = format;
    this.data   = data;

    this.setTitle(i18n.tr("Zuordnung der Spalten"));
    this.setSize(680,700);

  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    // BUGZILLA 281
    Container options = new SimpleContainer(parent);
    options.addInput(this.getProfiles());
    
    ButtonArea b = new ButtonArea();
    b.addButton(i18n.tr("CSV-Datei neu laden"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        reload();
      }
    },null,false,"view-refresh.png");
    b.addButton(i18n.tr("Profil speichern..."), new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        // Wir erzeugen erstmal ein neues Profil
        // Der User entscheidet dann im CSVProfileStoreDialog, ob er ueberschreibt oder nicht
        Profile p = new Profile();
        p.setColumns(getColumns());
        p.setName(getProfile().getName());
        p.setSystem(false);
        p.setFileEncoding((String)getFileEncoding().getValue());
        p.setQuotingChar((String)getQuoteChar().getValue());
        p.setSeparatorChar((String)getSeparatorChar().getValue());
        p.setSkipLines(((Integer)getSkipLines().getValue()).intValue());
        
        Profile pNew = ProfileUtil.add(format,p);
        if (pNew != null)
        {
          // Liste in der Auswahlbox neu laden und das neue Profil selektieren
          getProfiles().setList(ProfileUtil.read(format));
          getProfiles().setPreselected(pNew);
        }
      }
    },null,false,"document-save.png");
    b.addButton(i18n.tr("Profil löschen..."), new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          if (!Application.getCallback().askUser(i18n.tr("Soll das Profil wirklich gelöscht werden?"),false))
            return;
        }
        catch (ApplicationException | OperationCanceledException e)
        {
          throw e;
        }
        catch (Exception e)
        {
          Logger.error("unable to delete profile",e);
          getError().setValue(i18n.tr("Fehler beim Löschen: {0}",e.getMessage()));
          return;
        }
        if (ProfileUtil.delete(format,getProfile()))
        {
          // Liste aktualisieren
          getProfiles().setList(ProfileUtil.read(format));
          
          // Nach dem Loeschen die Auswahl zurueck auf das Default-Profil setzen
          getProfiles().setValue(getProfiles().getList().get(0));
        }

        // Einstellungen des ausgewaehlten Profils uebernehmen
        update();
        
        // Datei damit neu laden
        reload();
      }
    },null,false,"user-trash-full.png");
    options.addButtonArea(b);

    options.addText("",false);
    options.addHeadline(i18n.tr("Optionen"));
    options.addInput(this.getFileEncoding());
    options.addInput(this.getSeparatorChar());
    options.addInput(this.getQuoteChar());
    options.addInput(this.getSkipLines());
    
    options.addText("",false);
    // BUGZILLA 412
    options.addHeadline(i18n.tr("Zuordnung der Spalten"));
    options.addText(i18n.tr("In der linken Spalte sehen Sie die erste Zeile Ihrer CSV-Datei.\n" +
                            "Ordnen Sie die Felder bitte über die Auswahl-Elemente auf der rechte Seite zu."),true);

    this.parent = new Composite(parent,SWT.NONE);
    this.parent.setLayoutData(new GridData(GridData.FILL_BOTH));
    this.parent.setLayout(new GridLayout());
    reload();
    
    SimpleContainer c = new SimpleContainer(parent);
    c.addInput(this.getError());
    
    ButtonArea b2 = new ButtonArea();
    b2.addButton(i18n.tr("Import starten"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        result = getProfile();
        result.setFileEncoding((String)getFileEncoding().getValue());
        result.setQuotingChar((String)getQuoteChar().getValue());
        result.setSeparatorChar((String)getSeparatorChar().getValue());
        result.setSkipLines(((Integer)getSkipLines().getValue()).intValue());
        result.setColumns(getColumns());
        
        settings.setAttribute(format.getType().getSimpleName() + ".defaultprofile",result.getName());
        close();
      }
    },null,false,"ok.png");
    b2.addButton(new Cancel());
    c.addButtonArea(b2);
  }
  
  /**
   * Liefert die aktuelle Spalten-Zuordnung.
   * @return die aktuelle Spalten-Zuordnung.
   */
  private List<Column> getColumns()
  {
    // Spalten noch zuordnen
    List<Column> columns = new ArrayList<Column>();
    for (int i=0;i<selects.size();++i)
    {
      SelectInput input = selects.get(i);
      Column c = (Column) input.getValue();
      if (c == null)
        continue; // Spalte nicht zugeordnet
      
      // Spalten konnen mehrfach zugeordnet werden.
      // Daher verwenden wir einen Clone. Andernfalls wuerden
      // wir nur die letzte Zuordnung speichern
      try
      {
        c = (Column) c.clone();
      }
      catch (CloneNotSupportedException e) {/*dann halt nicht */}
      
      // Spaltennummer speichern
      c.setColumn(i);
      columns.add(c);
    }
    return columns;
  }
  
  /**
   * Laedt die CSV-Datei mit den aktuellen Parametern neu ein.
   */
  private void reload()
  {
    ICsvListReader csv = null;
    
    try
    {
      SWTUtil.disposeChildren(this.parent);
      this.parent.setLayout(new GridLayout());

      this.selects.clear();

      Profile p = getProfile();
      List<List<String>> lines = new ArrayList<List<String>>(); // Liste mit Zeilen mit Listen von Spalten %-)
      int cols = 0;

      ////////////////////////////////////////////////////////////////////////////
      // CSV-Datei einlesen
      CsvPreference prefs = p.createCsvPreference();
      
      final String enc = (String) this.getFileEncoding().getValue();
      Charset charset = null;
      
      try
      {
        charset = Charset.forName(enc);
      }
      catch (UnsupportedCharsetException e)
      {
        getError().setValue(i18n.tr("Encoding {0} ignoriert, wird nicht unterstützt",enc));
      }
      csv = new CsvListReader(new InputStreamReader(new ByteArrayInputStream(this.data),charset != null ? charset : Charset.defaultCharset()),prefs);
      List<String> line = null;
      while ((line = csv.read()) != null)
      {
        // Der CSV-Reader verwendet das List-Objekt leider immer
        // wieder (wird fuer die naechste Zeile geleert und neu
        // befuellt. Daher koennen wir sie nicht so einfach hier
        // reinpacken sondern muessen die Werte rauskopieren
        List<String> l = new ArrayList(line);
        lines.add(l);

        // Wir verwenden als Basis die Zeile mit den meisten Spalten
        cols = Math.max(l.size(),cols);
      }
      
      if (cols == 0)
      {
        this.getError().setValue(i18n.tr("CSV-Datei enthält keine Spalten"));
        return;
      }

      if (lines.size() == 0)
      {
        this.getError().setValue(i18n.tr("CSV-Datei enthält keine Zeilen"));
        return;
      }

      
      int skip = ((Integer)this.getSkipLines().getValue()).intValue();
      if (lines.size() <= skip)
      {
        this.getError().setValue(i18n.tr("CSV-Datei enthält nur {0} Zeilen",Integer.toString(lines.size())));
        return;
      }
      
      //
      ////////////////////////////////////////////////////////////////////////////

      List<Column> all     = format.getDefaultProfile().getColumns(); // Als Auswahlmoeglichkeit immer die aus dem Default
      List<Column> columns = p.getColumns(); // Die, die im Template tatsaechlich zugeordnet sind.
      List<String> current = lines.get(skip); // Die erste anzuzeigende Zeile

      ScrolledContainer container = new ScrolledContainer(this.parent);

      for (int i=0;i<cols;++i)
      {
        String value = "";
        try
        {
          value = current.get(i);
          if (value.length() > 30)
            value = value.substring(0,30) + "...";
        } catch (Exception e) {} // Spalte gibts in der Zeile nicht
        
        final SelectInput s = new SelectInput(all,getColumn(columns,i));
        s.setName((i+1) + ". " + (value != null ? value : "<" + i18n.tr("leer") + ">"));
        s.setPleaseChoose("<" + i18n.tr("Nicht zugeordnet") + ">");
        selects.add(s);

        container.addInput(s);
      }
      container.update();
      
      this.parent.layout(true);
      this.getError().setValue("");
    }
    catch (Exception e)
    {
      Logger.error("unable to read file",e);
      this.getError().setValue(i18n.tr("Fehler beim Lesen der Datei: {0}",e.getMessage()));
    }
    finally
    {
      IOUtil.close(csv);
    }
  }
  
  /**
   * Liefert die Spalte fuer die angegebene Nummer.
   * @param list Liste der Spalten.
   * @param nr die angegebene Nummer.
   * @return die Spalte oder NULL, wenn keine zugeordnet ist.
   */
  private Column getColumn(List<Column> list, int nr)
  {
    for (Column c:list)
    {
      if (c.getColumn() == nr)
        return c;
    }
    return null;
  }
  
  /**
   * Liefert das angepasste Mapping zurueck.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.result;
  }
  
  
  /**
   * Liefert ein Eingabe-Feld fuer das Trennzeichen.
   * @return Eingabe-Feld.
   */
  private TextInput getSeparatorChar()
  {
    if (this.sepChar != null)
      return this.sepChar;
    
    this.sepChar = new TextInput(getProfile().getSeparatorChar(),1);
    this.sepChar.setName(i18n.tr("Trennzeichen"));
    this.sepChar.setComment(i18n.tr("Zeichen, mit dem die Spalten getrennt sind"));
    this.sepChar.setMandatory(true);
    return this.sepChar;
  }
  
  /**
   * Liefert ein Label mit einer Fehlermeldung.
   * @return Label.
   */
  private LabelInput getError()
  {
    if (this.error != null)
      return this.error;
    
    this.error = new LabelInput("");
    this.error.setName("");
    this.error.setColor(Color.ERROR);
    return this.error;
  }

  /**
   * Liefert ein Eingabe-Feld fuer das Quoting-Zeichen.
   * @return Eingabe-Feld.
   */
  private TextInput getQuoteChar()
  {
    if (this.quoteChar != null)
      return this.quoteChar;
    
    this.quoteChar = new TextInput(this.getProfile().getQuotingChar(),1);
    this.quoteChar.setName(i18n.tr("Anführungszeichen"));
    this.quoteChar.setComment(i18n.tr("Zeichen, mit dem die Spalten umschlossen sind"));
    this.quoteChar.setMandatory(false);
    return this.quoteChar;
  }
  
  /**
   * Liefert ein Eingabe-Feld fuer den Zeichensatz.
   * @return Eingabe
   */
  private SelectInput getFileEncoding()
  {
    if (this.encoding != null)
      return this.encoding;
    
    this.encoding = new SelectInput(CHARSETS,this.getProfile().getFileEncoding());
    this.encoding.setName(i18n.tr("Zeichensatz"));
    this.encoding.setComment(i18n.tr("Zeichensatz der CSV-Datei"));
    this.encoding.setMandatory(true);
    this.encoding.setEditable(true);
    return this.encoding;
  }
  
  /**
   * Liefert ein Eingabe-Feld, mit dem ausgewaehlt werden kann, wieviele
   * Zeilen am Anfang uebersprungen werden sollen.
   * @return
   */
  private SpinnerInput getSkipLines()
  {
    if (this.skipLines != null)
      return this.skipLines;
    
    this.skipLines = new SpinnerInput(0,10,this.getProfile().getSkipLines());
    this.skipLines.setName(i18n.tr("Zeilen überspringen"));
    this.skipLines.setComment(i18n.tr("Zu überspringende Zeilen am Datei-Anfang"));
    this.skipLines.setMandatory(false);
    return this.skipLines;
  }
  
  /**
   * Liefert das aktuelle Profil.
   * @return das aktuelle Profil.
   */
  private Profile getProfile()
  {
    return (Profile) this.getProfiles().getValue();
  }
  
  /**
   * Liefert eine Auswahlbox mit den verfuegbaren Profilen.
   * @return eine Auswahlbox mit den verfuegbaren Profilen.
   * @throws ApplicationException
   */
  private SelectInput getProfiles()
  {
    if (this.profiles != null)
      return this.profiles;
    
    final List<Profile> list = ProfileUtil.read(this.format);
    final String name = settings.getString(format.getType().getSimpleName() + ".defaultprofile",null);
    Profile p = null;
    if (name != null)
    {
      for (Profile pr:list)
      {
        if (StringUtils.equals(pr.getName(),name))
        {
          p = pr;
          break;
        }
      }
    }
    this.profiles = new SelectInput(list,p);
    this.profiles.setAttribute("name");
    this.profiles.setName(i18n.tr("Profil"));
    this.profiles.addListener(new Listener() {
      @Override
      public void handleEvent(Event event)
      {
        if (event != null && event.type == SWT.Selection)
        {
          update();
          reload();
        }
      }
    });
    return this.profiles;
  }
  
  /**
   * Aktualisiert die Auswahl- und Eingabefelder nach Wechsel des Profils.
   */
  private void update()
  {
    Profile p = getProfile();
    Logger.info("changing profile to: " + p);
    
    // Einstellungen in die Eingabefelder uebernehmen
    this.getFileEncoding().setValue(p.getFileEncoding());
    this.getQuoteChar().setValue(p.getQuotingChar());
    this.getSeparatorChar().setValue(p.getSeparatorChar());
    this.getSkipLines().setValue(Integer.valueOf(p.getSkipLines()));
    
    // Datei mit den neuen Einstellungen laden
    reload();
  }
}
