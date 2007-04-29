/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/Attic/UmsatzKategorieListeDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2007/04/29 10:19:59 $
 * $Author: jost $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by Heiner Jostkleigrewe
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.io.UmsatzKategorieKategorieliste;
import de.willuhn.jameica.hbci.io.UmsatzKategorieKomplettliste;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Dialog, ueber den die Umsatz-Kategorie-Liste im PDF-Format ausgegeben werden
 * kann.
 */
public class UmsatzKategorieListeDialog extends AbstractDialog
{

  private I18N i18n;

  private Input artDerListe = null;

  private CheckboxInput openFile = null;

  private List list = null;

  private Settings settings = null;

  private Konto k;

  private Date start;

  private Date end;

  public UmsatzKategorieListeDialog(List list, Konto k, Date start, Date end)
  {
    super(POSITION_CENTER);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources()
        .getI18N();
    setTitle(i18n.tr("Umsatz-Kategorien-Liste"));
    this.list = list;
    this.k = k;
    this.start = start;
    this.end = end;

    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    LabelGroup group = new LabelGroup(parent, i18n.tr("Umfang der Liste"));
    group.addText(i18n.tr("Bitte wählen Sie das gewünschte Listen-Layout aus"),
        true);

    Input layouts = getArtDerListe();
    group.addLabelPair(i18n.tr("Verfügbare Layouts:"), layouts);

    boolean listeEnabled = !(layouts instanceof LabelInput);

    if (listeEnabled)
    {
      group.addCheckbox(getOpenFile(), i18n.tr("Datei nach dem Export öffnen"));
    }

    ButtonArea buttons = new ButtonArea(parent, 2);
    Button button = new Button(i18n.tr("Starten"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        liste();
      }
    }, null, true);
    button.setEnabled(listeEnabled);
    buttons.addButton(button);
    buttons.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    });
  }

  /**
   * Erstellung der Liste starten.
   * 
   * @throws ApplicationException
   */
  private void liste() throws ApplicationException
  {
    final String auswahl = (String) getArtDerListe().getValue();
    // }
    // catch (Exception e)
    // {
    // Logger.error("Fehler bei der Erstellung der Liste", e);
    // throw new ApplicationException(i18n
    // .tr("Fehler beim Starten der Listenerstellung"), e);
    // }

    if (auswahl == null)
      throw new ApplicationException(i18n
          .tr("Bitte wählen Sie ein Listen-Layout aus"));

    settings.setAttribute("lastformat", auswahl);

    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setText(i18n.tr("Bitte geben Sie eine Dateinamen für die Liste ein."));
    String ext = "pdf";
    fd.setFileName(i18n.tr("umsatzkategorien-{0}." + ext, HBCI.FASTDATEFORMAT
        .format(new Date())));

    String path = settings
        .getString("lastdir", System.getProperty("user.home"));
    if (path != null && path.length() > 0)
      fd.setFilterPath(path);

    final String s = fd.open();

    if (s == null || s.length() == 0)
    {
      close();
      return;
    }

    final File file = new File(s);
    if (file.exists())
    {
      try
      {
        YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_MOUSE);
        d.setTitle(i18n.tr("Datei existiert bereits"));
        d.setText(i18n.tr("Möchten Sie die Datei überschreiben?"));
        Boolean choice = (Boolean) d.open();
        if (!choice.booleanValue())
        {
          // Dialog schliessen
          close();
          return;
        }
      }
      catch (Exception e)
      {
        // Dialog schliessen
        close();
        Logger.error("error while saving export file", e);
        throw new ApplicationException(i18n.tr(
            "Fehler beim Speichern der Export-Datei in {0}", s), e);
      }
    }

    // Wir merken uns noch das Verzeichnis vom letzten mal
    settings.setAttribute("lastdir", file.getParent());

    // Dialog schliessen
    final boolean open = ((Boolean) getOpenFile().getValue()).booleanValue();
    settings.setAttribute("open", open);
    close();

    BackgroundTask t = new BackgroundTask()
    {
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          // Der Exporter schliesst den OutputStream selbst
          OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
          if (auswahl.equals("Komplettliste"))
          {
            new UmsatzKategorieKomplettliste(os, monitor, list, k, start, end);
          }
          else if (auswahl.equals("Nur Kategorien"))
          {
            new UmsatzKategorieKategorieliste(os, monitor, list, k, start, end);
          }
          else
          {
            throw new ApplicationException("ungültige Auswahl der Liste");
          }
          if (open)
          {
            GUI.getDisplay().asyncExec(new Runnable()
            {
              public void run()
              {
                try
                {
                  new Program().handleAction(file);
                }
                catch (ApplicationException ae)
                {
                  Application.getMessagingFactory().sendMessage(
                      new StatusBarMessage(ae.getLocalizedMessage(),
                          StatusBarMessage.TYPE_ERROR));
                }
              }
            });
          }
        }
        catch (ApplicationException ae)
        {
          monitor.setStatusText(ae.getMessage());
          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          throw ae;
        }
        catch (Exception e)
        {
          monitor.setStatus(ProgressMonitor.STATUS_ERROR);
          Logger.error("error while writing objects to " + s, e);
          ApplicationException ae = new ApplicationException(i18n.tr(
              "Fehler bei der Erstellung der Liste nach {0}", s), e);
          monitor.setStatusText(ae.getMessage());
          throw ae;
        }
      }

      public void interrupt()
      {
      }

      public boolean isInterrupted()
      {
        return false;
      }
    };

    Application.getController().start(t);
  }

  /**
   * Liefert eine Checkbox.
   * 
   * @return Checkbox.
   */
  private CheckboxInput getOpenFile()
  {
    if (this.openFile == null)
      this.openFile = new CheckboxInput(settings.getBoolean("open", true));
    return this.openFile;
  }

  /**
   * Liefert eine Liste der verfuegbaren Layouts.
   * 
   * @return Liste der Layouts.
   * @throws Exception
   */
  private Input getArtDerListe()
  {
    if (artDerListe != null)
      return artDerListe;

    String lastFormat = settings.getString("lastformat", null);

    // Exp[] exp = (Exp[]) l.toArray(new Exp[size]);
    String[] layouts = { "Komplettliste", "Nur Kategorien" };
    artDerListe = new SelectInput(layouts, lastFormat);
    return artDerListe;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return null;
  }

}

/*******************************************************************************
 * $Log: UmsatzKategorieListeDialog.java,v $
 * Revision 1.1  2007/04/29 10:19:59  jost
 * Neu: PDF-Ausgabe der UmsÃ¤tze nach Kategorien
 *
 ******************************************************************************/
