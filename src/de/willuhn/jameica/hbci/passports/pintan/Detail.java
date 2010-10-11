/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/Detail.java,v $
 * $Revision: 1.4 $
 * $Date: 2010/10/11 20:58:51 $
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
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ButtonArea;
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
      group.addCheckbox(control.getSaveTAN(),i18n.tr("Verbrauchte TANs merken"));
      group.addCheckbox(control.getShowTan(),i18n.tr("TANs während der Eingabe anzeigen"));

    }
    
    {
      ButtonArea buttons = new ButtonArea(getParent(),2);
      // BUGZILLA 218
      String secMech = control.getConfig().getSecMech();
      Button b = new Button(i18n.tr("TAN-Verfahren zurücksetzen"), new Action() {
        public void handleAction(Object context) throws ApplicationException
        {
          control.handleDeleteSecMech();
        }
      },null,false,"edit-undo.png");
      b.setEnabled(secMech != null && secMech.length() > 0);
      buttons.addButton(b);

      buttons.addButton(i18n.tr("Verbrauchte TANs"),new Action()
      {
        public void handleAction(Object context) throws ApplicationException
        {
          control.handleShowUsedTans();
        }
      },null,false,"emblem-default.png");

    }

    new Headline(getParent(),i18n.tr("Fest zugeordnete Konten"));
    control.getKontoAuswahl().paint(getParent());

    ButtonArea buttons = new ButtonArea(getParent(),4);
    buttons.addButton(new Back(true));
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
  }

  /**
   * @see de.willuhn.jameica.gui.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
  }

}


/**********************************************************************
 * $Log: Detail.java,v $
 * Revision 1.4  2010/10/11 20:58:51  willuhn
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
 *
 * Revision 1.11  2009/06/16 14:04:34  willuhn
 * @N Dialog zum Anzeigen der BPD/UPD
 *
 * Revision 1.10  2007/08/31 09:43:55  willuhn
 * @N Einer PIN/TAN-Config koennen jetzt mehrere Konten zugeordnet werden
 *
 * Revision 1.9  2006/08/03 15:31:35  willuhn
 * @N Bug 62 completed
 *
 * Revision 1.8  2006/08/03 13:51:38  willuhn
 * @N Bug 62
 * @C HBCICallback-Handling nach Zustaendigkeit auf Passports verteilt
 *
 * Revision 1.7  2006/01/10 22:34:07  willuhn
 * @B bug 173
 *
 * Revision 1.6  2005/07/18 12:53:30  web0
 * @B bug 96
 *
 * Revision 1.5  2005/06/21 21:44:49  web0
 * @B bug 80
 *
 * Revision 1.4  2005/04/27 00:30:12  web0
 * @N real test connection
 * @N all hbci versions are now shown in select box
 * @C userid and customerid are changable
 *
 * Revision 1.3  2005/03/11 00:49:30  web0
 * *** empty log message ***
 *
 * Revision 1.2  2005/03/10 18:38:48  web0
 * @N more PinTan Code
 *
 * Revision 1.1  2005/03/08 18:44:57  web0
 * *** empty log message ***
 *
 **********************************************************************/