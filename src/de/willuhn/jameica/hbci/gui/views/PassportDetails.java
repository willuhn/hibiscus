/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/views/Attic/PassportDetails.java,v $
 * $Revision: 1.1 $
 * $Date: 2004/02/11 00:11:20 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.views;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.Application;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.CheckboxInput;
import de.willuhn.jameica.gui.parts.Input;
import de.willuhn.jameica.gui.parts.LabelGroup;
import de.willuhn.jameica.gui.views.AbstractView;
import de.willuhn.jameica.hbci.gui.controller.PassportControl;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog ueber den die Details des Passport konfiguriert werden koennen.
 */
public class PassportDetails extends AbstractView {

  /**
   * ct.
   * @param parent
   */
  public PassportDetails(Composite parent) {
    super(parent);
  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#bind()
   */
  public void bind() throws Exception {
  	addHeadline("Eigenschaften des Sicherheitsmediums");
  	
  	PassportControl control = new PassportControl(this);
  	
  	LabelGroup group = new LabelGroup(getParent(),I18N.tr("Eigenschaften"));

		try {
			LinkedHashMap fields = control.getParamLabelPairs();
			Iterator i = fields.keySet().iterator();
			Input input = null;
			String name = null;
			while (i.hasNext())
			{
				name = (String) i.next();
				input = (Input) fields.get(name);
				group.addLabelPair(I18N.tr(name),input);
			}

			fields = control.getParamCheckboxes();
			i = fields.keySet().iterator();
			CheckboxInput input2 = null;
			while (i.hasNext())
			{
				name = (String) i.next();
				input2 = (CheckboxInput) fields.get(name);
				group.addCheckbox(input2,I18N.tr(name));
			}
		}
		catch (RemoteException e)
		{
			Application.getLog().error("error while loading passport params",e);
			GUI.setActionText(I18N.tr("Fehler beim Laden der Einstellungen"));
		}

		// und noch die Abschicken-Knoepfe
		ButtonArea buttonArea = new ButtonArea(getParent(),2);
		buttonArea.addCancelButton(control);
		buttonArea.addStoreButton(control);

  }

  /**
   * @see de.willuhn.jameica.gui.views.AbstractView#unbind()
   */
  public void unbind() throws ApplicationException {

  }

}


/**********************************************************************
 * $Log: PassportDetails.java,v $
 * Revision 1.1  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 **********************************************************************/