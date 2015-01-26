package org.ebml.matroska.jmf;

import javax.media.protocol.SourceStream;
// CSOFF: AvoidStarImport
import org.ebml.io.*;

// CSON: AvoidStarImport

/**
 * <p>
 * Title: JEBML
 * </p>
 * <p>
 * Description: Java Classes to Read EBML Elements
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002-2004 John Cannon <spyder@matroska.org>, Jory Stone <jcsston@toughguy.net>
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author jcsston
 * @version 1.0
 */

public class JMFPullSourceStreamDataSource implements DataSource
{
  protected javax.media.protocol.PullSourceStream source = null;
  protected javax.media.protocol.Seekable seeking = null;
  protected byte[] buffer = new byte[1];
  protected long pos = 0;

  public JMFPullSourceStreamDataSource(final javax.media.protocol.PullSourceStream source)
  {
    this.source = source;
    if (source instanceof javax.media.protocol.Seekable)
    {
      seeking = (javax.media.protocol.Seekable) source;
      if (!seeking.isRandomAccess())
      {
        seeking = null;
      }
    }
  }

  @Override
  public byte readByte()
  {
    try
    {
      final int l = source.read(buffer, 0, 1);
      pos += l;
      return buffer[0];
    }
    catch (final java.io.IOException e)
    {
      // Internal error?
      return 0;
    }
  }

  @Override
  public int read(final byte[] buff)
  {
    try
    {
      final int l = source.read(buff, 0, buff.length);
      pos += l;
      return l;
    }
    catch (final java.io.IOException e)
    {
      // Internal error?
      return 0;
    }
  }

  @Override
  public int read(final byte[] buff, final int offset, final int length)
  {
    try
    {
      final int l = source.read(buff, offset, length);
      pos += l;
      return l;
    }
    catch (final java.io.IOException e)
    {
      // Internal error?
      return 0;
    }
  }

  @Override
  public long skip(long offset)
  {
    try
    {
      final long origPos = pos;
      while ((offset--) > 0)
      {
        pos += source.read(buffer, 0, 1);
      }
      return pos - origPos;
    }
    catch (final java.io.IOException e)
    {
      System.err.printf("Caught error: %s\n", e);
      // Internal error?

    }
    return 0;
  }

  @Override
  public long length()
  {
    final long length = source.getContentLength();
    if (length == SourceStream.LENGTH_UNKNOWN)
    {
      return -1;
    }

    return length;
  }

  @Override
  public long getFilePointer()
  {
    return pos;
  }

  @Override
  public boolean isSeekable()
  {
    if (seeking != null)
    {
      return true;
    }
    return false;
  }

  @Override
  public long seek(final long pos)
  {
    if (seeking != null)
    {
      return seeking.seek(pos);
    }
    return -1;
  }
}
