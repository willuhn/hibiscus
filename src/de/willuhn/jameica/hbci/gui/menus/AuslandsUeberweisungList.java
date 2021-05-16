/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.menus;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.internal.action.Print;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungDelete;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungExecute;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungExport;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungImport;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungNew;
import de.willuhn.jameica.hbci.gui.action.Duplicate;
import de.willuhn.jameica.hbci.gui.action.SepaUeberweisungMerge;
import de.willuhn.jameica.hbci.gui.action.TerminableMarkExecuted;
import de.willuhn.jameica.hbci.io.print.PrintSupportAuslandsUeberweisungList;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Kontext-Menu, welches an Listen mit Auslands-Ueberweisungen gehangen werden kann.
 * Es ist fix und fertig vorkonfiguriert und mit Elementen gefuellt.
 */
public class AuslandsUeberweisungList extends ContextMenu
{
	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Ueberweisungen.
	 */
	public AuslandsUeberweisungList()
	{
		addItem(new SingleItem(i18n.tr("Öffnen"), new AuslandsUeberweisungNew(),"document-open.png"));
    addItem(new ContextMenuItem(i18n.tr("Neue SEPA-Überweisung..."), new UNeu(),"text-x-generic.png"));
    addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new AuslandsUeberweisungDelete(),"user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new SingleItem(i18n.tr("Duplizieren..."), new Duplicate(),"edit-copy.png"));
    addItem(new NotActiveMultiMenuItem(i18n.tr("Zu Sammelaufträgen zusammenfassen..."), new SepaUeberweisungMerge(),"sueberweisung.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new NotActiveSingleMenuItem(i18n.tr("Jetzt ausführen..."), new AuslandsUeberweisungExecute(),"emblem-important.png"));
    addItem(new NotActiveMultiMenuItem(i18n.tr("Als \"ausgeführt\" markieren..."), new TerminableMarkExecuted(),"emblem-default.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedContextMenuItem(i18n.tr("Drucken..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new Print().handleAction(new PrintSupportAuslandsUeberweisungList(context));
      }
    },"document-print.png"));
    addItem(new CheckedContextMenuItem(i18n.tr("Exportieren..."),new AuslandsUeberweisungExport(),"document-save.png"));
    addItem(new ContextMenuItem(i18n.tr("Importieren..."),new AuslandsUeberweisungImport(),"document-open.png"));
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
      if (o instanceof AuslandsUeberweisung[])
        return false;
      return super.isEnabledFor(o);
    }
  }

	/**
	 * Ueberschreiben wir, um <b>grundsaetzlich</b> eine neue Ueberweisung
	 * anzulegen - auch wenn der Focus auf einer existierenden liegt.
   */
  private class UNeu extends AuslandsUeberweisungNew
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
	 * Ueberschreiben wir, damit das Item nur dann aktiv ist, wenn die
	 * Ueberweisung noch nicht ausgefuehrt wurde.
   */
  private class NotActiveSingleMenuItem extends CheckedContextMenuItem
	{

    /**
     * ct.
     * @param text anzuzeigender Text.
     * @param a auszufuehrende Action.
     * @param icon optionales Icon.
     */
    public NotActiveSingleMenuItem(String text, Action a, String icon)
    {
      super(text, a, icon);
    }

	  /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o == null || !(o instanceof AuslandsUeberweisung))
        return false;

      try
    	{
        if (o instanceof AuslandsUeberweisung[])
          return false;
        AuslandsUeberweisung u = (AuslandsUeberweisung) o;
        return !u.ausgefuehrt();
    	}
    	catch (Exception e)
    	{
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Prüfen, ob Auftrag bereits ausgeführt wurde"),StatusBarMessage.TYPE_ERROR));
    		Logger.error("error while enable check in menu item",e);
    	}
    	return false;
    }
	}

  /**
   * Liefert nur dann true, wenn alle uebergebenen Ueberweisungen noch nicht
   * ausgefuehrt wurden.
   */
  private class NotActiveMultiMenuItem extends CheckedContextMenuItem
  {

    /**
     * ct.
     * @param text anzuzeigender Text.
     * @param a auszufuehrende Action.
     * @param icon optionales Icon.
     */
    public NotActiveMultiMenuItem(String text, Action a, String icon)
    {
      super(text, a, icon);
    }

    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o == null || (!(o instanceof AuslandsUeberweisung) && !(o instanceof AuslandsUeberweisung[])))
        return false;
      try
      {
        if (o instanceof AuslandsUeberweisung)
          return !((AuslandsUeberweisung)o).ausgefuehrt();

        AuslandsUeberweisung[] t = (AuslandsUeberweisung[]) o;
        for (AuslandsUeberweisung ueberweisung : t)
        {
          if (ueberweisung.ausgefuehrt())
            return false;
        }
        return true;
      }
      catch (RemoteException e)
      {
        Logger.error("unable to check if terminable is already executed",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Prüfen, ob Auftrag bereits ausgeführt wurde"),StatusBarMessage.TYPE_ERROR));
      }
      return false;
    }
  }

}


/**********************************************************************
 * $Log: AuslandsUeberweisungList.java,v $
 * Revision 1.6  2012/01/27 22:43:22  willuhn
 * @N BUGZILLA 1181
 *
 * Revision 1.5  2011-04-11 14:36:37  willuhn
 * @N Druck-Support fuer Lastschriften und SEPA-Ueberweisungen
 *
 * Revision 1.4  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.3  2009/03/17 23:44:15  willuhn
 * @N BUGZILLA 159 - Auslandsueberweisungen. Erste Version
 *
 * Revision 1.2  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 * Revision 1.1  2009/02/17 00:00:02  willuhn
 * @N BUGZILLA 159 - Erster Code fuer Auslands-Ueberweisungen
 *
 **********************************************************************/