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

import org.ebml.BinaryElement;
import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.FloatElement;
import org.ebml.MasterElement;
import org.ebml.StringElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataSource;
import org.ebml.io.DataWriter;

/**
 * Matroska Track Class
 */
public class MatroskaFileTrack
{
  private int trackNo = 1;
  private long trackUID = 1337;
  private byte trackType;
  private boolean flagEnabled = true;
  private boolean flagDefault = true;
  private boolean flagForced = false;
  private boolean flagLacing = true;
  private int minCache = 0;
  private int maxBlockAdditionalId = 0;
  private String name = "unnamed";
  private String language = "eng";
  private String codecID;
  private byte[] codecPrivate;
  private long defaultDuration;
  private boolean codecDecodeAll = true;
  private int seekPreroll = 0;

  public static class MatroskaVideoTrack
  {
    private short pixelWidth;
    private short pixelHeight;
    private short displayWidth = 0;
    private short displayHeight = 0;

    public short getPixelWidth()
    {
      return pixelWidth;
    }

    public void setPixelWidth(final short pixelWidth)
    {
      this.pixelWidth = pixelWidth;
    }

    public short getPixelHeight()
    {
      return pixelHeight;
    }

    public void setPixelHeight(final short pixelHeight)
    {
      this.pixelHeight = pixelHeight;
    }

    public short getDisplayWidth()
    {
      return displayWidth;
    }

    public void setDisplayWidth(final short displayWidth)
    {
      this.displayWidth = displayWidth;
    }

    public short getDisplayHeight()
    {
      return displayHeight;
    }

    public void setDisplayHeight(final short displayHeight)
    {
      this.displayHeight = displayHeight;
    }
  }

  private MatroskaVideoTrack video = null;

  public static class MatroskaAudioTrack
  {
    private float samplingFrequency;
    private float outputSamplingFrequency;
    public short channels;
    public byte bitDepth;

    public float getSamplingFrequency()
    {
      return samplingFrequency;
    }

    public void setSamplingFrequency(final float samplingFrequency)
    {
      this.samplingFrequency = samplingFrequency;
    }

    public float getOutputSamplingFrequency()
    {
      return outputSamplingFrequency;
    }

    public void setOutputSamplingFrequency(final float outputSamplingFrequency)
    {
      this.outputSamplingFrequency = outputSamplingFrequency;
    }
  }

  private MatroskaAudioTrack audio = null;

  /**
   * Converts the Track to String form
   * 
   * @return String form of MatroskaFileTrack data
   */
  @Override
  public String toString()
  {
    String s = new String();

    s += "\t\t" + "TrackNo: " + getTrackNo() + "\n";
    s += "\t\t" + "TrackUID: " + getTrackUID() + "\n";
    s += "\t\t" + "TrackType: " + MatroskaDocType.TrackTypeToString(getTrackType()) + "\n";
    s += "\t\t" + "DefaultDuration: " + getDefaultDuration() + "\n";
    s += "\t\t" + "Name: " + getName() + "\n";
    s += "\t\t" + "Language: " + getLanguage() + "\n";
    s += "\t\t" + "CodecID: " + getCodecID() + "\n";
    if (getCodecPrivate() != null)
      s += "\t\t" + "CodecPrivate: " + getCodecPrivate().length + " byte(s)" + "\n";

    if (getTrackType() == MatroskaDocType.track_video)
    {
      s += "\t\t" + "PixelWidth: " + video.getPixelWidth() + "\n";
      s += "\t\t" + "PixelHeight: " + video.getPixelHeight() + "\n";
      s += "\t\t" + "DisplayWidth: " + video.getDisplayWidth() + "\n";
      s += "\t\t" + "DisplayHeight: " + video.getDisplayHeight() + "\n";
    }

    if (getTrackType() == MatroskaDocType.track_audio)
    {
      s += "\t\t" + "SamplingFrequency: " + audio.getSamplingFrequency() + "\n";
      if (audio.getOutputSamplingFrequency() != 0)
        s += "\t\t" + "OutputSamplingFrequency: " + audio.getOutputSamplingFrequency() + "\n";
      s += "\t\t" + "Channels: " + audio.channels + "\n";
      if (audio.bitDepth != 0)
        s += "\t\t" + "BitDepth: " + audio.bitDepth + "\n";
    }

    return s;
  }

  static MatroskaFileTrack fromElement(final Element level2, final DataSource ioDS, final EBMLReader reader)
  {
    // TODO: read all possible elements
    Element level3 = ((MasterElement) level2).readNextChild(reader);
    Element level4 = null;
    final MatroskaFileTrack track = new MatroskaFileTrack();
    System.out.println("Reading track from doc!");
    while (level3 != null)
    {
      if (level3.equals(MatroskaDocType.TrackNumber_Id))
      {
        level3.readData(ioDS);
        track.setTrackNo((int) ((UnsignedIntegerElement) level3).getValue());
      }
      else if (level3.equals(MatroskaDocType.TrackUID_Id))
      {
        level3.readData(ioDS);
        track.setTrackUID(((UnsignedIntegerElement) level3).getValue());

      }
      else if (level3.equals(MatroskaDocType.TrackType_Id))
      {
        level3.readData(ioDS);
        track.setTrackType((byte) ((UnsignedIntegerElement) level3).getValue());

      }
      else if (level3.equals(MatroskaDocType.TrackDefaultDuration_Id))
      {
        level3.readData(ioDS);
        track.setDefaultDuration(((UnsignedIntegerElement) level3).getValue());

      }
      else if (level3.equals(MatroskaDocType.TrackName_Id))
      {
        level3.readData(ioDS);
        track.setName(((StringElement) level3).getValue());

      }
      else if (level3.equals(MatroskaDocType.TrackLanguage_Id))
      {
        level3.readData(ioDS);
        track.setLanguage(((StringElement) level3).getValue());

      }
      else if (level3.equals(MatroskaDocType.TrackCodecID_Id))
      {
        level3.readData(ioDS);
        track.setCodecID(((StringElement) level3).getValue());

      }
      else if (level3.equals(MatroskaDocType.TrackCodecPrivate_Id))
      {
        level3.readData(ioDS);
        track.setCodecPrivate(((BinaryElement) level3).getData());

      }
      else if (level3.equals(MatroskaDocType.TrackVideo_Id))
      {
        level4 = ((MasterElement) level3).readNextChild(reader);
        track.video = new MatroskaVideoTrack();
        while (level4 != null)
        {
          if (level4.equals(MatroskaDocType.PixelWidth_Id))
          {
            level4.readData(ioDS);
            track.video.setPixelWidth((short) ((UnsignedIntegerElement) level4).getValue());

          }
          else if (level4.equals(MatroskaDocType.PixelHeight_Id))
          {
            level4.readData(ioDS);
            track.video.setPixelHeight((short) ((UnsignedIntegerElement) level4).getValue());

          }
          else if (level4.equals(MatroskaDocType.DisplayWidth_Id))
          {
            level4.readData(ioDS);
            track.video.setDisplayWidth((short) ((UnsignedIntegerElement) level4).getValue());

          }
          else if (level4.equals(MatroskaDocType.DisplayHeight_Id))
          {
            level4.readData(ioDS);
            track.video.setDisplayHeight((short) ((UnsignedIntegerElement) level4).getValue());
          }

          level4.skipData(ioDS);
          level4 = ((MasterElement) level3).readNextChild(reader);
        }

      }
      else if (level3.equals(MatroskaDocType.TrackAudio_Id))
      {
        level4 = ((MasterElement) level3).readNextChild(reader);
        track.audio = new MatroskaAudioTrack();
        while (level4 != null)
        {
          if (level4.equals(MatroskaDocType.SamplingFrequency_Id))
          {
            level4.readData(ioDS);
            track.audio.setSamplingFrequency((float) ((FloatElement) level4).getValue());

          }
          else if (level4.equals(MatroskaDocType.OutputSamplingFrequency_Id))
          {
            level4.readData(ioDS);
            track.audio.setOutputSamplingFrequency((float) ((FloatElement) level4).getValue());

          }
          else if (level4.equals(MatroskaDocType.Channels_Id))
          {
            level4.readData(ioDS);
            track.audio.channels = (short) ((UnsignedIntegerElement) level4).getValue();

          }
          else if (level4.equals(MatroskaDocType.BitDepth_Id))
          {
            level4.readData(ioDS);
            track.audio.bitDepth = (byte) ((UnsignedIntegerElement) level4).getValue();
          }

          level4.skipData(ioDS);
          level4 = ((MasterElement) level3).readNextChild(reader);
        }

      }
      level3.skipData(ioDS);
      level3 = ((MasterElement) level2).readNextChild(reader);
    }
    System.out.println("Read track from doc!");
    return track;
  }

  Element toElement()
  {
    final MatroskaDocType doc = MatroskaDocType.obj;
    final MasterElement trackEntryElem = (MasterElement) doc.createElement(MatroskaDocType.TrackEntry_Id);

    final UnsignedIntegerElement trackNoElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackNumber_Id);
    trackNoElem.setValue(this.getTrackNo());

    final UnsignedIntegerElement trackUIDElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackUID_Id);
    trackUIDElem.setValue(this.getTrackUID());

    final UnsignedIntegerElement trackTypeElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackType_Id);
    trackTypeElem.setValue(this.getTrackType());

    final UnsignedIntegerElement trackFlagEnabledElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackFlagEnabled_Id);
    trackFlagEnabledElem.setValue(this.isFlagEnabled() ? 1 : 0);

    final UnsignedIntegerElement trackFlagDefaultElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackFlagDefault_Id);
    trackFlagDefaultElem.setValue(this.isFlagDefault() ? 1 : 0);

    final UnsignedIntegerElement trackFlagForcedElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackFlagForced_Id);
    trackFlagForcedElem.setValue(this.isFlagForced() ? 1 : 0);

    final UnsignedIntegerElement trackFlagLacingElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackFlagLacing_Id);
    trackFlagLacingElem.setValue(this.isFlagLacing() ? 1 : 0);

    final UnsignedIntegerElement trackMinCacheElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackMinCache_Id);
    trackMinCacheElem.setValue(this.getMinCache());

    final UnsignedIntegerElement trackMaxBlockAddIdElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackMaxBlockAdditionID_Id);
    trackMaxBlockAddIdElem.setValue(this.getMaxBlockAdditionalId());

    final StringElement trackNameElem = (StringElement) doc.createElement(MatroskaDocType.TrackName_Id);
    trackNameElem.setValue(this.getName());

    final StringElement trackLangElem = (StringElement) doc.createElement(MatroskaDocType.TrackLanguage_Id);
    trackLangElem.setValue(this.getLanguage());

    final StringElement trackCodecIDElem = (StringElement) doc.createElement(MatroskaDocType.TrackCodecID_Id);
    trackCodecIDElem.setValue(this.getCodecID());

    trackEntryElem.addChildElement(trackNoElem);
    trackEntryElem.addChildElement(trackUIDElem);
    trackEntryElem.addChildElement(trackTypeElem);

    trackEntryElem.addChildElement(trackFlagEnabledElem);
    trackEntryElem.addChildElement(trackFlagDefaultElem);
    trackEntryElem.addChildElement(trackFlagForcedElem);
    trackEntryElem.addChildElement(trackFlagLacingElem);
    trackEntryElem.addChildElement(trackMinCacheElem);
    trackEntryElem.addChildElement(trackMaxBlockAddIdElem);

    // trackEntryElem.addChildElement(trackNameElem); This element is broken. TODO: Add UTF-8 element support.
    trackEntryElem.addChildElement(trackLangElem);
    trackEntryElem.addChildElement(trackCodecIDElem);

    if (codecPrivate != null)
    {
      final BinaryElement trackCodecPrivateElem = (BinaryElement) doc.createElement(MatroskaDocType.TrackCodecPrivate_Id);
      trackCodecPrivateElem.setData(this.getCodecPrivate());
      trackEntryElem.addChildElement(trackCodecPrivateElem);

    }

    final UnsignedIntegerElement trackDefaultDurationElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackDefaultDuration_Id);
    trackDefaultDurationElem.setValue(this.getDefaultDuration());

    final UnsignedIntegerElement trackCodecDecodeAllElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackCodecDecodeAll_Id);
    trackCodecDecodeAllElem.setValue(this.codecDecodeAll ? 1 : 0);

    // final UnsignedIntegerElement trackSeekPrerollElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackSeekPreroll_Id);
    // trackSeekPrerollElem.setValue(this.seekPreroll);

    trackEntryElem.addChildElement(trackDefaultDurationElem);
    trackEntryElem.addChildElement(trackCodecDecodeAllElem);
    // trackEntryElem.addChildElement(trackSeekPrerollElem);

    // Now we add the audio/video dependant sub-elements
    if (this.getTrackType() == MatroskaDocType.track_video)
    {
      final MasterElement trackVideoElem = (MasterElement) doc.createElement(MatroskaDocType.TrackVideo_Id);

      final UnsignedIntegerElement trackVideoPixelWidthElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.PixelWidth_Id);
      trackVideoPixelWidthElem.setValue(this.video.getPixelWidth());

      final UnsignedIntegerElement trackVideoPixelHeightElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.PixelHeight_Id);
      trackVideoPixelHeightElem.setValue(this.video.getPixelHeight());

      final UnsignedIntegerElement trackVideoDisplayWidthElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.DisplayWidth_Id);
      trackVideoDisplayWidthElem.setValue(this.video.getDisplayWidth());

      final UnsignedIntegerElement trackVideoDisplayHeightElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.DisplayHeight_Id);
      trackVideoDisplayHeightElem.setValue(this.video.getDisplayHeight());

      trackVideoElem.addChildElement(trackVideoPixelWidthElem);
      trackVideoElem.addChildElement(trackVideoPixelHeightElem);
      trackVideoElem.addChildElement(trackVideoDisplayWidthElem);
      trackVideoElem.addChildElement(trackVideoDisplayHeightElem);

      trackEntryElem.addChildElement(trackVideoElem);
    }
    else if (this.getTrackType() == MatroskaDocType.track_audio)
    {
      final MasterElement trackAudioElem = (MasterElement) doc.createElement(MatroskaDocType.TrackAudio_Id);

      final UnsignedIntegerElement trackAudioChannelsElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.Channels_Id);
      trackAudioChannelsElem.setValue(this.audio.channels);

      final UnsignedIntegerElement trackAudioBitDepthElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.BitDepth_Id);
      trackAudioBitDepthElem.setValue(this.audio.bitDepth);

      final FloatElement trackAudioSamplingRateElem = (FloatElement) doc.createElement(MatroskaDocType.SamplingFrequency_Id);
      trackAudioSamplingRateElem.setValue(this.audio.getSamplingFrequency());

      final FloatElement trackAudioOutputSamplingFrequencyElem = (FloatElement) doc.createElement(MatroskaDocType.OutputSamplingFrequency_Id);
      trackAudioOutputSamplingFrequencyElem.setValue(this.audio.getOutputSamplingFrequency());

      trackAudioElem.addChildElement(trackAudioChannelsElem);
      trackAudioElem.addChildElement(trackAudioBitDepthElem);
      trackAudioElem.addChildElement(trackAudioSamplingRateElem);
      trackAudioElem.addChildElement(trackAudioOutputSamplingFrequencyElem);

      trackEntryElem.addChildElement(trackAudioElem);
    }
    return trackEntryElem;
  }

  static long writeTracks(final Collection<MatroskaFileTrack> tracks, final DataWriter ioDW)
  {
    final MasterElement tracksElem = (MasterElement) MatroskaDocType.obj.createElement(MatroskaDocType.Tracks_Id);

    for (final MatroskaFileTrack track: tracks)
    {
      tracksElem.addChildElement(track.toElement());
    }

    return tracksElem.writeElement(ioDW);
  }

  public int getTrackNo()
  {
    return trackNo;
  }

  public void setTrackNo(final int trackNo)
  {
    this.trackNo = trackNo;
  }

  public long getTrackUID()
  {
    return trackUID;
  }

  public void setTrackUID(final long trackUID)
  {
    this.trackUID = trackUID;
  }

  public byte getTrackType()
  {
    return trackType;
  }

  public void setTrackType(final byte trackType)
  {
    this.trackType = trackType;
  }

  public boolean isFlagEnabled()
  {
    return flagEnabled;
  }

  public void setFlagEnabled(final boolean flagEnabled)
  {
    this.flagEnabled = flagEnabled;
  }

  public boolean isFlagDefault()
  {
    return flagDefault;
  }

  public void setFlagDefault(final boolean flagDefault)
  {
    this.flagDefault = flagDefault;
  }

  public boolean isFlagForced()
  {
    return flagForced;
  }

  public void setFlagForced(final boolean flagForced)
  {
    this.flagForced = flagForced;
  }

  public boolean isFlagLacing()
  {
    return flagLacing;
  }

  public void setFlagLacing(final boolean flagLacing)
  {
    this.flagLacing = flagLacing;
  }

  public int getMinCache()
  {
    return minCache;
  }

  public void setMinCache(final int minCache)
  {
    this.minCache = minCache;
  }

  public int getMaxBlockAdditionalId()
  {
    return maxBlockAdditionalId;
  }

  public void setMaxBlockAdditionalId(final int maxBlockAdditionalId)
  {
    this.maxBlockAdditionalId = maxBlockAdditionalId;
  }

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public String getLanguage()
  {
    return language;
  }

  public void setLanguage(final String language)
  {
    this.language = language;
  }

  public String getCodecID()
  {
    return codecID;
  }

  public void setCodecID(final String codecID)
  {
    this.codecID = codecID;
  }

  public byte[] getCodecPrivate()
  {
    return codecPrivate;
  }

  public void setCodecPrivate(final byte[] codecPrivate)
  {
    this.codecPrivate = codecPrivate;
  }

  public long getDefaultDuration()
  {
    return defaultDuration;
  }

  public void setDefaultDuration(final long defaultDuration)
  {
    this.defaultDuration = defaultDuration;
  }

  public boolean isCodecDecodeAll()
  {
    return codecDecodeAll;
  }

  public void setCodecDecodeAll(final boolean codecDecodeAll)
  {
    this.codecDecodeAll = codecDecodeAll;
  }

  public int getSeekPreroll()
  {
    return seekPreroll;
  }

  public void setSeekPreroll(final int seekPreroll)
  {
    this.seekPreroll = seekPreroll;
  }

  public MatroskaVideoTrack getVideo()
  {
    return video;
  }

  public void setVideo(final MatroskaVideoTrack video)
  {
    this.video = video;
  }

  public MatroskaAudioTrack getAudio()
  {
    return audio;
  }

  public void setAudio(final MatroskaAudioTrack audio)
  {
    this.audio = audio;
  }
}
