/**********************************************************************
 *
 * Copyright (c) 2023 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.pintan;

import java.util.ArrayList;
import java.util.List;

import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

import org.apache.commons.lang.StringUtils;
import org.kapott.hbci.smartcardio.ChipTanCardService;
import org.kapott.hbci.smartcardio.SmartCardService;

import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Helfer-Klasse für den Zugriff auf die Smartcards.
 */
public class SmartCardUtil
{
  private final static Class<? extends SmartCardService> TYPE = ChipTanCardService.class;
  
  /**
   * Liefert die Liste der verfügbaren Kartenleser.
   * @return die Liste der verfügbaren Kartenleser. Nie NULL sondern höchstens eine leere Liste.
   */
  public static List<String> getAvailable()
  {
    final List<String> available = new ArrayList<String>();
    try
    {
      TerminalFactory terminalFactory = TerminalFactory.getDefault();
      CardTerminals terminals = terminalFactory != null ? terminalFactory.terminals() : null;
      if (terminals == null)
        return available;
      
      final List<CardTerminal> list = terminals.list();
      if (list != null && list.size() > 0)
      {
        for (CardTerminal t:list)
        {
          String name = StringUtils.trimToNull(t.getName());
          if (name != null)
            available.add(name);
        }
      }
    }
    catch (Throwable t)
    {
      Logger.info("unable to determine card reader list: " + t.getMessage());
      Logger.write(Level.DEBUG,"stacktrace for debugging purpose",t);
    }
    return available;
  }
  
  /**
   * Liefert einen Smartcard-Service für den angegebenen Kartenleser.
   * @param name optionale Angabe des Namens des Kartenlesers. Der Name kann "*" als Wildcard enthalten.
   * In dem Fall wird der erste passende Kartenleser geliefert. 
   * @return der Kartenleser oder NULL, wenn keiner gefunden wurde.
   */
  public static ChipTanCardService getService(String name)
  {
    name = StringUtils.trimToNull(name);
    
    // Kein Name angegeben.
    if (name == null)
      return SmartCardService.createInstance(TYPE,null);

    // Keine Wildcards angegeben - Name muss exakt passen
    if (!name.contains("*"))
      return SmartCardService.createInstance(TYPE,name);
    
    // Wir fangen hier nicht mit Regexen an. Ein einfaches * am Anfang und/oder Ende reicht
    // Andernfalls müssten wir erkennen, ob mit der Bezeichnung eigentlich ein Regex gemeint
    // ist oder der Kartenleser tatsächlich so heisst.
    boolean starts = name.startsWith("*");
    boolean ends   = name.endsWith("*");
    name = name.replace("*","");
    
    String match = null;
    for (String s:getAvailable())
    {
      if (starts && ends && s.contains(name))
      {
        match = s;
        break;
      }
      
      if (starts && s.startsWith(name))
      {
        match = s;
        break;
      }

      if (starts && s.endsWith(name))
      {
        match = s;
        break;
      }
    }

    return match != null ? SmartCardService.createInstance(TYPE,match) : null;
  }
}
