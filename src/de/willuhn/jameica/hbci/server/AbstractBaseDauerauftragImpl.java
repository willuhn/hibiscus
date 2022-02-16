/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.hbci.rmi.BaseDauerauftrag;
import de.willuhn.jameica.hbci.rmi.Turnus;
import de.willuhn.logging.Logger;

/**
 * Abstrakte Basis-Implementierung eines Dauerauftrags.
 */
public abstract class AbstractBaseDauerauftragImpl extends AbstractHibiscusTransferImpl implements BaseDauerauftrag
{

  /**
   * ct.
   * @throws RemoteException
   */
  public AbstractBaseDauerauftragImpl() throws RemoteException
  {
    super();
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "zweck";
  }

  @Override
  public Date getErsteZahlung() throws RemoteException
  {
    return (Date) getAttribute("erste_zahlung");
  }

  @Override
  public Date getLetzteZahlung() throws RemoteException
  {
		return (Date) getAttribute("letzte_zahlung");
  }

  @Override
  public Turnus getTurnus() throws RemoteException
  {
		// Zwischen Dauerauftrag und Turnus existiert kein Constraint.
		// Folglich kann auch kein Turnus via Fremd-Schluessel geladen
		// werden. Hintergrund: Wuerde o.g. der Fall sein, dann wuerde
		// die Aenderung eines Zahlungsturnus bei einem Dauerauftrag
		// gleichzeitig die Aenderung bei einem anderen bedeuten, der
		// auf den gleichen Fremdschluessel verweist.
		// Daher existiert die Turnus-Tabelle eher als Sammlung von
		// Templates. Dennoch wollen wir das Turnus-Objekt des
		// Komforts halber benutzen und erstellen daher einfach diese
		// synthetischen Turnus-Objekte.
		Integer ze        = (Integer)getAttribute("zeiteinheit");
		Integer intervall = (Integer)getAttribute("intervall");
		Integer tag				= (Integer)getAttribute("tag");
		if (ze == null || intervall == null || tag == null)
			return null;
  	Turnus t = (Turnus) getService().createObject(Turnus.class,null);
  	t.setIntervall(intervall.intValue());
		t.setZeiteinheit(ze.intValue());
		t.setTag(tag.intValue());
		return t;
  }

        @Override
	public boolean isActive() throws RemoteException
	{
		return getOrderID() != null && getOrderID().length() > 0;
	}

  @Override
  public void setErsteZahlung(Date datum) throws RemoteException
  {
  	setAttribute("erste_zahlung",datum);
  }

  @Override
  public void setLetzteZahlung(Date datum) throws RemoteException
  {
		setAttribute("letzte_zahlung",datum);
  }

  @Override
  public void setTurnus(Turnus turnus) throws RemoteException
  {
  	if (turnus == null)
  		return;
  	
		setAttribute("zeiteinheit",	Integer.valueOf(turnus.getZeiteinheit()));
		setAttribute("intervall",		Integer.valueOf(turnus.getIntervall()));
		setAttribute("tag",					Integer.valueOf(turnus.getTag()));
  }

  @Override
  public boolean equals(GenericObject o) throws RemoteException
  {
		if (o == null || !(o instanceof BaseDauerauftrag))
			return false;
		
		try
		{
		  BaseDauerauftrag other = (BaseDauerauftrag) o;

      // Wenn beide eine ID haben, brauchen wir nur anhand der ID vergleichen
      // Die Pruefung via Checksumme ist nur noetig, wenn neue Datensaetze
      // gespeichert werden sollen
      String id1 = this.getID();
      String id2 = other.getID();
      if (id1 != null && id2 != null)
        return id1.equals(id2);
		  
			return other.getChecksum() == getChecksum();
		}
    catch (Exception e)
    {
      Logger.error("error while comparing objects",e);
      return false;
    }
  }

  @Override
  public String getOrderID() throws RemoteException
  {
    return (String) getAttribute("orderid");
  }

  @Override
  public void setOrderID(String id) throws RemoteException
  {
  	setAttribute("orderid",id);
  }

  /**
   * Ueberschreiben wir, um beim synthetischen Attribut "turnus_id" ein
   * Turnus-Objekt liefern zu koennen.
   */
  @Override
  public Object getAttribute(String arg0) throws RemoteException
  {
  	if ("turnus_id".equals(arg0))
  		return getTurnus();
    if ("naechste_zahlung".equals(arg0))
      return getNaechsteZahlung();
    return super.getAttribute(arg0);
  }

  @Override
  public Date getNaechsteZahlung() throws RemoteException
  {
    return TurnusHelper.getNaechsteZahlung(this.getErsteZahlung(),
                                           this.getLetzteZahlung(),
                                           this.getTurnus(),
                                           new Date());
  }
}
