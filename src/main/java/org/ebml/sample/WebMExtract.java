package org.ebml.sample;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.ebml.io.FileDataSource;
import org.ebml.matroska.MatroskaFile;
import org.ebml.matroska.MatroskaFileFrame;
import org.ebml.matroska.MatroskaFileTrack;

/**
 * <p>
 * Title: JEBML
 * </p>
 * <p>
 * Description: Java Classes to Extract keyframes from WebM Files as WebP Images
 * </p>
 * <p>
 * Copyright: Copyright (c) 2011 Brooss
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author brooss
 * @version 1.0
 */

public final class WebMExtract
{
  private WebMExtract()
  {
  }

  public static void main(final String[] args)
  {
    System.out.println("JEBML WebMExtract");

    if (args.length < 1)
    {
      System.out.println("Please provide a WebM filename on the command-line");
      return;
    }
    try
    {
      readFile(args[0]);

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

    MatroskaFileTrack track = null;
    for (final MatroskaFileTrack t: mF.getTrackList())
    {
      if (t.getCodecID().compareTo("V_VP8") == 0)
      {
        track = t;
      }

    }

    if (track != null)
    {
      MatroskaFileFrame frame = mF.getNextFrame(track.getTrackNo());
      int count = 0;
      while (frame != null)
      {
        if (frame.isKeyFrame())
        {
          System.out.println("Extracting VP8 frame " + count);
          final String outputFilename = filename + "" + count + ".webp"; // + ".wav";
          final FileOutputStream oFS = new FileOutputStream(outputFilename);

          oFS.write("RIFF".getBytes(StandardCharsets.UTF_8));

          writeIntLE(oFS, frame.getData().length + 20 - 8);

          oFS.write("WEBPVP8".getBytes(StandardCharsets.UTF_8));
          oFS.write(0x20);
          writeIntLE(oFS, (frame.getData().length + 20 - 8) - 0xc);

          oFS.write(frame.getData());
        }
        frame = mF.getNextFrame(track.getTrackNo());
        count++;
      }
    }

    final long endTime = System.currentTimeMillis();
    System.out.println("Scan complete. Took: " + ((endTime - startTime) / 1000.0) + " seconds");
  }

  public static void writeIntLE(final FileOutputStream out, final int value)
  {

    try
    {
      out.write(value & 0xFF);
      out.write((value >> 8) & 0xFF);
      out.write((value >> 16) & 0xFF);
      out.write((value >> 24) & 0xFF);
    }
    catch (final IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
