/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import java.io.FileInputStream;
import java.io.InputStreamReader;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.KontoControl;
import de.willuhn.jameica.hbci.passports.pintan.PhotoTANDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Zeigt eine Liste mit den vorhandenen Bankverbindungen an.
 */
public class KontoList extends AbstractView
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();


  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  public void bind() throws Exception
  {
    FileInputStream fis = new FileInputStream("/work/willuhn/git/hbci4java/src/test/resources/org/kapott/hbci4java/secmech/TestMatrixCode-001.txt");
    InputStreamReader reader = new InputStreamReader(fis,"UTF-8");
    
    StringBuilder sb = new StringBuilder();
    char[] buf = new char[1024];
    int read = 0;
    while ((read = reader.read(buf)) != -1)
    {
      sb.append(buf,0,read);
    }
    fis.close();
    PhotoTANDialog d = new PhotoTANDialog(null,sb.toString());
    d.setText("Smart-TAN photo\nChallenge\n\n1. Stecken Sie Ihre Chipkarte in den SmatrtTAN-Leser und drücken Sie Scan.\n2.Halten Sie den SmartTAN-Leser so vor die ashdf aldfhd,.fjg skdfljhglksehrgl kjsdfljksdlfgh sldkjfgh sdlkjfh lskdjfhgl ksdjhg lkjsdhgfl ksjdh flgkj sdhlk ghsdlkfjh lksdjh flk jsdlkjfgh lksdh fglksjdhflkg sldköfh dsö fsdölkfjg ösdfjksdfjkg löksdfjgpw85 ösfojgh ösdfhög jfhög flköhg ösdfkjh ödfkjgh ökdg hsdöfgh ödshgalsdf lauigh ldaioqzwero  347ahsdvjklhgasdkufg aslhgb asdgvf asdflg\nasdhasljdgfasgfuzqwgerfg jhasdgfegzrfl hadf\naskljdf lasdfhgasdkjhfg ljsdhfhj sdfjdsljj sd\naskdjf asdjfg fsdjasdjfhgljasdf lj adlfgh ldf");
    d.open();

		final KontoControl control = new KontoControl(this);
		GUI.getView().setTitle(i18n.tr("Vorhandene Konten"));

    control.getKontoListe().paint(getParent());
  }
}
