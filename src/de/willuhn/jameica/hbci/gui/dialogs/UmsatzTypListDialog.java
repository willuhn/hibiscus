/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/UmsatzTypAuswahlDialog.java,v $
 * $Revision: 1.14 $
 * $Date: 2011/05/06 09:05:35 $
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
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.UmsatzTypUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Fertig konfigurierter Dialog zur Auswahl einer Umsatz-Kategorie.
 * Jedoch nicht anhand einer Selectbox sondern als Tabelle mit Suchfeld.
 * Das laesst sich besser mit der Tastatur bedienen.
 */
public class UmsatzTypListDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private static Hashtable<String,Color> colorCache = new Hashtable<String,Color>();
  
  private List<FormattedType> list = null;
  private UmsatzTyp choosen        = null;
  private int typ                  = UmsatzTyp.TYP_EGAL;
  
  private TextInput search         = null;
  private TablePart table          = null;
  private Button apply             = null;
  
  /**
   * ct.
   * @param position
   * @param preselected der vorausgewaehlte Umsatztyp.
   * @param typ Filter auf Kategorie-Typen.
   * Kategorien vom Typ "egal" werden grundsaetzlich angezeigt.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @throws RemoteException
   */
  public UmsatzTypListDialog(int position, UmsatzTyp preselected, int typ) throws RemoteException
  {
    super(position);
    this.choosen = preselected;
    this.typ = typ;

    this.setTitle(i18n.tr("Auswahl der Kategorie"));
    this.setSize(370,400);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return choosen;
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    this.list = this.init(this.typ);
    
    Container group = new SimpleContainer(parent,true);
    
    group.addText(i18n.tr("Bitte wählen Sie die zu verwendende Kategorie aus."),true);
    TextInput text = this.getSearch();
    group.addInput(text);
    group.addPart(this.getTable());

    ////////////////
    // geht erst nach dem Paint
    if (this.choosen != null)
      this.getTable().select(new FormattedType(this.choosen));
    
    
    text.getControl().addKeyListener(new DelayedAdapter());
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(this.getApplyButton());
    buttons.addButton(i18n.tr("K&eine Kategorie"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        choosen = null;
        close();
      }
    },null,false,"list-remove.png");
    buttons.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
    
    group.addButtonArea(buttons);
  }
  
  /**
   * Liefert den Apply-Button.
   * @return der Apply-Button.
   */
  private Button getApplyButton()
  {
    if (this.apply != null)
      return this.apply;
    
    this.apply = new Button(i18n.tr("Übernehmen"),new Apply(),null,true,"ok.png");
    this.apply.setEnabled(false); // initial deaktiviert
    return this.apply;
  }
  
  /**
   * Liefert das Such-Feld.
   * @return das Such-Feld.
   */
  private TextInput getSearch()
  {
    if (this.search != null)
      return this.search;
    
    this.search = new TextInput("");
    this.search.focus();
    this.search.setName(i18n.tr("Suchbegriff"));
    return this.search;
  }
  
  /**
   * Liefert die Tabelle mit den Kategorien.
   * @return die Tabelle mit den Kategorien.
   */
  private TablePart getTable()
  {
    if (this.table != null)
      return this.table;
    
    this.table = new TablePart(this.list,new Apply());
    this.table.setSummary(false);
    this.table.addColumn(i18n.tr("Bezeichnung"),"name");
    this.table.setFormatter(new TableFormatter()
    {
      /**
       * @see de.willuhn.jameica.gui.formatter.TableFormatter#format(org.eclipse.swt.widgets.TableItem)
       */
      public void format(TableItem item)
      {
        if (item == null)
          return;

        try
        {
          FormattedType ft = (FormattedType) item.getData();
          if (ft == null)
            return;
          
          UmsatzTyp ut = ft.type;
          if (ut == null)
            return;
          
          Color c = null;
          
          if (ut.isCustomColor())
          {
            int[] color = ut.getColor();
            if (color == null || color.length != 3)
              return;
            
            RGB rgb = new RGB(color[0],color[1],color[2]);
            c = colorCache.get(rgb.toString());
            if (c == null)
            {
              c = new Color(GUI.getDisplay(),rgb);
              colorCache.put(rgb.toString(),c);
            }
          }
          else
          {
            int t = ut.getTyp();
            if (t == UmsatzTyp.TYP_AUSGABE)
              c = Settings.getBuchungSollForeground();
            else if (t == UmsatzTyp.TYP_EINNAHME)
              c = Settings.getBuchungHabenForeground();
            else
              c = de.willuhn.jameica.gui.util.Color.WIDGET_FG.getSWTColor();
          }
          item.setForeground(c);
        }
        catch (Exception e)
        {
          Logger.error("unable to apply custom color",e);
        }
      }
    });
    
    this.table.addSelectionListener(new Listener() {
      public void handleEvent(Event event)
      {
        getApplyButton().setEnabled(event.data != null);
      }
    });
    this.getApplyButton().setEnabled(this.choosen != null);
    return this.table;
  }
  
  /**
   * Action fuer das Uebernehmen der Auswahl.
   */
  private class Apply implements Action
  {
    public void handleAction(Object context) throws ApplicationException
    {
      FormattedType ft = (FormattedType) getTable().getSelection();
      choosen = ft != null ? ft.type : null;
      if (choosen != null)
        close();
    }
  }
  
  /**
   * Initialisiert die Liste der anzuzeigenden Kategorien.
   * @param typ der Kategorie-Typ.
   * @return initialisierte Liste.
   * @throws RemoteException
   */
  private List<FormattedType> init(int typ) throws RemoteException
  {
    List<FormattedType> l = new LinkedList<FormattedType>();
    DBIterator list = UmsatzTypUtil.getRootElements();
    while (list.hasNext())
    {
      add((UmsatzTyp)list.next(),l,typ);
    }
    return l;
  }
  
  /**
   * Fuegt die Kinder der Kategorie hinzu.
   * @param t die Kategorie.
   * @param l die Liste, wo die Kinder drin landen sollen.
   * @param typ Typ-Filter.
   * @throws RemoteException
   */
  private void add(UmsatzTyp t, List<FormattedType> l, int typ) throws RemoteException
  {
    // Wir filtern hier zwei Faelle:
    
    // a) typ == TYP_EGAL -> es wird nichts gefiltert
    // b) typ != TYP_EGAL -> es werden nur die angezeigt, bei denen TYP_EGAL oder Typ passt
    
    int ti = t.getTyp();
    if (typ == UmsatzTyp.TYP_EGAL || (ti == UmsatzTyp.TYP_EGAL || ti == typ))
    {
      l.add(new FormattedType(t));
      
      GenericIterator children = t.getChildren();
      while (children.hasNext())
      {
        add((UmsatzTyp) children.next(),l,typ);
      }
    }
  }
  
  
  /**
   * Hilfsklasse, um die Einrueckungen anzuzeigen.
   * Wir berechnen das vor, damit es schneller geht. Ginge
   * auch per Formatter live - aber das wuerde unnoetig Rechenzeit
   * verbrauchen.
   */
  public class FormattedType
  {
    private UmsatzTyp type = null;
    private String name    = null;
    private String lower   = null;
    
    /**
     * ct.
     * @param type der Umsatz-Typ.
     * @throws RemoteException
     */
    private FormattedType(UmsatzTyp type) throws RemoteException
    {
      this.type  = type;
      this.name  = this.type.getName();
      this.lower = this.name.toLowerCase();
      
      try
      {
        int depth = type.getPath().size();
        for (int i=0;i<depth;++i)
        {
          this.name = "    " + this.name;
        }
      }
      catch (Exception e)
      {
        Logger.error("unable to indent category name",e);
      }
    }
    
    /**
     * Liefert den Namen der Kategorie.
     * @return der Name der Kategorie.
     */
    public String getName()
    {
      return this.name;
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object other)
    {
      if (!(other instanceof FormattedType))
        return false;

      try
      {
        return BeanUtil.equals(this.type,((FormattedType)other).type);
      }
      catch (RemoteException re)
      {
        Logger.error("error while comparing categories",re);
        return false;
      }
    }
  }
  
  /**
   * Da KeyAdapter/KeyListener nicht von swt.Listener abgeleitet
   * sind, muessen wir leider dieses schraege Konstrukt verenden,
   * um den DelayedListener verwenden zu koennen
   */
  private class DelayedAdapter extends KeyAdapter
  {
    private Listener forward = new DelayedListener(150,new Listener()
    {
      /**
       * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
       */
      public void handleEvent(Event event)
      {
        TablePart table = getTable();
        table.removeAll();
        
        String text = (String) getSearch().getValue();
        text = text.trim().toLowerCase();
        try
        {
          for (FormattedType t:list)
          {
            if (text.length() == 0)
            {
              table.addItem(t);
              continue;
            }
            
            if (t.lower.contains(text))
              table.addItem(t);
          }
        }
        catch (RemoteException re)
        {
          Logger.error("error while adding items to table",re);
        }
      }
    
    });

    /**
     * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
     */
    public void keyReleased(KeyEvent e)
    {
      forward.handleEvent(null); // Das Event-Objekt interessiert uns eh nicht
    }
  }
  
}
