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

  @Override
  public Konto getKonto() throws RemoteException
  {
    return (Konto) this.getAttribute("konto_id");
  }

  @Override
  public void setKonto(Konto konto) throws RemoteException
  {
    this.setAttribute("konto_id",konto);
  }

  @Override
  public Date getAusfuehrungsdatum() throws RemoteException
  {
    return (Date) this.getAttribute("ausgefuehrt_am");
  }

  @Override
  public String getKommentar() throws RemoteException
  {
    return (String) this.getAttribute("kommentar");
  }

  @Override
  public void setKommentar(String kommentar) throws RemoteException
  {
    this.setAttribute("kommentar",kommentar);
  }

  @Override
  public String getPfad() throws RemoteException
  {
    return (String) this.getAttribute("pfad");
  }

  @Override
  public void setPfad(String pfad) throws RemoteException
  {
    this.setAttribute("pfad",pfad);
  }

  @Override
  public String getDateiname() throws RemoteException
  {
    return (String) this.getAttribute("dateiname");
  }

  @Override
  public void setDateiname(String dateiname) throws RemoteException
  {
    this.setAttribute("dateiname",dateiname);
  }

  @Override
  public String getUUID() throws RemoteException
  {
    return (String) this.getAttribute("uuid");
  }

  @Override
  public void setUUID(String uuid) throws RemoteException
  {
    this.setAttribute("uuid",uuid);
  }

  @Override
  public String getFormat() throws RemoteException
  {
    return (String) this.getAttribute("format");
  }

  @Override
  public void setFormat(String format) throws RemoteException
  {
    this.setAttribute("format",format);
  }

  @Override
  public Date getErstellungsdatum() throws RemoteException
  {
    return (Date) this.getAttribute("erstellungsdatum");
  }

  @Override
  public void setErstellungsdatum(Date d) throws RemoteException
  {
    this.setAttribute("erstellungsdatum",d);
  }

  @Override
  public Date getVon() throws RemoteException
  {
    return (Date) this.getAttribute("von");
  }

  @Override
  public void setVon(Date von) throws RemoteException
  {
    this.setAttribute("von",von);
  }

  @Override
  public Date getBis() throws RemoteException
  {
    return (Date) this.getAttribute("bis");
  }

  @Override
  public void setBis(Date bis) throws RemoteException
  {
    this.setAttribute("bis",bis);
  }

  @Override
  public Integer getJahr() throws RemoteException
  {
    return (Integer) this.getAttribute("jahr");
  }

  @Override
  public void setJahr(Integer jahr) throws RemoteException
  {
    this.setAttribute("jahr",jahr);
  }

  @Override
  public Integer getNummer() throws RemoteException
  {
    return (Integer) this.getAttribute("nummer");
  }

  @Override
  public void setNummer(Integer nummer) throws RemoteException
  {
    this.setAttribute("nummer",nummer);
  }

  @Override
  public String getName1() throws RemoteException
  {
    return (String) this.getAttribute("name1");
  }

  @Override
  public void setName1(String name1) throws RemoteException
  {
    this.setAttribute("name1",name1);
  }

  @Override
  public String getName2() throws RemoteException
  {
    return (String) this.getAttribute("name2");
  }

  @Override
  public void setName2(String name2) throws RemoteException
  {
    this.setAttribute("name2",name2);
  }

  @Override
  public String getName3() throws RemoteException
  {
    return (String) this.getAttribute("name3");
  }

  @Override
  public void setName3(String name3) throws RemoteException
  {
    this.setAttribute("name3",name3);
  }

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

  @Override
  public void setQuittungscode(byte[] code) throws RemoteException
  {
    String base64 = null;
    
    if (code != null && code.length > 0)
      base64 = Base64.encode(code);
    
    this.setAttribute("quittungscode",base64);
  }
  
  @Override
  public Date getQuittiertAm() throws RemoteException
  {
    return (Date) this.getAttribute("quittiert_am");
  }
  
  @Override
  public void setQuittiertAm(Date d) throws RemoteException
  {
    this.setAttribute("quittiert_am",d);
  }
  
  @Override
  public Date getGelesenAm() throws RemoteException
  {
    return (Date) this.getAttribute("gelesen_am");
  }
  
  @Override
  public void setGelesenAm(Date d) throws RemoteException
  {
    this.setAttribute("gelesen_am",d);
  }

  @Override
  public String getPrimaryAttribute() throws RemoteException
  {
    return "name1";
  }

  @Override
  protected String getTableName()
  {
    return "kontoauszug";
  }
  
  @Override
  public void insert() throws RemoteException, ApplicationException
  {
    this.setAttribute("ausgefuehrt_am",new Date());
    super.insert();
  }
  @Override
  protected Class getForeignObject(String field) throws RemoteException
  {
    if ("konto_id".equals(field))
      return Konto.class;
    return null;
  }

}


