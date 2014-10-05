/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/SammelTransferBuchungList.java,v $
 * $Revision: 1.10 $
 * $Date: 2011/08/10 12:47:28 $
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.parts.columns.BlzColumn;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Vorkonfigurierte Tabelle mit Buchungen eines Sammel-Auftrages.
 */
public class SammelTransferBuchungList extends TablePart
{
  private I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private MessageConsumer mc = null;

  // BUGZILLA 107 http://www.willuhn.de/bugzilla/show_bug.cgi?id=107
  /**
   * ct.
   * @param list Liste von Buchungen (SammelTransferBuchung).
   * @param action Aktion, die beim Klick ausgefuehrt werden soll.
   */
  public SammelTransferBuchungList(final GenericIterator list, Action action)
  {
    super(list,action);
    addColumn(i18n.tr("Auftrag"),"this", new Formatter() {
      public String format(Object o)
      {
        if (o == null || !(o instanceof SammelTransferBuchung))
          return null;
        try
        {
          SammelTransferBuchung sb = (SammelTransferBuchung) o;
          SammelTransfer s = sb.getSammelTransfer();
          if (s == null)
            return null;
          return i18n.tr("{0}: {1}", new String[]{HBCI.DATEFORMAT.format(s.getTermin()),s.getBezeichnung()});
        }
        catch (RemoteException e)
        {
          Logger.error("unable to read name of sammeltransfer",e);
          return i18n.tr("Zugehöriger Sammel-Auftrag nicht ermittelbar");
        }
      }
    });
    addColumn(i18n.tr("Verwendungszweck"),"zweck");
    addColumn(i18n.tr("Kontoinhaber"),"gegenkonto_name");
    addColumn(i18n.tr("Kontonummer"),"gegenkonto_nr");
    addColumn(new BlzColumn("gegenkonto_blz",i18n.tr("Bankleitzahl")));
    addColumn(i18n.tr("Betrag"),"this",new Formatter() {
      public String format(Object o)
      {
        if (o == null || !(o instanceof SammelTransferBuchung))
          return null;
        try
        {
          SammelTransferBuchung b = (SammelTransferBuchung) o;
          SammelTransfer s = b.getSammelTransfer();
          String curr = HBCIProperties.CURRENCY_DEFAULT_DE;
          if (s != null)
            curr = s.getKonto().getWaehrung();
          return new CurrencyFormatter(curr,HBCI.DECIMALFORMAT).format(new Double(b.getBetrag()));
        }
        catch (RemoteException e)
        {
          Logger.error("unable to read sammeltransfer");
          return i18n.tr("Betrag nicht ermittelbar");
        }
      }
    });
    addColumn(i18n.tr("Warnungen"),"warnung");

    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        try {
          SammelTransferBuchung b = (SammelTransferBuchung) item.getData();
          if (StringUtils.trimToNull(b.getWarnung()) != null)
            item.setForeground(Color.ERROR.getSWTColor());
          else if (b.getSammelTransfer().ausgefuehrt())
            item.setForeground(Color.COMMENT.getSWTColor());
        }
        catch (RemoteException e) {
          Logger.error("unable to read sammeltransfer",e);
        }
      }
    });
    setRememberColWidths(true);
    setRememberOrder(true);
  }
 
  /**
   * ct.
   * @param a der Sammel-Auftrag, fuer den die Buchungen angezeigt werden sollen.
   * @param action Aktion, die beim Klick ausgefuehrt werden soll.
   * @throws RemoteException
   */
  public SammelTransferBuchungList(final SammelTransfer a, Action action) throws RemoteException
  {
    super(a.getBuchungen(), action);
    addColumn(i18n.tr("Verwendungszweck"),"zweck");
    addColumn(i18n.tr("Kontoinhaber"),"gegenkonto_name");
    addColumn(i18n.tr("Kontonummer"),"gegenkonto_nr");
    addColumn(i18n.tr("Bankleitzahl"),"gegenkonto_blz", new Formatter() {
      /**
       * @see de.willuhn.jameica.gui.formatter.Formatter#format(java.lang.Object)
       */
      public String format(Object o)
      {
        if (o == null)
          return null;
        String blz = o.toString();
        String name = HBCIProperties.getNameForBank(blz);
        if (name != null && name.length() > 0)
          blz += " [" + name + "]";
        return blz;
      }
    
    });
    Konto k = a.getKonto();
    String curr = k != null ? k.getWaehrung() : "";
    addColumn(i18n.tr("Betrag"),"betrag",new CurrencyFormatter(curr,HBCI.DECIMALFORMAT));
    addColumn(i18n.tr("Warnungen"),"warnung");

    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        try {
          SammelTransferBuchung b = (SammelTransferBuchung) item.getData();
          if (StringUtils.trimToNull(b.getWarnung()) != null)
            item.setForeground(Color.ERROR.getSWTColor());
          else if (a.ausgefuehrt())
            item.setForeground(Color.COMMENT.getSWTColor());
        }
        catch (RemoteException e) { /*ignore */}
      }
    });
    
    setRememberColWidths(true);
    setRememberOrder(true);
    setMulti(true); // BUGZILLA 1118
    // Wir erstellen noch einen Message-Consumer, damit wir ueber neu eintreffende
    // Buchungen informiert werden.
    this.mc = new STMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);
  }

  /**
   * Ueberschrieben, um einen DisposeListener an das Composite zu haengen.
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
   * Hilfsklasse damit wir ueber importierte Buchungen informiert werden.
   */
  public class STMessageConsumer implements MessageConsumer
  {
    /**
     * ct.
     */
    public STMessageConsumer()
    {
      super();
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{ImportMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      if (message == null || !(message instanceof ImportMessage))
        return;
      final GenericObject o = ((ImportMessage)message).getObject();
      
      if (o == null || !(o instanceof SammelTransferBuchung))
        return;
      
      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          try
          {
            addItem(o);
            sort();
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
