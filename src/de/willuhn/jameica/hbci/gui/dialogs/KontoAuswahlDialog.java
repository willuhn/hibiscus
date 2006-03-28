/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/KontoAuswahlDialog.java,v $
 * $Revision: 1.7 $
 * $Date: 2006/03/28 22:53:19 $
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

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.gui.parts.KontoList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, ueber den man ein Konto auswaehlen kann.
 */
public class KontoAuswahlDialog extends AbstractDialog
{

	private I18N i18n;
  private String text = null;
	private Konto choosen = null;
  
  private GenericIterator konten = null;

  /**
   * ct.
   * @param position
   */
  public KontoAuswahlDialog(int position)
  {
    this(null,position);
  }

  /**
   * ct.
   * @param konten Liste der anzuzeigenden Konten.
   * @param position
   */
  public KontoAuswahlDialog(GenericIterator konten, int position)
  {
    super(position);
    this.konten = konten;
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    this.setTitle(i18n.tr("Konto-Auswahl"));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  protected void paint(Composite parent) throws Exception
  {
		LabelGroup group = new LabelGroup(parent,i18n.tr("Verfügbare Konten"));
			
		if (text == null || text.length() == 0)
      text = i18n.tr("Bitte wählen Sie das gewünschte Konto aus.");
    group.addText(text,true);

    Action a = new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof Konto))
          return;
        choosen = (Konto) context;
        close();
      }
    };    
		final KontoList konten = this.konten != null ? new KontoList(this.konten,a) : new KontoList(a);
    konten.setContextMenu(null);
    konten.setMulti(false);
    konten.setSummary(false);
    konten.paint(parent);

		ButtonArea b = new ButtonArea(parent,2);
		b.addButton(i18n.tr("Übernehmen"), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
				Object o = konten.getSelection();
        if (o == null || !(o instanceof Konto))
          return;

        choosen = (Konto) o;
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
   * Liefert das ausgewaehlte Konto zurueck oder <code>null</code> wenn der
   * Abbrechen-Knopf gedrueckt wurde.
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return choosen;
  }
  
  /**
   * Optionale Angabe des anzuzeigenden Textes.
   * Wird hier kein Wert gesetzt, wird ein Standard-Text angezeigt.
   * @param text
   */
  public void setText(String text)
  {
    this.text = text;
  }

}


/**********************************************************************
 * $Log: KontoAuswahlDialog.java,v $
 * Revision 1.7  2006/03/28 22:53:19  willuhn
 * @B bug 218
 *
 * Revision 1.6  2006/01/17 00:22:36  willuhn
 * @N erster Code fuer Swift MT940-Import
 *
 * Revision 1.5  2005/06/27 15:35:27  web0
 * @B bug 84
 *
 * Revision 1.4  2005/06/23 23:03:20  web0
 * @N much better KontoAuswahlDialog
 *
 * Revision 1.3  2004/10/25 23:12:02  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/19 23:33:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/10/17 16:28:46  willuhn
 * @N Die ersten Dauerauftraege abgerufen ;)
 *
 **********************************************************************/