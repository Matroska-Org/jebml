package org.ebml;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests {@link BitSet} 
 */
public class BitSetTest
{
  private static final Logger LOG = LoggerFactory.getLogger(BitSetTest.class);

  private void testBits(int expectByte, int... bits)
  {
    LOG.info("Testing bits {}, expecting {}", Arrays.toString(bits), expectByte);
    BitSet bitSet = new BitSet(8);
    for (int bit : bits)
    {
      bitSet.set(bit);
    }
    Assert.assertEquals((byte) expectByte, bitSet.toByteArray()[0]);
  }

  @Test
  public void testOneBit()
  {
    testBits(0x80, 0);
    testBits(0x20, 2);
    testBits(0x04, 5);
  }

  @Test
  public void testTwoBits()
  {
    testBits(0x48, 1, 4);
    testBits(0x12, 3, 6);
  }

  @Test
  public void testManyBits()
  {
    testBits(0x58, 1, 3, 4);
    testBits(0xF0, 0, 1, 2, 3);
    testBits(0xFF, 0, 1, 2, 3, 4, 5, 6, 7);
  }
}
