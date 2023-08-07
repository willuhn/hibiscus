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
import java.util.List;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.gui.internal.action.Print;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungNew;
import de.willuhn.jameica.hbci.gui.action.DBObjectDelete;
import de.willuhn.jameica.hbci.gui.action.EmpfaengerAdd;
import de.willuhn.jameica.hbci.gui.action.UmsatzAssign;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetailEdit;
import de.willuhn.jameica.hbci.gui.action.UmsatzExport;
import de.willuhn.jameica.hbci.gui.action.UmsatzImport;
import de.willuhn.jameica.hbci.gui.action.UmsatzMarkChecked;
import de.willuhn.jameica.hbci.gui.action.UmsatzMarkUnChecked;
import de.willuhn.jameica.hbci.gui.action.UmsatzTypNew;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.gui.parts.UmsatzSetAllReadContextMenuItem;
import de.willuhn.jameica.hbci.gui.parts.UmsatzSetReadContextMenuItem;
import de.willuhn.jameica.hbci.gui.parts.UmsatzSetUnreadContextMenuItem;
import de.willuhn.jameica.hbci.io.print.PrintSupportUmsatzList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.hbci.server.UmsatzTreeNode;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Listen von Umsaetzen
 * angehaengt werden kann.
 */
public class UmsatzList extends ContextMenu implements Extendable
{

	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * Erzeugt ein Kontext-Menu fuer eine Liste von Umsaetzen.
   */
  public UmsatzList()
  {
    this(null);
  }

  /**
	 * Erzeugt ein Kontext-Menu fuer eine Liste von Umsaetzen.
   * @param konto optionale Angabe des Kontos.
	 */
	public UmsatzList(final Konto konto)
	{
		addItem(new OpenItem());
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new UmsatzItem(i18n.tr("In Adressbuch übernehmen"),new EmpfaengerAdd(),"contact-new.png"));
    addItem(new UmsatzItem(i18n.tr("Als neue Überweisung anlegen..."),new AuslandsUeberweisungNew(),"ueberweisung.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new UmsatzBookedItem(i18n.tr("als \"geprüft\" markieren..."),new UmsatzMarkChecked(),"emblem-default.png","ALT+G"));
    addItem(new UmsatzBookedItem(i18n.tr("als \"ungeprüft\" markieren..."),new UmsatzMarkUnChecked(),"edit-undo.png","CTRL+ALT+G"));
    addReverseBookItem();
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new UmsatzItem(i18n.tr("Drucken..."),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        new Print().handleAction(new PrintSupportUmsatzList(context));
      }
    },"document-print.png"));
    addItem(new UmsatzOrGroupItem(i18n.tr("Exportieren..."),new UmsatzExport(),"document-save.png"));
    addItem(new ContextMenuItem(i18n.tr("Importieren..."),new UmsatzImport()
    {

      public void handleAction(Object context) throws ApplicationException
      {
        super.handleAction(konto != null ? konto : context);
      }
      
    }
    ,"document-open.png"));
    
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new UmsatzItem(i18n.tr("Löschen..."), new DBObjectDelete(),"user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    
    // BUGZILLA 512 / 1115
    addItem(new UmsatzBookedItem(i18n.tr("Kategorie zuordnen..."),new UmsatzAssign(),"x-office-spreadsheet.png","ALT+K"));
    addItem(new CheckedSingleContextMenuItem(i18n.tr("Kategorie bearbeiten..."),new UmsatzTypNew(),"document-open.png")
    {
      public boolean isEnabledFor(Object o)
      {
        // Wen es ein Umsatz ist, dann nur aktivieren, wenn der Umsatz eine Kategorie hat
        if (o instanceof Umsatz)
        {
          try
          {
            return ((Umsatz)o).getUmsatzTyp() != null;
          }
          catch (RemoteException re)
          {
            Logger.error("unable to check umsatztyp",re);
          }
        }
        
        // Ansonsten wie gehabt
        return super.isEnabledFor(o);
      }
      
    });
    addItem(new ContextMenuItem(i18n.tr("Neue Kategorie anlegen..."),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        // BUGZILLA 926
        UmsatzTyp ut = null;
        if (context != null)
        {
          try
          {
            if (context instanceof Umsatz)
            {
              Umsatz u = (Umsatz) context;
              ut = (UmsatzTyp) Settings.getDBService().createObject(UmsatzTyp.class,null);
              ut.setName(u.getGegenkontoName());
              ut.setPattern(u.getZweck());
            }
            else if (context instanceof UmsatzTyp)
            {
              ut = (UmsatzTyp) Settings.getDBService().createObject(UmsatzTyp.class,null);
              ut.setParent((UmsatzTyp) context);
            }
            else if (context instanceof UmsatzTreeNode)
            {
              ut = (UmsatzTyp) Settings.getDBService().createObject(UmsatzTyp.class,null);
              ut.setParent(((UmsatzTreeNode) context).getUmsatzTyp());
            }
          }
          catch (Exception e)
          {
            Logger.error("error while preparing category",e);
          }
        }
        new UmsatzTypNew().handleAction(ut);
      }
    },"text-x-generic.png"));

    addItem(ContextMenuItem.SEPARATOR);
    addItem(new UmsatzSetAllReadContextMenuItem());
    addItem(new UmsatzSetReadContextMenuItem());
    addItem(new UmsatzSetUnreadContextMenuItem());

    // Wir geben das Context-Menu jetzt noch zur Erweiterung frei.
    ExtensionRegistry.extend(this);

	}

	/**
	 * Erzeugt den Menu-Eintrag fuer eine manuelle Gegenbuchung auf einem Offline-Konto.
	 */
	private void addReverseBookItem()
	{
    try
    {
      final List<Konto> konten = KontoUtil.getKonten(KontoFilter.OFFLINE);
      final int size = konten.size();
      
      // Wir haben gar keine Offline-Konten. Dann brauchen wir auch den Menu-Eintrag nicht.
      if (size == 0)
        return;

      // Genau ein Konto. Dann brauchen wir kein Unter-Menu.
      if (size == 1)
      {
        final Konto k = konten.get(0);
        addItem(new UmsatzItem(i18n.tr("Gegenbuchung erzeugen auf: {0}",KontoUtil.toString(k)),new UmsatzDetailEdit().asReverse(k), "edit-copy.png"));
        return;
      }
      
      // Mehrere Konten. Dann mit Untermenu
      final ContextMenu ctx = new ContextMenu();
      ctx.setText(i18n.tr("Gegenbuchung erzeugen auf..."));
      ctx.setImage(SWTUtil.getImage("edit-copy.png"));
      for (final Konto ko:konten)
      {
        ctx.addItem(new ContextMenuItem(KontoUtil.toString(ko), new UmsatzDetailEdit().asReverse(ko)));
      }
      addMenu(ctx);
    }
    catch (RemoteException e)
    {
      Logger.error("error while creating reverse booking context menu entry",e);
    }
	}

  /**
   * Pruefen, ob es sich wirklich um einen Umsatz handelt.
   */
  private class UmsatzItem extends CheckedContextMenuItem
  {
    /**
     * ct.
     * @param text Label.
     * @param action Action.
     * @param icon optionales Icon.
     */
    public UmsatzItem(String text, Action action, String icon)
    {
      super(text,action,icon);
    }

    /**
     * @see de.willuhn.jameica.gui.parts.CheckedContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if ((o instanceof Umsatz) || (o instanceof Umsatz[]))
        return super.isEnabledFor(o);
      return false;
    }
  }
  
  /**
   * Akzeptiert Umsaetze oder eine einzelne Umsatzgruppe.
   * Die Gruppe allerdings nur, wenn sie direkt Umsaetze enthaelt.
   * Indirekte Umsaetze ueber Unterkategorien sind nicht moeglich.
   */
  private class UmsatzOrGroupItem extends CheckedContextMenuItem
  {
    /**
     * ct.
     * @param text Label.
     * @param action Action.
     * @param icon optionales Icon.
     */
    public UmsatzOrGroupItem(String text, Action action, String icon)
    {
      super(text,action,icon);
    }

    /**
     * @see de.willuhn.jameica.gui.parts.CheckedContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if ((o instanceof Umsatz) || (o instanceof Umsatz[]))
        return super.isEnabledFor(o);
      
      if (o instanceof UmsatzTreeNode)
      {
        UmsatzTreeNode node = (UmsatzTreeNode) o;
        return (node.getUmsaetze().size() > 0 || node.getSubGroups().size() > 0) && super.isEnabledFor(o);
      }
      if (o instanceof UmsatzTreeNode[])
      {
        for (UmsatzTreeNode node:(UmsatzTreeNode[])o)
        {
          if (node.getUmsaetze().size() > 0 || node.getSubGroups().size() > 0)
            return super.isEnabledFor(o);
        }
        return false;
      }
      return false;
    }
  }

  /**
   * Ueberschrieben, um zu pruefen, ob ein Array oder ein einzelnes Element markiert ist.
   */
  private class OpenItem extends UmsatzItem
  {
    private OpenItem()
    {
      super(i18n.tr("Öffnen"),new UmsatzDetail(),"document-open.png");
    }
    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o instanceof Umsatz)
        return super.isEnabledFor(o);
      return false;
    }
  }

  /**
   * @see de.willuhn.jameica.gui.extension.Extendable#getExtendableID()
   */
  public String getExtendableID()
  {
    return this.getClass().getName();
  }

  /**
   * Ueberschrieben, um nur fuer gebuchte Umsaetze zu aktivieren
   */
  private class UmsatzBookedItem extends UmsatzItem
  {
    /**
     * ct.
     * @param text Label.
     * @param action Action.
     * @param icon optionales Icon.
     * @param shortcut Shortcut.
     */
    public UmsatzBookedItem(String text, Action action, String icon, String shortcut)
    {
      super(text,action,icon);
      this.setShortcut(shortcut);
    }
    
    /**
     * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if ((o instanceof Umsatz) || (o instanceof Umsatz[]))
      {
        Umsatz[] umsaetze = null;
        
        if (o instanceof Umsatz)
          umsaetze = new Umsatz[]{(Umsatz) o};
        else
          umsaetze = (Umsatz[]) o;

        try
        {
          for (int i=0;i<umsaetze.length;++i)
          {
            if (umsaetze[i].hasFlag(Umsatz.FLAG_NOTBOOKED))
              return false;
          }
        }
        catch (RemoteException re)
        {
          Logger.error("unable to check for not-booked entries",re);
        }
        return super.isEnabledFor(o);
      }
      return false;
    }
  }
  

}
