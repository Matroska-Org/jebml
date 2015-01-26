package org.ebml.matroska;

import static org.junit.Assert.assertEquals;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EbmlElementTest
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
  public void testEbmlCodedSizes()
  {
    // TODO: The upper bound here is all that the write-side supports. Should it be better?
    for (int i = 0; i < 268435455; ++i)
    {
      final byte[] encoded = Element.makeEbmlCodedSize(i);
      final long decoded = EBMLReader.readEBMLCodeSize(encoded[0]);
      assertEquals(encoded.length, decoded);
    }
  }
}
