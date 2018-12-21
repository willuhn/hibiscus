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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.filter.AddressFilter;
import de.willuhn.jameica.hbci.gui.parts.EmpfaengerList;
import de.willuhn.jameica.hbci.rmi.Address;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, ueber den man eine Adresse auswaehlen kann.
 */
public class AdresseAuswahlDialog extends AbstractDialog
{

	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
	private Address choosen        = null;
	private AddressFilter filter   = null;

  /**
   * ct.
   * @param position
   */
  public AdresseAuswahlDialog(int position)
  {
    this(position,null);
  }
  
	/**
   * ct.
   * @param position
   * @param filter optionale Angabe eines Adress-Filters.
   */
  public AdresseAuswahlDialog(int position, AddressFilter filter)
  {
    super(position);
    this.filter = filter;

		this.setTitle(i18n.tr("Adressbuch"));
    this.setSize(640,460);
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
    Action a = new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof Address))
          return;
        choosen = (Address) context;
        close();
      }
    };    


    Container c1 = new SimpleContainer(parent,true,1);
    final EmpfaengerList empf = new EmpfaengerList(a,this.filter,false);
    empf.setContextMenu(null);
    empf.setMulti(false);
    empf.removeFeature(FeatureSummary.class);
    
    final Button apply = new Button(i18n.tr("Übernehmen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        Object o = empf.getSelection();
        if (o == null || !(o instanceof Address))
          return;

        choosen = (Address) o;
        close();
      }
    },null,true,"ok.png");
    apply.setEnabled(false);
    empf.addSelectionListener(new Listener() {
      public void handleEvent(Event event)
      {
        apply.setEnabled(empf.getSelection() != null);
      }
    });
    
    empf.paint(c1.getComposite());

		ButtonArea b = new ButtonArea();
		b.addButton(apply);
		b.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
				throw new OperationCanceledException();
      }
    },null,false,"process-stop.png");
		
    Container c2 = new SimpleContainer(parent);
		c2.addButtonArea(b);
  }

  /**
   * Liefert das ausgewaehlte Konto zurueck oder <code>null</code> wenn der
   * Abbrechen-Knopf gedrueckt wurde.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return choosen;
  }
}
