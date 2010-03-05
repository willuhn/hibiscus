/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/UmsatzTypInput.java,v $
 * $Revision: 1.8 $
 * $Date: 2010/03/05 18:29:26 $
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
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Fertig konfigurierte Auswahlbox fuer die Umsatz-Kategorie.
 */
public class UmsatzTypInput extends SelectInput
{

  private I18N i18n = null;

  /**
   * ct.
   * @param list Liste der Umsatz-Typen.
   * @param typ Filter auf Kategorie-Typen.
   * Kategorien vom Typ "egal" werden grundsaetzlich angezeigt.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @throws RemoteException
   */
  public UmsatzTypInput(DBIterator list, int typ) throws RemoteException
  {
    this(list,null,typ);
  }

  /**
   * ct.
   * @param list Liste der Umsatz-Typen.
   * @param umsatzTyp der vorselektierter Umsatz-Typ.
   * @throws RemoteException
   */
  public UmsatzTypInput(DBIterator list, UmsatzTyp umsatzTyp) throws RemoteException
  {
    this(list,umsatzTyp,UmsatzTyp.TYP_EGAL);
  }

  /**
   * ct.
   * @param list Liste der Umsatz-Typen.
   * @param umsatzTyp der vorselectierte Umsatz-Typ.
   * @param typ Filter auf Kategorie-Typen.
   * Kategorien vom Typ "egal" werden grundsaetzlich angezeigt.
   * @see UmsatzTyp#TYP_AUSGABE
   * @see UmsatzTyp#TYP_EINNAHME
   * @throws RemoteException
   */
  private UmsatzTypInput(DBIterator list, UmsatzTyp umsatzTyp, int typ) throws RemoteException
  {
    super(filter(list,typ), umsatzTyp);

    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
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
   * Haengt an den Iterator ggf noch einen Filter an.
   * @param list der Iterator.
   * @param typ der Kategorie-Typ.
   * @return korrigierte Liste.
   * @throws RemoteException
   */
  private static DBIterator filter(DBIterator list, int typ) throws RemoteException
  {
    if (typ != UmsatzTyp.TYP_EGAL)
      list.addFilter("umsatztyp = " + typ + " or umsatztyp = " + UmsatzTyp.TYP_EGAL + " or umsatztyp is null");
    return list;
  }

  /**
   * @see de.willuhn.jameica.gui.input.SelectInput#format(java.lang.Object)
   */
  protected String format(Object bean)
  {
    String name = super.format(bean);
    return name;
    
// TODO: Einrueckung
//    try
//    {
//      UmsatzTyp t = (UmsatzTyp) bean;
//      int depth = t.getPath().size();
//      for (int i=0;i<depth;++i)
//      {
//        name = "    " + name;
//      }
//    }
//    catch (Exception e)
//    {
//      Logger.error("unable to indent category name",e);
//    }
//    return name;
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
 * Revision 1.8  2010/03/05 18:29:26  willuhn
 * @B Einrueckung nochmal entfernt - das kann dazu fuehren, dass Kinder falsch einsortiert werden (ein einfaches order by parent_id reicht nicht)
 *
 * Revision 1.7  2010/03/05 18:07:26  willuhn
 * @N Unterkategorien in Selectbox einruecken
 *
 * Revision 1.6  2008/08/29 16:46:24  willuhn
 * @N BUGZILLA 616
 *
 * Revision 1.5  2008/08/08 08:57:14  willuhn
 * @N BUGZILLA 614
 *
 * Revision 1.4  2007/04/02 23:01:17  willuhn
 * @D diverse Javadoc-Warnings
 * @C Umstellung auf neues SelectInput
 *
 * Revision 1.3  2007/03/21 18:47:36  willuhn
 * @N Neue Spalte in Kategorie-Tree
 * @N Sortierung des Kontoauszuges wie in Tabelle angezeigt
 * @C Code cleanup
 *
 * Revision 1.2  2007/03/18 08:13:55  jost
 * Sortierte Anzeige der Umsatz-Kategorien.
 *
 * Revision 1.1  2006/11/30 23:48:40  willuhn
 * @N Erste Version der Umsatz-Kategorien drin
 *
 **********************************************************************/