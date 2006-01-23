/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/CSVImportDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2006/01/23 23:07:23 $
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

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
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

  private I18N i18n           = null;
  private CSVMapping mapping  = null;
  private String[] line       = null;
  
  private ArrayList selects   = null;
  private CheckboxInput skip  = null;
  
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
    // setSize(350,300);
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Spalten in der Import-Datei"));
    
    this.skip = new CheckboxInput(this.mapping.skipFirstLine());
    group.addCheckbox(this.skip,i18n.tr("Erste Zeile beim Import überspringen (Spalten-Name)"));

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
      
      group.addLabelPair((i+1) + ". " + line[i],select); 
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
        mapping.setSkipFirstLine(((Boolean)skip.getValue()).booleanValue());
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
 * Revision 1.2  2006/01/23 23:07:23  willuhn
 * @N csv import stuff
 *
 * Revision 1.1  2006/01/23 18:13:19  willuhn
 * @N first code for csv import
 *
 **********************************************************************/