package org.ebml.matroska;

import java.util.ArrayList;

import org.ebml.MasterElement;
import org.ebml.io.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatroskaFileTracks
{
  private static final int BLOCK_SIZE = 4096;
  private static final Logger LOG = LoggerFactory.getLogger(MatroskaFileTracks.class);

  private final ArrayList<MatroskaFileTrack> tracks = new ArrayList<>();

  private final long myPosition;

  public MatroskaFileTracks(final long position)
  {
    myPosition = position;
  }

  public void addTrack(final MatroskaFileTrack track)
  {
    tracks.add(track);
  }

  public long writeTracks(final DataWriter ioDW)
  {
    final MasterElement tracksElem = MatroskaDocTypes.Tracks.getInstance();

    for (final MatroskaFileTrack track: tracks)
    {
      tracksElem.addChildElement(track.toElement());
    }
    tracksElem.writeElement(ioDW);
    assert BLOCK_SIZE > tracksElem.getTotalSize();
    new VoidElement(BLOCK_SIZE - tracksElem.getTotalSize()).writeElement(ioDW);
    return BLOCK_SIZE;
  }

  public void update(final DataWriter ioDW)
  {
    LOG.info("Updating tracks list!");
    final long start = ioDW.getFilePointer();
    ioDW.seek(myPosition);
    writeTracks(ioDW);
    ioDW.seek(start);
  }
}
