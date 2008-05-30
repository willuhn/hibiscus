/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/ErweiterteVerwendungszwecke.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/05/30 12:02:08 $
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Container, der eine Liste mit erweiterten Verwendungszwecken anzeigen kann.
 */
public class ErweiterteVerwendungszwecke implements Part
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private String[] orig        = null;
  private boolean readOnly     = false;
  private ArrayList fields     = new ArrayList();
  private Button add           = null;

  /**
   * ct.
   * @param lines die Zeilen Verwendungszweck.
   * @param readOnly true, wenn alle Felder Read-Only sein sollen.
   */
  public ErweiterteVerwendungszwecke(String[] lines, boolean readOnly)
  {
    this.orig = lines;
    this.readOnly = readOnly;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    this.fields.clear();
    
    Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
    int size = settings.getInt("transfertype.usagelist.size",5);
    if (orig != null && orig.length > size)
      size = orig.length;

    final Container container = new ScrolledContainer(parent);
    for (int i=0;i<size;++i)
    {
      createLine(container,(orig != null && orig.length > i) ? orig[i] : null,i);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Button zum Hinzufuegen von weiteren Zeilen
    this.add = new Button("  +  ",new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        int size = fields.size();
        createLine(container,null,size);
        Composite comp = container.getComposite();
        comp.setSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT)); // Groesse neu berechnen, damit der Scrollbalken passt
        add.setEnabled(!readOnly && size+3 < HBCIProperties.HBCI_TRANSFER_USAGE_MAXNUM);
        
      }
    });
    this.add.setEnabled(!this.readOnly && size+3 <= HBCIProperties.HBCI_TRANSFER_USAGE_MAXNUM);
    ButtonArea buttons = new ButtonArea(parent,1);
    buttons.addButton(add);
    //
    ////////////////////////////////////////////////////////////////////////////

    Composite comp = container.getComposite();
    comp.setSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT)); // Groesse neu berechnen
  }
  
  /**
   * Erzeugt eine neue Zeile Verwendungszweck.
   * @param container der Container, zu dem das Feld hinzugefuegt werden soll.
   * @param text Anzuzeigender Text im Eingabefeld.
   * @param pos Position des Verwendungszwecks.
   */
  private void createLine(Container container, String text, int pos)
  {
    final TextInput zweck = new TextInput(text,HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
    zweck.setEnabled(!this.readOnly);
    zweck.setName(i18n.tr("Verwendungszweck {0}","" + (pos+3)));
    zweck.setValidChars(HBCIProperties.HBCI_DTAUS_VALIDCHARS);

    // Das erste Feld ohne Inhalt kriegt den Fokus
    if (!haveFocus && (text == null || text.length() == 0))
    {
      zweck.focus();
      haveFocus = true;
    }
    container.addInput(zweck);
    this.fields.add(zweck);
  }
  
  private boolean haveFocus = false;
  
  /**
   * Liefert eine Liste der eingegebenen Verwendungszwecke.
   * @return Liste der Verwendungszwecke. Nie null sondern hoechstens ein leeres Array.
   * @throws RemoteException
   */
  public String[] getTexts() throws RemoteException
  {
    ArrayList list = new ArrayList();
    
    int size = this.fields.size();
    for (int i=0;i<size;++i)
    {
      TextInput text = (TextInput) this.fields.get(i);
      String value = (String) text.getValue();
      
      // Leere Verwendungszwecke ignorieren
      if (value == null)
        continue;
      value = value.trim();
      if (value.length() == 0)
        continue;
      
      list.add(value);
    }

    // Wir sichern die neuen Zeilen als "orig", damit sie wieder
    // da sind, wenn der Dialog ohne geaenderte Daten nochmal
    // geoeffnet wird.
    this.orig = (String[]) list.toArray(new String[list.size()]);
    return this.orig;
  }
}


/**********************************************************************
 * $Log: ErweiterteVerwendungszwecke.java,v $
 * Revision 1.2  2008/05/30 12:02:08  willuhn
 * @N Erster Code fuer erweiterte Verwendungszwecke - NOCH NICHT FREIGESCHALTET!
 *
 * Revision 1.1  2008/02/22 00:52:36  willuhn
 * @N Erste Dialoge fuer erweiterte Verwendungszwecke (noch auskommentiert)
 *
 **********************************************************************/
