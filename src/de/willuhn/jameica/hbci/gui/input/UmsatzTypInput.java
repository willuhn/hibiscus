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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.input.ButtonInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.UmsatzTypListDialog;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.UmsatzTypBean;
import de.willuhn.jameica.hbci.server.UmsatzTypUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Fertig konfigurierte Auswahlbox fuer die Umsatz-Kategorie.
 */
public class UmsatzTypInput extends SelectInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final static String DATAKEY_RESIZE_LISTENER = "jameica.hbci.umsatztypinput.path.listener";

  private boolean haveComments      = false;
  private boolean haveCustomComment = false;
  private boolean haveAutoComment   = false;
  private boolean showPathName      = false;
  private Combo combo               = null;
  private int pathDisplayWidth      = -1;
  private boolean refreshingPath    = false;

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
  
  @Override
  public void setPreselected(Object preselected)
  {
    if(preselected instanceof UmsatzTyp)
      preselected = new UmsatzTypBean((UmsatzTyp) preselected);

    super.setPreselected(preselected);
  }

  /**
   * Aktiviert optional die Anzeige des vollstaendigen Pfads der Kategorie.
   * @param showPathName true, wenn statt eingerueckter Bezeichnung der volle Pfad angezeigt werden soll.
   */
  public void setShowPathName(boolean showPathName)
  {
    this.showPathName = showPathName;
    this.refreshPathDisplay(); // Anzeige bei bereits gezeichneter Combo aktualisieren
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
   * @see de.willuhn.jameica.gui.input.SelectInput#format(java.lang.Object)
   */
  @Override
  protected String format(Object bean)
  {
    if (!this.showPathName || !(bean instanceof UmsatzTypBean))
      return super.format(bean);

    try
    {
      String path = ((UmsatzTypBean) bean).getPathName();
      String s = StringUtils.trimToNull(path);
      if (s != null)
        return this.abbreviateLeadingToFit(s);
    }
    catch (Exception e)
    {
      Logger.error("unable to format category path",e);
    }

    return super.format(bean);
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

  /**
   * @see de.willuhn.jameica.gui.input.SelectInput#getControl()
   */
  @Override
  public Control getControl()
  {
    Control control = super.getControl();
    if (!(control instanceof Combo))
      return control;

    this.combo = (Combo) control;
    if (this.combo.getData(DATAKEY_RESIZE_LISTENER) == null)
    {
      this.combo.setData(DATAKEY_RESIZE_LISTENER,Boolean.TRUE);
      this.combo.addControlListener(new ControlAdapter() {
        @Override
        public void controlResized(ControlEvent e)
        {
          refreshPathDisplay();
        }
      });
      this.combo.addListener(SWT.Selection,e -> updateTooltip());
    }
    this.refreshPathDisplay();
    this.updateTooltip();
    return control;
  }

  /**
   * Aktualisiert die Pfad-Darstellung in Abhaengigkeit von der aktuellen Feldbreite.
   */
  private void refreshPathDisplay()
  {
    if (!this.showPathName || this.refreshingPath)
      return;

    if (this.combo == null || this.combo.isDisposed())
      return;

    // Platz fuer den Pfeil der Combo plus etwas Innenabstand freilassen,
    // damit rechts nichts unter dem Dropdown-Symbol abgeschnitten wird.
    int arrowWidth = this.combo.getItemHeight();
    int textPadding = 24;
    int width = Math.max(0,this.combo.getClientArea().width - arrowWidth - textPadding);
    if (width <= 0 || width == this.pathDisplayWidth)
      return;

    Object selectedBean = super.getValue();
    try
    {
      this.refreshingPath = true;
      this.pathDisplayWidth = width;
      this.setList(this.getList());
      super.setPreselected(selectedBean);
      this.updateTooltip();
    }
    finally
    {
      this.refreshingPath = false;
    }
  }

  /**
   * Setzt den vollstaendigen Pfad als Tooltip, wenn die Pfad-Anzeige aktiv ist.
   */
  private void updateTooltip()
  {
    if (this.combo == null || this.combo.isDisposed())
      return;

    if (!this.showPathName)
    {
      this.combo.setToolTipText(null);
      return;
    }

    try
    {
      Object selected = super.getValue();
      if (!(selected instanceof UmsatzTypBean))
      {
        this.combo.setToolTipText(null);
        return;
      }

      String path = ((UmsatzTypBean) selected).getPathName();
      this.combo.setToolTipText(StringUtils.trimToNull(path));
    }
    catch (Exception e)
    {
      Logger.error("unable to update category tooltip",e);
    }
  }

  /**
   * Kuerzt den String linksbuendig so, dass das Ende (Basename) sichtbar bleibt.
   * @param value der zu kuerzende Text.
   * @return gekuerzter Text.
   */
  private String abbreviateLeadingToFit(String value)
  {
    if (!this.showPathName)
      return value;

    if (this.combo == null || this.combo.isDisposed() || this.pathDisplayWidth <= 0)
      return value;

    GC gc = new GC(this.combo);
    try
    {
      Point full = gc.textExtent(value);
      if (full.x <= this.pathDisplayWidth)
        return value;

      final String prefix = "...";
      int prefixWidth = gc.textExtent(prefix).x;
      if (prefixWidth >= this.pathDisplayWidth)
        return prefix;

      int left = 0;
      int right = value.length();
      while (left < right)
      {
        int mid = (left + right) / 2;
        String candidate = prefix + value.substring(mid);
        if (gc.textExtent(candidate).x <= this.pathDisplayWidth)
          right = mid;
        else
          left = mid + 1;
      }

      return prefix + value.substring(Math.min(left,value.length()));
    }
    finally
    {
      gc.dispose();
    }
  }

  /**
   * Liefert ein Eingabefeld bestehend aus Selectbox und zusaetzlichem Dialog-Button.
   * @param position Position des Dialogs.
   * @param typ Filter auf Kategorie-Typen fuer den Dialog.
   * @return kombiniertes Eingabefeld.
   */
  public Input getSelectionWithDialogButton(int position, int typ)
  {
    return new UmsatzTypDialogButtonInput(position,typ);
  }

  /**
   * Loest ein Selection-Event auf der Selectbox aus.
   */
  private void fireSelection()
  {
    try
    {
      if (getParent() == null || getParent().isDisposed())
        return;
      Control control = getControl();
      if (control == null || control.isDisposed())
        return;
      control.notifyListeners(SWT.Selection,new Event());
    }
    catch (Exception e)
    {
      Logger.error("unable to fire selection event",e);
    }
  }

  /**
   * Wrapper aus Auswahlfeld plus "..."-Button zum Starten des Kategorie-Dialogs.
   */
  private class UmsatzTypDialogButtonInput extends ButtonInput
  {
    private final int position;
    private final int typ;

    private UmsatzTypDialogButtonInput(int position, int typ)
    {
      this.position = position;
      this.typ = typ;
      this.setButtonText("...");
      this.addButtonListener(new Listener()
      {
        public void handleEvent(Event event)
        {
          try
          {
            UmsatzTyp current = (UmsatzTyp) UmsatzTypInput.this.getValue();
            if (current == UmsatzTypUtil.UNASSIGNED)
              current = null;

            UmsatzTypListDialog d = new UmsatzTypListDialog(UmsatzTypDialogButtonInput.this.position,current,UmsatzTypDialogButtonInput.this.typ);
            UmsatzTyp selected = (UmsatzTyp) d.open();
            UmsatzTypInput.this.setValue(selected);
            refreshComment();
            fireSelection();
          }
          catch (OperationCanceledException oce)
          {
            Logger.debug("operation cancelled");
          }
          catch (Exception e)
          {
            Logger.error("unable to choose category",e);
          }
        }
      });
    }

    /**
     * @see de.willuhn.jameica.gui.input.ButtonInput#getClientControl(org.eclipse.swt.widgets.Composite)
     */
    public Control getClientControl(Composite parent)
    {
      UmsatzTypInput.this.paint(parent);
      return UmsatzTypInput.this.getParent();
    }

    /**
     * @see de.willuhn.jameica.gui.input.Input#getValue()
     */
    public Object getValue()
    {
      return UmsatzTypInput.this.getValue();
    }

    /**
     * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
     */
    public void setValue(Object value)
    {
      UmsatzTypInput.this.setValue(value);
    }
  }
}
