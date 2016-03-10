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
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.ebml.DateElement;
import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.FloatElement;
import org.ebml.MasterElement;
import org.ebml.SignedIntegerElement;
import org.ebml.StringElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatroskaFile
{
  /**
   * Number of Clusters to search before assuming that a track has ended
   */
  public static final int CLUSTER_TRACK_SEARCH_COUNT = 4;
  protected static final Logger LOG = LoggerFactory.getLogger(MatroskaFile.class);

  static
  {
    // References DocTypes to force static init of its memebers
    MatroskaDocTypes.Void.getLevel();
  }

  private final DataSource ioDS;
  private final EBMLReader reader;
  private Element level0 = null;
  private String segmentTitle;
  private Date segmentDate;
  private String muxingApp;
  private String writingApp;
  private long timecodeScale = 1000000;
  private double duration;
  private final ArrayList<MatroskaFileTrack> trackList = new ArrayList<>();
  private final ArrayList<MatroskaFileTagEntry> tagList = new ArrayList<>();
  private final Queue<MatroskaFileFrame> frameQueue = new ConcurrentLinkedQueue<>();
  private boolean scanFirstCluster = true;

  /**
   * Primary Constructor for Matroska File class.
   *
   * @param inputDataSource DataSource to read the Matroska file from
   */
  public MatroskaFile(final DataSource inputDataSource)
  {
    ioDS = inputDataSource;
    reader = new EBMLReader(ioDS);
  }

  /**
   * Read / Parse the Matroska file. Call this before any other method.
   * 
   * @throws RuntimeException On various errors
   */
  public void readFile()
  {
    Element level1 = null;
    final Element level2 = null;
    // Element level3 = null;
    // Element level4 = null;

    level0 = reader.readNextElement();
    if (level0 == null)
    {
      throw new java.lang.RuntimeException("Error: Unable to scan for EBML elements");
    }

    if (level0.isType(MatroskaDocTypes.EBML.getType()))
    {
      level1 = ((MasterElement) level0).readNextChild(reader);

      while (level1 != null)
      {
        // System.out.printf("Found element %s\n", level1.getElementType().name);
        level1.readData(ioDS);
        if (level1.isType(MatroskaDocTypes.DocType.getType()))
        {
          final String docType = ((StringElement) level1).getValue();
          if (docType.compareTo("matroska") != 0 && docType.compareTo("webm") != 0)
          {
            throw new java.lang.RuntimeException("Error: DocType is not matroska, \"" + ((StringElement) level1).getValue() + "\"");
          }
        }
        level1 = ((MasterElement) level0).readNextChild(reader);
      }
    }
    else
    {
      throw new java.lang.RuntimeException("Error: EBML Header not the first element in the file");
    }

    level0 = reader.readNextElement();
    if (level0.isType(MatroskaDocTypes.Segment.getType()))
    {
      level1 = ((MasterElement) level0).readNextChild(reader);
      LOG.debug("Got segment element");
      while (level1 != null)
      {
        LOG.debug("Got {} element in segment", level1.getElementType().getName());
        if (level1.isType(MatroskaDocTypes.Info.getType()))
        {
          parseSegmentInfo(level1, level2);

        }
        else if (level1.isType(MatroskaDocTypes.Tracks.getType()))
        {
          parseTracks(level1, level2);

        }
        else if (level1.isType(MatroskaDocTypes.Cluster.getType()))
        {
          if (scanFirstCluster)
          {
            parseNextCluster(level1);
          }
          // Break out of this loop, we should only parse the first cluster
          break;

        }
        else if (level1.isType(MatroskaDocTypes.Tags.getType()))
        {
          parseTags(level1, level2);

        }

        level1.skipData(ioDS);
        level1 = ((MasterElement) level0).readNextChild(reader);
      }
    }
    else
    {
      throw new java.lang.RuntimeException(String.format("Error: Segment not the second element in the file: was %s instead",
                                                         level0.getElementType().getName()));
    }
  }

  /**
   * Get the Next MatroskaFileFrame
   *
   * @return The next MatroskaFileFrame in the queue, or null if the file has ended
   */
  public MatroskaFileFrame getNextFrame()
  {
    if (frameQueue.isEmpty())
    {
      fillFrameQueue();
    }

    // If FrameQueue is still empty, must be the end of the file
    if (frameQueue.isEmpty())
    {
      return null;
    }
    return frameQueue.remove();
  }

  /**
   * Get the Next MatroskaFileFrame, limited by TrackNo
   *
   * @param trackNo The track number to only get MatroskaFileFrame(s) from
   * @return The next MatroskaFileFrame in the queue, or null if there are no more frames for the TrackNo track
   */
  public MatroskaFileFrame getNextFrame(final int trackNo)
  {
    if (frameQueue.isEmpty())
    {
      fillFrameQueue();
    }

    // If FrameQueue is still empty, must be the end of the file
    if (frameQueue.isEmpty())
    {
      return null;
    }
    int tryCount = 0;
    MatroskaFileFrame frame = null;
    try
    {
      final Iterator<MatroskaFileFrame> iter = frameQueue.iterator();
      while (frame == null)
      {
        if (iter.hasNext())
        {
          frame = iter.next();
          if (frame.getTrackNo() == trackNo)
          {
            synchronized (frameQueue)
            {
              iter.remove();
            }
            return frame;
          }
          frame = null;
        }
        else
        {
          fillFrameQueue();
          if (++tryCount > CLUSTER_TRACK_SEARCH_COUNT)
          {
            // If we have not found any frames belonging to a track in 4 clusters
            // there is a good chance that the track is over
            return null;
          }
        }
      }
    }
    catch (final RuntimeException ex)
    {
      ex.printStackTrace();
      return null;
    }

    return frame;
  }

  public boolean isSeekable()
  {
    return this.ioDS.isSeekable();
  }

  /**
   * Seek to the requested timecode, rescaning clusters and/or discarding frames until we reach the nearest possible timecode, rounded down.
   *
   * <p>
   * For example<br>
   * Say we have a file with 10 frames
   * <table>
   * <tr>
   * <th>Frame No</th>
   * <th>Timecode</th>
   * </tr>
   * <tr>
   * <td>Frame 1</td>
   * <td>0ms</td>
   * </tr>
   * <tr>
   * <td>Frame 2</td>
   * <td>50ms</td>
   * </tr>
   * <tr>
   * <td>Frame 3</td>
   * <td>100ms</td>
   * </tr>
   * <tr>
   * <td>Frame 4</td>
   * <td>150ms</td>
   * </tr>
   * <tr>
   * <td>Frame 5</td>
   * <td>200ms</td>
   * </tr>
   * <tr>
   * <td>Frame 6</td>
   * <td>250ms</td>
   * </tr>
   * <tr>
   * <td>Frame 7</td>
   * <td>300ms</td>
   * </tr>
   * <tr>
   * <td>Frame 8</td>
   * <td>350ms</td>
   * </tr>
   * <tr>
   * <td>Frame 9</td>
   * <td>400ms</td>
   * </tr>
   * <tr>
   * <td>Frame 10</td>
   * <td>450ms</td>
   * </tr>
   * </table>
   * We are requested to seek to 333ms, so we discard frames until we hit an timecode larger than the requested. We would seek to Frame 7 at 300ms.
   * </p>
   *
   * @param timecode Timecode to seek to in millseconds
   * @return Actual timecode we seeked to
   */
  public long seek(final long timecode)
  {
    return 0;
  }

  private void fillFrameQueue()
  {
    if (level0 == null)
    {
      throw new java.lang.IllegalStateException("Call readFile() before reading frames");
    }

    synchronized (level0)
    {
      Element level1 = ((MasterElement) level0).readNextChild(reader);
      while (level1 != null)
      {
        if (level1.isType(MatroskaDocTypes.Cluster.getType()))
        {
          parseNextCluster(level1);
        }

        level1.skipData(ioDS);
        level1 = ((MasterElement) level0).readNextChild(reader);
      }
    }
  }

  private void parseNextCluster(final Element level1)
  {
    Element level2 = null;
    Element level3 = null;
    long clusterTimecode = 0;
    level2 = ((MasterElement) level1).readNextChild(reader);

    while (level2 != null)
    {
      if (level2.isType(MatroskaDocTypes.Timecode.getType()))
      {
        level2.readData(ioDS);
        clusterTimecode = ((UnsignedIntegerElement) level2).getValue();

      }
      else if (level2.isType(MatroskaDocTypes.SimpleBlock.getType()))
      {
        level2.readData(ioDS);
        MatroskaBlock block = null;
        final long blockDuration = 0;
        block = new MatroskaBlock(level2.getData());

        block.parseBlock();
        final MatroskaFileFrame frame = new MatroskaFileFrame();
        frame.setTrackNo(block.getTrackNo());
        frame.setTimecode(block.getAdjustedBlockTimecode(clusterTimecode, this.timecodeScale));
        frame.setDuration(blockDuration);
        frame.setData(block.getFrame(0));
        frame.setKeyFrame(block.isKeyFrame());
        synchronized (frameQueue)
        {
          frameQueue.add(new MatroskaFileFrame(frame));
        }

        if (block.getFrameCount() > 1)
        {
          for (int f = 1; f < block.getFrameCount(); f++)
          {
            frame.setData(block.getFrame(f));
            frameQueue.add(new MatroskaFileFrame(frame));
          }
        }
        level2.skipData(ioDS);

      }
      else if (level2.isType(MatroskaDocTypes.BlockGroup.getType()))
      {
        long blockDuration = 0;
        long blockReference = 0;
        level3 = ((MasterElement) level2).readNextChild(reader);
        MatroskaBlock block = null;
        while (level3 != null)
        {
          if (level3.isType(MatroskaDocTypes.Block.getType()))
          {
            level3.readData(ioDS);
            block = new MatroskaBlock(level3.getData());
            block.parseBlock();

          }
          else if (level3.isType(MatroskaDocTypes.BlockDuration.getType()))
          {
            level3.readData(ioDS);
            blockDuration = ((UnsignedIntegerElement) level3).getValue();

          }
          else if (level3.isType(MatroskaDocTypes.ReferenceBlock.getType()))
          {
            level3.readData(ioDS);
            blockReference = ((SignedIntegerElement) level3).getValue();
          }

          level3.skipData(ioDS);
          level3 = ((MasterElement) level2).readNextChild(reader);
        }

        if (block == null)
        {
          throw new java.lang.NullPointerException("BlockGroup element with no child Block!");
        }

        final MatroskaFileFrame frame = new MatroskaFileFrame();
        frame.setTrackNo(block.getTrackNo());
        frame.setTimecode(block.getAdjustedBlockTimecode(clusterTimecode, this.timecodeScale));
        frame.setDuration(blockDuration);
        frame.addReferences(blockReference);
        frame.setData(block.getFrame(0));
        frameQueue.add(new MatroskaFileFrame(frame));

        if (block.getFrameCount() > 1)
        {
          for (int f = 1; f < block.getFrameCount(); f++)
          {
            frame.setData(block.getFrame(f));
            /*
             * if (badMP3Headers()) { throw new RuntimeException("Bad Data!"); }
             */

            frameQueue.add(new MatroskaFileFrame(frame));
            /*
             * if (badMP3Headers()) { throw new RuntimeException("Bad Data!"); }
             */
          }
        }
      }

      level2.skipData(ioDS);
      level2 = ((MasterElement) level1).readNextChild(reader);
    }
  }

  protected boolean badMP3Headers()
  {
    final Iterator<MatroskaFileFrame> iter = frameQueue.iterator();
    while (iter.hasNext())
    {
      final MatroskaFileFrame frame = iter.next();
      if (frame.getTrackNo() == 2
          && frame.getData().get(3) != 0x54)
      {
        throw new RuntimeException("Bad MP3 Header! Index: " + iter);
      }
    }
    return false;
  }

  private void parseSegmentInfo(final Element level1, Element level2)
  {
    level2 = ((MasterElement) level1).readNextChild(reader);

    while (level2 != null)
    {
      if (level2.isType(MatroskaDocTypes.Title.getType()))
      {
        level2.readData(ioDS);
        segmentTitle = ((StringElement) level2).getValue();

      }
      else if (level2.isType(MatroskaDocTypes.DateUTC.getType()))
      {
        level2.readData(ioDS);
        segmentDate = ((DateElement) level2).getDate();

      }
      else if (level2.isType(MatroskaDocTypes.MuxingApp.getType()))
      {
        level2.readData(ioDS);
        muxingApp = ((StringElement) level2).getValue();

      }
      else if (level2.isType(MatroskaDocTypes.WritingApp.getType()))
      {
        level2.readData(ioDS);
        writingApp = ((StringElement) level2).getValue();

      }
      else if (level2.isType(MatroskaDocTypes.Duration.getType()))
      {
        level2.readData(ioDS);
        duration = ((FloatElement) level2).getValue();

      }
      else if (level2.isType(MatroskaDocTypes.TimecodeScale.getType()))
      {
        level2.readData(ioDS);

        timecodeScale = ((UnsignedIntegerElement) level2).getValue();
      }

      level2.skipData(ioDS);
      level2 = ((MasterElement) level1).readNextChild(reader);
    }
  }

  private void parseTracks(final Element level1, Element level2)
  {
    level2 = ((MasterElement) level1).readNextChild(reader);

    while (level2 != null)
    {
      if (level2.isType(MatroskaDocTypes.TrackEntry.getType()))
      {
        trackList.add(MatroskaFileTrack.fromElement(level2, ioDS, reader));
      }
      level2.skipData(ioDS);
      level2 = ((MasterElement) level1).readNextChild(reader);
    }
  }

  private void parseTags(final Element level1, Element level2)
  {
    Element level3 = null;
    Element level4 = null;
    level2 = ((MasterElement) level1).readNextChild(reader);

    while (level2 != null)
    {
      if (level2.isType(MatroskaDocTypes.Tag.getType()))
      {
        final MatroskaFileTagEntry tag = new MatroskaFileTagEntry();
        level3 = ((MasterElement) level2).readNextChild(reader);

        while (level3 != null)
        {
          if (level3.isType(MatroskaDocTypes.Targets.getType()))
          {
            level4 = ((MasterElement) level3).readNextChild(reader);

            while (level4 != null)
            {
              if (level4.isType(MatroskaDocTypes.TagTrackUID.getType()))
              {
                level4.readData(ioDS);
                tag.trackUID.add(new Long(((UnsignedIntegerElement) level4).getValue()));

              }
              else if (level4.isType(MatroskaDocTypes.TagChapterUID.getType()))
              {
                level4.readData(ioDS);
                tag.chapterUID.add(new Long(((UnsignedIntegerElement) level4).getValue()));

              }
              else if (level4.isType(MatroskaDocTypes.TagAttachmentUID.getType()))
              {
                level4.readData(ioDS);
                tag.attachmentUID.add(new Long(((UnsignedIntegerElement) level4).getValue()));
              }

              level4.skipData(ioDS);
              level4 = ((MasterElement) level3).readNextChild(reader);
            }

          }
          else if (level3.isType(MatroskaDocTypes.SimpleTag.getType()))
          {
            tag.simpleTags.add(parseTagsSimpleTag(level3, level4));
          }
          level3.skipData(ioDS);
          level3 = ((MasterElement) level2).readNextChild(reader);
        }
        tagList.add(tag);
      }

      level2.skipData(ioDS);
      level2 = ((MasterElement) level1).readNextChild(reader);
    }
  }

  private MatroskaFileSimpleTag parseTagsSimpleTag(final Element level3, Element level4)
  {
    final MatroskaFileSimpleTag simpleTag = new MatroskaFileSimpleTag();
    level4 = ((MasterElement) level3).readNextChild(reader);

    while (level4 != null)
    {
      if (level4.isType(MatroskaDocTypes.TagName.getType()))
      {
        level4.readData(ioDS);
        simpleTag.name = ((StringElement) level4).getValue();

      }
      else if (level4.isType(MatroskaDocTypes.TagString.getType()))
      {
        level4.readData(ioDS);
        simpleTag.value = ((StringElement) level4).getValue();

      }

      level4.skipData(ioDS);
      level4 = ((MasterElement) level3).readNextChild(reader);
    }

    return simpleTag;
  }

  /**
   * Get a String report for the Matroska file. Call readFile() before this method, else the report will be empty.
   *
   * @return String Report
   */
  public String getReport()
  {
    final java.io.StringWriter s = new java.io.StringWriter();
    int t;

    s.write("MatroskaFile report\n");

    s.write("Infomation Segment \n");
    s.write("\tSegment Title: " + segmentTitle + "\n");
    s.write("\tSegment Date: " + segmentDate + "\n");
    s.write("\tMuxing App : " + muxingApp + "\n");
    s.write("\tWriting App : " + writingApp + "\n");
    s.write("\tDuration : " + duration / 1000 + "sec \n");
    s.write("\tTimecodeScale : " + timecodeScale + "\n");

    s.write("Track Count: " + trackList.size() + "\n");
    for (t = 0; t < trackList.size(); t++)
    {
      s.write("\tTrack " + t + "\n");
      s.write(trackList.get(t).toString());
    }

    s.write("Tag Count: " + tagList.size() + "\n");
    for (t = 0; t < tagList.size(); t++)
    {
      s.write("\tTag Entry \n");
      s.write(tagList.get(t).toString());
    }

    s.write("End report\n");

    return s.getBuffer().toString();
  }

  public String getWritingApp()
  {
    return writingApp;
  }

  /**
   * Returns an array of the tracks. If there are no MatroskaFileTracks to return the returned array will have a size of 0.
   *
   * @return Array of MatroskaFileTrack's
   */
  public MatroskaFileTrack[] getTrackList()
  {
    if (trackList.size() > 0)
    {
      final MatroskaFileTrack[] tracks = new MatroskaFileTrack[trackList.size()];
      for (int t = 0; t < trackList.size(); t++)
      {
        tracks[t] = trackList.get(t);
      }
      return tracks;
    }
    else
    {
      return new MatroskaFileTrack[0];
    }
  }

  /**
   * <p>
   * This differs from the getTrackList method in that this method scans each track and returns the one that has the same track number as TrackNo.
   * </p>
   *
   * <p>
   * Note: TrackNo != track index
   * </p>
   *
   * @param trackNo The actual track number of the MatroskaFileTrack you would like to get
   * @return null if no MatroskaFileTrack is found with the requested TrackNo
   */
  public MatroskaFileTrack getTrack(final int trackNo)
  {
    for (int t = 0; t < trackList.size(); t++)
    {
      final MatroskaFileTrack track = trackList.get(t);
      if (track.getTrackNo() == trackNo)
      {
        return track;
      }
    }
    return null;
  }

  /**
   * Get the timecode scale for this MatroskaFile. In Matroska the timecodes are stored scaled by this value. However any MatroskaFileFrame you get
   * through the methods of this class will already have the timecodes correctly scaled to millseconds.
   *
   * @return TimecodeScale
   */
  public long getTimecodeScale()
  {
    return timecodeScale;
  }

  public String getSegmentTitle()
  {
    return segmentTitle;
  }

  public String getMuxingApp()
  {
    return muxingApp;
  }

  /**
   * Get the duration for this MatroskaFile. This is the duration value stored in the segment info. Which may or may not be the exact length of all,
   * some, or one of the tracks.
   *
   * @return Duration in seconds
   */
  public double getDuration()
  {
    return duration;
  }

  /**
   * Sets if the readFile() method should scan the first cluster for infomation. Set to false for faster parsing.
   */
  public void setScanFirstCluster(final boolean scanFirstCluster)
  {
    this.scanFirstCluster = scanFirstCluster;
  }

  /**
   * Gets if the readFile() method should scan the first cluster for infomation. When set to false parsing is slightly faster.
   */
  public boolean getScanFirstCluster()
  {
    return scanFirstCluster;
  }
}
