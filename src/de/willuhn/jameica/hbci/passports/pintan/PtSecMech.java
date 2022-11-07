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

import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Parser fuer die TAN-Verfahren.
 */
public class PtSecMech
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private String id   = null;
  private String name = null;
  
  /**
   * Parst die Liste der TAN-Verfahren aus dem String.
   * @param text String mit den TAN-Verfahren. 
   * @return die geparste Liste.
   * @throws ApplicationException
   */
  public final static List<PtSecMech> parse(String text) throws ApplicationException
  {
    if (text == null || text.length() == 0)
      throw new ApplicationException(i18n.tr("Keine TAN-Verfahren verfügbar"));


    List<PtSecMech> list = new ArrayList<PtSecMech>();
    String[] lines = text.split("\\|");
    for (String s:lines)
    {
      PtSecMech mech = PtSecMech.create(s);
      if (mech == null)
      {
        Logger.warn("invalid tan mech: " + s + " - skipping");
        continue;
      }
      list.add(mech);
    }
    
    return list;
  }
  
  /**
   * Prueft, ob die ID des angegebenen TAN-Verfahrens in der Liste enthalten ist.
   * @param text der Text mit den TAN-Verfahren.
   * @param id die ID des gesuchten TAN-Verfahrens.
   * @return das gefundene TAN-Verfahren oder NULL, wenn es nicht enthalten ist.
   */
  public static PtSecMech contains(String text, String id)
  {
    if (id == null || text == null)
    {
      Logger.warn("no tan mechs or no id given");
      return null;
    }
    
    try
    {
      List<PtSecMech> list = PtSecMech.parse(text);
      for (PtSecMech p:list)
      {
        if (p.getId().equals(id))
          return p;
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to find tan mech " + id + " in list " + text,e);
      return null;
    }
    
    Logger.warn("tan mech " + id + " not found in list " + text);
    return null;
  }
  
  /**
   * Erzeugt ein PTSechMech-Objekt aus dem Text.
   * Der Text ist fuer gewoehnlich so zusammengesetzt: "{@code <id>:<name>}".
   * @param text der zu parsende Text.
   * @return das PTSechMech-Objekt oder NULL, wenn es kein interpretierbares TAN-Verfahren war.
   */
  public static PtSecMech create(String text)
  {
    if (text == null || text.length() == 0)
      return null;

    int pos = text.indexOf(":");
    if (pos <= 0)
      return null;

    PtSecMech result = new PtSecMech();
    result.id   = text.substring(0,pos);
    result.name = text.substring(pos+1);
    
    return result;
  }

  /**
   * Erzeugt ein PTSechMech-Objekt aus dem Text.
   * Hier wird auch toleriert, wenn nur die Nummer angegeben ist.
   * @param text der Text mit dem TAN-Verfahren.
   * @return das PTSechMech-Objekt oder NULL, wenn kein Text angegeben wurde.
   */
  public static PtSecMech createFailsafe(String text)
  {
    if (text == null || text.length() == 0)
      return null;

    int pos = text.indexOf(":");
    if (pos > 0)
      return create(text);

    PtSecMech result = new PtSecMech();
    result.id   = text;
    result.name = i18n.tr("TAN-Verfahren"); // Dummy-Name
    
    return result;
  }

  /**
   * Liefert die ID des TAN-Verfahrens.
   * @return id die ID des TAN-Verfahrens.
   */
  public String getId()
  {
    return id;
  }

  /**
   * Liefert den Namen des TAN-Verfahrens.
   * @return name Name des TAN-Verfahrens.
   */
  public String getName()
  {
    return name;
  }
  
  /**
   * Liefert den Namen des TAN-Verfahrens mit der ID.
   * @return der Name des TAN-Verfahrens mit der ID.
   */
  public String getLongname()
  {
    return "[" + this.id + "] " + this.name;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (obj == null || !(obj instanceof PtSecMech))
      return false;

    String id = ((PtSecMech)obj).getId();
    return this.id.equals(id);
  }
  
  /**
   * Liefert true, wenn es sich um ein Flicker-Code Verfahren handelt.
   * @return true, wenn es sich um ein Flicker-Code Verfahren handelt.
   */
  public boolean isFlickerCode()
  {
    if (this.name == null)
      return false;

    String s = this.name.toLowerCase();
    return s.contains("usb") ||
           s.contains("optic") ||
           s.contains("optisch") ||
           s.contains("flicker") ||
           s.contains("komfort") ||
           s.contains("comfort") ||
           s.equalsIgnoreCase("chipTAN 1.4");
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return this.id + ":" + this.name;
  }
}
