/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/SparQuote.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/07/13 00:21:15 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * View zur Anzeige der Sparquote.
 */
public class SparQuote extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    GUI.getView().setTitle(i18n.tr("Sparquote eines Kontos"));

    de.willuhn.jameica.hbci.gui.parts.SparQuote q = new de.willuhn.jameica.hbci.gui.parts.SparQuote();
    q.paint(getParent());
    
    ButtonArea buttons = new ButtonArea(getParent(),1);
    buttons.addButton(i18n.tr("Zurück"),new Back(),null,true);
  }

}


/*********************************************************************
 * $Log: SparQuote.java,v $
 * Revision 1.1  2006/07/13 00:21:15  willuhn
 * @N Neue Auswertung "Sparquote"
 *
 **********************************************************************/