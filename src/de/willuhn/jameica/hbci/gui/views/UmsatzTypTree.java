/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.MultiInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypTreeExport;
import de.willuhn.jameica.hbci.gui.controller.UmsatzTypTreeControl;
import de.willuhn.jameica.hbci.gui.parts.UmsatzTypVerlauf;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * View zur Anzeige der Umsatz-Kategorien.
 */
public class UmsatzTypTree extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Umsätze nach Kategorien"));

    final UmsatzTypTreeControl control = new UmsatzTypTreeControl(this);

    {
      final TabFolder folder = new TabFolder(this.getParent(), SWT.NONE);
      folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      TabGroup tab = new TabGroup(folder,i18n.tr("Anzeige einschränken"));

      ColumnLayout cols = new ColumnLayout(tab.getComposite(),2);

      Container left = new SimpleContainer(cols.getComposite());
      left.addInput(control.getKontoAuswahl());

      Input t = control.getText();
      left.addInput(t);

      // Duerfen wir erst nach dem Zeichnen
      final Listener l = control.changedListener(t);
      t.getControl().addKeyListener(new KeyAdapter(){
        /**
         * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
         */
        public void keyReleased(KeyEvent e)
        {
          l.handleEvent(null);
        }
      });

      Container right = new SimpleContainer(cols.getComposite());

      right.addInput(control.getRange());
      MultiInput range = new MultiInput(control.getStart(),control.getEnd());
      right.addInput(range);
    }

    ButtonArea buttons = new ButtonArea();

    buttons.addButton(i18n.tr("Alle aufklappen/zuklappen"), new Action() {

      public void handleAction(Object context) throws ApplicationException
      {
        control.handleExpand();
      }
    },null,false,"folder.png");
    buttons.addButton(i18n.tr("Exportieren..."), new Action(){
      public void handleAction(Object context) throws ApplicationException
      {
        // Muss ich in die Action verpacken, weil der Button sonst mit dem
        // Default-Tree gefuellt wird. Wird die Aktion dann tatsaechlich
        // ausgefuehrt, wuerde die Action immer den gleichen Tree erhalten -
        // unabhaengig davon, was in der View gerade angezeigt wird.
        // Siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?p=43866#43866
        try
        {
          new UmsatzTypTreeExport().handleAction(control.getUmsatzTree());
        }
        catch (RemoteException re)
        {
          Logger.error("unable to load umsatz tree",re);
          throw new ApplicationException(i18n.tr("Fehler beim Laden der Umsätze"),re);
        }
      }
    },null,false,"document-save.png");
    buttons.addButton(i18n.tr("Aktualisieren"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleReload();
      }
    }, null, true, "view-refresh.png");
  
    buttons.paint(getParent());

    final TabFolder folder = new TabFolder(getParent(), SWT.NONE);
    folder.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        if (folder.getSelectionIndex() == 1)
          control.handleRefreshChart();
      }
    });
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));

    TabGroup tg1 = new TabGroup(folder,i18n.tr("Tabellarisch"),true,1);
    TreePart tree = control.getTree();
    tree.paint(tg1.getComposite());

    final TabGroup tg2 = new TabGroup(folder,i18n.tr("Im Verlauf"),true,1);
    UmsatzTypVerlauf chart = control.getChart();
    chart.paint(tg2.getComposite());

  }
}
