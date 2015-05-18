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
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matroska Frame, holds a Matroska frame timecode, duration, and data. <br>
 * Note that the data, track number, and timecode fields are all mandatory. 
 */
public class MatroskaFileFrame
{
  private static final Logger LOG = LoggerFactory.getLogger(MatroskaFileFrame.class);

  private int trackNo;
  /**
   * A timecode, it should be in ms
   */
  private long timecode;
  /**
   * The duration of this frame, it should also be in ms
   */
  private long duration = Long.MIN_VALUE;

  /**
   * List of references
   */
  private ArrayList<Long> references = new ArrayList<>();
  /**
   * The frame data
   */
  private ByteBuffer data;
  private boolean keyFrame;

  /**
   * MatroskaFrame Default constructor
   */
  public MatroskaFileFrame()
  {
    references = new ArrayList<>();
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
      this.setData(copy.getData().duplicate());
    }
  }

  /**
   * @return true if this is a key frame
   */
  public boolean isKeyFrame()
  {
    return keyFrame;
  }

  /**
   * @param keyFrame true if this is a key frame
   */
  public void setKeyFrame(final boolean keyFrame)
  {
    this.keyFrame = keyFrame;
  }

  /**
   * @return The track this frame belongs to
   */
  public int getTrackNo()
  {
    return trackNo;
  }

  /**
   * @param trackNo The track this frame belongs to
   */
  public void setTrackNo(final int trackNo)
  {
    this.trackNo = trackNo;
  }

  /**
   * @return The list of frames that this frame references
   */
  public ArrayList<Long> getReferences()
  {
    return references;
  }

  /**
   * @param references Some frames that this frame references, added to the list.
   */
  public void addReferences(final long... references)
  {
    for (final Long ref: references)
    {
      this.references.add(ref);
    }
  }

  /**
   * @return the presentation timecode that this frame is associated with
   */
  public long getTimecode()
  {
    return timecode;
  }

  /**
   * @param timecode the presentation timecode that this frame is associated with
   */
  public void setTimecode(final long timecode)
  {
    this.timecode = timecode;
  }

  /**
   * @return duration of this frame
   */
  public long getDuration()
  {
    return duration;
  }

  /**
   * @param duration duration of this frame
   */
  public void setDuration(final long duration)
  {
    this.duration = duration;
  }

  /**
   * @return the data contained in this frame.
   */
  public ByteBuffer getData()
  {
    return data.duplicate();
  }

  /**
   * @param data the data associated with this frame
   */
  public void setData(final ByteBuffer data)
  {
    LOG.trace("Setting data with size {}", data.remaining());
    this.data = data.duplicate();
  }

}
