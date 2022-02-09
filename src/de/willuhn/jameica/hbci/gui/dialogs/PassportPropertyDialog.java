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

import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.PassportDeleteBPD;
import de.willuhn.jameica.hbci.gui.parts.PassportPropertyList;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog, der die UPD/BPD eines Passports anzeigt.
 */
public class PassportPropertyDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private HBCIPassport passport = null;

  /**
   * ct.
   * @param position
   * @param passport der Passport.
   */
  public PassportPropertyDialog(int position, HBCIPassport passport)
  {
    super(position);
    this.setTitle(i18n.tr("BPD/UPD"));
    this.setSize(560,400);
    this.passport = passport;
  }

  @Override
  protected Object getData() throws Exception
  {
    return this.passport;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent);
    container.addText(i18n.tr("Bank-Parameter (BPD) und User-Parameter (UPD) dieses Sicherheitsmediums"),true);
    final PassportPropertyList table = new PassportPropertyList(this.passport);
    table.paint(parent);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("BPD l�schen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        String s = i18n.tr("Die BPD (Bank-Parameter-Daten) werden beim n�chsten Verbindungsaufbau \n" +
        		               "mit der Bank automatisch (oder durch Klick auf \"Konfiguration testen\") erneut abgerufen.\n\n" +
        		               "Hinweis: Bei Verwendung einer Chipkarte m�ssen Sie gleich die PIN eingeben.\n\n" +
        		               "BPD jetzt l�schen?");
        try
        {
          if (!Application.getCallback().askUser(s))
            return;
          
          new PassportDeleteBPD().handleAction(passport);

          // Aus der Tabelle in der Anzeige loeschen
          table.clearBPD();
          
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("BPD gel�scht"),StatusBarMessage.TYPE_SUCCESS));
        }
        catch (OperationCanceledException oce)
        {
          Logger.info(oce.getMessage());
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (Exception e)
        {
          Logger.error("unable to delete bpd",e);
        }
      }
    },null,false,"user-trash-full.png");
    buttons.addButton(i18n.tr("Schlie�en"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true,"window-close.png");
    buttons.paint(parent);
  }

}
