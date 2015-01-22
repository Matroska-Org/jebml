package org.ebml.matroska;

import org.ebml.BinaryElement;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.VoidElement;
import org.ebml.io.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatroskaFileMetaSeek
{
  private static final Logger LOG = LoggerFactory.getLogger(MatroskaFileMetaSeek.class);
  private static final long BLOCK_RESERVE_SIZE = 256;
  private final long myPosition;
  private final MatroskaDocType doc;
  private final MasterElement seekHeadElem;
  private final VoidElement placeHolderElem;
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
    myPosition = referencePosition;
    seekHeadElem = (MasterElement) doc.createElement(MatroskaDocType.SeekHead_Id);
    placeHolderElem = new VoidElement();
    placeHolderElem.setSize(BLOCK_RESERVE_SIZE - 6);
    seekHeadElem.addChildElement(placeHolderElem);
  }

  /**
   * Writes this object into the data stream at the current position. Note that this method reserves some space for further additions in the stream,
   * if the stream is seekable. Following subsequent additions to the object, the update() method can be used to update the originally written object.
   * 
   * @param ioDW data stream to write to
   * @return length of data written
   */
  public long write(final DataWriter ioDW)
  {
    final long elemLen = seekHeadElem.writeElement(ioDW);
    assert elemLen == BLOCK_RESERVE_SIZE;
    assert (ioDW.getFilePointer() - myPosition) == elemLen;
    return BLOCK_RESERVE_SIZE;
  }

  /**
   * Updates the representation of the object in the datastream to account for added indexed elements
   * 
   * @param ioDW the data stream containing this object
   */
  public void update(final DataWriter ioDW)
  {
    assert ioDW.isSeekable();
    final long pos = ioDW.getFilePointer();
    ioDW.seek(myPosition);
    write(ioDW);
    ioDW.seek(pos);
    LOG.debug("Updated metaseek section.");
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
    LOG.debug("Adding indexed element {} @ {}", element.getElementType().name, filePosition - referencePosition);
    addIndexedElement(element.getType(), filePosition);
  }

  /**
   * Adds elements to the seek index. These should be level 1 objects only. If this object has already been written, you must use the update() method
   * for changes to take effect.
   * 
   * @param element The element itself
   * @param filePosition Position in the data stream where the element has been written.
   * @return
   */
  public void addIndexedElement(final byte[] elementType, final long filePosition)
  {
    LOG.debug("Adding indexed element @ {}", filePosition - referencePosition);
    final MasterElement seekEntryElem = (MasterElement) doc.createElement(MatroskaDocType.SeekEntry_Id);
    final BinaryElement seekEntryIdElem = (BinaryElement) doc.createElement(MatroskaDocType.SeekID_Id);
    seekEntryIdElem.setData(elementType);

    final UnsignedIntegerElement seekEntryPosElem = (UnsignedIntegerElement) doc.createElement(MatroskaDocType.SeekPosition_Id);
    seekEntryPosElem.setValue(filePosition - referencePosition);

    seekEntryElem.addChildElement(seekEntryIdElem);
    seekEntryElem.addChildElement(seekEntryPosElem);

    seekHeadElem.addChildElement(seekEntryElem);
    placeHolderElem.reduceSize(seekEntryElem.getTotalSize());
    seekHeadElem.setSize(BLOCK_RESERVE_SIZE - 6);
  }
}
