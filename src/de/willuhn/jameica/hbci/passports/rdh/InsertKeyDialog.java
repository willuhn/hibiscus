/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passports/rdh/InsertKeyDialog.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/07/20 08:46:28 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passports.rdh;

import java.io.File;

import de.willuhn.jameica.gui.dialogs.WaitDialog;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Dialog, der den User zur Eingabe der Schluesseldiskette auffordert.
 */
public class InsertKeyDialog extends WaitDialog
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  private File file = null;

  /**
   * @param f die Schluesseldatei.
   */
  public InsertKeyDialog(File f)
  {
    super(InsertKeyDialog.POSITION_CENTER);
    this.file = f;
    setTitle(i18n.tr("Schlüsseldiskette/USB-Stick einlegen"));
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
                   "Bitte legen Sie die Diskette/den USB-Stick ein.\n" +
                   "\nDateiname: {0}\n",this.file.getAbsolutePath());
  }

}


/*********************************************************************
 * $Log: InsertKeyDialog.java,v $
 * Revision 1.2  2011/07/20 08:46:28  willuhn
 * @N GUI poliert
 *
 * Revision 1.1  2010/06/17 11:26:48  willuhn
 * @B In HBCICallbackSWT wurden die RDH-Passports nicht korrekt ausgefiltert
 * @C komplettes Projekt "hbci_passport_rdh" in Hibiscus verschoben - es macht eigentlich keinen Sinn mehr, das in separaten Projekten zu fuehren
 * @N BUGZILLA 312
 * @N Neue Icons in Schluesselverwaltung
 * @N GUI-Polish in Schluesselverwaltung
 *
 * Revision 1.3  2009/03/29 22:25:56  willuhn
 * @B Warte-Dialog wurde nicht angezeigt, wenn Schluesseldiskette nicht eingelegt
 *
 * Revision 1.2  2007/03/21 13:48:50  willuhn
 * @N new abstract "WaitDialog"
 * @N force redraw in backgroundtask monitor/statusbar
 *
 * Revision 1.1  2006/12/21 12:10:55  willuhn
 * @N new "Insert key" dialog
 *
 **********************************************************************/