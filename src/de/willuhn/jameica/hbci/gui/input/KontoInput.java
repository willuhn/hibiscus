/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.messaging.SaldoMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
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

  private Konto konto = null;
  private static List<String> groups = null;

  private KontoListener listener = null;
  private String token = null;
  private boolean store = true;
  private Control control = null;
  private boolean supportGroups = false;

  private MessageConsumer mc = new SaldoMessageConsumer();

  /**
   * ct.
   * @param konto ausgewaehltes Konto.
   * @param filter optionaler Konto-Filter.
   * @throws RemoteException
   */
  public KontoInput(Konto konto, KontoFilter filter) throws RemoteException
  {
    super(init(filter),konto);
    this.konto = konto;
    setName(i18n.tr("Konto"));

    // Wenn nur ein Konto hinterlegt ist das gleich selektieren
    // Und nur dann, wenn wir keine Gruppen haben - die landen naemlich auch in this.list
    if (groups == null || groups.size() == 0)
    {
      List konten = this.getList();
      if (konten != null && konten.size() == 1)
        this.setPreselected(konten.get(0));
    }
    
    setPleaseChoose(i18n.tr("Bitte wählen..."));
    this.setComment("");

    this.listener = new KontoListener();
    this.addListener(this.listener);

    // einmal ausloesen
    this.listener.handleEvent(null);
  }

  /**
   * Legt fest, ob die Kontoauswahl das Zurueckliefern von Gruppen unterstuetzen soll.
   * @param b true, wenn es unterstuetzt werden soll.
   * In dem Fall liefert das Input einen String mit der ausgewaehlten Kategorie.
   * Andernfalls wird in diesem Fall NULL zurueckgeliefert. Per Default ist dieses
   * Feature (aus Gruenden der Abwaertskompatibilitaet) deaktiviert - muss also explizit
   * an den Stellen aktiviert werden, wo es verwendet wird.
   */
  public void setSupportGroups(boolean b)
  {
    this.supportGroups = b;
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
   * @param store wenn die hier getroffene Auswahl auch gespeichert werden soll.
   */
  public void setRememberSelection(String s, boolean store)
  {
    if (s == null || s.length() == 0)
      return;

    this.token = s;
    this.store = store;

    String id = settings.getString(this.token,null);
    if (this.konto == null && id != null && id.length() > 0) // BUGZILLA 1446 - nur uebernehmen, wenn wir noch keins vorausgewaehlt haben
    {
      try
      {
        // Checken, ob wir das Konto haben
        for (Konto k:KontoUtil.getKonten(null))
        {
          if (id.equals(k.getID()))
          {
            this.setPreselected(k);
            return;
          }
        }
        
        // OK, wir haben die ID nicht gefunden. Eventuell ist es eine Gruppe
        if (this.supportGroups)
        {
          for (String group:KontoUtil.getGroups())
          {
            if (id.equals(group))
            {
              this.setPreselected(group);
              return;
            }
          }
        }
        else
        {
          settings.setAttribute(this.token,(String) null); // Vorauswahl loeschen
        }
      }
      catch (Exception e)
      {
        Logger.error("unable to load accounts",e);
      }
    }
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
    this.setRememberSelection(s,true);
  }

  /**
   * @see de.willuhn.jameica.gui.input.SelectInput#getControl()
   */
  public Control getControl()
  {
    if (this.control != null)
      return this.control;

    this.control = super.getControl();

    Application.getMessagingFactory().registerMessageConsumer(this.mc);
    this.control.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
        storeSelection();
      }
    });
    return this.control;
  }

  /**
   * Speichert die aktuelle Auswahl.
   */
  private void storeSelection()
  {
    if (!this.store || this.token == null)
      return;

    try
    {
      Object o = getValue();
      String value = null;

      if (o != null)
      {
        if (o instanceof Konto)
          value = ((Konto)o).getID();
        else
          value = o.toString();
      }
      settings.setAttribute(token,value);
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
  private static List init(KontoFilter filter) throws RemoteException
  {
    groups = KontoUtil.getGroups(); // Gruppen neu laden
    boolean haveGroups = groups.size() > 0;

    DBIterator it = Settings.getDBService().createList(Konto.class);
    it.setOrder("ORDER BY LOWER(kategorie), blz, bezeichnung, kontonummer");
    List l = new ArrayList();

    String current = null;

    while (it.hasNext())
    {
      Konto k = (Konto) it.next();

      if (filter == null || filter.accept(k))
      {
        if (haveGroups)
        {
          String kat = StringUtils.trimToNull(k.getKategorie());
          if (kat != null) // haben wir eine Kategorie?
          {
            if (current == null || !kat.equals(current)) // Neue Kategorie?
            {
              l.add(kat);
              current = kat;
            }
          }
        }
        l.add(k);
      }
    }
    return l;
  }

  /**
   * @see de.willuhn.jameica.gui.input.SelectInput#getValue()
   */
  public Object getValue()
  {
    Object o = super.getValue();

    if ((o instanceof String) && !this.supportGroups) // Kategorie
    {
      GUI.getView().setErrorText(i18n.tr("Die Auswahl einer Konto-Gruppen ist hier nicht möglich"));
      return null;
    }
    return o;
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

      boolean disabled = k.hasFlag(Konto.FLAG_DISABLED);

      StringBuffer sb = new StringBuffer();
      if (groups.size() > 0)
        sb.append("   "); // Wir haben Gruppen - also einruecken
      if (disabled)
        sb.append("[");

      String blz = k.getBLZ();
      String bankName = HBCIProperties.getNameForBank(blz);
      if (bankName != null && bankName.length() > 0)
      {
        sb.append(bankName);
      }
      else
      {
        sb.append("BLZ ");
        sb.append(blz);
      }
      sb.append(": " + k.getName());

      String bez = k.getBezeichnung();
      if (bez != null && bez.length() > 0)
      {
        sb.append(" - ");
        sb.append(bez);
      }

      sb.append(" [" + i18n.tr("Kto. {0}",k.getKontonummer()) + "]");


      if (k.getSaldoDatum() != null)
      {
        sb.append(" - ");
        sb.append(i18n.tr("Saldo: {0} {1}", HBCI.DECIMALFORMAT.format(k.getSaldo()), k.getWaehrung()));
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
          setComment(i18n.tr("Saldo: {0} {1} vom {2}", HBCI.DECIMALFORMAT.format(konto.getSaldo()), w, HBCI.DATEFORMAT.format(datum)));
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

  /**
   * Wird ueber Saldo-Aenderungen benachrichtigt.
   */
  private class SaldoMessageConsumer implements MessageConsumer
  {
    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{SaldoMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      SaldoMessage msg = (SaldoMessage) message;
      GenericObject o = msg.getObject();
      if (!(o instanceof Konto))
        return;

      final Konto konto = (Konto) o;

      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          // Checken, ob wir das Konto in der Liste haben. Wenn ja, aktualisieren
          // wir dessen Saldo
          List list = null;

          try
          {
            list = getList();

            if (list == null)
              return;

            for (int i=0;i<list.size();++i)
            {
              Object item = list.get(i);
              if (!(item instanceof Konto))
                continue; // Ist eine Konto-Gruppe

              Konto k = (Konto) item;
              if (BeanUtil.equals(konto,k))
              {
                list.set(i,konto);
                break;
              }
            }

            // Liste neu zeichnen lassen. Das aktualisiert die Kommentare
            // und den Text in der Kombo-Box
            setValue(getValue());
            setList(list);
            if (listener != null)
              listener.handleEvent(null);
          }
          catch (NoSuchMethodError e)
          {
            // TODO "getList" hab ich erst am 15.04. eingebaut. Das catch kann hier also mal irgendwann weg
            Logger.warn(e.getMessage() + " - update your jameica installation");
          }
          catch (Exception e)
          {
            Logger.error("unable to refresh konto",e);
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

}
