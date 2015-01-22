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

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.ebml.MasterElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataWriter;

/**
 * A cluster of frames in a file. Used internally during muxing.
 */
class MatroskaCluster extends MasterElement
{
  private final Queue<MatroskaFileFrame> frames = new ConcurrentLinkedQueue<>();
  private final Set<Integer> tracks = new HashSet<>();
  public long clusterTimecode = Long.MAX_VALUE;
  private int sizeLimit = Integer.MAX_VALUE;
  private int totalSize = 0;
  private long durationLimit = Long.MAX_VALUE;

  public MatroskaCluster(final byte[] type)
  {
    super(type);
  }

  void setLimitParameters(final long duration, final int size)
  {
    this.sizeLimit = size;
    this.durationLimit = duration;
  }

  /**
   * Add a frame to the cluster
   * 
   * @param frame
   * @return false if you should begin another cluster.
   */
  public boolean AddFrame(final MatroskaFileFrame frame)
  {
    // Is this the earliest timecode?
    if (frame.getTimecode() < clusterTimecode)
    {
      clusterTimecode = frame.getTimecode();
    }
    frames.add(frame);
    totalSize += frame.getData().length;
    tracks.add(frame.getTrackNo());
    return ((frame.getTimecode() - clusterTimecode) < durationLimit) && (totalSize < sizeLimit);
  }

  public long flush(final DataWriter ioDW)
  {
    if (frames.size() == 0)
      return 0;
    try
    {
      final MasterElement clusterElem = (MasterElement) MatroskaDocType.obj.createElement(MatroskaDocType.Cluster_Id);
      final UnsignedIntegerElement timecodeElem = (UnsignedIntegerElement) MatroskaDocType.obj.createElement(MatroskaDocType.ClusterTimecode_Id);
      timecodeElem.setValue(clusterTimecode);
      clusterElem.addChildElement(timecodeElem);

      MatroskaSimpleBlock block = null;
      boolean forceNew = true;
      long lastTimecode = 0;
      int lastTrackNumber = 0;
      for (final MatroskaFileFrame frame: frames)
      {
        frame.setTimecode(frame.getTimecode() - clusterTimecode);
        if (forceNew || lastTimecode != frame.getTimecode() || lastTrackNumber != frame.getTrackNo())
        {
          if (block != null)
          {
            clusterElem.addChildElement(block.toElement());
          }
          block = new MatroskaSimpleBlock();
        }
        lastTimecode = frame.getTimecode();
        lastTrackNumber = frame.getTrackNo();
        forceNew = !block.addFrame(frame);
      }
      if (block != null)
      {
        clusterElem.addChildElement(block.toElement());
      }
      return clusterElem.writeElement(ioDW);
    }
    finally
    {
      frames.clear();
      tracks.clear();
      totalSize = 0;
      clusterTimecode = Long.MAX_VALUE;
    }
  }

  public long getClusterTimecode()
  {
    return clusterTimecode;
  }

  public Collection<Integer> getTracks()
  {
    return tracks;
  }
}
