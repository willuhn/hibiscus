/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/dialogs/TurnusEditDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/06/07 16:30:02 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.gui.dialogs;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.Settings;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.jameica.hbci.server.TurnusHelper;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Turnus neu anlegen/aendern.
 */
public class TurnusEditDialog extends AbstractDialog {

	private I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  // Das Fachobjekt
  private Turnus turnus = null;

  private SelectInput intervall       = null;
  private SelectInput zeiteinheit     = null;
  private SelectInput tagMonatlich    = null;
  private SelectInput tagWoechentlich = null;

  private String pleaseChoose         = i18n.tr("Bitte wählen...");

  /**
   * @param position
   * @param turnus
   */
  public TurnusEditDialog(int position, Turnus turnus)
  {
    super(position);
		this.setTitle(i18n.tr("Zahlungsturnus anlegen/bearbeiten"));
    this.turnus = turnus;
  }

	/**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
	protected void paint(Composite parent) throws Exception
	{

		I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

		LabelGroup group = new LabelGroup(parent,i18n.tr("Eigenschaften"));

		try {
			group.addLabelPair(i18n.tr("Zeiteinheit"),   getZeiteinheit());
			group.addLabelPair(i18n.tr("Zahlung aller"), getIntervall());
			group.addLabelPair(i18n.tr("Zahlung am"),    getTagWoechentlich());
			group.addLabelPair("",                       getTagMonatlich());
			
		}
		catch (RemoteException e)
		{
			Logger.error("error while reading turnus",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen des Zahlungsturnus."));
		}

		// und noch die Abschicken-Knoepfe
		ButtonArea buttonArea = new ButtonArea(parent,2);
		buttonArea.addButton(i18n.tr("Übernehmen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				handleStore();
				close();
			}
		},null,true);
		buttonArea.addButton(i18n.tr("Abbrechen"), new Action()
		{
			public void handleAction(Object context) throws ApplicationException
			{
				close();
			}
		});

  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  protected Object getData() throws Exception
  {
    return getTurnus();
  }






  /**
   * Liefert den Turnus.
   * @return Turnus.
   * @throws RemoteException
   */
  private Turnus getTurnus() throws RemoteException
  {
    if (turnus == null)
      turnus = (Turnus) Settings.getDBService().createObject(Turnus.class,null);
    return turnus;
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
   * Speichert den Turnus.
   */
  public void handleStore()
  {
    try
    {
      Turnus t = getTurnus();
      if (t.isInitial())
      {
        Logger.warn("unable to change turnus, part of initial system data");
        return;
      }
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
    }
    catch (Exception e)
    {
      Logger.error("error while storing turnus",e);
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

    private Tag(int id)
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
 * $Log: TurnusEditDialog.java,v $
 * Revision 1.1  2005/06/07 16:30:02  web0
 * @B Turnus-Dialog "geradegezogen" und ergonomischer gestaltet
 *
 **********************************************************************/