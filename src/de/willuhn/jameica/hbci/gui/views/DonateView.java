/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
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
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.AuslandsUeberweisung;
import de.willuhn.jameica.hbci.rmi.SepaDauerauftrag;
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

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("Spenden f�r Hibiscus"));
    
    {
      Composite comp = new Composite(this.getParent(),SWT.NONE);
      comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      comp.setLayout(SWTUtil.createGrid(2,false));
      
      Container container = new SimpleContainer(comp);
      container.addHeadline(i18n.tr("Warum eigentlich?") + "  ");
      container.addText(i18n.tr("Viele Opensource-Anwendungen werden nicht von finanzstarken Unternehmen programmiert " +
                                "sondern von freiwilligen Entwicklern, die das in ihrer Freizeit tun. Hibiscus ist ein " +
                                "solches Projekt.\n\n" +
                                "Neben der Zeit, die ich f�r die Weiterentwicklung von Hibiscus investiere, " +
                                "ben�tige ich nat�rlich auch Geld f�r die Miete des Webservers, f�r zu testende Chipkarten-Leser " +
                                "und auch f�r die Computer und Betriebssysteme, auf denen Hibiscus laufen soll. Leider " +
                                "konnte ich bisher kein Unternehmen finden, welches mich sponsert."),true);
      
      Canvas c = SWTUtil.getCanvas(comp,SWTUtil.getImage("hibiscus-donate.png"),SWT.TOP | SWT.LEFT);
      ((GridData)c.getLayoutData()).minimumWidth = 157;
    }
    
    {
      Container container = new SimpleContainer(getParent());
      container.addHeadline(i18n.tr("Unterst�tzen"));
      container.addText(i18n.tr("Ich w�rde mich freuen, wenn Sie das Projekt mit unterst�tzen wollen. Durch Klick auf " +
      		                      "\"Dauerauftrag erstellen\" bzw. \"...oder �berweisung\" k�nnen Sie eine einmalige �berweisung oder einen Dauerauftrag (z.Bsp. mit 1 oder 2 EUR) " +
      		                      "erstellen, in dem mein Konto bereits als Empf�nger eingetragen ist.\n\n" +
      		                      "Nur wenn Sie wollen - es ist v�llig freiwillig.\n\n" +
      		                      "Vielen Dank!\n" +
      		                      "Olaf Willuhn"),true);
    }

    {
      
      final char[] iban = new char[]{'D','E','1','7','8','6','0','5','0','2','0','0','1','2','1','0','3','2','2','5','2','4'};
      final char[] bic  = new char[]{'S','O','L','A','D','E','S','1','G','R','M'};
      final String name = "Olaf Willuhn";
      
      ButtonArea buttons = new ButtonArea();
      buttons.addButton(i18n.tr("Dauerauftrag erstellen"),new Action() {
        @Override
        public void handleAction(Object context) throws ApplicationException
        {
          try
          {
            SepaDauerauftrag d = (SepaDauerauftrag) Settings.getDBService().createObject(SepaDauerauftrag.class,null);
            d.setGegenkontoBLZ(new String(bic));
            d.setGegenkontoNummer(new String(iban));
            d.setGegenkontoName(name);
            d.setZweck("Hibiscus-Spende");

            // Wir lassen 7 Tage Vorlauf
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE,7);
            d.setErsteZahlung(cal.getTime());
            Turnus turnus = (Turnus) Settings.getDBService().createObject(Turnus.class,null);
            turnus.setIntervall(1);
            turnus.setTag(cal.get(Calendar.DAY_OF_MONTH));
            turnus.setZeiteinheit(Turnus.ZEITEINHEIT_MONATLICH);
            d.setTurnus(turnus);
            new de.willuhn.jameica.hbci.gui.action.SepaDauerauftragNew().handleAction(d);
          }
          catch (Exception e)
          {
            Logger.error("unable to create sepa-dauerauftrag",e);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anlegen des SEPA-Dauerauftrages: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
          }
        }
      },null,false,"emblem-special.png");
      buttons.addButton(i18n.tr("...oder �berweisung"),new Action() {
        @Override
        public void handleAction(Object context) throws ApplicationException
        {
          try
          {
            AuslandsUeberweisung u = (AuslandsUeberweisung) Settings.getDBService().createObject(AuslandsUeberweisung.class,null);
            u.setGegenkontoBLZ(new String(bic));
            u.setGegenkontoNummer(new String(iban));
            u.setGegenkontoName(name);
            u.setZweck("Spende Hibiscus");
            new de.willuhn.jameica.hbci.gui.action.AuslandsUeberweisungNew().handleAction(u);
          }
          catch (Exception e)
          {
            Logger.error("unable to create sepa ueberweisung",e);
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Anlegen der SEPA-�berweisung: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
          }
        }
      },null,false,"ueberweisung.png");
      buttons.paint(getParent());
    }
    
    
  }

}
