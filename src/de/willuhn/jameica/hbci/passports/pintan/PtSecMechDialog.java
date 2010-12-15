/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/PtSecMechDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/12/15 13:17:25 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.pintan;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum zur Auswahl des Pin/Tan Scurity-Mechanismus.
 * BUGZILLA 200
 */
public class PtSecMechDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private String options      = null;

  private SelectInput type    = null;
  private CheckboxInput save  = null;
  private PinTanConfig config = null;
  
  private PtSecMech choosen   = null;
  
  /**
   * ct.
   * @param config die PINTAN-Config.
   * @param options die zur Verfuegung stehenden Optionen.
   */
  public PtSecMechDialog(PinTanConfig config, String options)
  {
    super(PtSecMechDialog.POSITION_CENTER);
    this.config = config;
    this.options = options;
    
    setTitle(i18n.tr("Auswahl des PIN/TAN-Verfahrens"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent,i18n.tr("Verfügbare Verfahren"));
    
    group.addText(i18n.tr("Bitte wählen Sie das gewünschte PIN/TAN-Verfahren"),true);
    
    group.addLabelPair(i18n.tr("Bezeichnung"), getType());
    group.addCheckbox(getSave(),i18n.tr("Auswahl speichern"));
    
    ButtonArea buttons = new ButtonArea(parent,2);
    buttons.addButton(i18n.tr("Übernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          choosen = (PtSecMech) getType().getValue();
          
          if (choosen != null)
          {
            Boolean b = (Boolean) getSave().getValue();
            if (b.booleanValue())
            {
              // BUGZILLA 218
              try
              {
                Application.getCallback().notifyUser(
                    i18n.tr("Sie können diese Vorauswahl später in der PIN/TAN-Konfiguration\n" +
                             "über die Option \"Automatische Auswahl des TAN-Verfahrens löschen\"\n" +
                             "wieder rückgängig machen."));
              }
              catch (Exception e)
              {
                Logger.error("unable to notify user",e);
              }
              if (config != null)
                config.setSecMech(choosen.getId());
            }
          }
          close();
        }
        catch (RemoteException e)
        {
          Logger.error("unable to apply data",e);
          throw new ApplicationException(i18n.tr("Fehler beim Übernehmen des PIN/TAN-Verfahrens"));
        }
      }
    },null,true);
    buttons.addButton(i18n.tr("Abbrechen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    if (choosen == null)
      return null;
    return choosen.getId();
  }

  /**
   * Liefert eine Checkbox, mit der der User entscheiden kann, ob seine Auswahl gespeichert werden soll.
   * @return Checkbox.
   */
  private CheckboxInput getSave()
  {
    if (this.save != null)
      return this.save;
    
    this.save = new CheckboxInput(false);
    return this.save;
  }
  
  /**
   * Erzeugt eine Combo-Box mit der Auswahl der verfuegbaren Verfahren.
   * @return Auwahl-Feld.
   * @throws ApplicationException
   */
  private SelectInput getType() throws ApplicationException
  {
    if (this.type != null)
      return this.type;

    List<PtSecMech> list = PtSecMech.parse(this.options);
    this.type = new SelectInput(list,null);
    this.type.setAttribute("name");
    return this.type;
  }
}


/*********************************************************************
 * $Log: PtSecMechDialog.java,v $
 * Revision 1.2  2010/12/15 13:17:25  willuhn
 * @N Code zum Parsen der TAN-Verfahren in PtSecMech ausgelagert. Wenn ein TAN-Verfahren aus Vorauswahl abgespeichert wurde, wird es nun nur noch dann automatisch verwendet, wenn es in der aktuellen Liste der TAN-Verfahren noch enthalten ist. Siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?t=12545
 *
 * Revision 1.1  2010/06/17 11:38:15  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 *
 * Revision 1.2  2006/08/05 20:45:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2006/08/03 13:51:38  willuhn
 * @N Bug 62
 * @C HBCICallback-Handling nach Zustaendigkeit auf Passports verteilt
 *
 * Revision 1.2  2006/03/28 17:52:23  willuhn
 * @B bug 218
 *
 * Revision 1.1  2006/02/23 22:14:58  willuhn
 * @B bug 200 (Speichern der Auswahl)
 *
 * Revision 1.1  2006/02/21 22:51:36  willuhn
 * @B bug 200
 *
 **********************************************************************/