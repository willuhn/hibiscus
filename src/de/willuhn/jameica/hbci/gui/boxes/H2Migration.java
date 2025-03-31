/**********************************************************************
 *
 * Copyright (c) 2025 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import java.rmi.RemoteException;

import javax.annotation.Resource;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.hbci.H2MigrationTask;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.server.DBSupportH2Impl;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Wizard fuer die H2-Migration.
 */
public class H2Migration extends AbstractBox
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  @Resource private DBSupportH2Impl driver;
  @Resource private H2MigrationTask task;
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#isActive()
   */
  public boolean isActive()
  {
    return super.isActive() && isEnabled();
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return true;
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 0;
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Datenbank-Migration auf neue H2-Version");
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#isEnabled()
   */
  public boolean isEnabled()
  {
    return super.isEnabled() && this.driver.canMigrate();
  }
  
  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    final InfoPanel panel = new InfoPanel();
    panel.setTitle(i18n.tr("Datenbank-Migration auf neue H2-Version"));
    panel.setIcon("dialog-information-large.png");
    panel.setText(i18n.tr("Ihre Hibiscus-Installation verwendet eine veraltete Datenbank-Version. Klicken Sie auf \"Datenbank jetzt migrieren\", um die Datenbank auf das neue Format umzustellen."));
    panel.addButton(new Button(i18n.tr("Datenbank jetzt migrieren"),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        Logger.warn("starting database migration from mckoi to h2");
        Application.getController().start(task);
      }
    },null,false,"go-next.png"));
    panel.paint(parent);

  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#getHeight()
   */
  public int getHeight()
  {
    return 150;
  }

}
