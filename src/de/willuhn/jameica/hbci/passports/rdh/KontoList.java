/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/KontoList.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/08/05 11:21:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.passports.rdh.rmi.RDHKey;
import de.willuhn.jameica.hbci.passports.rdh.server.PassportImpl;
import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Vorkonfigurierte Tabelle zur Anzeige der zugeordneten und zuordenbaren Konten zu einem Schluessel.
 * BUGZILLA 314
 */
public class KontoList extends de.willuhn.jameica.hbci.gui.parts.KontoList
{
  private RDHKey myKey = null;

  /**
   * ct.
   * @param key der Schluessel, fuer den die Konten angezeigt werden sollen.
   * @throws RemoteException
   */
  public KontoList(RDHKey key) throws RemoteException
  {
    super(PseudoIterator.fromArray(new Konto[0]),new KontoNew());
    this.setCheckable(true);
    this.setSummary(false);
    this.myKey = key;
  }

  /**
   * @see de.willuhn.jameica.gui.parts.TablePart#paint(org.eclipse.swt.widgets.Composite)
   */
  public synchronized void paint(Composite parent) throws RemoteException
  {
    // Erst das Parent zeichnen, damit wir anschliessend die
    // Konten checkable machen koennen.
    super.paint(parent);
    
    /////////////////////////////////////////////////////////////////
    // Wir ermitteln die Liste der bereits verlinkten Konten
    ArrayList linked = new ArrayList();
    GenericIterator keys = RDHKeyFactory.getKeys();
    while (keys.hasNext())
    {
      RDHKey key = (RDHKey) keys.next();
      if (!key.isEnabled())
        continue;
      
      if (this.myKey != null && this.myKey.equals(key))
        continue; // Das sind wir selbst

      Konto[] konten = key.getKonten();
      if (konten == null || konten.length == 0)
        continue;
      for (int i=0;i<konten.length;++i)
      {
        linked.add(konten[i]);
      }
    }
    GenericIterator exclude = PseudoIterator.fromArray((GenericObject[])linked.toArray(new GenericObject[linked.size()]));
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Liste der existierenden Konten mit Schluesseldiskette ermitteln
    // Davon ziehen wir die bereits verlinkten ab
    ArrayList konten = new ArrayList();
    DBIterator list = de.willuhn.jameica.hbci.Settings.getDBService().createList(Konto.class);
    list.addFilter("passport_class = ?",PassportImpl.class.getName());
    list.setOrder("ORDER BY blz, bezeichnung");
    while (list.hasNext())
    {
      Konto k = (Konto) list.next();
      if (exclude.contains(k) != null)
        continue; // Ist schon mit einer anderen Diskette verlinkt
      konten.add(k);
    }
    /////////////////////////////////////////////////////////////////
    
    /////////////////////////////////////////////////////////////////
    // Tabelle erzeugen und nur die relevanten markieren
    GenericIterator all = PseudoIterator.fromArray((Konto[]) konten.toArray(new Konto[konten.size()]));

    // Die derzeit markierten
    GenericIterator checked = null;
    if (myKey != null)
    {
      Konto[] k = myKey.getKonten();
      if (k != null && k.length > 0)
        checked = PseudoIterator.fromArray(k);
    }

    while (all.hasNext())
    {
      Konto k = (Konto) all.next();
      this.addItem(k,checked != null && (checked.contains(k) != null));
    }
    /////////////////////////////////////////////////////////////////
  }

}


/*********************************************************************
 * $Log: KontoList.java,v $
 * Revision 1.3  2011/08/05 11:21:59  willuhn
 * @N Erster Code fuer eine Umsatz-Preview
 * @C Compiler-Warnings
 * @N DateFromInput/DateToInput - damit sind die Felder fuer den Zeitraum jetzt ueberall einheitlich
 *
 * Revision 1.2  2010-09-07 15:17:07  willuhn
 * @N GUI-Cleanup
 *
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.1  2007/05/30 14:48:50  willuhn
 * @N Bug 314
 *
 **********************************************************************/