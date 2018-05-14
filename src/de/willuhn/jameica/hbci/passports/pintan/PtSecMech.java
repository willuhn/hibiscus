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
      try
      {
        list.add(new PtSecMech(s));
      }
      catch (Exception e)
      {
        Logger.error("invalid tan mech: " + s + " - skipping",e);
      }
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
   * ct.
   * @param text
   * @throws Exception
   */
  private PtSecMech(String text) throws Exception
  {
    if (text == null || text.length() == 0 || text.indexOf(":") == -1)
      throw new Exception();
      
    String[] s = text.split(":");

    if (s[0] == null || s[0].length() == 0 ||
        s[1] == null || s[1].length() == 0)
      throw new Exception();

    this.id   = s[0];
    this.name = s[1];
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
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj)
  {
    if (obj == null || !(obj instanceof PtSecMech))
      return false;

    String id = ((PtSecMech)obj).getId();
    return this.id.equals(id);
  }

  
  
}


/*********************************************************************
 * $Log: PtSecMech.java,v $
 * Revision 1.1  2010/12/15 13:17:25  willuhn
 * @N Code zum Parsen der TAN-Verfahren in PtSecMech ausgelagert. Wenn ein TAN-Verfahren aus Vorauswahl abgespeichert wurde, wird es nun nur noch dann automatisch verwendet, wenn es in der aktuellen Liste der TAN-Verfahren noch enthalten ist. Siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?t=12545
 *
 **********************************************************************/