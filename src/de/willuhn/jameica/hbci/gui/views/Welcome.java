/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/Welcome.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/02/22 20:04:53 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.views;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Text;
import de.willuhn.jameica.gui.views.AbstractView;
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
		GUI.setTitleText(I18N.tr("HBCI"));
  	Text text = new Text(I18N.tr("HBCI-Plugin für Jameica"));
  	text.paint(getParent());
  	
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