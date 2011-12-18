/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/AbstractFromToList.java,v $
 * $Revision: 1.13 $
 * $Date: 2011/12/18 23:20:20 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

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
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.input.DateFromInput;
import de.willuhn.jameica.hbci.gui.input.DateToInput;
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
  protected final static I18N i18n       = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
  
  private Input from             = null;
  private Input to               = null;
  private Input text             = null;

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
    this.from.setName(i18n.tr("Anzeige von"));
    this.from.setComment(null);
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
   * Liefert das Eingabe-Datum fuer das End-Datum.
   * @return Eingabe-Feld.
   */
  public synchronized Input getTo()
  {
    if (this.to != null)
      return this.to;

    this.to = new DateToInput();
    this.to.setName(i18n.tr("Anzeige bis"));
    this.to.setComment(null);
    return this.to;
  }

  /**
   * Ueberschrieben, um einen DisposeListener an das Composite zu haengen.
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    final TabFolder folder = new TabFolder(parent, SWT.NONE);
    folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    TabGroup tab = new TabGroup(folder,i18n.tr("Anzeige einschränken"));

    ColumnLayout cols = new ColumnLayout(tab.getComposite(),4);
    
    {
      Container c = new SimpleContainer(cols.getComposite());
      
      Input t = this.getText();
      c.addInput(t);
      
      // Duerfen wir erst nach dem Zeichnen
      t.getControl().addKeyListener(new DelayedAdapter());
    }
    
    {
      Container c = new SimpleContainer(cols.getComposite());
      c.addInput(this.getFrom());
    }
    
    {
      Container c = new SimpleContainer(cols.getComposite());
      c.addInput(this.getTo());
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
    GenericIterator items = getList((Date)getFrom().getValue(),(Date)getTo().getValue(),(String)getText().getValue());
    if (items != null)
    {
      items.begin();
      while (items.hasNext())
        addItem(items.next());
    }

    super.paint(parent);
  }
  
  /**
   * Liefert die Button-Area der Komponente.
   * @return this Buttons.
   */
  public ButtonArea getButtons()
  {
    return this.buttons;
  }
  
  /**
   * Liefert die Liste der fuer diesen Zeitraum geltenden Daten.
   * @param from Start-Datum. Kann null sein.
   * @param to End-Datum. Kann null sein.
   * @param text Suchbegriff
   * @return Liste der Daten dieses Zeitraumes.
   * @throws RemoteException
   */
  protected abstract DBIterator getList(Date from, Date to, String text) throws RemoteException;
  
  /**
   * Aktualisiert die Tabelle der angezeigten Daten.
   * Die Aktualisierung geschieht um einige Millisekunden verzoegert,
   * damit ggf. schnell aufeinander folgende Events gebuendelt werden.
   * @param force true, wenn die Daten auch dann aktualisiert werden sollen,
   * wenn an den Eingabe-Feldern nichts geaendert wurde.
   */
  private synchronized void handleReload(boolean force)
  {
    final Date dfrom  = (Date) getFrom().getValue();
    final Date dto    = (Date) getTo().getValue();
    final String text = (String) getText().getValue();
    
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
    GUI.getView().setErrorText("");

    GUI.getView().setLogoText(i18n.tr("Aktualisiere Daten..."));
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
          GenericIterator items = getList(dfrom,dto,text);
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
        finally
        {
          GUI.getView().setLogoText("");
        }
      }
    });
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
      return (from != null && from.hasChanged()) ||
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


/**********************************************************************
 * $Log: AbstractFromToList.java,v $
 * Revision 1.13  2011/12/18 23:20:20  willuhn
 * @N GUI-Politur
 *
 * Revision 1.12  2011-08-05 11:21:58  willuhn
 * @N Erster Code fuer eine Umsatz-Preview
 * @C Compiler-Warnings
 * @N DateFromInput/DateToInput - damit sind die Felder fuer den Zeitraum jetzt ueberall einheitlich
 *
 * Revision 1.11  2011-06-28 09:24:35  willuhn
 * @N Position speichern
 *
 * Revision 1.10  2011-01-20 17:13:21  willuhn
 * @C HBCIProperties#startOfDay und HBCIProperties#endOfDay nach Jameica in DateUtil verschoben
 *
 * Revision 1.9  2010-08-16 11:13:52  willuhn
 * @N In den Auftragslisten kann jetzt auch nach einem Text gesucht werden
 *
 * Revision 1.8  2007/06/04 23:23:47  willuhn
 * @B error while saving transfer list date
 *
 * Revision 1.7  2007/06/04 22:18:29  willuhn
 * @B typo
 *
 * Revision 1.6  2007/04/26 23:08:13  willuhn
 * @C Umstellung auf DelayedListener
 *
 * Revision 1.5  2007/04/26 18:27:38  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2007/04/26 15:02:36  willuhn
 * @N Optisches Feedback beim Neuladen der Daten
 *
 * Revision 1.3  2007/04/26 13:59:31  willuhn
 * @N Besseres Reload-Verhalten in Transfer-Listen
 *
 * Revision 1.2  2007/04/24 17:15:51  willuhn
 * @B Vergessen, "size" hochzuzaehlen, wenn Objekte vor paint() hinzugefuegt werden
 *
 * Revision 1.1  2007/04/24 16:55:00  willuhn
 * @N Aktualisierte Daten nur bei geaendertem Datum laden
 *
 **********************************************************************/