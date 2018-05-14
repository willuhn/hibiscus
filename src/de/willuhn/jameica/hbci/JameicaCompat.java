/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Hilfsklasse fuer Abwaertskompatibilitaet zu aelteren Jameica-Versionen.
 */
public class JameicaCompat
{
  /**
   * Prueft, ob in dem Objekt "o" die Methode "method" existiert.
   * Wenn ja, wird sie ausgefuehrt und der Rueckgabewert geliefert.
   * Falls nicht, wird geprueft, ob eine Member-Variable "field" existiert
   * und deren Wert zurueckgeliefert.
   * @param o das Objekt.
   * @param method die aufzurufende Methode.
   * @param field das alternative Feld, falls die Methode in dieser Jameica-Version noch nicht existiert.
   * @return der Rueckgabewert der Methode bzw. der Wert der Member-Variable.
   * @throws Exception
   */
  public static Object get(Object o, String method, String field) throws Exception
  {
    if (o == null)
      return null;
    
    Method m = findMethod(o,null,method);
    if (m != null)
      return m.invoke(o);
    
    Field f = findField(o,field);
    if (f != null)
    {
      f.setAccessible(true);
      return f.get(o);
    }
    
    return null;
  }

  /**
   * Prueft, ob in dem Objekt "o" die Methode "method" existiert.
   * Wenn ja, wird sie ausgefuehrt und der Parameter "value" als Wert uebergeben.
   * Falls nicht, wird geprueft, ob eine Member-Variable "field" existiert
   * und deren Wert gesetzt.
   * @param o das Objekt.
   * @param value der Wert.
   * @param method die aufzurufende Methode.
   * @param field das alternative Feld, falls die Methode in dieser Jameica-Version noch nicht existiert.
   * @throws Exception
   */
  public static void set(Object o, Object value, String method, String field) throws Exception
  {
    if (o == null)
      return;
    
    Method m = findMethod(o,value,method);
    if (m != null)
      m.invoke(o,value);
    
    Field f = findField(o,field);
    if (f != null)
    {
      f.setAccessible(true);
      f.set(o,value);
    }
  }
  
  /**
   * Sucht rekursiv nach der angegebenen Methode.
   * @param o das Objekt.
   * @param param der Parameter.
   * @param name die Methode. Muss public sein.
   * @return die Methode oder NULL, wenn sie nicht gefunden wurd.
   */
  private static Method findMethod(Object o, Object param, String name) throws Exception
  {
    if (o == null || name == null || name.length() == 0)
      return null;
    
    Class c = o.getClass();

    // "getMethod" mach selbst bereits die Rekursion
    try
    {
      Method m = param != null ? c.getMethod(name,param.getClass()) : c.getMethod(name);
      if (m != null)
        return m;
    }
    catch (NoSuchMethodException e)
    {
      // OK, wir suchen weiter
    }
    
    return null;
  }
  
  /**
   * Sucht rekursiv nach dem angegebenen Feld.
   * @param o das Objekt.
   * @param name das Feld. Muss NICHT public sein.
   * @return das Feld oder NULL, wenn es nicht gefunden wurd.
   */
  private static Field findField(Object o, String name) throws Exception
  {
    if (o == null || name == null || name.length() == 0)
      return null;
    
    Class c = o.getClass();

    for (int i=0;i<20;++i) // Limitiert auf maximal 20 Schritte
    {
      try
      {
        Field f = c.getDeclaredField(name);
        if (f != null)
          return f;
      }
      catch (NoSuchFieldException e)
      {
        // OK, wir suchen weiter
      }
      
      c = c.getSuperclass();
      if (c == null || c.equals(Object.class))
        return null;
    }
    
    return null;
  }
}
