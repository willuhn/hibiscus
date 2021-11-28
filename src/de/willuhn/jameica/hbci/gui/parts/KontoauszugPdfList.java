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
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.MultiInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TableChangeListener;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureShortcut;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.KontoFetchKontoauszug;
import de.willuhn.jameica.hbci.gui.action.KontoauszugOpen;
import de.willuhn.jameica.hbci.gui.action.KontoauszugPdfSettings;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.DateFromInput;
import de.willuhn.jameica.hbci.gui.input.DateToInput;
import de.willuhn.jameica.hbci.gui.input.InputCompat;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.RangeInput;
import de.willuhn.jameica.hbci.gui.parts.columns.KontoColumn;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.hbci.server.KontoauszugPdfUtil;
import de.willuhn.jameica.hbci.server.Range;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Fertig konfigurierte Liste zur Anzeige der Liste mit den PDF-Kontoauszuegen.
 */
public class KontoauszugPdfList extends TablePart
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private final MessageConsumer mc;

  private KontoInput kontoAuswahl       = null;
  private Input from                    = null;
  private Input to                      = null;
  private RangeInput range              = null;
  private CheckboxInput unread          = null;
  private CheckboxInput inclusiveFilter = null;

  private final Listener listener;

  /**
   * ct.
   */
  public KontoauszugPdfList()
  {
    super(new KontoauszugOpen());

    this.addFeature(new FeatureShortcut());

    this.listener = new Listener() {
      public void handleEvent(Event event) {
        // Wenn das event "null" ist, kann es nicht von SWT ausgeloest worden sein
        // sondern manuell von uns. In dem Fall machen wir ein forciertes Update
        // - ohne zu beruecksichtigen, ob in den Eingabe-Feldern wirklich was
        // geaendert wurde
        handleReload(event == null);
      }
    };

    this.setFormatter(new TableFormatter() {
      
      @Override
      public void format(TableItem item)
      {
        try
        {
          Kontoauszug k = (Kontoauszug) item.getData();
          
          if (k.getGelesenAm() != null)
          {
            item.setFont(Font.DEFAULT.getSWTFont());
            item.setImage(0,SWTUtil.getImage("emblem-default.png"));
          }
          else
          {
            item.setFont(Font.BOLD.getSWTFont());
            item.setImage(0,null); // Image wieder entfernen. Noetig, weil wir auch bei Updates aufgerufen werden
          }
        }
        catch (RemoteException re)
        {
          Logger.error("error while checking read-state of account statements",re);
        }
      }
    });
    
    final DateFormatter df = new DateFormatter();
    this.addColumn(new KontoColumn());
    this.addColumn(i18n.tr("Jahr"),"jahr");
    this.addColumn(i18n.tr("Nummer"),"nummer");
    this.addColumn(i18n.tr("Von"),"von",df);
    this.addColumn(i18n.tr("Bis"),"bis",df);
    this.addColumn(i18n.tr("Erstellt am"),"erstellungsdatum",df);
    this.addColumn(i18n.tr("Abgerufen am"),"ausgefuehrt_am",df);
    this.addColumn(i18n.tr("Quittiert am"),"quittiert_am",df);
    this.addColumn(i18n.tr("Notiz"),"kommentar",null,true);

    this.setRememberOrder(true);
    this.setRememberColWidths(true);
    this.setRememberState(true);
    this.setMulti(true);

    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.KontoauszugPdfList());
    
    this.addChangeListener(new TableChangeListener() {
      public void itemChanged(Object object, String attribute, String newValue) throws ApplicationException
      {
        try
        {
          Kontoauszug u = (Kontoauszug) object;
          BeanUtil.set(u,attribute,newValue);
          u.store();
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (Exception e)
        {
          Logger.error("unable to apply changes",e);
          throw new ApplicationException(i18n.tr("Fehlgeschlagen: {0}",e.getMessage()));
        }
      }
    });

    // Wir erstellen noch einen Message-Consumer, damit wir ueber neu eintreffende Kontoauszuege
    // informiert werden.
    this.mc = new TransferMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);

  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    final TabFolder folder = new TabFolder(parent, SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    TabGroup tab = new TabGroup(folder,i18n.tr("Anzeige einschränken"));

    ColumnLayout cols = new ColumnLayout(tab.getComposite(),2);
    
    {
      Container left = new SimpleContainer(cols.getComposite());
      left.addInput(this.getKontoAuswahl());
      left.addInput(this.getInclusiveFilter());
      left.addInput(this.getUnread());
    }
    
    {
      Container right = new SimpleContainer(cols.getComposite());
      right.addInput(this.getRange());
      MultiInput range = new MultiInput(this.getFrom(),this.getTo());
      right.addInput(range);
    }
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Kontoauszüge abrufen..."), new KontoAction(new KontoFetchKontoauszug()),null,false,"mail-send-receive.png");
    buttons.addButton(i18n.tr("Einstellungen"),new KontoAction(new KontoauszugPdfSettings()),null,false,"document-properties.png");
    buttons.addButton(i18n.tr("Aktualisieren"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        handleReload(true);
      }
    },null,true,"view-refresh.png");
    buttons.paint(parent);
   
    this.handleReload(true);
    
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });
    super.paint(parent);
  }
  
  /**
   * Liefert eine Auswahlbox fuer das Konto.
   * @return Auswahlbox.
   * @throws RemoteException
   */
  private KontoInput getKontoAuswahl() throws RemoteException
  {
    if (this.kontoAuswahl != null)
      return this.kontoAuswahl;

    this.kontoAuswahl = new KontoInput(null,KontoFilter.ONLINE);
    this.kontoAuswahl.setSupportGroups(true);
    this.kontoAuswahl.setRememberSelection("kontoauszuege.filter.konto");
    this.kontoAuswahl.setComment(null);
    this.kontoAuswahl.setPleaseChoose(i18n.tr("<Alle Konten>"));
    this.kontoAuswahl.addListener(this.listener);
    return this.kontoAuswahl;
  }
  
  /**
   * Liefert das Eingabe-Datum fuer das Start-Datum.
   * @return Eingabe-Feld.
   */
  private synchronized Input getFrom()
  {
    if (this.from != null)
      return this.from;
    
    this.from = new DateFromInput(null,"kontoauszuege.filter.from");
    this.from.setName(i18n.tr("Von"));
    this.from.setComment(null);
    this.from.addListener(this.listener);
    return this.from;
  }
  
  /**
   * Liefert das Eingabe-Datum fuer das End-Datum.
   * @return Eingabe-Feld.
   */
  public synchronized Input getTo()
  {
    if (this.to != null)
      return this.to;

    this.to = new DateToInput(null,"kontoauszuege.filter.to");
    this.to.setName(i18n.tr("bis"));
    this.to.setComment(null);
    this.to.addListener(this.listener);
    return this.to;
  }
  
  /**
   * Liefert eine Auswahl mit Zeit-Presets.
   * @return eine Auswahl mit Zeit-Presets.
   */
  public RangeInput getRange()
  {
    if (this.range != null)
      return this.range;
    
    this.range = new RangeInput(this.getFrom(),this.getTo(),Range.CATEGORY_AUSWERTUNG,"kontoauszuege.filter.range");
    this.range.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        if (range.getValue() != null && range.hasChanged())
          handleReload(true);
      }
    });
    
    return this.range;
  }
  
  /**
   * Liefert eine Checkbox, um nur die ungelesenene Kontoauszuege anzuzeigen.
   * @return Checkbox.
   */
  public CheckboxInput getUnread()
  {
    if (this.unread != null)
      return this.unread;
    
    this.unread = new CheckboxInput(settings.getBoolean("kontoauszuege.filter.unread",false));
    this.unread.setName(i18n.tr("Nur ungelesene Kontoauszüge anzeigen"));
    this.unread.addListener(this.listener);
    return this.unread;
  }
  
  /**
   * Liefert eine Checkbox, um auch nur teilweise im Zeitraum liegende Kontoauszuege anzuzeigen.
   * @return Checkbox.
   */
  public CheckboxInput getInclusiveFilter()
  {
    if (this.inclusiveFilter != null)
      return this.inclusiveFilter;
    
    this.inclusiveFilter = new CheckboxInput(settings.getBoolean("kontoauszuege.filter.inclusivefilter",false));
    this.inclusiveFilter.setName(i18n.tr("Auch nur teilweise im Zeitraum liegende Kontoauszüge anzeigen"));
    this.inclusiveFilter.addListener(this.listener);
    this.inclusiveFilter.addListener(new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        settings.setAttribute("kontoauszuege.filter.inclusivefilter", (Boolean) getInclusiveFilter().getValue());
      }
    });
    return this.inclusiveFilter;
  }

  /**
   * Aktualisiert die Tabelle der angezeigten Daten.
   * Die Aktualisierung geschieht um einige Millisekunden verzoegert,
   * damit ggf. schnell aufeinander folgende Events gebuendelt werden.
   * @param force true, wenn die Daten auch dann aktualisiert werden sollen,
   * wenn an den Eingabe-Feldern nichts geaendert wurde.
   */
  private synchronized void handleReload(boolean force)
  {
    try
    {
      final Object konto   = getKontoAuswahl().getValue();
      final Date dfrom     = (Date) getFrom().getValue();
      final Date dto       = (Date) getTo().getValue();
      final Boolean unread = (Boolean) getUnread().getValue();
      final Boolean inclusiveFilter = (Boolean) getInclusiveFilter().getValue();
      
      if (!force)
      {
        // Wenn es kein forcierter Reload ist, pruefen wir,
        // ob sich etwas geaendert hat oder Eingabe-Fehler
        // vorliegen
        if (!hasChanged())
          return;

        if (dfrom != null && dto != null && dfrom.after(dto))
        {
          GUI.getView().setErrorText(i18n.tr("End-Datum muss sich nach dem Start-Datum befinden"));
          return;
        }
      }

      GUI.startSync(new Runnable() //Sanduhr anzeigen
      {
        public void run()
        {
          try
          {
            removeAll();

            // Liste neu laden
            GenericIterator items = KontoauszugPdfUtil.getList(konto,dfrom,dto,unread != null ? unread.booleanValue() : false, inclusiveFilter != null ? inclusiveFilter.booleanValue() : false);
            if (items == null)
              return;
            
            items.begin();
            while (items.hasNext())
              addItem(items.next());
            
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
    catch (Exception e)
    {
      Logger.error("error while reloading data",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren der Tabelle"), StatusBarMessage.TYPE_ERROR));
    }
  }
  
  /**
   * Prueft, ob seit der letzten Aktion Eingaben geaendert wurden.
   * Ist das nicht der Fall, muss die Tabelle nicht neu geladen werden.
   * @return true, wenn sich wirklich was geaendert hat.
   */
  protected boolean hasChanged()
  {
    return InputCompat.valueHasChanged(kontoAuswahl, from, to, unread, inclusiveFilter);
  }
  
  /**
   * Hilfsklasse damit wir ueber importierte Transfers informiert werden.
   */
  public class TransferMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{
        ImportMessage.class,
        ObjectChangedMessage.class
      };
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(final Message message) throws Exception
    {
      if (message == null)
        return;
      
      final GenericObject o = ((ObjectMessage)message).getObject();
      
      if (o == null)
        return;
      
      // Checken, ob uns der Transfer-Typ interessiert
      if (!(o instanceof Kontoauszug))
        return;

      GUI.getDisplay().asyncExec(new Runnable() {
        public void run()
        {
          try
          {
            if (message instanceof ObjectChangedMessage)
            {
              updateItem(o,o);
            }
            else if (message instanceof ImportMessage)
            {
              addItem(o);
              sort();
              // Filter anwenden
              handleReload(true);
            }
            
          }
          catch (Exception e)
          {
            Logger.error("unable to update item",e);
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
   * Oeffnet die Einstellungen.
   * @throws ApplicationException
   */
  public void handleSettings() throws ApplicationException
  {
    new KontoAction(new KontoauszugPdfSettings()).handleAction(null);
  }

  /**
   * Uebernimmt automatisch das ausgewaehlte Konto in die Action.
   */
  private class KontoAction implements Action
  {
    private final Action redirect;
    
    /**
     * ct.
     * @param redirect
     */
    private KontoAction(Action redirect)
    {
      this.redirect = redirect;
    }
    
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    @Override
    public void handleAction(Object context) throws ApplicationException
    {
      // Wenn aktuell ein Konto ausgewaehlt ist, dann uebergeben wir dieses,
      // damit es in den Einstellungen vorausgewaehlt ist
      Konto k = null;
      try
      {
        Object o = getKontoAuswahl().getValue();
        if (o != null && (o instanceof Konto))
          k = (Konto) o;
      }
      catch (Exception e)
      {
        Logger.error("unable to determine account",e);
      }
      
      this.redirect.handleAction(k);
    }
  }

}


