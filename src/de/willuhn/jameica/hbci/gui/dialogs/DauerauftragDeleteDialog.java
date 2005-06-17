/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/DauerauftragDeleteDialog.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/06/17 08:25:05 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.gui.dialogs;

import java.util.Date;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.DialogInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

// BUGZILLA #18 http://www.willuhn.de/bugzilla/show_bug.cgi?id=18
/**
 * Oeffnet einen Dialog zur Auswahl des Ziel-Datums zum Loeschen des
 * Dauerauftrages.
 */
public class DauerauftragDeleteDialog extends AbstractDialog {

	private I18N i18n;

	private Date date              = null;
  private DialogInput dateInput  = null;
  private CheckboxInput box      = null;
  private LabelInput comment     = null;

  /**
   * @param position
   */
  public DauerauftragDeleteDialog(int position) {
    super(position);

		i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    this.setTitle(i18n.tr("Zieldatum"));
    
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception {

		LabelGroup group = new LabelGroup(parent,i18n.tr("Zieldatum zur Löschung des Dauerauftrages"));
			
  	group.addText(i18n.tr("Bitte wählen Sie das Datum aus, zu dem Sie den Dauerauftrag löschen wollen\n" +                          "Hinweis: Es ist durchaus möglich, dass Ihre Bank das Löschen eines " +
                          "Dauerauftrages zu einem definierten Datum nicht unterstützt. Wählen Sie " +
                          "in diesem Fall bitte \"Zum nächstmöglichen Zeitpunkt\""),true);
    
    box = new CheckboxInput(false);
    box.addListener(new Listener()
    {
      public void handleEvent(Event event)
      {
        Boolean b = (Boolean) box.getValue();
        if (b.booleanValue())
          dateInput.disableButton();
        else
          dateInput.enableButton();
      }
    });

    group.addCheckbox(box,i18n.tr("Zum nächstmöglichen Zeitpunkt"));

    CalendarDialog cd = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
    cd.setTitle(i18n.tr("Zieldatum"));
    cd.addCloseListener(new Listener() {
      public void handleEvent(Event event) {
        if (event == null || event.data == null || !(event.data instanceof Date))
          return;

        date = (Date) event.data;
        // Wir rechnen nicht mit gestern sonder mit heute, weil "date"
        // keine Uhrzeit enthaelt, "today" jedoch schon und so selbst dann
        // ein Fehler kommen wuerde, wenn der User den aktuellen Tag auswaehlt
        Date today = new Date(System.currentTimeMillis() - (1000l * 60 * 60 * 24));
        if (date != null && date.before(today))
        {
          comment.setValue(i18n.tr("Datum darf nicht in der Vergangenheit liegen"));
          return;
        }

        comment.setValue("");
        dateInput.setValue(date);
        dateInput.setText(HBCI.DATEFORMAT.format(date));
      }
    });

    cd.setDate(date);
    dateInput = new DialogInput(HBCI.DATEFORMAT.format(date),cd);
    dateInput.disableClientControl();
    dateInput.setValue(date);
		group.addLabelPair(i18n.tr("Dauerauftrag löschen zum"),dateInput);

    comment = new LabelInput("");
    group.addLabelPair("",comment);
    
    ButtonArea b = new ButtonArea(parent,2);
		b.addButton(i18n.tr("Übernehmen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        Boolean b = (Boolean) box.getValue();
        if (b.booleanValue())
        {
          date = null;
        }
        else
        {
          date = (Date) dateInput.getValue();
          // Wir rechnen nicht mit gestern sonder mit heute, weil "date"
          // keine Uhrzeit enthaelt, "today" jedoch schon und so selbst dann
          // ein Fehler kommen wuerde, wenn der User den aktuellen Tag auswaehlt
          Date today = new Date(System.currentTimeMillis() - (1000l * 60 * 60 * 24));
          if (date != null && date.before(today))
          {
            comment.setValue(i18n.tr("Datum darf nicht in der Vergangenheit liegen"));
            return;
          }
        }
				close();
      }
    });
		b.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    });
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception {
    return date;
  }


}


/**********************************************************************
 * $Log: DauerauftragDeleteDialog.java,v $
 * Revision 1.4  2005/06/17 08:25:05  web0
 * *** empty log message ***
 *
 * Revision 1.3  2005/06/08 10:24:41  web0
 * @B dialog muss bei "naechstmoeglicher Zeitpunkt" null liefern
 *
 * Revision 1.2  2005/06/07 22:19:57  web0
 * @B bug 49
 *
 * Revision 1.1  2005/06/07 21:57:25  web0
 * @B bug 18
 *
 **********************************************************************/