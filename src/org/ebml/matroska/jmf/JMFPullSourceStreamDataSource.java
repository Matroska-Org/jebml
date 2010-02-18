package org.ebml.matroska.jmf;

import org.ebml.*;
import org.ebml.io.*;

/**
 * <p>Title: JEBML</p>
 * <p>Description: Java Classes to Read EBML Elements</p>
 * <p>Copyright: Copyright (c) 2002-2004 John Cannon <spyder@matroska.org>, Jory Stone <jcsston@toughguy.net></p>
 * <p>Company: </p>
 * @author jcsston
 * @version 1.0
 */

public class JMFPullSourceStreamDataSource implements DataSource {
  protected javax.media.protocol.PullSourceStream source = null;
  protected javax.media.protocol.Seekable seeking = null;
  protected byte [] buffer = new byte[1];
  protected long pos = 0;

  public JMFPullSourceStreamDataSource(javax.media.protocol.PullSourceStream source) {
    this.source = source;
    if (source instanceof javax.media.protocol.Seekable) {
      seeking = (javax.media.protocol.Seekable)source;
      if (!seeking.isRandomAccess()) {
        seeking = null;
      }
    }
  }
  public byte readByte() {
    try {
      int l = source.read(buffer, 0, 1);
      pos += l;
      return buffer[0];
    } catch (java.io.IOException e) {
      // Internal error?
      return 0;
    }
  }
  public int read(byte[] buff) {
    try {
      int l = source.read(buff, 0, buff.length);
      pos += l;
      return l;
    } catch (java.io.IOException e) {
      // Internal error?
      return 0;
    }
  }
  public int read(byte[] buff, int offset, int length) {
    try {
      int l = source.read(buff, offset, length);
      pos += l;
      return l;
    } catch (java.io.IOException e) {
      // Internal error?
      return 0;
    }
  }
  public long skip(long offset) {
    try {
      long origPos = pos;
      while ((offset--) > 0) {
        pos += source.read(buffer, 0, 1);
      }
      return pos-origPos;
    } catch (java.io.IOException e) {
      // Internal error?

    }
    return 0;
  }

  public long length() {
    long length = source.getContentLength();
    if (length == source.LENGTH_UNKNOWN)
      return -1;

    return length;
  }

  public long getFilePointer() {
    return pos;
  }

  public boolean isSeekable() {
    if (seeking != null)
      return true;
    return false;
  }

  public long seek(long pos) {
    if (seeking != null) {
      return seeking.seek(pos);
    }
    return -1;
  }
}
