/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/TurnusControl.java,v $
 * $Revision: 1.7 $
 * $Date: 2005/01/05 15:17:50 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.controller;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.hbci.server.TurnusHelper;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 */
public class TurnusControl
{

	// Das Fachobjekt
	private Turnus turnus = null;

	private SelectInput intervall				= null;
	private SelectInput zeiteinheit			= null;
	private SelectInput tagMonatlich		= null;
	private SelectInput tagWoechentlich	= null;
	private Input comment				      	= null;

	private TablePart turnusList	      = null;

	private I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

	private String pleaseChoose         = i18n.tr("Bitte wählen...");

	/**
	 * Liefert eine Liste mit existierenden Zahlungsturnus(sen?).
   * @return Liste existierender Turn^WDatensätze ;).
   * @throws RemoteException
   */
  public Part getTurnusList() throws RemoteException
	{
		if (turnusList != null)
			return turnusList;

		DBIterator list = Settings.getDBService().createList(Turnus.class);

		turnusList = new TablePart(list,new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				try
				{
					if (context == null)
						return;
					turnus = (Turnus) context;
			
					int zh = turnus.getZeiteinheit();
					getZeiteinheit().setPreselected(new Zeiteinheit(zh));
					getIntervall().setPreselected("" + turnus.getIntervall());
					if (zh == Turnus.ZEITEINHEIT_MONATLICH)
					{
						getTagMonatlich().setPreselected("" + turnus.getTag());
					}
					else
					{
						getTagWoechentlich().setPreselected(new Tag(turnus.getTag()));
					}
					new TagListener().handleEvent(null);
				}
				catch (Exception e)
				{
					Logger.error("error while updating combo boxes",e);
					try
					{
						getComment().setValue(i18n.tr("Fehler beim Laden des Turnus"));
					}
					catch (RemoteException e2)
					{
						// useless
					}
				}
			}
		});

		turnusList.addColumn(i18n.tr("Bezeichnung"),"bezeichnung");
		turnusList.disableSummary();
		return turnusList;
	}

	/**
	 * Liefert den Turnus.
   * @return Turnus.
   */
  public Turnus getTurnus() throws RemoteException
	{
		if (turnus == null)
			turnus = (Turnus) Settings.getDBService().createObject(Turnus.class,null);
		return turnus;
	}

	/**
	 * Liefert ein Kommentar-Feld.
   * @return Kommentar-Feld.
   * @throws RemoteException
   */
  public Input getComment() throws RemoteException
	{
		if (comment != null)
			return comment;
		comment = new LabelInput("");
		return comment;
	}

	/**
	 * Liefert ein Auswahlfeld fuer das Intervall.
   * @return Intervall.
   * @throws RemoteException
   */
  public SelectInput getIntervall() throws RemoteException
	{
		if (intervall != null)
			return intervall;

		intervall = new SelectInput(new String[]{this.pleaseChoose,"1","2","3","4","5","6"},""+getTurnus().getIntervall());

		if (getTurnus().getZeiteinheit() == Turnus.ZEITEINHEIT_MONATLICH)
			intervall.setComment(i18n.tr("Monate"));
		else
			intervall.setComment(i18n.tr("Wochen"));

		return intervall;
	}

	/**
	 * Liefert eine Auswahl fuer die Zeiteinheit.
   * @return Zeiteinheit.
   * @throws RemoteException
   */
  public SelectInput getZeiteinheit() throws RemoteException
	{
		if (zeiteinheit != null)
			return zeiteinheit;

		// Wir bauen uns zwei synthetische GenericObjects, die fuer uns die 
		// Anzeige des Zeitintervalls machen
		GenericObject[] values = new GenericObject[]
		{
			new Zeiteinheit(Turnus.ZEITEINHEIT_MONATLICH),
			new Zeiteinheit(Turnus.ZEITEINHEIT_WOECHENTLICH)
		};
		zeiteinheit = new SelectInput(PseudoIterator.fromArray(values),new Zeiteinheit(getTurnus().getZeiteinheit()));
		zeiteinheit.addListener(new TagListener());
		return zeiteinheit;
	}

	/**
	 * Liefert ein Auswahl-Feld fuer den Tag bei monatlicher Zahlung.
   * @return Tag.
   * @throws RemoteException
   */
  public SelectInput getTagMonatlich() throws RemoteException
	{
		if (tagMonatlich != null)
			return tagMonatlich;

		String[] values = new String[31];
		values[0] = this.pleaseChoose;
		for (int i=1;i<31;++i)
		{
			values[i] = ""+i;
		}
		tagMonatlich = new SelectInput(values,""+getTurnus().getTag());
		return tagMonatlich;
	}

	/**
	 * Liefert ein Auswahl-Feld fuer den Tag bei woechentlicher Zahlung.
	 * @return Tag.
	 * @throws RemoteException
	 */
	public SelectInput getTagWoechentlich() throws RemoteException
	{
		if (tagWoechentlich != null)
			return tagWoechentlich;

		GenericObject[] values = new GenericObject[8];
		values[0] = new Tag(-1);
		for (int i=1;i<8;++i)
		{
			values[i] = new Tag(i);
		}
		tagWoechentlich = new SelectInput(PseudoIterator.fromArray(values),new Tag(getTurnus().getTag()));
		new TagListener().handleEvent(null); // einmal ausloesen, um die readOnly-Flags zu setzen
		return tagWoechentlich;
	}
	
	/**
   * Setzt die Eingabe-Felder zur Eingabe eines neuen Turnus zurueck.
   */
  public void handleCreate()
	{
		try
		{
			this.turnus = null;
			this.getZeiteinheit().setPreselected(new Zeiteinheit(Turnus.ZEITEINHEIT_MONATLICH));
			this.getIntervall().setPreselected(this.pleaseChoose);
			this.getTagMonatlich().enable();
			this.getTagWoechentlich().disable();
			this.getTagMonatlich().setPreselected(this.pleaseChoose);
			this.getTagWoechentlich().setPreselected(new Tag(-1));
		}
		catch (Exception e)
		{
			Logger.error("error while handling create",e);
			try
			{
				this.getComment().setValue(i18n.tr("Fehler beim Anlegen des Zahlungsturnus"));
			}
			catch (RemoteException e2)
			{
				// useless
			}
		}
	}

	/**
	 * Speichert den Turnus.
	 */
	public void handleStore()
	{
		try
		{
			Turnus t = getTurnus();
			Zeiteinheit zh = (Zeiteinheit) getZeiteinheit().getValue();
			t.setZeiteinheit(zh.id);
			if (zh.id == Turnus.ZEITEINHEIT_WOECHENTLICH)
			{
				Tag tag = (Tag)getTagWoechentlich().getValue();
				t.setTag(tag.id);
			}
			else
			{
				t.setTag(Integer.parseInt((String)getTagMonatlich().getValue()));
			}

			t.setIntervall(Integer.parseInt((String)getIntervall().getValue()));
			t.store();
			getComment().setValue(i18n.tr("Zahlungsturnus gespeichert"));
		}
		catch (ApplicationException e)
		{
			try
			{
				getComment().setValue(e.getMessage());
			}
			catch (RemoteException e2)
			{
				GUI.getStatusBar().setErrorText(e.getMessage());
			}
		}
		catch (Exception e2)
		{
			Logger.error("error while storing turnus",e2);
			try
			{
				getComment().setValue(i18n.tr("Fehler beim Speichern des Zahlungsturnus"));
			}
			catch (RemoteException e)
			{
				GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Speichern des Zahlungsturnus"));
			}
		}
	}

	private class TagListener implements Listener
	{

    /**
     * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
     */
    public void handleEvent(Event event)
    {
    	try
    	{
    		Zeiteinheit zh = (Zeiteinheit) getZeiteinheit().getValue();
    		if (zh == null)
    			return;
    		if (zh.id == Turnus.ZEITEINHEIT_MONATLICH)
    		{
    			getTagMonatlich().enable();
    			getTagWoechentlich().disable();
    			getTagWoechentlich().setPreselected(new Tag(-1));
					getIntervall().setComment(i18n.tr("Monate"));
    		}
    		else
    		{
					getTagMonatlich().disable();
					getTagMonatlich().setPreselected(pleaseChoose);
					getTagWoechentlich().enable();
					getIntervall().setComment(i18n.tr("Wochen"));
    		}
    	}
    	catch (Exception e)
    	{
    		Logger.error("error while reading zeiteinheit",e);
    	}
    }
	}

	/**
	 * Kleines HilfsObjekt zur Anzeige der Zeiteinheit.
   */
  private class Zeiteinheit implements GenericObject
	{

		private int id = -1;
		private String name = pleaseChoose;

		private Zeiteinheit(int id)
		{
			this.id = id;
			if (this.id == Turnus.ZEITEINHEIT_MONATLICH)
				this.name = i18n.tr("monatlich");
			else if (this.id == Turnus.ZEITEINHEIT_WOECHENTLICH)
				this.name = i18n.tr("wöchentlich");
		}

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
	    return name;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return ""+id;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "foo";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject arg0) throws RemoteException
    {
    	if (arg0 == null)
    		return false;
    	return this.getID().equals(arg0.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[] {"foo"};
    }
	}

	private class Tag implements GenericObject
	{

		private int id = -1;
		private String name = pleaseChoose;

		private Tag(int id) throws RemoteException
		{
			this.id = id;
			if (this.id > 0)
				this.name = TurnusHelper.getWochentag(id);
		}

    /**
     * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) throws RemoteException
    {
      return name;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getID()
     */
    public String getID() throws RemoteException
    {
      return ""+id;
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
     */
    public String getPrimaryAttribute() throws RemoteException
    {
      return "foo";
    }

    /**
     * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
     */
    public boolean equals(GenericObject arg0) throws RemoteException
    {
			if (arg0 == null)
				return false;
			return this.getID().equals(arg0.getID());
    }

    /**
     * @see de.willuhn.datasource.GenericObject#getAttributeNames()
     */
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[] {"foo"};
    }
	}
}


/**********************************************************************
 * $Log: TurnusControl.java,v $
 * Revision 1.7  2005/01/05 15:17:50  willuhn
 * @N Neues Service-System in Jameica
 *
 * Revision 1.6  2004/11/26 01:23:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2004/11/26 00:04:08  willuhn
 * @N TurnusDetail
 *
 * Revision 1.4  2004/11/18 23:46:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/11/15 00:38:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/11/14 19:21:37  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/11/13 17:02:04  willuhn
 * @N Bearbeiten des Zahlungsturnus
 *
 **********************************************************************/