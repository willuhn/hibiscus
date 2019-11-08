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
    return hasChanged || valueHasChanged(inputs);
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
    
    try
    {
      for (Input i:inputs)
      {
        if (i == null)
          continue;
        
        if (i.hasChanged())
          return true;
      }
    }
    catch(Exception e)
    {
      Logger.error("unable to check change status",e);
      return true;
    }
    
    return false;
  }
}
