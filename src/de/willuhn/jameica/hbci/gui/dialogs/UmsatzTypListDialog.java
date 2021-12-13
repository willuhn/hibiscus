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

import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

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
  private static Hashtable<String,Color> colorCache = new Hashtable<String,Color>();
  
  private final static int WINDOW_WIDTH = 370;
  private final static int WINDOW_HEIGHT = 500;

  private List<UmsatzTypBean> list = null;
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
    
    this.list =  UmsatzTypUtil.getList(null,this.typ);

    this.setTitle(i18n.tr("Auswahl der Kategorie"));
    setSize(settings.getInt("window.width",WINDOW_WIDTH),settings.getInt("window.height",WINDOW_HEIGHT));
  }

  @Override
  protected Object getData() throws Exception
  {
    return choosen;
  }
  
  @Override
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent,true);
    
    group.addText(i18n.tr("Bitte w�hlen Sie die zu verwendende Kategorie aus."),true);
    TextInput text = this.getSearch();
    group.addInput(text);
    group.addPart(this.getTable());

    ////////////////
    // geht erst nach dem Paint
    if (this.choosen != null)
      this.getTable().select(new UmsatzTypBean(this.choosen));
    
    
    text.getControl().addKeyListener(new DelayedAdapter());
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(this.getApplyButton());
    buttons.addButton(i18n.tr("K&eine Kategorie"), new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        choosen = null;
        close();
      }
    },null,false,"list-remove.png");
    buttons.addButton(i18n.tr("Abbrechen"), new Action()
    {
      @Override
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
        
        Point size = shell.getSize();
        Logger.debug("saving window size: " + size.x + "x" + size.y);
        settings.setAttribute("window.width",size.x);
        settings.setAttribute("window.height",size.y);
      }
    });
  }
  
  /**
   * Liefert den Apply-Button.
   * @return der Apply-Button.
   */
  private Button getApplyButton()
  {
    if (this.apply != null)
      return this.apply;
    
    this.apply = new Button(i18n.tr("�bernehmen"),new Apply(),null,true,"ok.png");
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
      @Override
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
              c = de.willuhn.jameica.gui.util.Color.FOREGROUND.getSWTColor();
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
    @Override
    public void handleAction(Object context) throws ApplicationException
    {
      UmsatzTypBean b = (UmsatzTypBean) getTable().getSelection();
      choosen = b != null ? b.getTyp() : null;
      if (choosen != null)
        close();
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
      @Override
      public void handleEvent(Event event)
      {
        TablePart table = getTable();
        table.removeAll();
        
        String text = (String) getSearch().getValue();
        text = text.trim().toLowerCase();
        try
        {
          for (UmsatzTypBean t:list)
          {
            if (text.length() == 0)
            {
              table.addItem(t);
              continue;
            }
            
            if (t.getTyp().getName().toLowerCase().contains(text))
              table.addItem(t);
          }
        }
        catch (RemoteException re)
        {
          Logger.error("error while adding items to table",re);
        }
      }
    
    });

    @Override
    public void keyReleased(KeyEvent e)
    {
      forward.handleEvent(null); // Das Event-Objekt interessiert uns eh nicht
    }
  }
  
}
