/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/Attic/TurnusControl.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/11/15 00:38:30 $
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
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
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
public class TurnusControl extends AbstractControl
{

	// Das Fachobjekt
	private Turnus turnus = null;

	private Input intervall				= null;
	private Input zeiteinheit			= null;
	private Input tagMonatlich		= null;
	private Input tagWoechentlich	= null;
	private Input comment					= null;

	private I18N i18n;

  /**
   * @param view
   */
  public TurnusControl(AbstractView view)
  {
    super(view);
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

	/**
	 * Liefert den Turnus.
   * @return Turnus.
   */
  public Turnus getTurnus() throws RemoteException
	{
		if (turnus != null)
			return turnus;
		turnus = (Turnus) getCurrentObject();
		
		if (turnus != null)
			return turnus;
		
		turnus = (Turnus) Settings.getDBService().createObject(Turnus.class,null);
		return turnus;
	}

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
  public Input getIntervall() throws RemoteException
	{
		if (intervall != null)
			return intervall;

		intervall = new SelectInput(new String[]{"1","2","3","4","5","6"},""+getTurnus().getIntervall());

		if (getTurnus().getZeiteinheit() == Turnus.ZEITEINHEIT_MONATLICH)
			intervall.setComment(i18n.tr("Monate"));
		else
			intervall.setComment(i18n.tr("Wochen"));

		intervall.addListener(new Listener()
		{
			public void handleEvent(Event event)
			{
				try
				{
					Zeiteinheit ze = (Zeiteinheit) getZeiteinheit().getValue();
					if (ze.id == Turnus.ZEITEINHEIT_MONATLICH)
						intervall.setComment(i18n.tr("Monate"));
					else
						intervall.setComment(i18n.tr("Wochen"));
				}
				catch (Exception e)
				{
				}
			}
		});
		return intervall;
	}

	/**
	 * Liefert eine Auswahl fuer die Zeiteinheit.
   * @return Zeiteinheit.
   * @throws RemoteException
   */
  public Input getZeiteinheit() throws RemoteException
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
  public Input getTagMonatlich() throws RemoteException
	{
		if (tagMonatlich != null)
			return tagMonatlich;

		String[] values = new String[30];
		for (int i=0;i<30;++i)
		{
			values[i] = ""+(i+1);
		}
		tagMonatlich = new SelectInput(values,""+getTurnus().getTag());
		return tagMonatlich;
	}

	/**
	 * Liefert ein Auswahl-Feld fuer den Tag bei woechentlicher Zahlung.
	 * @return Tag.
	 * @throws RemoteException
	 */
	public Input getTagWoechentlich() throws RemoteException
	{
		if (tagWoechentlich != null)
			return tagWoechentlich;

		GenericObject[] values = new GenericObject[7];
		for (int i=0;i<7;++i)
		{
			values[i] = new Tag(i+1);
		}
		tagWoechentlich = new SelectInput(PseudoIterator.fromArray(values),new Tag(getTurnus().getTag()));
		return tagWoechentlich;
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
    		}
    		else
    		{
					getTagMonatlich().disable();
					getTagWoechentlich().enable();
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
		private String name = "unbekannt";

		private Zeiteinheit(int id)
		{
			this.id = id;
			if (this.id == Turnus.ZEITEINHEIT_MONATLICH)
				this.name = i18n.tr("monatlich");
			else
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
	}

	private class Tag implements GenericObject
	{

		private int id = -1;
		private String name = "unbekannt";

		private Tag(int id) throws RemoteException
		{
			this.id = id;
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
	}
}


/**********************************************************************
 * $Log: TurnusControl.java,v $
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