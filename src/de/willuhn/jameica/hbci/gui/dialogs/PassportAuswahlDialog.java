/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.input.PassportInput;
import de.willuhn.jameica.hbci.passport.Passport;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Auswahl eines Passports.
 */
public class PassportAuswahlDialog extends AbstractDialog
{
  private final static int WINDOW_WIDTH = 400;

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private Konto konto         = null;
  private PassportInput input = null;
  private Passport passport   = null;
  private LabelInput comment  = null;

  /**
   * ct.
   * @param position
   */
  public PassportAuswahlDialog(int position)
  {
    this(null,position);
  }

  /**
   * ct.
   * @param konto Optionale Angabe des Kontos. Ist es angegeben, wird
   * der Passport des Kontos vorselektiert.
   * @param position
   */
  public PassportAuswahlDialog(Konto konto, int position)
  {
    super(position);
    this.konto = konto;
    this.setTitle(i18n.tr("Auswahl des HBCI-Verfahrens"));
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container c = new SimpleContainer(parent);
    c.addText(i18n.tr("Bitte wählen Sie das zu verwendende HBCI-Verfahren aus."),false);
    c.addInput(this.getInput());
    c.addInput(this.getComment());
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          passport = (Passport) getInput().getValue();
          if (passport == null)
          {
            getComment().setValue(i18n.tr("Bitte wählen Sie ein Verfahren aus."));
            return;
          }
          close();
        }
        catch (Exception e)
        {
          Logger.error("unable to choose passport",e);
          throw new ApplicationException(i18n.tr("Auswahl fehlgeschlagen: {0}",e.getMessage()));
        }
      }
    },null,true,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException(i18n.tr("Vorgang abgebrochen"));
      }
    },null,false,"process-stop.png");
    
    c.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }
  
  /**
   * Liefert das Auswahlfeld fuer den Passport.
   * @return das Auswahlfeld.
   * @throws RemoteException
   */
  private PassportInput getInput() throws RemoteException
  {
    if (this.input == null)
      this.input = new PassportInput(this.konto);
    return this.input;
  }
  
  /**
   * Liefert ein Textfeld mit Kommentar/Fehler.
   * @return Textfeld.
   * @throws RemoteException
   */
  private LabelInput getComment() throws RemoteException
  {
    if (this.comment == null)
    {
      this.comment = new LabelInput("");
      this.comment.setName("");
      this.comment.setColor(Color.ERROR);
    }
    return this.comment;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.passport;
  }

}


/*********************************************************************
 * $Log: PassportAuswahlDialog.java,v $
 * Revision 1.3  2011/04/29 11:38:58  willuhn
 * @N Konfiguration der HBCI-Medien ueberarbeitet. Es gibt nun direkt in der Navi einen Punkt "Bank-Zugaenge", in der alle Medien angezeigt werden.
 *
 * Revision 1.2  2005/08/01 23:27:42  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/06/21 21:48:24  web0
 * @B bug 80
 *
 **********************************************************************/