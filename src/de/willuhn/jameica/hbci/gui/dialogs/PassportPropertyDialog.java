/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/PassportPropertyDialog.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/05/16 09:55:29 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.passport.AbstractHBCIPassport;
import org.kapott.hbci.passport.HBCIPassport;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
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

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.passport;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container container = new SimpleContainer(parent);
    container.addText(i18n.tr("Bank-Parameter (BPD) und User-Parameter (UPD) dieses Sicherheitsmediums"),true);
    final PassportPropertyList table = new PassportPropertyList(this.passport);
    table.paint(parent);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("BPD löschen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        String s = i18n.tr("Die BPD (Bank-Parameter-Daten) werden beim nächsten Verbindungsaufbau \n" +
        		               "mit der Bank automatisch erneut abgerufen.\n\n" +
        		               "BPD jetzt löschen?");
        try
        {
          if (!Application.getCallback().askUser(s))
            return;
          
          passport.clearBPD();
          
          // Das triggert beim naechsten Verbindungsaufbau
          // HBCIHandler.<clinit>
          // -> HBCIHandler.registerUser()
          // -> HBCIUser.register()
          // -> HBCIUser.updateUserData()
          // -> HBCIUser.fetchSysId() - und das holt die BPD beim naechsten mal ueber einen nicht-anonymen Dialog
          ((AbstractHBCIPassport)passport).syncSysId();
          
          passport.saveChanges();
          table.clearBPD();
          
          // Noch aus der Tabelle loeschen
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("BPD gelöscht"),StatusBarMessage.TYPE_SUCCESS));
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
    buttons.addButton(i18n.tr("Schließen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true,"window-close.png");
    buttons.paint(parent);
  }

}


/**********************************************************************
 * $Log: PassportPropertyDialog.java,v $
 * Revision 1.4  2011/05/16 09:55:29  willuhn
 * @N Funktion zum Loeschen der BPD
 *
 * Revision 1.3  2011-05-06 12:35:24  willuhn
 * @R Nicht mehr noetig - macht AbstractDialog jetzt selbst
 *
 * Revision 1.2  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.1  2009/06/16 15:32:30  willuhn
 * *** empty log message ***
 *
 **********************************************************************/
