/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
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
  private final static int WINDOW_WIDTH = 550;
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct
   * @param position
   */
  public DebugDialog(int position)
  {
    super(position);
    this.setTitle(i18n.tr("Datenbank-Informationen"));
    setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }

  @Override
  protected Object getData() throws Exception
  {
    return null;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    HBCIDBService service = (HBCIDBService) Application.getServiceFactory().lookup(HBCI.class,"database");
    DBSupport driver      = service.getDriver();

    StringBuffer sb = new StringBuffer();
    sb.append(i18n.tr("JDBC-Treiber: {0}\n",driver.getJdbcDriver()));
    sb.append(i18n.tr("JDBC-URL: {0}\n",driver.getJdbcUrl()));
    sb.append(i18n.tr("JDBC-Username: {0}\n",driver.getJdbcUsername()));
    sb.append(i18n.tr("JDBC-Passwort: {0}\n",driver.getJdbcPassword()));
    
    Container container = new SimpleContainer(parent);
    container.addHeadline(i18n.tr("Datenbank-Einstellungen"));
    TextAreaInput text = new TextAreaInput(sb.toString());
    container.addPart(text);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Schlie�en"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true,"window-close.png");
    container.addButtonArea(buttons);
    
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }

}


/*********************************************************************
 * $Log: DebugDialog.java,v $
 * Revision 1.2  2011/03/30 08:19:37  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.1  2008/05/06 10:10:56  willuhn
 * @N Diagnose-Dialog, mit dem man die JDBC-Verbindungsdaten (u.a. auch das JDBC-Passwort) ausgeben kann
 *
 **********************************************************************/