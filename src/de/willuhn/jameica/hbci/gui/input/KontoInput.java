/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/KontoInput.java,v $
 * $Revision: 1.10 $
 * $Date: 2011/08/30 12:09:40 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Autosuggest-Feld zur Eingabe/Auswahl eines Kontos.
 */
public class KontoInput extends SelectInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(KontoInput.class);
  private KontoListener listener = null;
  private String token = null;
  private Control control = null;
  
  /**
   * ct.
   * @param konto ausgewaehltes Konto.
   * @param filter optionaler Konto-Filter.
   * @throws RemoteException
   */
  public KontoInput(Konto konto, KontoFilter filter) throws RemoteException
  {
    super(init(filter),konto);
    setName(i18n.tr("Konto"));
    setPleaseChoose(i18n.tr("Bitte wählen..."));
    this.setComment("");
    
    if (konto == null)
    {
      konto = Settings.getDefaultKonto();
      if (konto != null)
        setPreselected(konto);
    }
    this.listener = new KontoListener();
    this.addListener(this.listener);

    // einmal ausloesen
    this.listener.handleEvent(null);
  }
  
  /**
   * Die Kontoauswahl kann sich das zuletzt ausgewaehlte Konto merken.
   * Damit dann aber nicht auf allen Dialogen das gleiche Konto vorausgewaehlt ist,
   * kann man hier einen individuellen Freitext-Token uebergeben, der als Key fuer
   * das Speichern des zuletzt ausgewaehlten Kontos verwendet wird. Ueberall dort,
   * wo also der gleiche Token verwendet wird, wird auch das gleiche Konto
   * vorausgewaehlt. Der Konto kann z.Bsp. "auswertungen" heissen. Wenn dieser
   * auf allen Dialogen der Auswertungen verwendet wird, wird dort dann auch ueberall
   * das gleiche Konto vorausgewaehlt sein.
   * @param s der Restore-Token.
   */
  public void setRememberSelection(String s)
  {
    if (s == null || s.length() == 0)
      return;
    
    this.token = s;

    String id = settings.getString(this.token,null);
    if (id != null && id.length() > 0)
    {
      try
      {
        Konto k = (Konto) Settings.getDBService().createObject(Konto.class,id);
        this.setPreselected(k);
      }
      catch (Exception e)
      {
        // Konto konnte nicht geladen werden. Vorauswahl loeschen
        settings.setAttribute(this.token,(String) null);
      }
    }
    
    // Listener hinzufuegen
    this.addListener(new Listener() {
      public void handleEvent(Event event)
      {
        storeSelection();
      }
    });
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.SelectInput#getControl()
   */
  public Control getControl()
  {
    if (this.control != null)
      return this.control;
    
    this.control = super.getControl();
    if (this.token != null)
    {
      this.control.addDisposeListener(new DisposeListener() {
        public void widgetDisposed(DisposeEvent e)
        {
          storeSelection();
        }
      });
    }
    return this.control;
  }
  
  /**
   * Speichert die aktuelle Auswahl.
   */
  private void storeSelection()
  {
    try
    {
      Konto k = (Konto) getValue();
      settings.setAttribute(token,(String) (k != null ? k.getID() : null));
    }
    catch (Exception e)
    {
      // Hier lief was schief. Wir loeschen die Vorauswahl
      settings.setAttribute(token,(String) null);
    }
  }

  /**
   * Initialisiert die Liste der Konten.
   * @param filter Konto-Filter.
   * @return Liste der Konten.
   * @throws RemoteException
   */
  private static List<Konto> init(KontoFilter filter) throws RemoteException
  {
    DBIterator it = Settings.getDBService().createList(Konto.class);
    it.setOrder("ORDER BY blz, kontonummer");
    List<Konto> l = new ArrayList<Konto>();
    while (it.hasNext())
    {
      Konto k = (Konto) it.next();
      if (filter == null || filter.accept(k))
        l.add(k);
    }
    return l;
  }

  /**
   * @see de.willuhn.jameica.gui.input.SelectInput#format(java.lang.Object)
   */
  protected String format(Object bean)
  {
    if (bean == null)
      return null;
    
    if (!(bean instanceof Konto))
      return bean.toString();
    
    try
    {
      Konto k = (Konto) bean;
      
      Konto kd = Settings.getDefaultKonto();
      boolean isDefault = (kd != null && kd.equals(k));


      boolean disabled = (k.getFlags() & Konto.FLAG_DISABLED) == Konto.FLAG_DISABLED;

      StringBuffer sb = new StringBuffer();
      if (isDefault)
        sb.append("> ");
      if (disabled)
        sb.append("[");
      
      sb.append(i18n.tr("Kto. {0}",k.getKontonummer()));
      
      String blz = k.getBLZ();
      sb.append(" [");
      String bankName = HBCIUtils.getNameForBLZ(blz);
      if (bankName != null && bankName.length() > 0)
      {
        sb.append(bankName);
      }
      else
      {
        sb.append("BLZ ");
        sb.append(blz);
      }
      sb.append("] ");
      sb.append(k.getName());

      String bez = k.getBezeichnung();
      if (bez != null && bez.length() > 0)
      {
        sb.append(" - ");
        sb.append(bez);
      }
      
      if (k.getSaldoDatum() != null)
      {
        sb.append(", ");
        sb.append(i18n.tr("Saldo: {0} {1}", new String[]{HBCI.DECIMALFORMAT.format(k.getSaldo()),k.getWaehrung()}));
      }
      
      if (disabled)
        sb.append("]");
      return sb.toString();
    }
    catch (RemoteException re)
    {
      Logger.error("unable to format address",re);
      return null;
    }
  }

  /**
   * Listener, der die Auswahl des Kontos ueberwacht und den Kommentar anpasst.
   */
  private class KontoListener implements Listener
  {
    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event) {

      try {
        Object o = getValue();
        if (o == null || !(o instanceof Konto))
        {
          setComment("");
          return;
        }

        Konto konto = (Konto) o;
        String w = konto.getWaehrung();

        Date datum = konto.getSaldoDatum();
        if (datum != null)
          setComment(i18n.tr("Saldo: {0} {1} vom {2}", new String[]{HBCI.DECIMALFORMAT.format(konto.getSaldo()),w,HBCI.DATEFORMAT.format(datum)}));
        else
          setComment("");
      }
      catch (RemoteException er)
      {
        Logger.error("error while updating currency",er);
        GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei Ermittlung der Währung"));
      }
    }
  }

}


/**********************************************************************
 * $Log: KontoInput.java,v $
 * Revision 1.10  2011/08/30 12:09:40  willuhn
 * @N BUGZILLA 1125
 *
 * Revision 1.9  2011-05-20 16:22:31  willuhn
 * @N Termin-Eingabefeld in eigene Klasse ausgelagert (verhindert duplizierten Code) - bessere Kommentare
 *
 * Revision 1.8  2011-05-19 08:41:53  willuhn
 * @N BUGZILLA 1038 - generische Loesung
 *
 * Revision 1.7  2010-08-12 17:12:32  willuhn
 * @N Saldo-Chart komplett ueberarbeitet (Daten wurden vorher mehrmals geladen, Summen-Funktion, Anzeige mehrerer Konten, Durchschnitt ueber mehrere Konten, Bugfixing, echte "Homogenisierung" der Salden via SaldoFinder)
 *
 * Revision 1.6  2009-10-20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.5  2009/10/07 23:08:56  willuhn
 * @N BUGZILLA 745: Deaktivierte Konten in Auswertungen zwar noch anzeigen, jedoch mit "[]" umschlossen. Bei der Erstellung von neuen Auftraegen bleiben sie jedoch ausgeblendet. Bei der Gelegenheit wird das Default-Konto jetzt mit ">" markiert
 *
 * Revision 1.4  2009/09/15 00:23:35  willuhn
 * @N BUGZILLA 745
 *
 * Revision 1.3  2009/01/12 00:46:50  willuhn
 * @N Vereinheitlichtes KontoInput in den Auswertungen
 *
 * Revision 1.2  2009/01/04 16:38:55  willuhn
 * @N BUGZILLA 523 - ein Konto kann jetzt als Default markiert werden. Das wird bei Auftraegen vorausgewaehlt und ist fett markiert
 *
 * Revision 1.1  2009/01/04 16:18:22  willuhn
 * @N BUGZILLA 404 - Kontoauswahl via SelectBox
 *
 **********************************************************************/
