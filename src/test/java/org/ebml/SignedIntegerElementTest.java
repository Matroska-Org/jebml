package org.ebml;

import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignedIntegerElementTest
{

  private static final Logger LOG = LoggerFactory.getLogger(UnsignedIntegerElementTest.class);

  private final MockWriter writer = new MockWriter();
  private final ProtoType<SignedIntegerElement> typeInfo = new ProtoType<>(SignedIntegerElement.class,
                                                                           "test",
                                                                           new byte[] {(byte) 0xC2 },
                                                                           1);
  private final SignedIntegerElement elem = typeInfo.getInstance();

  @Before
  public void setUp() throws Exception
  {
  }

  @After
  public void tearDown() throws Exception
  {
  }

  @Test
  public void testPositiveData()
  {
    for (int i = 0; i < 8; ++i)
    {
      final long value = (long) (42 * Math.pow(256, i));
      LOG.debug("Testing {} for {}", value, i);
      elem.setValue(value);
      Assert.assertEquals(value, elem.getValue());
      elem.writeData(writer);
      final ByteBuffer buf = writer.getBuff();
      buf.flip();
      Assert.assertEquals(i + 1, buf.remaining());
      Assert.assertEquals((byte) 42, buf.get());
      for (int k = i; k > 0; --k)
      {
        Assert.assertEquals(0, buf.get());
      }
      buf.clear();
    }
  }

  @Test
  public void testNegativeData()
  {
    for (int i = 0; i < 7; ++i)
    {
      final long value = (long) (-42 * Math.pow(256, i));
      LOG.debug("Testing {} for {}", value, i);
      elem.setValue(value);
      Assert.assertEquals(value, elem.getValue());
      elem.writeData(writer);
      final ByteBuffer buf = writer.getBuff();
      buf.flip();
      Assert.assertEquals(i + 1, buf.remaining());
      Assert.assertEquals((byte) -42, buf.get());
      for (int k = i; k > 0; --k)
      {
        Assert.assertEquals(0, buf.get());
      }
      buf.clear();
    }
  }

  @Test
  public void testPositiveElement()
  {
    for (int i = 0; i < 8; ++i)
    {
      final long value = (long) (42 * Math.pow(256, i));
      final long size = Element.getMinByteSize(value);
      final long sizeSize = Element.getMinByteSizeUnsigned(size);
      LOG.debug("Testing element {} val {} ({})", i, value, size);

      elem.setValue(value);
      Assert.assertEquals(value, elem.getValue());
      elem.writeElement(writer);
      final ByteBuffer buf = writer.getBuff();
      buf.flip();
      Assert.assertEquals(i + 2 + sizeSize, buf.remaining());
      Assert.assertEquals((byte) 0xC2, buf.get());
      Assert.assertEquals(size, EBMLReader.readEBMLCode(buf));
      Assert.assertEquals((byte) 42, buf.get());
      for (int k = i; k > 0; --k)
      {
        Assert.assertEquals(0, buf.get());
      }
      buf.clear();
    }
  }

  @Test
  public void testNegativeElement()
  {
    for (int i = 0; i < 8; ++i)
    {
      final long value = (long) (-42 * Math.pow(256, i));
      final long size = Element.getMinByteSize(value);
      final long sizeSize = Element.getMinByteSizeUnsigned(size);
      LOG.debug("Testing element {} val {} ({})", i, value, size);

      elem.setValue(value);
      Assert.assertEquals(value, elem.getValue());
      elem.writeElement(writer);
      final ByteBuffer buf = writer.getBuff();
      buf.flip();
      Assert.assertEquals(i + 2 + sizeSize, buf.remaining());
      Assert.assertEquals((byte) 0xC2, buf.get());
      Assert.assertEquals(size, EBMLReader.readEBMLCode(buf));
      Assert.assertEquals((byte) -42, buf.get());
      for (int k = i; k > 0; --k)
      {
        Assert.assertEquals(0, buf.get());
      }
      buf.clear();
    }
  }
}
