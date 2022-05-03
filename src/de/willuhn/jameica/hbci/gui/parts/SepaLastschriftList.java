/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.parts.columns.AusgefuehrtColumn;
import de.willuhn.jameica.hbci.gui.parts.columns.KontoColumn;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste mit SEPA-Lastschriften.
 */
public class SepaLastschriftList extends AbstractTransferList
{

  /**
   * @param action
   * @throws RemoteException
   */
  public SepaLastschriftList(Action action) throws RemoteException
  {
    super(action);
    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.SepaLastschriftList());
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractTransferList#initColums()
   */
  protected void initColums()
  {
    addColumn(new KontoColumn());
    addColumn(i18n.tr("Gegenkonto Inhaber"),"empfaenger_name");
    addColumn(i18n.tr("Gegenkonto BIC"),"empfaenger_bic");
    addColumn(i18n.tr("Verwendungszweck"),"zweck");
    addColumn(i18n.tr("Betrag"),"betrag", new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
    addColumn(i18n.tr("Termin"),"termin", new DateFormatter(HBCI.DATEFORMAT),false,Column.ALIGN_RIGHT);
    addColumn(new AusgefuehrtColumn());
    addColumn(i18n.tr("Zieltermin"),"targetdate", new DateFormatter(HBCI.DATEFORMAT),false,Column.ALIGN_RIGHT);
    addColumn(i18n.tr("Art"),"sepatype");
    addColumn(i18n.tr("Sequenz"),"sequencetype");
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractTransferList#getObjectType()
   */
  protected Class getObjectType()
  {
    return SepaLastschrift.class;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.parts.AbstractTransferList#getList(java.lang.Object, java.util.Date, java.util.Date, java.lang.String)
   */
  protected DBIterator getList(Object konto, Date from, Date to, String text) throws RemoteException
  {
    DBIterator list = super.getList(konto, from, to, text);
    if (text != null && text.length() > 0)
    {
      String s = "%" + text.toLowerCase() + "%";
      list.addFilter("(LOWER(empfaenger_konto) like ? or LOWER(empfaenger_name) like ? or LOWER(zweck) like ?)", s, s, s);
    }
    
    return list;
  }
}
