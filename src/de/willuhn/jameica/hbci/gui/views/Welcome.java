/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/Welcome.java,v $
 * $Revision: 1.14 $
 * $Date: 2004/10/08 13:37:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.controller.WelcomeControl;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * 
 */
public class Welcome extends AbstractView
{

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception
  {
		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
		WelcomeControl control = new WelcomeControl(this);

		GUI.getView().setTitle(i18n.tr("Hibiscus - HBCI-Onlinebanking"));

		Composite comp = new Composite(getParent(),SWT.NONE);
		comp.setBackground(Color.BACKGROUND.getSWTColor());
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout(3,false));

		new Headline(comp,i18n.tr("Offene Überweisungen"));
		Label sep = new Label(comp,SWT.SEPARATOR);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.verticalSpan = 3;
		sep.setLayoutData(gd);
		new Headline(comp,i18n.tr("Konten"));

		control.getOffeneUeberweisungen().paint(comp);
		control.getKontoStats().paint(comp);	
		
		control.getQuickLinks().paint(comp);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException
  {
  }

}


/**********************************************************************
 * $Log: Welcome.java,v $
 * Revision 1.14  2004/10/08 13:37:48  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2004/09/13 20:54:38  willuhn
 * @N bg color
 *
 * Revision 1.12  2004/07/25 17:15:05  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.11  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.10  2004/07/20 23:31:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/05/25 23:23:18  willuhn
 * @N UeberweisungTyp
 * @N Protokoll
 *
 * Revision 1.8  2004/05/23 15:33:10  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2004/04/12 19:15:31  willuhn
 * @C refactoring
 *
 * Revision 1.6  2004/04/05 23:28:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.3  2004/02/22 20:04:53  willuhn
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.2  2004/02/20 20:45:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/09 13:06:03  willuhn
 * @C misc
 *
 **********************************************************************/