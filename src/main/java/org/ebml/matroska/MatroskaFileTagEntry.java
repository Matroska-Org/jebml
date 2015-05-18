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
package org.ebml.matroska;

import java.util.ArrayList;

public class MatroskaFileTagEntry
{
  public ArrayList<Long> trackUID = new ArrayList<>();
  public ArrayList<Long> chapterUID = new ArrayList<>();
  public ArrayList<Long> attachmentUID = new ArrayList<>();
  public ArrayList<MatroskaFileSimpleTag> simpleTags = new ArrayList<>();

  @Override
  public String toString()
  {
    String s = new String();

    if (trackUID.size() > 0)
    {
      s += "\t\t" + "TrackUID: " + trackUID.toString() + "\n";
    }
    if (chapterUID.size() > 0)
    {
      s += "\t\t" + "ChapterUID: " + chapterUID.toString() + "\n";
    }
    if (attachmentUID.size() > 0)
    {
      s += "\t\t" + "AttachmentUID: " + attachmentUID.toString() + "\n";
    }

    for (int t = 0; t < simpleTags.size(); t++)
    {
      s += simpleTags.get(t).toString(2);
    }

    return s;
  }
}
