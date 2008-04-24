/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/CSVImportDialog.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/04/24 11:37:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.io.CSVMapping;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog zum Importieren von CSV-Daten.
 */
public class CSVImportDialog extends AbstractDialog
{
  private final static String[] CHARSETS = new String[]{"ISO-8859-1","ISO-8859-15","UTF-8"};

  private I18N i18n           = null;
  private CSVMapping mapping  = null;
  private String[] line       = null;

  private ArrayList selects   = null;
  
  private TextInput sepChar       = null;
  private TextInput quoteChar     = null;
  private SelectInput encoding    = null;
  private CheckboxInput skipFirst = null;
  
  
  /**
   * ct.
   * @param line erste Zeile der zu importierenden Datei.
   * @param mapping das CSV-Mapping.
   * @param position
   */
  public CSVImportDialog(String[] line, CSVMapping mapping, int position)
  {
    super(position);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    this.line = line;
    this.mapping = mapping;
    setTitle(i18n.tr("Zuordnung der Spalten"));
    setSize(580,450);
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    // BUGZILLA 281
    LabelGroup options = new LabelGroup(parent,i18n.tr("Optionen"));
    options.addInput(this.getCharset());
    options.addInput(this.getSeparatorChar());
    options.addInput(this.getQuoteChar());
    options.addInput(this.getSkipFirst());
    
    // BUGZILLA 412
    new Headline(parent,i18n.tr("Feld-Zuordnungen"));
    ScrolledContainer container = new ScrolledContainer(parent);

    container.addText(i18n.tr("In der linken Spalte sehen Sie die erste Zeile Ihrer CSV-Datei.\n" +
        "Ordnen Sie die Felder bitte über die Auswahl-Elemente auf der rechte Seite zu."),true);
    

    this.selects   = new ArrayList();
    ArrayList list = new ArrayList();
    Hashtable ht   = this.mapping.getNames();
    Enumeration e  = ht.keys();

    // Zuallererst fuegen wir ein FieldObject fuer "keine Zuordnung" ein
    list.add(new FieldObject(null,i18n.tr("Nicht zugeordnet")));

    // und jetzt die aus dem CSV-Mapping.
    while (e.hasMoreElements())
    {
      String field = (String) e.nextElement();
      String name  = (String) ht.get(field);
      list.add(new FieldObject(field,name));
    }

    for (int i=0;i<line.length;++i)
    {
      // Wir erzeugen einen Iterator mit der Liste der zur Verfuegung stehenden Felder
      GenericIterator it = PseudoIterator.fromArray((FieldObject[]) list.toArray(new FieldObject[list.size()]));
      
      // Wir ermitteln das derzeitige Feld fuer diese Spalte
      FieldObject current = null;
      String field = this.mapping.get(i);
      String name  = null;
      if (field != null)
        name = (String) ht.get(field);

      // Nur wenn wir fuer diese Spalte beide Werte (Feld und Bezeichnung)
      // finden, markieren wird es als Vorauswahl.
      if (field != null && name != null)
        current = new FieldObject(field,name);
      final SelectInput select = new SelectInput(it, current);

      // Wir pappen die Select-Inputs in eine Liste, damit wir die Werte
      // spaeter wieder auslesen koennen.
      this.selects.add(select);
      
      container.addLabelPair((i+1) + ". " + line[i],select); 
    }
    
    ButtonArea b = new ButtonArea(parent,2);
    b.addButton(i18n.tr("Übernehmen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        // Hier Daten uebernehmen
        for (int i=0;i<selects.size();++i)
        {
          SelectInput select = (SelectInput) selects.get(i);
          FieldObject fo = (FieldObject) select.getValue();
          if (fo.field == null)
            mapping.remove(i); // kein Zuordnung
          else
            mapping.set(i,fo.field);
        }
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
   * Liefert das angepasste Mapping zurueck.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.mapping;
  }
  
  
  
  /**
   * Liefert ein Eingabe-Feld fuer das Trennzeichen.
   * @return Eingabe-Feld.
   */
  private TextInput getSeparatorChar()
  {
    if (this.sepChar == null)
    {
      this.sepChar = new TextInput(mapping.getSeparatorChar(),1);
      this.sepChar.setComment(i18n.tr("Zeichen, mit dem die Spalten getrennt sind"));
      this.sepChar.setMandatory(true);
      this.sepChar.setName(i18n.tr("Trennzeichen"));
      this.sepChar.addListener(new Listener() {
        public void handleEvent(Event event) {
          mapping.setSeparatorChar((String)sepChar.getValue());
        }
      
      });
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
      this.quoteChar = new TextInput(mapping.getQuotingChar(),1);
      this.quoteChar.setComment(i18n.tr("Zeichen, mit dem die Spalten umschlossen sind"));
      this.quoteChar.setMandatory(false);
      this.quoteChar.setName(i18n.tr("Anführungszeichen"));
      this.quoteChar.addListener(new Listener() {
        public void handleEvent(Event event) {
          mapping.setQuotingChar((String) quoteChar.getValue());
        }
      
      });
    }
    return this.quoteChar;
  }
  
  /**
   * Liefert ein Eingabe-Feld fuer den Zeichensatz.
   * @return Eingabe
   */
  private SelectInput getCharset()
  {
    if (this.encoding == null)
    {
      this.encoding = new SelectInput(CHARSETS,mapping.getFileEncoding());
      this.encoding.setComment(i18n.tr("Zeichensatz der CSV-Datei"));
      this.encoding.setName(i18n.tr("Zeichensatz"));
      this.encoding.setMandatory(true);
      this.encoding.setEditable(true);
      this.encoding.addListener(new Listener() {
        public void handleEvent(Event event) {
          mapping.setFileEncoding((String)encoding.getValue());
        }
      });
    }
    return this.encoding;
  }
  
  /**
   * Liefert eine Checkbox, mit der die erste Zeile der Datei ignoriert werden kann.
   * @return Checkbox.
   */
  private CheckboxInput getSkipFirst()
  {
    if (this.skipFirst == null)
    {
      this.skipFirst = new CheckboxInput(mapping.getSkipFirst());
      this.skipFirst.setName(i18n.tr("Erste Zeile der CSV-Datei überspringen"));
      this.skipFirst.addListener(new Listener() {
        public void handleEvent(Event event) {
          mapping.setSkipFirst(((Boolean) skipFirst.getValue()).booleanValue());
        }
      });
    }
    return this.skipFirst;
  }
  
  
  /**
   * Hilfsklasse zum Anzeigen der Feld-Bezeichner.
   */
  private class FieldObject implements GenericObject
  {
    private String field = null;
    private String name = null;
    
    /**
     * ct.
     * @param field Name des Feldes.
     * @param name sprechende Bezeichnung des Feldes.
     */
    private FieldObject(String field, String name)
    {
      this.field = field;
      this.name = name;
    }
    
    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      if ("name".equalsIgnoreCase(arg0))
        return name;
      return field;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"field","name"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return field;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "name";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject arg0) throws RemoteException
    {
      if (arg0 == null)
        return false;
      
      String id = getID();
      String id2 = arg0.getID();
      
      if (id == null && id2 == null)
        return true;
      
      if (id == null || id2 == null)
        return false;
      
      return this.getID().equals(arg0.getID());
    }
    
  }

}


/*********************************************************************
 * $Log: CSVImportDialog.java,v $
 * Revision 1.5  2008/04/24 11:37:21  willuhn
 * @N BUGZILLA 304
 *
 * Revision 1.4  2007/06/13 09:43:05  willuhn
 * @B Bug 412
 *
 * Revision 1.3  2006/08/21 23:15:01  willuhn
 * @N Bug 184 (CSV-Import)
 *
 * Revision 1.2  2006/01/23 23:07:23  willuhn
 * @N csv import stuff
 *
 * Revision 1.1  2006/01/23 18:13:19  willuhn
 * @N first code for csv import
 *
 **********************************************************************/