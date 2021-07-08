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
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.Feature;
import de.willuhn.jameica.gui.parts.table.Feature.Context;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.ColorUtil;
import de.willuhn.jameica.hbci.gui.action.KontoFetchFromPassport;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.KontoartInput;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectMessage;
import de.willuhn.jameica.hbci.messaging.SaldoMessage;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste aller Konten.
 */
public class KontoList extends TablePart implements Part, Extendable
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();

  private TextInput search = null;
  private CheckboxInput onlyActive = null;
  private SelectInput accountType = null;

  private MessageConsumer mc = null;
  private boolean showFilter = true;

  private Listener listener = new MyListener();
  private KeyListener delayed = new MyDelayedListener();

  /**
   * ct.
   * @param konten
   * @param action
   */
  public KontoList(List<Konto> konten, Action action)
  {
    super(konten,action);
    this.addFeature(new FeatureSummary());

    addColumn(i18n.tr("Kontonummer"),"kontonummer",null,false,Column.ALIGN_RIGHT);
    addColumn(i18n.tr("Bankleitzahl"),"blz", new Formatter() {
      public String format(Object o)
      {
        if (o == null)
          return null;
        try
        {
          String blz = o.toString();
          String name = HBCIProperties.getNameForBank(blz);
          if (name == null || name.length() == 0)
            return blz;
          return blz + " [" + name + "]";
        }
        catch (Exception e)
        {
          Logger.error("error while formatting blz",e);
          return o.toString();
        }
      }
    });
    addColumn(i18n.tr("Bezeichnung"),"bezeichnung");
    addColumn(i18n.tr("Gruppe"),"kategorie");
    addColumn(i18n.tr("Notiz"),"kommentar");
    addColumn(i18n.tr("Verfahren"),"passport_class", new Formatter() {
      public String format(Object o)
      {
        if (o == null || !(o instanceof String))
          return null;
        Passport p;
        try
        {
          p = PassportRegistry.findByClass((String)o);
          return p.getName();
        }
        catch (Exception e)
        {
          Logger.error("error while loading hbci passport for konto",e);
          return i18n.tr("Fehler beim Ermitteln des Verfahrens");
        }
      }
    });
    addColumn(i18n.tr("Saldo"),"saldo",null,false,Column.ALIGN_RIGHT);
    addColumn(i18n.tr("Verfügbar"),"saldo_available",null,false,Column.ALIGN_RIGHT);
    // BUGZILLA 108 http://www.willuhn.de/bugzilla/show_bug.cgi?id=108
    addColumn(i18n.tr("Saldo aktualisiert am"),"saldo_datum", new DateFormatter(HBCI.LONGDATEFORMAT));
    
    final boolean bold = Settings.getBoldValues();

    setFormatter(new TableFormatter()
    {
      public void format(TableItem item)
      {
        Konto k = (Konto) item.getData();
        final int saldocolumn = 6;
        try {
          double saldo = k.getSaldo();
          if ((saldo == 0 && k.getSaldoDatum() == null) || Double.isNaN(saldo))
            item.setText(saldocolumn,"");
          else
            item.setText(saldocolumn,HBCI.DECIMALFORMAT.format(saldo) + " " + k.getWaehrung());

          double avail = k.getSaldoAvailable();
          if ((avail == 0 && k.getSaldoDatum() == null) || Double.isNaN(avail))
            item.setText(saldocolumn+1,"");
          else
            item.setText(saldocolumn+1,HBCI.DECIMALFORMAT.format(avail) + " " + k.getWaehrung());

          if (bold)
            item.setFont(saldocolumn,Font.BOLD.getSWTFont());

          item.setForeground(saldocolumn+1,ColorUtil.getForeground(k.getSaldoAvailable()));

          // Deaktivierte Konten grau
          if (k.hasFlag(Konto.FLAG_DISABLED))
            item.setForeground(Color.COMMENT.getSWTColor());

          // Offline-Konten blau
          else if (k.hasFlag(Konto.FLAG_OFFLINE))
            item.setForeground(Color.LINK.getSWTColor());

          // Sonst schwarz
          else
            item.setForeground(Color.FOREGROUND.getSWTColor());

          item.setForeground(saldocolumn,ColorUtil.getForeground(k.getSaldo()));
        }
        catch (RemoteException e)
        {
          Logger.error("error while formatting saldo",e);
        }
      }
    });

    // BUGZILLA 84 http://www.willuhn.de/bugzilla/show_bug.cgi?id=84
    setRememberOrder(true);

    // BUGZILLA 233 http://www.willuhn.de/bugzilla/show_bug.cgi?id=233
    setRememberColWidths(true);

    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.KontoList());

    this.setMulti(true);

    this.mc = new SaldoMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);

    this.addSelectionListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        featureEvent(Feature.Event.REFRESH,null);
      }
    });

    ExtensionRegistry.extend(this);
  }

  /**
   * @see de.willuhn.jameica.gui.extension.Extendable#getExtendableID()
   */
  public String getExtendableID()
  {
    return this.getClass().getName();
  }

  /**
   * Legt fest, ob die Filtermoeglichkeiten angezeigt werden sollen.
   * @param b true, wenn die Filtermoeglichkeiten angezeigt werden sollen.
   */
  public void setShowFilter(boolean b)
  {
    this.showFilter = b;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    if (this.showFilter)
    {
      // Daten initial laden
      reload();

      final TabFolder folder = new TabFolder(parent, SWT.NONE);
      folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      TabGroup tab = new TabGroup(folder,i18n.tr("Anzeige einschränken"));

      ColumnLayout cols = new ColumnLayout(tab.getComposite(),2);

      {
        Container c = new SimpleContainer(cols.getComposite());
        TextInput search = this.getText();
        c.addInput(search);
        search.getControl().addKeyListener(this.delayed);

        c.addInput(this.getActiveOnly());
      }

      {
        Container c = new SimpleContainer(cols.getComposite());
        c.addInput(this.getAccountType());
      }

      ButtonArea buttons = new ButtonArea();
      buttons.addButton(i18n.tr("Konten über den Bank-Zugang importieren..."), new Action() {
        public void handleAction(Object context) throws ApplicationException
        {
          new KontoFetchFromPassport().handleAction(getSelection());
        }
      },null,false,"mail-send-receive.png");
      buttons.addButton(i18n.tr("Konto manuell anlegen"),new KontoNew(),null,false,"list-add.png");
      buttons.paint(parent);
    }

    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });

    super.paint(parent);
  }

  /**
   * Liefert das Eingabefeld mit dem Suchbegriff.
   * @return das Eingabefeld mit dem Suchbegriff.
   */
  private TextInput getText()
  {
    if (this.search != null)
      return this.search;

    this.search = new TextInput(settings.getString("kontolist.filter.text",null),255);
    this.search.setName(i18n.tr("Suchbegriff"));
    return this.search;
  }

  /**
   * Liefert die Checkbox, mit der eingestellt werden kann, ob nur aktive Konten angezeigt werden sollen.
   * @return Checkbox.
   */
  private CheckboxInput getActiveOnly()
  {
    if (this.onlyActive != null)
      return this.onlyActive;

    this.onlyActive = new CheckboxInput(settings.getBoolean("kontolist.filter.active",false));
    this.onlyActive.setName(i18n.tr("Nur aktive Konten"));
    this.onlyActive.addListener(this.listener);
    return this.onlyActive;
  }

  /**
   * Liefert eine Auswahlbox mit der Kontoart.
   * @return eine Auswahlbox mit der Kontoart.
   */
  private SelectInput getAccountType()
  {
    if (this.accountType != null)
      return this.accountType;

    this.accountType = new KontoartInput(settings.getInt("kontolist.filter.type",-1));
    this.accountType.addListener(this.listener);
    return this.accountType;
  }

  /**
   * Laedt die Liste der Konten neu.
   */
  private void reload()
  {
    GUI.startSync(new Runnable() {

      @Override
      public void run()
      {
        try
        {
          removeAll();

          // Liste neu laden
          List<Konto> konten = getList();
          if (konten == null)
            return;

          for (Konto k:konten)
            addItem(k);

          // Sortierung wiederherstellen
          sort();
        }
        catch (Exception e)
        {
          Logger.error("error while reloading table",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren der Tabelle"), StatusBarMessage.TYPE_ERROR));
        }
      }
    });
  }

  /**
   * Liefert die Liste der gefundenen Konten.
   * @return die Liste der Konten.
   * @throws RemoteException
   */
  private List<Konto> getList() throws RemoteException
  {
    final String text = (String) this.getText().getValue();
    final boolean activeOnly = ((Boolean)this.getActiveOnly().getValue()).booleanValue();
    final Integer type = (Integer) getAccountType().getValue();

    Integer flags = activeOnly ? Konto.FLAG_DISABLED : null;
    List<Konto> list = KontoUtil.getKonten(KontoFilter.SEARCH(text,flags,type));

    // Speichern der Werte aus den Filter-Feldern.
    settings.setAttribute("kontolist.filter.text",text);
    settings.setAttribute("kontolist.filter.active",activeOnly);
    settings.setAttribute("kontolist.filter.type",type != null ? type.intValue() : -1);

    return list;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.TablePart#createFeatureEventContext(de.willuhn.jameica.gui.parts.table.Feature.Event, java.lang.Object)
   */
  @Override
  protected Context createFeatureEventContext(de.willuhn.jameica.gui.parts.table.Feature.Event e, Object data)
  {
    Context ctx = super.createFeatureEventContext(e, data);
    if (this.hasEvent(FeatureSummary.class,e))
      ctx.addon.put(FeatureSummary.CTX_KEY_TEXT,this.getSummaryText());
    return ctx;
  }

  /**
   * Liefert die Summenzeile.
   * @return die Summenzeile.
   */
  private String getSummaryText()
  {
    try
    {
      List<Konto> items = null;
      Object o = this.getSelection();

      // Nur wenn mehr als ein Konto markiert ist, nehmen
      // wir von den markierten die Summe. Sonst immer von
      // allen
      boolean selected = (o != null && (o instanceof Konto[]));

      if (selected)
        items = Arrays.asList((Konto[])o);
      else
        items = this.getItems();

      double sum = 0.0d;
      for (Konto k:items)
      {
        sum += k.getSaldo();
      }

      if (selected)
        return i18n.tr("{0} Konten markiert, Gesamt-Saldo: {1} {2}",new String[]{Integer.toString(items.size()),HBCI.DECIMALFORMAT.format(sum),HBCIProperties.CURRENCY_DEFAULT_DE});

      return i18n.tr("Gesamt-Saldo: {0} {1}",new String[]{HBCI.DECIMALFORMAT.format(sum),HBCIProperties.CURRENCY_DEFAULT_DE});
    }
    catch (Exception ex)
    {
      Logger.error("error while updating summary",ex);
    }

    return null;
  }

  /**
   * Hilfsklasse damit wir ueber neue Salden informiert werden.
   */
  public class SaldoMessageConsumer implements MessageConsumer
  {
    /**
     * ct.
     */
    public SaldoMessageConsumer()
    {
      super();
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{
        SaldoMessage.class,
        ObjectChangedMessage.class
      };
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      if (message == null)
        return;

      final GenericObject o = ((ObjectMessage)message).getObject();

      if (o == null || !(o instanceof Konto))
        return;

      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          try
          {
            int index = removeItem(o);
            if (index == -1)
              return; // Objekt war nicht in der Tabelle

            // Aktualisieren, in dem wir es neu an der gleichen Position eintragen
           addItem(o,index);

           // Wir markieren es noch in der Tabelle
           Object prev = getSelection();
           if (prev != null && prev == o)
             select(o);

           // Summen-Zeile aktualisieren
           featureEvent(Feature.Event.REFRESH,null);
          }
          catch (Exception e)
          {
            Logger.error("unable to add object to list",e);
          }
        }
      });
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }
  }

  /**
   * Listener zum Neuladen der Daten.
   */
  private class MyListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleEvent(Event event)
    {
      reload();
    }
  }
  
  /**
   * Listener fuer das verzoegerte Reload.
   */
  private class MyDelayedListener extends KeyAdapter
  {
    private Listener forward = new DelayedListener(700,listener);

    /**
     * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
     */
    public void keyReleased(KeyEvent e)
    {
      forward.handleEvent(null);
    }
  }
}
