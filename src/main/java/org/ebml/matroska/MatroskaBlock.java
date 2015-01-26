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

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.util.ArrayCopy;

public class MatroskaBlock
{
  protected int[] sizes = null;
  protected int headerSize = 0;
  protected int blockTimecode = 0;
  protected int trackNo = 0;
  private boolean keyFrame;
  private final byte[] data;

  public MatroskaBlock(final byte[] data)
  {
    this.data = data;
  }

  // public void readData(DataSource source) {
  // parseBlock();
  // }

  public void parseBlock()
  {
    int index = 0;
    trackNo = (int) EBMLReader.readEBMLCode(data);
    index = Element.codedSizeLength(trackNo);
    headerSize += index;

    final short blockTimecode1 = (short) (data[index++] & 0xFF);
    final short blockTimecode2 = (short) (data[index++] & 0xFF);
    if (blockTimecode1 != 0 || blockTimecode2 != 0)
    {
      blockTimecode = (blockTimecode1 << 8) | blockTimecode2;
    }

    final int keyFlag = data[index] & 0x80;
    if (keyFlag > 0)
    {
      this.keyFrame = true;
    }
    else
    {
      this.keyFrame = false;
    }

    final int laceFlag = data[index] & 0x06;
    index++;
    // Increase the HeaderSize by the number of bytes we have read
    headerSize += 3;
    if (laceFlag != 0x00)
    {
      // We have lacing
      final byte laceCount = data[index++];
      headerSize += 1;
      if (laceFlag == 0x02)
      { // Xiph Lacing
        sizes = readXiphLaceSizes(index, laceCount);

      }
      else if (laceFlag == 0x06)
      { // EBML Lacing
        sizes = readEBMLLaceSizes(index, laceCount);

      }
      else if (laceFlag == 0x04)
      { // Fixed Size Lacing
        sizes = new int[laceCount + 1];
        sizes[0] = (data.length - headerSize) / (laceCount + 1);
        for (int s = 0; s < laceCount; s++)
        {
          sizes[s + 1] = sizes[0];
        }
      }
      else
      {
        throw new RuntimeException("Unsupported lacing type flag.");
      }
    }
    // data = new byte[(int)(this.getSize() - HeaderSize)];
    // source.read(data, 0, data.length);
    // this.dataRead = true;
  }

  private int[] readEBMLLaceSizes(int index, final short laceCount)
  {
    final int[] laceSizes = new int[laceCount + 1];
    laceSizes[laceCount] = data.length;

    // This uses the DataSource.getBytePosition() for finding the header size
    // because of the trouble of finding the byte size of sized ebml coded integers
    // long ByteStartPos = source.getFilePointer();
    final int startIndex = index;

    laceSizes[0] = (int) EBMLReader.readEBMLCode(data, index);
    index += Element.codedSizeLength(laceSizes[0]);
    laceSizes[laceCount] -= laceSizes[0];

    long firstEBMLSize = laceSizes[0];
    long lastEBMLSize = 0;
    for (int l = 0; l < laceCount - 1; l++)
    {
      lastEBMLSize = EBMLReader.readSignedEBMLCode(data, index);
      index += Element.codedSizeLength(lastEBMLSize);

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

  private int[] readXiphLaceSizes(int index, final short laceCount)
  {
    final int[] laceSizes = new int[laceCount + 1];
    laceSizes[laceCount] = data.length;

    // long ByteStartPos = source.getFilePointer();

    for (int l = 0; l < laceCount; l++)
    {
      short laceSizeByte = 255;
      while (laceSizeByte == 255)
      {
        laceSizeByte = (short) (data[index++] & 0xFF);
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

  public byte[] getFrame(final int frame)
  {
    if (sizes == null)
    {
      if (frame != 0)
      {
        throw new IllegalArgumentException("Tried to read laced frame on non-laced Block. MatroskaBlock.getFrame(frame > 0)");
      }
      final byte[] frameData = new byte[data.length - headerSize];
      ArrayCopy.arraycopy(data, headerSize, frameData, 0, frameData.length);

      return frameData;
    }
    final byte[] frameData = new byte[sizes[frame]];

    // Calc the frame data offset
    int startOffset = headerSize;
    for (int s = 0; s < frame; s++)
    {
      startOffset += sizes[s];
    }

    // Copy the frame data
    ArrayCopy.arraycopy(data, startOffset, frameData, 0, frameData.length);

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
