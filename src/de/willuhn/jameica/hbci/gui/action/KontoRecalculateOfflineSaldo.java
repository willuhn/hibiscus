package de.willuhn.jameica.hbci.gui.action;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zur Neubestimmung der Buchungssalden eines Offline-Kontos
 */
public class KontoRecalculateOfflineSaldo implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Objekt vom Typ <code>Konto</code> im Context.
   * 
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {

    if (context == null || !(context instanceof Konto))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus"));

    try
    {
      Konto k = (Konto) context;
      if (k.isNewObject() || !k.hasFlag(Konto.FLAG_OFFLINE))
        return;

      String q = i18n.tr("SaldenNeuBerechnenBestaetigung");

      if (!Application.getCallback().askUser(q))
        return;

      Umsatz previousIterator=null;
      List<Umsatz> umsaetze=new ArrayList<Umsatz>();
      DBIterator it = k.getUmsaetze();
      double currentSaldo=0d;
      while (it.hasNext()){
        Umsatz um = (Umsatz)it.next();
        checkOrder(previousIterator, um);
        if(um.hasFlag(Umsatz.FLAG_CHECKED)){
          currentSaldo=um.getSaldo();
          break;
        }
        umsaetze.add(um);
        previousIterator=um;
      }

      Collections.reverse(umsaetze);

      for (Umsatz umsatz : umsaetze)
      {
        currentSaldo+=umsatz.getBetrag();
        umsatz.setSaldo(currentSaldo);
        umsatz.store();
      }
      k.setSaldo(currentSaldo);
      k.store();
      Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(k));
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Salden neu berechnet."), StatusBarMessage.TYPE_SUCCESS));
    }
    catch (Exception e)
    {
      Logger.error("error while recalculating balances", e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Berechnen der Salden."), StatusBarMessage.TYPE_ERROR));
      return;
    }
  }

  private void checkOrder(Umsatz previousIterator, Umsatz current) throws RemoteException{
    if(previousIterator!=null){
      boolean orderOK=true;
      if(current.getDatum().after(previousIterator.getDatum())){
        orderOK=false;
      }else if(current.getDatum().equals(previousIterator.getDatum())){
        if(Long.parseLong(current.getID())>Long.parseLong(previousIterator.getID())){
          orderOK=false;
        }
      }
      if(!orderOK){
        throw new IllegalStateException("unexpected order of bookings; cancel recalculating balance");
      }
    }
  }

}