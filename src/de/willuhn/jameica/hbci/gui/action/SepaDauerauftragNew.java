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

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer neuen SEPA-Dauerauftrag.
 */
public class SepaDauerauftragNew implements Action
{

  /**
   * Als Context kann ein Konto, ein Empfaenger oder ein SEPA-Dauerauftrag angegeben werden.
   * Abhaengig davon wird das eine oder andere Feld in dem SEPA-Dauerauftrag 
   * vorausgefuellt oder der uebergebene SEPA-Dauerauftrag geladen.
   * Wenn nichts angegeben ist, wird ein leerer SEPA-Dauerauftrag erstellt und angezeigt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		SepaDauerauftrag d = null;

		if (context instanceof SepaDauerauftrag)
		{
			d = (SepaDauerauftrag) context;
		}
		else if (context instanceof Konto)
		{
			try {
				Konto k = (Konto) context;
				d = (SepaDauerauftrag) Settings.getDBService().createObject(SepaDauerauftrag.class,null);
				if (!k.hasFlag(Konto.FLAG_DISABLED) && !k.hasFlag(Konto.FLAG_OFFLINE))
  				d.setKonto(k);
			}
			catch (RemoteException e)
			{
				// Dann halt nicht
			}
		}
		else if (context instanceof Address)
		{
			try {
				Address e = (Address) context;
				d = (SepaDauerauftrag) Settings.getDBService().createObject(SepaDauerauftrag.class,null);
				d.setGegenkonto(e);
			}
			catch (RemoteException e)
			{
				throw new ApplicationException(i18n.tr("Fehler beim Anlegen des Dauerauftrages"));
			}
		}

  	GUI.startView(de.willuhn.jameica.hbci.gui.views.SepaDauerauftragNew.class,d);
  }

}

