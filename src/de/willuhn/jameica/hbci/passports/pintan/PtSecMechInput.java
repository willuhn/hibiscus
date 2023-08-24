/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.pintan;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.ButtonInput;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Vorkonfiguriertes Eingabefeld zum Anzeigen/Zurücksetzen des TAN-Verfahrens.
 */
public class PtSecMechInput extends ButtonInput
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private PinTanConfig conf = null;
  private Text label = null;
  
  /**
   * ct.
   * @param conf die PIN/TAN-Config.
   */
  public PtSecMechInput(PinTanConfig conf)
  {
    this.conf = conf;
    this.setName(i18n.tr("Gespeichertes TAN-Verfahren"));
    this.setButtonText(i18n.tr("TAN-Verfahren zurücksetzen") + " ");
    this.disableClientControl();
    this.addButtonListener(e -> {
      try
      {
        new PtSecMechDeleteSettings().handleAction(conf);
        label.setText("");
        disableButton();
      }
      catch (Exception ex)
      {
        Logger.error("error while deleting tan settings",ex);
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Zurücksetzen der TAN-Verfahren"),StatusBarMessage.TYPE_ERROR));
      }
    });

    try
    {
      final PtSecMech secMech = this.conf.getCurrentSecMech();
      final String tanMedia = this.conf.getTanMedia();
      final boolean enabled = secMech != null || (tanMedia != null && tanMedia.length() > 0);
      if (enabled)
        this.enableButton();
      else
        this.disableButton();
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine pin/tan sec mech",re);
    }

  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#getValue()
   */
  @Override
  public Object getValue()
  {
    return null;
  }

  /**
   * @see de.willuhn.jameica.gui.input.Input#setValue(java.lang.Object)
   */
  @Override
  public void setValue(Object value)
  {
  }

  /**
   * @see de.willuhn.jameica.gui.input.ButtonInput#getClientControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public Control getClientControl(Composite parent)
  {
    if (this.label != null)
      return this.label;

    this.label = GUI.getStyleFactory().createText(parent);
    this.label.setToolTipText(i18n.tr("Wird nur angezeigt, wenn mehrere TAN-Verfahren zur Auswahl stehen und eines davon gespeichert wurde"));
    this.label.setMessage("<" + i18n.tr("Kein TAN-Verfahren gespeichert") + ">");
    
    try
    {
      final PtSecMech secMech = this.conf.getCurrentSecMech();
      final String tanMedia = this.conf.getTanMedia();
      final boolean enabled = secMech != null || (tanMedia != null && tanMedia.length() > 0);

      if (enabled)
      {
        final StringBuilder sb = new StringBuilder();
        if (secMech != null)
        {
          if (tanMedia != null && tanMedia.length() > 0)
            sb.append(i18n.tr("Verfahren: {0}",secMech.getName()));
          else
            sb.append(secMech.getName());
        }
        if (tanMedia != null && tanMedia.length() > 0)
        {
          if (sb.length() > 0)
            sb.append(", ");
          sb.append(i18n.tr("Medienbezeichnung: {0}",tanMedia));
        }
        this.label.setText(sb.toString());
      }
    }
    catch (RemoteException re)
    {
      Logger.error("unable to determine pin/tan sec mech",re);
    }
    return label;
  }

}


