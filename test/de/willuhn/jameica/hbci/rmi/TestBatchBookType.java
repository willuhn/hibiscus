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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testet die Klasse "BatchBookType".
 */
public class TestBatchBookType
{
  /**
   * Vollständige Auflistung der möglichen Werte stellt sicher, dass in den anderen Tests kein Fall vergessen wird.
   */
  @Test
  void esGibtNurDreiWerteImEnum()
  {
    BatchBookType[] expected = { BatchBookType.NONE, BatchBookType.TRUE, BatchBookType.FALSE };
    assertArrayEquals(expected, BatchBookType.values());
  }

  @Test
  public void byValueBoolean()
  {
    assertEquals(BatchBookType.NONE, BatchBookType.byValue((Boolean)null));
    assertEquals(BatchBookType.TRUE, BatchBookType.byValue(Boolean.TRUE));
    assertEquals(BatchBookType.FALSE,BatchBookType.byValue(Boolean.FALSE));
    assertEquals(BatchBookType.TRUE, BatchBookType.byValue(true));
    assertEquals(BatchBookType.FALSE,BatchBookType.byValue(false));
  }

  @Test
  public void byValueString()
  {
    assertEquals(BatchBookType.NONE, BatchBookType.byValue(""));
    assertEquals(BatchBookType.TRUE, BatchBookType.byValue("1"));
    assertEquals(BatchBookType.FALSE,BatchBookType.byValue("0"));
  }

  @Test
  public void getValueBoolean()
  {
    assertNull(BatchBookType.NONE.getBooleanValue());
    assertTrue(BatchBookType.TRUE.getBooleanValue());
    assertFalse(BatchBookType.FALSE.getBooleanValue());
  }

  @Test
  public void getValueString()
  {
    assertEquals("",  BatchBookType.NONE.getValue());
    assertEquals("1", BatchBookType.TRUE.getValue());
    assertEquals("0", BatchBookType.FALSE.getValue());
  }

  @Test
  public void alleWerteHabenEineBeschreibung()
  {
    for (BatchBookType x : BatchBookType.values())
    {
      assertNotNull(x.getDescription());
      assertNotEquals(0, x.getDescription().length(), String.format("%s hat keine Beschreibung", x.name()));
    }
  }
}
