package org.ebml.matroska.jmf;

import org.ebml.matroska.*;
import javax.media.Track;
import javax.media.Format;
import javax.media.Time;
import javax.media.Buffer;
import javax.media.TrackListener;
import javax.media.format.*;

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
  protected int TrackNo = -1;
  protected boolean Valid = false;
  protected Format format = null;
  protected boolean Enabled = false;
  protected long StartTimecode = -1;

  public MatroskaDemultiplexerTrack(final MatroskaFile file, final int TrackNo)
  {
    this.file = file;
    this.TrackNo = TrackNo;
    init();
  }

  public void init()
  {
    final MatroskaFileTrack track = file.getTrack(TrackNo);
    if (track.CodecID.compareTo("V_MS/VFW/FOURCC") == 0)
    {

    }
    else if (track.CodecID.compareTo("A_MPEG/L3") == 0)
    {
      if (track.audio.BitDepth == 0)
        track.audio.BitDepth = 16;
      format = new AudioFormat(AudioFormat.MPEGLAYER3, track.audio.SamplingFrequency, track.audio.BitDepth, track.audio.Channels);

      setEnabled(true);
    }
    else if (track.CodecID.compareTo("A_MPEG/L2") == 0)
    {
      if (track.audio.BitDepth == 0)
        track.audio.BitDepth = 16;
      format = new AudioFormat("mpegaudio", track.audio.SamplingFrequency, track.audio.BitDepth, track.audio.Channels);

      setEnabled(true);
    }
  }

  @Override
  public Format getFormat()
  {
    return format;
  }

  @Override
  public void setEnabled(final boolean Enabled)
  {
    this.Enabled = Enabled;
  }

  @Override
  public boolean isEnabled()
  {
    return Enabled;
  }

  @Override
  public Time getStartTime()
  {
    return new Time(StartTimecode);
  }

  @Override
  public void readFrame(final Buffer buffer)
  {
    // System.out.println("MatroskaDemultiplexerTrack.readFrame(buffer = " + buffer + ")");
    final MatroskaFileFrame frame = file.getNextFrame(TrackNo);
    if (frame == null)
    {
      buffer.setDiscard(true);
      buffer.setEOM(true);
      return;
    }
    if (StartTimecode == -1)
    {
      StartTimecode = frame.getTimecode();
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
