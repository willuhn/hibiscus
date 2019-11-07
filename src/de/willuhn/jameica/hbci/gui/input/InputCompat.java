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
   * @param hasChanged Ein bekannter Wert fuer der hasChanged()-Status.
   * @param inputs Die Inputs deren hasChanged()-Methode abgefragt werden soll.
   * @return true wenn der Wert mindestens eines Inputs sich seit dem letzten Aufruf von hasChanged()
   *              der Inputs geaendert hat.
   */
  public static boolean valueHasChanged(boolean hasChanged, Input... inputs) {
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
    boolean result = false;
    
    try
    {
      for(Input i : inputs)
      {
        if(i != null)
        {
          result = i.hasChanged() || result;
        }
      }
    } catch(Exception e)
    {
      Logger.error("unable to check change status",e);
      result = true;
    }
    
    return result;
  }
}
