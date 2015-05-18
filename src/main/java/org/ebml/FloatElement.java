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

import java.nio.ByteBuffer;

public class FloatElement extends BinaryElement
{
  public FloatElement(final byte[] type)
  {
    super(type);
  }

  public FloatElement()
  {
    super();
  }

  /**
   * Set the float value of this element
   * 
   * @param value Float value to set
   * @throws ArithmeticException if the float value is larger than Double.MAX_VALUE
   */
  public void setValue(final double value)
  {
    if (value < Float.MAX_VALUE)
    {
      final ByteBuffer buf = ByteBuffer.allocate(4);
      buf.putFloat((float) value);
      buf.flip();
      setData(buf);
    }
    else
    {
      final ByteBuffer buf = ByteBuffer.allocate(4);
      buf.putDouble(value);
      buf.flip();
      setData(buf);
    }
  }

  /**
   * Get the float value of this element
   * 
   * @return Float value of this element
   * @throws ArithmeticException for 80-bit or 10-byte floats. AFAIK Java doesn't support them
   */
  public double getValue()
  {
    data.mark();
    try
    {
      if (size == 4)
      {
        return data.getFloat();
      }
      else if (size == 8)
      {
        return data.getDouble();
      }
      else
      {
        throw new ArithmeticException("80-bit floats are not supported");
      }
    }
    finally
    {
      data.reset();
    }
  }
}
