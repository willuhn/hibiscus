/**********************************************************************
 *
 * Copyright (c) 2024 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.forecast.ForecastCreator;
import de.willuhn.jameica.hbci.forecast.SaldoLimit;
import de.willuhn.jameica.hbci.forecast.SaldoLimit.Type;
import de.willuhn.jameica.hbci.gui.action.KontoLimitsConfigure;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt Saldo-Limits an, insofern welche in nächster Zeit erreicht werden.
 */
public class SaldoLimits extends AbstractBox implements Box
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 0;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Saldo-Limits der Konten");
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    final List<SaldoLimit> limits = ForecastCreator.getLimits();
    
    if (limits.isEmpty())
      return;

    final Container c = new SimpleContainer(parent);
    c.addText(i18n.tr("Bei den folgenden Konten werden die Salden voraussichtlich die angegebenen Limits erreichen."),true,Color.ERROR);
    
    final TablePart table = new TablePart(limits,new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null)
          return;
        
        SaldoLimit l = (SaldoLimit) context;
        new KontoNew().handleAction(l.getKonto());
      }
    });
    table.setRememberColWidths(true);
    table.setRememberOrder(true);
    table.removeFeature(FeatureSummary.class);
    table.addColumn(i18n.tr("Konto"),"konto",k -> KontoUtil.toString((Konto) k));
    table.addColumn(i18n.tr("Art des Limits"),"type",t -> ((Type)t).getDescription());
    table.addColumn(i18n.tr("Limit"),"value",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    table.addColumn(i18n.tr("Voraussichtlich erreicht am"),"date", new DateFormatter(HBCI.DATEFORMAT),false,Column.ALIGN_RIGHT);

    final AtomicInteger notify = new AtomicInteger();

    table.setFormatter(new TableFormatter() {
      
      @Override
      public void format(TableItem item)
      {
        final SaldoLimit limit = (SaldoLimit) item.getData();
        if (limit.isNotify())
        {
          item.setFont(Font.BOLD.getSWTFont());
          notify.incrementAndGet();
        }
      }
    });
    table.paint(parent);

    final ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Aktualisieren"),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          ForecastCreator.updateLimits();
          table.removeAll();
          notify.set(0);
          for (SaldoLimit l:ForecastCreator.getLimits())
          {
            table.addItem(l);
          }
          GUI.getNavigation().setUnreadCount("jameica.start",notify.get());
        }
        catch (RemoteException re)
        {
          Logger.error("unable to reload limits",re);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Aktualisieren der Limits fehlgeschlagen"),StatusBarMessage.TYPE_ERROR));
        }
      }
    },null,false,"view-refresh.png");
    buttons.addButton(i18n.tr("Limits konfigurieren") + "...",new KontoLimitsConfigure(),null,false,"office-chart-area.png");
    buttons.paint(parent);
    
    GUI.getNavigation().setUnreadCount("jameica.start",notify.get());
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#isActive()
   */
  public boolean isActive()
  {
    return super.isActive() && isEnabled(); // Nicht konfigurierbar
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isEnabled()
   */
  public boolean isEnabled()
  {
    // Box wird angezeigt, sobald Limits gefunden wurden
    return !ForecastCreator.getLimits().isEmpty();
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#getHeight()
   */
  @Override
  public int getHeight()
  {
    return 220;
  }
}
