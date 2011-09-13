/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/AdresseAuswahlDialog.java,v $
 * $Revision: 1.10 $
 * $Date: 2011/09/13 08:54:49 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
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
    this.setSize(600,360);
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
    final EmpfaengerList empf = new EmpfaengerList(a,this.filter);
    empf.setContextMenu(null);
    empf.setMulti(false);
    empf.setSummary(false);
    
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


/**********************************************************************
 * $Log: AdresseAuswahlDialog.java,v $
 * Revision 1.10  2011/09/13 08:54:49  willuhn
 * @C Uebernehmen-Button nur aktivieren, wenn etwas ausgewaehlt wurde
 *
 * Revision 1.9  2011-09-12 15:37:42  willuhn
 * @C GUI cleanup
 * @N Icons in Buttons anzeigen
 *
 * Revision 1.8  2011-05-06 12:35:24  willuhn
 * @R Nicht mehr noetig - macht AbstractDialog jetzt selbst
 *
 * Revision 1.7  2009/10/20 23:12:58  willuhn
 * @N Support fuer SEPA-Ueberweisungen
 * @N Konten um IBAN und BIC erweitert
 *
 * Revision 1.6  2009/03/13 00:25:12  willuhn
 * @N Code fuer Auslandsueberweisungen fast fertig
 *
 * Revision 1.5  2009/02/19 23:42:01  willuhn
 * @N Filter fuer Adressbuch zum Ausblenden von Adressen (z.Bsp. bei Auslandsueberweisungen alle ausblenden, die keine IBAN haben)
 *
 * Revision 1.4  2008/12/02 10:52:23  willuhn
 * @B DecimalInput kann NULL liefern
 * @B Double.NaN beruecksichtigen
 *
 * Revision 1.3  2007/04/23 18:07:15  willuhn
 * @C Redesign: "Adresse" nach "HibiscusAddress" umbenannt
 * @C Redesign: "Transfer" nach "HibiscusTransfer" umbenannt
 * @C Redesign: Neues Interface "Transfer", welches von Ueberweisungen, Lastschriften UND Umsaetzen implementiert wird
 * @N Anbindung externer Adressbuecher
 *
 * Revision 1.2  2006/02/21 23:55:32  willuhn
 * @N Update auf hbci4java rc6
 *
 * Revision 1.1  2005/08/16 21:33:13  willuhn
 * @N Kommentar-Feld in Adressen
 * @N Neuer Adress-Auswahl-Dialog
 * @B Checkbox "in Adressbuch speichern" in Ueberweisungen
 *
 **********************************************************************/