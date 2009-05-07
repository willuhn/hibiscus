/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/PassportInput.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/05/07 13:36:57 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.input;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Eine Auswahl-Box fuer das Sicherheitsmedium.
 */
public class PassportInput extends SelectInput
{
  private final static I18N i18n          = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private static List<Passport> passports = null;
  
  private Composite comp                  = null;
  
  /**
   * ct.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public PassportInput() throws RemoteException, ApplicationException
  {
    this(null);
  }

  /**
   * ct.
   * @param konto das Konto.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public PassportInput(Konto konto) throws RemoteException, ApplicationException
  {
    super(init(),null);

    List<Passport> list = init();
    String currentClass = (konto != null ? konto.getPassportClass() : null);
    for (Passport p:list)
    {
      if (currentClass != null && p.getClass().getName().equals(currentClass))
      {
        setPreselected(p);
        break;
      }
    }
    
    this.setPleaseChoose("Bitte wählen...");
    this.setAttribute("name");
    this.setName(i18n.tr("Sicherheitsmedium"));
  }
  
  /**
   * Initialisiert die Passport-Liste.
   * @return Liste der Passports.
   */
  private static List<Passport> init()
  {
    if (passports == null)
    {
      try
      {
        passports = Arrays.asList(PassportRegistry.getPassports());
      }
      catch (ApplicationException ae)
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
      }
      catch (Exception e)
      {
        Logger.error("error while loading passport list",e);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Laden der Sicherheitsmedien"),StatusBarMessage.TYPE_ERROR));
      }
    }
    
    if (passports == null)
      passports = new ArrayList<Passport>();
    
    return passports;
  }

  /**
   * @see de.willuhn.jameica.gui.input.SelectInput#getControl()
   */
  public Control getControl()
  {
    Control combo = super.getControl();
    
    // Wir haengen jetzt noch unseren Button dran
    Button button = new Button(i18n.tr("Konfigurieren..."),new Action()
    {
      /**
       * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
       */
      public void handleAction(Object context) throws ApplicationException
      {
        try 
        {
          Object value = PassportInput.this.getValue();
          if (value == null)
          {
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Bitte wählen Sie ein Sicherheitsmedium aus"),StatusBarMessage.TYPE_ERROR));
            return;
          }
          new PassportDetail().handleAction(value);
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
      }
    },null,false,"document-properties.png");
    try
    {
      button.paint(getParent());
    }
    catch (RemoteException re)
    {
      Logger.error("unable to append configure button",re);
    }
    return combo;
  }

  /**
   * @see de.willuhn.jameica.gui.input.AbstractInput#getParent()
   */
  public Composite getParent()
  {
    if (this.comp != null)
      return this.comp;
    
    this.comp = new Composite(super.getParent(), SWT.NONE);
    this.comp.setBackground(Color.BACKGROUND.getSWTColor());
    GridLayout layout = new GridLayout(3, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.horizontalSpacing = 5;
    layout.verticalSpacing = 0;
    this.comp.setLayout(layout);
    final GridData g = new GridData(getStyleBits());
    g.horizontalSpan = 2;
    this.comp.setLayoutData(g);
    
    return this.comp;
  }
  
}


/*********************************************************************
 * $Log: PassportInput.java,v $
 * Revision 1.4  2009/05/07 13:36:57  willuhn
 * @R Hilfsobjekt "PassportObject" entfernt
 * @C Cleanup in PassportInput (insb. der weisse Hintergrund hinter dem "Konfigurieren..."-Button hat gestoert
 *
 * Revision 1.3  2009/01/04 17:43:29  willuhn
 * @N BUGZILLA 532
 *
 * Revision 1.2  2006/06/19 10:57:04  willuhn
 * @N neue Methode setEnabled(boolean) in Input
 *
 * Revision 1.1  2006/03/21 00:43:14  willuhn
 * @B bug 209
 *
 **********************************************************************/