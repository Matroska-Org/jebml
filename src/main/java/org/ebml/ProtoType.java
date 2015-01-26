package org.ebml;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtoType<T extends Element>
{
  private static final Logger LOG = LoggerFactory.getLogger(ProtoType.class);
  private static final HashMap<Long, ProtoType<? extends Element>> CLASS_MAP = new HashMap<>();
  Class<T> clazz;
  private final byte[] type;

  private final String name;
  private final int level;

  public ProtoType(final Class<T> clazz, final String name, final byte[] type, final int level)
  {
    this.clazz = clazz;
    this.type = type;
    this.name = name;
    this.level = level;
    final long codename = EBMLReader.parseEBMLCode(type);
    CLASS_MAP.put(codename, this);
  }

  public T getInstance()
  {
    LOG.trace("Instantiating {}", name);
    try
    {
      final T elem = clazz.newInstance();
      elem.setType(type);
      elem.setElementType(this);
      return elem;
    }
    catch (InstantiationException | IllegalAccessException e)
    {
      LOG.error("Failed to instantiate: this should never happen!", e);
      throw new RuntimeException(e);
    }
  }

  public static Element getInstance(final byte[] type)
  {
    final long codename = EBMLReader.parseEBMLCode(type);
    return CLASS_MAP.get(Long.valueOf(codename)).getInstance();
  }

  public String getName()
  {
    return name;
  }

  public int getLevel()
  {
    return level;
  }

  public byte[] getType()
  {
    return type;
  }

}
