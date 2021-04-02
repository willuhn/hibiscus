/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.pintan;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.SelectInput;
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

  private String options      = null;
  private String choosen      = null;
  
  /**
   * ct.
   * @param config die PINTAN-Config.
   * @param options Mit Pipe-Symbol getrennte Liste der Medien-Bezeichnungen, wie sie von der Bank kam.
   */
  public TanMediaDialog(PinTanConfig config, String options)
  {
    super(TanMediaDialog.POSITION_CENTER);
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
    
    this.save = new CheckboxInput(true);
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

    Set<String> set = new HashSet<String>();
    
    // Die Namen von der Bank
    if (this.options != null)
    {
      String[] names = this.options.split("\\|");
      for (String s:names)
      {
        set.add(s);
      }
    }

    // Und jetzt noch die gespeicherten reinmergen.
    if (this.config != null)
    {
      try
      {
        String[] names = this.config.getTanMedias();
        for (String s:names)
        {
          set.add(s);
        }
      }
      catch (Exception e)
      {
        Logger.error("unable to get previous tan medias",e);
      }
    }
    
    List<String> result = new ArrayList<String>();
    result.addAll(set);
    Collections.sort(result);
    
    this.media = new SelectInput(result,null);
    this.media.setEditable(true);
    return this.media;
  }
}
