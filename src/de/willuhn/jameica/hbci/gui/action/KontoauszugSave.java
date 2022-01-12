/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.action;

import java.io.File;
import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.jameica.hbci.server.KontoauszugPdfUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Speichern eines Kontoauszuges.
 */
public class KontoauszugSave implements Action
{
  private final static Settings SETTINGS = new Settings(KontoauszugSave.class);
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Kontoauszug))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie den zu speichernden Kontoauszug"));
    
    Kontoauszug k = (Kontoauszug) context;
    
    try
    {
      FileDialog fd = new FileDialog(GUI.getShell(),SWT.SAVE);
      fd.setText(i18n.tr("Bitte geben Sie eine Datei ein, in der der Kontoauszug gespeichert werden soll."));
      fd.setOverwrite(true);
      
      String name = k.getDateiname();
      if (name == null || name.length() == 0)
      {
        String s = KontoauszugPdfUtil.createPath(k.getKonto(),k);
        File f = new File(s);
        name = f.getName();
      }
      fd.setFileName(name);

      String path = SETTINGS.getString("lastdir",System.getProperty("user.home"));
      if (path != null && path.length() > 0)
        fd.setFilterPath(path);

      final String s = fd.open();
      
      if (s == null || s.length() == 0)
        throw new OperationCanceledException();

      final File file = new File(s);
      
      // Wir merken uns noch das Verzeichnis vom letzten mal
      SETTINGS.setAttribute("lastdir",file.getParent());

      KontoauszugPdfUtil.store(k,file);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Kontoauszug gespeichert"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (RemoteException re)
    {
      Logger.error("unable to save file",re);
      throw new ApplicationException(i18n.tr("Speichern des Kontoauszuges fehlgeschlagen: {0}",re.getMessage()));
    }
  }

}


