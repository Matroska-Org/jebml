package org.ebml.sample;

// CSOFF: AvoidStarImport
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.ebml.io.*;
import org.ebml.matroska.*;
import org.ebml.matroska.util.MatroskaFileFilter;

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

public class EbmlSampleAppFrame extends JFrame
{

  private static final long serialVersionUID = 1L;
  JPanel contentPane;
  JMenuBar jMenuBar1 = new JMenuBar();
  JMenu jMenuFile = new JMenu();
  JMenuItem jMenuFileExit = new JMenuItem();
  BorderLayout borderLayout1 = new BorderLayout();
  JMenuItem jMenuItemOpen = new JMenuItem();
  JFileChooser jFileChooser1 = new JFileChooser();
  JTextArea jTextArea1 = new JTextArea();

  // Construct the frame
  public EbmlSampleAppFrame()
  {
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try
    {
      jbInit();
    }
    catch (final Exception e)
    {
      e.printStackTrace();
    }
  }

  // Component initialization
  private void jbInit() throws Exception
  {
    contentPane = (JPanel) this.getContentPane();
    contentPane.setLayout(borderLayout1);
    this.setSize(new Dimension(400, 300));
    this.setTitle("Ebml Sample App");
    jMenuFile.setText("File");
    jMenuFileExit.setText("Exit");
    jMenuFileExit.addActionListener(new
        EbmlSampleAppFrameJMenuFileExitActionAdapter(this));
    jMenuItemOpen.setText("Open");
    jMenuItemOpen.addActionListener(new
        EbmlSampleAppFrameJMenuItemOpenActionAdapter(this));
    jFileChooser1.setAcceptAllFileFilterUsed(true);
    jFileChooser1.setDialogTitle("Select a Matroska File");
    jFileChooser1.setFileFilter(null);
    jTextArea1.setTabSize(8);
    jMenuFile.add(jMenuItemOpen);
    jMenuFile.add(jMenuFileExit);
    jMenuBar1.add(jMenuFile);
    contentPane.add(jTextArea1, BorderLayout.CENTER);
    this.setJMenuBar(jMenuBar1);
  }

  // File | Exit action performed
  public void jMenuFileExitActionPerformed(final ActionEvent e)
  {
    System.exit(0);
  }

  // Overridden so we can exit when window is closed
  @Override
  protected void processWindowEvent(final WindowEvent e)
  {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING)
    {
      jMenuFileExitActionPerformed(null);
    }
  }

  void jMenuItemOpenActionPerformed(final ActionEvent e)
  {
    try
    {
      jFileChooser1.setFileFilter(new MatroskaFileFilter());

      final int ret = jFileChooser1.showOpenDialog(this);
      if (ret == JFileChooser.APPROVE_OPTION)
      {
        /*
         * FileInputStream inFS1 = new FileInputStream("I:\\videos\\111-Videos_111\\Linkin Park - Crawling-remux.mkvmerge.mp3"); FileOutputStream
         * outFS1 = new FileOutputStream("I:\\videos\\111-Videos_111\\Linkin Park - Crawling-remux.mkvmerge.java.mp3"); byte [] buffer = new byte[64];
         * int len = 64; while (len > 0) { len = inFS1.read(buffer); outFS1.write(buffer, 0, len); }
         */
        // FileOutputStream outFS = new FileOutputStream(jFileChooser1.getSelectedFile() + ".mp3");
        final FileInputStream ioF = new FileInputStream(jFileChooser1.getSelectedFile());
        jTextArea1.append("Scanning file: " + jFileChooser1.getSelectedFile().toString() + "\n");

        final MatroskaFile mF = new MatroskaFile(new InputStreamDataSource(ioF));
        mF.readFile();
        jTextArea1.append(mF.getReport());

        /*
         * MatroskaFile.MatroskaFrame frame = mF.getNextFrame(2); while (frame != null) { frame = mF.getNextFrame(2); if (frame != null)
         * outFS.write(frame.Data); }
         */

        jTextArea1.append("Scan complete.\n");
      }
    }
    catch (final java.io.FileNotFoundException ex)
    {
      jTextArea1.append("File Not Found!\n");
      ex.printStackTrace();
    }
    catch (final java.lang.RuntimeException ex)
    {
      jTextArea1.append("Error: " + ex.toString() + ex.getMessage() + "\"\n");
      ex.printStackTrace();
    }
  }
}

class EbmlSampleAppFrameJMenuFileExitActionAdapter implements ActionListener
{
  EbmlSampleAppFrame adaptee;

  EbmlSampleAppFrameJMenuFileExitActionAdapter(final EbmlSampleAppFrame adaptee)
  {
    this.adaptee = adaptee;
  }

  @Override
  public void actionPerformed(final ActionEvent e)
  {
    adaptee.jMenuFileExitActionPerformed(e);
  }
}

class EbmlSampleAppFrameJMenuItemOpenActionAdapter implements java.awt.event.ActionListener
{
  EbmlSampleAppFrame adaptee;

  EbmlSampleAppFrameJMenuItemOpenActionAdapter(final EbmlSampleAppFrame adaptee)
  {
    this.adaptee = adaptee;
  }

  @Override
  public void actionPerformed(final ActionEvent e)
  {
    adaptee.jMenuItemOpenActionPerformed(e);
  }
}
