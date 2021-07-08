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

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.server.Range;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Vorkonfigurierte Liste mit auswaehlbaren Zeitraeumen fuer eine bestimmte Kategorie.
 */
public class RangeList extends TablePart
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private String category = null;

  /**
   * ct.
   * @param category die Kategorie.
   */
  public RangeList(final String category)
  {
    super(null);
    this.setCheckable(true);
    this.setMulti(false);
    this.setRememberColWidths(true);
    this.setRememberOrder(false);
    this.removeFeature(FeatureSummary.class);
    this.addColumn(i18n.tr("Bezeichnung"),null);
    this.category = category;

    final ContextMenu ctx = new ContextMenu();
    ctx.addItem(new ContextMenuItem(i18n.tr("Zurücksetzen"), new Action() {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        Range.resetActiveRanges(category);
        final List<Range> active = Range.getActiveRanges(category);
        for (Range r:Range.KNOWN)
        {
          setChecked(r,active.contains(r));
        }
      }
    },"edit-undo.png"));
    this.setContextMenu(ctx);

  }

  /**
   * @see de.willuhn.jameica.gui.parts.TablePart#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public synchronized void paint(Composite parent) throws RemoteException
  {
    super.paint(parent);

    final List<Range> active = Range.getActiveRanges(category);
    for (Range r:Range.KNOWN)
    {
      this.addItem(r,active.contains(r));
    }
  }

}
