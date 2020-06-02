/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.util.Objects;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.hbci.rmi.HibiscusAddress;
import de.willuhn.jameica.hbci.server.HibiscusAddressImpl;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action, der ein Objekt vom Typ HibiscusAddress uebergeben wird.
 * Sie prueft, ob er bereits im Adressbuch existiert. Wenn nicht, legt sie ihn neu an.
 * Falls er existiert, wird der Benutzer gefragt, ob er die Adresse ueberschreiben moechte.
 */
public class HibiscusAddressUpdate implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private HibiscusAddress address = null;
  private boolean create = false;
  
  /**
   * Speichert die Adresse, die auf Veraenderungen geprueft werden soll.
   * @param a Adresse, die auf Veraenderungen geprueft werden soll.
   */
  public void setAddress(Address a)
  {
    if (a instanceof HibiscusAddress) // Checken, ob es ueberhaupt eine Adresse aus dem Adressbuch ist
      this.address = (HibiscusAddress) a;
  }
  
  /**
   * Legt fest, ob die Adresse ggf. neu angelegt werden soll.
   * @param b true, wenn die Adresse ggf. neu angelegt werden soll.
   */
  public void setCreate(boolean b)
  {
    this.create = b;
  }
  
  /**
   * Erwartet ein Objekt vom Typ <code>Address</code>.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    // Wir haben gar keine Adresse als Basis. Dann muessen wir auch nichts vergleichen - Verhalten wie bisher.
    // In dem Fall muss aber die Checkbox "Zum Adressbuch hinzufügen" aktiviert sein. Ansonsten wird gar nichts gemacht.
    if (this.address == null)
    {
      if (this.create)
        new EmpfaengerAdd().handleAction(context);
      
      return;
    }

    // Wir haben keine Daten. Damit haben wir nichts zu pruefen
    if (context == null || !(context instanceof HibiscusAddress))
      return;
    
    final HibiscusAddress newAddress = (HibiscusAddress) context;

    try
    {
      // Wenn der User eine existierende Adresse gewaehlt hat, checken wir auf Aenderungen.
      // Auch dann, wenn die Checkbox "Zum Adressbuch hinzufügen" nicht aktiviert wurde.
  
      // Checken, ob sich ueberhaupt was geaendert hat
      final boolean ibanChanged = this.changed(this.address.getIban(),newAddress.getIban(),false);
      final boolean bicChanged  = this.changed(this.address.getBic(),newAddress.getBic(),false);
      final boolean nameChanged = this.changed(this.address.getName(),newAddress.getName(),true);
      
      // Wenn sich nichts geaendert hat, brauchen wir den User auch nicht fragen, ob wir aktualisieren sollen.
      if (!ibanChanged && !bicChanged && !nameChanged)
        return;
  
      if (!Application.getCallback().askUser(i18n.tr("Die Adresse wurde geändert.\nSollen die Änderungen auch in das Adressbuch übernommen werden?")))
        return;
  
      if (ibanChanged)
        this.address.setIban(newAddress.getIban());
      
      if (bicChanged)
        this.address.setBic(newAddress.getBic());
      
      if (nameChanged)
        this.address.setName(newAddress.getName());
  
      this.address.store();
      
      // Wir muessen in der uebergebenen Adresse (wir wurde ja nur on-the-fly erstellt) die ID der existierenden speichern.
      // Wird beim Aufrufer fuer das Speichern von MetaKey.ADDRESS_ID und der Mandats-Daten verwendet
      if (newAddress instanceof HibiscusAddressImpl)
      {
        HibiscusAddressImpl a = (HibiscusAddressImpl) newAddress;
        a.setID(this.address.getID());
      }
		}
		catch (ApplicationException ae)
		{
			throw ae;
		}
		catch (Exception e)
		{
			Logger.error("error while storing address",e);
			throw new ApplicationException(i18n.tr("Speichern der Adresse fehlgeschlagen: {0}",e.getMessage()));
		}
  }
  
  /**
   * Vergleicht die beiden Werte.
   * @param s1 Wert 1.
   * @param s2 Wert 2.
   * @param exact true, wenn die Werte exakt verglichen werden sollen. False, wenn Gross-/Kleinschreibung und Leerzeichen egal sind.
   * @return true, wenn sie geaendert wurden.
   */
  private boolean changed(String s1, String s2, boolean exact)
  {
    if (exact)
      return !Objects.equals(s1,s2);
    
    return !Objects.equals(this.prepare(s1),this.prepare(s2));
  }
  
  /**
   * Bereitet eine IBAN/BIC auf den Vergleich vor.
   * @param s der String.
   * @return der vorbereitete String.
   */
  private String prepare(String s)
  {
    if (s == null)
      return s;
    
    return s.replace(" ","").toLowerCase();
  }
}
