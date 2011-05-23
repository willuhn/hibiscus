/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/Detail.java,v $
 * $Revision: 1.7 $
 * $Date: 2011/05/23 10:47:29 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.pintan;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Detail-Ansicht eines Passports.
 */
public class Detail extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {

		final Controller control = new Controller(this);

		GUI.getView().setTitle(i18n.tr("Details der PIN/TAN-Konfiguration"));

    ColumnLayout layout = new ColumnLayout(getParent(),2);

    {
      Container group = new SimpleContainer(layout.getComposite());
      group.addHeadline(i18n.tr("Verbindungsdaten zur Bank"));
      group.addInput(control.getURL());
      group.addInput(control.getPort());
      group.addInput(control.getFilterType());
      group.addInput(control.getHBCIVersion());
    }
    
    {
      Container group = new SimpleContainer(layout.getComposite());
      group.addHeadline(i18n.tr("Benutzerdaten"));
      group.addInput(control.getUserId());
      group.addInput(control.getCustomerId());
      group.addInput(control.getBLZ());
    }
    
    {
      Container group = new SimpleContainer(getParent());
      group.addHeadline(i18n.tr("Erweiterte Einstellungen"));
      group.addInput(control.getBezeichnung());
      group.addCheckbox(control.getShowTan(),i18n.tr("TANs während der Eingabe anzeigen"));
    }
    
    {
      ButtonArea buttons = new ButtonArea();
      // BUGZILLA 218
      String secMech  = control.getConfig().getSecMech();
      String tanMedia = control.getConfig().getTanMedia();
      Button b = new Button(i18n.tr("TAN-Verfahren zurücksetzen"), new Action() {
        public void handleAction(Object context) throws ApplicationException
        {
          control.handleDeleteTanSettings();
        }
      },null,false,"edit-undo.png");
      b.setEnabled((secMech != null && secMech.length() > 0) || (tanMedia != null && tanMedia.length() > 0));
      buttons.addButton(b);
      buttons.paint(getParent());
    }

    new Headline(getParent(),i18n.tr("Fest zugeordnete Konten"));
    control.getKontoAuswahl().paint(getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("BPD/UPD"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleDisplayProperties();
      }
    },null,false,"text-x-generic.png");
    buttons.addButton(i18n.tr("Konfiguration testen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
				control.handleTest();
      }
    },null,false,"dialog-information.png");
    buttons.addButton(i18n.tr("Speichern"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        control.handleStore();
      }
    },null,false,"document-save.png");
    buttons.paint(getParent());
  }
}


/**********************************************************************
 * $Log: Detail.java,v $
 * Revision 1.7  2011/05/23 10:47:29  willuhn
 * @R BUGZILLA 62 - Speichern der verbrauchten TANs ausgebaut. Seit smsTAN/chipTAN gibt es zum einen ohnehin keine TAN-Listen mehr. Zum anderen kann das jetzt sogar Fehler ausloesen, wenn ueber eines der neuen TAN-Verfahren die gleiche TAN generiert wird, die frueher irgendwann schonmal zufaellig generiert wurde. TANs sind inzwischen fluechtige und werden dynamisch erzeugt. Daher ist es unsinnig, die zu speichern. Zumal es das Wallet sinnlos aufblaeht.
 *
 * Revision 1.6  2011-05-09 09:35:15  willuhn
 * @N BUGZILLA 827
 *
 * Revision 1.5  2011-04-08 15:19:14  willuhn
 * @R Alle Zurueck-Buttons entfernt - es gibt jetzt einen globalen Zurueck-Button oben rechts
 * @C Code-Cleanup
 *
 * Revision 1.4  2010-10-11 20:58:51  willuhn
 * @N BUGZILLA 927
 *
 * Revision 1.3  2010-07-22 12:37:41  willuhn
 * @N GUI poliert
 *
 * Revision 1.2  2010-07-13 11:01:05  willuhn
 * @N Icons in PIN/TAN-Config
 *
 * Revision 1.1  2010/06/17 11:38:15  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 **********************************************************************/