/**********************************************************************
 *
 * Copyright (c) 2021 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passports.pintan;

import java.rmi.RemoteException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.PasswordInput;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.NotificationPanel;
import de.willuhn.jameica.gui.parts.NotificationPanel.Type;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.MetaKey;
import de.willuhn.jameica.hbci.passports.pintan.rmi.PinTanConfig;
import de.willuhn.jameica.hbci.rmi.HibiscusDBObject;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.hbci.HBCIContext;
import de.willuhn.jameica.hbci.synchronize.SynchronizeSession;
import de.willuhn.jameica.hbci.synchronize.hbci.HBCISynchronizeBackend;
import de.willuhn.jameica.services.BeanService;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog für die TAN-Eingabe.
 * Es muss weder Text, noch Titel oder LabelText gesetzt werden.
 * Das ist alles schon drin.
 */
public class TANDialog extends AbstractDialog
{
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  protected final static int WINDOW_WIDTH  = 500;
  protected final static int WINDOW_HEIGHT = 550;

  protected PinTanConfig config = null;
  private HibiscusDBObject context = null;
  
  private NotificationPanel panel = null;
  
  private PasswordInput tanInput = null;
  private Button okButton = null;
  
  private String konto = null;
  private String text = null;

  private boolean showTan = true;
  private String tan = null;
  private boolean needTan = true;
  
  /**
   * ct.
   * @param config die PINTAN-Config.
   * @throws RemoteException
   */
  public TANDialog(PinTanConfig config) throws RemoteException
  {
    super(TANDialog.POSITION_CENTER);
    
    this.config = config;
    this.setTitle(this.needTan ? i18n.tr("TAN-Eingabe") : i18n.tr("Auftrag freigeben"));
    this.setText(null); // Fuer die Generierung des Default-Textes
		this.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);

    try
    {
      this.showTan = this.config != null && this.config.getShowTan();
    }
    catch (Exception e)
    {
      Logger.error("unable to determine if TAN should be shown",e);
    }

    try
    {
      final BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      final SynchronizeSession session = service.get(HBCISynchronizeBackend.class).getCurrentSession();
      final Konto konto = session != null ? session.getKonto() : null;
      
      if (konto != null)
      {
        this.konto = konto.getBezeichnung();
        String name = HBCIProperties.getNameForBank(konto.getBLZ());
        if (name != null && name.length() > 0)
          this.konto += " [" + name + "]";

        this.setTitle(i18n.tr(this.needTan ? "TAN-Eingabe - Konto {0}" : "Auftrag freigeben - Konto {0}",this.konto));
      }
      
      if (session != null)
      {
        this.addCloseListener(new Listener() {
          
          @Override
          public void handleEvent(Event event)
          {
            if (event.detail == SWT.CANCEL)
            {
              // TAN-Dialog wurde abgebrochen
              if (context != null)
              {
                try
                {
                  Logger.info("mark job as tan cancelled: " + HBCIContext.toString(context));
                  MetaKey.TAN_CANCEL.set(context,HBCI.LONGDATEFORMAT.format(new Date()));
                }
                catch (Exception e)
                {
                  Logger.error("unable to set tan cancel flag in object",e);
                }
              }
            }
          }
        });
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to determine current konto",e);
    }
  }
  
  /**
   * Liefert das Notification-Panel.
   * @return das Notification-Panel.
   */
  private NotificationPanel getPanel()
  {
    if (this.panel != null)
      return this.panel;
    
    this.panel = new NotificationPanel();
    return this.panel;
  }
  
  /**
   * Liefert das Eingabefeld fuer die TAN.
   * @return  das Eingabefeld fuer die TAN.
   */
  private PasswordInput getTANInput()
  {
    if (this.tanInput != null)
      return this.tanInput;

    this.tanInput = new PasswordInput(this.tan);
    this.tanInput.setName(i18n.tr("Ihre TAN-Eingabe"));
    this.tanInput.setShowPassword(this.showTan);
    this.tanInput.focus();
    return this.tanInput;
  }
  
  /**
   * Liefert den OK-Button.
   * @return der OK-Button.
   */
  private Button getOkButton()
  {
    if (this.okButton != null)
      return this.okButton;
    
    this.okButton = new Button("    " + i18n.tr("OK") + "    ",new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        tan = (String) getTANInput().getValue();
        close();
      }
    },null,true,"ok.png");
    this.okButton.setEnabled(!this.needTan);
    return this.okButton;
  }
  
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    // Oberer Bereich
    {
      final Container c = new SimpleContainer(parent);
      c.addPart(this.getPanel());
      
      if (this.needTan)
        setInfoText(Type.INFO,i18n.tr("Bitte geben Sie die TAN ein."));
      else
        setInfoText(Type.INFO,i18n.tr("Bitte geben Sie den Auftrag auf Ihrem Smartphone frei."));
      
      final String auftrag = this.context != null ? HBCIContext.toString(this.context) : null;
      final boolean haveAuftrag = StringUtils.trimToNull(auftrag) != null;
      
      if (this.konto != null || haveAuftrag)
        c.addHeadline(i18n.tr("Konto und Auftrag"));
      
      if (this.konto != null)
        c.addText(i18n.tr("Konto") + ": " + this.konto,true);
      
      if (haveAuftrag)
        c.addText(i18n.tr("Auftrag") + ": " + auftrag,true);
    }

    // Oberer Erweiterungsbereich
    this.extendTop(new SimpleContainer(parent,false,1));

    {
      final Container c = new SimpleContainer(parent,true,1);
      c.addHeadline(i18n.tr("Informationen der Bank"));
      final StyledText msg = new StyledText(c.getComposite(),SWT.WRAP | SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
      msg.setText(this.text);
      msg.setLayoutData(new GridData(GridData.FILL_BOTH));
      msg.setMargins(5,5,5,5);
      msg.setEditable(false);
    }

    final Container c = new SimpleContainer(parent);
    
    if (this.needTan)
    {
      final PasswordInput tan = this.getTANInput();
      c.addInput(tan);
      
      tan.getControl().addKeyListener(new KeyAdapter() {
        /**
         * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
         */
        @Override
        public void keyReleased(KeyEvent e)
        {
          getOkButton().setEnabled(StringUtils.trimToNull((String) tan.getValue()) != null);
        }
      });
    }
    
    // Unterer Erweiterungsbereich
    this.extendBottom(new SimpleContainer(parent,false,1));
    
    // Gemeinsamer unterer Bereich
    Container bottom = new SimpleContainer(parent);
    final ButtonArea buttons = new ButtonArea();
    buttons.addButton(this.getOkButton());
    buttons.addButton(new Cancel());
    bottom.addButtonArea(buttons);

    addShellListener(new ShellListener() {
      public void shellClosed(ShellEvent e) {
        throw new OperationCanceledException("dialog cancelled via close button");
      }
      public void shellActivated(ShellEvent e) {}
      public void shellDeactivated(ShellEvent e) {}
      public void shellDeiconified(ShellEvent e) {}
      public void shellIconified(ShellEvent e) {}
    });
    
    this.getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,WINDOW_HEIGHT));
  }
  
  /**
   * Erweitert den Dialog unten.
   * @param c der Container.
   * @throws Exception
   */
  protected void extendBottom(Container c) throws Exception
  {
  }

  /**
   * Erweitert den oben..
   * @param c der Container.
   * @throws Exception
   */
  protected void extendTop(Container c) throws Exception
  {
  }

  /**
   * Uebernimmt die TAN manuell.
   * @param tan die TAN.
   */
  public final void setTAN(final String tan)
  {
    this.tan = tan;
    getTANInput().setValue(tan);
    getOkButton().setEnabled(StringUtils.trimToNull(tan) != null || !this.needTan);
  }
  
  /**
   * Legt fest, ob die TAN angezeigt werden soll.
   * @param show true, wenn die TAN angezeigt werden soll.
   */
  public void setShowTAN(boolean show)
  {
    this.showTan = show;
  }
  
  /**
   * Legt fest, ob überhaupt eine TAN benötigt wird.
   * @param t true, wenn eine benötigt wird (default=true).
   */
  public void setNeedTAN(boolean t)
  {
    this.needTan = t;
  }

  /**
   * Zeigt einen Hinweis-Text an.
   * @param type die Darstellungsform.
   * @param text der anzuzeigende Text.
   */
  public final void setInfoText(Type type, String text)
  {
    this.getPanel().setText(type,text);
  }


  /**
   * Speichert den zugehoerigen Auftrag, insofern ermittelbar.
   * @param context der zugehoerige Auftrag.
   */
  public void setContext(HibiscusDBObject context)
  {
    this.context = context;
  }
  
  /**
   * Liefert den zugehoerigen Auftrag, insofern ermittelbar.
   * @return transfer der zugehoerige Auftrag.
   */
  public HibiscusDBObject getContext()
  {
    return context;
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected Object getData() throws Exception
  {
    return this.tan;
  }
  
  /**
   * Speichert den anzuzeigenden Text.
   * @param text der anzuzeigende Text.
   */
  public void setText(String text)
  {
    if (text == null || text.length() == 0)
    {
      text = i18n.tr("Bitte geben Sie eine TAN-Nummer ein.");
    }
    else
    {
      // Der Text kann ein "challenge" von der Bank enthalten. Dieser kann Formatierungen enthalten.
      // Z.Bsp. "<b>Text</b>", "<li>..." usw. Siehe
      // FinTS_3.0_Security_Sicherheitsverfahren_PINTAN_Rel_20101027_final_version.pdf
      // Seite 127. Das muss noch beachtet werden
      // Eigentlich kann man in den BPD noch nachschauen, ob fuer das TAN-Verfahren "ischallengestructured=J",
      // aber das brauchen wir nicht. Wenn HTML-Tags drin stehen, ersetzen wir sie gegen Formatierungen.
      
      text = text.replaceAll("<br>","\n");
      text = text.replaceAll("<p>","\n\n");
      
      text = text.replaceAll("<p>","\n\n");
      
      text = text.replaceAll("<ul>","\n");
      text = text.replaceAll("</ul>","");
      text = text.replaceAll("<ol>","\n");
      text = text.replaceAll("</ol>","");
      text = text.replaceAll("</li>","\n");

      text = text.replaceAll("<li>","  - ");

      // Unterstuetzen wir noch nicht
      text = text.replaceAll("<b>","");
      text = text.replaceAll("</b>","");
      text = text.replaceAll("<i>","");
      text = text.replaceAll("</i>","");
      text = text.replaceAll("<u>","");
      text = text.replaceAll("</u>","");
    }
    this.text = text;
  }
}
