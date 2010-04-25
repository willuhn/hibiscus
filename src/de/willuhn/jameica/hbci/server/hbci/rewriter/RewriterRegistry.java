/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/rewriter/RewriterRegistry.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/04/25 23:09:04 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.hbci.rewriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Registry fuer die Rewriter.
 */
public class RewriterRegistry
{
  private static Map<String,UmsatzRewriter> umsatzRewriters = null;
  
  /**
   * Liefert den Rewriter fuer die BLZ oder NULL wenn keiner existiert.
   * @param blz die BLZ.
   * @return der Rewriter oder NULL.
   */
  public static synchronized UmsatzRewriter getRewriter(String blz)
  {
    if (blz == null)
      return null;
    
    if (umsatzRewriters == null)
    {
      umsatzRewriters = new HashMap<String,UmsatzRewriter>();
      try
      {
        ClassFinder finder = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getClassLoader().getClassFinder();
        Class<UmsatzRewriter>[] classes = finder.findImplementors(UmsatzRewriter.class);
        for (Class<UmsatzRewriter> c:classes)
        {
          try
          {
            UmsatzRewriter u = c.newInstance();
            List<String> blzList = u.getBlzList();
            for (String s:blzList)
              umsatzRewriters.put(s,u);
          }
          catch (Exception e)
          {
            Logger.error("unable to load rewriter " + c.getName() + ", skipping",e);
          }
        }
      }
      catch (Exception e)
      {
        Logger.warn("no umsatz rewriters found");
      }
    }
    return umsatzRewriters.get(blz);
  }
}



/**********************************************************************
 * $Log: RewriterRegistry.java,v $
 * Revision 1.1  2010/04/25 23:09:04  willuhn
 * @N BUGZILLA 244
 *
 **********************************************************************/