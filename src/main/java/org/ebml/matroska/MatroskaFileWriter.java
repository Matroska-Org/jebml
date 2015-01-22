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
import java.util.Date;

import org.ebml.DateElement;
import org.ebml.FloatElement;
import org.ebml.MasterElement;
import org.ebml.StringElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataWriter;

/**
 * Summary description for MatroskaFileWriter.
 */
public class MatroskaFileWriter
{
  protected DataWriter ioDW;
  protected MatroskaDocType doc = new MatroskaDocType();

  private long timecodeScale = 1000000;
  private Double duration;
  private final Date segmentDate = new Date();
  private final ArrayList<MatroskaFileTrack> trackList = new ArrayList<MatroskaFileTrack>();
  private final MatroskaFileMetaSeek metaSeek;
  private final MatroskaFileCues cueData;
  private final MatroskaCluster cluster;
  private boolean inited = false;

  public MatroskaFileWriter(final DataWriter outputDataWriter)
  {
    ioDW = outputDataWriter;
    writeEBMLHeader();
    writeSegmentHeader();
    metaSeek = new MatroskaFileMetaSeek(doc, ioDW.getFilePointer());
    cueData = new MatroskaFileCues(doc, ioDW.getFilePointer());
    metaSeek.write(ioDW);
    cluster = (MatroskaCluster) doc.createElement(MatroskaDocType.Cluster_Id);
  }

  void writeEBMLHeader()
  {
    final MasterElement ebmlHeaderElem = (MasterElement) doc.createElement(MatroskaDocType.EBMLHeader_Id);

    final UnsignedIntegerElement ebmlVersionElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.EBMLVersion_Id);
    ebmlVersionElem.setValue(1);

    final UnsignedIntegerElement ebmlReadVersionElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.EBMLReadVersion_Id);
    ebmlReadVersionElem.setValue(1);

    final UnsignedIntegerElement ebmlMaxIdLenElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.EBMLMaxIDLength_Id);
    ebmlMaxIdLenElem.setValue(4);

    final UnsignedIntegerElement ebmlMaxSizeLenElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.EBMLMaxSizeLength_Id);
    ebmlMaxSizeLenElem.setValue(8);

    final StringElement docTypeElem = (StringElement) doc.createElement(MatroskaDocType.DocType_Id);
    docTypeElem.setValue("matroska");

    final UnsignedIntegerElement docTypeVersionElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.DocTypeVersion_Id);
    docTypeVersionElem.setValue(1);

    final UnsignedIntegerElement docTypeReadVersionElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.DocTypeReadVersion_Id);
    docTypeReadVersionElem.setValue(1);

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
    final MatroskaSegment segmentElem = (MatroskaSegment) doc.createElement(MatroskaDocType.Segment_Id);
    segmentElem.setUnknownSize(true);
    segmentElem.writeHeaderData(ioDW);
  }

  void writeSegmentInfo()
  {
    final MasterElement segmentInfoElem = (MasterElement) doc.createElement(MatroskaDocType.SegmentInfo_Id);

    final StringElement writingAppElem = (StringElement) doc.createElement(MatroskaDocType.WritingApp_Id);
    writingAppElem.setValue("Matroska File Writer v1.0");

    final StringElement muxingAppElem = (StringElement) doc.createElement(MatroskaDocType.MuxingApp_Id);
    muxingAppElem.setValue("JEBML v1.0");

    final DateElement dateElem = (DateElement) doc.createElement(MatroskaDocType.DateUTC_Id);
    dateElem.setDate(segmentDate);

    // Add timecode scale
    final UnsignedIntegerElement timecodescaleElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TimecodeScale_Id);
    timecodescaleElem.setValue(timecodeScale);

    segmentInfoElem.addChildElement(dateElem);
    segmentInfoElem.addChildElement(timecodescaleElem);

    if (duration != null)
    {
      final FloatElement durationElem = (FloatElement) doc.createElement(MatroskaDocType.Duration_Id);
      durationElem.setValue(duration * 1000.0);
      segmentInfoElem.addChildElement(durationElem);
    }

    segmentInfoElem.addChildElement(writingAppElem);
    segmentInfoElem.addChildElement(muxingAppElem);

    metaSeek.addIndexedElement(segmentInfoElem, ioDW.getFilePointer());
    segmentInfoElem.writeElement(ioDW);
  }

  void writeTracks()
  {
    final long pos = MatroskaFileTrack.writeTracks(trackList, ioDW);
    metaSeek.addIndexedElement(MatroskaDocType.Tracks_Id, pos);
    metaSeek.update(ioDW);
  }

  public long getTimecodeScale()
  {
    return timecodeScale;
  }

  /**
   * Sets the time scale used in this file. This is the number of nanoseconds represented by the timecode unit in frames. Defaults to 1,000,000. Must
   * be set before init() is called.
   * 
   * @param timecodeScale
   */
  public void setTimecodeScale(final long timecodeScale)
  {
    assert !inited;
    this.timecodeScale = timecodeScale;
  }

  public double getDuration()
  {
    return duration;
  }

  /**
   * Sets the duration of the file. Note that this may only be set with any effect prior to the init() method being called. Optional.
   * 
   * @param duration
   */
  public void setDuration(final double duration)
  {
    assert !inited;
    this.duration = duration;
  }

  /**
   * Adds a track to the file. Note that this is required for every track to be included, and must be done prior to the init() method being called.
   * 
   * @param track
   */
  public void addTrack(final MatroskaFileTrack track)
  {
    assert !inited;
    trackList.add(track);
  }

  /**
   * Sets up the inital file-start headers. Must be called prior to adding any frames and after all tracks have been added.
   *
   */
  public void init()
  {
    assert !inited;
    inited = true;
    writeSegmentInfo();
    writeTracks();
    cluster.setLimitParameters(5000, 1024 * 1024);
    metaSeek.addIndexedElement(cluster, ioDW.getFilePointer());
  }

  /**
   * Add a frame
   * 
   * @param frame The frame to add
   */
  public void addFrame(final MatroskaFileFrame frame)
  {
    assert inited;
    if (!cluster.AddFrame(frame))
    {
      final long clusterPos = ioDW.getFilePointer();
      cueData.addCue(clusterPos, cluster.getClusterTimecode(), cluster.getTracks());
      cluster.flush(ioDW);
      System.out.println("Cluster-flush!");
    }
  }

  /**
   * Finalizes the file by writing the final headers, index, and last few frames.
   *
   */
  public void close()
  {
    assert inited;
    final long clusterPos = ioDW.getFilePointer();
    cueData.addCue(clusterPos, cluster.getClusterTimecode(), cluster.getTracks());
    cluster.flush(ioDW);

    // final Element cues = cueData.toElement();
    // metaSeek.addIndexedElement(cues, ioDW.getFilePointer());
    // cues.writeElement(ioDW);
    metaSeek.update(ioDW);
  }
}
