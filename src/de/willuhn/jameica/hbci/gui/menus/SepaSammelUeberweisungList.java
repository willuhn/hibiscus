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
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Duplicate;
import de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungDelete;
import de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungExecute;
import de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungExport;
import de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungImport;
import de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungNew;
import de.willuhn.jameica.hbci.gui.action.SepaSammelUeberweisungSplit;
import de.willuhn.jameica.hbci.gui.action.TerminableMarkExecuted;
import de.willuhn.jameica.hbci.io.print.PrintSupportSepaSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.SepaSammelUeberweisung;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Kontext-Menu, welches an Listen mit SEPA-Sammelueberweisungen gehangen werden kann.
 * Es ist fix und fertig vorkonfiguriert und mit Elementen gefuellt.
 */
public class SepaSammelUeberweisungList extends ContextMenu
{
	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

	/**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von SEPA-Sammelueberweisungen.
	 */
	public SepaSammelUeberweisungList()
	{
		addItem(new SingleItem(i18n.tr("Öffnen"), new SepaSammelUeberweisungNew(),"document-open.png"));
    addItem(new ContextMenuItem(i18n.tr("Neue SEPA-Sammelüberweisung..."), new SNeu(),"text-x-generic.png"));
    addItem(new CheckedContextMenuItem(i18n.tr("Löschen..."), new SepaSammelUeberweisungDelete(),"user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new SingleItem(i18n.tr("Duplizieren..."), new Duplicate(),"edit-copy.png"));
    addItem(new NotActiveMultiMenuItem(i18n.tr("In Einzelaufträge teilen..."), new SepaSammelUeberweisungSplit(),"ueberweisung.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new NotActiveMenuItem(i18n.tr("Jetzt ausführen..."), new SepaSammelUeberweisungExecute(),"emblem-important.png"));
    addItem(new NotActiveMultiMenuItem(i18n.tr("Als \"ausgeführt\" markieren..."), new TerminableMarkExecuted(),"emblem-default.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedSingleContextMenuItem(i18n.tr("Drucken..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new Print().handleAction(new PrintSupportSepaSammelUeberweisung((SepaSammelUeberweisung) context));
      }
    },"document-print.png"));
    addItem(new CheckedContextMenuItem(i18n.tr("Exportieren..."),new SepaSammelUeberweisungExport(),"document-save.png"));
    addItem(new ContextMenuItem(i18n.tr("Importieren..."),new SepaSammelUeberweisungImport(),"document-open.png"));
		
	}

	/**
	 * Ueberschreiben wir, um <b>grundsaetzlich</b> eine neue SEPA-Sammelueberweisung
	 * anzulegen - auch wenn der Focus auf einer existierenden liegt.
   */
  private class SNeu extends SepaSammelUeberweisungNew
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
      if (o instanceof SepaSammelUeberweisung[])
        return false;
      return super.isEnabledFor(o);
    }
  }

  
	/**
	 * Ueberschreiben wir, damit das Item nur dann aktiv ist, wenn die
	 * Ueberweisung noch nicht ausgefuehrt wurde.
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
    	if (o == null || !(o instanceof SepaSammelUeberweisung))
    		return false;
    	try
    	{
    	  SepaSammelUeberweisung u = (SepaSammelUeberweisung) o;
    		return !u.ausgefuehrt();
    	}
    	catch (Exception e)
    	{
    		Logger.error("error while enable check in menu item",e);
    	}
    	return false;
    }
	}
  
  /**
   * Liefert nur dann true, wenn alle uebergebenen Auftraege noch nicht
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
      if (o == null || (!(o instanceof Terminable) && !(o instanceof Terminable[])))
        return false;
      try
      {
        if (o instanceof Terminable)
          return !((Terminable)o).ausgefuehrt();

        Terminable[] t = (Terminable[]) o;
        for (Terminable mitTermin : t)
        {
          if (mitTermin.ausgefuehrt())
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
