/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;
import java.util.List;

import de.willuhn.datasource.rmi.Changeable;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.SepaSammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransfer;
import de.willuhn.jameica.hbci.rmi.SepaSammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisungBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Auflösen einer Sepa-Sammelbuchung in Einzelbuchungen.
 */
public class SepaSammelTransferSplit implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if(!(context instanceof SepaSammelTransfer)){
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen Sammler aus"));
    }
    try
    {
      SepaSammelTransfer sammler = (SepaSammelTransfer)context;
      List<SepaSammelTransferBuchung> buchungen = sammler.getBuchungen();
      for (SepaSammelTransferBuchung sammelTransferBuchung : buchungen)
      {
        if(!(sammelTransferBuchung instanceof SepaSammelLastBuchung) &&!(sammelTransferBuchung instanceof SepaSammelUeberweisungBuchung)){
          Logger.error("unexpected booking type "+ sammelTransferBuchung.getClass().getName());
          throw new ApplicationException(i18n.tr("Sammler enthält unerwarteten Vorgangstyp"));
        }
      }
      for (SepaSammelTransferBuchung sammelTransferBuchung : buchungen)
      {
        Changeable clone=null;
        if(sammelTransferBuchung instanceof SepaSammelLastBuchung){
          clone = SepaLastschriftNew.cloneFromSammelLastBuchung((SepaSammelLastBuchung)sammelTransferBuchung);
        }else if(sammelTransferBuchung instanceof SepaSammelUeberweisungBuchung){
          clone = AuslandsUeberweisungNew.cloneFromSammelUeberweisungBuchung((SepaSammelUeberweisungBuchung)sammelTransferBuchung);
        }
        clone.store();
      }
      sammler.delete();
    } catch (RemoteException e)
    {
      Logger.error("Error while splitting", e);
    }
  }
}