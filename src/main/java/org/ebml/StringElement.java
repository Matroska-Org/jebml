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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Baisc class for handling an EBML string data type. This class encapsulates both UTF and ASCII string types and can use any string type supported by
 * the Java platform.
 *
 * @author John Cannon
 */
public class StringElement extends BinaryElement
{

  private Charset charset = StandardCharsets.US_ASCII;

  /** Creates a new instance of StringElement */
  public StringElement(final byte[] typeID)
  {
    super(typeID);
  }

  public StringElement()
  {
    super();
  }

  public StringElement(final byte[] typeID, final Charset encoding)
  {
    super(typeID);
    charset = encoding;
  }

  public StringElement(final Charset encoding)
  {
    super();
    charset = encoding;
  }

  public String getValue()
  {
    return new String(data, charset);
  }

  public void setValue(final String value)
  {
    setData(value.getBytes(charset));
  }

  public Charset getEncoding()
  {
    return charset;
  }
}
