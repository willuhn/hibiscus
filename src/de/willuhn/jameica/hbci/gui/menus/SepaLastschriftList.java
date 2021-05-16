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
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.Duplicate;
import de.willuhn.jameica.hbci.gui.action.SepaLastschriftExecute;
import de.willuhn.jameica.hbci.gui.action.SepaLastschriftExport;
import de.willuhn.jameica.hbci.gui.action.SepaLastschriftImport;
import de.willuhn.jameica.hbci.gui.action.SepaLastschriftMerge;
import de.willuhn.jameica.hbci.gui.action.SepaLastschriftNew;
import de.willuhn.jameica.hbci.gui.action.TerminableMarkExecuted;
import de.willuhn.jameica.hbci.io.print.PrintSupportSepaLastschriftList;
import de.willuhn.jameica.hbci.rmi.SepaLastschrift;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Kontext-Menu, welches an Listen mit SEPA-Lastschriften gehangen werden kann.
 * Es ist fix und fertig vorkonfiguriert und mit Elementen gefuellt.
 */
public class SepaLastschriftList extends ContextMenu
{
	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Lastschriften.
	 */
	public SepaLastschriftList()
	{
		addItem(new SingleItem(i18n.tr("Öffnen"), new SepaLastschriftNew(),"document-open.png"));
    addItem(new ContextMenuItem(i18n.tr("Neue SEPA-Lastschrift..."), new UNeu(),"text-x-generic.png"));
    addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new DBObjectDelete(),"user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new SingleItem(i18n.tr("Duplizieren..."), new Duplicate(),"edit-copy.png"));
    addItem(new NotActiveMultiMenuItem(i18n.tr("Zu Sammelaufträgen zusammenfassen..."), new SepaLastschriftMerge(),"slastschrift.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new NotActiveSingleMenuItem(i18n.tr("Jetzt ausführen..."), new SepaLastschriftExecute(),"emblem-important.png"));
    addItem(new NotActiveMultiMenuItem(i18n.tr("Als \"ausgeführt\" markieren..."), new TerminableMarkExecuted(),"emblem-default.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedContextMenuItem(i18n.tr("Drucken..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new Print().handleAction(new PrintSupportSepaLastschriftList(context));
      }
    },"document-print.png"));
    addItem(new CheckedContextMenuItem(i18n.tr("Exportieren..."),new SepaLastschriftExport(),"document-save.png"));
    addItem(new ContextMenuItem(i18n.tr("Importieren..."),new SepaLastschriftImport(),"document-open.png"));
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
      if (o instanceof SepaLastschrift[])
        return false;
      return super.isEnabledFor(o);
    }
  }

	/**
	 * Ueberschreiben wir, um <b>grundsaetzlich</b> eine neue Lastschrift
	 * anzulegen - auch wenn der Focus auf einer existierenden liegt.
   */
  private class UNeu extends SepaLastschriftNew
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
	 * Lastschrift noch nicht ausgefuehrt wurde.
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
      if (o == null || !(o instanceof SepaLastschrift))
        return false;

      try
    	{
        if (o instanceof SepaLastschrift[])
          return false;
        SepaLastschrift u = (SepaLastschrift) o;
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
   * Liefert nur dann true, wenn alle uebergebenen Lastschriften noch nicht
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
      if (o == null || (!(o instanceof SepaLastschrift) && !(o instanceof SepaLastschrift[])))
        return false;
      try
      {
        if (o instanceof SepaLastschrift)
          return !((SepaLastschrift)o).ausgefuehrt();

        SepaLastschrift[] t = (SepaLastschrift[]) o;
        for (SepaLastschrift lastschrift : t)
        {
          if (lastschrift.ausgefuehrt())
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
