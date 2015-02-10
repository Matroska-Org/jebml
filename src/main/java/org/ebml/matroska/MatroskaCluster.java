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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.ebml.MasterElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A cluster of frames in a file. Used internally during muxing.
 */
class MatroskaCluster
{
  private static final Logger LOG = LoggerFactory.getLogger(MatroskaCluster.class);
  private final Queue<MatroskaFileFrame> frames = new ConcurrentLinkedQueue<>();
  private final Set<Integer> tracks = new HashSet<>();
  private final List<Long> sliencedTracks = new ArrayList<>();

  private long clusterTimecode = Long.MAX_VALUE;
  private int sizeLimit = Integer.MAX_VALUE;
  private int totalSize = 0;
  private long durationLimit = Long.MAX_VALUE;

  public MatroskaCluster()
  {
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
  public boolean addFrame(final MatroskaFileFrame frame)
  {
    // Is this the earliest timecode?
    if (frame.getTimecode() < clusterTimecode)
    {
      clusterTimecode = frame.getTimecode();
    }
    frames.add(frame);
    totalSize += frame.getData().remaining();
    tracks.add(frame.getTrackNo());
    return ((frame.getTimecode() - clusterTimecode) < durationLimit) && (totalSize < sizeLimit);
  }

  public long flush(final DataWriter ioDW)
  {
    if (frames.size() == 0)
    {
      return 0;
    }
    try
    {
      final MasterElement clusterElem = MatroskaDocTypes.Cluster.getInstance();
      final UnsignedIntegerElement timecodeElem = MatroskaDocTypes.Timecode.getInstance();
      timecodeElem.setValue(clusterTimecode);
      clusterElem.addChildElement(timecodeElem);

      if (!sliencedTracks.isEmpty())
      {
        final MasterElement silentElem = MatroskaDocTypes.SilentTracks.getInstance();
        for (final Long silent: sliencedTracks)
        {
          final UnsignedIntegerElement silentTrackElem = MatroskaDocTypes.SilentTrackNumber.getInstance();
          silentTrackElem.setValue(silent);
          silentElem.addChildElement(silentTrackElem);
        }
        clusterElem.addChildElement(silentElem);
      }

      MatroskaSimpleBlock block = null;
      boolean forceNew = true;
      long lastTimecode = 0;
      int lastTrackNumber = 0;
      LOG.trace("Timecode for cluster set to {}", clusterTimecode);
      for (final MatroskaFileFrame frame: frames)
      {
        frame.setTimecode(frame.getTimecode() - clusterTimecode);
        LOG.trace("Timecode for frame set to {}", frame.getTimecode());
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

  public void unsilenceTrack(final long trackNumber)
  {
    sliencedTracks.remove(trackNumber);
  }

  public void silenceTrack(final long trackNumber)
  {
    sliencedTracks.add(trackNumber);
  }
}
