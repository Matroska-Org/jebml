package org.ebml.matroska;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.Random;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MockSource;
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
    final Random rnd = new Random();
    for (int i = 0; i < 268435455; i += rnd.nextInt(42))
    {
      final byte[] encoded = Element.makeEbmlCodedSize(i);
      final long decoded = EBMLReader.readEBMLCodeSize(encoded[0]);
      assertEquals(encoded.length, decoded);
    }
  }

  @Test
  public void testMakeReadEbmlCode()
  {
    final ByteBuffer buffer = ByteBuffer.allocate(1024);
    final Random rnd = new Random();
    for (int i = 0; i < 268435455; i += rnd.nextInt(42))
    {
      buffer.put(Element.makeEbmlCodedSize(i));
      buffer.flip();

      final MockSource src = new MockSource(buffer);
      assertEquals(i, EBMLReader.readEBMLCode(src));
      buffer.clear();
    }
  }

  @Test
  public void testMakeParseEbmlCode()
  {
    final ByteBuffer buffer = ByteBuffer.allocate(32);
    final Random rnd = new Random();
    for (int i = 0; i < 268435455; i += rnd.nextInt(42))
    {
      final byte[] arr = Element.makeEbmlCodedSize(i);
      arr[0] &= (0xff >>> arr.length);
      buffer.put(arr);
      buffer.flip();
      assertEquals(i, EBMLReader.parseEBMLCode(buffer));
      buffer.clear();
    }
  }
}
