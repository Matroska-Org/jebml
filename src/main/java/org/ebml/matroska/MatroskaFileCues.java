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
  private long endOfEbmlHeaderBytePosition;
  
  public MatroskaFileCues(long endOfEbmlHeaderBytePosition)
  {
    this.endOfEbmlHeaderBytePosition = endOfEbmlHeaderBytePosition;
  }

  public void addCue(long positionInFile, long timecodeOfCluster, Collection<Integer> clusterTrackNumbers)
  {
    LOG.debug("Adding matroska cue to cues element at position [{}], using timecode [{}], for track numbers [{}]", positionInFile, timecodeOfCluster, clusterTrackNumbers);

    UnsignedIntegerElement cueTime = MatroskaDocTypes.CueTime.getInstance();
    cueTime.setValue(timecodeOfCluster);
    MasterElement cuePoint = MatroskaDocTypes.CuePoint.getInstance();
    MasterElement cueTrackPositions = createCueTrackPositions(positionInFile, clusterTrackNumbers);
    
    cues.addChildElement(cuePoint);
    cuePoint.addChildElement(cueTime);
    cuePoint.addChildElement(cueTrackPositions);
    
    LOG.debug("Finished adding matroska cue to cues element");
  }

  private MasterElement createCueTrackPositions(final long positionInFile, final Collection<Integer> trackNumbers)
  {
    MasterElement cueTrackPositions = MatroskaDocTypes.CueTrackPositions.getInstance();
    
    for (Integer trackNumber : trackNumbers)
    {
      UnsignedIntegerElement cueTrack = MatroskaDocTypes.CueTrack.getInstance();
      cueTrack.setValue(trackNumber);
      
      UnsignedIntegerElement cueClusterPosition = MatroskaDocTypes.CueClusterPosition.getInstance();
      cueClusterPosition.setValue(getPositionRelativeToSegmentEbmlElement(positionInFile));
      
      cueTrackPositions.addChildElement(cueTrack);
      cueTrackPositions.addChildElement(cueClusterPosition);
    }
    return cueTrackPositions;
  }
  
  public Element write(DataWriter ioDW, MatroskaFileMetaSeek metaSeek)
  {
    long currentBytePositionInFile = ioDW.getFilePointer();
    LOG.debug("Writing matroska cues at file byte position [{}]", currentBytePositionInFile);
    long numberOfBytesInCueData = cues.writeElement(ioDW);
    LOG.debug("Done writing matroska cues, number of bytes was [{}]", numberOfBytesInCueData);
    
    metaSeek.addIndexedElement(cues, getPositionRelativeToSegmentEbmlElement(currentBytePositionInFile));
    
    return cues;
  }

  private long getPositionRelativeToSegmentEbmlElement(long currentBytePositionInFile)
  {
    return currentBytePositionInFile - endOfEbmlHeaderBytePosition;
  }
}
