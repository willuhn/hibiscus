/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * GPLv2
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.DBPropertyUtil.HBCITypedProperties;
import de.willuhn.jameica.hbci.server.DBPropertyUtil.Prefix;
import de.willuhn.util.TypedProperties;

/**
 * Hilfsklasse zum Durchsuchen der BPD.
 */
public class BPDUtil
{
  /**
   * Query-Parameter fuer die BPD fuer "SEPA-Dauerauftrag aendern".
   */
  public final static String BPD_QUERY_SEPADAUER_EDIT = "Params%.DauerSEPAEditPar%.ParDauerSEPAEdit.%";

  /**
   * Query-Parameter fuer die BPD fuer "Kontoauszug".
   */
  public final static String BPD_QUERY_KONTOAUSZUG = "Params%.KontoauszugPar%.ParKontoauszug.%";

  /**
   * Query-Parameter fuer die BPD fuer "Kontoauszug PDF".
   */
  public final static String BPD_QUERY_KONTOAUSZUG_PDF = "Params%.KontoauszugPdfPar%.ParKontoauszugPdf.%";
  
  /**
   * Enum fuer vordefinierte Queries von BPD.
   */
  public enum Query
  {
    /**
     * Query fuer die Suche nach den BPD-Parametern fuer die Bearbeitung von Dauerauftraegen.
     */
    DauerEdit("DauerSEPAEdit"),
    
    /**
     * Query fuer die Suche nach den BPD-Parametern fuer den Abruf der Kontoauszuege.
     */
    Kontoauszug("Kontoauszug"),

    /**
     * Query fuer die Suche nach den BPD-Parametern fuer den Abruf der Kontoauszuege im PDF-Format.
     */
    KontoauszugPdf("KontoauszugPdf"),
    
    ;
    
    private String query = null;
    
    /**
     * ct.
     * @param query
     */
    private Query(String query)
    {
      this.query = query;
    }
  }

  /**
   * Liefert die BPD fuer das Konto und den angegebenen Suchfilter.
   * @param konto das Konto.
   * @param query Suchfilter.
   * @return Liste der Properties.
   * Die Schluesselnamen sind um alle Prefixe gekuerzt, enthalten also nur noch den
   * eigentlichen Parameternamen wie etwas "maxusage".
   * Die Funktion liefert nie NULL sondern hoechstens leere Properties.
   * @throws RemoteException
   */
  public static TypedProperties getBPD(Konto konto, final Query query) throws RemoteException
  {
    final TypedProperties props = new HBCITypedProperties();

    // Konto und Query angegeben?
    if (konto == null || query == null)
      return props;
    
    // Kundennummer korrekt?
    String kd = konto.getKundennummer();
    if (kd == null || kd.length() == 0 || !kd.trim().matches("[0-9a-zA-Z]{1,30}"))
      return props;

    final HBCIDBService service = Settings.getDBService();
    
    // Wir haengen noch unseren Prefix mit BPD und Kundennummer vorn dran. Das wurde vom Callback so erfasst
    final String prefix = Prefix.BPD.value() + DBPropertyUtil.SEP + kd.trim() + DBPropertyUtil.SEP;
    
    // Wir ermitteln erstmal die hoechste Segment-Version des Geschaeftsvorfalls
    String q = prefix + "Params_%." + query.query + "Par%.SegHead.version";
    String version = (String) service.execute("select max(content) from property where name like ?",new String[] {q},new ResultSetExtractor()
    {
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        if (rs.next())
          return rs.getString(1);
        
        return null;
      }
    });
    
    // Jetzt suchen wir nach den BPD-Parametern fuer die hoechste Segment-Version
    q = prefix + "Params_%." + query.query + "Par" + (version != null ? version : "%") + ".Par" + query.query + "%";
    service.execute("select name,content from property where name like ? order by name",new String[] {q},new ResultSetExtractor()
    {
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        while (rs.next())
        {
          String name  = rs.getString(1);
          String value = rs.getString(2);

          if (name == null || value == null) continue;
          
          // Wir trimmen noch den Prefix aus dem Namen raus
          name = name.substring(name.lastIndexOf('.')+1);
          props.put(name,value);
        }
        return null;
      }
    });
    
    return props;
  }
}
