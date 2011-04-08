/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/ProtokollList.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/04/08 15:19:14 $
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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.KontoControl;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste der Protokoll-Eintraege eines Kontos an.
 */
public class ProtokollList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    KontoControl control = new KontoControl(this);

    Konto k = control.getKonto();
    if (k != null)
    {
      String s1 = k.getBezeichnung();
      if (s1 == null) s1 = "";

      String s2 = k.getKontonummer();
      GUI.getView().setTitle(i18n.tr("Protokoll des Kontos: {0} [Kto.-Nr.: {1}]",new String[]{s1,s2}));
    }
    else
      GUI.getView().setTitle(i18n.tr("Protokoll des Kontos"));
		
    control.getProtokoll().paint(getParent());
  }
}


/**********************************************************************
 * $Log: ProtokollList.java,v $
 * Revision 1.5  2011/04/08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 **********************************************************************/