/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/rewriter/NetbankUmsatzRewriter.java,v $
 * $Revision: 1.7 $
 * $Date: 2011/06/17 15:22:50 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.hbci.rewriter;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;

/**
 * Implementierung des Rewriters fuer die Netbank.
 * BUGZILLA 244
 */
public class NetbankUmsatzRewriter implements UmsatzRewriter
{
  /**
   * @see de.willuhn.jameica.hbci.server.hbci.rewriter.UmsatzRewriter#getBlzList()
   */
  public List<String> getBlzList()
  {
    List<String> list = new ArrayList<String>();
    list.add("20090500"); // Netbank
    list.add("60090800"); // SpardaBank BW
    list.add("33060592"); // SpardaBank West - siehe https://www.willuhn.de/blog/index.php?url=archives/519-Neues-in-Hibiscus.html&serendipity[csuccess]=true#c1146
    list.add("12096597"); // SpardaBank Berlin - https://www.willuhn.de/bugzilla/show_bug.cgi?id=887#c14
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
    
    // Alte Verwendungszwecke erstmal loeschen
    u.setZweck(null);
    u.setZweck2(null);
    u.setWeitereVerwendungszwecke(null);

    // 1. Zeile = Name
    u.setGegenkontoName(lines.remove(0).trim());
    if (lines.size() == 0) return; // haben wir noch was uebrig?

    // Die letzte Zeile ist nach bisherigen Erkenntnissen immer
    // das Gegenkonto
    if (applyGegenkonto(u,lines.get(lines.size()-1).trim()))
    {
      // Jepp, konnte als Gegenkonto geparst werden.
      // Dann abschneiden
      lines.remove(lines.size()-1);
      if (lines.size() == 0) return; // haben wir noch was uebrig?
    }

    // 1. Verwendungszweck
    u.setZweck(lines.remove(0).trim());
    if (lines.size() == 0) return; // haben wir noch was uebrig?

    // 2. Verwendungszweck
    u.setZweck2(lines.remove(0).trim());
    if (lines.size() == 0) return; // haben wir noch was uebrig?

    // 3. weitere Verwendungszwecke
    u.setWeitereVerwendungszwecke(lines.toArray(new String[lines.size()]));
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
    if (!gk.matches("^KTO/BLZ [0-9]{3,10}/[0-9]{8}"))
      return false;
    
    gk = gk.replaceAll("KTO/BLZ","").replaceAll("[^0-9/]","");
    String[] sl = gk.split("/");
    u.setGegenkontoNummer(sl[0]);
    u.setGegenkontoBLZ(sl[1]);
    return true;
  }
}



/**********************************************************************
 * $Log: NetbankUmsatzRewriter.java,v $
 * Revision 1.7  2011/06/17 15:22:50  willuhn
 * @N added 12096597
 *
 * Revision 1.6  2011-06-07 10:07:51  willuhn
 * @C Verwendungszweck-Handling vereinheitlicht/vereinfacht - geht jetzt fast ueberall ueber VerwendungszweckUtil
 *
 * Revision 1.5  2010-09-08 15:31:46  willuhn
 * @N Spardabank West
 *
 * Revision 1.4  2010/05/11 10:31:56  willuhn
 * @N Siehe Mail von Markus vom 11.05.2010
 *
 * Revision 1.3  2010/04/29 09:28:12  willuhn
 * @B BUGZILLA 244
 *
 * Revision 1.2  2010/04/26 08:35:29  willuhn
 * @N BUGZILLA 244
 *
 * Revision 1.1  2010/04/25 23:09:04  willuhn
 * @N BUGZILLA 244
 *
 **********************************************************************/