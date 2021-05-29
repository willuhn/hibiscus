/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testet die Klasse "BatchBookType".
 */
public class TestBatchBookType
{
  /**
   * Testet das Lookup per Boolean.
   */
  @Test
  public void testByBooleanValue()
  {
    assertEquals(BatchBookType.NONE, BatchBookType.byValue((Boolean)null));
    assertEquals(BatchBookType.TRUE, BatchBookType.byValue(Boolean.TRUE));
    assertEquals(BatchBookType.FALSE,BatchBookType.byValue(Boolean.FALSE));
  }

  /**
   * Testet das Lookup per String.
   */
  @Test
  public void testByStringValue()
  {
    assertEquals(null, (String) null);
    assertEquals(BatchBookType.TRUE.getValue(), "1");
    assertEquals(BatchBookType.FALSE.getValue(),"0");
  }

}
