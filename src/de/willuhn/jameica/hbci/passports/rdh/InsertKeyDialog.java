/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh;

import java.io.File;

import de.willuhn.jameica.gui.dialogs.WaitDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Dialog, der den User zur Eingabe der Schluesseldatei auffordert.
 */
public class InsertKeyDialog extends WaitDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private final File file;

  /**
   * @param f die Schluesseldatei.
   */
  public InsertKeyDialog(File f)
  {
    super(InsertKeyDialog.POSITION_CENTER);
    this.file = f;
    setTitle(i18n.tr("Schlüsseldatei/USB-Stick einlegen"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.WaitDialog#check()
   */
  protected boolean check()
  {
    if (file.exists() && file.canRead() && file.canWrite())
    {
      // Wir warten hier noch kurz, damit das Mounten sicher abgeschlossen ist
      try
      {
        Thread.sleep(800l);
      }
      catch (Exception e)
      {
        // dann halt nicht
      }
      return true;
    }
    return false;
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.WaitDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return new Boolean(file.exists() && file.canRead() && file.canWrite());
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.WaitDialog#getText()
   */
  public String getText()
  {
    return i18n.tr("Die Schlüsseldatei wurde nicht gefunden.\n" +
                   "Bitte legen Sie den USB-Stick ein.\n" +
                   "\nDateiname: {0}\n",this.file.getAbsolutePath());
  }

}
