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

import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.io.DataWriter;

/**
 * Summary description for MatroskaSegment.
 */
public class MatroskaSegment extends MasterElement
{
  protected boolean bUnknownSize = false;

  public MatroskaSegment()
  {
    super(MatroskaDocTypes.Segment.getType().array());
  }

  /**
   * Write the element header data. The override will write the size as unknown if the flag is set.
   */
  @Override
  public long writeHeaderData(final DataWriter writer)
  {
    long len = 0;

    final ByteBuffer type = getType();
    len += type.remaining();
    writer.write(type);

    byte[] size;
    if (bUnknownSize)
    {
      size = new byte[5];
      size[0] = (byte) (0xFF >>> (size.length - 1));
      for (int i = 1; i < size.length; i++)
      {
        size[i] = (byte) 0xFF;
      }
    }
    else
    {
      size = Element.makeEbmlCodedSize(getSize());
    }

    len += size.length;
    writer.write(ByteBuffer.wrap(size));

    return len;
  }

  /**
   * Setter for Unknown Size flag. This is a special case for ebml. The size value is filled with 1's.
   */
  public void setUnknownSize(final boolean bUnknownSize)
  {
    this.bUnknownSize = bUnknownSize;
  }

  /**
   * Getter for Unknown Size flag.
   */
  public boolean getUnknownSize()
  {
    return bUnknownSize;
  }
}
