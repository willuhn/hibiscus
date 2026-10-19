/**********************************************************************
 *
 * Copyright (c) 2026 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.io;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Util-Klasse fuer JAXB-Serialisierung/Deserialisierung.
 */
public class JAXBUtil
{
  /**
   * Erzeugt einen Serializer fuer den angegebenen Typ.
   * @param type der Typ der zu serialisierenden Klasse.
   * @return der Serializer.
   * @throws ApplicationException 
   */
  public static Marshaller createMarshaller(Class type) throws ApplicationException
  {
    try
    {
      JAXBContext ctx = JAXBContext.newInstance(type);
      Marshaller m = ctx.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      return m;
    }
    catch (Exception e)
    {
      Logger.error("unable to load marshaller",e);
      throw new ApplicationException(Application.getI18n().tr("Speichern der Daten fehlgeschlagen: {0}",e.getMessage()));
    }
  }
  
  /**
   * Erzeugt einen De-Serializer fuer den angegebenen Typ.
   * @param type der Typ der zu de-serialisierenden Klasse.
   * @return der De-Serializer.
   * @throws ApplicationException 
   */
  public static Unmarshaller createUnmarshaller(Class type) throws ApplicationException
  {
    try
    {
      JAXBContext ctx = JAXBContext.newInstance(type);
      Unmarshaller u = ctx.createUnmarshaller();
      return u;
    }
    catch (JAXBException e)
    {
      throw new ApplicationException(Application.getI18n().tr("Laden der Daten fehlgeschlagen: {0}",e.getMessage()));
    }
  }
}
