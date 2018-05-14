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

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.ColorUtil;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectMessage;
import de.willuhn.jameica.hbci.messaging.SaldoMessage;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste aller Konten.
 */
public class KontoList extends TablePart implements Part
{

  // BUGZILLA 476
  private MessageConsumer mc = null;

  private I18N i18n;

  /**
   * @param action
   * @throws RemoteException
   */
  public KontoList(Action action) throws RemoteException
  {
    this(init(), action);
  }

  /**
   * ct.
   * @param konten
   * @param action
   */
  public KontoList(GenericIterator konten, Action action)
  {
    super(konten,action);

    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

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
    addSaldoAvailable(konten);
    // BUGZILLA 108 http://www.willuhn.de/bugzilla/show_bug.cgi?id=108
    addColumn(i18n.tr("Saldo aktualisiert am"),"saldo_datum", new DateFormatter(HBCI.LONGDATEFORMAT));
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
            item.setText(saldocolumn,HBCI.DECIMALFORMAT.format(k.getSaldo()) + " " + k.getWaehrung());

          // Checken, ob Konto deaktiviert ist
          int flags = k.getFlags();

          // Deaktivierte Konten grau
          if ((flags & Konto.FLAG_DISABLED) == Konto.FLAG_DISABLED)
            item.setForeground(Color.COMMENT.getSWTColor());

          // Offline-Konten blau
          else if ((flags & Konto.FLAG_OFFLINE) == Konto.FLAG_OFFLINE)
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
        refreshSummary();
      }
    });
  }

  /**
   * Fuegt die Spalte "verfuegbarer Betrag" hinzu, wenn wenigstens ein Konto
   * aus der Liste einen solchen besitzt.
   * @param konten Liste der zu checkenden Konten.
   */
  private void addSaldoAvailable(GenericIterator konten)
  {
    try
    {
      while (konten.hasNext())
      {
        Konto k = (Konto) konten.next();
        if ((k.getFlags() & Konto.FLAG_OFFLINE) == Konto.FLAG_OFFLINE)
          continue; // ignorieren
        double d = k.getSaldoAvailable();
        if (!Double.isNaN(d))
        {
          // Wir haben tatsaechlich eines, wo was drin steht
          Column col = new Column("saldo_available",i18n.tr("Verfügbar"),new CurrencyFormatter(k.getWaehrung(),HBCI.DECIMALFORMAT),false,Column.ALIGN_RIGHT);
          addColumn(col);
          return;
        }
      }
    }
    catch (RemoteException re)
    {
      Logger.error("unable to check if at least one account has an available value",re);
    }
    finally
    {
      try
      {
        konten.begin();
      }
      catch (Exception e)
      {
        Logger.error("unable to reset iterator",e);
      }
    }
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    parent.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e)
      {
        Application.getMessagingFactory().unRegisterMessageConsumer(mc);
      }
    });
    super.paint(parent);
  }

  /**
   * Initialisiert die Konten-Liste.
   * @return Liste der Konten.
   * @throws RemoteException
   */
  private static DBIterator init() throws RemoteException
  {
    DBIterator i = Settings.getDBService().createList(Konto.class);
    i.setOrder("ORDER BY LOWER(kategorie), blz, kontonummer, bezeichnung");
    return i;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.TablePart#getSummary()
   */
  protected String getSummary()
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
    catch (Exception e)
    {
      Logger.error("error while updating summary",e);
    }
    return super.getSummary();
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
           refreshSummary();
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
}
