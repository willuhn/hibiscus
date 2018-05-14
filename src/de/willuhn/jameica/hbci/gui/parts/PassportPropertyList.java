/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste der BPD/UPD an.
 */
public class PassportPropertyList implements Part
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private final static String PREFIX_BPD = "BPD";
  private final static String PREFIX_UPD = "UPD";
  
  private HBCIPassport passport = null;
  private List<Value> list      = new ArrayList<Value>();
  private PropertyTable table   = null;
  private TextInput search      = null;

  /**
   * ct.
   * @param passport Passport.
   */
  public PassportPropertyList(HBCIPassport passport)
  {
    this.passport = passport;
  }
  
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    Container container = new SimpleContainer(parent);

    this.list.addAll(init(PREFIX_BPD,this.passport.getBPD()));
    this.list.addAll(init(PREFIX_UPD,this.passport.getUPD()));

    this.search = new TextInput(null);
    this.search.setName(i18n.tr("Suche"));
    container.addInput(this.search);
    this.search.getControl().addKeyListener(new DelayedAdapter());
    
    this.table = new PropertyTable();
    this.table.paint(parent);
  }
  
  /**
   * Loescht alle BPD aus der Tabelle.
   */
  public synchronized void clearBPD()
  {
    List<Value> newList = new ArrayList<Value>();
    for (Value value:this.list)
    {
      if (!PREFIX_BPD.equals(value.prefix))
        newList.add(value);
    }
    this.list = newList;
    reload();
  }
  
  /**
   * Initialisiert die Liste der Werte.
   * @param prefix BPD/UPD.
   * @param props Properties aus dem Passport.
   * @return die Liste der Beans.
   */
  private static List<Value> init(String prefix, Properties props)
  {
    List<Value> l = new ArrayList<Value>();
    if (props == null)
      return l;

    String[] keys = props.keySet().toArray(new String[props.size()]);
    // Alphabetisch sortieren
    Arrays.sort(keys);

    for (String key:keys)
      l.add(new Value(prefix,key,props.getProperty(key)));
    return l;
  }
  
  /**
   * Aktualisiert die Daten.
   */
  private void reload()
  {
    try
    {
      String text = (String) search.getValue();
      table.removeAll();

      String lower = null;
      for (Value v:list)
      {
        if (text == null || text.length() == 0)
        {
          table.addItem(v);
          continue;
        }

        String prefix = v.prefix;
        String name = v.name;
        String value = v.value;
        
        if (name == null) name = "";
        if (value == null) value = "";
        if (lower == null) lower = text.toLowerCase();
        
        if (name.toLowerCase().indexOf(lower) != -1 || 
            value.toLowerCase().indexOf(lower) != -1 ||
            prefix.toLowerCase().indexOf(lower) != -1)
          table.addItem(v);
      }
    }
    catch (OperationCanceledException oce)
    {
      Logger.info("operation cancelled");
    }
    catch (Exception e)
    {
      Logger.error("unable to perform search",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Ausführen der Suche: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Listener fuer die Suche.
   */
  private class DelayedAdapter extends KeyAdapter
  {
    private Listener forward = new DelayedListener(new Listener()
    {
      /**
       * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
       */
      public void handleEvent(Event event)
      {
        // hier kommt dann das verzoegerte Event an.
        reload();
      }
    
    });

    /**
     * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
     */
    public void keyReleased(KeyEvent e)
    {
      forward.handleEvent(null);
    }
  }
    
  /**
   * Kapslet eine Tabelle mit BPD/UPD-Properties.
   */
  private class PropertyTable extends TablePart
  {
    /**
     * ct.
     */
    private PropertyTable()
    {
      super(list,null);
      this.setRememberColWidths(true);
      this.setSummary(true);
      
      this.addColumn(i18n.tr("BPD/UPD"),"prefix");
      this.addColumn(i18n.tr("Parameter"),"name");
      this.addColumn(i18n.tr("Wert"),"value");
    }
  }

  /**
   * Kapselt ein einzelnes Property aus den BPD/UPD.
   */
  private static class Value implements GenericObject
  {
    private String prefix = null;
    private String name   = null;
    private String value  = null;
    
    /**
     * ct.
     * @param prefix
     * @param name
     * @param value
     */
    private Value(String prefix, String name, String value)
    {
      this.prefix = prefix;
      this.name  = name;
      this.value = value;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject other) throws RemoteException
    {
      if (other == null || !(other instanceof Value))
        return false;
      return this.getID().equals(other.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) throws RemoteException
    {
      if ("name".equals(name))
        return this.name;
      if ("prefix".equals(name))
        return this.prefix;
      return this.value;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[]{"prefix","name","value"};
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return this.prefix + this.name + this.value;
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


/**********************************************************************
 * $Log: PassportPropertyList.java,v $
 * Revision 1.3  2011/05/16 09:55:29  willuhn
 * @N Funktion zum Loeschen der BPD
 *
 * Revision 1.2  2009/06/29 09:17:11  willuhn
 * @B NPE
 *
 * Revision 1.1  2009/06/16 15:34:19  willuhn
 * @N Dialog zum Anzeigen der BPD/UPD
 *
 **********************************************************************/
