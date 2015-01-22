package org.ebml.matroska;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.io.DataSource;
import org.ebml.io.FileDataSource;
import org.ebml.io.FileDataWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MatroskaFileWriterTest
{
  private File destination;
  private FileDataWriter ioDW;
  private MatroskaFileTrack testTrack;
  private int timecode = 1337;

  @Before
  public void setUp() throws Exception
  {
    destination = File.createTempFile("test", ".mkv");
    ioDW = new FileDataWriter(destination.getPath());
    testTrack = new MatroskaFileTrack();
    testTrack.setTrackNo(42);
    testTrack.setTrackType(MatroskaDocType.track_subtitle);
    testTrack.setCodecID("some subtitle codec");
    testTrack.setDefaultDuration(33);
  }

  @After
  public void tearDown() throws Exception
  {
    ioDW.close();
    destination.delete();
  }

  @Test
  public void testWrite() throws FileNotFoundException, IOException
  {
    final MatroskaFileWriter writer = new MatroskaFileWriter(ioDW);
    writer.addTrack(testTrack);
    writer.init();
    writer.addFrame(generateFrame("I know a song..."));
    writer.close();

    final FileDataSource inputDataSource = new FileDataSource(destination.getPath());
    final MatroskaFile reader = new MatroskaFile(inputDataSource);
    reader.readFile();
    assertEquals(MatroskaDocType.track_subtitle, reader.getTrackList()[0].getTrackType());
    assertEquals(42, reader.getTrackList()[0].getTrackNo());
    System.out.println(reader.getReport());
  }

  @Test
  public void testDocTraversal() throws FileNotFoundException, IOException
  {
    // Tests that the document produced by the writer can be traversed succesfully.
    final MatroskaFileWriter writer = new MatroskaFileWriter(ioDW);
    writer.addTrack(testTrack);
    writer.init();
    writer.addFrame(generateFrame("I know a song..."));
    writer.close();

    final FileDataSource ioDS = new FileDataSource(destination.getPath());
    final EBMLReader reader = new EBMLReader(ioDS, MatroskaDocType.obj);
    Element level0 = reader.readNextElement();
    while (level0 != null)
    {
      traverseElement(level0, ioDS, reader, 0);
      level0.skipData(ioDS);
      level0 = reader.readNextElement();
    }
  }

  private void traverseElement(final Element levelN, final DataSource ioDS, final EBMLReader reader, final int level)
  {
    if (levelN == null)
    {
      return;
    }
    System.out.println(levelN.getElementType().name);
    if (!(levelN.equals(MatroskaDocType.Void_Id)))
    {
      assertEquals(level, levelN.getElementType().level);
    }
    if (levelN instanceof MasterElement)
    {
      Element levelNPlusOne = ((MasterElement) levelN).readNextChild(reader);
      while (levelNPlusOne != null)
      {
        traverseElement(levelNPlusOne, ioDS, reader, level + 1);
        levelNPlusOne.skipData(ioDS);
        levelNPlusOne = ((MasterElement) levelN).readNextChild(reader);
      }
    }
  }

  private MatroskaFileFrame generateFrame(final String string)
  {
    final MatroskaFileFrame frame = new MatroskaFileFrame();
    frame.setData(string.getBytes(StandardCharsets.UTF_8));
    frame.setTrackNo(1);
    frame.setTimecode(++timecode);
    return frame;
  }
}
