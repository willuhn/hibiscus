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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kapott.hbci.sepa.SepaVersion;
import org.kapott.hbci.sepa.SepaVersion.Type;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog zum Auswaehlen einer SEPA PAIN-Version.
 */
public class PainVersionDialog extends AbstractDialog
{
  private final static int WINDOW_WIDTH = 400;

  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  private Type type               = null;
  private SepaVersion painVersion = null;
  private Button ok               = null;

  /**
   * ct.
   * @param type der zu exportierende PAIN-Type.
   */
  public PainVersionDialog(Type type)
  {
    super(PainVersionDialog.POSITION_CENTER);
    this.setTitle(i18n.tr("SEPA XML-Version"));
    this.type = type;
    this.setSize(WINDOW_WIDTH,SWT.DEFAULT);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Container c = new SimpleContainer(parent);
    c.addText(i18n.tr("Bitte wählen Sie die zu verwendende SEPA XML-Version."),true);
    
    final SelectInput version = this.getPainVersionInput();
    final LabelInput msg      = this.getMessage();
    
    c.addInput(version);
    c.addInput(msg);
    
    ButtonArea buttons = new ButtonArea();
    this.ok = new Button(i18n.tr("Übernehmen"),new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        painVersion = (SepaVersion) version.getValue();
        if (painVersion == null)
        {
          msg.setValue(i18n.tr("Bitte wählen Sie eine SEPA XML-Version aus."));
          return;
        }
        close();
      }
    },null,true,"ok.png");
    buttons.addButton(ok);
    
    buttons.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
    
    c.addButtonArea(buttons);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,SWT.DEFAULT));
  }
  
  /**
   * Liefert ein Auswahlfeld mit der zu verwendenden PAIN-Version.
   * @return Auswahlfeld mit der PAIN-Version.
   */
  private SelectInput getPainVersionInput()
  {
    List<SepaVersion> list = SepaVersion.getKnownVersions(type);
    final SelectInput select = new SelectInput(list,SepaVersion.findGreatest(list));
    select.setAttribute("file");
    select.setName(i18n.tr("Schema-Version der SEPA XML-Datei"));
    select.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        if (ok != null)
          ok.setEnabled(select.getValue() != null);
      }
    });
    return select;
  }
  
  /**
   * Liefert ein Label fuer Fehlermeldungen.
   * @return ein Label fuer Fehlermeldungen.
   */
  private LabelInput getMessage()
  {
    LabelInput label = new LabelInput("");
    label.setColor(Color.ERROR);
    label.setName("");
    return label;
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return this.painVersion;
  }
}


