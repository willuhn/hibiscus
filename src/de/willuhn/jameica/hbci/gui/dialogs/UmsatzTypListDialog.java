/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.ColorUtil;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.UmsatzTypBean;
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
  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(UmsatzTypListDialog.class);
  
  private final static int WINDOW_WIDTH = 370;
  private final static int WINDOW_HEIGHT = 500;

  private List<UmsatzTypBean> list = null;
  private UmsatzTyp choosen        = null;
  private int typ                  = UmsatzTyp.TYP_EGAL;
  
  private TextInput search         = null;
  private CheckboxInput children   = null;
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
    
    this.list =  UmsatzTypUtil.getList(null,this.typ);

    this.setTitle(i18n.tr("Auswahl der Kategorie"));
    setSize(settings.getInt("window.width",WINDOW_WIDTH),settings.getInt("window.height",WINDOW_HEIGHT));
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
    Container group = new SimpleContainer(parent,true);
    
    group.addText(i18n.tr("Bitte wählen Sie die zu verwendende Kategorie aus."),true);
    TextInput text = this.getSearch();
    group.addInput(text);
    group.addPart(this.getTable());
    group.addInput(this.getChildren());

    ////////////////
    // geht erst nach dem Paint
    if (this.choosen != null)
      this.getTable().select(new UmsatzTypBean(this.choosen));
    
    
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
    
    // Unabhaengig von dem, was der User als Groesse eingestellt hat, bleibt das die Minimalgroesse.
    getShell().setMinimumSize(WINDOW_WIDTH,WINDOW_HEIGHT);
    
    getShell().addDisposeListener(new DisposeListener() {
      
      @Override
      public void widgetDisposed(DisposeEvent e)
      {
        Shell shell = getShell();
        if (shell == null || shell.isDisposed())
          return;
        
        final Point size = shell.getSize();
        Logger.debug("saving window size: " + size.x + "x" + size.y);
        settings.setAttribute("window.width",size.x);
        settings.setAttribute("window.height",size.y);
      }
    });
  }
  
  /**
   * Liefert eine Checkbox, mit der festgelegt werden kann, ob bei einer Suche die Unterkategorien mit angezeigt werden sollen.
   * Auch denn, wenn sie den Suchbegriff nicht enthalten.
   * @return die Checkbox.
   */
  private CheckboxInput getChildren()
  {
    if (this.children != null)
      return this.children;
    
    this.children = new CheckboxInput(settings.getBoolean("search.children",true));
    this.children.setName(i18n.tr("Unterkategorien von Treffern anzeigen"));
    this.children.addListener(e -> {
      settings.setAttribute("search.children",((Boolean) this.children.getValue()).booleanValue());
      update();
    });
    return this.children;
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
    this.table.setRememberColWidths(true);
    this.table.addColumn(i18n.tr("Bezeichnung"),"indented");
    this.table.addColumn(i18n.tr("Kommentar"),"kommentar");
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
          UmsatzTypBean b = (UmsatzTypBean) item.getData();
          if (b == null)
            return;
          
          UmsatzTyp ut = b.getTyp();
          if (ut == null)
            return;
          
          // Uebergeordnete Kategorie nur anzeigen, wenn sie derzeit aufgrund eines Filters nicht angezeigt wird
          String q = (String) getSearch().getValue();
          if (StringUtils.trimToNull(q) != null) // Wenn nichts in der Suche steht, muss die Eltern-Kategorie da sein
          {
            // Nur dann etwas eintragen, wenn sie ein Parent hat und wenn dieses derzeit nicht angezeigt wird
            UmsatzTypBean parent = b.getParent();
            if (parent != null && !table.getItems().contains(parent))
              item.setText(0,b.getPathName());
          }
          
          Color c = null;
          
          if (ut.isCustomColor())
          {
            c = ColorUtil.getColor(ut);
          }
          else
          {
            int t = ut.getTyp();
            if (t == UmsatzTyp.TYP_AUSGABE)
              c = Settings.getBuchungSollForeground();
            else if (t == UmsatzTyp.TYP_EINNAHME)
              c = Settings.getBuchungHabenForeground();
            else
              c = de.willuhn.jameica.gui.util.Color.FOREGROUND.getSWTColor();
          }
          
          if (c != null)
            item.setForeground(c);
        }
        catch (Exception e)
        {
          Logger.error("unable to apply custom color",e);
        }
      }
    });
    
    this.table.addSelectionListener(e -> getApplyButton().setEnabled(e.data != null));
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
      UmsatzTypBean b = (UmsatzTypBean) getTable().getSelection();
      choosen = b != null ? b.getTyp() : null;
      if (choosen != null)
        close();
    }
  }
  
  /**
   * Aktualisiert die Anzeige.
   */
  private void update()
  {
    TablePart table = getTable();
    table.removeAll();
    
    final boolean children = ((Boolean) getChildren().getValue()).booleanValue();
    String text = (String) getSearch().getValue();
    text = text.trim().toLowerCase();
    
    try
    {
      final Set<String> lookup = children ? new HashSet<String>() : null;
      for (UmsatzTypBean t:list)
      {
        if (text.length() == 0)
        {
          table.addItem(t);
          continue;
        }
        
        if (!t.getTyp().getName().toLowerCase().contains(text))
          continue;

        addItems(t,lookup);
      }
    }
    catch (RemoteException re)
    {
      Logger.error("error while adding items to table",re);
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
        update();
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
  
  /**
   * Fuegt das Element zur Liste hinzu und eventuell die Kinder.
   * @param b das Element.
   * @param lookup Set mit Identifiern der bereits hinzugefügten wenn auch Kinder der Treffer angezeigt werden sollen.
   * @throws RemoteException
   */
  private void addItems(UmsatzTypBean b, Set<String> lookup) throws RemoteException
  {
    // Wenn wir auch Kinder von Treffern anzeigen, kann es sein, dass ein Element
    // mehrfach gematcht wird. Einmal direkt über den Suchbegriff und dann nochmal
    // als Kind einer übergeordneten Kategorie, falls deren Name ebenfalls auf den
    // Suchbegriff passt
    if (lookup != null)
    {
      if (lookup.contains(b.getID()))
        return;
      
      lookup.add(b.getID());
    }
    final TablePart table = this.getTable();
    table.addItem(b);
    if (lookup == null)
      return;

    final GenericIterator<UmsatzTypBean> children = b.getChildren();
    while (children.hasNext())
    {
      addItems(children.next(),lookup);
    }
  }
  
}
