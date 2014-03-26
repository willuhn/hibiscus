/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.rmi;

import org.junit.Assert;
import org.junit.Test;

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
    Assert.assertEquals(BatchBookType.NONE, BatchBookType.byValue((Boolean)null));
    Assert.assertEquals(BatchBookType.TRUE, BatchBookType.byValue(Boolean.TRUE));
    Assert.assertEquals(BatchBookType.FALSE,BatchBookType.byValue(Boolean.FALSE));
  }

  /**
   * Testet das Lookup per String.
   */
  @Test
  public void testByStringValue()
  {
    Assert.assertEquals(null, (String) null);
    Assert.assertEquals(BatchBookType.TRUE.getValue(), "1");
    Assert.assertEquals(BatchBookType.FALSE.getValue(),"0");
  }

}
