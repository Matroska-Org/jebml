package org.ebml.matroska;

public enum MatroskaLaceMode
{
  NONE(0),
  XIPH(1),
  EBML(3),
  FIXED(2);

  private final int representation;

  private MatroskaLaceMode(final int representation)
  {
    this.representation = representation;
  }

  public int getRepresentation()
  {
    return representation;
  }

  static MatroskaLaceMode fromRepresentation(final int representation)
  {
    for (final MatroskaLaceMode mode: values())
    {
      if (mode.representation == representation)
      {
        return mode;
      }
    }
    return null;
  }
}
