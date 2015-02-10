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
  private MasterElement cues = MatroskaDocTypes.Cues.getInstance();

  public void addCue(final long position, final long timecode, final Collection<Integer> trackNumbers)
  {
    LOG.debug("Adding matroska cue to cues element at position [{}], using timecode [{}], for track numbers [{}]", position, timecode, trackNumbers);

    UnsignedIntegerElement cueTime = MatroskaDocTypes.CueTime.getInstance();
    cueTime.setValue(timecode);
    MasterElement cuePoint = MatroskaDocTypes.CuePoint.getInstance();
    MasterElement cueTrackPositions = createCueTrackPositions(position, trackNumbers);
    
    cues.addChildElement(cuePoint);
    cuePoint.addChildElement(cueTime);
    cuePoint.addChildElement(cueTrackPositions);
    
    LOG.debug("Finished adding matroska cue to cues element");
  }

  private MasterElement createCueTrackPositions(final long position, final Collection<Integer> trackNumbers)
  {
    MasterElement cueTrackPositions = MatroskaDocTypes.CueTrackPositions.getInstance();
    
    for (Integer trackNumber : trackNumbers)
    {
      UnsignedIntegerElement cueTrack = MatroskaDocTypes.CueTrack.getInstance();
      cueTrack.setValue(trackNumber);
      
      UnsignedIntegerElement cueClusterPosition = MatroskaDocTypes.CueClusterPosition.getInstance();
      cueClusterPosition.setValue(position);
      
      cueTrackPositions.addChildElement(cueTrack);
      cueTrackPositions.addChildElement(cueClusterPosition);
    }
    return cueTrackPositions;
  }

  public Element writeAndReturnElement(final DataWriter ioDW)
  {
    LOG.debug("Writing matroska cues at file byte position [{}]", ioDW.getFilePointer());
    long size = cues.writeElement(ioDW);
    LOG.debug("Done writing matroska cues, number of bytes was [{}]", size);
    return cues;
  }
}
