/*******************************************************************************
 * Copyright (c) 2014, 2023 Quim Testar.
 *
 * This file is part of the Aletheia Proof Assistant.
 *
 * The Aletheia Proof Assistant is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The Aletheia Proof Assistant is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with the Aletheia Proof Assistant.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package aletheia.peertopeer.base.phase;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.apache.logging.log4j.Logger;

import aletheia.log4j.LoggerManager;
import aletheia.peertopeer.base.dialog.Dialog.DialogStreamException;
import aletheia.peertopeer.base.dialog.LoopDialogType;
import aletheia.peertopeer.base.dialog.LoopDialogTypeDialogActive;
import aletheia.peertopeer.base.dialog.LoopDialogTypeDialogPassive;
import aletheia.protocol.ProtocolException;
import aletheia.utilities.aborter.Aborter.AbortException;
import aletheia.utilities.aborter.ListenableAborter;

public abstract class LoopSubPhase<T extends LoopDialogType<?>> extends SubPhase
{
	private final static Logger logger = LoggerManager.instance.logger();

	private final Class<? extends LoopDialogTypeDialogActive<?>> loopDialogTypeDialogActiveClass;
	private final Class<? extends LoopDialogTypeDialogPassive<?>> loopDialogTypeDialogPassiveClass;

	private final Selector selector;

	protected abstract class Command<C extends Command<C>>
	{
		private final T loopDialogType;

		public class Result
		{
			public Result()
			{
			}
		}

		private Result result;
		private boolean cancelled;
		private Throwable cancelCause;

		public Command(T loopDialogType)
		{
			this.loopDialogType = loopDialogType;
			this.result = null;
			this.cancelled = false;
			this.cancelCause = null;
		}

		public T getLoopDialogType()
		{
			return loopDialogType;
		}

		public synchronized void setResult(Result result)
		{
			this.result = result;
			notifyAll();
		}

		public synchronized Result waitForResult(ListenableAborter aborter) throws InterruptedException, AbortException
		{
			if (currentThreadConnection())
				throw new IllegalStateException();
			ListenableAborter.Listener aborterListener = new ListenableAborter.Listener()
			{
				@Override
				public void abort()
				{
					synchronized (Command.this)
					{
						Command.this.notifyAll();
					}
				}
			};
			aborter.addListener(aborterListener);
			try
			{
				while ((result == null) && !cancelled)
				{
					aborter.checkAbort();
					wait();
				}
				return result;
			}
			finally
			{
				aborter.removeListener(aborterListener);
			}
		}

		public synchronized void cancel(Throwable cancelCause)
		{
			this.cancelled = true;
			this.cancelCause = cancelCause;
			notifyAll();
		}

		public final synchronized void cancel()
		{
			cancel(null);
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((loopDialogType == null) ? 0 : loopDialogType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if ((obj == null) || (getClass() != obj.getClass()))
				return false;
			@SuppressWarnings("unchecked")
			Command<?> other = (Command<?>) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (loopDialogType == null)
			{
				if (other.loopDialogType != null)
					return false;
			}
			else if (!loopDialogType.equals(other.loopDialogType))
				return false;
			return true;
		}

		private LoopSubPhase<T> getOuterType()
		{
			return LoopSubPhase.this;
		}

	}

	private final Queue<Command<?>> queue;
	private final Map<Command<?>, Command<?>> enqueued;
	private boolean open;

	public LoopSubPhase(Phase parentPhase, Class<? extends LoopDialogTypeDialogActive<?>> loopDialogTypeDialogActiveClass,
			Class<? extends LoopDialogTypeDialogPassive<?>> loopDialogTypeDialogPassiveClass) throws IOException
	{
		super(parentPhase);
		this.loopDialogTypeDialogActiveClass = loopDialogTypeDialogActiveClass;
		this.loopDialogTypeDialogPassiveClass = loopDialogTypeDialogPassiveClass;
		this.selector = Selector.open();
		getSocketChannel().register(selector, SelectionKey.OP_READ);
		this.queue = new ArrayDeque<>();
		this.enqueued = new HashMap<>();
		this.open = true;
	}

	public static class CancelledCommandException extends Exception
	{
		private static final long serialVersionUID = -2505446593818712327L;

		private CancelledCommandException()
		{
			super();
		}

		private CancelledCommandException(Throwable cause)
		{
			super(cause);
		}

	}

	protected <C extends Command<C>> Command<C>.Result commandResult(Command<C> command, ListenableAborter aborter)
			throws InterruptedException, CancelledCommandException, AbortException
	{
		Command<C> c = command(command);
		Command<C>.Result r = c.waitForResult(aborter);
		if (c.cancelled)
		{
			if (c.cancelCause != null)
				throw new CancelledCommandException(c.cancelCause);
			else
				throw new CancelledCommandException();
		}
		return r;
	}

	protected <C extends Command<C>> Command<C>.Result commandResult(Command<C> command) throws InterruptedException, CancelledCommandException
	{
		try
		{
			return commandResult(command, ListenableAborter.nullListenableAborter);
		}
		catch (AbortException e)
		{
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	protected synchronized <C extends Command<C>> Command<C> command(Command<C> command)
	{
		if (open)
		{
			Command<?> command_ = enqueued.get(command);
			if (command_ == null)
			{
				command_ = command;
				enqueued.put(command, command_);
				queue.offer(command);
				selector.wakeup();
			}
			return (Command<C>) command_;
		}
		else
		{
			command.cancel();
			return command;
		}
	}

	protected void closeQueue()
	{
		Collection<Command<?>> commands = new ArrayList<>();
		synchronized (this)
		{
			open = false;
			commands.addAll(queue);
			queue.clear();
		}
		for (Command<?> c : commands)
		{
			logger.debug("Cancelling command: " + c);
			c.cancel();
		}
	}

	private LoopDialogTypeDialogActive<?> loopDialogTypeDialogActive(T loopDialogType)
			throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		return dialog(loopDialogTypeDialogActiveClass, this, loopDialogType);
	}

	private LoopDialogTypeDialogPassive<?> loopDialogTypeDialogPassive() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		return dialog(loopDialogTypeDialogPassiveClass, this);
	}

	protected abstract boolean serverPhase(T loopDialogType) throws IOException, ProtocolException, InterruptedException, DialogStreamException;

	protected abstract boolean clientPhase(Command<?> command) throws IOException, ProtocolException, InterruptedException, DialogStreamException;

	private boolean loopPhaseIterationPassive() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		if (getDataIn().available() > 0)
		{
			@SuppressWarnings("unchecked")
			T loopDialogType = (T) loopDialogTypeDialogPassive().getReceivedSelection();
			if (!serverPhase(loopDialogType))
				return false;
		}
		return true;
	}

	private boolean loopPhaseIterationActive() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		while (true)
		{
			Command<?> command;
			synchronized (this)
			{
				if (queue.isEmpty())
					break;
				command = queue.peek();
			}
			LoopDialogTypeDialogActive<?> loopDialogTypeDialogActive = loopDialogTypeDialogActive(command.getLoopDialogType());
			if (loopDialogTypeDialogActive.isAcknowledged() || getGender().equals(loopDialogTypeDialogActive.getPrevails()))
			{
				synchronized (this)
				{
					enqueued.remove(command);
					queue.poll();
				}
				if (!clientPhase(command))
					return false;

			}
			else
			{
				@SuppressWarnings("unchecked")
				T loopDialogType = (T) loopDialogTypeDialogActive.getReceivedSelection();
				if (!serverPhase(loopDialogType))
					return false;
			}
		}
		return true;
	}

	protected void loopPhaseInitiate() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		logger.debug("loopPhaseInitiate");
	}

	protected void loopPhaseTerminate() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		logger.debug("loopPhaseTerminate");
		close();
	}

	@Override
	public void run() throws IOException, ProtocolException, InterruptedException, DialogStreamException
	{
		logger.debug(": starting");
		try
		{
			loopPhaseInitiate();
			while (true)
			{
				if (getDataIn().available() <= 0)
					selector.select();
				if (!loopPhaseIterationPassive() || !loopPhaseIterationActive())
					break;
			}
		}
		finally
		{
			loopPhaseTerminate();
			logger.debug(": ending");
		}
	}

	@Override
	public void shutdown(boolean fast)
	{
		super.shutdown(fast);
		if (fast)
			try
			{
				close();
			}
			catch (IOException e)
			{
				logger.error("fast shutdown", e);
			}
		else
			command(makeValedictionCommand());
	}

	protected abstract Command<?> makeValedictionCommand();

	public synchronized boolean isOpen()
	{
		return open;
	}

	@Override
	protected void finalize() throws Throwable
	{
		close();
	}

	protected void closeSelector() throws IOException
	{
		selector.close();
	}

	public void close() throws IOException
	{
		closeSelector();
		closeQueue();
	}

}
