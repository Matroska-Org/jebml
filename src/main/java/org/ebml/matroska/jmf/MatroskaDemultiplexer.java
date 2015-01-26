package org.ebml.matroska.jmf;

// CSOFF: AvoidStarImport
import org.ebml.matroska.*;

import java.io.*;
import java.util.*;
import javax.media.*;
import javax.media.protocol.*;
import com.sun.media.MimeManager;

// CSON: AvoidStarImport
/**
 * <p>
 * Title: JMF Matroska Demultiplexer
 * </p>
 * <p>
 * Description: A Matroska Demultiplexer for JMF (Java Media Framework)
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004 Jory Stone <jcsston@toughguy.net>
 * </p>
 * 
 * @author jcsston
 * @version 0.0.1
 */

// org.ebml.matroska.jmf.MatroskaDemultiplexer
public class MatroskaDemultiplexer implements Demultiplexer
{
  protected static ContentDescriptor[] supportedFormat = new ContentDescriptor[] {new ContentDescriptor("video.x_matroska"),
      new ContentDescriptor("audio.x_matroska") };
  protected boolean seekable = false;
  protected long lastTimecode = 0;

  protected MatroskaFile file = null;
  protected PullSourceStream stream = null;
  protected DataSource source = null;
  protected SourceStream[] streams = null;

  // Reg function
  public static void main(final String[] args)
  {
    try
    {
      // MatroskaDemultiplexer
      final String pluginName = "org.ebml.matroska.jmf.MatroskaDemultiplexer";
      final int pluginType = PlugInManager.DEMULTIPLEXER;

      boolean install = true;
      if (args.length > 0)
      {
        if (args[0].trim().compareToIgnoreCase("/u") == 0)
        {
          // Uninstall instead
          install = false;
        }
        else
        {
          // Display usage
          System.out.println("Usage:");
          System.out.println("\t    \t Install");
          System.out.println("\t /u \t Uninstall");
          return;
        }
      }

      boolean ret = true;
      if (install == true)
      {
        System.out.println(
            "Adding " + pluginName + " to the plug-in registry...");
        // Add the this plug-in to the plug-in registry
        ret = PlugInManager.addPlugIn(
                                      pluginName,
                                      new ContentDescriptor[] {new ContentDescriptor("video/x_matroska"),
                                          new ContentDescriptor("audio/x_matroska") }
                                      ,
                                      null,
                                      pluginType);
        if (ret == false)
        {
          System.out.println(
              "Failed, PlugInManager.addPlugIn() returned false.");
        }
        ret = MimeManager.addMimeType("mkv", "video/x_matroska");
        if (ret == false)
        {
          System.out.println(
              "Failed, MimeManager.addMimeType() returned false.");
        }
        ret = MimeManager.addMimeType("mka", "audio/x_matroska");
        if (ret == false)
        {
          System.out.println(
              "Failed, MimeManager.addMimeType() returned false.");
        }

      }
      else
      {
        // Uninstall
        System.out.println(
            "Removing " + pluginName + " from the plug-in registry...");
        // Remove the this plug-in from the plug-in registry
        ret = PlugInManager.removePlugIn(pluginName, pluginType);
        if (ret == false)
        {
          System.out.println(
              "Failed, PlugInManager.removePlugIn() returned false.");
        }
        ret = MimeManager.removeMimeType("mkv");
        if (ret == false)
        {
          System.out.println(
              "Failed, MimeManager.removeMimeType() returned false.");
        }
        ret = MimeManager.removeMimeType("mka");
        if (ret == false)
        {
          System.out.println(
              "Failed, MimeManager.removeMimeType() returned false.");
        }

      }

      // Save the changes to the plug-in registry
      PlugInManager.commit();

      // Save the changes to the mime type registry
      MimeManager.commit();

      System.out.println("Complete");

    }
    catch (final IOException ex)
    {
      ex.printStackTrace();
    }
  }

  @Override
  public ContentDescriptor[] getSupportedInputContentDescriptors()
  {
    System.out.println("MatroskaDemultiplexer.getSupportedInputContentDescriptors()");
    return supportedFormat;
  }

  @Override
  public Track[] getTracks() throws java.io.IOException,
                            javax.media.BadHeaderException
  {
    System.out.println("MatroskaDemultiplexer.getTracks()");
    if (file == null)
    {
      file = new MatroskaFile(new JMFPullSourceStreamDataSource(stream));
      try
      {
        file.readFile();
      }
      catch (final java.lang.RuntimeException ex)
      {
        ex.printStackTrace();
        throw new javax.media.BadHeaderException("MatroskaDemultiplexer: " +
                                                 ex.toString() + ex.getMessage());
      }
      seekable = file.isSeekable();
    }
    final ArrayList<MatroskaDemultiplexerTrack> demuxTracks = new ArrayList<MatroskaDemultiplexerTrack>();
    final MatroskaFileTrack[] fileTracks = file.getTrackList();
    for (int t = 0; t < fileTracks.length; t++)
    {
      final MatroskaDemultiplexerTrack track = new MatroskaDemultiplexerTrack(file, fileTracks[t].getTrackNo());
      if (track.isEnabled())
      {
        demuxTracks.add(track);
      }
    }

    if (demuxTracks.size() == 0)
    {
      throw new javax.media.BadHeaderException("MatroskaDemultiplexer: No playable tracks found.");
    }

    final Track[] tracks = new Track[demuxTracks.size()];
    for (int t = 0; t < demuxTracks.size(); t++)
    {
      tracks[t] = demuxTracks.get(t);
    }

    return tracks;
  }

  @Override
  public boolean isPositionable()
  {
    return seekable;
  }

  @Override
  public boolean isRandomAccess()
  {
    return seekable;
  }

  @Override
  public Time setPosition(final Time parm1, final int parm2)
  {
    System.out.println("MatroskaDemultiplexer.setPosition(parm1 = " + parm1 + ", parm2 = " + parm2 + ")");
    if (!seekable)
    {
      return getMediaTime();
    }
    else
    {
      throw new java.lang.UnsupportedOperationException(
                                                        "Method setPosition() not yet implemented.");
    }
  }

  @Override
  public Time getMediaTime()
  {
    return new Time(lastTimecode);
  }

  @Override
  public Time getDuration()
  {
    return new Time(file.getDuration());
  }

  @Override
  public String getName()
  {
    return "Matroska Demuxer v0.0.1";
  }

  /**
   * Opens the plug-in software or hardware component and acquires necessary resources. If all the needed resources could not be acquired, it throws a
   * ResourceUnavailableException. Data should not be passed into the plug-in without first calling this method.
   */
  @Override
  public void open() throws javax.media.ResourceUnavailableException
  {
    System.out.println("MatroskaDemultiplexer.open()");
    // Not needed
  }

  /**
   * Closes the plug-in component and releases resources. No more data will be accepted by the plug-in after a call to this method. The plug-in can be
   * reinstated after being closed by calling <code>open</code>.
   */
  @Override
  public void close()
  {
    System.out.println("MatroskaDemultiplexer.close()");
    if (source != null)
    {
      try
      {
        source.stop();
        source.disconnect();
      }
      catch (final IOException e)
      {
        System.out.printf("Caught some exception: %s\n", e);
        // Internal error?
      }
      source = null;
    }
  }

  /**
   * This get called when the player/processor is started.
   */
  @Override
  public void start() throws IOException
  {
    System.out.println("MatroskaDemultiplexer.start()");
    if (source != null)
    {
      source.start();
    }
  }

  /**
   * This get called when the player/processor is stopped.
   */
  @Override
  public void stop()
  {
    System.out.println("MatroskaDemultiplexer.stop()");
    if (source != null)
    {
      try
      {
        source.stop();
      }
      catch (final IOException e)
      {
        System.out.printf("Caught some exception: %s\n", e);
        // Internal errors?
      }
    }
  }

  /**
   * Resets the state of the plug-in. Typically at end of media or when media is repositioned.
   */
  @Override
  public void reset()
  {
    // Not needed
  }

  @Override
  public void setSource(final DataSource source) throws java.io.IOException,
                                                javax.media.IncompatibleSourceException
  {
    System.out.println("MatroskaDemultiplexer.setSource(source = " + source + ")");
    if (!(source instanceof PullDataSource))
    {
      throw new IncompatibleSourceException("DataSource not supported: " +
                                            source);
    }
    else
    {
      streams = ((PullDataSource) source).getStreams();
    }

    if (streams == null)
    {
      throw new IOException("Got a null stream from the DataSource");
    }

    if (streams.length == 0)
    {
      throw new IOException("Got a empty stream array from the DataSource");
    }

    this.source = source;
    // this.streams = streams;

    // positionable = (streams[0] instanceof Seekable);
    // seekable = positionable && ((Seekable) streams[0]).isRandomAccess();

    if (!supports(streams))
    {
      throw new IncompatibleSourceException("DataSource not supported: " +
                                            source);
    }
    stream = (PullSourceStream) streams[0];
  }

  /**
   * A Demultiplexer may support pull only or push only or both pull and push streams. Some Demultiplexer may have other requirements. For e.g a
   * quicktime Demultiplexer imposes an additional requirement that isSeekable() and isRandomAccess() be true
   */
  protected boolean supports(final SourceStream[] streams)
  {
    return ((streams[0] != null) && (streams[0] instanceof PullSourceStream));

  }

  /**
   * Not sure what these Control methods do...
   */
  @Override
  public Object[] getControls()
  {
    return new Object[0];
  }

  @Override
  public Object getControl(final String controlType)
  {
    return null;
  }

}
