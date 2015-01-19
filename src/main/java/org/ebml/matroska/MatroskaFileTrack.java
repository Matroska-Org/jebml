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
  public short TrackNo;
  public long TrackUID;
  public byte TrackType;
  public boolean flagEnabled = true;
  public boolean flagDefault = true;
  public boolean flagForced = false;
  public boolean flagLacing = true;
  public int minCache = 0;
  public int maxBlockAdditionalId = 0;
  public String Name;
  public String Language;
  public String CodecID;
  public byte[] CodecPrivate;
  public long DefaultDuration;
  public boolean codecDecodeAll = true;
  public int seekPreroll = 0;

  public static class MatroskaVideoTrack
  {
    public short PixelWidth;
    public short PixelHeight;
    public short DisplayWidth;
    public short DisplayHeight;
  }

  public MatroskaVideoTrack video = null;

  public static class MatroskaAudioTrack
  {
    public float SamplingFrequency;
    public float OutputSamplingFrequency;
    public short Channels;
    public byte BitDepth;
  }

  public MatroskaAudioTrack audio = null;

  /**
   * Converts the Track to String form
   * 
   * @return String form of MatroskaFileTrack data
   */
  @Override
  public String toString()
  {
    String s = new String();

    s += "\t\t" + "TrackNo: " + TrackNo + "\n";
    s += "\t\t" + "TrackUID: " + TrackUID + "\n";
    s += "\t\t" + "TrackType: " + MatroskaDocType.TrackTypeToString(TrackType) + "\n";
    s += "\t\t" + "DefaultDuration: " + DefaultDuration + "\n";
    s += "\t\t" + "Name: " + Name + "\n";
    s += "\t\t" + "Language: " + Language + "\n";
    s += "\t\t" + "CodecID: " + CodecID + "\n";
    if (CodecPrivate != null)
      s += "\t\t" + "CodecPrivate: " + CodecPrivate.length + " byte(s)" + "\n";

    if (TrackType == MatroskaDocType.track_video)
    {
      s += "\t\t" + "PixelWidth: " + video.PixelWidth + "\n";
      s += "\t\t" + "PixelHeight: " + video.PixelHeight + "\n";
      s += "\t\t" + "DisplayWidth: " + video.DisplayWidth + "\n";
      s += "\t\t" + "DisplayHeight: " + video.DisplayHeight + "\n";
    }

    if (TrackType == MatroskaDocType.track_audio)
    {
      s += "\t\t" + "SamplingFrequency: " + audio.SamplingFrequency + "\n";
      if (audio.OutputSamplingFrequency != 0)
        s += "\t\t" + "OutputSamplingFrequency: " + audio.OutputSamplingFrequency + "\n";
      s += "\t\t" + "Channels: " + audio.Channels + "\n";
      if (audio.BitDepth != 0)
        s += "\t\t" + "BitDepth: " + audio.BitDepth + "\n";
    }

    return s;
  }

  static MatroskaFileTrack fromElement(final Element level2, final DataSource ioDS, final EBMLReader reader)
  {
    Element level3 = ((MasterElement) level2).readNextChild(reader);
    Element level4 = null;
    final MatroskaFileTrack track = new MatroskaFileTrack();
    level3 = ((MasterElement) level2).readNextChild(reader);

    while (level3 != null)
    {
      if (level3.equals(MatroskaDocType.TrackNumber_Id))
      {
        level3.readData(ioDS);
        track.TrackNo = (short) ((UnsignedIntegerElement) level3).getValue();

      }
      else if (level3.equals(MatroskaDocType.TrackUID_Id))
      {
        level3.readData(ioDS);
        track.TrackUID = ((UnsignedIntegerElement) level3).getValue();

      }
      else if (level3.equals(MatroskaDocType.TrackType_Id))
      {
        level3.readData(ioDS);
        track.TrackType = (byte) ((UnsignedIntegerElement) level3).getValue();

      }
      else if (level3.equals(MatroskaDocType.TrackDefaultDuration_Id))
      {
        level3.readData(ioDS);
        track.DefaultDuration = ((UnsignedIntegerElement) level3).getValue();

      }
      else if (level3.equals(MatroskaDocType.TrackName_Id))
      {
        level3.readData(ioDS);
        track.Name = ((StringElement) level3).getValue();

      }
      else if (level3.equals(MatroskaDocType.TrackLanguage_Id))
      {
        level3.readData(ioDS);
        track.Language = ((StringElement) level3).getValue();

      }
      else if (level3.equals(MatroskaDocType.TrackCodecID_Id))
      {
        level3.readData(ioDS);
        track.CodecID = ((StringElement) level3).getValue();

      }
      else if (level3.equals(MatroskaDocType.TrackCodecPrivate_Id))
      {
        level3.readData(ioDS);
        track.CodecPrivate = ((BinaryElement) level3).getData();

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
            track.video.PixelWidth = (short) ((UnsignedIntegerElement) level4).getValue();

          }
          else if (level4.equals(MatroskaDocType.PixelHeight_Id))
          {
            level4.readData(ioDS);
            track.video.PixelHeight = (short) ((UnsignedIntegerElement) level4).getValue();

          }
          else if (level4.equals(MatroskaDocType.DisplayWidth_Id))
          {
            level4.readData(ioDS);
            track.video.DisplayWidth = (short) ((UnsignedIntegerElement) level4).getValue();

          }
          else if (level4.equals(MatroskaDocType.DisplayHeight_Id))
          {
            level4.readData(ioDS);
            track.video.DisplayHeight = (short) ((UnsignedIntegerElement) level4).getValue();
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
            track.audio.SamplingFrequency = (float) ((FloatElement) level4).getValue();

          }
          else if (level4.equals(MatroskaDocType.OutputSamplingFrequency_Id))
          {
            level4.readData(ioDS);
            track.audio.OutputSamplingFrequency = (float) ((FloatElement) level4).getValue();

          }
          else if (level4.equals(MatroskaDocType.Channels_Id))
          {
            level4.readData(ioDS);
            track.audio.Channels = (short) ((UnsignedIntegerElement) level4).getValue();

          }
          else if (level4.equals(MatroskaDocType.BitDepth_Id))
          {
            level4.readData(ioDS);
            track.audio.BitDepth = (byte) ((UnsignedIntegerElement) level4).getValue();
          }

          level4.skipData(ioDS);
          level4 = ((MasterElement) level3).readNextChild(reader);
        }

      }
      level3.skipData(ioDS);
      level3 = ((MasterElement) level2).readNextChild(reader);
    }
    return track;
  }

  Element toElement()
  {
    final MatroskaDocType doc = MatroskaDocType.obj;
    final MasterElement trackEntryElem = (MasterElement) doc.createElement(MatroskaDocType.TrackEntry_Id);

    final UnsignedIntegerElement trackNoElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackNumber_Id);
    trackNoElem.setValue(this.TrackNo);

    final UnsignedIntegerElement trackUIDElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackUID_Id);
    trackUIDElem.setValue(this.TrackUID);

    final UnsignedIntegerElement trackTypeElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackType_Id);
    trackTypeElem.setValue(this.TrackType);

    final UnsignedIntegerElement trackFlagEnabledElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackType_Id);
    trackFlagEnabledElem.setValue(this.flagEnabled ? 1 : 0);

    final UnsignedIntegerElement trackFlagDefaultElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackType_Id);
    trackFlagDefaultElem.setValue(this.flagDefault ? 1 : 0);

    final UnsignedIntegerElement trackFlagForcedElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackType_Id);
    trackFlagForcedElem.setValue(this.flagForced ? 1 : 0);

    final UnsignedIntegerElement trackFlagLacingElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackType_Id);
    trackFlagLacingElem.setValue(this.flagLacing ? 1 : 0);

    final UnsignedIntegerElement trackMinCacheElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackType_Id);
    trackMinCacheElem.setValue(this.minCache);

    final UnsignedIntegerElement trackMaxBlockAddIdElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackType_Id);
    trackMaxBlockAddIdElem.setValue(this.maxBlockAdditionalId);

    final StringElement trackNameElem = (StringElement) doc.createElement(MatroskaDocType.TrackName_Id);
    trackNameElem.setValue(this.Name);

    final StringElement trackLangElem = (StringElement) doc.createElement(MatroskaDocType.TrackLanguage_Id);
    trackLangElem.setValue(this.Language);

    final StringElement trackCodecIDElem = (StringElement) doc.createElement(MatroskaDocType.TrackCodecID_Id);
    trackCodecIDElem.setValue(this.CodecID);

    final BinaryElement trackCodecPrivateElem = (BinaryElement) doc.createElement(MatroskaDocType.TrackCodecPrivate_Id);
    trackCodecPrivateElem.setData(this.CodecPrivate);

    final UnsignedIntegerElement trackDefaultDurationElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackDefaultDuration_Id);
    trackDefaultDurationElem.setValue(this.DefaultDuration);

    final UnsignedIntegerElement trackCodecDecodeAllElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackType_Id);
    trackCodecDecodeAllElem.setValue(this.codecDecodeAll ? 1 : 0);

    final UnsignedIntegerElement trackSeekPrerollElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.TrackType_Id);
    trackSeekPrerollElem.setValue(this.seekPreroll);

    trackEntryElem.addChildElement(trackNoElem);
    trackEntryElem.addChildElement(trackUIDElem);
    trackEntryElem.addChildElement(trackTypeElem);
    trackEntryElem.addChildElement(trackFlagEnabledElem);
    trackEntryElem.addChildElement(trackFlagDefaultElem);
    trackEntryElem.addChildElement(trackFlagForcedElem);
    trackEntryElem.addChildElement(trackFlagLacingElem);
    trackEntryElem.addChildElement(trackMinCacheElem);
    trackEntryElem.addChildElement(trackMaxBlockAddIdElem);
    trackEntryElem.addChildElement(trackNameElem);
    trackEntryElem.addChildElement(trackLangElem);
    trackEntryElem.addChildElement(trackCodecIDElem);
    trackEntryElem.addChildElement(trackCodecPrivateElem);
    trackEntryElem.addChildElement(trackDefaultDurationElem);
    trackEntryElem.addChildElement(trackCodecDecodeAllElem);
    trackEntryElem.addChildElement(trackSeekPrerollElem);

    // Now we add the audio/video dependant sub-elements
    if (this.TrackType == MatroskaDocType.track_video)
    {
      final MasterElement trackVideoElem = (MasterElement) doc.createElement(MatroskaDocType.TrackVideo_Id);

      final UnsignedIntegerElement trackVideoPixelWidthElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.PixelWidth_Id);
      trackVideoPixelWidthElem.setValue(this.video.PixelWidth);

      final UnsignedIntegerElement trackVideoPixelHeightElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.PixelHeight_Id);
      trackVideoPixelHeightElem.setValue(this.video.PixelHeight);

      final UnsignedIntegerElement trackVideoDisplayWidthElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.DisplayWidth_Id);
      trackVideoDisplayWidthElem.setValue(this.video.DisplayWidth);

      final UnsignedIntegerElement trackVideoDisplayHeightElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.DisplayHeight_Id);
      trackVideoDisplayHeightElem.setValue(this.video.DisplayHeight);

      trackVideoElem.addChildElement(trackVideoPixelWidthElem);
      trackVideoElem.addChildElement(trackVideoPixelHeightElem);
      trackVideoElem.addChildElement(trackVideoDisplayWidthElem);
      trackVideoElem.addChildElement(trackVideoDisplayHeightElem);

      trackEntryElem.addChildElement(trackVideoElem);
    }
    else if (this.TrackType == MatroskaDocType.track_audio)
    {
      final MasterElement trackAudioElem = (MasterElement) doc.createElement(MatroskaDocType.TrackVideo_Id);

      final UnsignedIntegerElement trackAudioChannelsElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.Channels_Id);
      trackAudioChannelsElem.setValue(this.audio.Channels);

      final UnsignedIntegerElement trackAudioBitDepthElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.BitDepth_Id);
      trackAudioBitDepthElem.setValue(this.audio.BitDepth);

      final FloatElement trackAudioSamplingRateElem = (FloatElement) doc.createElement(MatroskaDocType.SamplingFrequency_Id);
      trackAudioSamplingRateElem.setValue(this.audio.SamplingFrequency);

      final FloatElement trackAudioOutputSamplingFrequencyElem = (FloatElement) doc.createElement(MatroskaDocType.OutputSamplingFrequency_Id);
      trackAudioOutputSamplingFrequencyElem.setValue(this.audio.OutputSamplingFrequency);

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
}
