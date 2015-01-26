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
package org.ebml.io;

import java.io.IOException;
import java.io.InputStream;

/*
 * InputStreamDataSource
 *
 * Created on November 19, 2002, 9:35 PM
 *
 * @author  John Cannon
 */
public class InputStreamDataSource implements DataSource
{
  protected InputStream in = null;
  protected long pos = 0;
  protected byte[] buffer = new byte[1];

  /** Creates a new instance of InputStreamDataSource */
  public InputStreamDataSource(final InputStream in)
  {
    this.in = in;
  }

  public InputStream getInputStream()
  {
    return in;
  }

  @Override
  public byte readByte()
  {
    try
    {
      final int l = in.read(buffer);
      pos += l;
      return buffer[0];
    }
    catch (final IOException e)
    {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public int read(final byte[] buff)
  {
    try
    {
      final int l = in.read(buff);
      pos += l;
      return l;
    }
    catch (final IOException e)
    {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public int read(final byte[] buff, final int offset, final int length)
  {
    try
    {
      final int l = in.read(buff, offset, length);
      pos += l;
      return l;
    }
    catch (final IOException e)
    {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public long skip(final long offset)
  {
    try
    {
      final long l = in.skip(offset);
      pos += l;
      return l;
    }
    catch (final IOException e)
    {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public long length()
  {
    try
    {
      return pos + in.available();
    }
    catch (final IOException e)
    {
      e.printStackTrace();
      return -1;
    }
  }

  @Override
  public long getFilePointer()
  {
    return pos;
  }

  @Override
  public boolean isSeekable()
  {
    return false;
  }

  @Override
  public long seek(final long pos)
  {
    return pos;
  }

}
