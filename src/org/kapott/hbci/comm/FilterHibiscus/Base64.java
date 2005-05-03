/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/org/kapott/hbci/comm/FilterHibiscus/Attic/Base64.java,v $
 * $Revision: 1.1 $
 * $Date: 2005/05/03 22:43:19 $
 * $Author: web0 $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package org.kapott.hbci.comm.FilterHibiscus;

import java.io.IOException;

import org.kapott.hbci.comm.Filter;

import de.willuhn.logging.Logger;

/**
 * Implementierung eines alternativen Base64-Filters, da HBCIUtils.decodeBase64(byte[])
 * im 2.5er Snapshot von August 2004 leider ein Fehler enthalten ist. Die Klasse
 * wird via Filter.getInstance(String) instanziiert.
 */
public class Base64 extends Filter
{

  /**
   * ct.
   */
  public Base64()
  {
    super();
    Logger.info("creating custom base64 filter " + this.getClass().getName());
  }

  /**
   * @see org.kapott.hbci.comm.Filter#encode(java.lang.String)
   */
  public byte[] encode(String st)
  {
    if (st == null)
      return null;
    return de.willuhn.util.Base64.encode(st.getBytes()).getBytes();
  }

  /**
   * @see org.kapott.hbci.comm.Filter#decode(java.lang.String)
   */
  public String decode(String st)
  {
    if (st == null)
      return null;
    try
    {
      return new String(de.willuhn.util.Base64.decode(st),"ISO-8859-1");
    }
    catch (IOException e)
    {
      Logger.error("error while decoding base64 data, returning string unchanged",e);
      return st;
    }
  }

}

/**********************************************************************
 * $Log: Base64.java,v $
 * Revision 1.1  2005/05/03 22:43:19  web0
 * @B Bug 39
 *
 **********************************************************************/