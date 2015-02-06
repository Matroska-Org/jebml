package org.ebml.matroska;

import java.util.Collection;

import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatroskaFileCues
{
  private static final Logger LOG = LoggerFactory.getLogger(MatroskaFileCues.class);
  private MasterElement cues;
  
  public MatroskaFileCues(final long filePointer)
  {
    cues = MatroskaDocTypes.Cues.getInstance();
  }

  public void addCue(final long position, final long timecode, final Collection<Integer> trackNumbers)
  {
    LOG.debug("Adding matroska cue to cues element");
    MasterElement cuePoint = MatroskaDocTypes.CuePoint.getInstance();
    
    UnsignedIntegerElement cueTime = MatroskaDocTypes.CueTime.getInstance();
    cueTime.setValue(timecode);
    
    MasterElement cueTrackPositions = MatroskaDocTypes.CueTrackPositions.getInstance();
    for (Integer trackNumber : trackNumbers)
    {
      UnsignedIntegerElement cueTrack = MatroskaDocTypes.CueTrack.getInstance();
      cueTrack.setValue(trackNumber);
      UnsignedIntegerElement cueClusterPosition =MatroskaDocTypes.CueClusterPosition.getInstance();
      cueClusterPosition.setValue(position);
      
      cueTrackPositions.addChildElement(cueTrack);
      cueTrackPositions.addChildElement(cueClusterPosition);
    }
    cuePoint.addChildElement(cueTime);
    cuePoint.addChildElement(cueTrackPositions);
    cues.addChildElement(cuePoint);
    LOG.debug("Finished adding matroska cue to cues element");
  }

  public Element writeAndReturnElement(final DataWriter ioDW)
  {
    LOG.debug("Writing matroska cues");
    cues.writeElement(ioDW);
    LOG.debug("Done writing matroska cues");
    return cues;
  }
}
