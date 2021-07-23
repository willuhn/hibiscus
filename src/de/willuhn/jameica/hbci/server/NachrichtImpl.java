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

import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Nachricht;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung der System-Nachrichten einer Bank.
 */
public class NachrichtImpl extends AbstractHibiscusDBObject implements Nachricht
{

  private final static transient I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  /**
   * @throws java.rmi.RemoteException
   */
  public NachrichtImpl() throws RemoteException
  {
    super();
  }

  @Override
  protected String getTableName()
  {
    return "systemnachricht";
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "nachricht";
  }

  @Override
  protected void insertCheck() throws ApplicationException
  {
    try
    {
      if (getBLZ() == null || getBLZ().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie eine Bankleitzahl ein"));
      if (getNachricht() == null || getNachricht().length() == 0)
        throw new ApplicationException(i18n.tr("Bitte geben Sie einen Nachrichtentext ein"));
    }
    catch (RemoteException e)
    {
      Logger.error("error while checking system message",e);
      throw new ApplicationException(i18n.tr("Fehler beim Speichern der System-Nachricht"));
    }
  }

  @Override
  protected void updateCheck() throws ApplicationException
  {
    this.insertCheck();
  }

  @Override
  public String getBLZ() throws RemoteException
  {
    return (String) getAttribute("blz");
  }

  @Override
  public Date getDatum() throws RemoteException
  {
    return (Date) getAttribute("datum");
  }

  @Override
  public boolean isGelesen() throws RemoteException
  {
    Integer i = (Integer) getAttribute("gelesen");
    if (i == null)
      return false;
    return i.intValue() == 1;
  }

  @Override
  public String getNachricht() throws RemoteException
  {
    return (String) getAttribute("nachricht");
  }

  @Override
  public void setBLZ(String blz) throws RemoteException
  {
    this.setAttribute("blz",blz);
  }

  @Override
  public void setDatum(Date datum) throws RemoteException
  {
    this.setAttribute("datum",datum);
  }

  @Override
  public void setGelesen(boolean b) throws RemoteException
  {
    this.setAttribute("gelesen",new Integer(b ? 1 : 0));
  }

  @Override
  public void setNachricht(String nachricht) throws RemoteException
  {
    this.setAttribute("nachricht",nachricht);
  }

  @Override
  public void store() throws RemoteException, ApplicationException
  {
    if (getDatum() == null)
      setDatum(new Date());
    if (getAttribute("gelesen") == null)
      setGelesen(false);
    super.store();
  }

}


/**********************************************************************
 * $Log: NachrichtImpl.java,v $
 * Revision 1.7  2011/10/18 09:28:14  willuhn
 * @N Gemeinsames Basis-Interface "HibiscusDBObject" fuer alle Entities (ausser Version und DBProperty) mit der Implementierung "AbstractHibiscusDBObject". Damit koennen jetzt zu jedem Fachobjekt beliebige Meta-Daten in der Datenbank gespeichert werden. Wird im ersten Schritt fuer die Reminder verwendet, um zu einem Auftrag die UUID des Reminders am Objekt speichern zu koennen
 *
 * Revision 1.6  2008/04/27 22:22:56  willuhn
 * @C I18N-Referenzen statisch
 *
 * Revision 1.5  2007/10/02 16:08:55  willuhn
 * @C Bugfix mit dem falschen Spaltentyp nochmal ueberarbeitet
 *
 * Revision 1.4  2007/10/01 09:37:42  willuhn
 * @B H2: Felder vom Typ "TEXT" werden von H2 als InputStreamReader geliefert. Felder umsatz.kommentar und protokoll.nachricht auf "VARCHAR(1000)" geaendert und fuer Migration in den Gettern beides beruecksichtigt
 *
 * Revision 1.3  2006/12/01 00:02:34  willuhn
 * @C made unserializable members transient
 *
 * Revision 1.2  2005/05/30 22:55:27  web0
 * *** empty log message ***
 *
 * Revision 1.1  2005/05/09 17:26:56  web0
 * @N Bugzilla 68
 *
 **********************************************************************/