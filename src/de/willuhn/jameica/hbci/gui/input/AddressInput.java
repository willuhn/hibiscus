/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/AddressInput.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/01/04 01:25:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.input.SearchInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.AddressbookService;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Autosuggest-Feld zur Eingabe/Auswahl einer Adresse.
 */
public class AddressInput extends SearchInput
{
  private I18N i18n = null;

  /**
   * ct.
   * @param name Anzuzeigender Name.
   */
  public AddressInput(String name)
  {
    super();
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.setValue(name);
  }

  /**
   * @see de.willuhn.jameica.gui.input.SearchInput#format(java.lang.Object)
   */
  protected String format(Object bean)
  {
    if (bean == null)
      return null;
    
    if (!(bean instanceof Address))
      return bean.toString();
    
    try
    {
      Address a = (Address) bean;
      StringBuffer sb = new StringBuffer(a.getName());
      
      String blz = a.getBlz();
      if (blz != null && blz.length() > 0)
      {
        sb.append(" - ");
        String bankName = HBCIUtils.getNameForBLZ(blz);
        if (bankName != null && bankName.length() > 0)
        {
          sb.append(bankName);
        }
        else
        {
          sb.append("BLZ ");
          sb.append(blz);
        }
      }
      String comment = a.getKommentar();
      if (comment != null && comment.length() > 0)
      {
        sb.append(" (");
        sb.append(comment);
        sb.append(")");
      }
      return sb.toString();
    }
    catch (RemoteException re)
    {
      Logger.error("unable to format address",re);
      return null;
    }
  }


  /**
   * @see de.willuhn.jameica.gui.input.SearchInput#startSearch(java.lang.String)
   */
  public List startSearch(String text)
  {
    try
    {
      AddressbookService service = (AddressbookService) Application.getServiceFactory().lookup(HBCI.class,"addressbook");
      return service.findAddresses(text);
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(), StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("error while searching in address book",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Suchen im Adressbuch: {0}",e.getMessage()), StatusBarMessage.TYPE_ERROR));
    }
    return new ArrayList();
  }

}


/**********************************************************************
 * $Log: AddressInput.java,v $
 * Revision 1.1  2009/01/04 01:25:47  willuhn
 * @N Checksumme von Umsaetzen wird nun generell beim Anlegen des Datensatzes gespeichert. Damit koennen Umsaetze nun problemlos geaendert werden, ohne mit "hasChangedByUser" checken zu muessen. Die Checksumme bleibt immer erhalten, weil sie in UmsatzImpl#insert() sofort zu Beginn angelegt wird
 * @N Umsaetze sind nun vollstaendig editierbar
 *
 **********************************************************************/
