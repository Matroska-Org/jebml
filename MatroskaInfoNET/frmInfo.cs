using System;
using System.Drawing;
using System.Collections;
using System.ComponentModel;
using System.Windows.Forms;
using System.Data;

namespace MatroskaInfoNET
{
	/// <summary>
	/// Summary description for Form1.
	/// </summary>
	public class frmInfo : System.Windows.Forms.Form
	{
		private System.Windows.Forms.TextBox textBoxReport;
		private System.Windows.Forms.Button buttonScan;
		private System.Windows.Forms.OpenFileDialog openFileDialog1;
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.Container components = null;

		public frmInfo()
		{
			//
			// Required for Windows Form Designer support
			//
			InitializeComponent();

			//
			// TODO: Add any constructor code after InitializeComponent call
			//
		}

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		protected override void Dispose( bool disposing )
		{
			if( disposing )
			{
				if (components != null) 
				{
					components.Dispose();
				}
			}
			base.Dispose( disposing );
		}

		#region Windows Form Designer generated code
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
      this.textBoxReport = new System.Windows.Forms.TextBox();
      this.buttonScan = new System.Windows.Forms.Button();
      this.openFileDialog1 = new System.Windows.Forms.OpenFileDialog();
      this.SuspendLayout();
      // 
      // textBoxReport
      // 
      this.textBoxReport.Location = new System.Drawing.Point(8, 8);
      this.textBoxReport.Multiline = true;
      this.textBoxReport.Name = "textBoxReport";
      this.textBoxReport.ScrollBars = System.Windows.Forms.ScrollBars.Both;
      this.textBoxReport.Size = new System.Drawing.Size(320, 256);
      this.textBoxReport.TabIndex = 0;
      this.textBoxReport.Text = "";
      // 
      // buttonScan
      // 
      this.buttonScan.Location = new System.Drawing.Point(248, 272);
      this.buttonScan.Name = "buttonScan";
      this.buttonScan.TabIndex = 1;
      this.buttonScan.Text = "Scan";
      this.buttonScan.Click += new System.EventHandler(this.buttonScan_Click);
      // 
      // openFileDialog1
      // 
      this.openFileDialog1.Filter = "Matroska Files (*.mkv, *.mka)|*.mkv;*.mka|All Files (*.*)|*.*";
      // 
      // frmInfo
      // 
      this.AutoScaleBaseSize = new System.Drawing.Size(5, 13);
      this.ClientSize = new System.Drawing.Size(336, 302);
      this.Controls.Add(this.buttonScan);
      this.Controls.Add(this.textBoxReport);
      this.Name = "frmInfo";
      this.Text = "Matroska File Info";
      this.ResumeLayout(false);

    }
		#endregion

		/// <summary>
		/// The main entry point for the application.
		/// </summary>
		[STAThread]
		static void Main() 
		{
			Application.Run(new frmInfo());
		}

		private void buttonScan_Click(object sender, System.EventArgs e)
		{
			if (openFileDialog1.ShowDialog(this) == DialogResult.OK) 
			{
				string filename = openFileDialog1.FileName;
        textBoxReport.AppendText("Scanning file: " + filename);
        textBoxReport.AppendText("\r\n");
        try 
        {
          org.ebml.FileDataSource dataSource = new org.ebml.FileDataSource(filename);
          org.ebml.matroska.MatroskaFile mkFile = new org.ebml.matroska.MatroskaFile(dataSource);
          mkFile.setScanFirstCluster(false);
          mkFile.readFile();
          string report = mkFile.getReport();
          report = report.Replace("\n", "\r\n");
          //report = report.Replace("\t", "  ");
          textBoxReport.AppendText(report);
        } 
        catch (Exception ex) 
        {
          textBoxReport.AppendText("\r\n");
          textBoxReport.AppendText(ex.ToString());
        }


			}
		}
	}
}
