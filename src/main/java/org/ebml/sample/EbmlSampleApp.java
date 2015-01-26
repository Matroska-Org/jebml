package org.ebml.sample;

// CSOFF: AvoidStarImport
import java.awt.*;
import javax.swing.*;

// CSON: AvoidStarImport

/**
 * <p>
 * Title: EBMLReader
 * </p>
 * <p>
 * Description: Java Classes to Read EBML Elements
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004 Jory Stone <jcsston@toughguy.net>
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author jcsston
 * @version 1.0
 */

public class EbmlSampleApp
{
  boolean packFrame = false;

  // Construct the application
  public EbmlSampleApp()
  {
    final EbmlSampleAppFrame frame = new EbmlSampleAppFrame();
    // Validate frames that have preset sizes
    // Pack frames that have useful preferred size info, e.g. from their layout
    if (packFrame)
    {
      frame.pack();
    }
    else
    {
      frame.validate();
    }
    // Center the window
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height)
    {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width)
    {
      frameSize.width = screenSize.width;
    }
    frame.setLocation((screenSize.width - frameSize.width) / 2,
                      (screenSize.height - frameSize.height) / 2);
    frame.setVisible(true);
  }

  // Main method
  public static void main(final String[] args)
  {
    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (final Exception e)
    {
      e.printStackTrace();
    }
    new EbmlSampleApp();
  }
}
