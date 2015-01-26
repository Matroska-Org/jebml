package org.ebml.matroska;

import java.util.Collection;

import org.ebml.Element;

public class MatroskaFileCues
{

  public MatroskaFileCues(final long filePointer)
  {
  }

  public void addCue(final long position, final long timecode, final Collection<Integer> trackNumbers)
  {

  }

  Element toElement()
  {
    // TODO: do the real stuff
    return MatroskaDocTypes.Cues.getInstance();
  }
}
