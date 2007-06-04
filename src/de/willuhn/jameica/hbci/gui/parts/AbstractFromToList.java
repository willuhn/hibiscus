/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/AbstractFromToList.java,v $
 * $Revision: 1.8 $
 * $Date: 2007/06/04 23:23:47 $
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
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Abstrakte Tabelle mit Filter "von" und "bis".
 */
public abstract class AbstractFromToList extends TablePart implements Part
{

  protected I18N i18n            = null;
  
  private Input from             = null;
  private Input to               = null;

  protected Listener listener    = null;
  protected de.willuhn.jameica.system.Settings mySettings = null;

  /**
   * ct.
   * @param action
   */
  public AbstractFromToList(Action action)
  {
    super(action);
    
    this.i18n       = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.mySettings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();

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
    // Als Startdatum nehmen wir den ersten des aktuellen Monats
    // Es sei denn, es ist eines gespeichert
    Date dFrom = null;
    String sFrom = mySettings.getString("transferlist.filter.from",null);
    if (sFrom != null && sFrom.length() > 0)
    {
      try
      {
        dFrom = HBCI.DATEFORMAT.parse(sFrom);
      }
      catch (Exception e)
      {
        Logger.error("unable to parse " + sFrom,e);
      }
    }
    if (dFrom == null)
    {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.DAY_OF_MONTH,1);
      dFrom = HBCIProperties.startOfDay(cal.getTime());
    }
    
    this.from = new DateInput(dFrom, HBCI.DATEFORMAT);
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

    // Als End-Datum nehmen wir keines.
    // Es sei denn, es ist ein aktuelles gespeichert
    String sTo = mySettings.getString("transferlist.filter.to",null);
    Date dTo = null;
    if (sTo != null && sTo.length() > 0)
    {
      try
      {
        dTo = HBCI.DATEFORMAT.parse(sTo);
      }
      catch (Exception e)
      {
        Logger.error("unable to parse " + sTo,e);
      }
    }

    
    this.to = new DateInput(dTo, HBCI.DATEFORMAT);
    this.to.addListener(this.listener);
    return this.to;
  }

  /**
   * Ueberschrieben, um einen DisposeListener an das Composite zu haengen.
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Anzeige einschränken"));

    group.addLabelPair(i18n.tr("Anzeige von"),getFrom());
    group.addLabelPair(i18n.tr("Anzeige bis"),getTo());
   
    // Erstbefuellung
    GenericIterator items = getList((Date)getFrom().getValue(),(Date)getTo().getValue());
    if (items != null)
    {
      items.begin();
      while (items.hasNext())
        addItem(items.next());
    }

    super.paint(parent);
  }
  
  /**
   * Liefert die Liste der fuer diesen Zeitraum geltenden Daten.
   * @param from Start-Datum. Kann null sein.
   * @param to End-Datum. Kann null sein.
   * @return Liste der Daten dieses Zeitraumes.
   * @throws RemoteException
   */
  protected abstract GenericIterator getList(Date from, Date to) throws RemoteException;
  
  /**
   * Aktualisiert die Tabelle der angezeigten Daten.
   * Die Aktualisierung geschieht um einige Millisekunden verzoegert,
   * damit ggf. schnell aufeinander folgende Events gebuendelt werden.
   * @param force true, wenn die Daten auch dann aktualisiert werden sollen,
   * wenn an den Eingabe-Feldern nichts geaendert wurde.
   */
  private synchronized void handleReload(boolean force)
  {
    final Date dfrom = (Date) getFrom().getValue();
    final Date dto   = (Date) getTo().getValue();
    
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
          GenericIterator items = getList(dfrom,dto);
          if (items == null)
            return;
          
          items.begin();
          while (items.hasNext())
            addItem(items.next());
          
          // Sortierung wiederherstellen
          sort();
          
          // Speichern der Werte aus den beiden Eingabe-Feldern.
          // Das From-Datum speichern wir immer
          mySettings.setAttribute("transferlist.filter.from",dfrom == null ? (String)null : HBCI.DATEFORMAT.format(dfrom));
            
          // Das End-Datum speichern wir nur, wenn es nicht das aktuelle Datum ist
          if (dto != null && !HBCIProperties.startOfDay(new Date()).equals(HBCIProperties.startOfDay(dto)))
            mySettings.setAttribute("transferlist.filter.to",HBCI.DATEFORMAT.format(dto));
          else
            mySettings.setAttribute("transferlist.filter.to",(String)null);
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
             (to != null && to.hasChanged());
    }
    catch (Exception e)
    {
      Logger.error("unable to check change status",e);
      return true;
    }
  }
}


/**********************************************************************
 * $Log: AbstractFromToList.java,v $
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