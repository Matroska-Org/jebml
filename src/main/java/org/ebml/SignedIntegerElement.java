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

/*
 * SignedInteger.java
 *
 * Created on February 17, 2003, 1:54 PM
 */

/**
 * Basic class for the Signed Integer EBML data type.
 * 
 * @author John Cannon
 */
public class SignedIntegerElement extends BinaryElement
{

  public SignedIntegerElement(final byte[] typeID)
  {
    super(typeID);
  }

  public SignedIntegerElement()
  {
    super();
  }

  public void setValue(final long value)
  {
    setData(ByteBuffer.wrap(packInt(value)));
  }

  public long getValue()
  {
    final byte[] dataArray = getDataArray();
    long l = 0;
    long tmp = 0;
    l |= ((long) dataArray[0] << (56 - ((8 - dataArray.length) * 8)));
    for (int i = 1; i < dataArray.length; i++)
    {
      tmp = ((long) dataArray[dataArray.length - i]) << 56;
      tmp >>>= 56 - (8 * (i - 1));
      l |= tmp;
    }
    return l;
  }
}
