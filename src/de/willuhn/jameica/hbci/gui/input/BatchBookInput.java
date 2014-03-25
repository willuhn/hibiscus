/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.rmi.BatchBookType;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;

/**
 * Auswahlfeld fuer den zu verwendenden Batchbook-Modus.
 */
public class BatchBookInput extends SelectInput
{
  private final static MetaKey KEY = MetaKey.SEPA_BATCHBOOK;
  
  /**
   * @param konto das Konto des Aufrages. Kann null sein.
   * @param object der Auftrag, fuer den der Modus angezeigt werden soll.
   * @throws RemoteException
   */
  public BatchBookInput(Konto konto, HibiscusDBObject object) throws RemoteException
  {
    super(BatchBookType.values(),null);
    this.setName(MetaKey.SEPA_BATCHBOOK.getDescription());
    this.setAttribute("description");
    this.update(konto,object);
  }
  
  /**
   * Aktualisiert die Auswahl basierend auf dem uebergebenen Konto und dem Auftrag.
   * @param konto das Konto des Aufrages. Kann null sein.
   * @param object der Auftrag, fuer den der Modus angezeigt werden soll.
   * @throws RemoteException
   */
  public void update(Konto konto, HibiscusDBObject object) throws RemoteException
  {
    // Checken, ob wir an dem Auftrag bereits einen Batchbook-Mode haben
    BatchBookType type = BatchBookType.byValue(KEY.get(object));
    
    // Ne, dann den vom Konto laden - falls es bekannt ist
    if (type == null && konto != null)
      type = BatchBookType.byValue(KEY.get(konto,object.getClass().getSimpleName()));
    
    // Ne, dann den Default-Wert nehmen
    if (type == null)
      type = BatchBookType.DEFAULT;
    
    this.setValue(type);
  }
  
}
