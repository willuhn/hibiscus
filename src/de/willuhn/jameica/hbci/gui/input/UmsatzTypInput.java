/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.UmsatzTypBean;
import de.willuhn.jameica.hbci.server.UmsatzTypUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Fertig konfigurierte Auswahlbox fuer die Umsatz-Kategorie.
 */
public class UmsatzTypInput extends SelectInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private boolean haveComments      = false;
  private boolean haveCustomComment = false;
  private boolean haveAutoComment   = false;

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
    this(preselected, typ, false);
  }

  /**
   * ct.
   * @param preselected der vorselectierte Umsatz-Typ.
   * @param typ Filter auf Kategorie-Typen.
   * Kategorien vom Typ "egal" werden grundsaetzlich angezeigt.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @param includeUnassignedType der fiktive Typ "nicht zugeordnet" soll mit angeboten werden
   * @throws RemoteException
   */
  public UmsatzTypInput(UmsatzTyp preselected, int typ, boolean includeUnassignedType) throws RemoteException
  {
    this(preselected,null,typ, includeUnassignedType);
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
   * @param unassigned der fiktive Typ "nicht zugeordnet" soll mit angeboten werden
   * @throws RemoteException
   */
  public UmsatzTypInput(UmsatzTyp preselected, UmsatzTyp skip, int typ, boolean unassigned) throws RemoteException
  {
    super((List) null, preselected != null ? new UmsatzTypBean(preselected) : null);
    List<Object> choices=new ArrayList<Object>(UmsatzTypUtil.getList(skip,typ));

    if (unassigned)
      choices.add(0,new UmsatzTypBean(UmsatzTypUtil.UNASSIGNED));

    this.setList(choices);
    this.setAttribute("indented");
    this.setName(i18n.tr("Umsatz-Kategorie"));
    this.setPleaseChoose(i18n.tr("<Keine Kategorie>"));

    // Checken, ob wir ueberhaupt irgendwelche Kategorien mit Kommentaren haben
    this.haveComments = Settings.getDBService().execute("select count(id) from umsatztyp where kommentar is not null and kommentar != ''", null, new ResultSetExtractor() {

      @Override
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        return (rs.next() && rs.getInt(1) > 0 ? Boolean.TRUE : null);
      }
    }) != null;

    refreshComment();

    // Kommentar aktualisieren
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
    this.haveCustomComment = StringUtils.trimToNull(comment) != null;
    this.haveAutoComment = this.haveComments && comment != null;

    if (!this.haveCustomComment && !this.haveAutoComment)
      return;

    super.setComment(comment);
  }

  /**
   * Aktualisiert den Kommentar.
   */
  private void refreshComment()
  {
    if (this.haveCustomComment)
      return;

    if (!this.haveAutoComment)
      return;

    try
    {
      UmsatzTyp ut = (UmsatzTyp) getValue();
      if (ut == null)
      {
        super.setComment("");
        return;
      }

      String comment = ut.getKommentar();
      if (StringUtils.trimToNull(comment) == null)
      {
        super.setComment("");
        return;
      }

      super.setComment(StringUtils.abbreviateMiddle(comment,"...",40));
    }
    catch (Exception e)
    {
      Logger.error("unable to refresh comment",e);
    }
  }
}
