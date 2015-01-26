package org.ebml;

import java.nio.charset.StandardCharsets;

public class UTF8StringElement extends StringElement
{
  public UTF8StringElement()
  {
    super(StandardCharsets.UTF_8);
  }
}
