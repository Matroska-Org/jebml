package org.ebml;

import java.nio.ByteBuffer;

import org.ebml.io.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockSource implements DataSource
{

  private static final Logger LOG = LoggerFactory.getLogger(MockSource.class);
  private final ByteBuffer buffer;

  public MockSource(final ByteBuffer buffer)
  {
    this.buffer = buffer;
  }

  @Override
  public long length()
  {
    return buffer.limit();
  }

  @Override
  public long getFilePointer()
  {
    return buffer.position();
  }

  @Override
  public boolean isSeekable()
  {
    return true;
  }

  @Override
  public long seek(final long pos)
  {
    buffer.position((int) pos);
    return pos;
  }

  @Override
  public byte readByte()
  {
    return buffer.get();
  }

  @Override
  public int read(final ByteBuffer buff)
  {
    int bytes = buffer.remaining();
    buff.put(buffer);
    return bytes;
  }

  @Override
  public long skip(final long offset)
  {
    buffer.position((int) (buffer.position() + offset));
    return offset;
  }
}
