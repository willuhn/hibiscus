/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.CustomRangeEdit;
import de.willuhn.jameica.hbci.server.Range;
import de.willuhn.jameica.hbci.server.Range.Category;
import de.willuhn.jameica.hbci.server.Range.CustomRange;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Vorkonfigurierte Liste mit auswaehlbaren Zeitraeumen fuer eine bestimmte Kategorie.
 */
public class RangeList extends TablePart
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private Category category = null;
  
  /**
   * ct.
   * @param category die Kategorie.
   */
  public RangeList(final Category category)
  {
    super(new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        if (!(context instanceof CustomRange))
          return;
        
        new CustomRangeEdit(category).handleAction(context);
      }
    });
    this.category = category;
    
    this.setFormatter(new TableFormatter() {
      
      @Override
      public void format(TableItem item)
      {
        final Object data = item.getData();
        if (data instanceof CustomRange)
          item.setFont(Font.BOLD.getSWTFont());
      }
    });
    
    this.setCheckable(true);
    this.setMulti(false);
    this.setRememberColWidths(true);
    this.setRememberState(true);
    this.setRememberOrder(false);
    this.removeFeature(FeatureSummary.class);
    this.addColumn(i18n.tr("Bezeichnung"),null);

    final ContextMenu ctx = new ContextMenu();
    ctx.addItem(new ContextMenuItem(i18n.tr("Neuer Zeitraum..."),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        new CustomRangeEdit(category).handleAction(CustomRange.create());
      }
    },"document-new.png"));
    ctx.addItem(new CustomRangeContextMenuItem(i18n.tr("Bearbeiten..."),new CustomRangeEdit(category),"document-open.png"));
    ctx.addItem(new CustomRangeContextMenuItem(i18n.tr("Löschen"),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        Range.deleteCustomRange(category,(CustomRange) context);
        reload();
      }
    },"user-trash-full.png"));
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new ContextMenuItem(i18n.tr("Zurücksetzen"), new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        Range.resetActiveRanges(category);
        final List<Range> active = Range.getActiveRanges(category);
        for (Range r:Range.getAllRanges(category))
        {
          setChecked(r,active.contains(r));
        }
      }
    },"edit-undo.png"));
    this.setContextMenu(ctx);

  }
  
  /**
   * Contextmenü-Eintrag für die benutzerdefinierten Zeiträume.
   */
  private class CustomRangeContextMenuItem extends CheckedSingleContextMenuItem
  {
    /**
     * ct.
     * @param name der Name.
     * @param action die Aktion.
     * @param icon das Icon.
     */
    public CustomRangeContextMenuItem(String name, Action action, String icon)
    {
      super(name,action,icon);
    }
    
    /**
     * @see de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem#isEnabledFor(java.lang.Object)
     */
    @Override
    public boolean isEnabledFor(Object o)
    {
      if (!super.isEnabledFor(o))
        return false;
      
      return (o instanceof CustomRange);
    }
  }
  
  /**
   * @see de.willuhn.jameica.gui.parts.TablePart#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public synchronized void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);
    this.reload();
  }
  
  /**
   * Lädt die Zeiträume neu.
   */
  private void reload()
  {
    try
    {
      this.removeAll();
      final List<Range> active = Range.getActiveRanges(category);
      for (Range r:Range.getAllRanges(category))
      {
        this.addItem(r,active.contains(r));
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to load ranges",e);
    }
  }

}
