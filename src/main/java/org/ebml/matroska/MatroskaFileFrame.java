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

import org.ebml.util.*;

/**
 * Matroska Frame, holds a Matroska frame timecode, duration, and data
 */
public class MatroskaFileFrame
{
  /**
   * Matroska Frame Puller interface
   */
  public interface MatroskaFramePuller
  {
    public void PushNewMatroskaFrame(MatroskaFileFrame frame);
  };

  /**
   * The track this frame belongs to
   */
  private int trackNo;
  /**
   * A timecode, it should be in ms
   */
  private long timecode;
  /**
   * The duration of this frame, it should also be in ms
   */
  private long duration;

  /**
   * List of references
   */
  private ArrayList<Long> references = new ArrayList<>();
  /**
   * The frame data
   */
  private byte[] data;
  private boolean keyFrame;

  /**
   * MatroskaFrame Default constructor
   */
  public MatroskaFileFrame()
  {
    references = new ArrayList<Long>();
    // System.out.println("new " + this);
  }

  /**
   * MatroskaFrame Copy constructor
   * 
   * @param copy MatroskaFrame to copy
   */
  public MatroskaFileFrame(final MatroskaFileFrame copy)
  {
    // System.out.println("MatroskaFrame copy " + this);
    this.trackNo = copy.trackNo;
    this.setTimecode(copy.getTimecode());
    this.setDuration(copy.getDuration());
    this.setKeyFrame(copy.isKeyFrame());
    if (copy.getReferences() != null)
    {
      this.references.addAll(copy.getReferences());
    }
    if (copy.getData() != null)
    {
      this.setData(new byte[copy.getData().length]);
      ArrayCopy.arraycopy(copy.getData(), 0, this.getData(), 0, copy.getData().length);
    }
  }

  public boolean isKeyFrame()
  {
    return keyFrame;
  }

  public int getTrackNo()
  {
    return trackNo;
  }

  public void setTrackNo(final int trackNo)
  {
    this.trackNo = trackNo;
  }

  public ArrayList<Long> getReferences()
  {
    return references;
  }

  public void addReferences(final long... references)
  {
    for (final Long ref: references)
    {
      this.references.add(ref);
    }
  }

  public long getTimecode()
  {
    return timecode;
  }

  public void setTimecode(final long timecode)
  {
    this.timecode = timecode;
  }

  public long getDuration()
  {
    return duration;
  }

  public void setDuration(final long duration)
  {
    this.duration = duration;
  }

  public byte[] getData()
  {
    return data;
  }

  public void setData(final byte[] data)
  {
    this.data = data;
  }

  public void setKeyFrame(final boolean keyFrame)
  {
    this.keyFrame = keyFrame;
  }
}
