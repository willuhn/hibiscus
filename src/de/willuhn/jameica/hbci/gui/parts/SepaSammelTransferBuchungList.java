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

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.formatter.IbanFormatter;
import de.willuhn.jameica.hbci.messaging.ImportMessage;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransferBuchung;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Vorkonfigurierte Tabelle mit Buchungen eines SEPA-Sammelauftrages.
 */
public class SepaSammelTransferBuchungList extends TablePart
{
  private I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private MessageConsumer mc = null;

  /**
   * ct.
   * @param t
   * @param action
   * @throws RemoteException
   */
  public SepaSammelTransferBuchungList(final SepaSammelTransfer t, Action action) throws RemoteException
  {
    this(t.getBuchungen(),action);
  }

  /**
   * ct.
   * @param list Liste von Buchungen (SammelTransferBuchung).
   * @param action Aktion, die beim Klick ausgefuehrt werden soll.
   * @throws RemoteException
   */
  public SepaSammelTransferBuchungList(final List<? extends SepaSammelTransferBuchung> list, Action action) throws RemoteException
  {
    super(list,action);
    
    addColumn(i18n.tr("Auftrag"),"this", new Formatter() {
      public String format(Object o)
      {
        if (o == null || !(o instanceof SepaSammelTransferBuchung))
          return null;
        try
        {
          SepaSammelTransferBuchung sb = (SepaSammelTransferBuchung) o;
          SepaSammelTransfer s = sb.getSammelTransfer();
          if (s == null)
            return null;
          return i18n.tr("{0}: {1}", HBCI.DATEFORMAT.format(s.getTermin()), s.getBezeichnung());
        }
        catch (RemoteException e)
        {
          Logger.error("unable to read name of sammeltransfer",e);
          return i18n.tr("Zugehöriger Sammel-Auftrag nicht ermittelbar");
        }
      }
    });
    addColumn(i18n.tr("Verwendungszweck"),"zweck");
    addColumn(i18n.tr("Kontoinhaber"),"empfaenger_name");
    addColumn(i18n.tr("IBAN"),"empfaenger_konto", new IbanFormatter());
    addColumn(i18n.tr("BIC"),"empfaenger_bic");
    addColumn(i18n.tr("Betrag"),"betrag",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT),false,Column.ALIGN_RIGHT);

    final boolean bold = Settings.getBoldValues();
    
    setFormatter(new TableFormatter() {
      public void format(TableItem item) {
        try {
          SepaSammelTransferBuchung b = (SepaSammelTransferBuchung) item.getData();
          if (b.getSammelTransfer().ausgefuehrt())
            item.setForeground(Color.COMMENT.getSWTColor());
          
          if (bold)
            item.setFont(5,Font.BOLD.getSWTFont());
        }
        catch (RemoteException e) {
          Logger.error("unable to read sepa sammeltransfer",e);
        }
      }
    });
    setRememberColWidths(true);
    setRememberOrder(true);
    
    this.addSelectionListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        refreshSummary();
      }
    });
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
   * @see de.willuhn.jameica.gui.parts.TablePart#getSummary()
   */
  @Override
  protected String getSummary()
  {
    try
    {
      Object o = this.getSelection();
      int size = this.size();
      boolean selective = (o != null && (o instanceof Object[]));
      
      List items = selective ? Arrays.asList((Object[])o) : this.getItems(false);

      double sum = 0.0d;

      String curr = null;
      for (Object item:items)
      {
        SepaSammelTransferBuchung t = (SepaSammelTransferBuchung) item;
        
        if (curr == null)
          curr = t.getSammelTransfer().getKonto().getWaehrung();
        sum += t.getBetrag();
      }
      
      if (curr == null)
        curr = HBCIProperties.CURRENCY_DEFAULT_DE;

      if (selective)
        return i18n.tr("{0} Buchungen, {1} markiert, Summe: {2} {3}",Integer.toString(size),Integer.toString(items.size()),HBCI.DECIMALFORMAT.format(sum),curr);
      return i18n.tr("{0} Buchungen, Summe: {1} {2}",Integer.toString(size),HBCI.DECIMALFORMAT.format(sum),curr);
    }
    catch (Exception e)
    {
      Logger.error("error while updating summary",e);
    }
    return super.getSummary();
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
      
      if (o == null || !(o instanceof SepaSammelTransferBuchung))
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
