/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.security.Wallet;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, der den Inhalt des Wallet anzeigt.
 */
public class WalletDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct
   * @param position
   */
  public WalletDialog(int position)
  {
    super(position);
    this.setTitle(i18n.tr("Inhalt des Wallet"));
    this.setSize(560,400);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Wallet wallet = Settings.getWallet();
    List<Entry> entries = new ArrayList<Entry>();
    Enumeration e = wallet.getKeys();
    while (e.hasMoreElements())
    {
      String key = (String) e.nextElement();
      entries.add(new Entry(key,wallet.get(key)));
    }

    TablePart table = new TablePart(entries,null);
    table.addColumn(i18n.tr("Name"),"name");
    table.addColumn(i18n.tr("Wert"),"value");
    table.setSummary(false);

    Container container = new SimpleContainer(parent);
    container.addText(i18n.tr("Inhalt der Wallet-Datei"),true);

    table.paint(parent);

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Schlieﬂen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    },null,true,"window-close.png");

    buttons.paint(parent);
  }

  /**
   * Hilfsklasse zum Anzeigen der Name-/Wert-Paare 
   */
  public class Entry 
  {
    private String name  = null;
    private String value = null;

    private Entry(String name, Serializable s)
    {
      this.name = name;
      this.value = s != null ? s.toString() : null;
    }

    /**
     * Liefert den Namen.
     * @return der Name.
     */
    public String getName()
    {
      return this.name;
    }

    /**
     * Liefert den Wert.
     * @return der Wert.
     */
    public String getValue()
    {
      return this.value;
    }

  }

}

/*********************************************************************
 * $Log: WalletDialog.java,v $
 * Revision 1.1  2011/08/08 16:02:45  willuhn
 * @N Dialog zum Anzeigen des Wallet-Inhaltes - nur zu Testzwecken
 *
 **********************************************************************/