/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
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
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
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

  private static List<String> groups = null;

  private KontoListener listener = null;
  private String token = null;
  private boolean store = true;
  private Control control = null;
  private boolean supportGroups = false;

  private final MessageConsumer mc = new SaldoMessageConsumer();

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
    // Wenn nur ein Konto hinterlegt ist das gleich vorselektieren
    if ( super.getList().size() == 1 ) {
      super.setPreselected(super.getList().get(0));
    } else {
      setPleaseChoose(i18n.tr("Bitte w�hlen..."));
    }
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

    final String id = settings.getString(this.token,null);
    if (id != null && id.length() > 0)
    {
      try
      {
        final Konto k = (Konto) Settings.getDBService().createObject(Konto.class,id);
        this.setPreselected(k);
      }
      catch (final Exception e)
      {
        // wir koennen leider nicht checken, ob "id" =~ /[0-9]{1,9}/ ist, weil der Gruppen-Name ja auch nur aus Zahlen bestehen kann
        // daher halt direkt im catch der ObjectNotFoundException
        if (this.supportGroups)
          this.setPreselected(id); // Koennte eine Kategorie sein
        else
          settings.setAttribute(this.token,(String) null); // Konto konnte nicht geladen werden. Vorauswahl loeschen
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
  @Override
  public Control getControl()
  {
    if (this.control != null)
      return this.control;

    this.control = super.getControl();

    Application.getMessagingFactory().registerMessageConsumer(this.mc);
    this.control.addDisposeListener(new DisposeListener() {
      @Override
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
      final Object o = getValue();
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
    catch (final Exception e)
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
    final boolean haveGroups = groups.size() > 0;

    final DBIterator it = Settings.getDBService().createList(Konto.class);
    it.setOrder("ORDER BY LOWER(kategorie), blz, kontonummer, bezeichnung");
    final List l = new ArrayList();

    String current = null;

    while (it.hasNext())
    {
      final Konto k = (Konto) it.next();

      if (filter == null || filter.accept(k))
      {
        if (haveGroups)
        {
          final String kat = StringUtils.trimToNull(k.getKategorie());
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
  @Override
  public Object getValue()
  {
    final Object o = super.getValue();

    if ((o instanceof String) && !this.supportGroups) // Kategorie
    {
      GUI.getView().setErrorText(i18n.tr("Die Auswahl einer Konto-Gruppen ist hier nicht m�glich"));
      return null;
    }
    return o;
  }

  /**
   * @see de.willuhn.jameica.gui.input.SelectInput#format(java.lang.Object)
   */
  @Override
  protected String format(Object bean)
  {
    if (bean == null)
      return null;

    if (!(bean instanceof Konto))
      return bean.toString();

    try
    {
      final Konto k = (Konto) bean;

      final boolean disabled = k.hasFlag(Konto.FLAG_DISABLED);

      final StringBuffer sb = new StringBuffer();
      if (groups.size() > 0)
        sb.append("   "); // Wir haben Gruppen - also einruecken
      if (disabled)
        sb.append("[");

      sb.append(i18n.tr("Kto. {0}",k.getKontonummer()));

      final String blz = k.getBLZ();
      sb.append(" [");
      final String bankName = HBCIUtils.getNameForBLZ(blz);
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

      final String bez = k.getBezeichnung();
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
    catch (final RemoteException re)
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
    @Override
    public void handleEvent(Event event) {

      try {
        final Object o = getValue();
        if (o == null || !(o instanceof Konto))
        {
          setComment("");
          return;
        }

        final Konto konto = (Konto) o;
        final String w = konto.getWaehrung();

        final Date datum = konto.getSaldoDatum();
        if (datum != null)
          setComment(i18n.tr("Saldo: {0} {1} vom {2}", new String[]{HBCI.DECIMALFORMAT.format(konto.getSaldo()),w,HBCI.DATEFORMAT.format(datum)}));
        else
          setComment("");
      }
      catch (final RemoteException er)
      {
        Logger.error("error while updating currency",er);
        GUI.getStatusBar().setErrorText(i18n.tr("Fehler bei Ermittlung der W�hrung"));
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
    @Override
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{SaldoMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    @Override
    public void handleMessage(Message message) throws Exception
    {
      final SaldoMessage msg = (SaldoMessage) message;
      final GenericObject o = msg.getObject();
      if (!(o instanceof Konto))
        return;

      final Konto konto = (Konto) o;

      GUI.getDisplay().syncExec(new Runnable() {
        @Override
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
              final Object item = list.get(i);
              if (!(item instanceof Konto))
                continue; // Ist eine Konto-Gruppe

              final Konto k = (Konto) item;
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
          catch (final NoSuchMethodError e)
          {
            // TODO "getList" hab ich erst am 15.04. eingebaut. Das catch kann hier also mal irgendwann weg
            Logger.warn(e.getMessage() + " - update your jameica installation");
          }
          catch (final Exception e)
          {
            Logger.error("unable to refresh konto",e);
          }
        }
      });
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    @Override
    public boolean autoRegister()
    {
      return false;
    }
  }

}
