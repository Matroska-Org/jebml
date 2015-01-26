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
import org.ebml.matroska.MatroskaFileTrack.TrackOperation;
import org.ebml.matroska.MatroskaFileTrack.TrackType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatroskaFileWriterTest
{
  private static final Logger LOG = LoggerFactory.getLogger(MatroskaFileWriterTest.class);
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
    testTrack.setTrackType(TrackType.SUBTITLE);
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
    writer.addFrame(generateFrame("I know a song...", 42));
    writer.close();

    final FileDataSource inputDataSource = new FileDataSource(destination.getPath());
    final MatroskaFile reader = new MatroskaFile(inputDataSource);
    reader.readFile();
    assertEquals(TrackType.SUBTITLE, reader.getTrackList()[0].getTrackType());
    assertEquals(42, reader.getTrackList()[0].getTrackNo());
    LOG.info(reader.getReport());
    testDocTraversal();
  }

  @Test
  public void testMultipleTracks() throws Exception
  {
    final MatroskaFileWriter writer = new MatroskaFileWriter(ioDW);
    writer.addTrack(testTrack);
    writer.addFrame(generateFrame("I know a song...", 42));
    final MatroskaFileTrack nextTrack = new MatroskaFileTrack();
    nextTrack.setTrackNo(2);
    nextTrack.setTrackType(TrackType.CONTROL);
    nextTrack.setCodecID("some logo thingy");
    nextTrack.setDefaultDuration(4242);
    writer.addTrack(nextTrack);
    writer.addFrame(generateFrame("that gets on everybody's nerves", 2));

    final MatroskaFileTrack virtualTrack = new MatroskaFileTrack();
    virtualTrack.setTrackNo(3);
    virtualTrack.setTrackType(TrackType.CONTROL);
    virtualTrack.setCodecID("virtual tracky!");
    virtualTrack.setDefaultDuration(1313);
    final TrackOperation operation = new TrackOperation();
    operation.addVirtualTrackPart(42);
    operation.addVirtualTrackPart(2);
    virtualTrack.setOperation(operation);
    writer.addTrack(virtualTrack);

    writer.close();

    final FileDataSource inputDataSource = new FileDataSource(destination.getPath());
    final MatroskaFile reader = new MatroskaFile(inputDataSource);
    reader.readFile();
    assertEquals(TrackType.SUBTITLE, reader.getTrackList()[0].getTrackType());
    assertEquals(42, reader.getTrackList()[0].getTrackNo());
    LOG.info(reader.getReport());
    testDocTraversal();
  }

  @Test
  public void testSilentTrack() throws FileNotFoundException, IOException
  {
    final MatroskaFileWriter writer = new MatroskaFileWriter(ioDW);
    writer.addTrack(testTrack);
    writer.silenceTrack(13);
    writer.addFrame(generateFrame("I know a song...", 42));
    writer.close();

    final FileDataSource inputDataSource = new FileDataSource(destination.getPath());
    final MatroskaFile reader = new MatroskaFile(inputDataSource);
    reader.readFile();
    assertEquals(TrackType.SUBTITLE, reader.getTrackList()[0].getTrackType());
    assertEquals(42, reader.getTrackList()[0].getTrackNo());
    LOG.info(reader.getReport());
    testDocTraversal();
  }

  public void testDocTraversal() throws FileNotFoundException, IOException
  {
    final FileDataSource ioDS = new FileDataSource(destination.getPath());
    final EBMLReader reader = new EBMLReader(ioDS);
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
    LOG.info("Found element {} at level {}", levelN.getElementType().getName(), level);

    final int elemLevel = levelN.getElementType().getLevel();
    if (elemLevel != -1)
    {
      assertEquals(level, elemLevel);
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

  private MatroskaFileFrame generateFrame(final String string, final int trackNo)
  {
    final MatroskaFileFrame frame = new MatroskaFileFrame();
    frame.setData(string.getBytes(StandardCharsets.UTF_8));
    frame.setTrackNo(trackNo);
    frame.setTimecode(++timecode);
    return frame;
  }
}
