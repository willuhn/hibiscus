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

import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.rmi.Kontoauszug;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.Base64;

/**
 * Implementierung der elektronischen Kontoauszuege pro Konto.
 */
public class KontoauszugImpl extends AbstractHibiscusDBObject implements Kontoauszug
{
  /**
   * ct.
   * @throws RemoteException
   */
  public KontoauszugImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getKonto()
   */
  @Override
  public Konto getKonto() throws RemoteException
  {
    return (Konto) this.getAttribute("konto_id");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setKonto(de.willuhn.jameica.hbci.rmi.Konto)
   */
  @Override
  public void setKonto(Konto konto) throws RemoteException
  {
    this.setAttribute("konto_id",konto);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getAusfuehrungsdatum()
   */
  @Override
  public Date getAusfuehrungsdatum() throws RemoteException
  {
    return (Date) this.getAttribute("ausgefuehrt_am");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getKommentar()
   */
  @Override
  public String getKommentar() throws RemoteException
  {
    return (String) this.getAttribute("kommentar");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setKommentar(java.lang.String)
   */
  @Override
  public void setKommentar(String kommentar) throws RemoteException
  {
    this.setAttribute("kommentar",kommentar);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getPfad()
   */
  @Override
  public String getPfad() throws RemoteException
  {
    return (String) this.getAttribute("pfad");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setPfad(java.lang.String)
   */
  @Override
  public void setPfad(String pfad) throws RemoteException
  {
    this.setAttribute("pfad",pfad);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getDateiname()
   */
  @Override
  public String getDateiname() throws RemoteException
  {
    return (String) this.getAttribute("dateiname");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setDateiname(java.lang.String)
   */
  @Override
  public void setDateiname(String dateiname) throws RemoteException
  {
    this.setAttribute("dateiname",dateiname);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getUUID()
   */
  @Override
  public String getUUID() throws RemoteException
  {
    return (String) this.getAttribute("uuid");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setUUID(java.lang.String)
   */
  @Override
  public void setUUID(String uuid) throws RemoteException
  {
    this.setAttribute("uuid",uuid);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getFormat()
   */
  @Override
  public String getFormat() throws RemoteException
  {
    return (String) this.getAttribute("format");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setFormat(java.lang.String)
   */
  @Override
  public void setFormat(String format) throws RemoteException
  {
    this.setAttribute("format",format);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getErstellungsdatum()
   */
  @Override
  public Date getErstellungsdatum() throws RemoteException
  {
    return (Date) this.getAttribute("erstellungsdatum");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setErstellungsdatum(java.util.Date)
   */
  @Override
  public void setErstellungsdatum(Date d) throws RemoteException
  {
    this.setAttribute("erstellungsdatum",d);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getVon()
   */
  @Override
  public Date getVon() throws RemoteException
  {
    return (Date) this.getAttribute("von");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setVon(java.util.Date)
   */
  @Override
  public void setVon(Date von) throws RemoteException
  {
    this.setAttribute("von",von);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getBis()
   */
  @Override
  public Date getBis() throws RemoteException
  {
    return (Date) this.getAttribute("bis");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setBis(java.util.Date)
   */
  @Override
  public void setBis(Date bis) throws RemoteException
  {
    this.setAttribute("bis",bis);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getJahr()
   */
  @Override
  public Integer getJahr() throws RemoteException
  {
    return (Integer) this.getAttribute("jahr");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setJahr(java.lang.Integer)
   */
  @Override
  public void setJahr(Integer jahr) throws RemoteException
  {
    this.setAttribute("jahr",jahr);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getNummer()
   */
  @Override
  public Integer getNummer() throws RemoteException
  {
    return (Integer) this.getAttribute("nummer");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setNummer(java.lang.Integer)
   */
  @Override
  public void setNummer(Integer nummer) throws RemoteException
  {
    this.setAttribute("nummer",nummer);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getName1()
   */
  @Override
  public String getName1() throws RemoteException
  {
    return (String) this.getAttribute("name1");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setName1(java.lang.String)
   */
  @Override
  public void setName1(String name1) throws RemoteException
  {
    this.setAttribute("name1",name1);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getName2()
   */
  @Override
  public String getName2() throws RemoteException
  {
    return (String) this.getAttribute("name2");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setName2(java.lang.String)
   */
  @Override
  public void setName2(String name2) throws RemoteException
  {
    this.setAttribute("name2",name2);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getName3()
   */
  @Override
  public String getName3() throws RemoteException
  {
    return (String) this.getAttribute("name3");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setName3(java.lang.String)
   */
  @Override
  public void setName3(String name3) throws RemoteException
  {
    this.setAttribute("name3",name3);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getQuittungscode()
   */
  @Override
  public byte[] getQuittungscode() throws RemoteException
  {
    String base64 = (String) this.getAttribute("quittungscode");
    if (base64 == null || base64.length() == 0)
      return null;

    try
    {
      return Base64.decode(base64);
    }
    catch (Exception e)
    {
      Logger.error("unable to decode base64 text",e);
      return null;
    }
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setQuittungscode(byte[])
   */
  @Override
  public void setQuittungscode(byte[] code) throws RemoteException
  {
    String base64 = null;

    if (code != null && code.length > 0)
      base64 = Base64.encode(code);

    this.setAttribute("quittungscode",base64);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getQuittiertAm()
   */
  @Override
  public Date getQuittiertAm() throws RemoteException
  {
    return (Date) this.getAttribute("quittiert_am");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setQuittiertAm(java.util.Date)
   */
  @Override
  public void setQuittiertAm(Date d) throws RemoteException
  {
    this.setAttribute("quittiert_am",d);
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#getGelesenAm()
   */
  @Override
  public Date getGelesenAm() throws RemoteException
  {
    return (Date) this.getAttribute("gelesen_am");
  }

  /**
   * @see de.willuhn.jameica.hbci.rmi.Kontoauszug#setGelesenAm(java.util.Date)
   */
  @Override
  public void setGelesenAm(Date d) throws RemoteException
  {
    this.setAttribute("gelesen_am",d);
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getPrimaryAttribute()
   */
  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "name1";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
   */
  @Override
  protected String getTableName()
  {
    return "kontoauszug";
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#insert()
   */
  @Override
  public void insert() throws RemoteException, ApplicationException
  {
    this.setAttribute("ausgefuehrt_am",new Date());
    super.insert();
  }

  /**
   * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
   */
  @Override
  protected Class getForeignObject(String field) throws RemoteException
  {
    if ("konto_id".equals(field))
      return Konto.class;
    return null;
  }

}
