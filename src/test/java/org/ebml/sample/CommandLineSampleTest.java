package org.ebml.sample;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CommandLineSampleTest
{

  @Before
  public void setUp() throws Exception
  {
  }

  @After
  public void tearDown() throws Exception
  {
  }

  @Test
  public void test()
  {
    CommandLineSample.main(new String[] {"mkv", "-i", "/home/michael/vx/vxfoundry/Products/vmsexports/trunk/tools/exporty.mkv" });
  }
}
