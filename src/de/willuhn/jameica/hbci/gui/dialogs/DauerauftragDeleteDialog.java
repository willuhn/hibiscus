/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/DauerauftragDeleteDialog.java,v $
 * $Revision: 1.9 $
 * $Date: 2011/05/06 12:35:24 $
 * $Author: willuhn $
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
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
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
public class DauerauftragDeleteDialog extends AbstractDialog
{
	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

	private Date date              = null;
  private DialogInput dateInput  = null;
  private CheckboxInput box      = null;
  private LabelInput comment     = null;

  /**
   * @param position
   */
  public DauerauftragDeleteDialog(int position) {
    super(position);
    this.setTitle(i18n.tr("Zieldatum"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
		Container group = new SimpleContainer(parent);

		group.addHeadline(i18n.tr("Zieldatum zur L�schung des Dauerauftrages"));
  	group.addText(i18n.tr("Bitte w�hlen Sie das Datum aus, zu dem Sie den Dauerauftrag l�schen wollen\n" +                          "Hinweis: Es ist durchaus m�glich, dass Ihre Bank das L�schen eines\n" +
                          "Dauerauftrages zu einem definierten Datum nicht unterst�tzt.\n" +
                          "W�hlen Sie in diesem Fall bitte \"Zum n�chstm�glichen Zeitpunkt\""),true);
    
    box = new CheckboxInput(true);
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

    group.addCheckbox(box,i18n.tr("Zum n�chstm�glichen Zeitpunkt"));

    CalendarDialog cd = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
    cd.setTitle(i18n.tr("Zieldatum"));
    cd.addCloseListener(new Listener() {
      public void handleEvent(Event event) {
        if (event == null || event.data == null || !(event.data instanceof Date))
          return;

        date = (Date) event.data;
        if (date == null)
        {
          comment.setValue(i18n.tr("Bitte w�hlen Sie ein Zieldatum aus"));
          dateInput.setValue(null);
          dateInput.setText(null);
          return;
        }

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

    // BUGZILLA #85 http://www.willuhn.de/bugzilla/show_bug.cgi?id=85
    cd.setDate(date == null ? new Date() : date);
    dateInput = new DialogInput(date == null ? null : HBCI.DATEFORMAT.format(date),cd);
    dateInput.disableClientControl();
    dateInput.setValue(date);
		group.addLabelPair(i18n.tr("Dauerauftrag l�schen zum"),dateInput);

    comment = new LabelInput("");
    comment.setColor(Color.ERROR);
    group.addLabelPair("",comment);
    
    ButtonArea b = new ButtonArea();
		b.addButton(i18n.tr("Jetzt bei der Bank l�schen"), new Action()
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
          if (date == null)
          {
            comment.setValue(i18n.tr("Bitte w�hlen Sie ein Datum aus."));
            return;
          }
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
    },null,false,"user-trash-full.png");
		b.addButton(i18n.tr("Abbrechen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        throw new OperationCanceledException();
      }
    },null,true,"process-stop.png");
		
		group.addButtonArea(b);
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
 * Revision 1.9  2011/05/06 12:35:24  willuhn
 * @R Nicht mehr noetig - macht AbstractDialog jetzt selbst
 *
 * Revision 1.8  2011-03-07 10:33:53  willuhn
 * @N BUGZILLA 999
 *
 * Revision 1.7  2006/06/06 21:42:21  willuhn
 * @N Zeilenumbrueche in Dialogen (fuer Windows)
 *
 * Revision 1.6  2005/06/23 17:05:33  web0
 * @B bug 85
 *
 * Revision 1.5  2005/06/21 20:11:10  web0
 * @C cvs merge
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