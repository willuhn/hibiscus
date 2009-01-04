/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/input/PassportInput.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/01/04 17:43:29 $
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.AbstractInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.PassportRegistry;
import de.willuhn.jameica.hbci.gui.action.PassportDetail;
import de.willuhn.jameica.hbci.gui.controller.PassportObject;
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
public class PassportInput extends AbstractInput
{
  private I18N i18n             = null;
  
  private Button button         = null;
  private SelectInput auswahl   = null;
  private Passport passport     = null;
  
  private boolean enabled       = true;
  
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
    this.i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
    Passport[] passports = null;
    try
    {
      passports = PassportRegistry.getPassports();
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while loading passport list",e);
      throw new ApplicationException(i18n.tr("Fehler beim Laden der Sicherheitsmedien"));
    }


    PassportObject[] p = new PassportObject[passports.length];
    for (int i=0;i<passports.length;++i)
    {
      p[i] = new PassportObject(passports[i]);
    }
    
    PassportObject current = null;
    if (konto != null && konto.getPassportClass() != null)
    {
      try
      {
        current = new PassportObject(konto.getPassportClass());
      }
      catch (Exception e)
      {
        Logger.error("error while loading passport",e);
        GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Laden des Sicherheitsmediums"));
      }
    }
    
    this.auswahl = new SelectInput(PseudoIterator.fromArray(p),current);
 
  }
  
  /**
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  public Object getValue()
  {
    PassportObject po = (PassportObject) this.auswahl.getValue();
    this.passport = po.getPassport();
    return this.passport;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  public void setValue(Object value)
  {
    if (value == null || !(value instanceof Passport))
      return;

    this.passport = (Passport) value;
    this.auswahl.setValue(this.passport);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getControl()
   */
  public Control getControl()
  {
    Composite comp = new Composite(getParent(),SWT.NONE);
    comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    comp.setBackground(Color.BACKGROUND.getSWTColor());
    GridLayout layout = new GridLayout(2, false);
    layout.marginHeight=0;
    layout.marginWidth=0;
    layout.horizontalSpacing = 5;
    layout.verticalSpacing = 0;
    comp.setLayout(layout);
  
    this.auswahl.paint(comp);
  
    button = GUI.getStyleFactory().createButton(comp);
    button.setText(i18n.tr("Konfigurieren..."));
    button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    button.setEnabled(this.enabled);
    button.setAlignment(SWT.RIGHT);
    button.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e)
      {
        try 
        {
          if (auswahl.getValue() == null)
          {
            GUI.getStatusBar().setErrorText(i18n.tr("Kein Sicherheitsmedium verfügbar"));
            return;
          }

          passport = ((PassportObject) auswahl.getValue()).getPassport();
          new PassportDetail().handleAction(passport);
        }
        catch (ApplicationException ae)
        {
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
        }
      }
    });
    return comp;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#focus()
   */
  public void focus()
  {
    this.auswahl.focus();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#disable()
   */
  public void disable()
  {
    this.setEnabled(false);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#enable()
   */
  public void enable()
  {
    this.setEnabled(true);
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#isEnabled()
   */
  public boolean isEnabled()
  {
    return this.auswahl.isEnabled();
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
    this.auswahl.setEnabled(this.enabled);
    if (this.button != null && !this.button.isDisposed())
      this.button.setEnabled(this.enabled);
  }

}


/*********************************************************************
 * $Log: PassportInput.java,v $
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