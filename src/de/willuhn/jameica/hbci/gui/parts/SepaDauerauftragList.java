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
import java.util.Date;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.datasource.GenericObject;
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
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.parts.columns.KontoColumn;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.messaging.ObjectMessage;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste der SEPA-Dauerauftraege.
 */
public class SepaDauerauftragList extends TablePart implements Part
{
  private MessageConsumer mc = null;

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @param action
   * @throws RemoteException
   */
  public SepaDauerauftragList(Action action) throws RemoteException
  {
    super(Settings.getDBService().createList(SepaDauerauftrag.class), action);

    final boolean bold = Settings.getBoldValues();
    
    setFormatter(new TableFormatter()
    {
      public void format(TableItem item)
      {
        try
        {
          if (item == null || item.getData() == null)
            return;
          SepaDauerauftrag d = (SepaDauerauftrag) item.getData();

          if (bold)
            item.setFont(4,Font.BOLD.getSWTFont());

          item.setFont(!d.isActive() ? Font.BOLD.getSWTFont() : Font.DEFAULT.getSWTFont());
          
          if (d.getLetzteZahlung() != null && new Date().after(d.getLetzteZahlung()))
            item.setForeground(Color.COMMENT.getSWTColor());
        }
        catch (Exception e)
        {
          Logger.error("error while checking finish date",e);
          GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Pr�fen des Ablaufdatums eines SEPA-Dauerauftrages"));
        }
      }
    });
    addColumn(new KontoColumn());
    addColumn(i18n.tr("Gegenkonto Inhaber"),"empfaenger_name");
    addColumn(i18n.tr("Gegenkonto BIC"),"empfaenger_bic");
    addColumn(i18n.tr("Verwendungszweck"),"zweck");
    addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    addColumn(i18n.tr("Turnus"),"turnus_id");
    addColumn(i18n.tr("N�chste Zahlung"),"naechste_zahlung", new DateFormatter(HBCI.DATEFORMAT),false,Column.ALIGN_RIGHT);
    addColumn(i18n.tr("aktiv?"),"orderid",new Formatter()
    {
      @Override
      public String format(Object o)
      {
        if (o == null)
          return "nein";
        String s = o.toString();
        if (s != null && s.length() > 0)
          return i18n.tr("ja");
        return i18n.tr("nein");
      }
    });

    // BUGZILLA 84 http://www.willuhn.de/bugzilla/show_bug.cgi?id=84
    setRememberOrder(true);

    // BUGZILLA 233 http://www.willuhn.de/bugzilla/show_bug.cgi?id=233
    setRememberColWidths(true);

    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.SepaDauerauftragList());
    
    // Wir erstellen noch einen Message-Consumer, damit wir ueber neu eintreffende und geaenderte Dauerauftraege
    // informiert werden.
    this.mc = new TransferMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.mc);
  }
  
  @Override
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
    @Override
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{
        ImportMessage.class,
        ObjectChangedMessage.class
      };
    }

    @Override
    public void handleMessage(final Message message) throws Exception
    {
      if (message == null)
        return;
      
      final GenericObject o = ((ObjectMessage)message).getObject();
      
      if (o == null)
        return;
      
      // Checken, ob uns der Transfer-Typ interessiert
      if (!(o instanceof SepaDauerauftrag))
        return;

      GUI.startSync(new Runnable() {
        public void run()
        {
          try
          {
            if (message instanceof ObjectChangedMessage)
            {
              updateItem(o,o);
            }
            else if (message instanceof ImportMessage)
            {
              addItem(o);
              sort();
            }
            
          }
          catch (Exception e)
          {
            Logger.error("unable to update item",e);
          }
        }
      });
    }

    @Override
    public boolean autoRegister()
    {
      return false;
    }
  }

}
