/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/parts/KontoList.java,v $
 * $Revision: 1.9 $
 * $Date: 2006/05/11 16:53:09 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.TableItem;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung einer fix und fertig vorkonfigurierten Liste aller Konten.
 */
public class KontoList extends TablePart implements Part
{

  private I18N i18n;

  /**
   * @param action
   * @throws RemoteException
   */
  public KontoList(Action action) throws RemoteException
  {
    this(init(), action);
  }

  /**
   * ct.
   * @param konten
   * @param action
   */
  public KontoList(GenericIterator konten, Action action)
  {
    super(konten,action);
    
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    addColumn(i18n.tr("Kontonummer"),"kontonummer");
    addColumn(i18n.tr("Bankleitzahl"),"blz", new Formatter() {
      public String format(Object o)
      {
        if (o == null)
          return null;
        try
        {
          String blz = o.toString();
          String name = HBCIUtils.getNameForBLZ(blz);
          if (name == null || name.length() == 0)
            return blz;
          return blz + " [" + name + "]";
        }
        catch (Exception e)
        {
          Logger.error("error while formatting blz",e);
          return o.toString();
        }
      }
    });
    addColumn(i18n.tr("Bezeichnung"),"bezeichnung");
    //addColumn(i18n.tr("Kontoinhaber"),"name");
    addColumn(i18n.tr("HBCI-Medium"),"passport_class", new Formatter() {
      public String format(Object o)
      {
        if (o == null || !(o instanceof String))
          return null;
        Passport p;
        try
        {
          p = PassportRegistry.findByClass((String)o);
          return p.getName();
        }
        catch (Exception e)
        {
          Logger.error("error while loading hbci passport for konto",e);
          return i18n.tr("Fehler beim Ermitteln des HBCI-Mediums");
        }
      }
    });
    addColumn(i18n.tr("Saldo"),"saldo");
    setFormatter(new TableFormatter()
    {
      public void format(TableItem item)
      {
        Konto k = (Konto) item.getData();
        try {
          item.setText(4,HBCI.DECIMALFORMAT.format(k.getSaldo()) + " " + k.getWaehrung());
          if (k.getSaldo() < 0)
            item.setForeground(Settings.getBuchungSollForeground());
        }
        catch (RemoteException e)
        {
          Logger.error("error while formatting saldo",e);
        }
      }
    });

    // BUGZILLA 84 http://www.willuhn.de/bugzilla/show_bug.cgi?id=84
    setRememberOrder(true);

    // BUGZILLA 233 http://www.willuhn.de/bugzilla/show_bug.cgi?id=233
    setRememberColWidths(true);

    setContextMenu(new de.willuhn.jameica.hbci.gui.menus.KontoList());
  }
  
  /**
   * Initialisiert die Konten-Liste.
   * @return Liste der Konten.
   * @throws RemoteException
   */
  private static DBIterator init() throws RemoteException
  {
    DBIterator i = Settings.getDBService().createList(Konto.class);
    i.setOrder("ORDER BY blz, bezeichnung");
    return i;
  }

}


/**********************************************************************
 * $Log: KontoList.java,v $
 * Revision 1.9  2006/05/11 16:53:09  willuhn
 * @B bug 233
 *
 * Revision 1.8  2006/04/25 23:25:12  willuhn
 * @N bug 81
 *
 * Revision 1.7  2005/08/01 16:10:41  web0
 * @N synchronize
 *
 * Revision 1.6  2005/06/27 15:35:27  web0
 * @B bug 84
 *
 * Revision 1.5  2005/06/23 22:02:53  web0
 * *** empty log message ***
 *
 * Revision 1.4  2005/06/23 22:01:04  web0
 * @N added hbci media to account list
 *
 * Revision 1.3  2005/06/21 20:11:10  web0
 * @C cvs merge
 *
 * Revision 1.2  2005/05/08 17:48:51  web0
 * @N Bug 56
 *
 * Revision 1.1  2005/05/02 23:56:45  web0
 * @B bug 66, 67
 * @C umsatzliste nach vorn verschoben
 * @C protokoll nach hinten verschoben
 *
 **********************************************************************/