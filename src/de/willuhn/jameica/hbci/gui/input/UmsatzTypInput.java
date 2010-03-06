/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/UmsatzTypInput.java,v $
 * $Revision: 1.12 $
 * $Date: 2010/03/06 00:03:23 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.Calendar;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.UmsatzTypUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Fertig konfigurierte Auswahlbox fuer die Umsatz-Kategorie.
 */
public class UmsatzTypInput extends SelectInput
{

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * ct.
   * @param preselected der vorselectierte Umsatz-Typ.
   * @param typ Filter auf Kategorie-Typen.
   * Kategorien vom Typ "egal" werden grundsaetzlich angezeigt.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @throws RemoteException
   */
  public UmsatzTypInput(UmsatzTyp preselected, int typ) throws RemoteException
  {
    super(init(typ), preselected);

    this.setPleaseChoose(i18n.tr("<Keine Kategorie>"));
    refreshComment();
    
    // Betrag aktualisieren
    this.addListener(new Listener() {
    
      public void handleEvent(Event event)
      {
        refreshComment();
      }
    
    });
  }
  
  /**
   * Initialisiert die Liste der anzuzeigenden Kategorien.
   * @param typ der Kategorie-Typ.
   * @return korrigierte Liste.
   * @throws RemoteException
   */
  private static DBIterator init(int typ) throws RemoteException
  {
    // TODO: Die Selectbox zeigt derzeit eine platte Liste aller Kategorien an.
    // Der User kann hier die Ordner-Struktur der Kategorien nicht erkennen.
    // Statt dem DBIterator sollte zuerst nur die Liste der Root-Elemente geladen
    // werden (UmsatzTypUtil.getRootElements()). Anschliessend sollte 
    // ueber jedes Element iteriert und rekursiv die Kinder angehaengt werden.
    // Dann sind sie erstmal in der richtigen Reihenfolge. Anschliessend sollte
    // die format(Object)-Funktion hier ueberschrieben werden, um abhaengig von
    // der Pfad-Tiefe einzuruecken.
    //
    // Die ganze Sache wuerde aber etliche SQL-Statements ausloesen. Das waere
    // alles andere als performant. Stattdessen koennte man im UmsatzTypAuswahlDialog
    // auch direkt den Tree anzeigen. Dann bleibt aber immer noch die Kategorie-
    // Auswahl in der Umsatz-Detail-Ansicht offen. Dort kann der Tree nicht
    // eingeblendet werden.
    
    DBIterator list = UmsatzTypUtil.getAll();
    if (typ != UmsatzTyp.TYP_EGAL)
      list.addFilter("umsatztyp = " + typ + " or umsatztyp = " + UmsatzTyp.TYP_EGAL + " or umsatztyp is null");
    return list;
  }

  /**
   * Aktualisiert den Kommentar.
   */
  private void refreshComment()
  {
    try
    {
      UmsatzTyp ut = (UmsatzTyp) getValue();
      if (ut == null)
      {
        setComment("");
        return;
      }
      
      Calendar cal = Calendar.getInstance();
      setComment(i18n.tr("Umsatz im laufenden Monat: {0} {1}", new String[]{HBCI.DECIMALFORMAT.format(ut.getUmsatz(cal.get(Calendar.DAY_OF_MONTH))), HBCIProperties.CURRENCY_DEFAULT_DE}));
    }
    catch (Exception e)
    {
      Logger.error("unable to refresh umsatz",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Aktualisieren des Umsatzes"), StatusBarMessage.TYPE_ERROR));
    }
  }
}


/*********************************************************************
 * $Log: UmsatzTypInput.java,v $
 * Revision 1.12  2010/03/06 00:03:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2010/03/05 23:59:31  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.10  2010/03/05 23:52:27  willuhn
 * @C Code-Cleanup
 * @C Liste der Kategorien kann jetzt nicht mehr von aussen an UmsatzTypInput uebergeben werden
 *
 * Revision 1.9  2010/03/05 23:29:18  willuhn
 * @N Statische Basis-Funktion zum Laden der Kategorien in der richtigen Reihenfolge
 *
 * Revision 1.8  2010/03/05 18:29:26  willuhn
 * @B Einrueckung nochmal entfernt - das kann dazu fuehren, dass Kinder falsch einsortiert werden (ein einfaches order by parent_id reicht nicht)
 *
 * Revision 1.7  2010/03/05 18:07:26  willuhn
 * @N Unterkategorien in Selectbox einruecken
 **********************************************************************/