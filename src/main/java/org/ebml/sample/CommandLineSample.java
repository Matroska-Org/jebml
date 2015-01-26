package org.ebml.sample;

import java.io.FileOutputStream;
import java.io.IOException;

import org.ebml.io.FileDataSource;
import org.ebml.io.FileDataWriter;
import org.ebml.matroska.MatroskaFile;
import org.ebml.matroska.MatroskaFileFrame;
import org.ebml.matroska.MatroskaFileTrack;
import org.ebml.matroska.MatroskaFileWriter;
import org.ebml.matroska.MatroskaFileTrack.TrackType;

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

public class CommandLineSample
{
  public static void main(final String[] args)
  {
    System.out.println("JEBML CommandLineSample - (c) 2004 Jory 'jcsston' Stone <jcsston@toughguy.net>");

    if (args.length < 3)
    {
      System.out.println("Please provide a command and matroska filename on the command-line");
      return;
    }

    try
    {
      final String mode = args[1];
      if (mode.compareTo("-i") == 0)
      {
        readFile(args[2]);
      }
      else if (mode.compareTo("-o") == 0)
      {
        writeFile(args[2]);
      }

    }
    catch (final IOException ex)
    {
      ex.printStackTrace();
    }

  }

  static void readFile(final String filename) throws IOException
  {
    System.out.println("Scanning file: " + filename);
    final long startTime = System.currentTimeMillis();

    final FileDataSource iFS = new FileDataSource(filename);
    final MatroskaFile mF = new MatroskaFile(iFS);
    mF.setScanFirstCluster(true);
    mF.readFile();

    System.out.println(mF.getReport());

    final MatroskaFileTrack track = mF.getTrack(1);
    if (track.getCodecID().compareTo("A_MPEG/L3") == 0)
    {
      System.out.println("Extracting mp3 track");
      final String outputFilename = filename + ".mp3";// + ".wav";
      try (FileOutputStream oFS = new FileOutputStream(outputFilename))
      {
        /*
         * WavLib.WaveFormatEx wfx = new WavLib.WaveFormatEx(); wfx.wFormatTag = 0x55; // MP3 wfx.nSamplesPerSec = (int)track.Audio_SamplingFrequency;
         * if (track.Audio_Channels == 0) { wfx.nChannels = 1; } else { wfx.nChannels = track.Audio_Channels; } if (track.Audio_BitDepth == 0) {
         * wfx.wBitsPerSample = 16; } else { wfx.wBitsPerSample = track.Audio_BitDepth; } //wfx.nBlockAlign = 4;
         * 
         * WavLib.WavWriter writer = new WavLib.WavWriter(); writer.Open(outputFilename, wfx);
         */

        MatroskaFileFrame frame = mF.getNextFrame();
        while (frame != null)
        {
          oFS.write(frame.getData());
          // writer.WriteSampleData(frame.Data, 0, frame.Data.length);
          frame = mF.getNextFrame();
        }
        // writer.Close();
      }
      System.out.println(track.getCodecID());
      System.out.println(track.getTrackType());
    }

    final long endTime = System.currentTimeMillis();
    System.out.println("Scan complete. Took: " + ((endTime - startTime) / 1000.0) + " seconds");
  }

  static void writeFile(final String filename) throws IOException
  {
    System.out.println("Write file: " + filename);

    final FileDataWriter iFW = new FileDataWriter(filename);
    final MatroskaFileWriter mFW = new MatroskaFileWriter(iFW);
    for (int i = 0; i < 5; i++)
    {
      final MatroskaFileTrack track = new MatroskaFileTrack();
      track.setTrackNo((short) i);
      track.setTrackUID(new java.util.Random().nextLong());
      track.setTrackType(TrackType.VIDEO);
      track.setName("Track " + Integer.toString(i));
      track.getVideo().setPixelWidth((short) 320);
      track.getVideo().setPixelHeight((short) 240);
      mFW.addTrack(track);
    }
    mFW.close();
    System.out.println("Write complete");
  }
}
