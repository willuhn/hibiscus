/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/VerwendungszweckDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/05/30 12:02:08 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.parts.ErweiterteVerwendungszwecke;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum Eingeben weiterer Zeilen Verwendungszweck.
 */
public class VerwendungszweckDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private ErweiterteVerwendungszwecke ewz = null;
  private String[] lines = null;
  private boolean readOnly = false;

  /**
   * ct
   * @param lines die Zeilen Verwendungszweck.
   * @param readOnly true, wenn der Dialog Read-Only sein soll.
   * @param pos Position des Dialogs.
   */
  public VerwendungszweckDialog(String[] lines, boolean readOnly, int pos)
  {
    super(pos);
    this.readOnly = readOnly;
    this.ewz = new ErweiterteVerwendungszwecke(lines,readOnly);
    setTitle(i18n.tr("Weitere Verwendungszwecke"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   * Liefert ein String-Array mit den Verwendungszwecken.
   */
  public Object getData() throws Exception
  {
    return this.lines;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    // Dialog bei Druck auf ESC automatisch schliessen
    parent.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        if (e.keyCode == SWT.ESC)
          throw new OperationCanceledException();
      }
    });
    
    this.ewz.paint(parent);
    Container container = new SimpleContainer(parent,true);
    
    Button apply = new Button(i18n.tr("Übernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          lines = ewz.getTexts();
          close();
        }
        catch (RemoteException re)
        {
          Logger.error("unable to apply data",re);
          throw new ApplicationException(i18n.tr("Fehler beim Übernehmen der Verwendungszwecke"));
        }
      }
    },null,true);
    apply.setEnabled(!this.readOnly);
    
    ButtonArea buttons = container.createButtonArea(2);
    buttons.addButton(apply);
    buttons.addButton(i18n.tr("Abbrechen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    });
  }

}


/*********************************************************************
 * $Log: VerwendungszweckDialog.java,v $
 * Revision 1.1  2008/05/30 12:02:08  willuhn
 * @N Erster Code fuer erweiterte Verwendungszwecke - NOCH NICHT FREIGESCHALTET!
 *
 **********************************************************************/