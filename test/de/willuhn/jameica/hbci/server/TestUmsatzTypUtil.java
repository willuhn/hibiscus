/**********************************************************************
 *
 * Copyright (c) 2022 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.server;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests fuer die UmsatztypUtils.
 */
public class TestUmsatzTypUtil
{
  
  /**
   * Testet das Splitten des Suchbegriffes.
   * @throws Exception
   */
  @Test
  public void test001() throws Exception
  {
    Assert.assertArrayEquals(new String[]{"foo","bar"},UmsatzTypUtil.splitQuery("foo,bar",","));
    Assert.assertArrayEquals(new String[]{"foo","bar"},UmsatzTypUtil.splitQuery(",foo,bar",","));
    Assert.assertArrayEquals(new String[]{"foo","bar"},UmsatzTypUtil.splitQuery("foo,bar,",","));
    Assert.assertArrayEquals(new String[]{"foo","b,ar"},UmsatzTypUtil.splitQuery("foo,b\\,ar,",","));
    Assert.assertArrayEquals(new String[]{"foo","bar"},UmsatzTypUtil.splitQuery("foo , bar",","));
    Assert.assertArrayEquals(new String[]{"foo","bar"},UmsatzTypUtil.splitQuery("foo,,bar",","));
  }
}


