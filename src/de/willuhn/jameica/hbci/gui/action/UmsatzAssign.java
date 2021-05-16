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

import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.dialogs.UmsatzTypListDialog;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ordnet ein oder meheren Umsaetzen eine Kategorie zu.
 */
public class UmsatzAssign implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erwartet ein Objekt vom Typ <code>Umsatz</code> oder <code>Umsatz[]</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
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

    UmsatzTyp ut = null;
    
    try
    {
      int typ = UmsatzTyp.TYP_EGAL;
      
      if (umsaetze.length == 1)
      {
        // Mal schauen, ob der Umsatz schon einen Typ hat
        ut = umsaetze[0].getUmsatzTyp();

        // Dialog anzeigen, bei dem nur die zum Betrag passenden Kategorien angezeigt werden
        if (ut == null && umsaetze[0].getBetrag() != 0)
          typ = (umsaetze[0].getBetrag() > 0 ? UmsatzTyp.TYP_EINNAHME : UmsatzTyp.TYP_AUSGABE);
      }
      UmsatzTypListDialog d = new UmsatzTypListDialog(UmsatzTypListDialog.POSITION_CENTER,ut,typ);
      ut = (UmsatzTyp) d.open();
    }
    catch (OperationCanceledException oce)
    {
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while choosing umsatztyp",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Auswählen der Umsatz-Kategorie"), StatusBarMessage.TYPE_ERROR));
    }
      

    try
    {
      umsaetze[0].transactionBegin();
      for (final Umsatz umsatz : umsaetze)
      {
        umsatz.setUmsatzTyp(ut);
        umsatz.store();
        Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(umsatz));
      }
      umsaetze[0].transactionCommit();
      
      if (ut == null)
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Zuordnung der Kategorie entfernt"), StatusBarMessage.TYPE_SUCCESS));
      else
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Umsatz-Kategorie {0} zugeordnet", ut.getName()), StatusBarMessage.TYPE_SUCCESS));
    }
		catch (ApplicationException ae)
		{
      rollback(umsaetze[0]);
			throw ae;
		}
		catch (Exception e)
		{
      rollback(umsaetze[0]);
			Logger.error("error while assigning umsaetze",e);
			Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Zuordnen der Umsatz-Kategorie"), StatusBarMessage.TYPE_ERROR));
		}
  }
  
  private void rollback(DBObject o)
  {
    if (o == null)
      return;
    try
    {
      Logger.info("rollback transaction");
      o.transactionRollback();
    }
    catch (Exception e)
    {
      Logger.error("unable to rollback transaction - useless",e);
    }
  }
}


/**********************************************************************
 * $Log: UmsatzAssign.java,v $
 * Revision 1.11  2011/05/11 10:20:28  willuhn
 * @N OCE fangen
 *
 * Revision 1.10  2011-05-06 09:04:16  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2010-09-27 11:51:38  willuhn
 * @N BUGZILLA 804
 *
 * Revision 1.8  2010/03/05 23:52:27  willuhn
 * @C Code-Cleanup
 * @C Liste der Kategorien kann jetzt nicht mehr von aussen an UmsatzTypInput uebergeben werden
 *
 * Revision 1.7  2008/08/29 16:46:23  willuhn
 * @N BUGZILLA 616
 *
 * Revision 1.6  2008/08/08 08:43:41  willuhn
 * @N BUGZILLA 614
 *
 * Revision 1.5  2007/04/23 18:07:14  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.4  2007/03/16 14:40:02  willuhn
 * @C Redesign ImportMessage
 * @N Aktualisierung der Umsatztabelle nach Kategorie-Zuordnung
 *
 * Revision 1.2  2006/12/29 14:28:47  willuhn
 * @B Bug 345
 * @B jede Menge Bugfixes bei SQL-Statements mit Valuta
 *
 * Revision 1.1  2006/11/30 23:48:40  willuhn
 * @N Erste Version der Umsatz-Kategorien drin
 *
 **********************************************************************/