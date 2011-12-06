/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/pintan/TanMediaDialog.java,v $
 * $Revision: 1.3 $
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

import org.eclipse.swt.widgets.Composite;
import org.kapott.hbci.manager.HBCIUtils;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCIFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zur Eingabe des TAN-Mediums.
 * BUGZILLA 827
 */
public class TanMediaDialog extends AbstractDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private SelectInput media   = null;
  private CheckboxInput save  = null;
  private PinTanConfig config = null;
  
  private String choosen      = null;
  
  /**
   * ct.
   * @param config die PINTAN-Config.
   */
  public TanMediaDialog(PinTanConfig config)
  {
    super(TanMediaDialog.POSITION_CENTER);
    this.config = config;
    
    String s = null;
    try
    {
      Konto konto = HBCIFactory.getInstance().getCurrentKonto();
      if (konto != null)
      {
        s = konto.getBezeichnung();
        String name = HBCIUtils.getNameForBLZ(konto.getBLZ());
        if (name != null && name.length() > 0)
          s += " [" + name + "]";
      }
    }
    catch (Exception e)
    {
      // ignore
    }

    if (s != null) setTitle(i18n.tr("TAN-Medium - Konto {0}",s));
    else           setTitle(i18n.tr("Auswahl des TAN-Mediums"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container group = new SimpleContainer(parent);
    
    group.addText(i18n.tr("Bitte wählen Sie die Bezeichnung des gewünschten TAN-Medium aus\n" +
    		                  "oder geben Sie die Bezeichnung neu ein.\n\n" +
    		                  "Beim smsTAN/mTAN-Verfahren ist das die Bezeichnung (nicht die Telefonnummer)\n" +
    		                  "Ihres Mobiltelefons, die Sie bei Ihrer Bank hinterlegt haben."),true);
    
    group.addLabelPair(i18n.tr("Bezeichnung"), getMedia());
    group.addCheckbox(getSave(),i18n.tr("Auswahl speichern"));
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(i18n.tr("Übernehmen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          choosen = (String) getMedia().getValue();
          
          if (choosen != null && config != null && choosen.length() > 0)
          {
            // Wir merken uns das gewaehlte TAN-Medium, damit der User es beim
            // naechsten Mal nicht neu eintippen kann sondern direkt auswaehlen
            config.addTanMedia(choosen);
            
            // User noch fragen, ob er seine Auswahl speichern will
            Boolean b = (Boolean) getSave().getValue();
            if (getSave().isEnabled() && b.booleanValue())
            {
              try
              {
                Application.getCallback().notifyUser(i18n.tr("Sie können diese Vorauswahl später in der PIN/TAN-Konfiguration\n" +
                                                             "über die Option \"TAN-Verfahren zurücksetzen\" wieder\n" +
                                                             "rückgängig machen."));
              }
              catch (Exception e)
              {
                Logger.error("unable to notify user",e);
              }
              config.setTanMedia(choosen);
            }
          }
          close();
        }
        catch (RemoteException e)
        {
          Logger.error("unable to apply data",e);
          throw new ApplicationException(i18n.tr("Fehler beim Übernehmen des TAN-Mediums"));
        }
      }
    },null,true,"ok.png");
    buttons.addButton(i18n.tr("Abbrechen"),new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
    group.addButtonArea(buttons);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return choosen;
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
   * Erzeugt eine Combo-Box mit der Auswahl der verfuegbaren TAN-Medien.
   * @return Auswahl-Feld.
   * @throws ApplicationException
   */
  private SelectInput getMedia() throws ApplicationException
  {
    if (this.media != null)
      return this.media;

    String[] list = new String[0];
    if (this.config != null)
    {
      try
      {
        list = this.config.getTanMedias();
      }
      catch (Exception e)
      {
        Logger.error("unable to get previous tan medias",e);
      }
    }
    
    this.media = new SelectInput(list,null);
    this.media.setEditable(true);
    return this.media;
  }
}


/*********************************************************************
 * $Log: TanMediaDialog.java,v $
 * Revision 1.3  2011/12/06 22:22:19  willuhn
 * @N BUGZILLA 1151 - Name des aktuellen Kontos anzeigen
 *
 * Revision 1.2  2011-07-25 07:57:09  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2011-05-09 09:35:15  willuhn
 * @N BUGZILLA 827
 *
 **********************************************************************/