/**
 * JEBML - Java library to read/write EBML/Matroska elements.
 * Copyright (C) 2004 Jory Stone <jebml@jory.info>
 * Based on Javatroska (C) 2002 John Cannon <spyder@matroska.org>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.ebml;

/*
 * Element.java
 *
 * Created on November 19, 2002, 9:11 PM
 */

import java.util.Arrays;

import org.ebml.io.DataSource;
import org.ebml.io.DataWriter;
import org.ebml.util.ArrayCopy;

/**
 * Defines the basic EBML element. Subclasses may provide child element access.
 * 
 * @author John Cannon
 */
public class Element
{
  private static int minSizeLength = 0;

  protected Element parent;
  protected ProtoType<?> typeInfo;
  protected byte[] type = {
      0x00 };
  protected long size = 0;
  protected byte[] data = null;
  protected boolean dataRead = false;

  /** Creates a new instance of Element */
  public Element(final byte[] type)
  {
    this.type = type;
  }

  public Element()
  {
  }

  /**
   * Read the element data
   */
  public void readData(final DataSource source)
  {
    // Setup a buffer for it's data
    this.data = new byte[(int) size];
    // Read the data
    source.read(this.data, 0, this.data.length);
    dataRead = true;
    // System.out.printf("Read %d bytes from %s", size, typeInfo.name);
  }

  /**
   * Skip the element data
   */
  public void skipData(final DataSource source)
  {
    if (!dataRead)
    {
      // Skip the data
      source.skip(size);
      dataRead = true;
    }
  }

  public long writeElement(final DataWriter writer)
  {
    return writeHeaderData(writer) + writeData(writer);
  }

  /**
   * Write the element header data. Override this in sub-classes for more specialized writing.
   */
  public long writeHeaderData(final DataWriter writer)
  {
    long len = 0;

    len += type.length;
    writer.write(type);

    final byte[] encodedSize = Element.makeEbmlCodedSize(getSize());
    // System.out.printf("Writing header for element %s with size %d (%s)\n", typeInfo.name, getTotalSize(), EBMLReader.bytesToHex(size));

    len += encodedSize.length;
    writer.write(encodedSize);

    return len;
  }

  /**
   * Write the element data. Override this in sub-classes for more specialized writing.
   */
  public long writeData(final DataWriter writer)
  {
    if (data == null)
    {
      throw new NullPointerException(String.format("No data to write: %s : %s", typeInfo.getName(), Arrays.toString(this.type)));
    }
    return writer.write(this.data);
  }

  /**
   * Getter for property data.
   * 
   * @return Value of property data.
   *
   */
  public byte[] getData()
  {
    return this.data;
  }

  /**
   * Setter for property data.
   * 
   * @param data New value of property data.
   *
   */
  public void setData(final byte[] data)
  {
    this.data = data;
    this.size = data.length;
  }

  /**
   * Clears the data of this element, useful if you just want this element to be a placeholder
   */
  public void clearData()
  {
    this.data = null;
  }

  /**
   * Getter for property size.
   * 
   * @return Value of property size.
   *
   */
  public long getSize()
  {
    return size;
  }

  /**
   * Setter for property size.
   * 
   * @param size New value of property size.
   *
   */
  public void setSize(final long size)
  {
    this.size = size;
  }

  /**
   * Get the total size of this element
   */
  public long getTotalSize()
  {
    long totalSize = 0;
    totalSize += getType().length;
    totalSize += Element.codedSizeLength(getSize());
    // totalSize += this.headerSize;
    totalSize += getSize();
    return totalSize;
  }

  /**
   * Getter for property type.
   * 
   * @return Value of property type.
   *
   */
  public byte[] getType()
  {
    return type;
  }

  /**
   * Setter for property type.
   * 
   * @param type New value of property type.
   *
   */
  public void setType(final byte[] type)
  {
    this.type = type;
  }

  public void setElementType(final ProtoType<?> typeInfo)
  {
    this.typeInfo = typeInfo;
  }

  public ProtoType<?> getElementType()
  {
    return typeInfo;
  }

  /**
   * Getter for property parent.
   * 
   * @return Value of property parent.
   *
   */
  public Element getParent()
  {
    return this.parent;
  }

  /**
   * Setter for property parent.
   * 
   * @param parent New value of property parent.
   *
   */
  public void setParent(final Element parent)
  {
    this.parent = parent;
  }

  public byte[] toByteArray()
  {
    final byte[] head = makeEbmlCode(type, size);
    final byte[] ret = new byte[head.length + data.length];
    org.ebml.util.ArrayCopy.arraycopy(head, 0, ret, 0, head.length);
    org.ebml.util.ArrayCopy.arraycopy(data, 0, ret, head.length, data.length);
    return ret;
  }

  public boolean isType(final byte[] typeId)
  {
    return Arrays.equals(this.type, typeId);
  }

  public static void setMinSizeLength(final int minSize)
  {
    minSizeLength = minSize;
  }

  public static int getMinSizeLength()
  {
    return minSizeLength;
  }

  public static byte[] makeEbmlCode(final byte[] typeID, final long size)
  {
    final int codedLen = codedSizeLength(size);
    final byte[] ret = new byte[typeID.length + codedLen];
    ArrayCopy.arraycopy(typeID, 0, ret, 0, typeID.length);
    final byte[] codedSize = makeEbmlCodedSize(size);
    ArrayCopy.arraycopy(codedSize, 0, ret, typeID.length, codedSize.length);
    return ret;
  }

  public static byte[] makeEbmlCodedSize(final long size)
  {
    final int len = codedSizeLength(size);
    final byte[] ret = new byte[len];
    // byte[] packedSize = packIntUnsigned(size);
    long mask = 0x00000000000000FFL;
    for (int i = 0; i < len; i++)
    {
      ret[len - 1 - i] = (byte) ((size & mask) >>> (i * 8));
      mask <<= 8;
    }
    // The first size bits should be clear, otherwise we have an error in the size determination.
    ret[0] |= 0x80 >> (len - 1);
    return ret;
  }

  public static int getMinByteSize(final long value)
  {
    if (value <= 0x7F && value >= 0x80)
    {
      return 1;
    }
    else if (value <= 0x7FFF && value >= 0x8000)
    {
      return 2;
    }
    else if (value <= 0x7FFFFF && value >= 0x800000)
    {
      return 3;
    }
    else if (value <= 0x7FFFFFFF && value >= 0x80000000)
    {
      return 4;
    }
    else if (value <= 0x7FFFFFFFFFL && value >= 0x8000000000L)
    {
      return 5;
    }
    else if (value <= 0x7FFFFFFFFFFFL && value >= 0x800000000000L)
    {
      return 6;
    }
    else if (value <= 0x7FFFFFFFFFFFFFL && value >= 0x80000000000000L)
    {
      return 7;
    }
    else
    {
      return 8;
    }
  }

  public static int getMinByteSizeUnsigned(final long value)
  {
    int size = 8;
    long mask = 0xFF00000000000000L;
    for (int i = 0; i < 8; i++)
    {
      if ((value & mask) == 0)
      {
        mask = mask >>> 8;
        size--;
      }
      else
      {
        return size;
      }
    }
    return 8;
  }

  public static int codedSizeLength(final long value)
  {
    int codedSize = 0;
    if (value < 127)
    {
      codedSize = 1;
    }
    else if (value < 16383)
    {
      codedSize = 2;
    }
    else if (value < 2097151)
    {
      codedSize = 3;
    }
    else if (value < 268435455)
    {
      codedSize = 4;
    }
    if ((minSizeLength > 0) && (codedSize <= minSizeLength))
    {
      codedSize = minSizeLength;
    }
    else
    {
      codedSize = 8;
    }
    return codedSize;
  }

  public static byte[] packIntUnsigned(final long value)
  {
    final int size = getMinByteSizeUnsigned(value);
    return packInt(value, size);
  }

  public static byte[] packInt(final long value)
  {
    final int size = getMinByteSize(value);
    return packInt(value, size);
  }

  public static byte[] packInt(final long value, final int size)
  {
    final byte[] ret = new byte[size];
    final long mask = 0x00000000000000FFL;
    int b = size - 1;
    for (int i = 0; i < size; i++)
    {
      ret[b] = (byte) (((value >>> (8 * i)) & mask));
      b--;
    }
    return ret;
  }
}
