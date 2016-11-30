package org.ebml;

import java.util.Arrays;

/**
 * Partially re-implements {@link java.util.BitSet} because Java's BitSet
 * is LSB0 instead of MSB0 and its too much of a pain to translate
 */
public class BitSet
{
  private static final int[] MASKS = new int[] {0x80,
                                                0x40,
                                                0x20,
                                                0x10,
                                                0x08,
                                                0x04,
                                                0x02,
                                                0x01 };
  private static final int[] NOT_MASKS = new int[] {0x7F,
                                                    0xBF,
                                                    0xDF,
                                                    0xEF,
                                                    0xF7,
                                                    0xFB,
                                                    0xFD,
                                                    0xFE };
  private byte[] bitSet;

  public BitSet(int bits)
  {
    int bytes = (bits % 8 == 0 ? 0 : 1) + bits / 8;
    bitSet = new byte[bytes];
  }

  public BitSet set(int bitNumber)
  {
    return set(bitNumber, true);
  }

  public BitSet unset(int bitNumber)
  {
    return set(bitNumber, false);
  }

  public BitSet set(int bitNumber, boolean value)
  {
    int bitInByte = bitNumber % 8;
    int byteNumber = bitNumber / 8;
    if (byteNumber > bitSet.length)
    {
      throw new IndexOutOfBoundsException("No such bit in set");
    }
    if (value)
    {
      bitSet[byteNumber] |= MASKS[bitInByte];
    }
    else
    {
      bitSet[byteNumber] &= NOT_MASKS[bitInByte];
    }
    return this;
  }

  /** 
   * @return
   */
  public byte[] toByteArray()
  {
    return Arrays.copyOf(bitSet, bitSet.length);
  }
}
