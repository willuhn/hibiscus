/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * GPLv2
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.menus;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungNew;
import de.willuhn.jameica.hbci.gui.action.FlaggableChange;
import de.willuhn.jameica.hbci.gui.action.KontoDelete;
import de.willuhn.jameica.hbci.gui.action.KontoDisable;
import de.willuhn.jameica.hbci.gui.action.KontoExport;
import de.willuhn.jameica.hbci.gui.action.KontoFetchUmsaetze;
import de.willuhn.jameica.hbci.gui.action.KontoImport;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.action.KontoRecalculateOfflineSaldo;
import de.willuhn.jameica.hbci.gui.action.KontoResetAuszugsdatum;
import de.willuhn.jameica.hbci.gui.action.KontoauszugList;
import de.willuhn.jameica.hbci.gui.action.SepaDauerauftragNew;
import de.willuhn.jameica.hbci.gui.action.SepaLastschriftNew;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetailEdit;
import de.willuhn.jameica.hbci.rmi.Flaggable;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Konto-Listen angehaengt
 * werden kann.
 */
public class KontoList extends ContextMenu implements Extendable
{

  private I18N i18n;

  /**
   * Erzeugt ein Kontext-Menu fuer eine Liste von Konten.
   */
  public KontoList()
  {
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    addItem(new CheckedSingleContextMenuItem(i18n.tr("Öffnen"), new KontoNew(),"document-open.png"));
    addItem(new ContextMenuItem(i18n.tr("Neues Konto..."), new KNeu(),"system-file-manager.png"));
    addItem(new CheckedSingleContextMenuItem(i18n.tr("Löschen..."), new KontoDelete(),"user-trash-full.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedSingleContextMenuItem(i18n.tr("Umsätze anzeigen..."),new KontoauszugList(),"text-x-generic.png"));
    addItem(new AccountItem(i18n.tr("Saldo/Umsätze abrufen..."),new KontoFetchUmsaetze(),"mail-send-receive.png"));
    addItem(ContextMenuItem.SEPARATOR);

    addItem(new AccountItem(i18n.tr("Neue Überweisung..."),new AuslandsUeberweisungNew(),"stock_next.png"));
    addItem(new AccountItem(i18n.tr("Neue Lastschrift..."),new SepaLastschriftNew(),"stock_previous.png"));
    addItem(new AccountItem(i18n.tr("Neuer Dauerauftrag..."),new SepaDauerauftragNew(),"stock_form-time-field.png"));
    addItem(new AccountItem(i18n.tr("Umsatz anlegen"),new UmsatzDetailEdit(),"emblem-documents.png").offlineAccount());

    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedContextMenuItem(i18n.tr("Exportieren..."),new KontoExport(),"document-save.png"));
    addItem(new ContextMenuItem(i18n.tr("Importieren..."),new KontoImport(),"document-open.png"));
    addItem(ContextMenuItem.SEPARATOR);
    addMenu(new ExtendedMenu());
  }

  /**
   * Erlaubt die Auswahl des Elements nur fuer Online-Kontos oder nur fuer Offline-Kontos.
   */
  private class AccountItem extends CheckedSingleContextMenuItem
  {
    private boolean offline = false;
    
    /**
     * ct.
     * @param text
     * @param a
     * @param icon
     */
    public AccountItem(String text, Action a, String icon)
    {
      super(text, a, icon);
    }

    /**
     * Erlaubt die Verwendung nur fuer Offline-Konten.
     * @return das modifizierte Objekt.
     */
    public AccountItem offlineAccount()
    {
      this.offline = true;
      return this;
    }
    
    /**
     * @see de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      try
      {
        if (o == null || !(o instanceof Konto) || !super.isEnabledFor(o))
          return false;

        Konto k = (Konto)o;
        return !k.hasFlag(Konto.FLAG_DISABLED) && k.hasFlag(Konto.FLAG_OFFLINE) == offline;
      }
      catch (RemoteException re)
      {
        Logger.error("error while checking flags",re);
        return false;
      }
    }
  }

  /**
   * Ueberschreiben wir, um <b>grundsaetzlich</b> ein neues Konto anzulegen -
   * auch wenn der Focus auf einem existierenden liegt.
   */
  private class KNeu extends KontoNew
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
   * @see de.willuhn.jameica.gui.extension.Extendable#getExtendableID()
   */
  public String getExtendableID()
  {
    return this.getClass().getName();
  }

  /**
   * Das "Erweitert..."-Menu.
   */
  private class ExtendedMenu extends ContextMenu
  {
    /**
     *
     */
    private ExtendedMenu()
    {
      this.setText(i18n.tr("Erweitert"));
      this.setImage(SWTUtil.getImage("emblem-symbolic-link.png"));
      addItem(new CheckedSingleContextMenuItem(i18n.tr("Saldo und Datum zurücksetzen..."), new KontoResetAuszugsdatum(),"edit-undo.png"));
      addItem(new AccountItem(i18n.tr("Salden neu berechnen..."), new KontoRecalculateOfflineSaldo(),"accessories-calculator.png").offlineAccount());
      addItem(new ChangeFlagsMenuItem(i18n.tr("Konto deaktivieren..."), new KontoDisable(),"network-offline.png",false));
      addItem(new ChangeFlagsMenuItem(i18n.tr("Konto aktivieren..."), new FlaggableChange(Konto.FLAG_DISABLED,false),"network-transmit-receive.png",true));
    }
  }

  /**
   * Kontextmenu zum Setzen von Flags fuer das Konto.
   */
  private class ChangeFlagsMenuItem extends CheckedSingleContextMenuItem
  {
    boolean f1 = false;

    /**
     * ct.
     * @param title
     * @param action
     * @param icon
     * @param f1
     */
    private ChangeFlagsMenuItem(String title, Action action, String icon, boolean f1)
    {
      super(title,action,icon);
      this.f1 = f1;
    }

    /**
     * @see de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem#isEnabledFor(java.lang.Object)
     */
    public boolean isEnabledFor(Object o)
    {
      if (o == null || !(o instanceof Flaggable))
        return false;

      try
      {
        boolean f2 = (((Flaggable)o).getFlags() & Konto.FLAG_DISABLED) != 0;

        // Fall 1) Konto ist aktiv und soll deaktiviert werden. f1 = false, f2 = false
        // Fall 2) Konto ist inaktiv und soll aktiviert werden. f1 = true, f1 = true
        // ---> umgekehrtes XOR (XNOR)
        return !(f1 ^ f2) && super.isEnabledFor(o);
      }
      catch (RemoteException re)
      {
        Logger.error("unable to check flags",re);
        return false;
      }
    }

  }

}
