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

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
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

	private final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  // Das Fachobjekt
  private Turnus turnus = null;

  private SelectInput intervall       = null;
  private SelectInput zeiteinheit     = null;
  private SelectInput tagMonatlich    = null;
  private SelectInput tagWoechentlich = null;
  private LabelInput error            = null;

  private String pleaseChoose         = i18n.tr("Bitte w�hlen...");
  private String lastOfMonth          = i18n.tr("Zum Monatsletzten");

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

	@Override
	protected void paint(Composite parent) throws Exception
	{
		Container group = new SimpleContainer(parent);

		try {
			group.addLabelPair(i18n.tr("Zeiteinheit"),   getZeiteinheit());
			group.addLabelPair(i18n.tr("Zahlung aller"), getIntervall());
			group.addLabelPair(i18n.tr("Zahlung am"),    getTagWoechentlich());
			group.addLabelPair("",                       getTagMonatlich());
			group.addInput(getError());
			
		}
		catch (RemoteException e)
		{
			Logger.error("error while reading turnus",e);
			GUI.getStatusBar().setErrorText(i18n.tr("Fehler beim Lesen des Zahlungsturnus."));
		}

		// und noch die Abschicken-Knoepfe
		ButtonArea buttonArea = new ButtonArea();
		buttonArea.addButton(i18n.tr("�bernehmen"), new Action()
		{
			@Override
			public void handleAction(Object context) throws ApplicationException
			{
			  try
			  {
	        handleStore();
	        close();
			  }
			  catch (ApplicationException ae)
			  {
			    getError().setValue(ae.getMessage());
			  }
			}
		},null,true,"ok.png");
		buttonArea.addButton(i18n.tr("Abbrechen"), new Action()
		{
			@Override
			public void handleAction(Object context) throws ApplicationException
			{
				close();
			}
		},null,false,"process-stop.png");
		group.addButtonArea(buttonArea);
  }

  @Override
  protected Object getData() throws Exception
  {
    return getTurnus();
  }


  /**
   * Liefert ein Label fuer Fehlermeldungen.
   * @return Label.
   */
  private LabelInput getError()
  {
    if (this.error == null)
    {
      this.error = new LabelInput("");
      this.error.setColor(Color.ERROR);
      this.error.setName("");
    }
    return this.error;
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

    intervall = new SelectInput(new String[]{this.pleaseChoose,"1","2","3","4","5","6","12"},""+getTurnus().getIntervall());

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

    String[] values = new String[32];
    values[0] = this.pleaseChoose;
    for (int i=1;i<31;++i)
    {
      values[i] = ""+i;
    }
    values[31] = this.lastOfMonth;
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
  private void handleStore() throws ApplicationException
  {
    try
    {
      Turnus t = getTurnus();
      if (t.isInitial())
        throw new ApplicationException(i18n.tr("Turnus ist Bestandteil der System-Daten und kann nicht ge�ndert werden."));
        
      Zeiteinheit zh = (Zeiteinheit) getZeiteinheit().getValue();
      t.setZeiteinheit(zh.id);
      if (zh.id == Turnus.ZEITEINHEIT_WOECHENTLICH)
      {
        Tag tag = (Tag)getTagWoechentlich().getValue();
        t.setTag(tag.id);
      }
      else
      {
        String s = (String)getTagMonatlich().getValue();
        if (this.lastOfMonth.equals(s))
          t.setTag(HBCIProperties.HBCI_LAST_OF_MONTH);
        else if (this.pleaseChoose.equals(s))
          t.setTag(1);
        else
          t.setTag(Integer.parseInt(s));
      }

      t.setIntervall(Integer.parseInt((String)getIntervall().getValue()));
      t.store();
    }
    catch (RemoteException re)
    {
      Logger.error("error while storing turnus",re);
      throw new ApplicationException(re.getMessage());
    }
  }

  private class TagListener implements Listener
  {

    @Override
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
        this.name = i18n.tr("w�chentlich");
    }

    @Override
    public Object getAttribute(String arg0) throws RemoteException
    {
      return name;
    }

    @Override
    public String getID() throws RemoteException
    {
      return ""+id;
    }

    @Override
    public String getPrimaryAttribute() throws RemoteException
    {
      return "foo";
    }

    @Override
    public boolean equals(GenericObject arg0) throws RemoteException
    {
      if (arg0 == null)
        return false;
      return this.getID().equals(arg0.getID());
    }

    @Override
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

    @Override
    public Object getAttribute(String arg0) throws RemoteException
    {
      return name;
    }

    @Override
    public String getID() throws RemoteException
    {
      return ""+id;
    }

    @Override
    public String getPrimaryAttribute() throws RemoteException
    {
      return "foo";
    }

    @Override
    public boolean equals(GenericObject arg0) throws RemoteException
    {
      if (arg0 == null)
        return false;
      return this.getID().equals(arg0.getID());
    }

    @Override
    public String[] getAttributeNames() throws RemoteException
    {
      return new String[] {"foo"};
    }
  }



}


/**********************************************************************
 * $Log: TurnusEditDialog.java,v $
 * Revision 1.4  2011/05/03 13:43:12  willuhn
 * @C Saubereres Fehlerhandling
 *
 * Revision 1.3  2007/03/14 11:51:23  willuhn
 * @N Zahlungsturnus "12"
 *
 * Revision 1.2  2005/06/07 22:19:57  web0
 * @B bug 49
 *
 * Revision 1.1  2005/06/07 16:30:02  web0
 * @B Turnus-Dialog "geradegezogen" und ergonomischer gestaltet
 *
 **********************************************************************/