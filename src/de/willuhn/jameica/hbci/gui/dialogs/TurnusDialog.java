/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/TurnusDialog.java,v $
 * $Revision: 1.5 $
 * $Date: 2005/06/27 15:35:27 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, welcher eine Liste der vorhandenen Turnus-Elemente anzeigt.
 */
public class TurnusDialog extends AbstractDialog {

	private I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private TablePart turnusList = null;
  private Turnus turnus = null;

  /**
   * @param position
   */
  public TurnusDialog(int position)
  {
    super(position);
		this.setTitle(i18n.tr("Zahlungsturnus auswählen"));
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
	protected void paint(Composite parent) throws Exception
	{

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    // Erzeugen der Liste mit den existierenden Elementen
    turnusList = new TablePart(Settings.getDBService().createList(Turnus.class), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          // Bei Doppelklick aktuellen Turnus zuweisen
          turnus = (Turnus) context;
        }
        catch (Exception e)
        {
          Logger.error("error while choosing turnus",e);
        }
        // und Dialog schliessen. getData() liefert dann den gerade ausgewaehlten zurueck.
        close();
      }
    });
    turnusList.addColumn(i18n.tr("Bezeichnung"),"bezeichnung");
    turnusList.setMulti(false);
    turnusList.setSummary(false);

    // Ein Formatter, der die initialen Turnusse rot markiert
    turnusList.setFormatter(new TableFormatter()
    {
      public void format(TableItem item)
      {
        try {
          Turnus t = (Turnus) item.getData();
          if (t.isInitial())
          {
            item.setForeground(Settings.getBuchungSollForeground());
          }
        }
        catch (Exception e)
        {
          Logger.error("error while formatting turnus",e);
        }
      }
    });

    ContextMenu c = new ContextMenu();

    // Einer fuer die Neuanlage
    c.addItem(new ContextMenuItem(i18n.tr("Neu..."), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        handleEdit(null);
      }
    }));

    // Ein Contextmenu-Eintrag zum Bearbeiten    
    c.addItem(new ContextMenuItem(i18n.tr("Bearbeiten..."), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof Turnus))
          return;
        handleEdit((Turnus) context);
      }
    })
    {
      /**
       * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
       */
      public boolean isEnabledFor(Object o)
      {
        try
        {
          if (o == null || !(o instanceof Turnus))
            return false;
          Turnus t = (Turnus) o;
          if (t.isInitial())
            return false;
        }
        catch (Exception e)
        {
          Logger.error("error while checking context menu item",e);
        }
        return super.isEnabledFor(o);
      }
    });


    // Ein Contextmenu-Eintrag zum Loeschen    
    c.addItem(new ContextMenuItem(i18n.tr("Löschen..."), new Action()
    {
      public void handleAction(Object context) throws ApplicationException
      {
        if (context == null || !(context instanceof Turnus))
          return;
        try
        {
          Turnus t = (Turnus) context;
          t.delete();
        }
        catch (Exception e)
        {
          Logger.error("error while deleting turnus",e);
        }
      }
    })
    {
      /**
       * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
       */
      public boolean isEnabledFor(Object o)
      {
        try
        {
          if (o == null || !(o instanceof Turnus))
            return false;
          Turnus t = (Turnus) o;
          if (t.isInitial())
            return false;
        }
        catch (Exception e)
        {
          Logger.error("error while checking context menu item",e);
        }
        return super.isEnabledFor(o);
      }
    });
   
    turnusList.setContextMenu(c);
    turnusList.paint(parent);

		// und noch die Abschicken-Knoepfe
		ButtonArea buttonArea = new ButtonArea(parent,3);
		buttonArea.addButton(i18n.tr("Neu"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
        handleEdit(null);
			}
		},null);
		buttonArea.addButton(i18n.tr("Übernehmen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
        try
        {
          // aktuell markierten Turnus nehmen
          turnus = (Turnus) turnusList.getSelection();
        }
        catch (Exception e)
        {
          Logger.error("error while choosing turnus",e);
        }
        // und Dialog schliessen
				close();
			}
		},null,true);
		buttonArea.addButton(i18n.tr("Abbrechen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
        // Turnus entfernen
        turnus = null;
        
        // und schliessen
				close();
			}
		});

  }

  private void handleEdit(Turnus t)
  {
    boolean isNew = t == null;

    // Turnus-Edit-Dialog ohne Turnus erzeugen
    TurnusEditDialog te = new TurnusEditDialog(TurnusEditDialog.POSITION_MOUSE,t);
    try
    {
      Turnus t2 = (Turnus) te.open();
      if (t2 != null)
      {
        // Turnus zur Liste hinzufuegen wenn es eine Neuanlage ist
        if (isNew)
        {
          turnusList.addItem(t2);
        }
        else
        {
          // Entfernen und hinzufuegen, um die Anzeige zu aktualisieren
          turnusList.removeItem(t2);
          turnusList.addItem(t2);
        }
            
        // und markieren
        
        turnusList.select(t2);
      }
    }
    catch (Exception e)
    {
      Logger.error("error while adding turnus",e);
    }
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return turnus;
  }
}


/**********************************************************************
 * $Log: TurnusDialog.java,v $
 * Revision 1.5  2005/06/27 15:35:27  web0
 * @B bug 84
 *
 * Revision 1.4  2005/06/07 16:30:02  web0
 * @B Turnus-Dialog "geradegezogen" und ergonomischer gestaltet
 *
 * Revision 1.3  2005/03/06 16:06:10  web0
 * *** empty log message ***
 *
 * Revision 1.2  2004/11/26 01:23:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/26 00:04:08  willuhn
 * @N TurnusDetail
 *
 * Revision 1.3  2004/11/18 23:46:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/11/15 00:38:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/13 17:12:15  willuhn
 * *** empty log message ***
 **********************************************************************/