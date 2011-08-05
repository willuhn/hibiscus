/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/KontoList.java,v $
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

package de.willuhn.jameica.hbci.passports.pintan;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.hbci.passports.pintan.server.PassportImpl;
import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Vorkonfigurierte Tabelle zur Anzeige der zugeordneten und zuordenbaren Konten zu einer PIN/TAN-Config.
 * BUGZILLA 314
 */
public class KontoList extends de.willuhn.jameica.hbci.gui.parts.KontoList
{
  private PinTanConfig myConfig = null;

  /**
   * ct.
   * @param config die Konfiguration, fuer den die Konten angezeigt werden sollen.
   * @throws RemoteException
   */
  public KontoList(PinTanConfig config) throws RemoteException
  {
    super(PseudoIterator.fromArray(new Konto[0]),new KontoNew());
    this.setCheckable(true);
    this.setSummary(false);
    this.myConfig = config;
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
    GenericIterator configs = PinTanConfigFactory.getConfigs();
    while (configs.hasNext())
    {
      PinTanConfig config = (PinTanConfig) configs.next();
      
      if (this.myConfig != null && this.myConfig.equals(config))
        continue; // Das sind wir selbst

      Konto[] konten = config.getKonten();
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
    // Liste der existierenden Konten mit PIN/TAN ermitteln
    // Davon ziehen wir die bereits verlinkten ab
    ArrayList konten = new ArrayList();
    DBIterator list = de.willuhn.jameica.hbci.Settings.getDBService().createList(Konto.class);
    list.addFilter("passport_class = ?",PassportImpl.class.getName());
    list.setOrder("ORDER BY blz, bezeichnung");
    while (list.hasNext())
    {
      Konto k = (Konto) list.next();
      if (exclude.contains(k) != null)
        continue; // Ist schon mit einer anderen PIN/TAN-Config verlinkt
      konten.add(k);
    }
    /////////////////////////////////////////////////////////////////
    
    /////////////////////////////////////////////////////////////////
    // Tabelle erzeugen und nur die relevanten markieren
    GenericIterator all = PseudoIterator.fromArray((Konto[]) konten.toArray(new Konto[konten.size()]));

    // Die derzeit markierten
    GenericIterator checked = null;
    if (myConfig != null)
    {
      Konto[] k = myConfig.getKonten();
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
 * Revision 1.1  2010/06/17 11:38:15  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.1  2007/08/31 09:43:55  willuhn
 * @N Einer PIN/TAN-Config koennen jetzt mehrere Konten zugeordnet werden
 *
 **********************************************************************/