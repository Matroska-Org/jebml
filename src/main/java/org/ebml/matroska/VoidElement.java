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

package org.ebml.matroska;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.io.DataWriter;

/**
 * Void element. Doesn't do anything but take up space for later use.
 */
public class VoidElement extends Element
{
  private static final long MAX_SIZE = (long) (Math.pow(2, 56) - 2);
  private long realSize;

  /*
   * Creates a new instance of Element
   * 
   * @param type The type ID of this element
   */
  public VoidElement(final long size)
  {
    super();
    setElementType(MatroskaDocTypes.Void);
    setType(MatroskaDocTypes.Void.getType());
    setSize(size);
  }

  /**
   * @see Element#writeHeaderData(DataWriter)
   */
  @Override
  public long writeHeaderData(final DataWriter writer)
  {

    int len = 0;

    len += getType().remaining();

    final byte[] encodedSize = Element.makeEbmlCodedSize(getSize(), (int) Math.min(realSize - len, 8));
    // System.out.printf("Writing header for element %s with size %d (%s)\n", typeInfo.name, getTotalSize(), EBMLReader.bytesToHex(size));

    len += encodedSize.length;
    final ByteBuffer buf = ByteBuffer.allocate(len);
    buf.put(getType());
    buf.put(encodedSize);
    buf.flip();
    LOG.trace("Writing out header {}, {}", buf.remaining(), EBMLReader.bytesToHex(buf.array()));
    writer.write(buf);
    return len;
  }

  @Override
  public long writeData(final DataWriter ioDW)
  {
    final byte[] voids = new byte[(int) getSize()];
    Arrays.fill(voids, (byte) 1);
    return ioDW.write(ByteBuffer.wrap(voids));
  }

  @Override
  public void setSize(final long size)
  {
    if (size < 2 || size > MAX_SIZE)
    {
      throw new IllegalArgumentException("Size must be greater than one and less than (2^52 - 2)");
    }
    // In order to be able to create voids of every possible size, we must force the size field to be larger than required
    // The simplest thing to do is to first force the size field to the maximum, 8 bytes. So a 9 byte void will have 0 "body" bytes
    // but 1 byte type header and 8 bytes size header.
    realSize = size;
    long partialSize = size - type.remaining();
    super.setSize(partialSize - Math.min(partialSize, 8));
  }

  public void reduceSize(final long size)
  {
    super.setSize(getSize() - size);
  }
}
