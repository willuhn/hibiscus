/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/KontoauszugList.java,v $
 * $Revision: 1.8 $
 * $Date: 2007/04/27 15:30:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.hbci.gui.controller.KontoauszugControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Zeigt Kontoauszüge an und gibt gibt sie in eine PDF-Datei aus.
 */
public class KontoauszugList extends AbstractView
{
  private I18N i18n = null;

  /**
   * ct.
   */
  public KontoauszugList()
  {
    super();
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Kontoauszüge"));

    final KontoauszugControl control = new KontoauszugControl(this);
    control.getKontoauszugList().paint(getParent());


    ButtonArea buttons = new ButtonArea(getParent(), 2);
    buttons.addButton(i18n.tr("Zurück"),new Back());
    // TODO
//    buttons.addButton(i18n.tr("Aktualisieren"), new Action()
//    {
//      public void handleAction(Object context) throws ApplicationException
//      {
//        control.handleReload();
//      }
//    },null,true);
    buttons.addButton(i18n.tr("Kontoauszug exportieren..."), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handlePrint();
      }
    });
  }

}

/*******************************************************************************
 * $Log: KontoauszugList.java,v $
 * Revision 1.8  2007/04/27 15:30:44  willuhn
 * @N Kontoauszug-Liste in TablePart verschoben
 *
 * Revision 1.7  2007/04/26 15:02:19  willuhn
 * @N Zusaetzliche Suche nach Gegenkonto
 *
 * Revision 1.6  2007/03/21 16:56:56  willuhn
 * @N Online-Hilfe aktualisiert
 * @N Bug 337 (Stichtag in Sparquote)
 * @C Refactoring in Sparquote
 *
 * Revision 1.5  2007/03/21 15:37:46  willuhn
 * @N Vorschau der Umsaetze in Auswertung "Kontoauszug"
 *
 * Revision 1.4  2006/07/03 23:04:32  willuhn
 * @N PDF-Reportwriter in IO-API gepresst, damit er auch an anderen Stellen (z.Bsp. in der Umsatzliste) mitverwendet werden kann.
 *
 * Revision 1.3  2006/06/19 16:20:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2006/05/15 20:14:51  jost
 * Ausgabe -> PDF-Ausgabe
 * Revision 1.1 2006/05/14 19:53:09 jost
 * Prerelease Kontoauszug-Report Revision 1.4 2006/01/18 00:51:00
 ******************************************************************************/
