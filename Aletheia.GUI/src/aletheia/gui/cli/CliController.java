/*******************************************************************************
 * Copyright (c) 2014 Quim Testar.
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
 ******************************************************************************/
package aletheia.gui.cli;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.Logger;

import aletheia.gui.cli.command.Command;
import aletheia.gui.cli.command.CommandSource;
import aletheia.gui.cli.command.gui.SimpleMessage;
import aletheia.gui.cli.command.gui.TraceException;
import aletheia.gui.cli.command.statement.NonOperationalCommand;
import aletheia.log4j.LoggerManager;

public class CliController extends Thread
{
	private static final Logger logger = LoggerManager.instance.logger();

	private class MemoryMonitorThread extends Thread
	{
		private static final int interval = 60;
		private static final double threshold = 1;
		private static final boolean cancelActiveCommand = true;

		private boolean shutdown;

		public MemoryMonitorThread()
		{
			super("CliController.MemoryMonitor");
			shutdown = false;
			setDaemon(true);
		}

		@Override
		public void run()
		{
			while (!shutdown)
			{
				try
				{
					ThreadMXBean tb = ManagementFactory.getThreadMXBean();
					long cpu0 = 0;
					for (long id : tb.getAllThreadIds())
						cpu0 += tb.getThreadCpuTime(id);
					long gc0 = 0;
					for (GarbageCollectorMXBean gcb : ManagementFactory.getGarbageCollectorMXBeans())
						gc0 += gcb.getCollectionTime() * 1e6;

					Thread.sleep(interval * 1000);

					long cpu1 = 0;
					for (long id : tb.getAllThreadIds())
						cpu1 += tb.getThreadCpuTime(id);
					long gc1 = 0;
					for (GarbageCollectorMXBean gcb : ManagementFactory.getGarbageCollectorMXBeans())
						gc1 += gcb.getCollectionTime() * 1e6;

					double f = ((double) (gc1 - gc0)) / (cpu1 - cpu0);
					if (f > threshold)
					{
						String message = "Memory overhead factor:" + f;
						logger.warn(message);
						//cliJPanelsMessage("Warning: " + message);
						if (cancelActiveCommand)
						{
							logger.info("Cancelling active command (if any)");
							cancelActiveCommand("by memory monitor");
						}
					}
				}
				catch (InterruptedException e)
				{

				}
			}

		}

		public void shutdown()
		{
			shutdown = true;
			interrupt();
		}
	}

	private final MemoryMonitorThread memoryMonitorThread;

	private final Set<CliJPanel> cliJPanels;

	private final BlockingQueue<Command> queue;

	private class MyUncaughtExceptionHandler implements UncaughtExceptionHandler
	{

		@Override
		public void uncaughtException(Thread t, Throwable e)
		{
			e.printStackTrace();
			logger.fatal(e.getMessage(), e);
			synchronized (CliController.this)
			{
				for (CliJPanel cliJPanel : cliJPanels)
					cliJPanel.getAletheiaJPanel().getAletheiaJFrame().fatalError(e);
			}
		}
	}

	private class CliControllerDummyCommand extends Command
	{

		protected CliControllerDummyCommand(CommandSource from)
		{
			super(from);
		}

		@Override
		public void run() throws Exception
		{
		}
	}

	private boolean shutdown;

	private Command activeCommand;

	public CliController()
	{
		super("CliController");
		this.memoryMonitorThread = new MemoryMonitorThread();
		this.memoryMonitorThread.start();
		this.cliJPanels = new HashSet<>();
		this.queue = new LinkedBlockingQueue<>();
		this.setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
		this.shutdown = false;
		this.activeCommand = null;
	}

	public void addCliJPanel(CliJPanel cliJPanel)
	{
		if (cliJPanel.getController() != this)
			throw new Error("Bad controller");
		cliJPanels.add(cliJPanel);
	}

	public void removeCliJPanel(CliJPanel cliJPanel)
	{
		cliJPanels.remove(cliJPanel);
	}

	protected void command(Command command) throws InterruptedException
	{
		queue.put(command);
	}

	public void shutdown(CliJPanel cliJPanel) throws InterruptedException
	{
		memoryMonitorThread.shutdown();
		shutdown = true;
		cancelActiveCommand("Quitting");
		command(new CliControllerDummyCommand(cliJPanel));
		if (!equals(Thread.currentThread()))
			join();
	}

	public synchronized void cancelActiveCommand(String cause)
	{
		if (activeCommand != null)
			activeCommand.cancel(cause);
	}

	public synchronized void cliJPanelsMessage(String message)
	{

		for (CliJPanel cliJPanel : cliJPanels)
		{
			try
			{
				command(new SimpleMessage(cliJPanel, message));
			}
			catch (InterruptedException e)
			{
				logger.warn("Sending simple message command: " + message, e);
			}
		}
	}

	@Override
	public void run()
	{
		while (!shutdown)
		{
			try
			{
				activeCommand = queue.take();
			}
			catch (InterruptedException e)
			{
				break;
			}
			Exception exception = null;
			try
			{
				if (activeCommand instanceof NonOperationalCommand)
				{
					((NonOperationalCommand) activeCommand).run();
				}
				else
				{
					waitCursor(activeCommand, true);
					try
					{
						activeCommand.run();
					}
					catch (Exception e)
					{
						try
						{
							queue.put(new TraceException(activeCommand.getFrom(), e));
							exception = e;
						}
						catch (InterruptedException e1)
						{
							logger.error(e1.getMessage(), e1);
						}
					}
				}
			}
			finally
			{
				commandDone(activeCommand, exception);
				waitCursor(activeCommand, false);
			}
			synchronized (this)
			{
				activeCommand = null;
			}
		}
	}

	private void commandDone(Command command, Exception exception)
	{
		try
		{
			((CliJPanel) command.getFrom()).commandDone(command, exception);
		}
		catch (InterruptedException e)
		{
		}
	}

	private void waitCursor(Command command, boolean wait)
	{
		((CliJPanel) command.getFrom()).waitCursor(wait);
	}

}
