/**
 * JEBML - Java library to read/write EBML/Matroska elements.
 * Copyright (C) 2004 Jory Stone <jebml@jory.info>
 * Based on Javatroska (C) 2002 John Cannon <spyder@matroska.org>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.ebml;

import java.nio.ByteBuffer;

import org.ebml.io.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EBMLReader.java
 *
 * Created on November 18, 2002, 4:03 PM
 *
 * @version 1.0
 */

/**
 * <h1>JEBML Intro</h1>
 * <hr>
 * <p>
 * The following are the basic steps of how reading in JEBML works.
 * </p>
 * <ul>
 * <li>1. The EbmlReader class reads the element header (id+size) and looks it up in the supplied DocType class.
 * <li>2. The correct element type (Binary, UInteger, String, etc) is created using the DocType data, BinaryElement is default element type for
 * unknown elements.
 * <li>3. The MatroskaDocType has the ids of all the elements staticly declared. <br>
 * So to easily find out what an element is, you can use some code like the following code
 * <p>
 * <code>
 * Element level1; <br>
 * // ... fill level1 <br>
 * if (level1.equals(MatroskaDocType.SegmentInfo_Id)) { <br>
 *   // Your Code Here <br>
 * } <br>
 * </code>
 * </p>
 * <li>4. To get the actual data for an Element you call the readData method, if you just want to skip it use skipData().
 * <li>5. MasterElements are special, they have the readNextChild() method which returns the next child element, returning null when all the children
 * have been read (it keeps track of the current inputstream position).
 * <li>The usage method for JEBML is very close to libebml/libmatroska.
 * </ul>
 * <hr>
 *
 * Reads EBML elements from a <code>DataSource</code> and looks them up in the provided <code>DocType</code>.
 *
 * @author (c) 2002 John Cannon
 * @author (c) 2004 Jory Stone
 */
public class EBMLReader
{
  private static final Logger LOG = LoggerFactory.getLogger(EBMLReader.class);
  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

  protected DataSource source;

  /**
   * Creates a new <code>EBMLReader</code> reading from the <code>DataSource
   * source</code>. The <code>DocType doc</code> is used to validate the document.
   *
   * @param source DataSource to read from
   * @param doc DocType to use to validate the docment
   */
  public EBMLReader(final DataSource source)
  {
    this.source = source;
  }

  public static String bytesToHex(final byte[] bytes)
  {
    final char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++)
    {
      final int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }

  public Element readNextElement()
  {
    // Read the type.
    final long position = source.getFilePointer();
    final ByteBuffer elementType = readEBMLCodeAsBytes(source);

    if (elementType == null)
    {
      // Failed to read type id
      return null;
    }

    final Element elem = ProtoType.getInstance(elementType);

    if (elem == null)
    {
      return null;
    }
    LOG.trace("Read element {}", elem.getElementType().getName());

    // Read the size.
    final long elementSize = readEBMLCode(source);
    if (elementSize == 0)
    {
      // Zero sized element is valid
      LOG.error("Invalid element size for {}", elem.typeInfo.getName());
    }
    final long end = source.getFilePointer();

    // Set it's size
    elem.setSize(elementSize);
    elem.setHeadersSize(end - position);
    LOG.trace("Read element {} with size {}", elem.typeInfo.getName(), elem.getTotalSize());

    // Setup a buffer for it's data
    // byte[] elementData = new byte[(int)elementSize];
    // Read the data
    // source.read(elementData, 0, elementData.length);
    // Set the data property on the element
    // elem.setData(elementData);

    // System.out.println("EBMLReader.readNextElement() returning element " + elem.getElementType().name + " with size " +
    // Long.toString(elem.getTotalSize()-elementSize)+" "+Long.toString(elementSize));

    // Return the element
    return elem;
  }

  public static ByteBuffer getEBMLCodeAsBytes(final DataSource source)
  {
    // Begin loop with byte set to newly read byte.
    final byte firstByte = source.readByte();
    final int numBytes = readEBMLCodeSize(firstByte);

    if (numBytes == 0)
    {
      LOG.error("Failed to read ebml code size from {}", firstByte);
      // Invalid size
      return null;
    }

    // Setup space to store the bits
    final ByteBuffer buf = ByteBuffer.allocate(numBytes);

    // Clear the 1 at the front of this byte, all the way to the beginning of the size
    buf.put((byte) (firstByte & ((0xFF >>> (numBytes)))));

    if (numBytes > 1)
    {
      // Read the rest of the size.
      source.read(buf);
    }
    buf.flip();
    return buf;
  }

  public static int readEBMLCodeSize(final byte firstByte)
  {
    int numBytes = 0;
    // Begin by counting the bits unset before the first '1'.
    long mask = 0x0080;
    for (int i = 0; i < 8; i++)
    {
      // Start at left, shift to right.
      if ((firstByte & mask) == mask)
      { // One found
        // Set number of bytes in size = i+1 ( we must count the 1 too)
        numBytes = i + 1;
        // exit loop by pushing i out of the limit
        i = 8;
      }
      mask >>>= 1;
    }
    // System.out.printf("Read code size %d\n", numBytes);
    return numBytes;
  }

  /**
   * Reads an (Unsigned) EBML code from the DataSource and encodes it into a long. This size should be cast into an int for actual use as Java only
   * allows upto 32-bit file I/O operations.
   *
   * @return ebml size
   */
  public static long readEBMLCode(final DataSource source)
  {
    // Begin loop with byte set to newly read byte.
    final byte firstByte = source.readByte();
    final int numBytes = readEBMLCodeSize(firstByte);
    LOG.trace("Reading ebml code of {} bytes", numBytes);
    if (numBytes == 0)
    {
      // Invalid size
      return 0;
    }

    // Setup space to store the bits
    final ByteBuffer data = ByteBuffer.allocate(numBytes);

    // Clear the 1 at the front of this byte, all the way to the beginning of the size
    data.put((byte) (firstByte & ((0xFF >>> (numBytes)))));

    if (numBytes > 1)
    {
      // Read the rest of the size.
      source.read(data);
    }
    data.flip();
    return parseEBMLCode(data);
  }

  /**
   * Takes a byte buffer and reads the bytes as an unsigned integer
   * 
   * @param data
   * @return
   */
  public static long parseEBMLCode(final ByteBuffer data)
  {
    if (data == null)
    {
      return 0;
    }
    data.mark();

    // Put this into a long
    long size = 0;
    for (int i = data.remaining() - 1; i >= 0; i--)
    {
      final long n = data.get() & 0xFF;
      size = size | (n << (8 * i));
    }
    data.reset();
    LOG.trace("Parsed ebml code {} as {}", bytesToHex(data.array()), size);
    return size;
  }

  /**
   * Reads an (Unsigned) EBML code from the DataSource and encodes it into a long. This size should be cast into an int for actual use as Java only
   * allows upto 32-bit file I/O operations.
   *
   * @return ebml size
   */
  public static long readEBMLCode(final ByteBuffer source)
  {
    // Begin loop with byte set to newly read byte.
    final byte firstByte = source.get();
    final int numBytes = readEBMLCodeSize(firstByte);
    if (numBytes == 0)
    {
      // Invalid size
      return 0;
    }

    // Setup space to store the bits
    final ByteBuffer data = ByteBuffer.allocate(numBytes);

    // Clear the 1 at the front of this byte, all the way to the beginning of the size
    data.put((byte) (firstByte & ((0xFF >>> (numBytes)))));

    // Read the rest of the size.
    for (int i = 1; i < numBytes; i++)
    {
      data.put(source.get());
    }

    data.flip();
    return parseEBMLCode(data);
  }

  /**
   * Reads an Signed EBML code from the DataSource and encodes it into a long. This size should be cast into an int for actual use as Java only allows
   * upto 32-bit file I/O operations.
   *
   * @return ebml size
   */
  public static long readSignedEBMLCode(final ByteBuffer source)
  {
    // Begin loop with byte set to newly read byte.
    final byte firstByte = source.get();
    final int numBytes = readEBMLCodeSize(firstByte);
    if (numBytes == 0)
    {
      // Invalid size
      return 0;
    }

    // Setup space to store the bits
    final ByteBuffer data = ByteBuffer.allocate(numBytes);

    // Clear the 1 at the front of this byte, all the way to the beginning of the size
    data.put((byte) (firstByte & ((0xFF >>> (numBytes)))));

    // Read the rest of the size.
    for (int i = 1; i < numBytes; i++)
    {
      data.put(source.get());
    }

    data.flip();
    // Put this into a long
    long size = parseEBMLCode(data);

    // Sign it ;)
    if (numBytes == 1)
    {
      size -= 63;

    }
    else if (numBytes == 2)
    {
      size -= 8191;

    }
    else if (numBytes == 3)
    {
      size -= 1048575;

    }
    else if (numBytes == 4)
    {
      size -= 134217727;
    }

    return size;
  }

  /**
   * Reads an Signed EBML code from the DataSource and encodes it into a long. This size should be cast into an int for actual use as Java only allows
   * upto 32-bit file I/O operations.
   *
   * @return ebml size
   */
  public static long readSignedEBMLCode(final DataSource source)
  {

    // Begin loop with byte set to newly read byte.
    final byte firstByte = source.readByte();
    final int numBytes = readEBMLCodeSize(firstByte);
    if (numBytes == 0)
    {
      // Invalid size
      return 0;
    }

    // Setup space to store the bits
    final ByteBuffer data = ByteBuffer.allocate(numBytes);

    // Clear the 1 at the front of this byte, all the way to the beginning of the size
    data.put((byte) (firstByte & ((0xFF >>> (numBytes)))));

    if (numBytes > 1)
    {
      // Read the rest of the size.
      source.read(data);
    }

    data.flip();
    // Put this into a long
    long size = parseEBMLCode(data);

    // Sign it ;)
    if (numBytes == 1)
    {
      size -= 63;

    }
    else if (numBytes == 2)
    {
      size -= 8191;

    }
    else if (numBytes == 3)
    {
      size -= 1048575;

    }
    else if (numBytes == 4)
    {
      size -= 134217727;
    }

    return size;
  }

  /**
   * Reads an EBML code from the DataSource.
   *
   * @return byte array filled with the ebml size, (size bits included)
   */
  public static ByteBuffer readEBMLCodeAsBytes(final DataSource source)
  {
    // Begin loop with byte set to newly read byte.
    final byte firstByte = source.readByte();
    final int numBytes = readEBMLCodeSize(firstByte);

    if (numBytes == 0)
    {
      LOG.error("Failed to read ebml code size from {}", firstByte);
      // Invalid size
      return null;
    }

    // Setup space to store the bits
    final ByteBuffer buf = ByteBuffer.allocate(numBytes);

    buf.put(firstByte);

    if (numBytes > 1)
    {
      // Read the rest of the size.
      source.read(buf);
    }
    buf.flip();
    return buf;
  }

}
