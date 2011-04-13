/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/UmsatzList.java,v $
 * $Revision: 1.16 $
 * $Date: 2011/04/13 17:35:46 $
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
import de.willuhn.jameica.gui.internal.parts.PanelButtonPrint;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.KontoFetchUmsaetze;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetailEdit;
import de.willuhn.jameica.hbci.gui.action.UmsatzImport;
import de.willuhn.jameica.hbci.gui.controller.UmsatzControl;
import de.willuhn.jameica.hbci.io.print.PrintSupportUmsatzList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Listet alle Umsaetze auf.
 */
public class UmsatzList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private UmsatzControl control = null;
  
  /**
   * ct.
   */
  public UmsatzList()
  {
    control = new UmsatzControl(this);
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {


    // BUGZILLA 38 http://www.willuhn.de/bugzilla/show_bug.cgi?id=38
    Konto k = control.getKonto();

    String s1 = k.getBezeichnung();
    if (s1 == null) s1 = "";

    String s2 = k.getKontonummer();
    
    double d = k.getSaldo();
    String s3 = null;
    if (k.getSaldoDatum() != null)
      s3 = HBCI.DECIMALFORMAT.format(d) + " " + k.getWaehrung(); // Saldo wurde schonmal abgerufen

    if (s3 == null)
  		GUI.getView().setTitle(i18n.tr("Kontoauszüge: {0} [Kto.-Nr.: {1}]",new String[]{s1,s2}));
    else
      GUI.getView().setTitle(i18n.tr("Kontoauszüge: {0} [Kto.-Nr.: {1}, Saldo: {2}]",new String[]{s1,s2,s3}));
		
		final TablePart list = control.getUmsatzListe();
		
    GUI.getView().addPanelButton(new PanelButtonPrint(new PrintSupportUmsatzList(list))
    {
      public boolean isEnabled()
      {
        return list.getSelection() != null && super.isEnabled();
      }
    });
		
		list.paint(getParent());
		
		ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Umsätze importieren..."), new UmsatzImport(),control.getKonto(),false,"document-open.png");

    int flags = control.getKonto().getFlags();

    Button fetch = null;

    if ((flags & Konto.FLAG_OFFLINE) == Konto.FLAG_OFFLINE)
      fetch = new Button(i18n.tr("Umsatz anlegen"), new UmsatzDetailEdit(),control.getKonto(),false,"emblem-documents.png");
    else
      fetch = new Button(i18n.tr("Umsätze abrufen"), new KontoFetchUmsaetze(),control.getKonto(),false,"mail-send-receive.png");
    
    fetch.setEnabled((flags & Konto.FLAG_DISABLED) != Konto.FLAG_DISABLED);
    buttons.addButton(fetch);

    buttons.paint(getParent());
  }
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#reload()
   */
  public void reload() throws ApplicationException
  {
    control.handleReload();
    super.reload();
  }
}


/**********************************************************************
 * $Log: UmsatzList.java,v $
 * Revision 1.16  2011/04/13 17:35:46  willuhn
 * @N Druck-Support fuer Kontoauszuege fehlte noch
 *
 * Revision 1.15  2011-04-08 15:19:13  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.14  2010-11-19 18:37:20  willuhn
 * @N Erste Version der Termin-View mit Appointment-Providern
 *
 * Revision 1.13  2010/04/22 16:40:57  willuhn
 * @N Manuelles Anlegen neuer Umsaetze fuer Offline-Konten moeglich
 *
 * Revision 1.12  2010/04/22 16:21:27  willuhn
 * @N HBCI-relevante Buttons und Aktionen fuer Offline-Konten sperren
 **********************************************************************/