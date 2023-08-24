/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.List;

import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungBuchungNew;
import de.willuhn.jameica.hbci.gui.parts.columns.AusgefuehrtColumn;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisungBuchung;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Überschrieben, um im Adressbuch noch ein paar Eckdaten des Auftrages mit anzeigen zu können.
 */
public class SimpleSepaSammelUeberweisungBuchungList extends SepaSammelTransferBuchungList
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * ct.
   * @param list die Liste der Buchungen.
   * @throws RemoteException
   */
  public SimpleSepaSammelUeberweisungBuchungList(List<SepaSammelUeberweisungBuchung> list) throws RemoteException
  {
    super(list,new SepaSammelUeberweisungBuchungNew());
    this.addColumn(i18n.tr("Termin"),"sepasueb_id.termin", new DateFormatter(HBCI.DATEFORMAT),false,Column.ALIGN_RIGHT);
    this.addColumn(new AusgefuehrtColumn("sepasueb_id.ausgefuehrt"));
    this.setRememberColWidths(true);
    this.setRememberColWidths(true);
    this.setRememberState(true);
  }

}


