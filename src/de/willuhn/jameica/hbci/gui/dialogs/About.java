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
import org.eclipse.swt.widgets.Label;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.DonateView;
import de.willuhn.jameica.hbci.rmi.Version;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * About-Dialog.
 */
public class About extends AbstractDialog
{

  /**
   * ct.
   * @param position
   */
  public About(int position)
  {
    super(position,true);

    AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
    final I18N i18n = plugin.getResources().getI18N();
    
    this.setTitle(i18n.tr("Über ..."));
    this.setPanelText(i18n.tr("Hibiscus {0}",plugin.getManifest().getVersion().toString()));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    AbstractPlugin plugin = Application.getPluginLoader().getPlugin(HBCI.class);
    final I18N i18n = plugin.getResources().getI18N();

    DBIterator list = Settings.getDBService().createList(Version.class);
    list.addFilter("name = ?","db");
    Version version = (Version) list.next();
    
    Label l = GUI.getStyleFactory().createLabel(parent,SWT.BORDER);
    l.setImage(SWTUtil.getImage("hibiscus-splash.png"));

    Container container = new LabelGroup(parent,i18n.tr("Versionsinformationen"),true);
    
    FormTextPart text = new FormTextPart();
    text.setText("<form>" +
      "<p><b>Hibiscus - HBCI-Onlinebanking für Jameica</b></p>" +
      "<p>Lizenz: GPL [<a href=\"http://www.gnu.org/copyleft/gpl.html\">www.gnu.org/copyleft/gpl.html</a>]<br/>" +
      "Copyright by Olaf Willuhn [<a href=\"mailto:hibiscus@willuhn.de\">hibiscus@willuhn.de</a>]<br/>" +
      "<a href=\"http://www.willuhn.de/products/hibiscus/\">www.willuhn.de/products/hibiscus/</a></p>" +
      "<p>Software-Version: " + plugin.getManifest().getVersion() + "<br/>" +
      "HBCI4Java-Version: " + HBCIUtils.version() + "<br/>" +
      "Datenbank-Version: " + version.getVersion() + "<br/>" +
      "Build: " + plugin.getManifest().getBuildnumber() + " [Datum " + plugin.getManifest().getBuildDate() + "]</p>" +
      "</form>");

    container.addPart(text);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Datenbank Infos"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          new DebugDialog(DebugDialog.POSITION_CENTER).open();
        }
        catch (OperationCanceledException oce)
        {
          Logger.info(oce.getMessage());
          return;
        }
        catch (Exception e)
        {
          Logger.error("unable to display debug dialog",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anzeigen der Datenbank-Informationen"), StatusBarMessage.TYPE_ERROR));
        }
      }
    },null,false,"dialog-information.png");
//    buttons.addButton(i18n.tr("Wallet"), new Action() {
//      public void handleAction(Object context) throws ApplicationException
//      {
//        try
//        {
//          new WalletDialog(DebugDialog.POSITION_CENTER).open();
//        }
//        catch (OperationCanceledException oce)
//        {
//          Logger.info(oce.getMessage());
//          return;
//        }
//        catch (Exception e)
//        {
//          Logger.error("unable to display wallet dialog",e);
//          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anzeigen des Wallet"), StatusBarMessage.TYPE_ERROR));
//        }
//      }
//    },null,false,"stock_keyring.png");
    buttons.addButton(i18n.tr("Spenden"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
        new DonateView().handleAction(null);
      }
    },null,false,"emblem-special.png");
    buttons.addButton(i18n.tr("Schließen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true,"window-close.png");
    
    container.addButtonArea(buttons);
    
    this.setSize(SWT.DEFAULT,600);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

}
