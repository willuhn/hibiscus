/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/ErweiterteVerwendungszwecke.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/02/22 00:52:36 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.rmi.Verwendungszweck;
import de.willuhn.jameica.plugin.PluginResources;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.I18N;


/**
 * Container, der eine Liste mit erweiterten Verwendungszwecken anzeigen kann.
 */
public class ErweiterteVerwendungszwecke implements Part
{
  private Transfer transfer = null;
  private ArrayList fields  = new ArrayList();

  /**
   * ct.
   * @param transfer Transfer, zu dem die Verwendungszwecke angezeigt werden sollen.
   */
  public ErweiterteVerwendungszwecke(Transfer transfer)
  {
    this.transfer = transfer;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    this.fields.clear();
    
    PluginResources res = Application.getPluginLoader().getPlugin(HBCI.class).getResources();
    Settings settings   = res.getSettings();
    I18N i18n           = res.getI18N();
    
    int size = settings.getInt("transfertype." + transfer.getTransferTyp() + ".usagelist.size",3);
    GenericIterator list = this.transfer.getWeitereVerwendungszwecke();
    
    if (list != null && list.size() > size)
      size = list.size();
    
    
    // TODO !EVZ
    // Hier sollte man noch eine Funktion einbauen, damit on-the-fly
    // weitere Zeilen hinzugefuegt werden, wenn alle ausgefuellt sind!
    Container container = new SimpleContainer(parent);
    
    for (int i=0;i<size;++i)
    {
      String text = null;
      if (list != null && list.hasNext())
      {
        Verwendungszweck z = (Verwendungszweck) list.next();
        text = z.getText();
      }
      final TextInput zweck = new TextInput(text,HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
      zweck.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);
      container.addLabelPair(i18n.tr("Verwendungszweck {0}","" + (i+3)),zweck);
      
      this.fields.add(zweck);
    }
  }
  
  /**
   * Liefert eine Liste der eingegebenen Verwendungszwecke.
   * @return Liste der Verwendungszwecke.
   * @throws RemoteException
   */
  public String[] getTexts() throws RemoteException
  {
    int size = this.fields.size();
    
    if (size == 0)
    {
      // Die Liste wurde entweder nicht angezeigt (z.Bsp. weil
      // es fuer diesen Transfer-Typ noch nicht implementiert ist)
      // oder das Objekt enthaelt keine erweiterten Verwendungszwecke
      // In beiden Faellen liefern wir die originalen Werte
      // zurueck, denn sie konnten vom User definitiv nicht
      // geaendert werden.
      GenericIterator orig = this.transfer.getWeitereVerwendungszwecke();
      if (orig == null || orig.size() == 0)
        return new String[0];

      String[] list = new String[orig.size()];
      int i = 0;
      while (orig.hasNext())
      {
        Verwendungszweck z = (Verwendungszweck) orig.next();
        list[i++] = z.getText();
      }
      return list;

    }
    
    String[] list = new String[size];
    for (int i = 0;i<size;++i)
    {
      TextInput text = (TextInput) this.fields.get(i);
      list[i] = (String) text.getValue();
    }
    return list;
  }
}


/**********************************************************************
 * $Log: ErweiterteVerwendungszwecke.java,v $
 * Revision 1.1  2008/02/22 00:52:36  willuhn
 * @N Erste Dialoge fuer erweiterte Verwendungszwecke (noch auskommentiert)
 *
 **********************************************************************/
