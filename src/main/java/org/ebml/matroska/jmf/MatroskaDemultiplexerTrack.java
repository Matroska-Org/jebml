package org.ebml.matroska.jmf;

// CSOFF: AvoidStarImport
import org.ebml.matroska.*;
import javax.media.Track;
import javax.media.Format;
import javax.media.Time;
import javax.media.Buffer;
import javax.media.TrackListener;
import javax.media.format.*;

//CSON: AvoidStarImport

/**
 * <p>
 * Title: JEBML
 * </p>
 * <p>
 * Description: Java Classes to Read EBML Elements
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002-2004 John Cannon <spyder@matroska.org>, Jory Stone <jcsston@toughguy.net>
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author jcsston
 * @version 1.0
 */

public class MatroskaDemultiplexerTrack implements Track
{
  protected MatroskaFile file = null;
  protected int trackNo = -1;
  protected boolean valid = false;
  protected Format format = null;
  protected boolean enabled = false;
  protected long startTimecode = -1;

  public MatroskaDemultiplexerTrack(final MatroskaFile file, final int trackNo)
  {
    this.file = file;
    this.trackNo = trackNo;
    init();
  }

  public void init()
  {
    final MatroskaFileTrack track = file.getTrack(trackNo);
    if (track.getCodecID().compareTo("V_MS/VFW/FOURCC") == 0)
    {
      System.out.println("Got fourcc");
    }
    else if (track.getCodecID().compareTo("A_MPEG/L3") == 0)
    {
      if (track.getAudio().getBitDepth() == 0)
      {
        track.getAudio().setBitDepth(16);
      }
      format = new AudioFormat(AudioFormat.MPEGLAYER3, track.getAudio().getSamplingFrequency(), track.getAudio().getBitDepth(), track.getAudio().getChannels());

      setEnabled(true);
    }
    else if (track.getCodecID().compareTo("A_MPEG/L2") == 0)
    {
      if (track.getAudio().getBitDepth() == 0)
      {
        track.getAudio().setBitDepth(16);
      }
      format = new AudioFormat("mpegaudio", track.getAudio().getSamplingFrequency(), track.getAudio().getBitDepth(), track.getAudio().getChannels());

      setEnabled(true);
    }
  }

  @Override
  public Format getFormat()
  {
    return format;
  }

  @Override
  public void setEnabled(final boolean enabled)
  {
    this.enabled = enabled;
  }

  @Override
  public boolean isEnabled()
  {
    return enabled;
  }

  @Override
  public Time getStartTime()
  {
    return new Time(startTimecode);
  }

  @Override
  public void readFrame(final Buffer buffer)
  {
    // System.out.println("MatroskaDemultiplexerTrack.readFrame(buffer = " + buffer + ")");
    final MatroskaFileFrame frame = file.getNextFrame(trackNo);
    if (frame == null)
    {
      buffer.setDiscard(true);
      buffer.setEOM(true);
      return;
    }
    if (startTimecode == -1)
    {
      startTimecode = frame.getTimecode();
    }
    buffer.setTimeStamp(frame.getTimecode() * 1000000);
    if (frame.getDuration() != 0)
    {
      buffer.setDuration(frame.getDuration() * 1000000);
    }
    else
    {
      buffer.setDuration(Buffer.TIME_UNKNOWN);
    }
    buffer.getData();
    buffer.setData(frame.getData());
    buffer.setLength(frame.getData().length);
  }

  @Override
  public int mapTimeToFrame(final Time time)
  {
    return MatroskaDemultiplexerTrack.FRAME_UNKNOWN;
  }

  @Override
  public Time mapFrameToTime(final int frame)
  {
    return MatroskaDemultiplexerTrack.TIME_UNKNOWN;
  }

  @Override
  public void setTrackListener(final TrackListener listen)
  {
    /** @todo Implement this javax.media.Track method */
    // throw new java.lang.UnsupportedOperationException("Method setTrackListener() not yet implemented.");
  }

  @Override
  public Time getDuration()
  {
    return new Time(file.getDuration());
  }
}
