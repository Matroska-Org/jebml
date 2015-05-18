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

import java.util.ArrayList;

import org.ebml.io.DataSource;
import org.ebml.io.DataWriter;

public class MasterElement extends Element
{
  protected long usedSize = 0;
  protected ArrayList<Element> children = new ArrayList<>();

  public MasterElement(final byte[] type)
  {
    super(type);
  }

  public MasterElement()
  {
    super();
  }

  public Element readNextChild(final EBMLReader reader)
  {
    if (usedSize >= this.getSize())
    {
      LOG.trace("Can't read any more children");
      return null;
    }

    final Element elem = reader.readNextElement();
    if (elem == null)
    {
      LOG.debug("Reader returned null");
      return null;
    }

    elem.setParent(this);

    usedSize += elem.getTotalSize();

    LOG.trace("Read element {} of size {}: {} remaining", elem.typeInfo.getName(), elem.getTotalSize(), size - usedSize);
    return elem;
  }

  /* Skip the element data */
  @Override
  public void skipData(final DataSource source)
  {
    // Skip the child elements
    source.skip(size - usedSize);
  }

  @Override
  public long writeData(final DataWriter writer)
  {
    long len = 0;
    for (int i = 0; i < children.size(); i++)
    {
      final Element elem = children.get(i);
      len += elem.writeElement(writer);
    }
    return len;
  }

  public void addChildElement(final Element elem)
  {
    children.add(elem);
    size += elem.getTotalSize();
  }
}
