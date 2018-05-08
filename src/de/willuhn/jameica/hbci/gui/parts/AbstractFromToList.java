/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.MultiInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.input.DateFromInput;
import de.willuhn.jameica.hbci.gui.input.DateToInput;
import de.willuhn.jameica.hbci.gui.input.KontoInput;
import de.willuhn.jameica.hbci.gui.input.RangeInput;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Abstrakte Tabelle mit Filter "von" und "bis".
 */
public abstract class AbstractFromToList extends TablePart implements Part
{
  protected final static I18N i18n         = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  protected final static Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  
  private KontoInput konto       = null;
  private Input from             = null;
  private Input to               = null;
  private RangeInput range       = null;
  private Input text             = null;
  private Container left         = null;

  protected Listener listener    = null;
  
  private ButtonArea buttons     = null;

  /**
   * ct.
   * @param action
   */
  public AbstractFromToList(Action action)
  {
    super(action);
    
    this.buttons = new ButtonArea();
    
    this.listener = new Listener() {
      public void handleEvent(Event event) {
        // Wenn das event "null" ist, kann es nicht
        // von SWT ausgeloest worden sein sondern
        // manuell von uns. In dem Fall machen wir ein
        // forciertes Update - ohne zu beruecksichtigen,
        // ob in den Eingabe-Feldern wirklich was geaendert
        // wurde
        handleReload(event == null);
      }
    };
    
    this.setRememberOrder(true);
    this.setRememberColWidths(true);
    this.setRememberState(true);
    this.setSummary(true);
    
    this.addSelectionListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        refreshSummary();
      }
    });
  }
  
  /**
   * Ueberschrieben, um die Summe zu berechnen.
   * @see de.willuhn.jameica.gui.parts.TablePart#getSummary()
   */
  @Override
  protected String getSummary()
  {
    try
    {
      Object o = this.getSelection();
      int size = this.size();

      // nichts markiert oder nur einer, dann muss nichts berechnet werden
      if (o == null || size == 1 || !(o instanceof Object[]))
      {
        return super.getSummary();
      }
      
      // Andernfalls berechnen wir die Summe
      Object[] list = (Object[]) o;
      BigDecimal sum = this.calculateSum(list);
      if (sum == null)
        return super.getSummary();
      
      return i18n.tr("{0} Aufträge, {1} markiert, Summe: {2} {3}",Integer.toString(size),Integer.toString(list.length),HBCI.DECIMALFORMAT.format(sum),HBCIProperties.CURRENCY_DEFAULT_DE);
    }
    catch (Exception e)
    {
      Logger.error("error while updating summary",e);
    }
    return super.getSummary();
  }
  
  /**
   * Liefert die Summe der angegebenen Auftraege.
   * @param selected die angegebenen Auftraege.
   * @return die Summe oder NULL, wenn nicht bekannt ist, wie die Summe berechnet werden kann.
   */
  protected BigDecimal calculateSum(Object[] selected) throws RemoteException
  {
    // Keine Ahnung, wie das zu berechnen ist
    if (!(selected instanceof Transfer[]))
      return null;
    
    BigDecimal sum = new BigDecimal(0);
    
    Transfer[] list = (Transfer[]) selected;
    for (Transfer u:list)
    {
      sum = sum.add(new BigDecimal(u.getBetrag()));
    }
    return sum;
  }
  
  /**
   * Liefert das Eingabe-Datum fuer das Start-Datum.
   * @return Eingabe-Feld.
   */
  private synchronized Input getFrom()
  {
    if (this.from != null)
      return this.from;
    
    this.from = new DateFromInput();
    this.from.setName(i18n.tr("Von"));
    this.from.setComment(null);
    this.from.addListener(this.listener);
    return this.from;
  }
  
  /**
   * Liefert ein Eingabefeld fuer einen Suchbegriff.
   * @return Eingabefeld fuer einen Suchbegriff.
   */
  public Input getText()
  {
    if (this.text != null)
      return this.text;

    this.text = new TextInput(settings.getString("transferlist.filter.text",null),255);
    this.text.setName(i18n.tr("Suchbegriff"));
    return this.text;
  }
  
  /**
   * Liefert ein Auswahlfeld fuer das Konto.
   * @return Auswahlfeld fuer das Konto.
   * @throws RemoteException
   */
  public KontoInput getKonto() throws RemoteException
  {
    if (this.konto != null)
      return this.konto;
    
    this.konto = new KontoInput(null,KontoFilter.ALL);
    this.konto.setPleaseChoose(i18n.tr("<Alle Konten>"));
    this.konto.setSupportGroups(true);
    this.konto.setComment(null);
    this.konto.setRememberSelection("auftraege");
    this.konto.addListener(this.listener);
    return this.konto;
  }
  
  /**
   * Liefert das Eingabe-Datum fuer das End-Datum.
   * @return Eingabe-Feld.
   */
  public synchronized Input getTo()
  {
    if (this.to != null)
      return this.to;

    this.to = new DateToInput();
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
    
    this.range = new RangeInput(this.getFrom(),this.getTo());
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
   * Ueberschrieben, um einen DisposeListener an das Composite zu haengen.
   * @see de.willuhn.jameica.gui.parts.TablePart#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    final TabFolder folder = new TabFolder(parent, SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    TabGroup tab = new TabGroup(folder,i18n.tr("Anzeige einschränken"));

    ColumnLayout cols = new ColumnLayout(tab.getComposite(),2);
    
    {
      this.left = new SimpleContainer(cols.getComposite());
      this.left.addInput(this.getKonto());
      
      Input t = this.getText();
      this.left.addInput(t);
      
      // Duerfen wir erst nach dem Zeichnen
      t.getControl().addKeyListener(new DelayedAdapter());
      
    }
    
    {
      Container right = new SimpleContainer(cols.getComposite());
      
      right.addInput(this.getRange());
      MultiInput range = new MultiInput(this.getFrom(),this.getTo());
      right.addInput(range);
    }

    this.buttons.addButton(i18n.tr("Aktualisieren"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        handleReload(true);
      }
    },null,true,"view-refresh.png");
    this.buttons.paint(parent);
   
    // Erstbefuellung
    GenericIterator items = getList(getKonto().getValue(),(Date)getFrom().getValue(),(Date)getTo().getValue(),(String)getText().getValue());
    if (items != null)
    {
      items.begin();
      while (items.hasNext())
        addItem(items.next());
    }

    super.paint(parent);
  }
  
  /**
   * Liefert den linken Container im Filter-Bereich.
   * @return der linke Container.
   */
  protected Container getLeft()
  {
    return this.left;
  }
  
  /**
   * Liefert die Button-Area der Komponente.
   * @return die Buttons.
   */
  public ButtonArea getButtons()
  {
    return this.buttons;
  }
  
  /**
   * Liefert die Liste der fuer diesen Zeitraum geltenden Daten.
   * @param konto das Konto oder die Gruppe. Kann null sein.
   * @param from Start-Datum. Kann null sein.
   * @param to End-Datum. Kann null sein.
   * @param text Suchbegriff
   * @return Liste der Daten dieses Zeitraumes.
   * @throws RemoteException
   */
  protected abstract DBIterator getList(Object konto, Date from, Date to, String text) throws RemoteException;
  
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
      final Object konto = getKonto().getValue();
      final Date dfrom   = (Date) getFrom().getValue();
      final Date dto     = (Date) getTo().getValue();
      final String text  = (String) getText().getValue();
      
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

      // Fehlertext "End-Datum muss ..." ggf. wieder entfernen
      GUI.startSync(new Runnable() //Sanduhr anzeigen
      {
        public void run()
        {
          try
          {
            // Ne, wir wurden nicht gekillt. Also machen wir uns ans Werk
            // erstmal alles entfernen.
            removeAll();

            // Liste neu laden
            GenericIterator items = getList(konto,dfrom,dto,text);
            if (items == null)
              return;
            
            items.begin();
            while (items.hasNext())
              addItem(items.next());
            
            // Sortierung wiederherstellen
            sort();
            
            // Speichern der Werte aus den Filter-Feldern.
            settings.setAttribute("transferlist.filter.text",text);
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
    try
    {
      return (konto != null && konto.hasChanged()) ||
             (from != null && from.hasChanged()) ||
             (to != null && to.hasChanged()) ||
             (text != null && text.hasChanged());
    }
    catch (Exception e)
    {
      Logger.error("unable to check change status",e);
      return true;
    }
  }
  
  /**
   * Da KeyAdapter/KeyListener nicht von swt.Listener abgeleitet
   * sind, muessen wir leider dieses schraege Konstrukt verenden,
   * um den DelayedListener verwenden zu koennen
   */
  private class DelayedAdapter extends KeyAdapter
  {
    private Listener forward = new DelayedListener(400,new Listener()
    {
      /**
       * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
       */
      public void handleEvent(Event event)
      {
        // hier kommt dann das verzoegerte Event an.
        handleReload(true);
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
