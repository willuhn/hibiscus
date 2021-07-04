/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.pintan;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.hbci.gui.action.KontoNew;
import de.willuhn.jameica.hbci.gui.menus.KontoList.Style;
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
    super(null,new KontoNew());
    this.setShowFilter(false);
    this.setContextMenu(new de.willuhn.jameica.hbci.gui.menus.KontoList(Style.PASSPORT));
    this.setCheckable(true);
    this.removeFeature(FeatureSummary.class);
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
    ArrayList<Konto> linked = new ArrayList<>();
    GenericIterator configs = PinTanConfigFactory.getConfigs();
    while (configs.hasNext())
    {
      PinTanConfig config = (PinTanConfig) configs.next();
      
      if (this.myConfig != null && this.myConfig.equals(config))
        continue; // Das sind wir selbst

      Konto[] konten = config.getKonten();
      if (konten == null || konten.length == 0)
        continue;
      linked.addAll(Arrays.asList(konten));
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
