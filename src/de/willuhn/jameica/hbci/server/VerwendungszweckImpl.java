/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/Attic/VerwendungszweckImpl.java,v $
 * $Revision: 1.3 $
 * $Date: 2008/12/09 13:01:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.rmi.RemoteException;

import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.rmi.Transfer;
import de.willuhn.jameica.hbci.rmi.Verwendungszweck;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines zusaetzlichen Verwendungszweckes.
 */
public class VerwendungszweckImpl extends AbstractDBObject implements Verwendungszweck
{

  /**
   * ct
   * @throws RemoteException
   */
  public VerwendungszweckImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "zweck";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  protected String getTableName()
  {
    return "verwendungszweck";
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Verwendungszweck#getText()
   */
  public String getText() throws RemoteException
  {
    return (String) getAttribute("zweck");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Verwendungszweck#setText(java.lang.String)
   */
  public void setText(String text) throws RemoteException
  {
    setAttribute("zweck",text);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Verwendungszweck#setTransfer(de.willuhn.jameica.hbci.rmi.Transfer)
   */
  public void setTransfer(Transfer t) throws RemoteException, ApplicationException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

    if (!(t instanceof DBObject))
      throw new ApplicationException(i18n.tr("Auftrag unterstützt keine erweiterten Verwendungszwecke"));
    
    DBObject g = (DBObject) t;
    if (g.isNewObject())
      throw new ApplicationException(i18n.tr("Bitte speichern Sie zuerst den Auftrag"));

    this.setAttribute("typ",new Integer(t.getTransferTyp()));
    this.setAttribute("auftrag_id",new Integer(g.getID()));
  }
}


/*********************************************************************
 * $Log: VerwendungszweckImpl.java,v $
 * Revision 1.3  2008/12/09 13:01:47  willuhn
 * @B auftrag_id wurde mit dem falschen Typ gespeichert - erzeugte unter MySQL eine ClassCastException Integer <-> String
 *
 * Revision 1.2  2008/11/26 00:39:36  willuhn
 * @N Erste Version erweiterter Verwendungszwecke. Muss dringend noch getestet werden.
 *
 * Revision 1.1  2008/02/15 17:39:10  willuhn
 * @N BUGZILLA 188 Basis-API fuer weitere Zeilen Verwendungszweck. GUI fehlt noch
 * @N DB-Update 0005. Speichern des Textschluessels bei Sammelauftragsbuchungen in der Datenbank
 *
 **********************************************************************/