/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Testet die Klasse "VerwendungszweckUtil".
 */
public class TestVerwendungszweckUtil
{
  /**
   * Testet die Funktion" rewrap".
   * @throws Exception
   */
  @Test
  public void testRewrap() throws Exception
  {
    String[] test =
    {
      "123456789012345678901234567890",
      "123456789012345678901234567890 ",
      null,
      "d",
      " ",
      "123456789012345678901234567"
    };
    
    String[] result = VerwendungszweckUtil.rewrap(27,test);
    
    Assert.assertTrue(Arrays.equals(new String[]{"123456789012345678901234567",
                                                 "890123456789012345678901234",
                                                 "567890d12345678901234567890",
                                                 "1234567"},
                                    result));
  }

}


