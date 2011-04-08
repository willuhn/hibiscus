/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/UmsatzTypTree.java,v $
 * $Revision: 1.13 $
 * $Date: 2011/04/08 15:19:13 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TreePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.LabelGroup;
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

    LabelGroup settings = new LabelGroup(getParent(), i18n.tr("Anzeige einschränken"));

    settings.addLabelPair(i18n.tr("Konto"), control.getKontoAuswahl());
    settings.addLabelPair(i18n.tr("Start-Datum"), control.getStart());
    settings.addLabelPair(i18n.tr("End-Datum"), control.getEnd());

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

    TabFolder folder = new TabFolder(getParent(), SWT.NONE);
    folder.addFocusListener(new FocusAdapter()
    {
      /**
       * @see org.eclipse.swt.events.FocusAdapter#focusGained(org.eclipse.swt.events.FocusEvent)
       */
      public void focusGained(FocusEvent e)
      {
        control.handleRefreshChart();
      }
    });
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));
    folder.setBackground(Color.BACKGROUND.getSWTColor());
    
    TabGroup tg1 = new TabGroup(folder,i18n.tr("Tabellarisch"));
    TreePart tree = control.getTree();
    tree.paint(tg1.getComposite());
    
    final TabGroup tg2 = new TabGroup(folder,i18n.tr("Im Verlauf"));
    UmsatzTypVerlauf chart = control.getChart();
    chart.paint(tg2.getComposite());

  }

}
/*******************************************************************************
 * $Log: UmsatzTypTree.java,v $
 * Revision 1.13  2011/04/08 15:19:13  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.12  2010/03/05 15:24:53  willuhn
 * @N BUGZILLA 686
 ******************************************************************************/