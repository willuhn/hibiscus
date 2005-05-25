/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/action/Attic/OffenerPostenNew.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/25 00:42:04 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.action;

import java.util.Date;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.dialogs.BetragDialog;
import de.willuhn.jameica.hbci.rmi.Adresse;
import de.willuhn.jameica.hbci.rmi.OPPattern;
import de.willuhn.jameica.hbci.rmi.OffenerPosten;
import de.willuhn.jameica.hbci.rmi.filter.Pattern;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action fuer die Detail-Ansicht eines offenen Posten.
 */
public class OffenerPostenNew implements Action
{
  private I18N i18n;

  /**
   * ct.
   */
  public OffenerPostenNew()
  {
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * Erwartet ein Objekt vom Typ <code>OffenerPosten</code> oder <code>Adresse</code> im Context.
   * Bei ersterem wird der Posten geoeffnet, bei letzterem wir ein neuer vorkonfigurierter offener Posten
   * mit dieser Adresse angelegt.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    OffenerPosten o = null;
    if (context instanceof OffenerPosten)
      o = (OffenerPosten) context;
    else if (context instanceof Adresse)
    {
      try
      {
        Double betrag = null;
        try
        {
          BetragDialog d = new BetragDialog(BetragDialog.POSITION_MOUSE);
          d.setTitle(i18n.tr("Erwarteter Betrag"));
          d.setText(i18n.tr("Bitte geben Sie den erwarteten Überweisungsbetrag ein"));
          betrag = (Double) d.open();
        }
        catch (OperationCanceledException oce)
        {
          // Das ist ok
          return;
        }
        catch (Exception e)
        {
          // Dann lassen wir den Betrag halt weg
          Logger.error("error while getting amount from user",e);
        }

        Adresse a = (Adresse) context;
        o = (OffenerPosten) Settings.getDBService().createObject(OffenerPosten.class,null);
        o.setBezeichnung(a.getName() + "[" + HBCI.DATEFORMAT.format(new Date()) + "]");
        o.store();
        
        OPPattern pattern = (OPPattern) Settings.getDBService().createObject(OPPattern.class,null);
        pattern.setOffenerPosten(o);
        pattern.setField("empfaenger_konto");
        pattern.setPattern(a.getKontonummer());
        pattern.setType(Pattern.TYPE_EQUALS);
        pattern.store();

        pattern = (OPPattern) Settings.getDBService().createObject(OPPattern.class,null);
        pattern.setOffenerPosten(o);
        pattern.setField("empfaenger_blz");
        pattern.setPattern(a.getBLZ());
        pattern.setType(Pattern.TYPE_EQUALS);
        pattern.store();

        if (betrag != null)
        {
          pattern = (OPPattern) Settings.getDBService().createObject(OPPattern.class,null);
          pattern.setOffenerPosten(o);
          pattern.setField("betrag");
          pattern.setPattern(betrag.toString());
          pattern.setType(Pattern.TYPE_EQUALS);
          pattern.store();
        }

        GUI.getStatusBar().setSuccessText(i18n.tr("Offener Posten gespeichert"));
      }
      catch (ApplicationException ae)
      {
        throw ae;
      }
      catch (Exception e)
      {
        Logger.error("error while creating OP entry",e);
        throw new ApplicationException(i18n.tr("Fehler beim Anlegen des offenen Postens"),e);
      }
    }
		GUI.startView(de.willuhn.jameica.hbci.gui.views.OffenerPostenNew.class,o);
  }

}


/**********************************************************************
 * $Log: OffenerPostenNew.java,v $
 * Revision 1.1  2005/05/25 00:42:04  web0
 * @N Dialoge fuer OP-Verwaltung
 *
 **********************************************************************/