/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.UmsatzTypBean;
import de.willuhn.jameica.hbci.server.UmsatzTypUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Fertig konfigurierte Auswahlbox fuer die Umsatz-Kategorie.
 */
public class UmsatzTypInput extends SelectInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private boolean haveComment = false;

  /**
   * ct.
   * @param preselected der vorselectierte Umsatz-Typ.
   * @param typ Filter auf Kategorie-Typen.
   * Kategorien vom Typ "egal" werden grundsaetzlich angezeigt.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @throws RemoteException
   */
  public UmsatzTypInput(UmsatzTyp preselected, int typ) throws RemoteException
  {
    this(preselected,null,typ);
  }

  /**
   * ct.
   * @param preselected der vorselectierte Umsatz-Typ.
   * @param skip einzelner Umsatz-Typ, der nicht angeboten werden soll.
   * Damit ist es zum Beispiel moeglich, eine Endlos-Rekursion zu erzeugen,
   * wenn ein Parent ausgewaehlt werden soll, der User aber die Kategorie
   * sich selbst als Parent zuordnet.
   * @param typ Filter auf Kategorie-Typen.
   * Kategorien vom Typ "egal" werden grundsaetzlich angezeigt.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @throws RemoteException
   */
  public UmsatzTypInput(UmsatzTyp preselected, UmsatzTyp skip, int typ) throws RemoteException
  {
    super((List) null, preselected != null ? new UmsatzTypBean(preselected) : null);
    this.setList(UmsatzTypUtil.getList(skip,typ));
    this.setAttribute("indented");
    this.setName(i18n.tr("Umsatz-Kategorie"));
    this.setPleaseChoose(i18n.tr("<Keine Kategorie>"));
    refreshComment();
    
    // Betrag aktualisieren
    this.addListener(new Listener() {
    
      public void handleEvent(Event event)
      {
        refreshComment();
      }
    
    });
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.SelectInput#getValue()
   */
  @Override
  public Object getValue()
  {
    UmsatzTypBean b = (UmsatzTypBean) super.getValue();
    return b != null ? b.getTyp() : null;
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#setComment(java.lang.String)
   */
  @Override
  public void setComment(String comment)
  {
    super.setComment(comment);
    this.haveComment = StringUtils.trimToNull(comment) != null;
  }
  
  /**
   * Aktualisiert den Kommentar.
   */
  private void refreshComment()
  {
    // Wir wuerden sonst u.U haufenweise Umsaetze und Kategorien laden, um die Summen
    // zu ermitteln. Und am Ende wird der Kommentar gar nicht angezeigt.
    if (!this.haveComment)
      return;
    
    try
    {
      UmsatzTyp ut = (UmsatzTyp) getValue();
      if (ut == null)
      {
        setComment("");
        return;
      }
      
      Calendar cal = Calendar.getInstance();
      setComment(i18n.tr("Umsatz im laufenden Monat: {0} {1}", new String[]{HBCI.DECIMALFORMAT.format(ut.getUmsatz(cal.get(Calendar.DAY_OF_MONTH))), HBCIProperties.CURRENCY_DEFAULT_DE}));
    }
    catch (Exception e)
    {
      Logger.error("unable to refresh umsatz",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren des Umsatzes"), StatusBarMessage.TYPE_ERROR));
    }
  }
}
