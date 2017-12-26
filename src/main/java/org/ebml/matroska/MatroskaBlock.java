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
import java.util.Arrays;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatroskaBlock
{
  private static final Logger LOG = LoggerFactory.getLogger(MatroskaBlock.class);

  protected int[] sizes = null;
  protected int headerSize = 0;
  protected int blockTimecode = 0;
  protected int trackNo = 0;
  private boolean keyFrame;
  private final ByteBuffer data;

  public MatroskaBlock(final ByteBuffer data)
  {
    LOG.trace("Block created with data {}, {}", data.limit(), data.position());
    this.data = data;
  }

  // public void readData(DataSource source) {
  // parseBlock();
  // }

  public void parseBlock()
  {
    int index = 0;
    trackNo = (int) EBMLReader.readEBMLCode(data);
    index = Element.codedSizeLength(trackNo, 0);
    headerSize += index;

    blockTimecode = data.getShort();
    LOG.trace("Block belongs to track {} @ {}", trackNo, blockTimecode);

    final byte flagsByte = data.get();
    final int keyFlag = flagsByte & 0x80;
    if (keyFlag > 0)
    {
      this.keyFrame = true;
    }
    else
    {
      this.keyFrame = false;
    }

    final int laceFlag = flagsByte & 0x06;
    index++;
    // Increase the HeaderSize by the number of bytes we have read
    headerSize += 3;
    if (laceFlag != 0x00)
    {
      // We have lacing
      final byte laceCount = data.get();
      headerSize += 1;
      if (laceFlag == 0x02)
      { // Xiph Lacing
        LOG.trace("Reading xiph lace sizes");
        sizes = readXiphLaceSizes(index, laceCount);
      }
      else if (laceFlag == 0x06)
      { // EBML Lacing
        LOG.trace("Reading ebml lace sizes");
        sizes = readEBMLLaceSizes(index, laceCount);
      }
      else if (laceFlag == 0x04)
      { // Fixed Size Lacing
        LOG.trace("Fixed lace sizes");
        sizes = new int[laceCount + 1];
        sizes[0] = data.remaining() / (laceCount + 1);
        for (int s = 0; s < laceCount; s++)
        {
          sizes[s + 1] = sizes[0];
        }
      }
      else
      {
        throw new RuntimeException("Unsupported lacing type flag.");
      }
      LOG.trace("Lace sizes: {}", Arrays.toString(sizes));
    }
    // data = new byte[(int)(this.getSize() - HeaderSize)];
    // source.read(data, 0, data.length);
    // this.dataRead = true;
    headerSize = data.position();
  }

  private int[] readEBMLLaceSizes(int index, final short laceCount)
  {
    final int[] laceSizes = new int[laceCount + 1];
    laceSizes[laceCount] = data.remaining();

    // This uses the DataSource.getBytePosition() for finding the header size
    // because of the trouble of finding the byte size of sized ebml coded integers
    // long ByteStartPos = source.getFilePointer();
    final int startIndex = index;

    laceSizes[0] = (int) EBMLReader.readEBMLCode(data);
    index += Element.codedSizeLength(laceSizes[0], 0);
    laceSizes[laceCount] -= laceSizes[0];

    long firstEBMLSize = laceSizes[0];
    long lastEBMLSize = 0;
    for (int l = 0; l < laceCount - 1; l++)
    {
      lastEBMLSize = EBMLReader.readSignedEBMLCode(data);
      index += Element.codedSizeLength(lastEBMLSize, 0);

      firstEBMLSize += lastEBMLSize;
      laceSizes[l + 1] = (int) firstEBMLSize;

      // Update the size of the last block
      laceSizes[laceCount] -= laceSizes[l + 1];
    }
    // long ByteEndPos = source.getFilePointer();

    // HeaderSize = HeaderSize + (int)(ByteEndPos - ByteStartPos);
    headerSize = headerSize + index - startIndex;
    laceSizes[laceCount] -= headerSize;

    return laceSizes;
  }

  private int[] readXiphLaceSizes(final int index, final short laceCount)
  {
    final int[] laceSizes = new int[laceCount + 1];
    laceSizes[laceCount] = data.remaining();

    // long ByteStartPos = source.getFilePointer();

    for (int l = 0; l < laceCount; l++)
    {
      short laceSizeByte = 255;
      while (laceSizeByte == 255)
      {
        laceSizeByte = (short) (data.get() & 0xFF);
        headerSize += 1;
        laceSizes[l] += laceSizeByte;
      }
      // Update the size of the last block
      laceSizes[laceCount] -= laceSizes[l];
    }
    // long ByteEndPos = source.getFilePointer();

    laceSizes[laceCount] -= headerSize;

    return laceSizes;
  }

  public int getFrameCount()
  {
    if (sizes == null)
    {
      return 1;
    }
    return sizes.length;
  }

  public ByteBuffer getFrame(final int frame)
  {
    int startOffset = headerSize;
    int endOffset;
    if (sizes == null)
    {
      if (frame != 0)
      {
        throw new IllegalArgumentException("Tried to read laced frame on non-laced Block. MatroskaBlock.getFrame(frame > 0)");
      }
      endOffset = headerSize + data.remaining();
    }
    else
    {
      // Calc the frame data offset
      for (int s = 0; s < frame; s++)
      {
        startOffset += sizes[s];
      }
      endOffset = sizes[frame] + startOffset;
    }

    // Copy the frame data
    final ByteBuffer frameData = data.duplicate();
    frameData.position(startOffset);
    frameData.limit(endOffset);
    return frameData;
  }

  public long getAdjustedBlockTimecode(final long clusterTimecode, final long timecodeScale)
  {
    return clusterTimecode + (blockTimecode); // * timecodeScale);
  }

  public int getTrackNo()
  {
    return trackNo;
  }

  public int getBlockTimecode()
  {
    return blockTimecode;
  }

  public void setFrameData(final short trackNo, final int timecode, final byte[] data)
  {

  }

  public boolean isKeyFrame()
  {
    return keyFrame;
  }
}
