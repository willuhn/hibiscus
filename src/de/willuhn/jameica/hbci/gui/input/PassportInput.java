/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/PassportInput.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/04/29 11:38:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.SynchronizeBackend;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Eine Auswahl-Box fuer das Sicherheitsmedium.
 */
public class PassportInput extends SelectInput
{
  private final static I18N i18n          = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private static List<Passport> passports = null;
  
  /**
   * ct.
   * @throws RemoteException
   */
  public PassportInput() throws RemoteException
  {
    this(null);
  }

  /**
   * ct.
   * @param konto das Konto.
   * @throws RemoteException
   */
  public PassportInput(Konto konto) throws RemoteException
  {
    super(init(),null);

    List<Passport> list = init();
    String currentClass = (konto != null ? konto.getPassportClass() : null);
    for (Passport p:list)
    {
      if (currentClass != null && p.getClass().getName().equals(currentClass))
      {
        setPreselected(p);
        break;
      }
    }
    
    this.setPleaseChoose("Bitte wählen...");
    this.setAttribute("name");
    this.setName(i18n.tr("Verfahren"));
  }
  
  /**
   * Aktualisiert die Liste der verfuegbaren Passports abhaengig vom ausgewaehlten Backend.
   * Derzeit werden hier erstmal hart alle HBCI-Passports entfernt, wenn nicht das HBCI-Backend ausgewaehlt ist.
   * @param backend das Backend.
   */
  public void update(SynchronizeBackend backend)
  {
    List<Passport> all = init();
    List<Passport> result = new ArrayList<Passport>();
    
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);
    boolean hb = backend != null && service.get(HBCISynchronizeBackend.class).equals(backend);
    
    for (Passport p:all)
    {
      boolean hp = (p instanceof de.willuhn.jameica.hbci.passports.ddv.rmi.Passport) ||
                   (p instanceof de.willuhn.jameica.hbci.passports.rdh.rmi.Passport) ||
                   (p instanceof de.willuhn.jameica.hbci.passports.pintan.rmi.Passport);

      // Nur hinzufuegen, wenn entweder beide true oder beide false sind.
      // Sprich: HBCI-Passports nur bei HBCI-Backend. Nicht-HBCI-Passports nur bei Nicht-HBCI-Backends
      if (hb == hp)
        result.add(p);
    }
    
    this.setList(result);
  }
  
  /**
   * Initialisiert die Passport-Liste.
   * @return Liste der Passports.
   */
  private static List<Passport> init()
  {
    if (passports == null)
    {
      try
      {
        passports = Arrays.asList(PassportRegistry.getPassports());
      }
      catch (ApplicationException ae)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
      }
      catch (Exception e)
      {
        Logger.error("error while loading passport list",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Laden der HBCI-Verfahren"),StatusBarMessage.TYPE_ERROR));
      }
    }
    
    if (passports == null)
      passports = new ArrayList<Passport>();
    
    return passports;
  }
}


/*********************************************************************
 * $Log: PassportInput.java,v $
 * Revision 1.6  2011/04/29 11:38:58  willuhn
 * @N Konfiguration der HBCI-Medien ueberarbeitet. Es gibt nun direkt in der Navi einen Punkt "Bank-Zugaenge", in der alle Medien angezeigt werden.
 *
 * Revision 1.5  2010/04/22 12:42:03  willuhn
 * @N Erste Version des Supports fuer Offline-Konten
 *
 * Revision 1.4  2009/05/07 13:36:57  willuhn
 * @R Hilfsobjekt "PassportObject" entfernt
 * @C Cleanup in PassportInput (insb. der weisse Hintergrund hinter dem "Konfigurieren..."-Button hat gestoert
 *
 * Revision 1.3  2009/01/04 17:43:29  willuhn
 * @N BUGZILLA 532
 *
 * Revision 1.2  2006/06/19 10:57:04  willuhn
 * @N neue Methode setEnabled(boolean) in Input
 *
 * Revision 1.1  2006/03/21 00:43:14  willuhn
 * @B bug 209
 *
 **********************************************************************/