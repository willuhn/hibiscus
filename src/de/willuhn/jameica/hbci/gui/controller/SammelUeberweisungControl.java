/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungBuchungNew;
import de.willuhn.jameica.hbci.gui.action.SammelUeberweisungNew;
import de.willuhn.jameica.hbci.gui.action.UeberweisungNew;
import de.willuhn.jameica.hbci.gui.parts.SammelTransferBuchungList;
import de.willuhn.jameica.hbci.gui.parts.SammelUeberweisungList;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisung;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung des Controllers fuer den Dialog "Liste der Sammellastschriften".
 */
public class SammelUeberweisungControl extends AbstractSammelTransferControl<SammelUeberweisung>
{
  private SammelUeberweisung transfer  = null;
  private SammelUeberweisungList table = null;
  private TablePart buchungen          = null;

  /**
   * ct.
   * @param view
   */
  public SammelUeberweisungControl(AbstractView view)
  {
    super(view);
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSammelTransferControl#getTransfer()
   */
  public SammelUeberweisung getTransfer() throws RemoteException
  {
    if (transfer != null)
      return transfer;

    transfer = (SammelUeberweisung) getCurrentObject();
    if (transfer != null)
      return transfer;

    transfer = (SammelUeberweisung) Settings.getDBService().createObject(SammelUeberweisung.class,null);
    return transfer;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSammelTransferControl#getListe()
   */
  public SammelUeberweisungList getListe() throws RemoteException
  {
    if (table != null)
      return table;

    table = new de.willuhn.jameica.hbci.gui.parts.SammelUeberweisungList(new SammelUeberweisungNew());
    return table;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSammelTransferControl#getBuchungen()
   */
  public TablePart getBuchungen() throws RemoteException
  {
    if (this.buchungen != null)
      return this.buchungen;
    
    Action a = new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new SammelUeberweisungBuchungNew().handleAction(context);
      }
    };
    
    this.buchungen = new SammelTransferBuchungList(getTransfer(),a);

    ContextMenu ctx = new ContextMenu();
    ctx.addItem(new CheckedSingleContextMenuItem(i18n.tr("Buchung öffnen"), new SammelUeberweisungBuchungNew(),"document-open.png"));
    ctx.addItem(new DeleteMenuItem());
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new CreateMenuItem(new SammelUeberweisungBuchungNew()));
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new CheckedContextMenuItem(i18n.tr("In Einzelüberweisung duplizieren"), new UeberweisungNew(),"ueberweisung.png"));
    this.buchungen.setContextMenu(ctx);
    return this.buchungen;
  }
}

/*****************************************************************************
 * $Log: SammelUeberweisungControl.java,v $
 * Revision 1.9  2011/08/10 12:47:28  willuhn
 * @N BUGZILLA 1118
 *
 * Revision 1.8  2011-04-11 16:48:33  willuhn
 * @N Drucken von Sammel- und Dauerauftraegen
 *
 * Revision 1.7  2010-12-13 11:01:08  willuhn
 * @B Wenn man einen Sammelauftrag in der Detailansicht loeschte, konnte man anschliessend noch doppelt auf die zugeordneten Buchungen klicken und eine ObjectNotFoundException ausloesen
 *
 * Revision 1.6  2009/11/26 12:00:21  willuhn
 * @N Buchungen aus Sammelauftraegen in Einzelauftraege duplizieren
 *
 * Revision 1.5  2009/05/08 13:58:30  willuhn
 * @N Icons in allen Menus und auf allen Buttons
 * @N Fuer Umsatz-Kategorien koennen nun benutzerdefinierte Farben vergeben werden
 *
 * Revision 1.4  2009/05/06 23:11:23  willuhn
 * @N Mehr Icons auf Buttons
*****************************************************************************/