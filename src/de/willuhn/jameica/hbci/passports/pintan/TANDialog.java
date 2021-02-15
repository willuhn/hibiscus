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
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.dialogs.PasswordDialog;
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
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Dialog für die TAN-Eingabe.
 * Es muss weder Text, noch Titel oder LabelText gesetzt werden.
 * Das ist alles schon drin.
 */
public class TANDialog extends PasswordDialog
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  protected final static int WINDOW_WIDTH  = 550;
  protected final static int WINDOW_HEIGHT = 300;

  protected PinTanConfig config = null;
  private HibiscusDBObject context = null;
  
  /**
   * ct.
   * @param config die PINTAN-Config.
   * @throws RemoteException
   */
  public TANDialog(PinTanConfig config) throws RemoteException
  {
    super(TANDialog.POSITION_CENTER);
    
    this.config = config;
		this.setSize(WINDOW_WIDTH,SWT.DEFAULT);

    // Deaktivierung der Anzeige von Sternen im TAN-Dialog.
    setShowPassword(this.config != null && this.config.getShowTan());
    
    setLabelText(i18n.tr("TAN"));
    
    // Einmal aufrufen, damit der Text gesetzt wird.
    setText(null);

    String s = null;
    try
    {
      final BeanService service = Application.getBootLoader().getBootable(BeanService.class);
      final SynchronizeSession session = service.get(HBCISynchronizeBackend.class).getCurrentSession();
      final Konto konto = session != null ? session.getKonto() : null;
      
      if (konto != null)
      {
        s = konto.getBezeichnung();
        String name = HBCIProperties.getNameForBank(konto.getBLZ());
        if (name != null && name.length() > 0)
          s += " [" + name + "]";
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

    if (s != null) setTitle(i18n.tr("TAN-Eingabe - Konto {0}",s));
    else           setTitle(i18n.tr("TAN-Eingabe"));
  
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#paint(org.eclipse.swt.widgets.Composite)
   * BUGZILLA 738
   */
  protected void paint(Composite parent) throws Exception
  {
    super.paint(parent);
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,WINDOW_HEIGHT));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.PasswordDialog#checkPassword(java.lang.String)
   */
  protected boolean checkPassword(String password)
	{
		if (password == null || password.length() == 0)
		{
			setErrorText(i18n.tr("Bitte geben Sie eine TAN ein.") + " " + getRetryString());
			return false;
		}
		return true;
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
   * BUGZILLA 150
   * @see PasswordDialog#setText(String)
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

    String ctx = this.context != null ? HBCIContext.toString(this.context) : null;
    if (ctx != null)
    {
      text += ("\n\n" + i18n.tr("Auftrag:\n{0}",ctx));
    }
    else
    {
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

      if (s != null)
        text += ("\n\n" + i18n.tr("Konto:\n{0}",s));
    }

    super.setText(text);
  }
  
  /**
	 * Liefert einen locale String mit der Anzahl der Restversuche.
	 * z.Bsp.: "Noch 2 Versuche.".
   * @return String mit den Restversuchen.
   */
  private String getRetryString()
	{
		String retries = getRemainingRetries() > 1 ? i18n.tr("Versuche") : i18n.tr("Versuch");
		return (i18n.tr("Noch") + " " + getRemainingRetries() + " " + retries + ".");
	}
}
