/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/updates/update0012.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/10/24 14:24:22 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

import java.io.StringReader;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.HBCIDBService;
import de.willuhn.jameica.hbci.rmi.Lastschrift;
import de.willuhn.jameica.hbci.rmi.SammelLastBuchung;
import de.willuhn.jameica.hbci.rmi.SammelUeberweisungBuchung;
import de.willuhn.jameica.hbci.rmi.Ueberweisung;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.DBSupportH2Impl;
import de.willuhn.jameica.hbci.server.DBSupportMySqlImpl;
import de.willuhn.jameica.hbci.server.HBCIDBServiceImpl;
import de.willuhn.jameica.hbci.server.HBCIUpdateProvider;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.sql.version.Update;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * Verschiebt die erweiterten Verwendungszwecke in die Spalte "zweck3".
 */
public class update0012 implements Update
{
  private Map statements = new HashMap();
  
  /**
   * ct
   */
  public update0012()
  {
    // Update fuer H2
    statements.put(DBSupportH2Impl.class.getName(),
        "ALTER TABLE ueberweisung ADD COLUMN zweck3 VARCHAR(1000) BEFORE termin;\n" +
        "ALTER TABLE umsatz ADD COLUMN zweck3 VARCHAR(1000) BEFORE datum;\n" +
        "ALTER TABLE dauerauftrag ADD COLUMN zweck3 VARCHAR(1000) BEFORE erste_zahlung;\n" +
        "ALTER TABLE lastschrift ADD COLUMN zweck3 VARCHAR(1000) BEFORE termin;\n" +
        "ALTER TABLE slastbuchung ADD COLUMN zweck3 VARCHAR(1000) BEFORE typ;\n" +
        "ALTER TABLE sueberweisungbuchung ADD COLUMN zweck3 VARCHAR(1000) BEFORE typ;\n"
    );

    // Update fuer MySQL
    statements.put(DBSupportMySqlImpl.class.getName(),
        "ALTER TABLE ueberweisung ADD COLUMN zweck3 TEXT AFTER zweck2;\n" +
        "ALTER TABLE umsatz ADD COLUMN zweck3 TEXT AFTER zweck2;\n" +
        "ALTER TABLE dauerauftrag ADD COLUMN zweck3 TEXT AFTER zweck2;\n" +
        "ALTER TABLE lastschrift ADD COLUMN zweck3 TEXT AFTER zweck2;\n" +
        "ALTER TABLE slastbuchung ADD COLUMN zweck3 TEXT AFTER zweck2;\n" +
        "ALTER TABLE sueberweisungbuchung ADD COLUMN zweck3 TEXT AFTER zweck2;\n"
    );
  }

  /**
   * @see de.willuhn.sql.version.Update#execute(de.willuhn.sql.version.UpdateProvider)
   */
  public void execute(UpdateProvider provider) throws ApplicationException
  {
    boolean kontoCheck = Settings.getKontoCheck();
    Settings.setKontoCheck(false);
    
    int maxUsage = HBCIProperties.HBCI_TRANSFER_USAGE_MAXNUM;
    if (maxUsage < 14)
    {
      de.willuhn.jameica.system.Settings s = new de.willuhn.jameica.system.Settings(HBCIProperties.class);
      s.setAttribute("hbci.transfer.usage.maxnum",14);
    }
    
    HBCIUpdateProvider myProvider = (HBCIUpdateProvider) provider;
    I18N i18n = myProvider.getResources().getI18N();

    try
    {
      // Wenn wir eine Tabelle erstellen wollen, muessen wir wissen, welche
      // SQL-Dialekt wir sprechen
      String driver = HBCIDBService.SETTINGS.getString("database.driver",DBSupportH2Impl.class.getName());
      String sql = (String) statements.get(driver);
      if (sql == null)
        throw new ApplicationException(i18n.tr("Datenbank {0} nicht wird unterstützt",driver));

      HBCIDBService service = null;
      try
      {
        //////////////////////////////////////////////////////////////////////////
        // Schritt 1: Neue Spalten anlegen
        Logger.info("update sql tables");
        ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
        myProvider.getProgressMonitor().log(i18n.tr("Tabellen aktualisiert"));
        //////////////////////////////////////////////////////////////////////////
        
        //////////////////////////////////////////////////////////////////////////
        // Schritt 2: Kopieren der bisherigen Zeilen
        Logger.info("copying data");
        myProvider.getProgressMonitor().log(i18n.tr("Kopiere Daten"));
        service = new HBCIDBServiceImpl();
        service.start();
        
        List<Line> lines = (List<Line>) service.execute("select * from verwendungszweck order by typ,auftrag_id,id",null,new ResultSetExtractor() {
          public Object extract(ResultSet rs) throws RemoteException, SQLException
          {
            List<Line> result = new ArrayList<Line>();
            while (rs.next())
            {
              Line line  = new Line();
              line.id    = rs.getInt("auftrag_id");
              line.typ   = rs.getInt("typ");
              line.zweck = rs.getString("zweck");
              result.add(line);
            }
            return result;
          }
        });
        
        List<String> l = new ArrayList<String>();
        int currentType = 0;
        int currentId   = 0;
        for (Line z:lines)
        {
          // Erstes Objekt oder noch das gleiche
          if (currentId == 0 || currentType == 0 || (z.id == currentId && z.typ == currentType))
          {
            if (currentId == 0 || currentType == 0)
            {
              currentId = z.id;
              currentType = z.typ;
            }
            // Zeile sammeln
            l.add(z.zweck);
            continue;
          }

          // Objekt-Wechsel -> flush
          if (l.size() > 0)
          {
            String[] sl = l.toArray(new String[l.size()]);
            
            String s = Integer.toString(currentId);
            Logger.info("copying " + sl.length + " usage lines for type: " + currentType + ", id: " + s);
            for (String value : sl)
              Logger.debug("  " + value);
            
            boolean ausgefuehrt = false;
            
            try
            {
              switch (currentType)
              {
              
                case 1: // Transfer.TYP_UEBERWEISUNG:
                  Ueberweisung tu  = (Ueberweisung) service.createObject(Ueberweisung.class,s);
                  tu.setWeitereVerwendungszwecke(sl);
                  ausgefuehrt = tu.ausgefuehrt();
                  if (ausgefuehrt) ((AbstractDBObject)tu).setAttribute("ausgefuehrt",new Integer(0));
                  tu.store();
                  if (ausgefuehrt) tu.setAusgefuehrt(true);
                  break;
                case 2: // Transfer.TYP_LASTSCHRIFT:
                  Lastschrift tl = (Lastschrift) service.createObject(Lastschrift.class,s);
                  tl.setWeitereVerwendungszwecke(sl);
                  ausgefuehrt = tl.ausgefuehrt();
                  if (ausgefuehrt) ((AbstractDBObject)tl).setAttribute("ausgefuehrt",new Integer(0));
                  tl.store();
                  if (ausgefuehrt) tl.setAusgefuehrt(true);
                  break;
                case 3: // Transfer.TYP_DAUERAUFTRAG:
                  Dauerauftrag td = (Dauerauftrag) service.createObject(Dauerauftrag.class,s);
                  td.setWeitereVerwendungszwecke(sl);
                  td.store();
                  break;
                case 4: // Transfer.TYP_UMSATZ:
                  Umsatz tum = (Umsatz) service.createObject(Umsatz.class,s);
                  tum.setWeitereVerwendungszwecke(sl);
                  tum.store();
                  break;
                case 5: // Transfer.TYP_SUEB_BUCHUNG:
                  SammelUeberweisungBuchung tub = (SammelUeberweisungBuchung) service.createObject(SammelUeberweisungBuchung.class,s);
                  tub.setWeitereVerwendungszwecke(sl);
                  tub.store();
                  break;
                case 6: // Transfer.TYP_SLAST_BUCHUNG:
                  SammelLastBuchung tsb = (SammelLastBuchung) service.createObject(SammelLastBuchung.class,s);
                  tsb.setWeitereVerwendungszwecke(sl);
                  tsb.store();
                  break;
              }
            }
            catch (ObjectNotFoundException onf)
            {
              Logger.warn(onf.getMessage() + ", skipping");
            }
          }
          currentId = 0;
          currentType = 0;
          l.clear();
        }
        //////////////////////////////////////////////////////////////////////////

        //////////////////////////////////////////////////////////////////////////
        // Schritt 3: Tabelle loeschen
        Logger.info("drop table");
        ScriptExecutor.execute(new StringReader("drop table verwendungszweck;\n"),myProvider.getConnection(),myProvider.getProgressMonitor());
        myProvider.getProgressMonitor().log(i18n.tr("Tabellen aktualisiert"));
        //////////////////////////////////////////////////////////////////////////
      }
      catch (ApplicationException ae)
      {
        throw ae;
      }
      catch (Exception e)
      {
        Logger.error("unable to execute update",e);
        throw new ApplicationException(i18n.tr("Fehler beim Ausführen des Updates"),e);
      }
      finally
      {
        if (service != null)
        {
          try
          {
            service.stop(true);
          }
          catch (Exception e)
          {
            Logger.error("error while closing db service",e);
          }
        }
      }
    }
    catch (ApplicationException ae)
    {
      Logger.error("rollback update",ae);
      String sql = "ALTER TABLE ueberweisung DROP zweck3;\n" +
                   "ALTER TABLE umsatz DROP zweck3;\n" +
                   "ALTER TABLE dauerauftrag DROP zweck3;\n" +
                   "ALTER TABLE lastschrift DROP zweck3;\n" +
                   "ALTER TABLE slastbuchung DROP zweck3;\n" +
                   "ALTER TABLE sueberweisungbuchung DROP zweck3;\n";
      try
      {
        ScriptExecutor.execute(new StringReader(sql),myProvider.getConnection(),myProvider.getProgressMonitor());
      }
      catch (Exception e2)
      {
        Logger.error("rollback failed",e2);
      }
      throw ae;
    }
    finally
    {
      Settings.setKontoCheck(kontoCheck);
    }
  }

  /**
   * @see de.willuhn.sql.version.Update#getName()
   */
  public String getName()
  {
    return "Datenbank-Update für erweiterte Verwendungszwecke";
  }
  
  /**
   * Hilfsklasse zum Halten einer Zeile Verwendungszweck.
   */
  private class Line
  {
    private int typ      = 0;
    private int id       = 0;
    private String zweck = null;
  }

}
