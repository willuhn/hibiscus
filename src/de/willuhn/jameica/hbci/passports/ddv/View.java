/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/ddv/View.java,v $
 * $Revision: 1.4 $
 * $Date: 2010/06/17 11:45:49 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.ddv;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.Back;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Konfiguration eines Passports vom Typ DDV.
 */
public class View extends AbstractView {

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception {

    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    
    GUI.getView().setTitle(i18n.tr("Eigenschaften des Chipkartenlesers"));
    
    final Controller control = new Controller(this);
    
    LabelGroup group = new LabelGroup(getParent(),i18n.tr("Eigenschaften"));
    
    group.addLabelPair(i18n.tr("Bezeichnung"),				control.getName());
    group.addLabelPair(i18n.tr("Vorkonfigurierte Leser"),	control.getReaderPresets());
    group.addLabelPair(i18n.tr("Port des Lesers"),			control.getPort());
    group.addLabelPair(i18n.tr("CTAPI Treiber-Datei"),		control.getCTAPI());
    group.addLabelPair(i18n.tr("Index des Lesers"),			control.getCTNumber());
    group.addLabelPair(i18n.tr("Index des HBCI-Zugangs"),	control.getEntryIndex());
    group.addLabelPair(i18n.tr("HBCI-Version"),             control.getHBCIVersion());

    group.addCheckbox(control.getBio(), 		i18n.tr("Biometrische Verfahren verwenden"));
    group.addCheckbox(control.getSoftPin(), i18n.tr("Tastatur des PCs zur PIN-Eingabe verwenden"));
		
    // und noch die Abschicken-Knoepfe
    ButtonArea buttonArea = new ButtonArea(getParent(),4);
    buttonArea.addButton(i18n.tr("Zurück"),new Back(),null,true);
    buttonArea.addButton(i18n.tr("Automatische Suche des Kartenlesers..."), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleScan();
      }
    });
    buttonArea.addButton(i18n.tr("Einstellungen testen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleTest();
      }
    });
    buttonArea.addButton(i18n.tr("Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
      	control.handleStore();
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {
  }

}


/**********************************************************************
 * $Log: View.java,v $
 * Revision 1.4  2010/06/17 11:45:49  willuhn
 * @C kompletten Code aus "hbci_passport_ddv" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.16  2007/07/24 13:50:27  willuhn
 * @N BUGZILLA 61
 *
 * Revision 1.15  2006/08/03 22:13:49  willuhn
 * @N OmniKey 4000 Preset
 *
 * Revision 1.14  2005/06/27 11:24:30  web0
 * @N HBCI-Version aenderbar
 *
 * Revision 1.13  2005/06/21 21:44:42  web0
 * @B bug 80
 *
 * Revision 1.12  2005/02/01 18:26:42  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2005/01/05 18:38:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/10/20 12:08:15  willuhn
 * @C MVC-Refactoring (new Controllers)
 *
 * Revision 1.9  2004/10/14 21:59:01  willuhn
 * @N refactoring
 *
 * Revision 1.8  2004/07/27 22:56:18  willuhn
 * @N Reader presets
 *
 * Revision 1.7  2004/07/25 15:05:40  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.6  2004/07/21 23:54:19  willuhn
 * @C massive Refactoring ;)
 *
 * Revision 1.5  2004/07/20 22:49:56  willuhn
 * @C Refactoring
 *
 * Revision 1.4  2004/07/19 22:37:28  willuhn
 * @B gna - Chipcard funktioniert ja doch ;)
 *
 * Revision 1.2  2004/07/08 23:22:36  willuhn
 * @R "Delete"-Button entfernt
 *
 * Revision 1.1  2004/05/04 23:24:34  willuhn
 * @N separated passports into eclipse project
 *
 * Revision 1.2  2004/05/04 23:07:23  willuhn
 * @C refactored Passport stuff
 *
 * Revision 1.1  2004/04/27 22:23:56  willuhn
 * @N configurierbarer CTAPI-Treiber
 * @C konkrete Passport-Klassen (DDV) nach de.willuhn.jameica.passports verschoben
 * @N verschiedenste Passport-Typen sind jetzt voellig frei erweiterbar (auch die Config-Dialoge)
 * @N crc32 Checksumme in Umsatz
 * @N neue Felder im Umsatz
 *
 * Revision 1.12  2004/04/19 22:05:52  willuhn
 * @C HBCIJobs refactored
 *
 * Revision 1.11  2004/04/14 23:53:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.9  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2004/03/19 01:44:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/03/04 00:26:24  willuhn
 * @N Ueberweisung
 *
 * Revision 1.6  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.5  2004/02/27 01:10:18  willuhn
 * @N passport config refactored
 *
 * Revision 1.4  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.3  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/12 00:38:41  willuhn
 * *** empty log message ***
 *
 **********************************************************************/