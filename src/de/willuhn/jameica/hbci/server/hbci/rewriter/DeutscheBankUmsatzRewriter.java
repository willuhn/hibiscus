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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;

/**
 * Implementierung des Rewriters fuer die Deutsche Bank.
 * BUGZILLA 887
 */
@Lifecycle(Type.CONTEXT)
public class DeutscheBankUmsatzRewriter implements UmsatzRewriter
{
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.rewriter.UmsatzRewriter#getBlzList()
   */
  public List<String> getBlzList()
  {
    List<String> list = new ArrayList<String>();
    list.add("50070024");
    list.add("10070024"); // Deutsche Bank Berlin
    list.add("60070070");
    list.add("62070081"); // Deutsche Bank Heilbronn
    list.add("10070848"); // Berliner Bank - ist eine Marke der Deutschen Bank
    list.add("26570090"); // Deutsche Bank Osnabrueck - siehe http://www.willuhn.de/blog/index.php?/archives/519-Neues-in-Hibiscus.html#c1541
    list.add("25770024"); // Deutsche Bank Celle - siehe Mail von Carsten vom 15.07.2012
    list.add("54570024"); // Deutsche Bank Ludwigshafen - siehe http://www.willuhn.de/blog/index.php?/archives/519-Neues-in-Hibiscus.html#c1801
    list.add("76026000"); // Norisbank - laut http://de.wikipedia.org/wiki/Norisbank eine Tochter der Deutschen Bank, siehe auch https://www.willuhn.de/bugzilla/show_bug.cgi?id=887#c55
    list.add("10077777"); // Norisbank - Filialnr. 431 - siehe http://www.willuhn.de/wiki/doku.php?id=support:list:banken:misc:pintan#norisbank
    return list;
  }

  /**
   * @see de.willuhn.jameica.hbci.server.hbci.rewriter.UmsatzRewriter#rewrite(de.willuhn.jameica.hbci.rmi.Umsatz)
   */
  public void rewrite(Umsatz u) throws Exception
  {
    String name = u.getGegenkontoName();
    if (name != null && name.length() > 0)
      return; // Steht schon was drin

    String kto  = u.getGegenkontoNummer();
    if (kto != null && kto.length() > 0)
      return; // Steht schon was drin
    
    String blz  = u.getGegenkontoBLZ();
    if (blz != null && blz.length() > 0)
      return; // Steht schon was drin
    
    String[] s = VerwendungszweckUtil.toArray(u);
    List<String> lines = new ArrayList<String>();
    lines.addAll(Arrays.asList(s));
    
    if (lines.size() == 0)
      return; // Kein Verwendungszweck da
    
    for (int i=0;i<lines.size();++i)
    {
      if (applyGegenkonto(u,lines.get(i)))
      {
        // Gegenkonto wurde gefunden. Dann ignorieren wir die erste Zeile
        // und uebernehmen die Zeile 1 als Gegenkonto-Inhaber und die
        // Zeilen 2 - (i-1) als Verwendungszweck

        // Alte Verwendungszwecke erstmal loeschen
        u.setZweck(null);
        u.setZweck2(null);
        u.setWeitereVerwendungszwecke(null);
        
        if (lines.size() < 2)
          return;

        // Gegenkonto-Inhaber
        u.setGegenkontoName(lines.get(1).trim());
        
        if (i >= 2)
        {
          List<String> list = new ArrayList<String>(lines.subList(2,i));
          if (list.size() == 0) return; // haben wir noch was uebrig?

          // 1. Verwendungszweck
          u.setZweck(list.remove(0).trim());
          if (list.size() == 0) return; // haben wir noch was uebrig?

          // 2. Verwendungszweck
          u.setZweck2(list.remove(0).trim());
          if (list.size() == 0) return; // haben wir noch was uebrig?

          // 3. weitere Verwendungszwecke
          u.setWeitereVerwendungszwecke(list.toArray(new String[list.size()]));
        }
      }
    }
  }
  
  /**
   * Versucht, aus der uebergebenen Verwendungszweck-Zeile Gegenkonto/BLZ zu parsen
   * und dem Umsatz zuzuordnen.
   * @param u der Umsatz
   * @param s der zu parsende Text.
   * @throws RemoteException
   */
  private boolean applyGegenkonto(Umsatz u, String s) throws RemoteException
  {
    if (s == null || s.length() == 0 || u == null)
      return false;
    
    String gk = s.trim();
    if (!gk.matches("^KTO([ ]{1,7})[0-9]{3,10} BLZ [0-9]{8}"))
      return false;
    
    gk = gk.replaceAll("[a-zA-Z]","").trim();
    String[] sl = gk.split(" {1,7}");
    if (sl.length != 2)
      return false;
    u.setGegenkontoNummer(sl[0].trim());
    u.setGegenkontoBLZ(sl[1].trim());
    return true;
  }
}



/**********************************************************************
 * $Log: DeutscheBankUmsatzRewriter.java,v $
 * Revision 1.8  2012/05/29 19:53:40  willuhn
 * @N BLZ 26570090 (Deutsche Bank Osnabrück zu Rewriter hinzugefügt. Siehe http://www.willuhn.de/blog/index.php?/archives/519-Neues-in-Hibiscus.html#c1541
 *
 * Revision 1.7  2012/02/26 14:07:51  willuhn
 * @N Lifecycle-Management via BeanService
 *
 * Revision 1.6  2011-06-22 13:26:04  willuhn
 * @N Berliner Bank
 *
 * Revision 1.5  2011-06-07 10:07:51  willuhn
 * @C Verwendungszweck-Handling vereinheitlicht/vereinfacht - geht jetzt fast ueberall ueber VerwendungszweckUtil
 *
 * Revision 1.4  2011-01-24 10:10:31  willuhn
 * @N Deutsche Bank Heilbronn
 *
 * Revision 1.3  2011-01-12 23:07:19  willuhn
 * @N BUGZILLA 887
 *
 * Revision 1.2  2010-11-18 10:22:08  willuhn
 * @N Siehe Mail von Axel vom 18.11.2010
 *
 * Revision 1.1  2010-08-02 09:02:23  willuhn
 * @N BUGZILLA 887
 *
 **********************************************************************/