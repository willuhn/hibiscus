/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/DonateView.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/08/20 12:42:02 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import java.util.Calendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.gui.action.DauerauftragNew;
import de.willuhn.jameica.hbci.rmi.Dauerauftrag;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * View fuer den Spenden-Aufruf.
 */
public class DonateView extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Spenden für Hibiscus"));
    
    {
      Composite comp = new Composite(this.getParent(),SWT.NONE);
      comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      comp.setLayout(SWTUtil.createGrid(2,false));
      
      Container container = new SimpleContainer(comp);
      container.addHeadline(i18n.tr("Warum eigentlich?") + "  ");
      container.addText(i18n.tr("Viele Opensource-Anwendungen werden nicht von finanzstarken Unternehmen programmiert " +
                                "sondern von freiwilligen Entwicklern, die das in ihrer Freizeit tun. Hibiscus ist ein " +
                                "solches Projekt.\n\n" +
                                "Neben der Zeit, die ich für die Weiterentwicklung von Hibiscus investiere, " +
                                "benötige ich natürlich auch Geld für die Miete des Webservers, für zu testende Chipkarten-Leser " +
                                "und auch für die Computer und Betriebssysteme, auf denen Hibiscus laufen soll. Leider " +
                                "konnte ich bisher kein Unternehmen finden, welches mich sponsored."),true);
      
      Canvas c = SWTUtil.getCanvas(comp,SWTUtil.getImage("hibiscus-donate.png"),SWT.TOP | SWT.LEFT);
      ((GridData)c.getLayoutData()).minimumWidth = 157;
    }
    
    {
      Container container = new SimpleContainer(getParent());
      container.addHeadline(i18n.tr("Idee"));
      container.addText(i18n.tr("Hibiscus wird von vielen tausend Usern in Deutschland genutzt. " +
      		                      "Eine sehr kleine, aber regelmäßige Spende (z.Bsp. ein oder zwei Euro im Monat) von " +
      		                      "nur einem Teil der vielen User würde bereits genügen, damit ich " +
      		                      "in Vollzeit an Hibiscus arbeiten könnte. " +
      		                      "Angenommen, es fänden sich 1000 User, die bereit sind, zwei Euro im Monat " +
      		                      "mittels Dauerauftrag zu spenden. Dann blieben nach Abzug der Steuern, die ich " +
      		                      "darauf bezahlen muss, immer noch über 1000,- Euro monatlich übrig.\n\n" +
      		                      "Ein kleiner Einsatz von Vielen aber eine große Wirkung. Hibiscus wäre nicht mehr " +
      		                      "länger ein reines Freizeitprojekt. Sondern eine Vollzeit-Aufgabe für mich. Und Sie " +
      		                      "wären meine Arbeitgeber. Eine faszinierende Idee, wie ich finde.\n\n" +
      		                      "Ich würde mich freuen, wenn Sie dies mit unterstützen wollen. Durch Klick auf " +
      		                      "\"Dauerauftrag erstellen\" können Sie einen Dauerauftrag anlegen, in dem bereits " +
      		                      "mein Konto als Empfänger eingetragen ist. Absenden müssen Sie ihn natürlich noch " +
      		                      "manuell ;)\n\n" +
      		                      "Vielen Dank!\n" +
      		                      "Olaf Willuhn"),true);
    }
    {
      ButtonArea buttons = new ButtonArea();
      buttons.addButton(new Back(true));
      buttons.addButton(i18n.tr("Dauerauftrag erstellen"),new Action() {
        public void handleAction(Object context) throws ApplicationException
        {
          try
          {
            Dauerauftrag d = (Dauerauftrag) Settings.getDBService().createObject(Dauerauftrag.class,null);
            d.setGegenkontoBLZ(new String(new char[]{'5','0','5','3','0','0','0','0'}));
            d.setGegenkontoNummer(new String(new char[]{'3','2','5','4','0','6'}));
            d.setGegenkontoName("Olaf Willuhn");
            d.setZweck("Hibiscus-Spende");

            // Wir lassen 4 Tage Vorlauf
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE,4);
            d.setErsteZahlung(cal.getTime());
            Turnus turnus = (Turnus) Settings.getDBService().createObject(Turnus.class,null);
            turnus.setIntervall(1);
            turnus.setTag(cal.get(Calendar.DAY_OF_MONTH));
            turnus.setZeiteinheit(Turnus.ZEITEINHEIT_MONATLICH);
            d.setTurnus(turnus);
            new DauerauftragNew().handleAction(d);
          }
          catch (Exception e)
          {
            Logger.error("unable to create dauerauftrag",e);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anlegen des Dauerauftrages: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
          }
        }
      },null,false,"emblem-special.png");
      buttons.paint(getParent());
    }
    
    
  }

}



/**********************************************************************
 * $Log: DonateView.java,v $
 * Revision 1.1  2010/08/20 12:42:02  willuhn
 * @N Neuer Spenden-Aufruf. Ich bin gespannt, ob das klappt ;)
 *
 **********************************************************************/