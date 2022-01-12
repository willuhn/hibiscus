/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.gui.dialogs.PassportAuswahlDialog;
import de.willuhn.jameica.hbci.gui.views.KontoList;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, welche die Konten aus einem Passport ermittelt und abspeichert.
 */
public class KontoFetchFromPassport implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Objekt vom Typ <code>de.willuhn.jameica.hbci.passport.Passport</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    Passport passport = null;
    
    try
    {
      if (context instanceof Konto)
      {
        try 
        {
          Konto k = (Konto) context;
          passport = PassportRegistry.findByClass(k.getPassportClass());
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (Exception e)
        {
          Logger.error("error while reading passport from select box",e);
          throw new ApplicationException(i18n.tr("Fehler beim Auslesen der Konto-Informationen"));
        }
      }
      else if (context instanceof Passport)
      {
        passport = (Passport) context;
      }
      else
      {
        PassportAuswahlDialog d = new PassportAuswahlDialog(PassportAuswahlDialog.POSITION_CENTER);
        passport = (Passport) d.open();
      }
    }
    catch (ApplicationException | OperationCanceledException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      Logger.error("unable to select passport",e);
      throw new ApplicationException(i18n.tr("Auswahl des Bank-Zugangs fehlgeschlagen"),e);
    }
    

		final Passport p = passport;

		GUI.startSync(new Runnable()
		{
			public void run() {
				try {

					GUI.getStatusBar().startProgress();
					GUI.getStatusBar().setSuccessText(i18n.tr("Bank-Zugang wird ausgelesen..."));
					new KontoMerge().handleAction(p.getHandle().getKonten());

					// Konto-Liste neu laden
					GUI.startView(KontoList.class,null);
				}
        catch (OperationCanceledException oce)
        {
          // ignore
        }
        catch (ApplicationException ae)
        {
          GUI.getStatusBar().setErrorText(ae.getMessage());
        }
				catch (Throwable t)
				{
					Logger.error("error while reading data from passport",t);
					GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen der Konto-Daten. Bitte prüfen Sie die Einstellungen des Bank-Zugangs."));
				}
				finally
				{
					GUI.getStatusBar().stopProgress();
				}
			}
		});
  }
}
