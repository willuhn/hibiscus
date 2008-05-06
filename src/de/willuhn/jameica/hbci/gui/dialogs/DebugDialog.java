/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/DebugDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/05/06 10:10:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.DBSupport;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, der Debug-Informationen - unter anderem ueber die Datenbank anzeigt.
 */
public class DebugDialog extends AbstractDialog
{
  private I18N i18n = null;

  /**
   * ct
   * @param position
   */
  public DebugDialog(int position)
  {
    super(position);
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    this.setTitle(i18n.tr("Diagnose-Informationen"));
    setSize(560,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    StringBuffer sb = new StringBuffer();
    HBCIDBService service = (HBCIDBService) Application.getServiceFactory().lookup(HBCI.class,"database");
    DBSupport driver      = service.getDriver();

    Container container = new SimpleContainer(parent);
    
    sb.append(i18n.tr("JDBC-Treiber: \"{0}\"\n",driver.getJdbcDriver()));
    sb.append(i18n.tr("JDBC-URL: \"{0}\"\n",driver.getJdbcUrl()));
    sb.append(i18n.tr("JDBC-Username: \"{0}\"\n",driver.getJdbcUsername()));
    sb.append(i18n.tr("JDBC-Passwort: \"{0}\"\n",driver.getJdbcPassword()));
    
    TextAreaInput text = new TextAreaInput(sb.toString());
    
    container.addLabelPair("",text);
    
    ButtonArea buttons = container.createButtonArea(1);
    buttons.addButton("   " + i18n.tr("Schlieﬂen") + "   ",new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true);
  }

}


/*********************************************************************
 * $Log: DebugDialog.java,v $
 * Revision 1.1  2008/05/06 10:10:56  willuhn
 * @N Diagnose-Dialog, mit dem man die JDBC-Verbindungsdaten (u.a. auch das JDBC-Passwort) ausgeben kann
 *
 **********************************************************************/