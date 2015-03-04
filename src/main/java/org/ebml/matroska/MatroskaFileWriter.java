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

import org.ebml.MasterElement;
import org.ebml.StringElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Primary API entrypoint for writing Matroska files.
 */
public class MatroskaFileWriter
{
  private static final Logger LOG = LoggerFactory.getLogger(MatroskaFileWriter.class);

  protected DataWriter ioDW;

  private final MatroskaFileMetaSeek metaSeek;
  private final MatroskaFileCues cueData;
  private final MatroskaCluster cluster;
  private final MatroskaSegmentInfo segmentInfoElem;
  private final MatroskaFileTracks tracks;

  /**
   * @param outputDataWriter DataWriter to write out to.
   */
  public MatroskaFileWriter(final DataWriter outputDataWriter)
  {
    ioDW = outputDataWriter;
    writeEBMLHeader();
    writeSegmentHeader();
    long endOfSegmentHeader = ioDW.getFilePointer();
    metaSeek = new MatroskaFileMetaSeek(endOfSegmentHeader);
    cueData = new MatroskaFileCues(endOfSegmentHeader);
    metaSeek.write(ioDW);
    segmentInfoElem = new MatroskaSegmentInfo(ioDW.getFilePointer());
    metaSeek.addIndexedElement(MatroskaDocTypes.Info.getType(), ioDW.getFilePointer());
    segmentInfoElem.writeElement(ioDW);
    metaSeek.addIndexedElement(MatroskaDocTypes.Tracks.getType(), ioDW.getFilePointer());
    tracks = new MatroskaFileTracks(ioDW.getFilePointer());
    tracks.writeTracks(ioDW);
    cluster = new MatroskaCluster();
    cluster.setLimitParameters(5000, 128 * 1024);
    metaSeek.addIndexedElement(MatroskaDocTypes.Cluster.getType(), ioDW.getFilePointer());
  }

  void writeEBMLHeader()
  {
    final MasterElement ebmlHeaderElem = MatroskaDocTypes.EBML.getInstance();

    final UnsignedIntegerElement ebmlVersionElem = MatroskaDocTypes.EBMLVersion.getInstance();
    ebmlVersionElem.setValue(1);

    final UnsignedIntegerElement ebmlReadVersionElem = MatroskaDocTypes.EBMLReadVersion.getInstance();
    ebmlReadVersionElem.setValue(1);

    final UnsignedIntegerElement ebmlMaxIdLenElem = MatroskaDocTypes.EBMLMaxIDLength.getInstance();
    ebmlMaxIdLenElem.setValue(4);

    final UnsignedIntegerElement ebmlMaxSizeLenElem = MatroskaDocTypes.EBMLMaxSizeLength.getInstance();
    ebmlMaxSizeLenElem.setValue(8);

    final StringElement docTypeElem = MatroskaDocTypes.DocType.getInstance();
    docTypeElem.setValue("matroska");

    final UnsignedIntegerElement docTypeVersionElem = MatroskaDocTypes.DocTypeVersion.getInstance();
    docTypeVersionElem.setValue(3);

    final UnsignedIntegerElement docTypeReadVersionElem = MatroskaDocTypes.DocTypeReadVersion.getInstance();
    docTypeReadVersionElem.setValue(2);

    ebmlHeaderElem.addChildElement(ebmlVersionElem);
    ebmlHeaderElem.addChildElement(ebmlReadVersionElem);
    ebmlHeaderElem.addChildElement(ebmlMaxIdLenElem);
    ebmlHeaderElem.addChildElement(ebmlMaxSizeLenElem);
    ebmlHeaderElem.addChildElement(docTypeElem);
    ebmlHeaderElem.addChildElement(docTypeVersionElem);
    ebmlHeaderElem.addChildElement(docTypeReadVersionElem);
    ebmlHeaderElem.writeElement(ioDW);
  }

  void writeSegmentHeader()
  {
    final MatroskaSegment segmentElem = new MatroskaSegment();
    segmentElem.setUnknownSize(true);
    segmentElem.writeHeaderData(ioDW);
  }

  void writeSegmentInfo()
  {
    segmentInfoElem.update(ioDW);
  }

  void writeTracks()
  {
    tracks.update(ioDW);
  }

  public long getTimecodeScale()
  {
    return segmentInfoElem.getTimecodeScale();
  }

  /**
   * Sets the time scale used in this file. This is the number of nanoseconds represented by the timecode unit in frames. Defaults to 1,000,000.
   * 
   * @param timecodeScale
   */
  public void setTimecodeScale(final long timecodeScale)
  {
    segmentInfoElem.setTimecodeScale(timecodeScale);
  }

  public double getDuration()
  {
    return segmentInfoElem.getDuration();
  }

  /**
   * Sets the duration of the file. Optional.
   * 
   * @param duration
   */
  public void setDuration(final double duration)
  {
    segmentInfoElem.setDuration(duration);
  }

  /**
   * Adds a track to the file. You may add tracks at any time before close()ing, even after adding frames for the track.
   * 
   * @param track
   */
  public void addTrack(final MatroskaFileTrack track)
  {
    tracks.addTrack(track);
  }

  /**
   * Adds the silent track notation for the given track to subsequent clusters, note that this has little effect on most players
   */
  public void silenceTrack(final long trackNumber)
  {
    cluster.silenceTrack(trackNumber);
  }

  /**
   * Removes the silent track notation for this track
   */
  public void unsilenceTrack(final long trackNumber)
  {
    cluster.unsilenceTrack(trackNumber);
  }

  /**
   * Add a frame
   * 
   * @param frame The frame to add
   */
  public void addFrame(final MatroskaFileFrame frame)
  {
    if (!cluster.addFrame(frame))
    {
      flush();
    }
  }

  /**
   * Flushes pending content to disk and starts a new cluster. This is typically not necessary to call manually. 
   */
  public void flush()
  {
    final long clusterPos = ioDW.getFilePointer();
    cueData.addCue(clusterPos, cluster.getClusterTimecode(), cluster.getTracks());
    LOG.debug("Cluster flushing, timecode {}", cluster.getClusterTimecode());
    cluster.flush(ioDW);
  }

  /**
   * Finalizes the file by writing the final headers, index, and flushing data to the writer.
   */
  public void close()
  {
    flush();

    cueData.write(ioDW, metaSeek);
    metaSeek.update(ioDW);
    segmentInfoElem.update(ioDW);
    tracks.update(ioDW);
  }
}
