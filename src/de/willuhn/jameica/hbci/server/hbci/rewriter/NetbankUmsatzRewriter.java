/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/hbci/rewriter/NetbankUmsatzRewriter.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/04/25 23:09:04 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server.hbci.rewriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.willuhn.jameica.hbci.rmi.Umsatz;

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
    list.add("20090500");
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
    
    String s1   = u.getZweck();
    String s2   = u.getZweck2();
    String[] s3 = u.getWeitereVerwendungszwecke();
    
    List<String> lines = new ArrayList<String>();
    if (s1 != null && s1.length() > 0) lines.add(s1);
    if (s2 != null && s2.length() > 0) lines.add(s2);
    if (s3 != null && s3.length > 0) lines.addAll(Arrays.asList(s3));
    
    if (lines.size() == 0)
      return; // Kein Verwendungszweck da

    // 1. Zeile = Name
    u.setGegenkontoName(lines.remove(0).trim());

    // haben wir noch was uebrig?
    if (lines.size() == 0)
      return;
    
    // 2. Zeile = 1. Verwendungszweck
    u.setZweck(lines.remove(0).trim());

    // haben wir noch was uebrig?
    if (lines.size() == 0)
      return;

    // 3. Zeile = 2. Verwendungszweck
    u.setZweck2(lines.remove(0).trim());

    // haben wir noch was uebrig?
    if (lines.size() == 0)
      return;
    
    // 4. Zeile ist u.U. das Gegenkonto
    String s = lines.remove(0).trim();
    if (s.matches("^KTO/BLZ [0-9]{3,10}/[0-9]{8}"))
    {
      s = s.replaceAll("KTO/BLZ","").replaceAll("[^0-9/]","");
      String[] sl = s.split("/");
      u.setGegenkontoNummer(sl[0]);
      u.setGegenkontoBLZ(sl[1]);
    }
    
    if (lines.size() == 0)
      return;
    
    u.setWeitereVerwendungszwecke(lines.toArray(new String[lines.size()]));
  }

}



/**********************************************************************
 * $Log: NetbankUmsatzRewriter.java,v $
 * Revision 1.1  2010/04/25 23:09:04  willuhn
 * @N BUGZILLA 244
 *
 **********************************************************************/