/**
 * Copyright (c) 2015 Pelco. All rights reserved.
 *
 * This file contains trade secrets of Pelco.  No part may be reproduced or
 * transmitted in any form by any means or for any purpose without the express
 * written permission of Pelco.
 */

package org.ebml.matroska;

import org.ebml.MockWriter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VoidElementTest
{
  @Before
  public void setUp() throws Exception
  {
  }

  @After
  public void tearDown() throws Exception
  {
  }

  /**
   * Voids of size 2 to 9 should be all header, no body
   */
  @Test
  public void test2To9()
  {
    for (int i = 2; i < 10; ++i)
    {
      VoidElement voidz = new VoidElement(i);
      MockWriter mw = new MockWriter();
      voidz.writeElement(mw);
      Assert.assertEquals(i, mw.getBuff().position());
    }
  }

  /**
   * Voids of size > 10 will have body. Under the previous schema for writing voids, value 127 would have failed.
   */
  @Test
  public void test10To130()
  {
    for (int i = 10; i < 130; ++i)
    {
      VoidElement voidz = new VoidElement(i);
      MockWriter mw = new MockWriter();
      voidz.writeElement(mw);
      Assert.assertEquals(i, mw.getBuff().position());
    }
  }
}
