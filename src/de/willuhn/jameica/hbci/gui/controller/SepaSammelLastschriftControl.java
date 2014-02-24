/*****************************************************************************
 * 
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 ****************************************************************************/

package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.SepaLastschriftNew;
import de.willuhn.jameica.hbci.gui.action.SepaSammelLastBuchungNew;
import de.willuhn.jameica.hbci.gui.action.SepaSammelLastschriftNew;
import de.willuhn.jameica.hbci.gui.parts.SepaSammelLastschriftList;
import de.willuhn.jameica.hbci.gui.parts.SepaSammelTransferBuchungList;
import de.willuhn.jameica.hbci.rmi.SepaLastSequenceType;
import de.willuhn.jameica.hbci.rmi.SepaLastType;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastschrift;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.DateUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Implementierung des Controllers fuer den Dialog "Liste der SEPA-Sammellastschriften".
 */
public class SepaSammelLastschriftControl extends AbstractSepaSammelTransferControl<SepaSammelLastschrift>
{
  private SepaSammelLastschrift transfer  = null;
  private SepaSammelLastschriftList table = null;
  private TablePart buchungen             = null;

  private SelectInput sequenceType        = null;
  private SelectInput type                = null;
  private DateInput targetDate            = null;


  /**
   * ct.
   * @param view
   */
  public SepaSammelLastschriftControl(AbstractView view)
  {
    super(view);
  }

  /**
   * Liefert das Eingabe-Feld fuer den Sequenztyp.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getSequenceType() throws RemoteException
  {
    if (this.sequenceType == null)
    {
      this.sequenceType = new SelectInput(SepaLastSequenceType.values(),getTransfer().getSequenceType());
      this.sequenceType.setName(i18n.tr("Sequenz-Typ"));
      this.sequenceType.setEnabled(!getTransfer().ausgefuehrt());
      this.sequenceType.setMandatory(true);
    }
    return this.sequenceType;
  }

  /**
   * Liefert das Eingabe-Feld fuer den Typ.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getType() throws RemoteException
  {
    if (this.type == null)
    {
      this.type = new SelectInput(SepaLastType.values(),getTransfer().getType());
      this.type.setName(i18n.tr("Lastschrift-Art"));
      this.type.setEnabled(!getTransfer().ausgefuehrt());
    }
    return this.type;
  }
  
  /**
   * Liefert das Eingabe-Feld fuer das Ausfuehrungsdatum.
   * @return Eingabe-Feld.
   * @throws RemoteException
   */
  public Input getTargetDate() throws RemoteException
  {
    if (this.targetDate == null)
    {
      this.targetDate = new DateInput(getTransfer().getTargetDate(),DateUtil.DEFAULT_FORMAT);
      this.targetDate.setName(i18n.tr("Fälligkeitsdatum"));
      this.targetDate.setEnabled(!getTransfer().ausgefuehrt());
    }
    return this.targetDate;
  }
  
  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferControl#handleStore()
   */
  public synchronized boolean handleStore()
  {
    try
    {
      SepaSammelLastschrift t = this.getTransfer();
      if (t.ausgefuehrt())
        return true;
      
      t.setSequenceType((SepaLastSequenceType)getSequenceType().getValue());
      t.setType((SepaLastType)getType().getValue());
      t.setTargetDate((Date) getTargetDate().getValue());
      
      this.store();
      
      return true;
    }
    catch (Exception e)
    {
      if (e instanceof ApplicationException)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(),StatusBarMessage.TYPE_ERROR));
      }
      else
      {
        Logger.error("error while saving order",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehlgeschlagen: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
      }
    }
    return false;

  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferControl#getTransfer()
   */
  public SepaSammelLastschrift getTransfer() throws RemoteException
  {
    if (transfer != null)
      return transfer;

    transfer = (SepaSammelLastschrift) getCurrentObject();
    if (transfer != null)
      return transfer;

    transfer = (SepaSammelLastschrift) Settings.getDBService().createObject(SepaSammelLastschrift.class,null);
    return transfer;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.controller.AbstractSepaSammelTransferControl#getListe()
   */
  public SepaSammelLastschriftList getListe() throws RemoteException
  {
    if (table != null)
      return table;

    table = new de.willuhn.jameica.hbci.gui.parts.SepaSammelLastschriftList(new SepaSammelLastschriftNew());
    return table;
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
        new SepaSammelLastBuchungNew().handleAction(context);
      }
    };
    
    this.buchungen = new SepaSammelTransferBuchungList(getTransfer(),a);

    ContextMenu ctx = new ContextMenu();
    ctx.addItem(new CheckedSingleContextMenuItem(i18n.tr("Buchung öffnen"), new SepaSammelLastBuchungNew(),"document-open.png"));
    ctx.addItem(new DeleteMenuItem());
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new CreateMenuItem(new SepaSammelLastBuchungNew()));
    ctx.addItem(ContextMenuItem.SEPARATOR);
    ctx.addItem(new CheckedContextMenuItem(i18n.tr("In Einzellastschrift duplizieren"), new SepaLastschriftNew(),"stock_previous.png"));
    this.buchungen.setContextMenu(ctx);
    return this.buchungen;
  }
}
