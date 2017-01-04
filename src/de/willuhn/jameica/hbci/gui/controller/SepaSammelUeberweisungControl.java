/*****************************************************************************
 * 
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 ****************************************************************************/

package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungNew;
import de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungBuchungNew;
import de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungNew;
import de.willuhn.jameica.hbci.gui.parts.SepaSammelTransferBuchungList;
import de.willuhn.jameica.hbci.gui.parts.SepaSammelUeberweisungList;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJob;
import de.willuhn.jameica.hbci.synchronize.jobs.SynchronizeJobSepaSammelUeberweisung;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung des Controllers fuer den Dialog "Liste der SEPA-Sammelueberweisungen".
 */
public class SepaSammelUeberweisungControl extends AbstractSepaSammelTransferControl<SepaSammelUeberweisung>
{
  private SepaSammelUeberweisung transfer  = null;
  private SepaSammelUeberweisungList table = null;
  private TablePart buchungen              = null;

  private Input name                       = null;


  /**
   * ct.
   * @param view
   */
  public SepaSammelUeberweisungControl(AbstractView view)
  {
    super(view);
  }

  /**
   * Ueberschrieben, um einen Namensvorschlag anzuzeigen.
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferControl#getName()
   */
  public Input getName() throws RemoteException
  {
    if (this.name != null)
      return this.name;
    
    this.name = super.getName();
    if (StringUtils.trimToNull((String)this.name.getValue()) == null)
      this.name.setValue(i18n.tr("SEPA-Sammelüberweisung vom {0}",HBCI.LONGDATEFORMAT.format(new Date())));
    return this.name;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferControl#getTransfer()
   */
  public SepaSammelUeberweisung getTransfer() throws RemoteException
  {
    if (transfer != null)
      return transfer;

    transfer = (SepaSammelUeberweisung) getCurrentObject();
    if (transfer != null)
      return transfer;

    transfer = (SepaSammelUeberweisung) Settings.getDBService().createObject(SepaSammelUeberweisung.class,null);
    return transfer;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferControl#getListe()
   */
  public SepaSammelUeberweisungList getListe() throws RemoteException
  {
    if (table != null)
      return table;

    table = new SepaSammelUeberweisungList(new SepaSammelUeberweisungNew());
    return table;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferControl#getSynchronizeJobType()
   */
  @Override
  public Class<? extends SynchronizeJob> getSynchronizeJobType()
  {
    return SynchronizeJobSepaSammelUeberweisung.class;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferControl#getBuchungen()
   */
  public TablePart getBuchungen() throws RemoteException
  {
    if (this.buchungen != null)
      return this.buchungen;
    
    Action a = new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new SepaSammelUeberweisungBuchungNew().handleAction(context);
      }
    };
    
    this.buchungen = new SepaSammelTransferBuchungList(getTransfer(),a);
    this.buchungen.setMulti(true);

    ContextMenu ctx = new ContextMenu();
    ctx.addItem(new CheckedSingleContextMenuItem(i18n.tr("Buchung öffnen"), new SepaSammelUeberweisungBuchungNew(),"document-open.png"));
    ctx.addItem(new DeleteMenuItem());
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new CreateMenuItem(new SepaSammelUeberweisungBuchungNew()));
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new CheckedSingleContextMenuItem(i18n.tr("In Einzelüberweisung duplizieren"), new AuslandsUeberweisungNew(),"stock_next.png"));
    this.buchungen.setContextMenu(ctx);
    return this.buchungen;
  }
}
