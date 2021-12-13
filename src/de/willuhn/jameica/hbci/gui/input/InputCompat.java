/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import de.willuhn.jameica.gui.input.Input;
import de.willuhn.logging.Logger;

/**
 * Kompatibilitaetsklasse fuer Inputs.
 */
public final class InputCompat
{

  /**
   * Prueft ob sich der Wert mindestens eines uebergebenen Inputs seit dem letzten Aufruf von
   * hasChanged() der Inputs geaendert hat. 
   * @param hasChanged Ein bekannter Wert fuer den hasChanged()-Status.
   * @param inputs Die Inputs deren hasChanged()-Methode abgefragt werden soll.
   * @return true wenn der Wert mindestens eines Inputs sich seit dem letzten Aufruf von hasChanged()
   *              der Inputs geaendert hat.
   */
  public static boolean valueHasChanged(boolean hasChanged, Input... inputs) {
    // Hier auch: Siehe unten: Keine Optimierung erlaubt. Erst muss "valueHasChanged" fuer alle Inputs durchlaufen werden
    return valueHasChanged(inputs) || hasChanged;
  }
  
  /**
   * Prueft ob sich der Wert mindestens eines uebergebenen Inputs seit dem letzten Aufruf von
   * hasChanged() der Inputs geaendert hat. 
   * @param inputs Die Inputs deren hasChanged()-Methode abgefragt werden soll.
   * @return true wenn der Wert mindestens eines Inputs sich seit dem letzten Aufruf von hasChanged()
   *              der Inputs geaendert hat.
   */
  public static boolean valueHasChanged(Input... inputs)
  {
    if (inputs == null || inputs.length == 0)
      return false;
    
    // Auch wenn fuer den Rueckgabewert nur relevant ist, ob einer davon true ist, muessen
    // dennoch alle durchlaufen werden, da das Input in "hasChanged" den internen Zustand aktualisiert.
    // Ein erneuter Aufruf von "hasChanged" liefert dann wieder so lange false, bis tatsaechlich wieder
    // etwas geaendert wurde.
    
    boolean b = false;
    try
    {
      for (Input i:inputs)
      {
        if (i == null)
          continue;
        
        b |= i.hasChanged();
      }
    }
    catch(Exception e)
    {
      Logger.error("unable to check change status",e);
      return true;
    }
    
    return b;
  }
}
