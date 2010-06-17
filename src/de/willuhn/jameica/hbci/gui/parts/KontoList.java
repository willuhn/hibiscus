/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/KontoList.java,v $
 * $Revision: 1.17 $
 * $Date: 2010/06/17 11:37:17 $
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

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.Settings;
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

    addColumn(i18n.tr("Kontonummer"),"kontonummer");
    addColumn(i18n.tr("Bankleitzahl"),"blz", new Formatter() {
      public String format(Object o)
      {
        if (o == null)
          return null;
        try
        {
          String blz = o.toString();
          String name = HBCIUtils.getNameForBLZ(blz);
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
    addColumn(i18n.tr("Kommentar"),"kommentar");
    addColumn(i18n.tr("HBCI-Medium"),"passport_class", new Formatter() {
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
          return i18n.tr("Fehler beim Ermitteln des HBCI-Mediums");
        }
      }
    });
    addColumn(i18n.tr("Saldo"),"saldo");
    setFormatter(new TableFormatter()
    {
      public void format(TableItem item)
      {
        Konto k = (Konto) item.getData();
        try {
          double saldo = k.getSaldo();
          if ((saldo == 0 && k.getSaldoDatum() == null) || Double.isNaN(saldo))
            item.setText(5,"");
          else
            item.setText(5,HBCI.DECIMALFORMAT.format(k.getSaldo()) + " " + k.getWaehrung());

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
            item.setForeground(Color.WIDGET_FG.getSWTColor());

          
          // Den Saldo faerben wir extra
          if (k.getSaldo() < 0) // Negativer Saldo rot
            item.setForeground(5,Settings.getBuchungSollForeground());
          else if (k.getSaldo() > 0) // Positiver Saldo gruen
            item.setForeground(5,Settings.getBuchungHabenForeground());
          
          Konto kd = Settings.getDefaultKonto();
          if (kd != null && kd.equals(k))
            item.setFont(Font.BOLD.getSWTFont());
          else
            item.setFont(Font.DEFAULT.getSWTFont());
          
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
    i.setOrder("ORDER BY blz, bezeichnung");
    return i;
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


/**********************************************************************
 * $Log: KontoList.java,v $
 * Revision 1.17  2010/06/17 11:37:17  willuhn
 * @N Farben der Konten etwas uebersichtlicher gestaltet
 *
 * Revision 1.16  2010/04/22 15:43:06  willuhn
 * @B Debugging
 * @N Kontoliste aktualisieren
 *
 * Revision 1.15  2009/09/15 00:23:35  willuhn
 * @N BUGZILLA 745
 *
 * Revision 1.14  2009/07/09 17:08:03  willuhn
 * @N BUGZILLA #740
 *
 * Revision 1.13  2009/01/05 10:13:46  willuhn
 * @B In der Spalte "HBCI-Medium" wurd versehentlich der Saldo angezeigt
 *
 * Revision 1.12  2009/01/04 17:43:29  willuhn
 * @N BUGZILLA 532
 *
 * Revision 1.11  2009/01/04 16:38:55  willuhn
 * @N BUGZILLA 523 - ein Konto kann jetzt als Default markiert werden. Das wird bei Auftraegen vorausgewaehlt und ist fett markiert
 *
 * Revision 1.10  2007/08/29 10:04:42  willuhn
 * @N Bug 476
 *
 * Revision 1.9  2006/05/11 16:53:09  willuhn
 * @B bug 233
 *
 * Revision 1.8  2006/04/25 23:25:12  willuhn
 * @N bug 81
 *
 * Revision 1.7  2005/08/01 16:10:41  web0
 * @N synchronize
 *
 * Revision 1.6  2005/06/27 15:35:27  web0
 * @B bug 84
 *
 * Revision 1.5  2005/06/23 22:02:53  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/06/23 22:01:04  web0
 * @N added hbci media to account list
 *
 * Revision 1.3  2005/06/21 20:11:10  web0
 * @C cvs merge
 *
 * Revision 1.2  2005/05/08 17:48:51  web0
 * @N Bug 56
 *
 * Revision 1.1  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 **********************************************************************/