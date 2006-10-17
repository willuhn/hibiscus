/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/AbstractTransferList.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/10/17 00:04:31 $
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

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.io.ImportMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit Transfers.
 */
public abstract class AbstractTransferList extends TablePart implements Part
{
  private I18N i18n = null;

  private MessageConsumer mc = null;

  /**
   * ct.
   * @param list Liste der anzuzeigenden Transfers.
   * @param action
   */
  public AbstractTransferList(GenericIterator list, Action action)
  {
    super(list, action);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        Terminable l = (Terminable) item.getData();
        if (l == null)
          return;

        try {
          if (l.getTermin().before(new Date()) && !l.ausgefuehrt())
          {
            item.setForeground(Settings.getUeberfaelligForeground());
          }
        }
        catch (RemoteException e) { /*ignore */}
      }
    });

    addColumn(i18n.tr("Konto"),"konto_id", new Formatter() {
      /**
       * @see de.willuhn.jameica.gui.formatter.Formatter#format(java.lang.Object)
       */
      public String format(Object o)
      {
        if (o == null || !(o instanceof Konto))
          return null;
        Konto k = (Konto) o;
        try
        {
          String s = k.getKontonummer();
          String name = k.getBezeichnung();
          if (name != null && name.length() > 0)
            s += " [" + name + "]";
          return s;
        }
        catch (RemoteException r)
        {
          Logger.error("unable to display konto",r);
          return null;
        }
      }
    
    });
    addColumn(i18n.tr("Gegenkonto Inhaber"),"empfaenger_name");
    addColumn(i18n.tr("Gegenkonto BLZ"),"empfaenger_blz", new Formatter() {
      /**
       * @see de.willuhn.jameica.gui.formatter.Formatter#format(java.lang.Object)
       */
      public String format(Object o)
      {
        if (o == null)
          return null;
        String blz = o.toString();
        String name = HBCIUtils.getNameForBLZ(blz);
        if (name != null && name.length() > 0)
          blz += " [" + name + "]";
        return blz;
      }
    
    });
    addColumn(i18n.tr("Verwendungszweck"),"zweck");
    addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    addColumn(i18n.tr("Termin"),"termin", new DateFormatter(HBCI.LONGDATEFORMAT));
    addColumn(i18n.tr("Status"),"ausgefuehrt",new Formatter() {
      public String format(Object o) {
        try {
          int i = ((Integer) o).intValue();
          return i == 1 ? i18n.tr("ausgeführt") : i18n.tr("offen");
        }
        catch (Exception e) {}
        return ""+o;
      }
    });
  
    // BUGZILLA 84 http://www.willuhn.de/bugzilla/show_bug.cgi?id=84
    setRememberOrder(true);

    // BUGZILLA 233 http://www.willuhn.de/bugzilla/show_bug.cgi?id=233
    setRememberColWidths(true);
    
    setMulti(true);

    // Wir erstellen noch einen Message-Consumer, damit wir ueber neu eintreffende
    // Transfers informiert werden.
    this.mc = new TransferMessageConsumer();
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
   * Hilfsklasse damit wir ueber importierte Transfers informiert werden.
   */
  public class TransferMessageConsumer implements MessageConsumer
  {
    /**
     * ct.
     */
    public TransferMessageConsumer()
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
      final GenericObject o = ((ImportMessage)message).getImportedObject();
      
      if (o == null || !(o instanceof Transfer))
        return;
      
      GUI.getDisplay().syncExec(new Runnable() {
        public void run()
        {
          try
          {
            addItem(o);
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
 * $Log: AbstractTransferList.java,v $
 * Revision 1.1  2006/10/17 00:04:31  willuhn
 * @N new Formatters in Transfer-Listen
 * @N merged UeberweisungList + LastschriftList into AbstractTransferList
 *
 **********************************************************************/