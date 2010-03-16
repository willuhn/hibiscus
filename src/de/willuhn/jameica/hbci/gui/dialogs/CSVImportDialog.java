/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/CSVImportDialog.java,v $
 * $Revision: 1.6 $
 * $Date: 2010/03/16 00:44:17 $
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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.SpinnerInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.ScrolledContainer;
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
  private List<List<String>> lines  = null;
  private int columnCount           = 0;

  private TextInput sepChar         = null;
  private TextInput quoteChar       = null;
  private SelectInput encoding      = null;
  private SpinnerInput skipLines    = null;
  
  private List<LabelInput> labels   = null;
  private List<SelectInput> selects = null;
  
  
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
    this.setTitle(i18n.tr("Zuordnung der Spalten"));
    this.setSize(620,450);

    this.format = format;
    
    ////////////////////////////////////////////////////////////////////////////
    // CSV-Datei einlesen
    Profile p = this.getProfile();
    this.lines = new ArrayList<List<String>>(); // Liste mit Zeilen mit Listen von Spalten %-)

    CsvPreference prefs = CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE;
    String sep = p.getSeparatorChar();
    String quo = p.getQuotingChar();
    if (sep != null && sep.length() == 1) prefs.setDelimiterChar(sep.charAt(0));
    if (quo != null && quo.length() == 1) prefs.setQuoteChar(quo.charAt(0));
    ICsvListReader csv = new CsvListReader(new InputStreamReader(new ByteArrayInputStream(data),p.getFileEncoding()),prefs);
    List<String> line = null;
    while ((line = csv.read()) != null)
    {
      // Der CSV-Reader verwendet das List-Objekt leider immer
      // wieder (wird fuer die naechste Zeile geleert und neu
      // befuellt. Daher koennen wir sie nicht so einfach hier
      // reinpacken sondern muessen die Werte rauskopieren
      List<String> l = new ArrayList();
      l.addAll(line);
      this.lines.add(l);

      // Wir verwenden als Basis die Zeile mit den meisten Spalten
      if (l.size() > this.columnCount)
        this.columnCount = l.size();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    if (this.columnCount == 0)
      throw new ApplicationException(i18n.tr("CSV-Datei enthält keine Spalten"));

    if (this.lines.size() == 0)
      throw new ApplicationException(i18n.tr("CSV-Datei enthält keine Zeilen"));
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Profile p = this.getProfile();
    List<Column> columns = p.getColumns();

    List<String> line = lines.get(p.getSkipLines()); // Die erste anzuzeigende Zeile

    this.labels  = new ArrayList<LabelInput>();
    this.selects = new ArrayList<SelectInput>();

    
    // BUGZILLA 281
    LabelGroup options = new LabelGroup(parent,i18n.tr("Optionen"));
    options.addInput(this.getFileEncoding());
    options.addInput(this.getSeparatorChar());
    options.addInput(this.getQuoteChar());
    options.addInput(this.getSkipLines());
    
    // BUGZILLA 412
    new Headline(parent,i18n.tr("Zuordnung der Spalten"));
    ScrolledContainer container = new ScrolledContainer(parent);

    container.addText(i18n.tr("In der linken Spalte sehen Sie die erste Zeile Ihrer CSV-Datei.\n" +
                              "Ordnen Sie die Felder bitte über die Auswahl-Elemente auf der rechte Seite zu."),true);
    
    for (int i=0;i<this.columnCount;++i)
    {
      String value = "";
      try
      {
        value = line.get(i);
      } catch (Exception e) {} // Spalte gibts in der Zeile nicht
      
      final LabelInput l = new LabelInput((i+1) + ". " + value);
      labels.add(l);
      
      final SelectInput s = new SelectInput(columns,getColumn(columns,i));
      s.setPleaseChoose("<" + i18n.tr("Nicht zugeordnet") + ">");
      selects.add(s);

      container.addLabelPair(l,s);
    }
    
    ButtonArea b = new ButtonArea(parent,2);
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
    });
    b.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    });
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
      this.profile = (Profile) decoder.readObject();
      
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
    return this.profile;
  }
  
  
  
  /**
   * Liefert ein Eingabe-Feld fuer das Trennzeichen.
   * @return Eingabe-Feld.
   */
  private TextInput getSeparatorChar()
  {
    if (this.sepChar == null)
    {
      this.sepChar = new TextInput(this.profile.getSeparatorChar(),1);
      this.sepChar.setName(i18n.tr("Trennzeichen"));
      this.sepChar.setComment(i18n.tr("Zeichen, mit dem die Spalten getrennt sind"));
      this.sepChar.setMandatory(true);
    }
    return this.sepChar;
  }
  
  /**
   * Liefert ein Eingabe-Feld fuer das Quoting-Zeichen.
   * @return Eingabe-Feld.
   */
  private TextInput getQuoteChar()
  {
    if (this.quoteChar == null)
    {
      this.quoteChar = new TextInput(this.profile.getQuotingChar(),1);
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
      this.encoding = new SelectInput(CHARSETS,this.profile.getFileEncoding());
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
      this.skipLines = new SpinnerInput(0,this.lines.size()-1,this.profile.getSkipLines());
      this.skipLines.setName(i18n.tr("Zeilen überspringen"));
      this.skipLines.setComment(i18n.tr("Zu überspringende Zeilen am Datei-Anfang"));
      this.skipLines.setMandatory(false);
      this.skipLines.addListener(new Listener()
      {
        public void handleEvent(Event event)
        {
          // Wir aktualisieren die Preview
          List<String> line = null;
          
          try
          {
            int row = ((Integer)skipLines.getValue()).intValue();
            line = lines.get(row);
          }
          catch (Exception e) {} // Die ausgewaehlte Zeile existiert nicht
          
          for (int i=0;i<labels.size();++i)
          {
            String preview = "";
            try
            {
              preview = line.get(i);
            }
            catch (Exception e) {} // Die ausgewaehlte Spalte existiert nicht
            labels.get(i).setValue((i+1) + ". " + preview);
          }
        }
      });
    }
    return this.skipLines;
  }
}


/*********************************************************************
 * $Log: CSVImportDialog.java,v $
 * Revision 1.6  2010/03/16 00:44:17  willuhn
 * @N Komplettes Redesign des CSV-Imports.
 *   - Kann nun erheblich einfacher auch fuer andere Datentypen (z.Bsp.Ueberweisungen) verwendet werden
 *   - Fehlertoleranter
 *   - Mehrfachzuordnung von Spalten (z.Bsp. bei erweitertem Verwendungszweck) moeglich
 *   - modulare Deserialisierung der Werte
 *   - CSV-Exports von Hibiscus koennen nun 1:1 auch wieder importiert werden (Import-Preset identisch mit Export-Format)
 *   - Import-Preset wird nun im XML-Format nach ~/.jameica/hibiscus/csv serialisiert. Damit wird es kuenftig moeglich sein,
 *     CSV-Import-Profile vorzukonfigurieren und anschliessend zu exportieren, um sie mit anderen Usern teilen zu koennen
 *
 * Revision 1.5  2008/04/24 11:37:21  willuhn
 * @N BUGZILLA 304
 **********************************************************************/