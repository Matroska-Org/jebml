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

import java.util.Arrays;

import org.ebml.Element;
import org.ebml.io.DataWriter;

/**
 * Void element. Doesn't do anything but take up space for later use.
 */
public class VoidElement extends Element
{
  private final static byte[] VOID_TYPE = new byte[] {(byte) 0xEC };

  /*
   * Creates a new instance of Element
   * 
   * @param type The type ID of this element
   */
  public VoidElement(final long size)
  {
    super(VOID_TYPE);
    setSize(size);
  }

  @Override
  public long writeData(final DataWriter ioDW)
  {
    final byte[] voids = new byte[(int) getSize()];
    Arrays.fill(voids, (byte) 1);
    return ioDW.write(voids);
  }

  @Override
  public void setSize(final long size)
  {
    super.setSize(size - Element.codedSizeLength(size) - type.length);
    assert getTotalSize() == size;
  }

  public void reduceSize(final long size)
  {
    super.setSize(getSize() - size);
  }
}
