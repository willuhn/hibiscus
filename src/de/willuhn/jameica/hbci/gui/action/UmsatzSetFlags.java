/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/UmsatzSetFlags.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/02/04 23:06:24 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Setz oder entfermnt die genannten Flags in ein oder mehreren Umsaetzen.
 */
public class UmsatzSetFlags implements Action
{
  private int flags   = Umsatz.FLAG_NONE;
  private boolean add = true;
  
  /**
   * ct.
   * @param flags die zu setzenden Flags.
   * @param add true, wenn Flags hinzugefuegt werden sollen. Andernfalls werden sie entfernt.
   * @see Umsatz#FLAG_CHECKED
   */
  public UmsatzSetFlags(int flags, boolean add)
  {
    this.flags = flags;
    this.add   = add;
  }

  /**
   * Erwartet ein Objekt vom Typ <code>Umsatz</code> oder <code>Umsatz[]</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		if (context == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Umsätze aus"));

    if (!(context instanceof Umsatz) && !(context instanceof Umsatz[]))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Umsätze aus"));

    Umsatz[] umsaetze = null;
    
    if (context instanceof Umsatz)
      umsaetze = new Umsatz[]{(Umsatz) context};
    else
      umsaetze = (Umsatz[]) context;

    if (umsaetze.length == 0)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie einen oder mehrere Umsätze aus"));

    try
    {
      umsaetze[0].transactionBegin();
      for (int i=0;i<umsaetze.length;++i)
      {
        int current = umsaetze[i].getFlags();
        if (this.add)
          umsaetze[i].setFlags(current | this.flags);
        else
          umsaetze[i].setFlags(current ^ this.flags);
        umsaetze[i].store();
        Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(umsaetze[i]));
      }
      umsaetze[0].transactionCommit();
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Änderungen gespeichert"), StatusBarMessage.TYPE_SUCCESS));
    }
		catch (Exception e)
		{
	    try {
	      umsaetze[0].transactionRollback();
	    }
	    catch (Exception e1) {
	      Logger.error("unable to rollback transaction",e1);
	    }
	    
	    if (e instanceof ApplicationException)
	      throw (ApplicationException) e;

	    Logger.error("error while setting flags",e);
			throw new ApplicationException(i18n.tr("Fehler beim Zuordnen der Umsatz-Kategorie"));
		}
  }
}


/**********************************************************************
 * $Log: UmsatzSetFlags.java,v $
 * Revision 1.1  2009/02/04 23:06:24  willuhn
 * @N BUGZILLA 308 - Umsaetze als "geprueft" markieren
 *
 **********************************************************************/