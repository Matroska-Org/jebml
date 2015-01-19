package org.ebml.matroska;

import org.ebml.BinaryElement;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataWriter;

public class MatroskaFileMetaSeek
{
  private static final long BLOCK_RESERVE_SIZE = 4096;
  private long myPosition = 0;
  private final MatroskaDocType doc;
  private final MasterElement seekHeadElem;
  private final long referencePosition;

  /**
   * Creates a MetaSeek object to index the positions of various other level 1 objects.
   * 
   * @param doc
   * @param referencePosition The first byte after the first Segment element in the file
   */
  public MatroskaFileMetaSeek(final MatroskaDocType doc, final long referencePosition)
  {
    this.doc = doc;
    this.referencePosition = referencePosition;
    seekHeadElem = (MasterElement) doc.createElement(MatroskaDocType.TrackVideo_Id);
  }

  /**
   * Writes this object into the data stream at the current position. Note that this method reserves some space for further additions in the stream,
   * if the stream is seekable. Following subsequent additions to the object, the update() method can be used to update the originally written object.
   * 
   * @param ioDW data stream to write to
   * @return ending position of the data stream.
   */
  public long write(final DataWriter ioDW)
  {
    myPosition = ioDW.getFilePointer();
    final long end = seekHeadElem.writeElement(ioDW);
    return ioDW.isSeekable() ? ioDW.seek(myPosition + BLOCK_RESERVE_SIZE) : end;
  }

  /**
   * Updates the representation of the object in the datastream to account for added indexed elements
   * 
   * @param ioDW a seekable data stream
   */
  public void update(final DataWriter ioDW)
  {
    assert ioDW.isSeekable();
    final long pos = ioDW.getFilePointer();
    ioDW.seek(myPosition);
    write(ioDW);
    ioDW.seek(pos);
  }

  /**
   * Adds elements to the seek index. These should be level 1 objects only. If this object has already been written, you must use the update() method
   * for changes to take effect.
   * 
   * @param element The element itself
   * @param filePosition Position in the data stream where the element has been written.
   */
  public void addIndexedElement(final Element element, final long filePosition)
  {
    addIndexedElement(element.getType(), filePosition);
  }

  /**
   * Adds elements to the seek index. These should be level 1 objects only. If this object has already been written, you must use the update() method
   * for changes to take effect.
   * 
   * @param element The element itself
   * @param filePosition Position in the data stream where the element has been written.
   */
  public void addIndexedElement(final byte[] elementType, final long filePosition)
  {
    final MasterElement seekEntryElem = (MasterElement) doc.createElement(MatroskaDocType.SeekEntry_Id);
    final BinaryElement seekEntryIdElem = (BinaryElement) doc.createElement(MatroskaDocType.SeekID_Id);
    seekEntryIdElem.setData(elementType);

    final UnsignedIntegerElement seekEntryPosElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.SeekID_Id);
    seekEntryPosElem.setValue(filePosition - referencePosition);

    seekEntryElem.addChildElement(seekEntryPosElem);
    seekEntryElem.addChildElement(seekEntryIdElem);

    seekHeadElem.addChildElement(seekEntryElem);
  }
}
