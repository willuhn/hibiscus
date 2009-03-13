/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/menus/AuslandsUeberweisungList.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/03/13 00:25:12 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.menus;

import de.willuhn.jameica.gui.parts.ContextMenu;

/**
 * Kontext-Menu, welches an Listen mit Auslands-Ueberweisungen gehangen werden kann.
 * Es ist fix und fertig vorkonfiguriert und mit Elementen gefuellt.
 */
public class AuslandsUeberweisungList extends ContextMenu
{
//	private I18N i18n	= null;

	  /**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Ueberweisungen.
	 */
	public AuslandsUeberweisungList()
	{
	  // TODO: ALU: Context-Menu fehlt noch
//		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

//		addItem(new SingleItem(i18n.tr("Öffnen"), new UeberweisungNew(),"document-open.png"));
//    addItem(new ContextMenuItem(i18n.tr("Neue Überweisung..."), new UNeu(),"text-x-generic.png"));
//    addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new DBObjectDelete(),"user-trash-full.png"));
//    addItem(ContextMenuItem.SEPARATOR);
//    addItem(new SingleItem(i18n.tr("Duplizieren..."), new UeberweisungDuplicate(),"edit-copy.png"));
//    addItem(ContextMenuItem.SEPARATOR);
//    addItem(new NotActiveSingleMenuItem(i18n.tr("Jetzt ausführen..."), new UeberweisungExecute(),"emblem-important.png"));
//    addItem(new NotActiveMultiMenuItem(i18n.tr("Als \"ausgeführt\" markieren..."), new TerminableMarkExecuted(),"emblem-default.png"));
//    addItem(ContextMenuItem.SEPARATOR);
//    addItem(new CheckedContextMenuItem(i18n.tr("Exportieren..."),new UeberweisungExport(),"document-save.png"));
//    addItem(new ContextMenuItem(i18n.tr("Importieren..."),new UeberweisungImport(),"document-open.png"));
		
	}

//  /**
//   * Ueberschrieben, um zu pruefen, ob ein Array oder ein einzelnes Element markiert ist.
//   */
//  private class SingleItem extends CheckedContextMenuItem
//  {
//    /**
//     * @param text
//     * @param action
//     * @param optionale Angabe eines Icons.
//     */
//    private SingleItem(String text, Action action, String icon)
//    {
//      super(text,action,icon);
//    }
//    /**
//     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
//     */
//    public boolean isEnabledFor(Object o)
//    {
//      if (o instanceof AuslandsUeberweisung[])
//        return false;
//      return super.isEnabledFor(o);
//    }
//  }
//
//	/**
//	 * Ueberschreiben wir, um <b>grundsaetzlich</b> eine neue Ueberweisung
//	 * anzulegen - auch wenn der Focus auf einer existierenden liegt.
//   */
//  private class UNeu extends UeberweisungNew
//	{
//    /**
//     * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
//     */
//    public void handleAction(Object context) throws ApplicationException
//    {
//    	super.handleAction(null);
//    }
//	} 
//	
//	/**
//	 * Ueberschreiben wir, damit das Item nur dann aktiv ist, wenn die
//	 * Ueberweisung noch nicht ausgefuehrt wurde.
//   */
//  private class NotActiveSingleMenuItem extends CheckedContextMenuItem
//	{
//		
//    /**
//     * ct.
//     * @param text anzuzeigender Text.
//     * @param a auszufuehrende Action.
//     * @param icon optionales Icon.
//     */
//    public NotActiveSingleMenuItem(String text, Action a, String icon)
//    {
//      super(text, a, icon);
//    }
//
//	  /**
//     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
//     */
//    public boolean isEnabledFor(Object o)
//    {
//      if (o == null || !(o instanceof AuslandsUeberweisung))
//        return false;
//
//      try
//    	{
//        if (o instanceof AuslandsUeberweisung[])
//          return false;
//        AuslandsUeberweisung u = (AuslandsUeberweisung) o;
//        return !u.ausgefuehrt();
//    	}
//    	catch (Exception e)
//    	{
//        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Prüfen, ob Auftrag bereits ausgeführt wurde"),StatusBarMessage.TYPE_ERROR));
//    		Logger.error("error while enable check in menu item",e);
//    	}
//    	return false;
//    }
//	}
//
//  /**
//   * Liefert nur dann true, wenn alle uebergebenen Ueberweisungen noch nicht
//   * ausgefuehrt wurden.
//   */
//  private class NotActiveMultiMenuItem extends CheckedContextMenuItem
//  {
//    
//    /**
//     * ct.
//     * @param text anzuzeigender Text.
//     * @param a auszufuehrende Action.
//     * @param icon optionales Icon.
//     */
//    public NotActiveMultiMenuItem(String text, Action a, String icon)
//    {
//      super(text, a, icon);
//    }
//
//    /**
//     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
//     */
//    public boolean isEnabledFor(Object o)
//    {
//      if (o == null || (!(o instanceof AuslandsUeberweisung) && !(o instanceof AuslandsUeberweisung[])))
//        return false;
//      try
//      {
//        if (o instanceof AuslandsUeberweisung)
//          return !((AuslandsUeberweisung)o).ausgefuehrt();
//
//        AuslandsUeberweisung[] t = (AuslandsUeberweisung[]) o;
//        for (int i=0;i<t.length;++i)
//        {
//          if (t[i].ausgefuehrt())
//            return false;
//        }
//        return true;
//      }
//      catch (RemoteException e)
//      {
//        Logger.error("unable to check if terminable is allready executed",e);
//        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Prüfen, ob Auftrag bereits ausgeführt wurde"),StatusBarMessage.TYPE_ERROR));
//      }
//      return false;
//    }
//  }

}


/**********************************************************************
 * $Log: AuslandsUeberweisungList.java,v $
 * Revision 1.2  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 * Revision 1.1  2009/02/17 00:00:02  willuhn
 * @N BUGZILLA 159 - Erster Code fuer Auslands-Ueberweisungen
 *
 **********************************************************************/