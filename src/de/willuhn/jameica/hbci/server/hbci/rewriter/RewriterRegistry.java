/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.hbci.rewriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Registry fuer die Rewriter.
 */
public class RewriterRegistry
{
  private final static Settings settings = new Settings(RewriterRegistry.class);
  private static Map<String,Class<UmsatzRewriter>> umsatzRewriters = null;
  
  /**
   * Liefert den Rewriter fuer die BLZ oder NULL wenn keiner existiert.
   * @param blz die BLZ.
   * @param konto optionale Angabe des Konto, damit es selektiv geblacklistet werden kann.
   * @return der Rewriter oder NULL.
   */
  public static synchronized UmsatzRewriter getRewriter(String blz, String konto)
  {
    if (blz == null)
      return null;
    
    // checken, ob die BLZ auf der Blacklist steht
    if (settings.getBoolean("blacklist.blz." + blz,false))
    {
      Logger.info("BLZ blacklisted for rewriters, skip rewriting");
      return null;
    }
    
    // checken, ob das Konto auf der Blacklist steht 
    if (konto != null && settings.getBoolean("blacklist.konto." + konto,false))
    {
      Logger.info("Konto blacklisted for rewriters, skip rewriting");
      return null;
    }
    
    BeanService service = Application.getBootLoader().getBootable(BeanService.class);

    if (umsatzRewriters == null)
    {
      umsatzRewriters = new HashMap<String,Class<UmsatzRewriter>>();
      try
      {
        ClassFinder finder = Application.getPluginLoader().getManifest(HBCI.class).getClassLoader().getClassFinder();
        Class<UmsatzRewriter>[] classes = finder.findImplementors(UmsatzRewriter.class);
        for (Class<UmsatzRewriter> c:classes)
        {
          try
          {
            UmsatzRewriter u = service.get(c);
            List<String> blzList = u.getBlzList();
            for (String s:blzList)
              umsatzRewriters.put(s,c);
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
    
    Class<UmsatzRewriter> c = umsatzRewriters.get(blz);
    if (c == null)
      return null;
    
    return service.get(c);
  }
}



/**********************************************************************
 * $Log: RewriterRegistry.java,v $
 * Revision 1.3  2012/03/28 22:47:18  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.2  2012/02/26 14:07:51  willuhn
 * @N Lifecycle-Management via BeanService
 *
 * Revision 1.1  2010/04/25 23:09:04  willuhn
 * @N BUGZILLA 244
 *
 **********************************************************************/