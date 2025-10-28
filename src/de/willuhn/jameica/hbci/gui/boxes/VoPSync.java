/**********************************************************************
 *
 * Copyright (c) 2025 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.boxes;

import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.action.PassportList;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Eine Box, die darauf hinweist, für VoP die Bankzugänge mal neu zu synchronisieren.
 */
public class VoPSync extends AbstractBox
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(VoPSync.class);
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  public String getName()
  {
    return "Hibiscus: " + i18n.tr("Empfängerprüfung / VoP (Verification of Payee)");
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#getHeight()
   */
  @Override
  public int getHeight()
  {
    return 200;
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isActive()
   */
  @Override
  public boolean isActive()
  {
    return !this.haveConfirmed();
  }
  
  /**
   * Liefert true, wenn der User den Dialog bestätigt hat.
   * @return true, wenn der User den Dialog bestätigt hat.
   */
  private boolean haveConfirmed()
  {
    return this.settings.getString("confirmed",null) != null;
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  public boolean getDefaultEnabled()
  {
    return !this.haveConfirmed();
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  public int getDefaultIndex()
  {
    return 0;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    final InfoPanel panel = new InfoPanel() {
      /**
       * @see de.willuhn.jameica.gui.parts.InfoPanel#extend(de.willuhn.jameica.gui.parts.InfoPanel.DrawState, org.eclipse.swt.widgets.Composite, java.lang.Object)
       */
      @Override
      public Composite extend(DrawState state, Composite comp, Object context)
      {
        if (state == DrawState.COMMENT_AFTER)
        {
          final CheckboxInput dismiss = new CheckboxInput(false);
          dismiss.setName(i18n.tr("Diesen Hinweis nicht mehr anzeigen"));
          dismiss.addListener(e -> {
            if (!((Boolean)dismiss.getValue()).booleanValue())
              return; // Den Fall gibt es nicht, weil die Box dann nicht angezeigt wird
            try
            {
              settings.setAttribute("confirmed",HBCI.LONGDATEFORMAT.format(new Date()));
              GUI.getCurrentView().reload();
            }
            catch (Exception ex)
            {
              Logger.error("unable to dismiss box",ex);
            }
          });
          dismiss.paint(comp);
        }
        return super.extend(state, comp, context);
      }
    };
    panel.setTitle(i18n.tr("Hinweis"));
    panel.setIcon("dialog-question-large.png");

    panel.setText(i18n.tr("Mit der Einführung der Empfängerprüfung ist es ggf. notwendig, den Bank-Zugang einmalig neu zu synchronisieren."));
    panel.setComment(i18n.tr("Klicken Sie auf \"Bank-Zugänge öffnen...\" und öffnen Sie diese anschließend durch Doppelklick. " +
                             "Klicken Sie anschließend ind der Detailansicht des Bank-Zugangs unten rechts auf \"Synchronisieren\"."));
    panel.setUrl("https://www.willuhn.de/wiki/doku.php?id=vop");
    
    panel.addButton(new Button(i18n.tr("Bank-Zugänge öffnen..."),new PassportList(),null,false,"system-users.png"));
    panel.paint(parent);
  }
}
