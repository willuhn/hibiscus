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
import org.eclipse.swt.widgets.TableItem;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, welcher eine Liste der vorhandenen Turnus-Elemente anzeigt.
 */
public class TurnusDialog extends AbstractDialog
{
	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

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
        try
        {
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
    
    Container container = new SimpleContainer(parent);
    container.addPart(turnusList);

		// und noch die Abschicken-Knoepfe
		ButtonArea buttonArea = new ButtonArea();
		buttonArea.addButton(i18n.tr("Neu"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
        handleEdit(null);
			}
		},null,false,"document-new.png");
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
		},null,true,"ok.png");
		buttonArea.addButton(i18n.tr("Abbrechen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
        // Turnus entfernen
        turnus = null;
        
        // und schliessen
				close();
			}
		},null,false,"process-stop.png");
		
		container.addButtonArea(buttonArea);
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
          int index = turnusList.removeItem(t2);
          if (index == -1) // war gar nicht enthalten
            return;
          turnusList.addItem(t2,index); // an der gleichen Stellen wieder einfuegen
        }
            
        // und markieren
        turnusList.select(t2);
      }
    }
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
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
 * Revision 1.8  2011/05/11 10:20:29  willuhn
 * @N OCE fangen
 *
 * Revision 1.7  2011-05-03 13:43:12  willuhn
 * @C Saubereres Fehlerhandling
 *
 * Revision 1.6  2011-04-29 12:29:53  willuhn
 * @N GUI-Polish
 *
 **********************************************************************/