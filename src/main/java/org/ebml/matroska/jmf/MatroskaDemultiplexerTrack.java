package org.ebml.matroska.jmf;

import org.ebml.matroska.*;
import javax.media.Track;
import javax.media.Format;
import javax.media.Time;
import javax.media.Buffer;
import javax.media.TrackListener;
import javax.media.format.*;

/**
 * <p>Title: JEBML</p>
 * <p>Description: Java Classes to Read EBML Elements</p>
 * <p>Copyright: Copyright (c) 2002-2004 John Cannon <spyder@matroska.org>, Jory Stone <jcsston@toughguy.net></p>
 * <p>Company: </p>
 * @author jcsston
 * @version 1.0
 */

public class MatroskaDemultiplexerTrack implements Track {
  protected MatroskaFile file = null;
  protected int TrackNo = -1;
  protected boolean Valid = false;
  protected Format format = null;
  protected boolean Enabled = false;
  protected long StartTimecode = -1;

  public MatroskaDemultiplexerTrack(MatroskaFile file, int TrackNo) {
    this.file = file;
    this.TrackNo = TrackNo;
    init();
  }

  public void init() {
    MatroskaFileTrack track = file.getTrack(TrackNo);
    if (track.CodecID.compareTo("V_MS/VFW/FOURCC") == 0) {

    } else if (track.CodecID.compareTo("A_MPEG/L3") == 0) {
      if (track.Audio_BitDepth == 0)
        track.Audio_BitDepth = 16;
      format = new AudioFormat(AudioFormat.MPEGLAYER3, track.Audio_SamplingFrequency, track.Audio_BitDepth, track.Audio_Channels);

      setEnabled(true);
    } else if (track.CodecID.compareTo("A_MPEG/L2") == 0) {
      if (track.Audio_BitDepth == 0)
        track.Audio_BitDepth = 16;
      format = new AudioFormat("mpegaudio", track.Audio_SamplingFrequency, track.Audio_BitDepth, track.Audio_Channels);

      setEnabled(true);
    }
  }

  public Format getFormat() {
    return format;
  }
  public void setEnabled(boolean Enabled) {
    this.Enabled = Enabled;
  }
  public boolean isEnabled() {
    return Enabled;
  }
  public Time getStartTime() {
   return new Time(StartTimecode);
  }
  public void readFrame(Buffer buffer) {
    //System.out.println("MatroskaDemultiplexerTrack.readFrame(buffer = " + buffer + ")");
    MatroskaFileFrame frame = file.getNextFrame(TrackNo);
    if (frame == null) {
      buffer.setDiscard(true);
      buffer.setEOM(true);
      return;
    }
    if (StartTimecode == -1) {
      StartTimecode = frame.Timecode;
    }
    buffer.setTimeStamp(frame.Timecode * 1000000);
    if (frame.Duration != 0) {
      buffer.setDuration(frame.Duration * 1000000);
    } else {
      buffer.setDuration(Buffer.TIME_UNKNOWN);
    }
    buffer.getData();
    buffer.setData(frame.Data);
    buffer.setLength(frame.Data.length);
  }
  public int mapTimeToFrame(Time time) {
    return MatroskaDemultiplexerTrack.FRAME_UNKNOWN;
  }
  public Time mapFrameToTime(int frame) {
    return MatroskaDemultiplexerTrack.TIME_UNKNOWN;
  }
  public void setTrackListener(TrackListener listen) {
    /**@todo Implement this javax.media.Track method*/
    //throw new java.lang.UnsupportedOperationException("Method setTrackListener() not yet implemented.");
  }
  public Time getDuration() {
    return new Time(file.getDuration());
  }
}
