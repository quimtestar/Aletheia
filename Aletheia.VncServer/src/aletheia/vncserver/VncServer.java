/*******************************************************************************
 * Copyright (c) 2021 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package aletheia.vncserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import aletheia.common.AletheiaConstants;

public class VncServer implements Runnable
{
	static class MyProperties extends Properties
	{
		private static final long serialVersionUID = -178997951384029525L;
		private static final String propertiesFileName_Default = "aletheia.vncserver.properties";
		private static final String propertiesFileName = System.getProperty("aletheia.vncserver.properties.file", propertiesFileName_Default);

		private static final String listen_port = "aletheia.vncserver.listen_port";
		private static final String display = "aletheia.vncserver.display";
		private static final String vnc_port = "aletheia.vncserver.vnc_port";
		private static final String geometry = "aletheia.vncserver.geometry";
		private static final String vnc_process_timeout_secs = "aletheia.vncserver.vnc_process_timeout_secs";

		private static final int default_listen_port = 5997;
		private static final String default_display = ":97";
		private static final int default_vnc_port = 5996;
		//private static final String default_geometry = "1400x720";
		private static final String default_geometry = "720x1400";
		private static final int default_vnc_process_timeout_secs = 300;

		private static final Properties defaults;

		static
		{
			defaults = new Properties();
			defaults.setProperty(listen_port, Integer.toString(default_listen_port));
			defaults.setProperty(display, default_display);
			defaults.setProperty(vnc_port, Integer.toString(default_vnc_port));
			defaults.setProperty(geometry, default_geometry);
			defaults.setProperty(vnc_process_timeout_secs, Integer.toString(default_vnc_process_timeout_secs));
		}

		public static String getPropertiesFileName()
		{
			return propertiesFileName;
		}

		private MyProperties()
		{
			super(defaults);
			try
			{
				InputStream is = new FileInputStream(propertiesFileName);
				try
				{
					load(is);
				}
				finally
				{
					is.close();
				}
			}
			catch (IOException e)
			{
			}
		}

		public int getListenPort()
		{
			return Integer.parseInt(getProperty(listen_port));
		}

		public String getDisplay()
		{
			return getProperty(display);
		}

		public int getVncPort()
		{
			return Integer.parseInt(getProperty(vnc_port));
		}

		public String getGeometry()
		{
			return getProperty(geometry);
		}

		public int getVncProcessTimeoutSecs()
		{
			return Integer.parseInt(getProperty(vnc_process_timeout_secs));
		}

	}

	private final static MyProperties properties = new MyProperties();
	private final static String propertiesFileName = MyProperties.getPropertiesFileName();
	private final static int listenPort = properties.getListenPort();
	private final static String display = properties.getDisplay();
	private final static int vncPort = properties.getVncPort();
	private final static String geometry = properties.getGeometry();
	private final static int vncProcessTimeoutSecs = properties.getVncProcessTimeoutSecs();

	private final ServerSocket serverSocket;
	private final ProcessBuilder vncProcessBuilder;
	private final ProcessBuilder aletheiaProcessBuilder;
	private Process vncProcess = null;
	private Process aletheiaProcess = null;

	public VncServer() throws IOException
	{
		this.serverSocket = new ServerSocket(listenPort);
		this.vncProcessBuilder = new ProcessBuilder("Xvnc", display, "-desktop", AletheiaConstants.TITLE, "-rfbport", Integer.toString(vncPort), "-localhost",
				"-geometry", geometry, "-rfbauth", System.getProperty("user.home") + "/.vnc/passwd");
		this.vncProcessBuilder.redirectOutput(Redirect.INHERIT);
		this.vncProcessBuilder.redirectError(Redirect.INHERIT);

		this.aletheiaProcessBuilder = new ProcessBuilder("java", "-cp", System.getProperty("java.class.path"),
				"-Daletheia.simple.properties.file=" + propertiesFileName, "aletheia.gui.app.SimpleAletheiaGUI");
		this.aletheiaProcessBuilder.environment().put("DISPLAY", display);
		this.aletheiaProcessBuilder.redirectOutput(Redirect.INHERIT);
		this.aletheiaProcessBuilder.redirectError(Redirect.INHERIT);
	}

	private synchronized Process startVncProcess() throws IOException, InterruptedException
	{
		if (vncProcess == null)
		{
			vncProcess = vncProcessBuilder.start();
			boolean exited = vncProcess.waitFor(1, TimeUnit.SECONDS);
			if (exited)
				vncProcess = null;
			else
				aletheiaProcess = aletheiaProcessBuilder.start();
		}
		return vncProcess;
	}

	private synchronized void stopVncProcess() throws InterruptedException
	{
		if (vncProcess != null)
		{
			if (aletheiaProcess != null)
			{
				aletheiaProcess.destroy();
				aletheiaProcess.waitFor();
				aletheiaProcess = null;
			}
			vncProcess.destroy();
			vncProcess.waitFor();
			vncProcess = null;
		}
	}

	private class StreamSpliceThread extends Thread
	{
		private final static int bufferSize = 8192;

		private final InputStream source;
		private final OutputStream target;

		public StreamSpliceThread(String name, InputStream source, OutputStream target)
		{
			super(name);
			this.source = source;
			this.target = target;
		}

		@Override
		public void run()
		{
			byte[] buffer = new byte[bufferSize];
			try
			{
				while (true)
				{
					int n = source.read(buffer);
					if (n <= 0)
						break;
					target.write(buffer, 0, n);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void iterate() throws IOException, InterruptedException
	{
		serverSocket.setSoTimeout(vncProcessTimeoutSecs * 1000);
		try (Socket socket = serverSocket.accept())
		{
			Process vncProcess = startVncProcess();
			if (vncProcess == null)
				return;
			try (Socket vncSocket = new Socket(InetAddress.getLoopbackAddress(), vncPort))
			{
				StreamSpliceThread inputStreamSpliceThread = new StreamSpliceThread("input", socket.getInputStream(), vncSocket.getOutputStream());
				inputStreamSpliceThread.start();
				StreamSpliceThread outputStreamSpliceThread = new StreamSpliceThread("output", vncSocket.getInputStream(), socket.getOutputStream());
				outputStreamSpliceThread.start();
				inputStreamSpliceThread.join();
				vncSocket.shutdownInput();
				outputStreamSpliceThread.join();
			}
		}
		catch (SocketTimeoutException e)
		{
			stopVncProcess();
		}
	}

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				iterate();
			}
			catch (IOException | InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException
	{
		VncServer vncServer = new VncServer();
		Runtime.getRuntime().addShutdownHook(new Thread()
		{

			@Override
			public void run()
			{
				try
				{
					vncServer.stopVncProcess();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		});
		vncServer.run();
	}

}
