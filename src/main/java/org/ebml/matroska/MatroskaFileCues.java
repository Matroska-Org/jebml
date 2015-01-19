package org.ebml.matroska;

import java.util.Collection;

import org.ebml.Element;

public class MatroskaFileCues
{

  public MatroskaFileCues(final MatroskaDocType doc, final long filePointer)
  {
  }

  public void addCue(final long position, final long timecode, final Collection<Integer> trackNumbers)
  {

  }

  Element toElement()
  {
    // TODO: find/create appropriate type for cues
    // TODO: do the real stuff
    return MatroskaDocType.obj.createElement(MatroskaDocType.Channels_Id);
  }
}
