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
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.util.ApplicationException;

/**
 * Action fuer neuen Dauerauftrag.
 */
public class DauerauftragNew implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
		Dauerauftrag d = null;

		if (context instanceof Dauerauftrag)
		{
			d = (Dauerauftrag) context;
		}
  	GUI.startView(de.willuhn.jameica.hbci.gui.views.DauerauftragNew.class,d);
  }

}
