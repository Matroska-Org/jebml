package org.ebml.matroska;

import java.util.ArrayList;

import org.ebml.MasterElement;
import org.ebml.io.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatroskaFileTags
{
  private static final int BLOCK_SIZE = 4096;
  private static final Logger LOG = LoggerFactory.getLogger(MatroskaFileTags.class);

  private final ArrayList<MatroskaFileTagEntry> tags = new ArrayList<>();

  private final long myPosition;

  public MatroskaFileTags(final long position)
  {
    myPosition = position;
  }

  public void addTag(final MatroskaFileTagEntry tag)
  {
    tags.add(tag);
  }

  public long writeTags(final DataWriter ioDW)
  {
    final MasterElement tagsElem = MatroskaDocTypes.Tags.getInstance();

    for (final MatroskaFileTagEntry tag: tags)
    {
      tagsElem.addChildElement(tag.toElement());
    }
    tagsElem.writeElement(ioDW);
    assert BLOCK_SIZE > tagsElem.getTotalSize();
    new VoidElement(BLOCK_SIZE - tagsElem.getTotalSize()).writeElement(ioDW);
    return BLOCK_SIZE;
  }

  public void update(final DataWriter ioDW)
  {
    LOG.info("Updating tags list!");
    final long start = ioDW.getFilePointer();
    ioDW.seek(myPosition);
    writeTags(ioDW);
    ioDW.seek(start);
  }
}
