/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/CSVImportDialog.java,v $
 * $Revision: 1.11 $
 * $Date: 2011/06/01 21:19:16 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
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
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog zum Importieren von CSV-Daten.
 */
public class CSVImportDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static String[] CHARSETS = new String[]{"ISO-8859-1","ISO-8859-15","UTF-8"};

  private Format format             = null;
  private Profile profile           = null;
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
    this.setSize(620,500);

  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    // BUGZILLA 281
    Container options = new SimpleContainer(parent);
    options.addHeadline(i18n.tr("Optionen"));
    options.addInput(this.getFileEncoding());
    options.addInput(this.getSeparatorChar());
    options.addInput(this.getQuoteChar());
    options.addInput(this.getSkipLines());
    
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
    
    ButtonArea b = new ButtonArea();
    b.addButton(i18n.tr("Übernehmen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        Profile p = getProfile();
        p.setFileEncoding((String)getFileEncoding().getValue());
        p.setQuotingChar((String)getQuoteChar().getValue());
        p.setSeparatorChar((String)getSeparatorChar().getValue());
        p.setSkipLines(((Integer)getSkipLines().getValue()).intValue());
        
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
        p.setColumns(columns);
        
        storeProfile(p);
        close();
      }
    },null,false,"ok.png");
    b.addButton(i18n.tr("Datei neu laden"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        Profile p = getProfile();
        p.setFileEncoding((String)getFileEncoding().getValue());
        p.setQuotingChar((String)getQuoteChar().getValue());
        p.setSeparatorChar((String)getSeparatorChar().getValue());
        p.setSkipLines(((Integer)getSkipLines().getValue()).intValue());
        reload();
      }
    },null,false,"view-refresh.png");
    b.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
    
    c.addButtonArea(b);
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

      Profile p = this.getProfile();
      List<List<String>> lines = new ArrayList<List<String>>(); // Liste mit Zeilen mit Listen von Spalten %-)
      int cols = 0;

      ////////////////////////////////////////////////////////////////////////////
      // CSV-Datei einlesen
      CsvPreference prefs = p.createCsvPreference();
      csv = new CsvListReader(new InputStreamReader(new ByteArrayInputStream(this.data),p.getFileEncoding()),prefs);
      List<String> line = null;
      while ((line = csv.read()) != null)
      {
        // Der CSV-Reader verwendet das List-Objekt leider immer
        // wieder (wird fuer die naechste Zeile geleert und neu
        // befuellt. Daher koennen wir sie nicht so einfach hier
        // reinpacken sondern muessen die Werte rauskopieren
        List<String> l = new ArrayList();
        l.addAll(line);
        lines.add(l);

        // Wir verwenden als Basis die Zeile mit den meisten Spalten
        if (l.size() > cols)
          cols = l.size();
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

      if (lines.size() <= p.getSkipLines())
      {
        this.getError().setValue(i18n.tr("CSV-Datei enthält nur {0} Zeilen",Integer.toString(lines.size())));
        return;
      }
      
      //
      ////////////////////////////////////////////////////////////////////////////

      List<Column> columns = p.getColumns();
      List<String> current = lines.get(p.getSkipLines()); // Die erste anzuzeigende Zeile

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
        
        final SelectInput s = new SelectInput(columns,getColumn(columns,i));
        s.setName((i+1) + ". " + value);
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
   * Prueft, ob ein serialisiertes, vom User abgespeichertes Profil existiert
   * und laedt dieses. Andernfalls wird das Default-Profil des Formats verwendet.
   * @return das Profil. Nie NULL sondern hoechstens das Default-Profil.
   */
  private Profile getProfile()
  {
    if (this.profile != null)
      return this.profile;

    Profile defaultProfile = this.format.getDefaultProfile();

    // 1. Wir nehmen erstmal das Default-Profil
    this.profile = defaultProfile;
    
    // 2. Mal schauen, ob wir ein gespeichertes Profil fuer das Format haben
    File dir = new File(Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath(),"csv");
    if (!dir.exists())
    {
      // Wir erstellen das Verzeichnis - dann muss es der User nicht selbst anlegen
      Logger.info("creating " + dir);
      dir.mkdirs();
      return this.profile; // Wenn das Verzeichnis noch nicht existierte, gibts auch kein Benutzer-Profil
    }
    
    File file = new File(dir,this.format.getClass().getName() + ".xml");
    if (!file.exists() || !file.canRead())
      return this.profile; // XML-Datei gibts nicht oder nicht lesbar - also Default-Profil
    
    Logger.info("reading csv profile " + file);
    XMLDecoder decoder = null;
    try
    {
      decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(file)));
      decoder.setExceptionListener(new ExceptionListener()
      {
        public void exceptionThrown(Exception e)
        {
          throw new RuntimeException(e);
        }
      });
      Profile p = (Profile) decoder.readObject();
      
      // Versionsnummer pruefen
      if (defaultProfile.getVersion() > p.getVersion())
      {
        Logger.info("default profile has changed, new version number " + defaultProfile.getVersion() + ". skipping serialized profile");
        return this.profile;
      }
        
        
      this.profile = p;
      
      // Der User hat beim letzten Mal eventuell nicht alle Spalten zugeordnet.
      // Die wuerden jetzt hier in dem Objekt fehlen. Daher nehmen wir
      // noch die Spalten aus dem Default-Profil und haengen die fehlenden noch an.
      List<Column> currentColumns = this.profile.getColumns();
      List<Column> defaultColumns = defaultProfile.getColumns();
      for (Column cd:defaultColumns)
      {
        String n1 = cd.getProperty();
        if (n1 == null)
          continue; // Sollte es eigentlich nicht geben
        
        cd.setColumn(-1); // gilt als nicht zugeordnet
        
        // Checken, ob im serialisierten Profil enthalten
        boolean found = false;
        for (Column cc:currentColumns)
        {
          String n2 = cc.getProperty();
          if (n2 == null)
            continue;
          if (n1.equals(n2))
          {
            found = true;
            break;
          }
        }
        
        // Wenn wir die Spalte im serialisierten Profil nicht gefunden
        // haben, haengen wir sie als nicht zugeordnet hinten dran
        if (!found)
          currentColumns.add(cd);
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to read profile " + file,e);
    }
    finally
    {
      if (decoder != null) {
        try {
          decoder.close();
        }
        catch (Exception e) { /* useless */}
      }
    }
    
    return this.profile;
  }
  
  
  /**
   * Serialisiert das Profil.
   * @param profile das zu serialisierende Profil.
   */
  private void storeProfile(Profile profile)
  {
    // 2. Mal schauen, ob wir ein gespeichertes Profil fuer das Format haben
    File dir = new File(Application.getPluginLoader().getPlugin(HBCI.class).getResources().getWorkPath(),"csv");
    File file = new File(dir,this.format.getClass().getName() + ".xml");
    
    Logger.info("writing csv profile " + file);
    XMLEncoder encoder = null;
    try
    {
      encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)));
      encoder.setExceptionListener(new ExceptionListener()
      {
        public void exceptionThrown(Exception e)
        {
          throw new RuntimeException(e);
        }
      });
      encoder.writeObject(profile);
    }
    catch (Exception e)
    {
      Logger.error("unable to store profile " + file,e);
    }
    finally
    {
      if (encoder != null) {
        try {
          encoder.close();
        }
        catch (Exception e) { /* useless */}
      }
    }
  }

  /**
   * Liefert das angepasste Mapping zurueck.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.getProfile();
  }
  
  
  /**
   * Liefert ein Eingabe-Feld fuer das Trennzeichen.
   * @return Eingabe-Feld.
   */
  private TextInput getSeparatorChar()
  {
    if (this.sepChar == null)
    {
      this.sepChar = new TextInput(this.getProfile().getSeparatorChar(),1);
      this.sepChar.setName(i18n.tr("Trennzeichen"));
      this.sepChar.setComment(i18n.tr("Zeichen, mit dem die Spalten getrennt sind"));
      this.sepChar.setMandatory(true);
    }
    return this.sepChar;
  }
  
  /**
   * Liefert ein Label mit einer Fehlermeldung.
   * @return Label.
   */
  private LabelInput getError()
  {
    if (this.error == null)
    {
      this.error = new LabelInput("");
      this.error.setName("");
      this.error.setColor(Color.ERROR);
    }
    return this.error;
  }

  /**
   * Liefert ein Eingabe-Feld fuer das Quoting-Zeichen.
   * @return Eingabe-Feld.
   */
  private TextInput getQuoteChar()
  {
    if (this.quoteChar == null)
    {
      this.quoteChar = new TextInput(this.getProfile().getQuotingChar(),1);
      this.quoteChar.setName(i18n.tr("Anführungszeichen"));
      this.quoteChar.setComment(i18n.tr("Zeichen, mit dem die Spalten umschlossen sind"));
      this.quoteChar.setMandatory(false);
    }
    return this.quoteChar;
  }
  
  /**
   * Liefert ein Eingabe-Feld fuer den Zeichensatz.
   * @return Eingabe
   */
  private SelectInput getFileEncoding()
  {
    if (this.encoding == null)
    {
      this.encoding = new SelectInput(CHARSETS,this.getProfile().getFileEncoding());
      this.encoding.setName(i18n.tr("Zeichensatz"));
      this.encoding.setComment(i18n.tr("Zeichensatz der CSV-Datei"));
      this.encoding.setMandatory(true);
      this.encoding.setEditable(true);
    }
    return this.encoding;
  }
  
  /**
   * Liefert ein Eingabe-Feld, mit dem ausgewaehlt werden kann, wieviele
   * Zeilen am Anfang uebersprungen werden sollen.
   * @return
   */
  private SpinnerInput getSkipLines()
  {
    if (this.skipLines == null)
    {
      this.skipLines = new SpinnerInput(0,10,this.getProfile().getSkipLines());
      this.skipLines.setName(i18n.tr("Zeilen überspringen"));
      this.skipLines.setComment(i18n.tr("Zu überspringende Zeilen am Datei-Anfang"));
      this.skipLines.setMandatory(false);
    }
    return this.skipLines;
  }
}
