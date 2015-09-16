/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/PtSecMechDialog.java,v $
 * $Revision: 1.8 $
 * $Date: 2011/12/06 22:22:19 $
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
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.synchronize.SynchronizeSession;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Auswahl des Pin/Tan Scurity-Mechanismus.
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
    
    String s = null;
    try
    {
      BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      SynchronizeSession session = service.get(HBCISynchronizeBackend.class).getCurrentSession();
      Konto konto = session != null ? session.getKonto() : null;
      
      if (konto != null)
      {
        s = konto.getBezeichnung();
        String name = HBCIProperties.getNameForBank(konto.getBLZ());
        if (name != null && name.length() > 0)
          s += " [" + name + "]";
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to determine current konto",e);
    }

    if (s != null) setTitle(i18n.tr("PIN/TAN-Verfahren - Konto {0}",s));
    else           setTitle(i18n.tr("Auswahl des PIN/TAN-Verfahrens"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent);
    
    group.addText(i18n.tr("Bitte wählen Sie das gewünschte PIN/TAN-Verfahren"),true);
    
    group.addLabelPair(i18n.tr("Bezeichnung"), getType());
    group.addCheckbox(getSave(),i18n.tr("Auswahl speichern"));
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          choosen = (PtSecMech) getType().getValue();
          
          if (choosen != null)
          {
            Boolean b = (Boolean) getSave().getValue();
            if (getSave().isEnabled() && b.booleanValue())
            {
              // BUGZILLA 218
              try
              {
                Application.getCallback().notifyUser(
                    i18n.tr("Sie können diese Vorauswahl später in der PIN/TAN-Konfiguration\n" +
                             "über die Option \"TAN-Verfahren zurücksetzen\" wieder\n" +
                             "rückgängig machen."));
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
    },null,true,"ok.png");
    buttons.addButton(new Cancel());
    
    group.addButtonArea(buttons);
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
    this.save.setEnabled(this.config != null);
    return this.save;
  }
  
  /**
   * Erzeugt eine Combo-Box mit der Auswahl der verfuegbaren Verfahren.
   * @return Auswahl-Feld.
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
 * Revision 1.8  2011/12/06 22:22:19  willuhn
 * @N BUGZILLA 1151 - Name des aktuellen Kontos anzeigen
 *
 * Revision 1.7  2011-05-09 09:22:02  willuhn
 * @C Checkbox nur anklickbar machen, wenn Config vorhanden
 *
 * Revision 1.6  2011-05-09 08:35:18  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2011-05-09 08:33:02  willuhn
 * @C GUI-polish
 *
 * Revision 1.4  2011-05-09 08:29:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2010-12-31 01:09:08  willuhn
 * @B Typos
 *
 * Revision 1.2  2010-12-15 13:17:25  willuhn
 * @N Code zum Parsen der TAN-Verfahren in PtSecMech ausgelagert. Wenn ein TAN-Verfahren aus Vorauswahl abgespeichert wurde, wird es nun nur noch dann automatisch verwendet, wenn es in der aktuellen Liste der TAN-Verfahren noch enthalten ist. Siehe http://www.onlinebanking-forum.de/phpBB2/viewtopic.php?t=12545
 *
 * Revision 1.1  2010/06/17 11:38:15  willuhn
 * @C kompletten Code aus "hbci_passport_pintan" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 **********************************************************************/