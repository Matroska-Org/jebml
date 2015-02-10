package org.ebml.matroska;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.ebml.BinaryElement;
import org.ebml.EBMLReader;
import org.ebml.Element;
import org.ebml.MasterElement;
import org.ebml.UnsignedIntegerElement;
import org.ebml.io.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MatroskaSimpleBlock
{
  private static final Logger LOG = LoggerFactory.getLogger(MatroskaSimpleBlock.class);

  // Note: this max size based on libmatroska src
  private static final int MAX_LACE_SIZE = 6 * 0xFF;
  private int trackNumber = 0;
  private short timecode = 0;
  private boolean keyFrame = true;
  private MatroskaLaceMode laceMode = MatroskaLaceMode.EBML;
  private boolean invisible = false;
  private boolean discardable = false;
  private final List<MatroskaFileFrame> frames = new ArrayList<>();
  private int totalSize = 18;

  private long duration = Long.MIN_VALUE;

  public MatroskaSimpleBlock()
  {

  }

  public MatroskaSimpleBlock(final long duration)
  {

  }

  static MatroskaSimpleBlock fromElement(final Element level3, final DataSource ioDS, final EBMLReader reader)
  {
    // TODO: make this work.
    return new MatroskaSimpleBlock();
  }

  Element toElement()
  {
    if (isSimpleBlock())
    {
      return toSimpleBlock();
    }
    return toBlockGroup();
  }

  Element toSimpleBlock()
  {
    final BinaryElement blockElem = MatroskaDocTypes.SimpleBlock.getInstance();
    blockElem.setData(createInnerData());
    return blockElem;
  }

  Element toBlockGroup()
  {
    final MasterElement blockGroupElem = MatroskaDocTypes.BlockGroup.getInstance();
    final BinaryElement blockElem = MatroskaDocTypes.Block.getInstance();
    blockElem.setData(createInnerData());
    final UnsignedIntegerElement durationElem = MatroskaDocTypes.BlockDuration.getInstance();
    durationElem.setValue(duration);
    blockGroupElem.addChildElement(blockElem);
    blockGroupElem.addChildElement(durationElem);
    return blockGroupElem;
  }

  private ByteBuffer createInnerData()
  {
    final ByteBuffer buf = ByteBuffer.allocate(totalSize);

    assert trackNumber < 0x4000;
    if (trackNumber < 0x80)
    {
      buf.put((byte) (trackNumber | 0x80));
    }
    else
    {
      buf.put((byte) (trackNumber >> 8 | 0x40));
      buf.put((byte) (trackNumber & 0xFF));
    }

    buf.putShort(timecode);
    final BitSet bs = new BitSet(8);
    bs.set(0, keyFrame);
    bs.set(4, invisible);
    ByteBuffer sizes = null;
    laceMode = pickBestLaceMode();
    // TODO: correctly calculate resultant buffer size for different lace modes
    switch (laceMode)
    {
      case EBML:
        bs.set(5);
        bs.set(6);
        sizes = ebmlEncodeLaceSizes();
        break;
      case XIPH:
        bs.set(6);
        sizes = xiphEncodeLaceSizes();
        break;
      case FIXED:
        sizes = fixedEncodeLaceSizes();
        bs.set(5);
        break;
      case NONE:
        LOG.trace("Lace mode none!");
        break;
      default:
        break;
    }
    bs.set(7, discardable);
    buf.put(bs.toByteArray());
    if (sizes != null)
    {
      buf.put(sizes);
    }
    for (final MatroskaFileFrame frame: frames)
    {
      LOG.trace("Writing frame {}", frame.getData().remaining());
      buf.put(frame.getData());
    }
    buf.flip();
    return buf;
  }

  private MatroskaLaceMode pickBestLaceMode()
  {
    if (frames.size() == 1)
    {
      return MatroskaLaceMode.NONE;
    }
    return laceMode;
  }

  private ByteBuffer fixedEncodeLaceSizes()
  {
    LOG.trace("Encoding fixed lace sizes");
    return ByteBuffer.allocate(1).put((byte) (frames.size() - 1));
  }

  private ByteBuffer xiphEncodeLaceSizes()
  {
    LOG.trace("Encoding xiph lace sizes");
    final ByteBuffer buf = ByteBuffer.allocate(30);
    buf.put((byte) (frames.size() - 1));
    for (int i = 0; i < frames.size() - 1; ++i)
    {
      int tmpSize = frames.get(i).getData().remaining();
      while (tmpSize >= 0xFF)
      {
        buf.put((byte) 0xFF);
        tmpSize -= 0xFF;
      }
      buf.put((byte) tmpSize);
    }
    return buf;
  }

  private ByteBuffer ebmlEncodeLaceSizes()
  {
    LOG.trace("Encoding ebml lace sizes");
    final ByteBuffer buf = ByteBuffer.allocate(30);
    buf.put((byte) (frames.size() - 1));
    for (int i = 0; i < frames.size() - 1; ++i)
    {
      final int tmpSize = frames.get(i).getData().remaining();
      buf.put(Element.makeEbmlCodedSize(tmpSize));
    }
    return buf;
  }

  public long getTimecode()
  {
    return timecode;
  }

  public void setTimecode(final long timecode)
  {
    assert timecode < Short.MAX_VALUE;
    this.timecode = (short) timecode;
  }

  public MatroskaLaceMode getLaceMode()
  {
    return laceMode;
  }

  public void setLaceMode(final MatroskaLaceMode laceMode)
  {
    this.laceMode = laceMode;
  }

  public boolean isInvisible()
  {
    return invisible;
  }

  public void setInvisible(final boolean invisible)
  {
    this.invisible = invisible;
  }

  public boolean isDiscardable()
  {
    return discardable;
  }

  public void setDiscardable(final boolean discardable)
  {
    this.discardable = discardable;
  }

  public boolean addFrame(final MatroskaFileFrame frame)
  {
    LOG.trace("Adding frame {}", frame.getData().remaining());
    setTimecode(frame.getTimecode());
    setTrackNumber(frame.getTrackNo());
    totalSize += frame.getData().remaining();
    frames.add(frame);
    if (frame.getDuration() != Long.MIN_VALUE)
    {
      duration = frame.getDuration();
    }

    if (frame.getData().remaining() > MAX_LACE_SIZE)
    {
      laceMode = MatroskaLaceMode.NONE;
      return false;
    }
    totalSize += 4;

    return !isSimpleBlock() || !(laceMode.equals(MatroskaLaceMode.NONE) || frames.size() > 8);
  }

  public int getTrackNumber()
  {
    return trackNumber;
  }

  public void setTrackNumber(final int trackNumber)
  {
    this.trackNumber = trackNumber;
  }

  public boolean isKeyFrame()
  {
    return keyFrame;
  }

  public void setKeyFrame(final boolean keyFrame)
  {
    this.keyFrame = keyFrame;
  }

  public long getDuration()
  {
    return duration;
  }

  public void setDuration(final long duration)
  {
    this.duration = duration;
  }

  public boolean isSimpleBlock()
  {
    return duration == Long.MIN_VALUE;
  }
}
