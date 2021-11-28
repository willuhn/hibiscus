/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.HibiscusTransfer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.SammelTransfer;
import de.willuhn.jameica.hbci.rmi.SammelTransferBuchung;
import de.willuhn.jameica.hbci.rmi.Terminable;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Container, der eine Liste mit erweiterten Verwendungszwecken anzeigen kann.
 */
public class ErweiterteVerwendungszwecke implements Part
{
  private final static I18N i18n    = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private String[] orig;
  private List<TextInput> fields    = new ArrayList<TextInput>();
  private Button add                = null;
  
  private boolean readonly;
  private Konto konto;

  /**
   * ct.
   * @param transfer der Auftrag.
   * @throws RemoteException
   */
  public ErweiterteVerwendungszwecke(HibiscusTransfer transfer) throws RemoteException
  {
    this.konto    = transfer.getKonto();
    this.readonly = ((transfer instanceof Terminable) && ((Terminable)transfer).ausgefuehrt());
    this.orig     = transfer.getWeitereVerwendungszwecke();
  }

  /**
   * ct.
   * @param buchung die Buchung.
   * @throws RemoteException
   */
  public ErweiterteVerwendungszwecke(SammelTransferBuchung buchung) throws RemoteException
  {
    SammelTransfer tf = buchung.getSammelTransfer();
    this.konto        = tf.getKonto();
    this.readonly     = tf.ausgefuehrt();
    this.orig         = buchung.getWeitereVerwendungszwecke();
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    this.fields.clear();

    final int maxusage = VerwendungszweckUtil.getMaxUsageUeb(konto);

    Settings settings = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getSettings();
    int size = settings.getInt("transfertype.usagelist.size",5);
    if (orig != null && orig.length > size)
      size = orig.length;

    final ScrolledContainer container = new ScrolledContainer(parent);
    for (int i=0;i<size;++i)
    {
      createLine(container,(orig != null && orig.length > i) ? orig[i] : null,i,readonly);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Button zum Hinzufuegen von weiteren Zeilen
    this.add = new Button("  +  ",new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        int size = fields.size();
        createLine(container,null,size,readonly);
        container.update();
        add.setEnabled(!readonly && size+3 < maxusage);
      }
    });
    this.add.setEnabled(!readonly && size+3 <= maxusage);
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(this.add);
    buttons.paint(parent);
    //
    ////////////////////////////////////////////////////////////////////////////

    // einmal initial die Groesse neu berechnen
    container.update();
  }
  
  /**
   * Erzeugt eine neue Zeile Verwendungszweck.
   * @param container der Container, zu dem das Feld hinzugefuegt werden soll.
   * @param text Anzuzeigender Text im Eingabefeld.
   * @param pos Position des Verwendungszwecks.
   */
  private void createLine(Container container, String text, int pos, boolean readonly)
  {
    final TextInput zweck = new TextInput(text,HBCIProperties.HBCI_TRANSFER_USAGE_MAXLENGTH);
    zweck.setEnabled(!readonly);
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
    List<String> list = new ArrayList<String>();
    for (TextInput text:this.fields)
    {
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
    this.orig = list.toArray(new String[list.size()]);
    return this.orig;
  }
}


/**********************************************************************
 * $Log: ErweiterteVerwendungszwecke.java,v $
 * Revision 1.8  2011/08/10 10:46:50  willuhn
 * @N Aenderungen nur an den DA-Eigenschaften zulassen, die gemaess BPD aenderbar sind
 * @R AccountUtil entfernt, Code nach VerwendungszweckUtil verschoben
 * @N Neue Abfrage-Funktion in DBPropertyUtil, um die BPD-Parameter zu Geschaeftsvorfaellen bequemer abfragen zu koennen
 *
 * Revision 1.7  2011-06-01 21:19:16  willuhn
 * @B Scroll-Fixes
 *
 * Revision 1.6  2011-04-29 12:25:36  willuhn
 * @N GUI-Polish
 *
 * Revision 1.5  2008/12/01 23:54:42  willuhn
 * @N BUGZILLA 188 Erweiterte Verwendungszwecke in Exports/Imports und Sammelauftraegen
 *
 * Revision 1.4  2008/11/26 00:39:36  willuhn
 * @N Erste Version erweiterter Verwendungszwecke. Muss dringend noch getestet werden.
 *
 * Revision 1.3  2008/09/16 23:43:32  willuhn
 * @N BPDs fuer Anzahl der moeglichen Zeilen Verwendungszweck auswerten - IN PROGRESS
 *
 * Revision 1.2  2008/05/30 12:02:08  willuhn
 * @N Erster Code fuer erweiterte Verwendungszwecke - NOCH NICHT FREIGESCHALTET!
 *
 * Revision 1.1  2008/02/22 00:52:36  willuhn
 * @N Erste Dialoge fuer erweiterte Verwendungszwecke (noch auskommentiert)
 *
 **********************************************************************/
