/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/SammelLastschriftList.java,v $
 * $Revision: 1.13 $
 * $Date: 2009/02/13 14:17:01 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.menus;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftExecute;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftExport;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftImport;
import de.willuhn.jameica.hbci.gui.action.SammelLastschriftNew;
import de.willuhn.jameica.hbci.gui.action.SammelTransferDuplicate;
import de.willuhn.jameica.hbci.gui.action.TerminableMarkExecuted;
import de.willuhn.jameica.hbci.rmi.SammelLastschrift;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Kontext-Menu, welches an Listen mit SammelLastschriften gehangen werden kann.
 * Es ist fix und fertig vorkonfiguriert und mit Elementen gefuellt.
 */
public class SammelLastschriftList extends ContextMenu
{
	private I18N i18n	= null;

	/**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Lastschriften.
	 */
	public SammelLastschriftList()
	{
		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		addItem(new SingleItem(i18n.tr("Öffnen"), new SammelLastschriftNew(),"document-open.png"));
    addItem(new ContextMenuItem(i18n.tr("Neue Sammel-Lastschrift..."), new SNeu(),"text-x-generic.png"));
    addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new DBObjectDelete(),"user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new SingleItem(i18n.tr("Duplizieren..."), new SammelTransferDuplicate(),"edit-copy.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new NotActiveMenuItem(i18n.tr("Jetzt ausführen..."), new SammelLastschriftExecute(),"emblem-important.png"));
    addItem(new ContextMenuItem(i18n.tr("Als \"ausgeführt\" markieren..."), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new TerminableMarkExecuted().handleAction(context);
        GUI.startView(GUI.getCurrentView().getClass(),GUI.getCurrentView().getCurrentObject());
      }
    },"emblem-default.png"){
      public boolean isEnabledFor(Object o)
      {
        if (o == null || (!(o instanceof Terminable) && !(o instanceof Terminable[])))
          return false;
        try
        {
          if (o instanceof Terminable)
            return !((Terminable)o).ausgefuehrt();

          Terminable[] t = (Terminable[]) o;
          for (int i=0;i<t.length;++i)
          {
            if (t[i].ausgefuehrt())
              return false;
          }
          return true;
        }
        catch (RemoteException e)
        {
          Logger.error("unable to check if terminable is allready executed",e);
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Prüfen, ob Auftrag bereits ausgeführt wurde"),StatusBarMessage.TYPE_ERROR));
          return false;
        }
      }
    });
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedContextMenuItem(i18n.tr("Exportieren..."),new SammelLastschriftExport(),"document-save.png"));
    addItem(new ContextMenuItem(i18n.tr("Importieren..."),new SammelLastschriftImport(),"document-open.png"));
		
	}

	/**
	 * Ueberschreiben wir, um <b>grundsaetzlich</b> eine neue Sammel-Lastschrift
	 * anzulegen - auch wenn der Focus auf einer existierenden liegt.
   */
  private class SNeu extends SammelLastschriftNew
	{
    /**
     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
     */
    public void handleAction(Object context) throws ApplicationException
    {
    	super.handleAction(null);
    }
	} 

  /**
   * Ueberschrieben, um zu pruefen, ob ein Array oder ein einzelnes Element markiert ist.
   */
  private class SingleItem extends CheckedContextMenuItem
  {
    /**
     * @param text
     * @param action
     * @param optionale Angabe eines Icons.
     */
    private SingleItem(String text, Action action, String icon)
    {
      super(text,action,icon);
    }
    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof SammelLastschrift[])
        return false;
      return super.isEnabledFor(o);
    }
  }

  
	/**
	 * Ueberschreiben wir, damit das Item nur dann aktiv ist, wenn die
	 * Lastschrift noch nicht ausgefuehrt wurde.
   */
  private class NotActiveMenuItem extends ContextMenuItem
	{
		
    /**
     * ct.
     * @param text anzuzeigender Text.
     * @param a auszufuehrende Action.
     * @param icon optionales Icon.
     */
    public NotActiveMenuItem(String text, Action a, String icon)
    {
      super(text, a, icon);
    }

	  /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
    	if (o == null || !(o instanceof SammelLastschrift))
    		return false;
    	try
    	{
				SammelLastschrift u = (SammelLastschrift) o;
    		return !u.ausgefuehrt();
    	}
    	catch (Exception e)
    	{
    		Logger.error("error while enable check in menu item",e);
    	}
    	return false;
    }
	}
}


/**********************************************************************
 * $Log: SammelLastschriftList.java,v $
 * Revision 1.13  2009/02/13 14:17:01  willuhn
 * @N BUGZILLA 700
 *
 * Revision 1.12  2008/12/19 12:16:05  willuhn
 * @N Mehr Icons
 * @C Reihenfolge der Contextmenu-Eintraege vereinheitlicht
 *
 * Revision 1.11  2007/12/06 23:53:35  willuhn
 * @C Menu-Eintraege uebersichtlicher angeordnet
 *
 * Revision 1.10  2006/10/23 21:16:51  willuhn
 * @N eBaykontoParser umbenannt und ueberarbeitet
 *
 * Revision 1.9  2006/08/07 14:45:18  willuhn
 * @B typos
 *
 * Revision 1.8  2006/08/07 14:31:59  willuhn
 * @B misc bugfixing
 * @C Redesign des DTAUS-Imports fuer Sammeltransfers
 *
 * Revision 1.7  2006/03/30 22:56:46  willuhn
 * @B bug 216
 *
 * Revision 1.6  2005/09/30 00:08:51  willuhn
 * @N SammelUeberweisungen (merged with SammelLastschrift)
 *
 * Revision 1.5  2005/08/22 10:36:38  willuhn
 * @N bug 115, 116
 *
 * Revision 1.4  2005/07/04 12:41:39  web0
 * @B bug 90
 *
 * Revision 1.3  2005/03/02 00:22:05  web0
 * @N first code for "Sammellastschrift"
 *
 * Revision 1.2  2005/03/01 18:51:04  web0
 * @N Dialoge fuer Sammel-Lastschriften
 *
 * Revision 1.1  2005/02/28 18:40:49  web0
 * @N first code for "Sammellastschrift"
 *
 **********************************************************************/