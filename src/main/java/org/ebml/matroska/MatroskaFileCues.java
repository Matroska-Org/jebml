package org.ebml.matroska;

import java.util.Collection;

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
    MasterElement cuePoint = MatroskaDocTypes.CuePoint.getInstance();
    
    UnsignedIntegerElement cueTime = MatroskaDocTypes.CueTime.getInstance();
    cueTime.setValue(timecode);
    
    MasterElement cueTrackPositions = MatroskaDocTypes.CueTrackPositions.getInstance();
    for (Integer i : trackNumbers)
    {
      UnsignedIntegerElement cueTrack = MatroskaDocTypes.CueTrack.getInstance();
      cueTrack.setValue(i);
      UnsignedIntegerElement cueClusterPosition =MatroskaDocTypes.CueClusterPosition.getInstance();
      cueClusterPosition.setValue(position);
      
      cueTrackPositions.addChildElement(cueTrack);
      cueTrackPositions.addChildElement(cueClusterPosition);
    }
    cuePoint.addChildElement(cueTime);
    cues.addChildElement(cuePoint);
  }

  public void update(final DataWriter ioDW)
  {
    long start = ioDW.getFilePointer();
    long amount = cues.writeElement(ioDW);
    ioDW.seek(amount + start);
  }
}
